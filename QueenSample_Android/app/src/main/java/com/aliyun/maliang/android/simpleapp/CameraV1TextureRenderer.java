package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 按照纹理的回调方式进行处理，直接使用QueenEngine实现
 */
public class CameraV1TextureRenderer extends SimpleCameraRenderer {
    private QueenEngine mQueenEngine;

    protected void onCreateEffector(Context context) {
        try {
            mQueenEngine = new QueenEngine(context);
        } catch (Exception e) { e.printStackTrace(); }
    }

    protected int onDrawWithEffectorProcess() {
        // 更新美颜特效参数，参数修改，在菜单组件中已完成交互
        QueenParamHolder.writeParamToEngine(mQueenEngine, false);
        // 设置抠图参数进行Y轴翻转，否则抠图mask会翻转过来
        if (mQueenEngine != null) {
            mQueenEngine.setSegmentInfoFlipY(true);
        }

        int in = QueenCameraHelper.get().inputAngle;
        int out = QueenCameraHelper.get().outAngle;
        int flip = QueenCameraHelper.get().flipAxis;
        int updateTextureId = processTextureInner(mQueenEngine, mOESTextureId, true,
                transformMatrix, mCameraPreviewWidth, mCameraPreviewHeight,
                in, out, flip);

        return updateTextureId;
    }

    @Override
    protected void onReleaseEffector() {
        // 释放Engine
        if (mQueenEngine != null) {
            mQueenEngine.release();
            mQueenEngine = null;
        }
    }

    // 非必需步骤调用，只有显示画面，与纹理传入的size不一致时，才需要设置ViewportSize，
    // 否则，默认使用输入纹理的size
    @Override
    protected void onSetViewportSize(int left, int bottom, int width, int height) {
        super.onSetViewportSize(left, bottom, width, height);
        mQueenEngine.setScreenViewport(left, bottom, width, height);
    }

    private int processTextureInner(QueenEngine engine, int textureId, boolean isOesTexture, float[] matrix, int width, int height, int inputAngle, int outAngle, int flipAxis) {
        int w = isOesTexture ? height : width;
        int h = isOesTexture ? width : height;
        engine.setInputTexture(textureId, w, h, isOesTexture);

        // 输出纹理id是否已生成
        int outTextId = mQueenEngine.getAutoGenOutTextureId();
        if (outTextId <= 0) {
            // 是否让Queen保持原纹理方向输出
            boolean keepInputDirection = isOesTexture;
            engine.autoGenOutTexture(keepInputDirection);
        }

        // 根据当前纹理数据到更新
        engine.updateInputTextureBufferAndRunAlg(inputAngle, outAngle, flipAxis, false);
        int result = matrix != null ? engine.renderTexture(matrix) : engine.render();
        // 处理不成功，则返回原始纹理id
        if (result != 0) {
            return textureId;
        } else {
            return mQueenEngine.getAutoGenOutTextureId();
        }
    }
}
