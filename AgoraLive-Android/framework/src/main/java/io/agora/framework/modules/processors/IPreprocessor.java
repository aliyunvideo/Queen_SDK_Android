package io.agora.framework.modules.processors;

import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.framework.modules.channels.VideoChannel;

public interface IPreprocessor {
    VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context);

    void initPreprocessor();

    void enablePreProcess(boolean enabled);

    void releasePreprocessor(VideoChannel.ChannelContext context);

    void setBlurValue(float blur);

    void setWhitenValue(float whiten);

    void setCheekValue(float cheek);

    void setEyeValue(float eye);
}
