// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.agora.capture.video.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import io.agora.framework.helpers.gles.core.GlUtil;


/**
 * Video Capture Device extension of VideoCapture to provide common functionality
 * for capture using android.hardware.Camera API (deprecated in API 21). For Normal
 * Android devices, it provides functionality for receiving copies of preview
 * frames via Java-allocated buffers.
 **/
@SuppressWarnings("deprecation")
public class VideoCaptureCamera
        extends VideoCapture implements Camera.PreviewCallback {
    private static final String TAG = VideoCaptureCamera.class.getSimpleName();
    private static final int NUM_CAPTURE_BUFFERS = 3;

    private int mExpectedFrameSize;

    private Camera mCamera;
    // Lock to mutually exclude execution of OnPreviewFrame() and {start/stop}Capture().
    private ReentrantLock mPreviewBufferLock = new ReentrantLock();
    private final Object mCameraStateLock = new Object();
    private volatile CameraState mCameraState = CameraState.STOPPED;
    private volatile boolean mPendingStartRequest;

    private Camera.CameraInfo getCameraInfo(int id) {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        try {
            Camera.getCameraInfo(id, cameraInfo);
        } catch (RuntimeException ex) {
            Log.e(TAG, "getCameraInfo: Camera.getCameraInfo: " + ex);
            return null;
        }
        return cameraInfo;
    }

    private static Camera.Parameters getCameraParameters(
            Camera camera) {
        Camera.Parameters parameters;
        try {
            parameters = camera.getParameters();
        } catch (RuntimeException ex) {
            Log.e(TAG, "getCameraParameters: android.hardware.Camera.getParameters: " + ex);
            if (camera != null) camera.release();
            return null;
        }
        return parameters;
    }

    private static class CaptureErrorCallback implements Camera.ErrorCallback {
        @Override
        public void onError(int error, Camera camera) {
            Log.e(TAG, "Camera capture error: " + error);
        }
    }

    protected int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    VideoCaptureCamera(Context context) {
        super(context);
    }

    @Override
    public boolean allocate(final int width, final int height, final int frameRate, final int facing) {
        Log.d(TAG, "allocate: requested width: " + width + " height: " + height + " fps: " + frameRate);

        synchronized (mCameraStateLock) {
            if (mCameraState != CameraState.STOPPED) {
                return false;
            }
        }

        mFacing = facing;
        Camera.CameraInfo info = new Camera.CameraInfo();
        int numCameras = getNumberOfCameras();
        for (int i = 0; i < numCameras; i++) {
            Camera.getCameraInfo(i, info);
            if (mFacing == Constant.CAMERA_FACING_FRONT && info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mCameraId = i;
                break;
            }

            if (mFacing == Constant.CAMERA_FACING_BACK && info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                mCameraId = i;
                break;
            }
        }

        try {
            mCamera = Camera.open(mCameraId);
        } catch (RuntimeException ex) {
            Log.e(TAG, "allocate: Camera.open: " + ex);
            return false;
        }

        Camera.CameraInfo cameraInfo = getCameraInfo(mCameraId);
        if (cameraInfo == null) {
            mCamera.release();
            mCamera = null;
            return false;
        }
        pCameraNativeOrientation = cameraInfo.orientation;
        // For Camera API, the readings of back-facing camera need to be inverted.
        pInvertDeviceOrientationReadings =
                (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT);

        // Making the texture transformation behaves
        // as the same as Camera2 api.
        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters = getCameraParameters(mCamera);
        if (parameters == null) {
            mCamera = null;
            return false;
        }

        // getSupportedPreviewFpsRange() returns a List with at least one
        // element, but when camera is in bad state, it can return null pointer.
        List<int[]> listFpsRange = parameters.getSupportedPreviewFpsRange();
        if (listFpsRange == null || listFpsRange.size() == 0) {
            Log.e(TAG, "allocate: no fps range found");
            return false;
        }
        final ArrayList<FrameRateRange> ranges =
                new ArrayList<>(listFpsRange.size());
        for (int[] range : listFpsRange) {
            ranges.add(new FrameRateRange(range[0], range[1]));
        }
        // API fps ranges are scaled up x1000 to avoid floating point.
        int frameRateScaled = frameRate * 1000;
        final FrameRateRange chosenRange =
                getClosestFrameRateRange(ranges, frameRateScaled);
        final int[] chosenFpsRange = new int[] {chosenRange.min, chosenRange.max};
        Log.d(TAG, "allocate: fps set to [" + chosenFpsRange[0] + "-" + chosenFpsRange[1] + "]");

        // Calculate size.
        List<Camera.Size> listCameraSize = parameters.getSupportedPreviewSizes();
        int minDiff = Integer.MAX_VALUE;
        int matchedWidth = width;
        int matchedHeight = height;
        for (Camera.Size size : listCameraSize) {
            int diff = Math.abs(size.width - width) + Math.abs(size.height - height);
            if (diff < minDiff && (size.width % 32 == 0)) {
                minDiff = diff;
                matchedWidth = size.width;
                matchedHeight = size.height;
            }
        }
        if (minDiff == Integer.MAX_VALUE) {
            Log.e(TAG, "Couldn't find resolution close to (" + width + "x" + height + ")");
            return false;
        }
        Log.d(TAG, "allocate: matched (" + matchedWidth +  " x " + matchedHeight + ")");

        mPreviewWidth = matchedWidth;
        mPreviewHeight = matchedHeight;
        pCaptureFormat = new VideoCaptureFormat(matchedWidth, matchedHeight,
                chosenFpsRange[1] / 1000, ImageFormat.NV21,
                GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
        parameters.setPreviewSize(matchedWidth, matchedHeight);
        parameters.setPreviewFpsRange(chosenFpsRange[0], chosenFpsRange[1]);
        parameters.setPreviewFormat(pCaptureFormat.getPixelFormat());
        try {
            mCamera.setParameters(parameters);
        } catch (RuntimeException ex) {
            Log.e(TAG, "setParameters: " + ex);
            return false;
        }

        mCamera.setErrorCallback(new CaptureErrorCallback());

        mExpectedFrameSize = pCaptureFormat.getWidth() * pCaptureFormat.getHeight()
                * ImageFormat.getBitsPerPixel(pCaptureFormat.getPixelFormat()) / 8;
        for (int i = 0; i < NUM_CAPTURE_BUFFERS; i++) {
            byte[] buffer = new byte[mExpectedFrameSize];
            mCamera.addCallbackBuffer(buffer);
        }

        synchronized (mCameraStateLock) {
            mCameraState = CameraState.OPENING;
        }

        return true;
    }

    protected void startPreview() {
        Log.d(TAG, "start preview");
        pPreviewSurfaceTexture = new SurfaceTexture(pPreviewTextureId);

        if (mCamera == null) {
            Log.e(TAG, "startCaptureAsync: mCamera is null");
            return;
        }

        try {
            mCamera.setPreviewCallbackWithBuffer(this);
            mCamera.setPreviewTexture(pPreviewSurfaceTexture);
            mCamera.startPreview();
            firstFrame = true;
        } catch (IOException | RuntimeException ex) {
            ex.printStackTrace();
        }

        synchronized (mCameraStateLock) {
            mCameraState = CameraState.STARTED;
        }
    }

    @Override
    public void startCaptureMaybeAsync(boolean needsPreview) {
        Log.d(TAG, "startCaptureMaybeAsync " + pPreviewTextureId);

        synchronized (mCameraStateLock) {
            if (mCameraState == CameraState.STOPPING) {
                mPendingStartRequest = true;
                Log.d(TAG, "startCaptureMaybeAsync pending start request");
            } else if (mCameraState == CameraState.OPENING) {
                if (pPreviewTextureId == -1) pPreviewTextureId = GlUtil.createTextureObject(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                if (pPreviewTextureId != -1) startPreview();
            } else {
                Log.w(TAG, "start camera capture in illegal state:" + mCameraState);
            }
        }
    }

    @Override
    public void stopCaptureAndBlockUntilStopped() {
        Log.d(TAG, "stopCaptureAndBlockUntilStopped");

        if (mCamera == null) {
            Log.e(TAG, "stopCaptureAndBlockUntilStopped: mCamera is null");
            return;
        }

        if (mCameraState != CameraState.STARTED) {
            return;
        }

        try {
            mCamera.stopPreview();
        } catch (RuntimeException ex) {
            Log.e(TAG, "setPreviewTexture: " + ex);
        }

        synchronized (mCameraStateLock) {
            mCameraState = CameraState.STOPPING;
        }
    }

    @Override
    public void deallocate(boolean disconnect) {
        Log.d(TAG, "deallocate " + disconnect);

        if (mCamera == null) return;

        stopCaptureAndBlockUntilStopped();

        if (pPreviewTextureId != -1) {
            int[] textures = new int[] {pPreviewTextureId};
            GLES20.glDeleteTextures(1, textures, 0);
            pPreviewTextureId = -1;
        }

        pCaptureFormat = null;
        mCamera.release();
        mCamera = null;

        synchronized (mCameraStateLock) {
            mCameraState = CameraState.STOPPED;
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, @NonNull Camera camera) {
        mPreviewBufferLock.lock();
        try {
            if (mCameraState != CameraState.STARTED) {
                return;
            }

            if (data.length != mExpectedFrameSize) {
                Log.e(TAG, "the frame size is not as expected");
                return;
            }

            pYUVImage = data;
            onFrameAvailable();
        } finally {
            mPreviewBufferLock.unlock();
            camera.addCallbackBuffer(data);
        }
    }
}
