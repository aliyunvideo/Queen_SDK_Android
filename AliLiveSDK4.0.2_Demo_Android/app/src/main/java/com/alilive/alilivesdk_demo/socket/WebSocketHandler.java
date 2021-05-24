package com.alilive.alilivesdk_demo.socket;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * data:2020-08-27
 */
public class WebSocketHandler extends WebSocketListener {
    private static final String TAG = "WebSocketHandler ";

    private String wsUrl;

    private WebSocket webSocket;
    private ConnectStatus status;
    private int mUserId = 0;
    private String token = "";
    private int mIntervalTime = 30*1000;
    private int mConnectTime = 3;
    private OkHttpClient client = new OkHttpClient.Builder()
        .build();

    private WebSocketHandler() {
    }

    private static WebSocketHandler INST;

    public static WebSocketHandler getInstance() {
        if (INST == null) {
            synchronized (WebSocketHandler.class) {
                INST = new WebSocketHandler();
            }
        }

        return INST;
    }

    public ConnectStatus getStatus() {
        return status;
    }

    public void connect(String wsUrl) {
        this.wsUrl = wsUrl;
        //构造request对象
        Request.Builder builder = new Request.Builder();
        try {
            builder.url(wsUrl);
            Request request = builder.build();
            webSocket = client.newWebSocket(request, this);
        }catch (Exception e) {
            Log.e(TAG, e.getMessage().toString());
        }
        status = ConnectStatus.Connecting;
    }

    public void reConnect() {
        if (webSocket != null) {
            webSocket = client.newWebSocket(webSocket.request(), this);
        }
        status = ConnectStatus.Reconnecting;
    }

    public void send(String text) {
        if (webSocket != null) {
            log("send： " + text);
            webSocket.send(text);
        }
    }

    public void cancel() {
        if (webSocket != null) {
            webSocket.cancel();
        }
    }

    public void close() {
        if (webSocket != null) {
            webSocket.close(1000, null);
        }
        mHeartbeatHandler.removeMessages(MSG_RECONNECT);
        mHeartbeatHandler.removeMessages(MSG_HEARTBEAT_SEND);
    }

    @Override
    public void onOpen(WebSocket webSocket, Response response) {
        super.onOpen(webSocket, response);
        log("onOpen");
        //if (this.status!=ConnectStatus.Reconnecting){
        //    if (mSocketIOCallBack != null) {
        //        mSocketIOCallBack.onOpen();
        //    }
        //}
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onOpen();
        }
        this.status = ConnectStatus.Open;

    }

    @Override
    public void onMessage(WebSocket webSocket, String text) {
        super.onMessage(webSocket, text);
        log("onMessage: " + text);
        String cmd = "";
        try {
            JSONObject object = new JSONObject(text);
            cmd = object.getString("cmd");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (cmd.equals("Ping")){
            mConnectTime = 3;
            mHeartbeatHandler.sendEmptyMessage(MSG_INTERVAL_TIME_UPDATE);
        }else {
            if (mSocketIOCallBack != null) {
                mSocketIOCallBack.onMessage(text);
            }
        }

    }

    @Override
    public void onMessage(WebSocket webSocket, ByteString bytes) {
        super.onMessage(webSocket, bytes);
    }

    @Override
    public void onClosing(WebSocket webSocket, int code, String reason) {
        super.onClosing(webSocket, code, reason);
        this.status = ConnectStatus.Closing;
        log("onClosing");
    }

    @Override
    public void onClosed(WebSocket webSocket, int code, String reason) {
        super.onClosed(webSocket, code, reason);
        log("onClosed");
        this.status = ConnectStatus.Closed;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onClose();
        }
    }

    @Override
    public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
        log("onFailure: " + t.toString());
        t.printStackTrace();
        this.status = ConnectStatus.Canceled;
        if (mSocketIOCallBack != null) {
            mSocketIOCallBack.onConnectError(t);
        }
    }
    private void log(String s){
        Log.e(TAG, s);
    }

    private WebSocketCallBack mSocketIOCallBack;

    public void setSocketIOCallBack(WebSocketCallBack callBack) {
        mSocketIOCallBack = callBack;
    }

    public void removeSocketIOCallBack() {
        mSocketIOCallBack = null;
    }
    public void startHeartbeat(int userId,String token){
        this.mUserId = userId;
        this.token = token;
        mHeartbeatHandler.sendEmptyMessage(MSG_HEARTBEAT_START);

    }

    /**
     * 开始心跳
     */
    private static final int MSG_HEARTBEAT_START = 0;
    /**
     * 发送心跳
     */
    private static final int MSG_HEARTBEAT_SEND = 1;
    /**
     * 进行重连
     */
    private static final int MSG_RECONNECT = 2;
    /**
     * 更新重连倒计时
     */
    private static final int MSG_INTERVAL_TIME_UPDATE = 3;
    /**
     * 取消发送心跳
     */
    private static final int MSG_CANCEL = 4;

    private Handler mHeartbeatHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MSG_HEARTBEAT_START:
                    mHeartbeatHandler.sendEmptyMessage(MSG_HEARTBEAT_SEND);
                    mHeartbeatHandler.sendEmptyMessageDelayed(MSG_RECONNECT,mIntervalTime+1000);
                    break;
                case MSG_HEARTBEAT_SEND:
                    // 发送心跳消息
                    send(getSendMessage());
                    mHeartbeatHandler.sendEmptyMessageDelayed(MSG_HEARTBEAT_SEND,mIntervalTime);
                    break;
                case MSG_RECONNECT:
                    if (mConnectTime>0){
                        //reConnect();
                        mHeartbeatHandler.sendEmptyMessageDelayed(MSG_RECONNECT,mIntervalTime+1000);
                        mConnectTime--;
                    }else {
                        close();
                    }
                    break;
                case MSG_INTERVAL_TIME_UPDATE:
                    mHeartbeatHandler.removeMessages(MSG_RECONNECT);
                    mHeartbeatHandler.sendEmptyMessageDelayed(MSG_RECONNECT,mIntervalTime+1000);
                    break;
                case MSG_CANCEL:
                    mHeartbeatHandler.removeMessages(MSG_RECONNECT);
                    mHeartbeatHandler.removeMessages(MSG_HEARTBEAT_SEND);
                    break;
                default:
                    break;
            }
        }
    };
    private String getSendMessage(){
        String jsonString = "";
        try {
            JSONObject object = new JSONObject();
            object.put("cmd", "Ping");
            object.put("userId",""+mUserId);
            object.put("token",token);
            jsonString = object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonString;

    }
    public enum ConnectStatus {
        Connecting, // the initial state of each web socket.
        Reconnecting,
        Open, // the web socket has been accepted by the remote peer
        Closing, // one of the peers on the web socket has initiated a graceful shutdown
        Closed, //  the web socket has transmitted all of its messages and has received all messages from the peer
        Canceled // the web socket connection failed
    }
}

