package com.aliyun.maliang.android.simpleapp.view;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.aliyun.maliang.android.simpleapp.camera.SimpleCamera;
import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraGLSurfaceView;
import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;

public class MainViewSurfacePanel {
    private Activity mAttachActivity;

    private SimpleCameraGLSurfaceView mGLSurfaceView;
    private int mCameraId;
    private SimpleCamera mCamera;

    public MainViewSurfacePanel(Activity activity) {
        mAttachActivity = activity;
    }

    public View createSurfaceView(SimpleCameraRenderer renderer) {
        mGLSurfaceView = new SimpleCameraGLSurfaceView(mAttachActivity);
        mGLSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        //设置相机前后
        QueenCameraHelper.get().initOrientation(mAttachActivity);
        mCameraId = QueenCameraHelper.get().getCameraId();
        mCamera = new SimpleCamera(mAttachActivity);
        mGLSurfaceView.init(mCamera, renderer, mAttachActivity);

        if (!mCamera.openCamera(1280,720, mCameraId)) {
            return null;
        }
        return mGLSurfaceView;
    }

    public void switchCamera() {
        if (mCameraId == android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraId = android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        mCamera.stopPreview();
        mCamera.reOpenPreview(1280,720, mCameraId);
        mGLSurfaceView.reBindCamera(mCamera);
        mCamera.startPreview();
        QueenCameraHelper.get().setCameraId(mCameraId);
    }

    public void onPause() {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mCamera != null) {
                        mCamera.stopPreview();
                        mCamera.releaseCamera();
                    }

                    mGLSurfaceView.releaseGLResource();
                    mGLSurfaceView.post(new Runnable() {
                        @Override
                        public void run() {
                            mGLSurfaceView.onPause();
                        }
                    });
                }
            });
        }
    }

    public void onResume() {
        if (mCamera != null) {
            int result = mCamera.startPreview();
            if (result < 0) {
                mCamera.reOpenPreview(1280,720, mCameraId);
                mGLSurfaceView.reBindCamera(mCamera);
                mCamera.startPreview();
            }
        }
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onResume();
        }
    }

    public void onDestroy() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.releaseCamera();
                    mCamera = null;
                }

                mGLSurfaceView.release();
                mGLSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        mGLSurfaceView.onPause();
                        mGLSurfaceView = null;
                    }
                });
            }
        });
    }
}
