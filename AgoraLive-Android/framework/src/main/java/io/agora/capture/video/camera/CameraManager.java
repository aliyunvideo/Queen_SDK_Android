package io.agora.capture.video.camera;

import android.content.Context;
import android.view.SurfaceView;
import android.view.TextureView;

import io.agora.framework.modules.channels.ChannelManager;
import io.agora.framework.modules.consumers.IVideoConsumer;
import io.agora.framework.modules.consumers.SurfaceViewConsumer;
import io.agora.framework.modules.consumers.TextureViewConsumer;
import io.agora.framework.modules.processors.IPreprocessor;

/**
 * VideoManager is designed as the up-level encapsulation of
 * video module. It opens a series of APIs to the outside world,
 * and makes camera behavior much easier by containing some of
 * the camera logical procedures.
 * It can be seen as a particular utility class to control the
 * camera video channel, which is defined as one implementation
 * of the video channel designed in the framework.
 * Maintaining a single CameraManager instance globally is enough.
 * Although it is ok to create an instance every time it needs
 * control over cameras, such behavior is unlikely to bring benefits.
 */
public class CameraManager {
    // CameraManager only controls camera channel
    private static final int CHANNEL_ID = ChannelManager.ChannelID.CAMERA;

    private static final int DEFAULT_FACING = Constant.CAMERA_FACING_FRONT;

    private CameraVideoChannel mCameraChannel;

    /**
     * Initializes the camera video channel, loads all the
     * resources needed during camera capturing.
     * @param context Android context
     * @param preprocessor usually is the implementation
     *                     of a third-party beautification library
     * @param facing must be one of Constant.CAMERA_FACING_FRONT
     *               and Constant.CAMERA_FACING_BACK
     * @see io.agora.capture.video.camera.Constant
     */
    public CameraManager(Context context, IPreprocessor preprocessor, int facing) {
        init(context, preprocessor, facing);
    }

    public CameraManager(Context context, IPreprocessor preprocessor) {
        init(context, preprocessor, DEFAULT_FACING);
    }

    /**
     * Initializes the camera video channel, loads all the
     * resources needed during camera capturing.
     * @param context Android context
     * @param preprocessor usually is the implementation
     *                     of a third-party beautification library
     * @param facing must be one of Constant.CAMERA_FACING_FRONT
     *               and Constant.CAMERA_FACING_BACK
     * @see io.agora.capture.video.camera.Constant
     */
    private void init(Context context, IPreprocessor preprocessor, int facing) {
        VideoModule videoModule = VideoModule.instance();
        if (!videoModule.hasInitialized()) {
            videoModule.init(context);
        }

        // The preprocessor must be set before
        // the video channel starts
        videoModule.setPreprocessor(CHANNEL_ID, preprocessor);
        videoModule.startChannel(CHANNEL_ID);
        mCameraChannel = (CameraVideoChannel)
                videoModule.getVideoChannel(CHANNEL_ID);
        mCameraChannel.setFacing(facing);
    }

    public void enablePreprocessor(boolean enabled) {
        if (mCameraChannel != null) {
            mCameraChannel.enablePreProcess(enabled);
        }
    }

    /**
     * Set camera preview. The view must be set before
     * attached to the window.
     * Currently only the latest preview set will display
     * local videos
     * If the TextureView is detached from the window,
     * it's previewing will be automatically stopped and it
     * is removed from the consumer list.
     * @param textureView
     */
    public void setLocalPreview(TextureView textureView) {
        TextureViewConsumer consumer = new TextureViewConsumer();
        textureView.setSurfaceTextureListener(consumer);

        if (textureView.isAttachedToWindow()) {
            consumer.setDefault(textureView.getSurfaceTexture(),
                    textureView.getMeasuredWidth(),
                    textureView.getMeasuredHeight());
            consumer.connectChannel(CHANNEL_ID);
        }
    }

    /**
     * Set camera preview. The view must be set before
     * attached to the window.
     * Currently only the latest preview set will display
     * local videos
     * If the SurfaceView is detached from the window, it's
     * previewing will be automatically stopped and it
     * is removed from the consumer list.
     * @param surfaceView
     */
    public void setLocalPreview(SurfaceView surfaceView) {
        SurfaceViewConsumer consumer = new SurfaceViewConsumer(surfaceView);
        surfaceView.getHolder().addCallback(consumer);

        if (surfaceView.isAttachedToWindow()) {
            consumer.setDefault();
            consumer.connectChannel(CHANNEL_ID);
        }
    }

    public void setCameraStateListener(CameraVideoChannel.OnCameraStateListener listener) {
        if (mCameraChannel != null) {
            mCameraChannel.setCameraStateListener(listener);
        }
    }

    public void setFacing(int facing) {
        if (mCameraChannel != null) {
            mCameraChannel.setFacing(facing);
        }
    }

    public void setPictureSize(int width, int height) {
        if (mCameraChannel != null) {
            mCameraChannel.setPictureSize(width, height);
        }
    }

    /**
     * Attach an off-screen consumer to the camera channel.
     * The consumer does not render on-screen frames.
     * The on-screen and off-screen consumers can be
     * attached and detached dynamically without affecting
     * the others.
     * @param consumer the consumer implementation
     */
    public void attachOffScreenConsumer(IVideoConsumer consumer) {
        if (mCameraChannel != null) {
            mCameraChannel.connectConsumer(consumer, IVideoConsumer.TYPE_OFF_SCREEN);
        }
    }

    public void detachOffScreenConsumer(IVideoConsumer consumer) {
        if (mCameraChannel != null) {
            mCameraChannel.disconnectConsumer(consumer);
        }
    }

    /**
     * Set the desired frame rate of the capture.
     * If not set, the default frame rate is 24
     * @param frameRate
     */
    public void setFrameRate(int frameRate) {
        if (mCameraChannel != null) {
            mCameraChannel.setIdealFrameRate(frameRate);
        }
    }

    public void startCapture() {
        if (mCameraChannel != null) {
            mCameraChannel.startCapture();
        }
    }

    public void stopCapture() {
        if (mCameraChannel != null) {
            mCameraChannel.stopCapture();
        }
    }

    public void switchCamera() {
        if (mCameraChannel != null) {
            mCameraChannel.switchCamera();
        }
    }

    public IPreprocessor getPreprocessor() {
        if (mCameraChannel != null) {
            return VideoModule.instance().getPreprocessor(CHANNEL_ID);
        }

        return null;
    }
}
