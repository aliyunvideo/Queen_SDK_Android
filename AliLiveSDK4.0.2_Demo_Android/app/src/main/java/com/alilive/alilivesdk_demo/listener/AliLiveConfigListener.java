package com.alilive.alilivesdk_demo.listener;

import com.alivc.live.AliLiveConstants;

public interface AliLiveConfigListener {

    /**
     * 耳返开关
     */
    void onEarbackChanged(boolean enableEarback);

    /**
     * 耳返音量
     * @param volume  音量 0~100 默认100
     */
    void onEarbackVolume(int volume);

    /**
     * 音调高低
     * @param value  默认值是1.0f，范围[0.5, 2.0]
     */
    void onPicthValue(float value);

    /**
     * 混响模式
     */
    void onAliLiveReverbMode(AliLiveConstants.AliLiveReverbMode reverbMode);

    /**
     * 变声模式
     */
    void onAliLiveVoiceChangerMode(AliLiveConstants.AliLiveVoiceChangerMode voiceChangerMode);
}
