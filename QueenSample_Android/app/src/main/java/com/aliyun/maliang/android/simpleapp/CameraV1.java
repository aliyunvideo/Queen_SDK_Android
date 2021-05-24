package com.aliyun.maliang.android.simpleapp;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import com.aliyun.maliang.android.simpleapp.collections.SimpleBytesBufPool;
import com.taobao.android.libqueen.models.Flip;


import java.io.IOException;
import java.util.List;

public class CameraV1 {
    private Activity mActivity;
    private int mCameraId;
    private Camera mCamera;
    private int mWidth;
    private int mHeight;

    private int mDeviceOrientation = 0;
    private int mDisplayOrientation = 0;
    private static String TAG = "CameraV1";
    private Camera.CameraInfo mInfo;

    public CameraV1(Activity activity) {
        mActivity = activity;
        mWidth = 1280;
        mHeight = 720;

        int byteSize = mWidth*mHeight* ImageFormat.getBitsPerPixel(ImageFormat.NV21)/8;
        mBytesBufPool = new SimpleBytesBufPool(3, byteSize);
    }

    public int inputAngle;
    public int outAngle;
    /**
     *  - 0(no flip)
     *  - 1(flip X axis)
     *  - 2(flip Y axis)
     * @reference com.taobao.android.libqueen.models.Flip
     */
    public int flipAxis;

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

    //只有4个方向，0是正常方向，其他是顺时针旋转方向
    public void setDeviceOrientation(int orientation, int displayOrientation) {
        if (mDisplayOrientation != displayOrientation) {
            setCameraDisplayOrientation(mActivity, mCameraId, mCamera);
        }
        mDeviceOrientation = orientation;
        mDisplayOrientation = displayOrientation;
        setCameraAngles();
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

            setCameraAngles();

            mCamera.setParameters(parameters);

            setCameraPreviewCfg();

            Log.i(TAG, "open camera");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    private int ta = 0;

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

    private void setCameraAngles() {
        if (mInfo == null) {
            mInfo = new Camera.CameraInfo();
        }

        Camera.getCameraInfo(mCameraId, mInfo);

        inputAngle = getInputAngle(mInfo);
        outAngle = getOutputAngle(mInfo);
        setFlipAxis(mInfo);
    }

    private void setFlipAxis(Camera.CameraInfo cameraInfo) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            flipAxis = Flip.kNone;
        } else {
            flipAxis = Flip.kFlipY;
        }
    }

    private int getOutputAngle(Camera.CameraInfo cameraInfo) {
        boolean isFont = cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK ? false : true;
        int angle = isFont ? (360 - mDeviceOrientation) % 360 : mDeviceOrientation % 360;
        return (angle - mDisplayOrientation + 360) % 360;
    }



    private int getInputAngle(Camera.CameraInfo cameraInfo) {
        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            return (360 + cameraInfo.orientation - mDeviceOrientation) % 360;
        } else {
            return (cameraInfo.orientation + mDeviceOrientation) % 360;
        }
    }

    public boolean isLandscape() {
        return (inputAngle - outAngle + 360) % 180 == 0;
    }



    private  byte[] yuv420spRotate270(byte[] src, int width, int height) {
        int count = 0;
        int uvHeight = height >> 1;
        int imgSize = width * height;
        byte[] des = new byte[imgSize * 3 >> 1];
        //copy y
        for (int j = width - 1; j >= 0; j--) {
            for (int i = 0; i < height; i++) {
                des[count++] = src[width * i + j];
            }
        }
        //u,v
        for (int j = width - 1; j > 0; j -= 2) {
            for (int i = 0; i < uvHeight; i++) {
                des[count++] = src[imgSize + width * i + j - 1];
                des[count++] = src[imgSize + width * i + j];
            }
        }
        return des;
    }



//    public  byte[] rotateYUV420Degree90(byte[] data, int imageWidth, int imageHeight) {
//        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
//        int i = 0;
//        for (int x = 0; x < imageWidth; x++) {
//            for (int y = imageHeight - 1; y >= 0; y--) {
//                yuv[i] = data[y * imageWidth + x];
//                i++;
//            }
//        }
//        i = imageWidth * imageHeight * 3 / 2 - 1;
//        for (int x = imageWidth - 1; x > 0; x = x - 2) {
//            for (int y = 0; y < imageHeight / 2; y++) {
//                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth) + x];
//                i--;
//                yuv[i] = data[(imageWidth * imageHeight) + (y * imageWidth)
//                        + (x - 1)];
//                i--;
//            }
//        }
//        return yuv;
//    }
//
//    private  byte[] rotateYUV420Degree180(byte[] data, int imageWidth, int imageHeight) {
//        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
//        int i = 0;
//        int count = 0;
//        for (i = imageWidth * imageHeight - 1; i >= 0; i--) {
//            yuv[count] = data[i];
//            count++;
//        }
//        i = imageWidth * imageHeight * 3 / 2 - 1;
//        for (i = imageWidth * imageHeight * 3 / 2 - 1; i >= imageWidth
//                * imageHeight; i -= 2) {
//            yuv[count++] = data[i - 1];
//            yuv[count++] = data[i];
//        }
//        return yuv;
//    }
//
//    private  byte[] rotateYUV420Degree270(byte[] data, int imageWidth,
//                                               int imageHeight) {
//        byte[] yuv = new byte[imageWidth * imageHeight * 3 / 2];
//        int nWidth = 0, nHeight = 0;
//        int wh = 0;
//        int uvHeight = 0;
//        if (imageWidth != nWidth || imageHeight != nHeight) {
//            nWidth = imageWidth;
//            nHeight = imageHeight;
//            wh = imageWidth * imageHeight;
//            uvHeight = imageHeight >> 1;// uvHeight = height / 2
//        }
//
//        int k = 0;
//        for (int i = 0; i < imageWidth; i++) {
//            int nPos = 0;
//            for (int j = 0; j < imageHeight; j++) {
//                yuv[k] = data[nPos + i];
//                k++;
//                nPos += imageWidth;
//            }
//        }
//        for (int i = 0; i < imageWidth; i += 2) {
//            int nPos = wh;
//            for (int j = 0; j < uvHeight; j++) {
//                yuv[k] = data[nPos + i];
//                yuv[k + 1] = data[nPos + i + 1];
//                k += 2;
//                nPos += imageWidth;
//            }
//        }
//        return rotateYUV420Degree180(rotateYUV420Degree90(data, imageWidth, imageHeight), imageWidth, imageHeight);
//    }

//
//    private void rotateYv12Degree90(byte[] src, int width, int height, byte[] dst, boolean clockwise) {
//        int area = width * height;
//
//        if (clockwise) {
//            rotateRectClockwiseDegree90(src, 0, width, height, dst, 0);
//            rotateRectClockwiseDegree90(src, area, width / 2, height / 2, dst, area);
//            rotateRectClockwiseDegree90(src, area * 5 / 4, width / 2, height / 2, dst, area * 5 / 4);
//        } else {
//            rotateRectAnticlockwiseDegree90(src, 0, width, height, dst, 0);
//            rotateRectAnticlockwiseDegree90(src, area, width / 2, height / 2, dst, area);
//            rotateRectAnticlockwiseDegree90(src, area * 5 / 4, width / 2, height / 2, dst, area * 5 / 4);
//        }
//    }
//
//    private void rotateRectClockwiseDegree90(byte[] src, int srcOffset, int width, int height, byte dst[], int dstOffset) {
//        int i, j;
//        int index = dstOffset;
//        for (i = 0; i < width; i++) {
//            for (j = height - 1; j >= 0; j--) {
//                dst[index] = src[srcOffset + j * width + i];
//                index++;
//            }
//        }
//    }
//
//    private void rotateRectAnticlockwiseDegree90(byte[] src, int srcOffset, int width, int height, byte dst[],
//                                             int dstOffset) {
//        int i, j;
//        int index = dstOffset;
//        for (i = width - 1; i >= 0; i--) {
//            for (j = 0; j < height; j++) {
//                dst[index] = src[srcOffset + j * width + i];
//                index++;
//            }
//        }
//    }


    private byte[] nv21toRGBA(byte[] data, int width, int height) {
        int size = width * height;
        byte[] bytes = new byte[size * 4];
        int y, u, v;
        int r, g, b;
        int index;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index = j % 2 == 0 ? j : j - 1;

                y = data[width * i + j] & 0xff;
                u = data[width * height + width * (i / 2) + index + 1] & 0xff;
                v = data[width * height + width * (i / 2) + index] & 0xff;

                r = y + (int) 1.370705f * (v - 128);
                g = y - (int) (0.698001f * (v - 128) + 0.337633f * (u - 128));
                b = y + (int) 1.732446f * (u - 128);

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                bytes[width * i * 4 + j * 4 + 0] = (byte) r;
                bytes[width * i * 4 + j * 4 + 1] = (byte) g;
                bytes[width * i * 4 + j * 4 + 2] = (byte) b;
                bytes[width * i * 4 + j * 4 + 3] = (byte) 255;//透明度
            }
        }
        return bytes;
    }



    public byte[] nv21toBGRA(byte[] data, int width, int height) {
        int size = width * height;
        byte[] bytes = new byte[size * 4];
        int y, u, v;
        int r, g, b;
        int index;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index = j % 2 == 0 ? j : j - 1;

                y = data[width * i + j] & 0xff;
                u = data[width * height + width * (i / 2) + index + 1] & 0xff;
                v = data[width * height + width * (i / 2) + index] & 0xff;

                r = y + (int) 1.370705f * (v - 128);
                g = y - (int) (0.698001f * (v - 128) + 0.337633f * (u - 128));
                b = y + (int) 1.732446f * (u - 128);

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

//                bytes[width * i * 4 + j * 4 + 0] = (byte) r;
//                bytes[width * i * 4 + j * 4 + 1] = (byte) g;
//                bytes[width * i * 4 + j * 4 + 2] = (byte) b;
//                bytes[width * i * 4 + j * 4 + 3] = (byte) 255;//透明度
                bytes[width * i * 4 + j * 4 + 0] = (byte) r;
                bytes[width * i * 4 + j * 4 + 1] = (byte) g;
                bytes[width * i * 4 + j * 4 + 2] = (byte) b;
                bytes[width * i * 4 + j * 4 + 3] = (byte) 255;//透明度
            }
        }
        return bytes;
    }




    static public void decodeYUV420SP(int[] rgba, byte[] yuv420sp, int width, int height) {
        final int frameSize = width * height;
        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0){
                    y = 0;
                }
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }
                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0){
                    r = 0;
                }
                else if (r > 262143){
                    r = 262143;
                }
                if (g < 0){
                    g = 0;
                }
                else if (g > 262143){
                    g = 262143;
                }
                if (b < 0){
                    b = 0;
                }
                else if (b > 262143){
                    b = 262143;
                }

                rgba[yp] = 0xff000000 | ((b << 6) & 0xff0000)
                        | ((g >> 2) & 0xff00) | ((r >> 10) & 0xff);
//				下面为百度到的方法，其实就是r和b变量调换下位置
//				rgba[yp] = 0xff000000 | ((r << 6) & 0xff0000)
//						| ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }
    }
    public static void applyGrayScale(int [] pixels, byte [] data, int width, int height) {
        int p;
        int size = width*height;
        for(int i = 0; i < size; i++) {
            p = data[i] & 0xFF;
            pixels[i] = 0xff000000 | p<<16 | p<<8 | p;
        }
    }


    public int getPrevieWidth(){return mWidth;}
    public int getPrevieHeight(){return mHeight;}

    public int getCameraOutWidth() {
        return isLandscape()? mWidth : mHeight;
    }

    public int getCameraOutHeight() {
        return isLandscape()? mHeight : mWidth;
    }

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
