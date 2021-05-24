package com.alilive.alilivesdk_demo.socket;

/**
 * data:2020-08-29
 */
public class Payload<T> {

    /**
     * code : 0
     * codeMsg :
     * data : {"anchorUserId":666,"rtmpPushUrl":"",
     * "rtmpPullUrl":"rtmp://apsaravideo-livedemo-rtmpplay.alivecdn
     * .com/livetest/42f743aa1d750583d967a9c69fcf622d?auth_key=1598697590-0-0
     * -372c68b7460b8f71aafb719cbc18eaaf","rtcPushUrl":"artc://apsaravideo-livedemo-rtcpush
     * .alivecdn.com/livetest/42f743aa1d750583d967a9c69fcf622d?auth_key=1598697590-0-0
     * -cce18592c571f5920bb4f506c75a50c6","allowMic":true,"isPublished":false}
     */

    private int code;
    private String codeMsg;
    private T data;

    public int getCode() { return code;}

    public void setCode(int code) { this.code = code;}

    public String getCodeMsg() { return codeMsg;}

    public void setCodeMsg(String codeMsg) { this.codeMsg = codeMsg;}

    public T getData() { return data;}

    public void setData(T data) { this.data = data;}

}
