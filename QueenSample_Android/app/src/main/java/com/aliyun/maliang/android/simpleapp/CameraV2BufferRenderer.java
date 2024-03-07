package com.aliyun.maliang.android.simpleapp;

import android.content.Context;
import android.opengl.GLES20;

import com.aliyun.android.libqueen.ImageFormat;
import com.aliyun.android.libqueen.QueenBeautyEffector;
import com.aliyun.android.libqueen.aio.QueenImageFormat;
import com.aliyun.android.libqueen.models.AlgType;
import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.GLTextureHelper;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyunsdk.queen.menu.utils.BitmapUtils;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 按照databuffer的回调方式进行处理
 */
public class CameraV2BufferRenderer extends SimpleCameraRenderer {
    private QueenBeautyEffector mQueenEffector;
    private byte[] mProcessOutBuffer = null;
    private int mProcessTextureId = -1;

    protected void onCreateEffector(Context context) {
        mQueenEffector = new QueenBeautyEffector();
        mQueenEffector.onCreateEngine(context);
//        mQueenEffector.getEngine().enableDebugLog();
//        mQueenEffector.getEngine().enableDetectPointDebug(AlgType.kFaceDetect, true);
    }

    private boolean isSetViewport = false;
    protected int onDrawWithEffectorProcess() {
        int updateTextureId = -1;
        int w = mCameraPreviewWidth;
        int h = mCameraPreviewHeight;
        byte[] buffer = mCamera.getLastUpdateCameraPixels();
        if (buffer == null)
            return updateTextureId;

        if (mQueenEffector.getEngine() != null) {
            // 更新美颜特效参数，参数修改，在菜单组件中已完成交互
            QueenParamHolder.writeParamToEngine(mQueenEffector.getEngine(), false);
            // 设置抠图参数进行Y轴翻转，否则抠图mask会翻转过来
            if (mQueenEffector.getEngine() != null) {
                mQueenEffector.getEngine().setSegmentInfoFlipY(true);
            }

            int in = QueenCameraHelper.get().inputAngle;
            int out = QueenCameraHelper.get().outAngle;
            int flip = QueenCameraHelper.get().flipAxis;
            byte[] rgbaBuf = GLTextureHelper.nv21toRGBA(buffer, w, h);

            if (mProcessOutBuffer == null || rgbaBuf.length != mProcessOutBuffer.length) {
                mProcessOutBuffer = new byte[rgbaBuf.length];
            }
            int result = mQueenEffector.onProcessDataBuf(rgbaBuf, mProcessOutBuffer, QueenImageFormat.RGBA, w, h, 0, in, out, flip);
            updateTextureId = GLTextureHelper.loadRgbaBuf2Texture(mProcessOutBuffer, w, h, mProcessTextureId);

        } else {
            byte[] rgbaBuf = GLTextureHelper.nv21toRGBA(buffer, w, h);
            updateTextureId = GLTextureHelper.loadRgbaBuf2Texture(rgbaBuf, w, h, mProcessTextureId);
        }
        mCamera.releaseData(buffer);
        mProcessTextureId = updateTextureId;

        return updateTextureId;
    }

    @Override
    protected void onReleaseEffector() {
        // 释放Engine
        mQueenEffector.onReleaseEngine();
    }

    // 非必需步骤调用，只有显示画面，与纹理传入的size不一致时，才需要设置ViewportSize，
    // 否则，默认使用输入纹理的size
    @Override
    protected void onSetViewportSize(int left, int bottom, int width, int height) {
        super.onSetViewportSize(left, bottom, width, height);
//        mQueenEffector.onSetOutViewportSize(left, bottom, width, height);
    }
}
