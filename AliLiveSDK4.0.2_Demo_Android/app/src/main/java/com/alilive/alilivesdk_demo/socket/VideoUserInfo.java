package com.alilive.alilivesdk_demo.socket;

import android.support.annotation.Nullable;

import com.alivc.live.AliLiveRenderView;

/**
 * data:2020-08-30
 * 用户用户视频显示
 */
public class VideoUserInfo {


    /**
     * userId : 1558
     * userName : zhushiyuan111ffdshow
     * rtcPullUrl : artc://apsaravideo-livedemo-rtcplay.alivecdn
     * .com/livetest/b8ccd95ce9273a085cc63514611ce1e2?auth_key=1598794295-0-0
     * -16ccdb52f20fbc54403f47ebc2c6a0d0
     */
    private String rtcPullUrl;
    private int userId;
    private String userName;

    /**
     * 从主播界面进入的
     */
    public transient boolean fromAnchorPage = false;

    //transient 防止gson 反序列化
    /**
     * 显示视频画面的布局
     */
    private transient AliLiveRenderView renderView;
    /**
     * 是否正在拉取远端画面
     */
    private transient boolean isPulling = false;
    /**
     * 是否为本地预览窗口
     */
    private transient boolean isLocalUser = false;
    public AliLiveRenderView getRenderView() {
        return renderView;
    }
    public VideoUserInfo(){

    }
    public VideoUserInfo(String pullUrl) {
        this.rtcPullUrl = pullUrl;
    }

    public void setRenderView(AliLiveRenderView renderView) {
        this.renderView = renderView;
    }


    public boolean isPulling() {
        return isPulling;
    }

    public void setPulling(boolean pulling) {
        isPulling = pulling;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof VideoUserInfo){
            if (rtcPullUrl != null && rtcPullUrl.equals(((VideoUserInfo)obj).rtcPullUrl)){
                return true;
            }
        }
        return super.equals(obj);
    }

    public int getUserId() { return userId;}

    public void setUserId(int userId) { this.userId = userId;}

    public String getUserName() { return userName;}

    public void setUserName(String userName) { this.userName = userName;}

    public String getRtcPullUrl() { return rtcPullUrl;}

    public void setRtcPullUrl(String rtcPullUrl) { this.rtcPullUrl = rtcPullUrl;}

    public boolean isLocalUser() {
        return isLocalUser;
    }

    public void setLocalUser(boolean localUser) {
        isLocalUser = localUser;
    }
}
