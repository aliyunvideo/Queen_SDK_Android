package io.agora.framework.modules.consumers;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.util.Log;
import android.view.TextureView;

import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.capture.video.camera.VideoModule;
import io.agora.framework.modules.channels.VideoChannel;

public class TextureViewConsumer extends BaseWindowConsumer implements TextureView.SurfaceTextureListener {
    private static final String TAG = TextureViewConsumer.class.getSimpleName();

    private SurfaceTexture mSurfaceTexture;
    private int mWidth;
    private int mHeight;

    public TextureViewConsumer() {
        super(VideoModule.instance());
    }

    @Override
    public void onConsumeFrame(VideoCaptureFrame frame, VideoChannel.ChannelContext context) {
        if (mSurfaceTexture == null) {
            return;
        }

        super.onConsumeFrame(frame, context);
    }

    @Override
    public Object onGetDrawingTarget() {
        return mSurfaceTexture;
    }

    @Override
    public int onMeasuredWidth() {
        return mSurfaceTexture != null ? mWidth : 0;
    }

    @Override
    public int onMeasuredHeight() {
        return mSurfaceTexture != null ? mHeight : 0;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        Log.i(TAG, "onSurfaceTextureAvailable");
        setDefault(surface, width, height);
        connectChannel(CHANNEL_ID);
    }

    public void setDefault(SurfaceTexture surface, int width, int height) {
        mSurfaceTexture = surface;
        surfaceDestroyed = false;
        needResetSurface = true;
        setSize(width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mvpInit = false;
        setSize(width, height);
        needResetSurface = true;
    }

    private void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        mSurfaceTexture.setDefaultBufferSize(mWidth, mHeight);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        disconnectChannel(CHANNEL_ID);
        surfaceDestroyed = true;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        // Log.i(TAG, "onSurfaceTextureUpdated");
    }
}
