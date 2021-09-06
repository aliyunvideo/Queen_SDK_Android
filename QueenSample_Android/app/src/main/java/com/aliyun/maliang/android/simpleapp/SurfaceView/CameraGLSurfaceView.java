package com.aliyun.maliang.android.simpleapp.SurfaceView;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.aliyun.maliang.android.simpleapp.CameraV1;

/**
 * GLSurfaceView，用于展示相机camera预览
 */
public class CameraGLSurfaceView extends GLSurfaceView {

    private CameraRenderer mRenderer;

    public CameraGLSurfaceView(Context context) {
        super(context);
    }

    public void init(CameraV1 camera, Context context) {
        setEGLContextClientVersion(2);
        mRenderer = new CameraRenderer();
        mRenderer.init(this, camera, context);
        setRenderer(mRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);
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
