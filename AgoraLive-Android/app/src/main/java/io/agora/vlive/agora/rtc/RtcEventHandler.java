package io.agora.vlive.agora.rtc;

import io.agora.rtc.IRtcEngineEventHandler;

public interface RtcEventHandler {
    void onRtcJoinChannelSuccess(String channel, int uid, int elapsed);

    void onRtcRemoteVideoStateChanged(int uid, int state, int reason, int elapsed);

    void onRtcStats(IRtcEngineEventHandler.RtcStats stats);

    void onRtcChannelMediaRelayStateChanged(int state, int code);

    void onRtcChannelMediaRelayEvent(int code);

    void onRtcAudioVolumeIndication(IRtcEngineEventHandler.AudioVolumeInfo[] speakers, int totalVolume);

    void onRtcAudioRouteChanged(int routing);
}
