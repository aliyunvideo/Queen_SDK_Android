package com.alilive.alilivesdk_demo.socket;

import java.util.List;

/**
 * data:2020-08-30
 */
public class NotifyPublish {

    /**
     * anchorUserId : 1553
     * anchorRtmpPullUrl : rtmp://apsaravideo-livedemo-rtmpplay.alivecdn
     * .com/livetest/76caf8ad4120dd0dca84edfa76e6fdea?auth_key=1598792526-0-0
     * -f33fe8dc421edd0e90354ce850acf90e
     * anchorRtcPullUrl : artc://apsaravideo-livedemo-rtcplay.alivecdn
     * .com/livetest/76caf8ad4120dd0dca84edfa76e6fdea?auth_key=1598792526-0-0
     * -e7ddf26b268e17c381f4464382d4bad3
     * rtcPullUrls : []
     */

    private int anchorUserId;
    private String anchorRtmpPullUrl;
    private String anchorRtcPullUrl;
    private String anchorRtsPullUrl;
    private List<VideoUserInfo> rtcPullUrls;
    private int type;//用于标示是否进行了网络重联

    public String getAnchorRtsPullUrl() {
        return anchorRtsPullUrl;
    }

    public void setAnchorRtsPullUrl(String anchorRtsPullUrl) {
        this.anchorRtsPullUrl = anchorRtsPullUrl;
    }
    public int getAnchorUserId() { return anchorUserId;}

    public void setAnchorUserId(int anchorUserId) { this.anchorUserId = anchorUserId;}

    public String getAnchorRtmpPullUrl() { return anchorRtmpPullUrl;}

    public void setAnchorRtmpPullUrl(String anchorRtmpPullUrl) {
        this.anchorRtmpPullUrl = anchorRtmpPullUrl;
    }

    public String getAnchorRtcPullUrl() { return anchorRtcPullUrl;}

    public void setAnchorRtcPullUrl(String anchorRtcPullUrl) {
        this.anchorRtcPullUrl = anchorRtcPullUrl;
    }

    public List<VideoUserInfo> getRtcPullUrls() { return rtcPullUrls;}

    public void setRtcPullUrls(List<VideoUserInfo> rtcPullUrls) { this.rtcPullUrls = rtcPullUrls;}
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
