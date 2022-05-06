package com.beautyQueen;

import android.content.Context;

import com.aliyun.android.libqueen.QueenEngine;

public class QueenRender implements IQueenRender {

    public static final float[] MATRIX_IDENTITY = new float[] {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    // Texture transformation matrix, the image will be displayed upright
    public static final float[] MATRIX_FLIP = {
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f
    };


    private IQueenRender mQueenRenderImpl;

    private QueenRender(Builder builder) {
        mQueenRenderImpl = new QueenRendererImpl(builder);
    }

    @Override
    public QueenEngine getEngine() {
        return mQueenRenderImpl.getEngine();
    }

    @Override
    public void onTextureCreate(Context context) {
        mQueenRenderImpl.onTextureCreate(context);
    }

    @Override
    public void onTextureSizeChanged(int left, int bottom, int width, int height) {
        mQueenRenderImpl.onTextureSizeChanged(left, bottom, width, height);
    }

    @Override
    public int onTextureProcess(int textureId, float[] matrix, int width, int height) {
        return mQueenRenderImpl.onTextureProcess(textureId, matrix, width, height);
    }

    @Override
    public int onTextureProcess(int textureId, boolean isOesTexture, float[] matrix, int width, int height) {
        return mQueenRenderImpl.onTextureProcess(textureId, isOesTexture, matrix, width, height);
    }

    @Override
    public int onTextureProcess(int textureId, boolean isOesTexture, float[] matrix, byte[] imageData, int format, int width, int height) {
        return mQueenRenderImpl.onTextureProcess(textureId, isOesTexture, matrix, imageData, format, width, height);
    }

    @Override
    public void onTextureDestroy() {
        mQueenRenderImpl.onTextureDestroy();
    }

    /***************************************/
    public static class Builder {

        boolean isDraw2Screen = false;

        public Builder setDraw2Screen(boolean toScreen) {
            isDraw2Screen = toScreen;
            return this;
        }

        public QueenRender build() {
            QueenRender render = new QueenRender(this);
            return render;
        }
    }

}
