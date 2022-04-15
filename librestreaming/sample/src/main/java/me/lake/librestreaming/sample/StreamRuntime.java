package me.lake.librestreaming.sample;

import android.app.Activity;
import android.hardware.Camera;
import android.view.Surface;

import com.aliyun.android.libqueen.models.Flip;

public class StreamRuntime {

    public static int sCurBindTargetFrameBuffer;

    public static int mCameraId;
    private static Camera.CameraInfo mInfo;
    public static int inputAngle;
    public static int outAngle;
    public static int flipAxis;

    private static int mDeviceOrientation = 0;
    private static int mDisplayOrientation = 0;

    // TODO: 注意,摄像头切换时,需要改变
    public static float[] transformMatrix = new float[16];

    public static void swapCamera() {
        int newCameraId = StreamRuntime.mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT
                ? Camera.CameraInfo.CAMERA_FACING_BACK
                :Camera.CameraInfo.CAMERA_FACING_FRONT;

        setCameraAngles(newCameraId);
    }

    public static void setCameraAngles(int newCameraId) {
        mCameraId = newCameraId;

        if (mInfo == null) {
            mInfo = new Camera.CameraInfo();
        }

        Camera.getCameraInfo(mCameraId, mInfo);

        inputAngle = getInputAngle(mInfo);
        inputAngle -= 90;       // 此处需要特殊处理,渲染画面传递回来数据已经旋转90.
        outAngle = getOutputAngle(mInfo);
        setFlipAxis(mInfo);
    }

    private static int getOutputAngle(Camera.CameraInfo cameraInfo) {
        //RBLog.i(TAG, "getOutputAngle " + orientation);
        boolean isFont = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK ? false : true;
        int angle = isFont ? (360 - mDeviceOrientation) % 360 : mDeviceOrientation % 360;
        return (angle - mDisplayOrientation + 360) % 360;
    }

    private static int getInputAngle(Camera.CameraInfo cameraInfo) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 + cameraInfo.orientation - mDeviceOrientation) % 360;
        } else {
            return (cameraInfo.orientation + mDeviceOrientation) % 360;
        }
    }

    private static void setFlipAxis(Camera.CameraInfo cameraInfo) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            flipAxis = Flip.kNone;
        } else {
            flipAxis = Flip.kFlipY;
        }
    }

    //只有4个方向，0是正常方向，其他是顺时针旋转方向
    public static void setDeviceOrientation(Activity activity, int orientation) {
        mDeviceOrientation = (orientation + 45) / 90 * 90;
        int displayOrientation = getDegrees(activity);
        mDisplayOrientation = displayOrientation;
        setCameraAngles(mCameraId);
    }

    public static int getDegrees(final Activity activity) {
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
}
