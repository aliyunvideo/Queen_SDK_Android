package com.aliyun.maliang.android.simpleapp.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * GLSurfaceView，用于展示相机camera预览
 */
public class SimpleCameraGLSurfaceView extends GLSurfaceView {

    private SimpleCameraRenderer mRenderer;

    public SimpleCameraGLSurfaceView(Context context) {
        super(context);
    }

    public void init(SimpleCamera camera, SimpleCameraRenderer render,  Context context) {
        setEGLContextClientVersion(2);
        mRenderer = render;
        mRenderer.init(this, camera, context);
    }

    public void reBindCamera(SimpleCamera camera) {
        mRenderer.reBindCamera(camera);
    }

    public void release() {
        if (mRenderer != null) {
            mRenderer.release();
            mRenderer = null;
        }
    }

    public void releaseGLResource() {
        if (mRenderer != null) {
            mRenderer.releaseGLResource();
        }
    }
}
