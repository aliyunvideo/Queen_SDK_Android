package com.alilive.alilivesdk_demo.socket;

import android.support.annotation.Nullable;

/**
 * data:2020-08-27
 */
public class UserInfo {

    /**
     * userId : 3
     * roomId : 3
     * userName : 用户名
     * role : anchor或audience
     */

    private int userId;
    private int roomId;
    private String userName;
    private String role;


    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof UserInfo){
            if (this.userId==((UserInfo)obj).userId){
                return true;
            }
        }
        return false;
    }

    public int getUserId() { return userId;}

    public void setUserId(int userId) { this.userId = userId;}

    public int getRoomId() { return roomId;}

    public void setRoomId(int roomId) { this.roomId = roomId;}

    public String getUserName() { return userName;}

    public void setUserName(String userName) { this.userName = userName;}

    public String getRole() { return role;}

    public void setRole(String role) { this.role = role;}

}
