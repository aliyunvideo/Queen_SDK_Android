package com.alivc.live.queenbeauty;

import android.content.Context;
//import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.opengl.GLES20;
import android.opengl.GLES30;
import android.os.SystemClock;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.alivc.live.beauty.BeautyInterface;
import com.alivc.live.beauty.constant.BeautyImageFormat;
import com.aliyun.android.libqueen.QueenConfig;
import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.Texture2D;
import com.aliyun.android.libqueen.exception.InitializationException;
import com.aliyun.android.libqueen.models.Flip;
import com.aliyunsdk.queen.param.QueenParamHolder;

import java.nio.IntBuffer;
import java.util.LinkedList;
import java.util.List;

import static android.opengl.GLES20.GL_FRAMEBUFFER;

/**
 * 互动模式下，美颜实现相关类
 */
@Keep
public class InteractQueenBeautyImpl implements BeautyInterface {
    private static final String TAG = InteractQueenBeautyImpl.class.getSimpleName();

    private final Context mContext;

    private final Object mCmdLock = new Object();
    private final List<Object> mCmdList = new LinkedList<>();

    private long glThreadId = -1;

    private QueenEngine mMediaChainEngine;

//    private Texture2D mInputTexture = null;
//    private Texture2D mInputTextureOES = null;
    private Texture2D mOutTexture2D = null;
    private int lastTextureWidth = 0;
    private int lastTextureHeight = 0;
    private Boolean lastTextureIsOes = null;

    private int mDeviceOrientation = 0;
    private OrientationEventListener mOrientationListener;

    private volatile boolean isBeautyEnable = false;
    private volatile boolean isAlgDataRendered = false;
    private boolean isFrameSync = true;

    private long mShareGlContext;

    public InteractQueenBeautyImpl(Context context) {
        Log.i(TAG, "construct");
        mContext = context;
        initOrientationListener(context);
    }

    private void resetQueenEngine() {
        Log.i(TAG, "resetQueenEngine");
        destoryQueenEngine();

        initQueenEngine(mShareGlContext);
    }

    private void destoryQueenEngine() {
        if (null != mMediaChainEngine) {
            if (mOutTexture2D != null) {
                mOutTexture2D.release();
                mOutTexture2D = null;
            }

//            if (mInputTexture != null) {
//                mInputTexture.release();
//                mInputTexture = null;
//            }
//
//            if (mInputTextureOES != null) {
//                mInputTextureOES.release();
//                mInputTextureOES = null;
//            }

            if (mMediaChainEngine != null) {
                mMediaChainEngine.release();
                mMediaChainEngine = null;
            }
        }
    }

    private void initQueenEngine(long glContext) {
        if (mMediaChainEngine == null) {
            // 美颜库需要在texture线程中运行，如果未创建美颜引擎, 创建美颜引擎
            if (glContext != 0L || glContext != -1L) {
                try {
                    QueenConfig queenConfig = new QueenConfig();
                    queenConfig.shareGlContext = glContext;
                    queenConfig.withContext = true;
                    queenConfig.withNewGlThread = true;
                    mMediaChainEngine = new QueenEngine(mContext, queenConfig);
                } catch (InitializationException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "init with gl context: " + glContext);
            } else {
                try {
                    mMediaChainEngine = new QueenEngine(mContext, false);
                } catch (InitializationException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "init without gl context");
            }

            isBeautyEnable = true;
        }
    }

    @Override
    public void init(long glContext) {
        Log.i(TAG, "init: " + glContext);
        mShareGlContext = glContext;

        initQueenEngine(mShareGlContext);
    }

    @Override
    public void release() {
        Log.d(TAG, "release");

        destroyOrientationListener();

        destoryQueenEngine();

        isBeautyEnable = false;
    }

    @Override
    public void setBeautyEnable(boolean enable) {
        Log.d(TAG, "setBeautyEnable: " + enable);
        isBeautyEnable = enable;
    }

    @Override
    public void setBeautyType(int type, boolean enable) {

    }

    @Override
    public void setBeautyParams(int type, float value) {

    }

    @Override
    public void setFaceShapeParams(int type, float value) {

    }

    @Override
    public void setMakeupParams(int type, String path) {

    }

    @Override
    public void setFilterParams(String path) {

    }

    @Override
    public void setMaterialParams(String path) {

    }

    @Override
    public void removeMaterialParams(String path) {

    }

    @Override
    public int onTextureInput(int inputTexture, int textureWidth, int textureHeight, float[] matrix, boolean isOES) {
        if (lastTextureIsOes == null) {
            lastTextureIsOes = isOES;
        } else if (lastTextureIsOes != isOES) {
            resetQueenEngine();
            lastTextureIsOes = isOES;
        }

        Log.i(TAG, "onTextureInput [inputTexture: " + inputTexture + ", isOES: " + isOES);
        glThreadId = Thread.currentThread().getId();

        if (mMediaChainEngine == null || !isBeautyEnable) {
            Log.e("QueenBeautyImpl", "mMediaChainEngine == null || !isBeautyEnable");
            return inputTexture;
        }

//        if (isOES) {
//            if (null == mInputTextureOES) {
//                mInputTextureOES = mMediaChainEngine.createTexture2D();
//                mInputTextureOES.init(inputTexture, textureHeight, textureWidth, isOES);
//            }
//            mInputTextureOES.saveToFile("/sdcard/Download/lzx_rtc/in_oes_" + System.currentTimeMillis() + "_" + inputTexture + ".jpg", Bitmap.CompressFormat.JPEG, 100);
//        } else {
//            if (null == mInputTexture) {
//                mInputTexture = mMediaChainEngine.createTexture2D();
//                mInputTexture.init(inputTexture, textureWidth, textureHeight, isOES);
//            }
//            mInputTexture.saveToFile("/sdcard/Download/lzx_rtc/in_" + System.currentTimeMillis() + "_" + inputTexture + ".jpg", Bitmap.CompressFormat.JPEG, 100);
//        }

        int[] oldFboId = new int[1];
        GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, IntBuffer.wrap(oldFboId));

        int width = textureWidth;
        int height = textureHeight;
        mMediaChainEngine.setInputTexture(inputTexture, width, height, isOES);

        //如果画面旋转的话，就需要重新创建设置大小
        if (lastTextureWidth != width || lastTextureHeight != height) {
            if (mOutTexture2D != null) {
                mOutTexture2D.release();
                mOutTexture2D = null;
            }
            lastTextureWidth = width;
            lastTextureHeight = height;
//            Log.e("QueenBeautyImpl", "setScreenViewport [w: " + w + ", h: " + h + "]");
//            mMediaChainEngine.setScreenViewport(0, 0, w, h);
        }

        if (mOutTexture2D == null) {
            mOutTexture2D = mMediaChainEngine.autoGenOutTexture(isOES);
        }

        if (mOutTexture2D == null) {
            Log.e(TAG, "mOutTexture2D == null");
            return inputTexture;
        }


        QueenRuntime.sHandDetectDebug = true;   // 开启显示手势点位
        QueenRuntime.sBodyDetectDebug = true;   // 开始显示人体点位
        QueenParamHolder.writeParamToEngine(mMediaChainEngine, true);
//        mMediaChainEngine.enableFacePointDebug(true);
        // 刷新当前镜头角度
        refreshCameraAngles();
        mMediaChainEngine.updateInputMatrix(matrix);

        long now = SystemClock.uptimeMillis();
        boolean hasRunAlg = false;
        if (isFrameSync) {
            if (isOES) {
                mMediaChainEngine.updateInputTextureBufferAndRunAlg(inputAngle, outAngle, flipAxis, false);
                mMediaChainEngine.setSegmentInfoFlipY(true);
            } else {
                if (outAngle == 90 || outAngle == 270) {// 右 out = 90 / 左 out = 270
                    // 推流的输入纹理经过处理，非原始摄像头采集纹理，这里单独针对角度适配: 右 out = 90 / 左 out = 270
                    mMediaChainEngine.setRenderAndFaceFlip(Flip.kFlipY, Flip.kNone);
                    // 此处的inputAngle实际为inputAngle += (outAngle-inputAngle),所以直接用outAngle代替
                    mMediaChainEngine.updateInputTextureBufferAndRunAlg(outAngle, (outAngle + 180) % 360, Flip.kFlipY, false);
                } else { // 正 out = 180 / 倒立 out = 0
                    // 解决抠图和美发头像上下翻转的问题
                    // 推流的输入纹理经过处理，非原始摄像头采集纹理，这里单独针对角度适配: 正 out = 180 : 倒立 out = 0
                    mMediaChainEngine.setRenderAndFaceFlip(Flip.kFlipY, Flip.kNone);
                    mMediaChainEngine.updateInputTextureBufferAndRunAlg(outAngle, 180 - outAngle, Flip.kFlipY, false);
                    mMediaChainEngine.setSegmentInfoFlipY(true);
                }
            }
            hasRunAlg = true;
        } else if (mAlgNativeBufferPtr != 0) {
            mMediaChainEngine.updateInputNativeBufferAndRunAlg(mAlgNativeBufferPtr, mAlgDataFormat, mAlgDataWidth, mAlgDataHeight, nAlgDataStride, inputAngle, outAngle, flipAxis);
            hasRunAlg = true;
        }
        int retCode = mMediaChainEngine.render();
        isAlgDataRendered = true;
        Log.i(TAG, Thread.currentThread().getId() + " - " +"render : " + (SystemClock.uptimeMillis()-now) + "ms, hasRunAlg: " + hasRunAlg +
                ", textureW: " + width + ", textureH: " + height +
                ", outTextureId: " + mOutTexture2D.getTextureId() + ", outTextureW: " + mOutTexture2D.getSize().x + ", outTexturH: " + mOutTexture2D.getSize().y +
                ", inputAngle: " + inputAngle + ", outAngle: " + outAngle +
                ", cameraId: " + mCurCameraId);
        if (retCode == -9 || retCode == -10) {
            Log.d(TAG, "queen error code:" + retCode + ",please ensure license valid");
            GLES20.glBindFramebuffer(GL_FRAMEBUFFER, oldFboId[0]);
            return inputTexture;
        }

        GLES20.glBindFramebuffer(GL_FRAMEBUFFER, oldFboId[0]);
//        mOutTexture2D.saveToFile("/sdcard/Download/lzx_rtc/out_"+System.currentTimeMillis()+"_"+mOutTexture2D.getTextureId()+".jpg", Bitmap.CompressFormat.JPEG, 100);

        return mOutTexture2D.getTextureId();
    }

    @Override
    public String getVersion() {
        return "";
    }


    @Override
    public void switchCameraId(int cameraId) {
        if (cameraId != mCurCameraId) {
            Camera.getCameraInfo(cameraId, mCameraInfo);
            mCurCameraId = cameraId;
        }
        boolean isCameraFront = mCurCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT;
        if (isCameraFront) {
            setCameraAngles4Front();
        } else {
            setCameraAngles4Back();
        }
    }

    private Camera.CameraInfo mCameraInfo = new Camera.CameraInfo();
    private int mCurCameraId = -1;
    private int inputAngle;
    private int outAngle;
    private int flipAxis;
    private long mAlgNativeBufferPtr;
    private int mAlgDataFormat, mAlgDataWidth, mAlgDataHeight, nAlgDataStride;

    private void refreshCameraAngles() {
        switchCameraId(mCurCameraId);
    }

    private void setCameraAngles4Back() {
        int displayOrientation = getDisplayOrientation();
        inputAngle = (mCameraInfo.orientation + mDeviceOrientation) % 360;
        int angle = mDeviceOrientation % 360;
        outAngle = (angle - displayOrientation + 360) % 360;

        if (displayOrientation == 0 || displayOrientation == 180) { //竖屏
            outAngle = (180 + displayOrientation - mDeviceOrientation + 360) % 360;
            if (mDeviceOrientation % 180 == 90) {
                outAngle = (180 + mDeviceOrientation) % 360;
            }
        }
        flipAxis = Flip.kFlipY;
    }

    private void setCameraAngles4Front() {
        // @keria, reference: https://www.atatech.org/articles/123323
        // inputAngle, outputAngle, flipAxis，太TM绕了...(〒︿〒)

        inputAngle = 0;
        outAngle = 0;

        int displayOrientation = getDisplayOrientation();
        if (displayOrientation == 90 || displayOrientation == 270) { //横屏
            inputAngle = (270 - mDeviceOrientation + 360) % 360;
            outAngle = (displayOrientation - mDeviceOrientation + 360) % 360;
        } else if (displayOrientation == 0 || displayOrientation == 180) { //竖屏
            inputAngle = (270 - mDeviceOrientation + 360) % 360;
            outAngle = (180 + displayOrientation - mDeviceOrientation + 360) % 360;
        }

        flipAxis = Flip.kFlipY;
    }


    // TODO: Warning: patch code, need to be replaced next version. We should get camera orientation by texture callback.
    private void initOrientationListener(@NonNull Context context) {
        mOrientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                // This is a method called frequently, which is bad for performance.
                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;
                }
                orientation = (orientation + 45) / 90 * 90;
                if (mDeviceOrientation != orientation) {
                    // 不能在此处更改mDisplayOrientation，屏幕旋转可能还未生效
                    Log.d(TAG, "Orientation Changed! displayOrientation: " + mDeviceOrientation + "->" + orientation);
                    mDeviceOrientation = orientation;
                }
            }
        };

        if (mOrientationListener.canDetectOrientation()) {
            Log.d(TAG, "Can detect orientation");
            mOrientationListener.enable();
        } else {
            Log.d(TAG, "Cannot detect orientation");
            mOrientationListener.disable();
        }
    }

    private void destroyOrientationListener() {
        if (mOrientationListener != null) {
            mOrientationListener.disable();
            mOrientationListener = null;
        }
    }

    private int getDisplayOrientation() {
        int displayOrientation = 0;
        if (mContext != null) {
            int angle = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
            switch (angle) {
                case Surface.ROTATION_0:
                    displayOrientation = 0;
                    break;
                case Surface.ROTATION_90:
                    displayOrientation = 90;
                    break;
                case Surface.ROTATION_180:
                    displayOrientation = 180;
                    break;
                case Surface.ROTATION_270:
                    displayOrientation = 270;
                    break;
                default:
                    break;
            }
        }
        return displayOrientation;
    }

    // 如果引擎未创建或者不是texture线程，先缓存设置
    private boolean isCurrentTextureThread(Object cmd) {
        long currentThreadId = Thread.currentThread().getId();
        if (mMediaChainEngine != null && glThreadId == currentThreadId) {
            return true;
        }

        Log.w(TAG, "now not in texture thread " + glThreadId + ", " + currentThreadId);
        synchronized (mCmdLock) {
            mCmdList.add(cmd);
        }
        return false;
    }

}