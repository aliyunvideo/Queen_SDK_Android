package com.alilive.alilivesdk_demo.socket;

import java.util.List;

/**
 * data:2020-08-30
 */
public class ApproveMicNotice {

    /**
     * approve : true
     * anchorUserId : 1554
     * anchorRtcPullUrl : artc://apsaravideo-livedemo-rtcplay.alivecdn
     * .com/livetest/8b7c5e65a73975e65de3d3dce9244ba4?auth_key=1598785463-0-0
     * -8b5be6baafe5adc6458ee99375f87dc0
     * rtcPushUrl : artc://apsaravideo-livedemo-rtcpush.alivecdn
     * .com/livetest/a3f3794ee62a42c4d68f77422700ec33?auth_key=1598785505-0-0
     * -a62bd8269435c63e126949628686e55b
     * rtcPullUrls : []
     */

    private boolean approve;
    private int anchorUserId;
        private String anchorRtcPullUrl;
    private String rtcPushUrl;
    private List<VideoUserInfo> rtcPullUrls;

    public boolean isApprove() { return approve;}

    public void setApprove(boolean approve) { this.approve = approve;}

    public int getAnchorUserId() { return anchorUserId;}

    public void setAnchorUserId(int anchorUserId) { this.anchorUserId = anchorUserId;}

    public String getAnchorRtcPullUrl() { return anchorRtcPullUrl;}

    public void setAnchorRtcPullUrl(String anchorRtcPullUrl) {
        this.anchorRtcPullUrl = anchorRtcPullUrl;
    }

    public String getRtcPushUrl() { return rtcPushUrl;}

    public void setRtcPushUrl(String rtcPushUrl) { this.rtcPushUrl = rtcPushUrl;}

    public List<VideoUserInfo> getRtcPullUrls() { return rtcPullUrls;}

    public void setRtcPullUrls(List<VideoUserInfo> rtcPullUrls) { this.rtcPullUrls = rtcPullUrls;}
}

