package com.aliyun.maliang.android.simpleapp.camera;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * GLSurfaceView，用于展示相机camera预览
 */
public class CameraV1GLSurfaceView extends GLSurfaceView {

    private CameraV1Renderer mRenderer;

    public CameraV1GLSurfaceView(Context context) {
        super(context);
    }

    public void init(CameraV1 camera, Context context) {
        setEGLContextClientVersion(2);
        mRenderer = new CameraV1Renderer();
        mRenderer.init(this, camera, context);
    }

    public void reBindCamera(CameraV1 camera) {
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
