package com.aliyun.maliang.android.simpleapp;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.aliyun.maliang.android.simpleapp.collections.SimpleBytesBufPool;

import java.io.IOException;
import java.util.List;

public class CameraV1 {
    private Activity mActivity;
    private int mCameraId;
    private Camera mCamera;
    private int mWidth;
    private int mHeight;

    private static String TAG = "CameraV1";

    public CameraV1(Activity activity) {
        mActivity = activity;
        mWidth = 1280;
        mHeight = 720;

        int byteSize = mWidth*mHeight* ImageFormat.getBitsPerPixel(ImageFormat.NV21)/8;
        mBytesBufPool = new SimpleBytesBufPool(3, byteSize);
    }

    private SimpleBytesBufPool mBytesBufPool;

    public byte[] getLastUpdateCameraPixels(){
        // 取数据进行消费 FIXME
        return mBytesBufPool.getLastBuffer();
    }

    public void releaseData(byte[] data) {
        mBytesBufPool.releaseBuffer(data);
    }

    public void relase() {
        mBytesBufPool.clear();
    }

    public synchronized boolean openCamera(int screenWidth, int screenHeight, int cameraId) {
        try {
            mCameraId = cameraId;
            mCamera = Camera.open(mCameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.set("orientation", "portrait");

            //设置连续对焦
            List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
            boolean hasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            if (hasAutoFocus){
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
            List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
            for(Camera.Size it :sizeList)
            {
                Log.i("CameraV1", "getSupportedPreviewSizes: "+it.width + "  " + it.height);
            }
            parameters.setPreviewSize(mWidth, mHeight);


            List<Integer> list1 =  mCamera.getParameters().getSupportedPreviewFormats();
//            大多数只支持17 和 842094169
            for(Integer it : list1) {//其内部实质上还是调用了迭代器遍历方式，这种循环方式还有其他限制，不建议使用。
                Log.i("CameraV1", "getSupportedPreviewFormats: "+it);
            }

            //NV21
            parameters.setPreviewFormat(ImageFormat.NV21);

            setCameraDisplayOrientation(mActivity, mCameraId, mCamera);

            mCamera.setParameters(parameters);

            setCameraPreviewCfg();

            Log.i(TAG, "open camera");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void setCameraPreviewCfg() {
        mCamera.addCallbackBuffer(mBytesBufPool.reusedBuffer());
        mCamera.setPreviewCallbackWithBuffer(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
                mBytesBufPool.updateBuffer(bytes);
                mCamera.addCallbackBuffer(mBytesBufPool.reusedBuffer());
            }
        });
    }

    public int getPrevieWidth(){return mWidth;}
    public int getPrevieHeight(){return mHeight;}

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int degrees = ActivityUtil.getDegrees(activity);

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        Log.v("setDisplayOrientation","::"+result);
        camera.setDisplayOrientation(result);

    }

    public void startPreview() {
        if (mCamera != null) {
            setCameraPreviewCfg();
            mCamera.startPreview();
        }
    }

    public void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
        }
    }

    public void reOpenPreview(int screenWidth, int screenHeight, int cameraId) {
        releaseCamera();
        openCamera(screenWidth, screenHeight, cameraId);
    }

    public SurfaceTexture mSurfaceTexture;
    public void setPreviewTexture(SurfaceTexture surfaceTexture) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewTexture(surfaceTexture);
                mSurfaceTexture = surfaceTexture;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public synchronized void releaseCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
}
