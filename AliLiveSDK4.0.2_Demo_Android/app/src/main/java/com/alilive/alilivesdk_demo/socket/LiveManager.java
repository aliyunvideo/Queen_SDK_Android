package com.alilive.alilivesdk_demo.socket;

/**
 * data:2020-08-27
 */
public class LiveManager {
    public static final String URL = "ws://11.164.235.97:8089/live";
    WebSocketHandler mSocketHandler;

    private void LiveManager(){

    }

    private static LiveManager INST;

    public static LiveManager getInstance() {
        if (INST == null) {
            synchronized (WebSocketHandler.class) {
                INST = new LiveManager();
            }
        }

        return INST;
    }

    public void joinRoom(UserInfo userInfo){


    }

}
