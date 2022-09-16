package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

import com.aliyun.android.libqueen.ImageFormat;
import com.aliyun.android.libqueen.QueenBeautyEffector;
import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 按照纹理的回调方式，同时算法，采用buffer的方式进行处理，效率最高
 */
public class CameraV3TextureAndBufferRenderer extends SimpleCameraRenderer {
    private QueenBeautyEffector mQueenEffector;

    protected void onCreateEffector(Context context) {
        mQueenEffector = new QueenBeautyEffector();
        mQueenEffector.onCreateEngine(context);
    }

    protected int onDrawWithEffectorProcess() {
        // 更新美颜特效参数
        QueenParamHolder.writeParamToEngine(mQueenEffector.getEngine(), false);
        // 设置抠图参数进行Y轴翻转，否则抠图mask会翻转过来
        if (mQueenEffector.getEngine() != null) {
            mQueenEffector.getEngine().setSegmentInfoFlipY(true);
        }

        int updateTextureId = mOESTextureId;
        // buffer数据来源，主要直接来自camera
        byte[] buffer = mCamera.getLastUpdateCameraPixels();
        if (buffer != null) {
            int in = QueenCameraHelper.get().inputAngle;
            int out = QueenCameraHelper.get().outAngle;
            int flip = QueenCameraHelper.get().flipAxis;
            updateTextureId = mQueenEffector.onProcessTextureAndBuffer(mOESTextureId, true,
                    transformMatrix, mCameraPreviewWidth, mCameraPreviewHeight,
                    in, out, flip,
                    buffer, ImageFormat.NV21);
        }

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
        mQueenEffector.onSetOutViewportSize(left, bottom, width, height);
    }
}
