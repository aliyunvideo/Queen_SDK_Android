package io.agora.vlive.agora;

import androidx.annotation.NonNull;

import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtm.RtmClient;
import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.agora.rtc.AgoraRtcHandler;
import io.agora.vlive.agora.rtc.RtcEventHandler;
import io.agora.vlive.agora.rtm.RtmMessageManager;
import io.agora.vlive.utils.UserUtil;

public class AgoraEngine {
    private static final String TAG = AgoraEngine.class.getSimpleName();

    private RtcEngine mRtcEngine;
    private AgoraRtcHandler mRtcEventHandler = new AgoraRtcHandler();

    private RtmClient mRtmClient;

    public AgoraEngine(@NonNull AgoraLiveApplication application, String appId) {
        try {
            mRtcEngine = RtcEngine.create(application, appId, mRtcEventHandler);
            mRtcEngine.enableVideo();
            mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING);
            mRtcEngine.enableDualStreamMode(false);
            mRtcEngine.setLogFile(UserUtil.rtcLogFilePath(application));

            mRtmClient = RtmClient.createInstance(application, appId, RtmMessageManager.instance());
            mRtmClient.setLogFile(UserUtil.rtmLogFilePath(application));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public RtmClient rtmClient() {
        return mRtmClient;
    }

    public void registerRtcHandler(RtcEventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.registerEventHandler(handler);
    }

    public void removeRtcHandler(RtcEventHandler handler) {
        if (mRtcEventHandler != null) mRtcEventHandler.removeEventHandler(handler);
    }

    public void release() {
        if (mRtcEngine != null) RtcEngine.destroy();
        if (mRtmClient != null) {
            mRtmClient.logout(null);
            mRtmClient.release();
        }
    }
}
