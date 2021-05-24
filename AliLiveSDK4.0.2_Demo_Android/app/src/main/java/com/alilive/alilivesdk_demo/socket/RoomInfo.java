package com.alilive.alilivesdk_demo.socket;

/**
 * data:2020-08-27
 */
public class RoomInfo {

    /**
     * anchorUserId : 666
     * rtmpPushUrl :
     * rtmpPullUrl : rtmp://apsaravideo-livedemo-rtmpplay.alivecdn
     * .com/livetest/42f743aa1d750583d967a9c69fcf622d?auth_key=1598697590-0-0
     * -372c68b7460b8f71aafb719cbc18eaaf
     * rtcPushUrl : artc://apsaravideo-livedemo-rtcpush.alivecdn
     * .com/livetest/42f743aa1d750583d967a9c69fcf622d?auth_key=1598697590-0-0
     * -cce18592c571f5920bb4f506c75a50c6
     * allowMic : true
     * isPublished : false
     */

    private int anchorUserId;
    private String rtmpPushUrl;
    private String rtmpPullUrl;
    private String rtcPushUrl;
    private String rongToken;
    private String rtsPullUrl;
    private String token;
    private boolean allowMic;
    private boolean isPublished;
    private String flvPullUrl;

    public String getFlvPullUrl() {
        return flvPullUrl;
    }

    public void setFlvPullUrl(String flvPullUrl) {
        this.flvPullUrl = flvPullUrl;
    }
    public int getAnchorUserId() { return anchorUserId;}

    public void setAnchorUserId(int anchorUserId) { this.anchorUserId = anchorUserId;}

    public String getRtmpPushUrl() { return rtmpPushUrl;}

    public void setRtmpPushUrl(String rtmpPushUrl) { this.rtmpPushUrl = rtmpPushUrl;}

    public String getRtmpPullUrl() { return rtmpPullUrl;}

    public void setRtmpPullUrl(String rtmpPullUrl) { this.rtmpPullUrl = rtmpPullUrl;}

    public String getRtcPushUrl() { return rtcPushUrl;}

    public void setRtcPushUrl(String rtcPushUrl) { this.rtcPushUrl = rtcPushUrl;}

    public boolean isAllowMic() { return allowMic;}

    public void setAllowMic(boolean allowMic) { this.allowMic = allowMic;}

    public boolean isPublished() { return isPublished;}

    public void setIsPublished(boolean isPublished) { this.isPublished = isPublished;}

    public String getRongToken() {
        return rongToken;
    }

    public void setRongToken(String rongToken) {
        this.rongToken = rongToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRtsPullUrl() {
        return rtsPullUrl;
    }

    public void setRtsPullUrl(String rtsPullUrl) {
        this.rtsPullUrl = rtsPullUrl;
    }

}
