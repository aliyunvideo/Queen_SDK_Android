package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

public interface ITextureObserver {
    void onTextureCreated(Context context);
    void onTextureChanged(int left, int bottom, int width, int height);
    int onTextureUpdated(int textureId, boolean isOesTexture, float[] matrix, byte[] imageData, int format, int width, int height);
    void onTextureDestroy();

    boolean captureFrame(String filePath);
}
