package com.aliyun.maliang.android.simpleapp.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLSurfaceView;

/**
 * GLSurfaceView，用于展示图片的预览
 */
public class SimpleImageGLSurfaceView extends GLSurfaceView {

    private SimpleImageRenderer mRenderer;

    public SimpleImageGLSurfaceView(Context context) {
        super(context);
    }

    public void init(SimpleImageRenderer render, Context context) {
        setEGLContextClientVersion(2);
        mRenderer = render;
        mRenderer.init(this, context);
    }

    public void release() {
        if (mRenderer != null) {
            mRenderer.release();
            mRenderer = null;
        }
    }

    public void updateInputBmp(Bitmap bitmap) {
        mRenderer.updateInputBmp(bitmap);
    }

    public void requestUpdateRender() {
        mRenderer.requestRender();
    }

    public void releaseGLResource() {
        if (mRenderer != null) {
            mRenderer.release();
        }
    }
}
