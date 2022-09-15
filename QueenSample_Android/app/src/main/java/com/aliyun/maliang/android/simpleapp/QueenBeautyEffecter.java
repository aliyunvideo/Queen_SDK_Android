package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

import com.aliyun.android.libqueen.ImageFormat;
import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.Texture2D;

public class QueenBeautyEffecter {

    private Texture2D mOutTexture;
    private QueenEngine engine;

    public QueenEngine getEngine() { return engine; }

    public void onInitEngine(Context context) {
        if (engine != null) {
            return;
        }

        try {
            // 注意，此处是将纹理直接显示上屏，也就是说由QueenEngine进行绘制显示出来，Queen直接render之后即可看到最后效果
            engine = new QueenEngine(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onSetOutViewportSize(int left, int bottom, int width, int height) {
        engine.setScreenViewport(left, bottom, width, height);
    }

    public int onProcessOesTexture(int textureId, float[] matrix, int width, int height, int inputAngle, int outAngle, int flipAxis) {
        return processTextureInner(textureId, true, matrix, width, height, inputAngle, outAngle, flipAxis);
    }

    public int onProcess2DTexture(int textureId, int width, int height, int inputAngle, int outAngle, int flipAxis) {
        return processTextureInner(textureId, false, null, width, height, inputAngle, outAngle, flipAxis);
    }

    private int processTextureInner(int textureId, boolean isOesTexture, float[] matrix, int width, int height, int inputAngle, int outAngle, int flipAxis) {
        int w = isOesTexture ? height : width;
        int h = isOesTexture ? width : height;
        engine.setInputTexture(textureId, w, h, isOesTexture);

        if (mOutTexture == null) {
            // 是否让Queen保持原纹理方向输出
            boolean keepInputDirection = isOesTexture;
            mOutTexture = engine.autoGenOutTexture(keepInputDirection);
        }

        // 根据当前纹理数据到更新
        engine.updateInputTextureBufferAndRunAlg(inputAngle, outAngle, flipAxis, false);
        int result = matrix != null ? engine.renderTexture(matrix) : engine.render();
        // 处理不成功，则返回原始纹理id
        if (result != 0) {
            return textureId;
        } else {
            return mOutTexture.getTextureId();
        }
    }

    public void onProcessDataBuf() {

    }

    public int onProcessTextureAndBuffer(int textureId, boolean isOesTexture, float[] matrix, int width, int height, int inputAngle, int outAngle, int flipAxis, byte[] buffer, int dataFormat) {
        int w = isOesTexture ? height : width;
        int h = isOesTexture ? width : height;
        engine.setInputTexture(textureId, w, h, isOesTexture);

        if (mOutTexture == null) {
            // 是否让Queen保持原纹理方向输出
            mOutTexture = engine.autoGenOutTexture(true);
        }

        // 根据当前纹理数据到更新
        engine.updateInputDataAndRunAlg(buffer, dataFormat, width, height, 0, inputAngle, outAngle, flipAxis);

        int result = engine.renderTexture(matrix);
        // 处理不成功，则返回原始纹理id
        if (result != 0) {
            return textureId;
        } else {
            return mOutTexture.getTextureId();
        }
    }

    public void onReleaseEngine() {
        if (engine != null) {
            engine.release();
            engine = null;
        }
        if (mOutTexture != null) {
            mOutTexture.release();
            mOutTexture = null;
        }
    }
}
