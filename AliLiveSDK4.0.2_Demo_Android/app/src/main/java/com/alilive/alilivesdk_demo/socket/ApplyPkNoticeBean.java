package com.alilive.alilivesdk_demo.socket;

/**
 * data:2020/9/27
 */
public class ApplyPkNoticeBean {
    private int fromUserId;
    private int fromRoomId;
    private String fromUserName;
    private String fromRtcPullUrl;

    public int getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(int fromUserId) {
        this.fromUserId = fromUserId;
    }

    public int getFromRoomId() {
        return fromRoomId;
    }

    public void setFromRoomId(int fromRoomId) {
        this.fromRoomId = fromRoomId;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getFromRtcPullUrl() {
        return fromRtcPullUrl;
    }

    public void setFromRtcPullUrl(String fromRtcPullUrl) {
        this.fromRtcPullUrl = fromRtcPullUrl;
    }
}
