package com.aliyun.queen;

import android.content.Context;

import com.aliyun.android.libqueen.QueenEngine;

public interface IQueenRender {

    void onTextureCreate(Context context);
    void onTextureSizeChanged(int left, int bottom, int width, int height);
    int onTextureProcess(int textureId, float[] matrix, int width, int height);
    int onTextureProcess(int textureId, boolean isOesTexture, float[] matrix, int width, int height);
    int onTextureProcess(int textureId, boolean isOesTexture, float[] matrix, byte[] imageData, int format, int width, int height);
    void onTextureDestroy();

    QueenEngine getEngine();

}
