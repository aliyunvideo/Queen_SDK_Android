package com.alilive.alilivesdk_demo.socket;

/**
 * data:2020-08-31
 */
public class SocketConstants {
    // 进出房间推拉流相关

    public static final String CMD_APPLY_MIC = "ApplyMic";
    public static final String CMD_JOIN_ROOM = "JoinRoom";
    public static final String CMD_PUBLISH = "Publish";
    public static final String CMD_NOTIFY_PUBLISH = "NotifyPublish";
    public static final String CMD_UNPUBLISH = "UnPublish";
    public static final String CMD_LEAVE_ROOM = "LeaveRoom";
    public static final String CMD_REFRESH_USER_STREAM_URL = "RefreshUserStreamUrl";//刷新url

    // 主播PK相关
    public static final String CMD_GET_ROOM_LIST = "GetOnlineRoomList";
    public static final String CMD_APPLY_PK = "ApplyPk";
    public static final String CMD_APPLY_PK_NOTICE = "ApplyPkNotice";
    public static final String CMD_APPROVE_PK = "ApprovePk";
    public static final String CMD_APPROVE_PK_NOTICE = "ApprovePkNotice";
    public static final String CMD_CANCEL_PK = "CancelPk";
    public static final String CMD_CANCEL_PK_NOTICE = "CancelPkNotice";

    // 连麦相关
    public static final String CMD_APPROVE_MIC = "ApproveMic";
    public static final String CMD_APPROVE_MIC_NOTICE = "ApproveMicNotice";
    public static final String CMD_APPLY_MIC_NOTICA = "ApplyMicNotice";

    public static final String TAG = "WebSocketHandler";
}
