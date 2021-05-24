package com.alilive.alilivesdk_demo.socket;

import java.util.List;

/**
 * data:2020/9/27
 */
public class OnLineRoomListBean {

    private List<OnlineRoomBean> onlineRoomList;

    public List<OnlineRoomBean> getOnlineRoomList() { return onlineRoomList;}

    public void setOnlineRoomList(List<OnlineRoomBean> onlineRoomList) {
        this.onlineRoomList = onlineRoomList;
    }

    public static class OnlineRoomBean {
        /**
         * roomId : 2003
         * userId : 577
         * userName : 用户16807
         */

        private int roomId;
        private int userId;
        private String userName;

        public int getRoomId() { return roomId;}

        public void setRoomId(int roomId) { this.roomId = roomId;}

        public int getUserId() { return userId;}

        public void setUserId(int userId) { this.userId = userId;}

        public String getUserName() { return userName;}

        public void setUserName(String userName) { this.userName = userName;}
    }
}
