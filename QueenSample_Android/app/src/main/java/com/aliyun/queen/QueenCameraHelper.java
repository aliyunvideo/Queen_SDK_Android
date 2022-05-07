package com.aliyun.queen;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.provider.Settings;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;

import java.lang.ref.WeakReference;
import java.security.MessageDigest;

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

    public static void printPkgMd5String(Context context) {
        String md5 = LicenseHelper.getPackageSignature(context);
        String pkg = context.getPackageName();
        android.util.Log.e("QUEEN_DEBUG", "==pkg=" + pkg + ", md5=" + md5);
    }

    private static final class LicenseHelper {

        public static String getPackageSignature(Context context) {
            String signature = "";
            if (null != context) {
                try {
                    PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                            context.getPackageName(), PackageManager.GET_SIGNATURES);
                    Signature[] signs = packageInfo.signatures;
                    if ((null != signs) && (0 < signs.length)) {
                        signature = hexdigest(signs[0].toByteArray());
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            return signature;
        }

        private static final char[] HEX_DIGIT = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};

        private static String hexdigest(byte[] paramArrayOfByte) {
            try {
                MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
                localMessageDigest.update(paramArrayOfByte);
                byte[] arrayOfByte = localMessageDigest.digest();
                char[] arrayOfChar = new char[32];
                int i = 0;
                int j = 0;
                while (true) {
                    if (i >= 16) {
                        return new String(arrayOfChar);
                    }
                    int k = arrayOfByte[i];
                    int m = j + 1;
                    arrayOfChar[j] = HEX_DIGIT[(0xF & k >>> 4)];
                    j = m + 1;
                    arrayOfChar[m] = HEX_DIGIT[(k & 0xF)];
                    i++;
                }
            } catch (Exception localException) {
                localException.printStackTrace();
            }
            return null;
        }

    }
}
