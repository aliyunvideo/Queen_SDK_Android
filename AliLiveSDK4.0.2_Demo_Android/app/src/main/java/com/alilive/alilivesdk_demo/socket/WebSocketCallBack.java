package com.alilive.alilivesdk_demo.socket;


/**
 * data:2020-08-27
 */
public interface WebSocketCallBack {
    void onOpen();
    void onMessage(String msg);
    void onClose();
    void onConnectError(Throwable t);
}
