package com.aliyun.maliang.android.simpleapp.CameraV1GLSurfaceView;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.widget.TextView;

import com.aliyun.maliang.android.simpleapp.CameraV1;

public class CameraV1GLSurfaceView extends GLSurfaceView {

    private CameraV1Renderer mRenderer;

    public CameraV1GLSurfaceView(Context context) {
        super(context);
    }

    public void init(CameraV1 camera, boolean isPreviewStarted, Context context) {
        setEGLContextClientVersion(2);
        mRenderer = new CameraV1Renderer();
        mRenderer.init(this, camera, isPreviewStarted, context);
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

    public void setFpsView(TextView textView) {
        if (mRenderer != null) {
            mRenderer.setFpsView(textView);
        }
    }
}
