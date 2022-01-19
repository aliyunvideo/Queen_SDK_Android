package com.aliyun.maliang.android.simpleapp.queen;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import java.lang.ref.WeakReference;

public class QueenCameraHelper {

    private int mOrientationConfig = Configuration.ORIENTATION_UNDEFINED;
    private OrientationEventListener mOrientationEventListener;

    private int mDeviceOrientation = 0;
    private int mDisplayOrientation = 0;
    private Camera.CameraInfo mInfo;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;  // 此处固定为前置摄像头，可更换

    public int inputAngle;
    public int outAngle;
    /**
     *  - 0(no flip)
     *  - 1(flip X axis)
     *  - 2(flip Y axis)
     * @reference com.taobao.android.libqueen.models.Flip
     */
    public int flipAxis;

    private Boolean isSystemAutoRotation = null;
    private WeakReference<Activity> mContext;

    private static QueenCameraHelper helperInstance = new QueenCameraHelper();

    public  static QueenCameraHelper get() {
        return helperInstance;
    }

    private QueenCameraHelper() {
        // 初始化默认的参数
        setCameraAngles();
    }

    public void initOrientation(final Activity activity) {
        mOrientationEventListener = new OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;
                }
                orientation = (orientation + 45) / 90 * 90;
                int degree = getDegrees(activity);

                setDeviceOrientation(activity, orientation, degree);
            }
        };
        mContext = new WeakReference<>(activity);
    }

    //只有4个方向，0是正常方向，其他是顺时针旋转方向
    public void setDeviceOrientation(final Activity activity, int orientation, int displayOrientation) {
//        if (mDisplayOrientation != displayOrientation) {
//            setCameraDisplayOrientation(activity, mCameraId);
//        }
        mDeviceOrientation = orientation;
        mDisplayOrientation = displayOrientation;
        setCameraAngles();
    }

//    public static void setCameraDisplayOrientation(Activity activity, int cameraId) {
//        Camera.CameraInfo info =
//                new Camera.CameraInfo();
//        Camera.getCameraInfo(cameraId, info);
//        int degrees = getDegrees(activity);
//
//        int result = (info.orientation + degrees) % 360;
//        result = (360 - result) % 360;  // compensate the mirror
//    }


    private void setCameraAngles() {
        if (mInfo == null) {
            mInfo = new Camera.CameraInfo();
        }

        Camera.getCameraInfo(mCameraId, mInfo);
        int newInputAngle = getInputAngle(mInfo);
        int newOutAngle = getOutputAngle(mInfo);
        boolean isAngleChanged = newInputAngle != inputAngle || newOutAngle != outAngle;
        inputAngle = newInputAngle;
        outAngle = newOutAngle;
        Log.i("QueenCameraHelper", "setCameraAngles [inputAngle: " + inputAngle + ", outAngle: " + outAngle + ", isFront: " + isFrontCamera() + "]");
        setFlipAxis(mInfo);
        if (isAngleChanged || isSystemAutoRotation == null) {
            // 获取是否开启自动旋转的特性
            configSystemAutoRotation();
        }
    }

    public void configSystemAutoRotation() {
        try {
            if (null != mContext && null != mContext.get()) {
                int accelerometerRotation = Settings.System.getInt(mContext.get().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION);
                Log.i("QueenCameraHelper", "configSystemAutoRotation [accelerometerRotation: " + accelerometerRotation + "]");
                isSystemAutoRotation = (1 == accelerometerRotation);
            }
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
    }

    private int getOutputAngle(Camera.CameraInfo cameraInfo) {
        if (isSystemAutoRotation != null && isSystemAutoRotation) {
            return 0;
        } else {
            boolean isFont = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK ? false : true;
            int angle = isFont ? (360 - mDeviceOrientation) % 360 : mDeviceOrientation % 360;
            return (angle - mDisplayOrientation + 360) % 360;
        }
    }

    private int getInputAngle(Camera.CameraInfo cameraInfo) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 + cameraInfo.orientation - mDeviceOrientation) % 360;
        } else {
            return (cameraInfo.orientation + mDeviceOrientation) % 360;
        }
    }

    private void setFlipAxis(Camera.CameraInfo cameraInfo) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            flipAxis = 0;
        } else {
            flipAxis = 2;
        }
    }

    public void onPause() {
        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    public void onResume() {
        if (mOrientationEventListener != null && mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    private static int getDegrees(final Activity activity) {
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
            default: break;
        }
        return degrees;
    }

    public boolean isLandscape() {
        return (inputAngle - outAngle + 360) % 180 == 0;
    }

    public boolean isFrontCamera() {
        if (null != mInfo) {
            return mInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT;
        }
        return true;
    }

    public void setCameraId(int cameraId) {
        this.mCameraId = cameraId;
    }

    public int getCameraId() {
        return mCameraId;
    }
}
