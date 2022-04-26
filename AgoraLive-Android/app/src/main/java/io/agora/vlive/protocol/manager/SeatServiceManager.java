package io.agora.vlive.protocol.manager;

import android.text.TextUtils;

import com.elvishew.xlog.XLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.agora.vlive.AgoraLiveApplication;
import io.agora.vlive.protocol.model.request.Request;
import io.agora.vlive.protocol.model.request.SeatInteractionRequest;
import io.agora.vlive.protocol.model.types.SeatInteraction;

/**
 * Manages seat requests, including interactions between
 * room owners and audience, and seat state changes.
 */
public class SeatServiceManager {
    public static class SeatApplicationUserInfo {
        public String userId;
        public String userName;

        public SeatApplicationUserInfo(String userId, String userName) {
            this.userId = userId;
            this.userName = userName;
        }
    }

    private AgoraLiveApplication mApplication;

    private HashMap<String, Long> mInvitingList = new HashMap<>();

    private ArrayList<SeatApplicationUserInfo> mApplicationList = new ArrayList<>();
    private HashMap<String, SeatApplicationUserInfo> mApplicationUserIdMap = new HashMap<>();

    public void addToInvitingList(String userId, long processId) {
        mInvitingList.remove(userId);
        mInvitingList.put(userId, processId);
    }

    public void removeFromInvitingList(String userId) {
        mInvitingList.remove(userId);
    }

    public void addToApplicationList(String userId, String userName) {
        if (!mApplicationUserIdMap.containsKey(userId)) {
            SeatApplicationUserInfo info = new SeatApplicationUserInfo(userId, userName);
            mApplicationList.add(info);
            mApplicationUserIdMap.put(userId, info);
        }
    }

    public void removeFromApplicationList(String userId) {
        SeatApplicationUserInfo info = mApplicationUserIdMap.remove(userId);
        if (info != null) {
            mApplicationList.remove(info);
        }
    }

    public List<SeatApplicationUserInfo> getAudienceApplication() {
        return mApplicationList;
    }

    public void clearAllList() {
        mInvitingList.clear();
        mApplicationList.clear();
    }

    public boolean userIsInvited(String userId) {
        return !mInvitingList.isEmpty() && mInvitingList.containsKey(userId);
    }

    public SeatServiceManager(AgoraLiveApplication application) {
        mApplication = application;
    }

    private void sendRequest(String token, String roomId, String userId, int seatNo, int type) {
        mApplication.proxy().sendRequest(Request.SEAT_INTERACTION,
                new SeatInteractionRequest(token, roomId, userId, seatNo, type));
    }

    private String getValidToken(String errorMessage) {
        String token = mApplication.config().getUserProfile().getToken();
        if (TextUtils.isEmpty(token)) {
            XLog.e(errorMessage);
            token = null;
        }

        return token;
    }

    public void invite(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager owner invite token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.OWNER_INVITE);
        }
    }

    public void apply(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager user apply token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.AUDIENCE_APPLY);
        }
    }

    public void ownerReject(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager owner reject token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.OWNER_REJECT);
        }
    }

    public void audienceReject(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager audience reject token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.AUDIENCE_REJECT);
        }
    }

    public void ownerAccept(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager owner accept token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.OWNER_ACCEPT);
        }
    }

    public void audienceAccept(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager audience accept token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.AUDIENCE_ACCEPT);
        }
    }

    public void forceLeave(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager force leave token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.OWNER_FORCE_LEAVE);
        }
    }

    public void hostLeave(String roomId, String userId, int seatNo) {
        String token = getValidToken("SeatManager host leave token invalid");
        if (token != null) {
            sendRequest(token, roomId, userId, seatNo, SeatInteraction.HOST_LEAVE);
        }
    }
}