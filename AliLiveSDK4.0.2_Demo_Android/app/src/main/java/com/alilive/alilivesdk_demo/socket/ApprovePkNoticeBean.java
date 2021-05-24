package com.alilive.alilivesdk_demo.socket;

/**
 * data:2020/9/27
 */
public class ApprovePkNoticeBean {
    private boolean approve;
    private int userId;
    private int roomId;
    private String rtcPullUrl;

    public boolean isApprove() {
        return approve;
    }

    public void setApprove(boolean approve) {
        this.approve = approve;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRtcPullUrl() {
        return rtcPullUrl;
    }

    public void setRtcPullUrl(String rtcPullUrl) {
        this.rtcPullUrl = rtcPullUrl;
    }
}
