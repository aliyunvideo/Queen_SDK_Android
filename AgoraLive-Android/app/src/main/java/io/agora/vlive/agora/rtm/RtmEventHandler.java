package io.agora.vlive.agora.rtm;

import java.util.Map;

import io.agora.rtm.RtmMessage;

public interface RtmEventHandler {
    void onConnectionStateChanged(int state, int reason);

    void onMessageReceived(RtmMessage rtmMessage, String peerId);

    void onTokenExpired();

    void onPeersOnlineStatusChanged(Map<String, Integer> map);
}
