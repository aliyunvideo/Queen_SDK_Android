package io.agora.vlive.agora.rtm;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.agora.rtm.ResultCallback;
import io.agora.rtm.RtmChannel;
import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelListener;
import io.agora.rtm.RtmChannelMember;
import io.agora.rtm.RtmClient;
import io.agora.rtm.RtmClientListener;
import io.agora.rtm.RtmMessage;
import io.agora.rtm.SendMessageOptions;
import io.agora.vlive.agora.rtm.model.ChatMessage;
import io.agora.vlive.agora.rtm.model.GiftMessage;
import io.agora.vlive.agora.rtm.model.GiftRankMessage;
import io.agora.vlive.agora.rtm.model.NotificationMessage;
import io.agora.vlive.agora.rtm.model.OwnerStateMessage;
import io.agora.vlive.agora.rtm.model.PKInvitationMessage;
import io.agora.vlive.agora.rtm.model.PKStateMessage;
import io.agora.vlive.agora.rtm.model.ProductPurchasedMessage;
import io.agora.vlive.agora.rtm.model.ProductStatedChangedMessage;
import io.agora.vlive.agora.rtm.model.SeatInteractionMessage;
import io.agora.vlive.agora.rtm.model.SeatStateMessage;
import io.agora.vlive.protocol.model.types.PKConstant;
import io.agora.vlive.protocol.model.types.SeatInteraction;

public class RtmMessageManager implements RtmClientListener, RtmChannelListener {
    private static final String TAG = RtmMessageManager.class.getSimpleName();

    private static final int PEER_MSG_TYPE_SEAT = 1;
    private static final int PEER_MSG_TYPE_PK = 2;
    private static final int PEER_MSG_TYPE_OWNER_PK_NOTIFY = 3;

    public static final int CHANNEL_MSG_TYPE_CHAT = 1;

    // Users enter or leave the room
    private static final int CHANNEL_MSG_TYPE_NOTIFY = 2;

    // Where the UI needs to show the user rank of gift values
    private static final int CHANNEL_MSG_TYPE_GIFT_RANK = 3;

    // Notifies that the room owner has changed his state
    private static final int CHANNEL_MSG_CMD_OWNER_STATE = 4;

    // Notifies that the seats' states have changed,
    // for multi-hosted rooms only
    private static final int CHANNEL_MSG_TYPE_SEAT = 5;

    // Notifies the PK states, for PK rooms only
    private static final int CHANNEL_MSG_TYPE_PK = 6;

    private static final int CHANNEL_MSG_TYPE_GIFT = 7;

    private static final int CHANNEL_MSG_TYPE_LEAVE = 8;

    private static final int CHANNEL_MSG_TYPE_PRODUCT_STATE_PURCHASED = 9;

    private static final int CHANNEL_MSG_TYPE_PRODUCT_STATE_CHANGED = 10;

    private static final int PEER_MSG_CMD_PK = 201;
    private static final int PEER_MSG_CMD_PK_REJECT = 202;
    private static final int PEER_MSG_CMD_PK_ACCEPT = 203;

    private volatile static RtmMessageManager sInstance;

    private RtmClient mRtmClient;
    private RtmChannel mRtmChannel;
    private SendMessageOptions mOptions;
    private List<RtmMessageListener> mMessageListeners;
    private Handler mHandler;

    private RtmMessageManager() {
        mOptions = new SendMessageOptions();
        mOptions.enableOfflineMessaging = false;
        mOptions.enableHistoricalMessaging = false;
        mMessageListeners = new ArrayList<>();
    }

    public static RtmMessageManager instance() {
        if (sInstance == null) {
            synchronized (RtmMessageManager.class) {
                if (sInstance == null) {
                    sInstance = new RtmMessageManager();
                }
            }
        }
        return sInstance;
    }

    public void init(RtmClient client) {
        mRtmClient = client;
    }

    public synchronized void joinChannel(String channel, ResultCallback<Void> callback) {
        if (mRtmChannel != null || mRtmClient == null) {
            return;
        }

        mRtmChannel = mRtmClient.createChannel(channel, this);
        mRtmChannel.join(callback);
    }

    public synchronized void leaveChannel(ResultCallback<Void> callback) {
        if (mRtmChannel == null) return;
        mRtmChannel.leave(callback);
        mRtmChannel.release();
        mRtmChannel = null;
    }

    private void sendChannelMessage(String message, ResultCallback<Void> callback) {
        if (mRtmChannel == null) return;
        RtmMessage msg = mRtmClient.createMessage(message);
        mRtmChannel.sendMessage(msg, mOptions, callback);
    }

    public void sendChatMessage(String userId, String nickname, String content, ResultCallback<Void> callback) {
        String json = getChatMessageJsonString(userId, nickname, content);
        sendChannelMessage(json, callback);
    }

    private String getChatMessageJsonString(String userId, String nickname, String content) {
        ChatMessage data = new ChatMessage(userId, nickname, content);
        return new GsonBuilder().create().toJson(data);
    }

    public void registerMessageHandler(RtmMessageListener handler) {
        if (!mMessageListeners.contains(handler)) mMessageListeners.add(handler);
    }

    public void removeMessageHandler(RtmMessageListener handler) {
        mMessageListeners.remove(handler);
    }

    public void setCallbackThread(Handler handler) {
        mHandler = handler;
    }

    @Override
    public void onConnectionStateChanged(int state, int reason) {
        for (RtmMessageListener handler : mMessageListeners) {
            handler.onRtmConnectionStateChanged(state, reason);
        }
    }

    @Override
    public void onMessageReceived(RtmMessage rtmMessage, final String peerId) {
        String rtmMessageString = rtmMessage.getText();
        XLog.d("peer message: " + rtmMessageString);

        try {
            JSONObject obj = new JSONObject(rtmMessageString);
            int cmd = obj.getInt("cmd");
            switch (cmd) {
                case PEER_MSG_TYPE_SEAT:
                    SeatInteractionMessage seatMessage = new GsonBuilder().create().
                            fromJson(rtmMessageString, SeatInteractionMessage.class);
                    handleSeatPeerMessageHandler(seatMessage);
                    break;
                case PEER_MSG_TYPE_PK:
                    PKInvitationMessage pkInvitationMessage = new GsonBuilder().create().fromJson(
                            rtmMessageString, PKInvitationMessage.class);
                    handlePKInvitationMessageHandler(pkInvitationMessage);
                    break;
                case PEER_MSG_TYPE_OWNER_PK_NOTIFY:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleSeatPeerMessageHandler(SeatInteractionMessage message) {
        if (mHandler != null) {
            mHandler.post(() -> handleSeatPeerMessage(message));
        } else {
            handleSeatPeerMessage(message);
        }
    }

    private void handleSeatPeerMessage(SeatInteractionMessage message) {
        String userId = message.data.fromUser.userId;
        String userName = message.data.fromUser.userName;
        int seatNo = message.data.no;
        for (RtmMessageListener listener : mMessageListeners) {
            switch (message.data.type) {
                case SeatInteraction.OWNER_INVITE:
                    listener.onRtmSeatInvited(userId, userName, seatNo);
                    break;
                case SeatInteraction.AUDIENCE_APPLY:
                    listener.onRtmSeatApplied(userId, userName, seatNo);
                    break;
                case SeatInteraction.OWNER_REJECT:
                    listener.onRtmApplicationRejected(message.data.processId, userId, userName, seatNo);
                    break;
                case SeatInteraction.AUDIENCE_REJECT:
                    listener.onRtmInvitationRejected(message.data.processId, userId, userName, seatNo);
                    break;
                case SeatInteraction.OWNER_ACCEPT:
                    listener.onRtmApplicationAccepted(message.data.processId, userId, userName, seatNo);
                    break;
                case SeatInteraction.AUDIENCE_ACCEPT:
                    listener.onRtmInvitationAccepted(message.data.processId, userId, userName, seatNo);
                    break;
                case SeatInteraction.OWNER_FORCE_LEAVE:
                    listener.onRtmOwnerForceLeaveSeat(userId, userName, seatNo);
                    break;
                case SeatInteraction.HOST_LEAVE:
                    listener.onRtmHostLeaveSeat(userId, userName, seatNo);
                    break;
            }
        }
    }

    private void handlePKInvitationMessageHandler(PKInvitationMessage message) {
        if (mHandler != null) {
            mHandler.post(() -> handlePKInvitationMessage(message));
        } else {
            handlePKInvitationMessage(message);
        }
    }

    private void handlePKInvitationMessage(PKInvitationMessage message) {
        String roomId = message.data.fromRoom.roomId;
        String userId = message.data.fromRoom.owner.userId;
        String userName = message.data.fromRoom.owner.userName;
        for (RtmMessageListener listener : mMessageListeners) {
            switch (message.data.type) {
                case PKConstant.PK_BEHAVIOR_INVITE:
                    listener.onRtmPkReceivedFromAnotherHost(userId, userName, roomId);
                    break;
                case PKConstant.PK_BEHAVIOR_ACCEPT:
                    listener.onRtmPkAcceptedByTargetHost(userId, userName, roomId);
                    break;
                case PKConstant.PK_BEHAVIOR_REJECT:
                    listener.onRtmPkRejectedByTargetHost(userId, userName, roomId);
                    break;
                case PKConstant.PK_BEHAVIOR_TIMEOUT:
                    break;
            }
        }
    }

    @Override
    public void onTokenExpired() {
        for (RtmMessageListener listener : mMessageListeners) {
            listener.onRtmTokenExpired();
        }
    }

    @Override
    public void onPeersOnlineStatusChanged(Map<String, Integer> map) {

    }

    @Override
    public void onMemberCountUpdated(int memberCount) {

    }

    @Override
    public void onAttributesUpdated(List<RtmChannelAttribute> attributeList) {
        for (RtmMessageListener listener : mMessageListeners) {
            listener.onRtmAttributesUpdated(attributeList);
        }
    }

    @Override
    public void onMessageReceived(RtmMessage rtmMessage, RtmChannelMember fromMember) {
        // Where channel messages are received
        String json = rtmMessage.getText();
        XLog.d("Channel message: " + rtmMessage.getText());

        Gson gson = new Gson();
        int cmd = -1;
        try {
            JSONObject obj = new JSONObject(json);
            cmd = obj.getInt("cmd");

            for (final RtmMessageListener listener : mMessageListeners) {
                switch (cmd) {
                    case CHANNEL_MSG_TYPE_CHAT:
                        ChatMessage chatMessage = gson.fromJson(json, ChatMessage.class);
                        handleChatMessage(listener, chatMessage);
                        break;
                    case CHANNEL_MSG_TYPE_NOTIFY:
                        NotificationMessage notification = gson.fromJson(json, NotificationMessage.class);
                        handleNotificationMessage(listener, notification);
                        break;
                    case CHANNEL_MSG_TYPE_GIFT_RANK:
                        GiftRankMessage rankMessage = gson.fromJson(json, GiftRankMessage.class);
                        handleGiftRankMessage(listener, rankMessage);
                        break;
                    case CHANNEL_MSG_CMD_OWNER_STATE:
                        OwnerStateMessage ownerMessage = gson.fromJson(json, OwnerStateMessage.class);
                        handleOwnerStateMessage(listener, ownerMessage);
                        break;
                    case CHANNEL_MSG_TYPE_SEAT:
                        SeatStateMessage seat = gson.fromJson(json, SeatStateMessage.class);
                        handleSeatStateMessage(listener, seat);
                        break;
                    case CHANNEL_MSG_TYPE_PK:
                        PKStateMessage pkStateMessage = gson.fromJson(json, PKStateMessage.class);
                        handlePKMessage(listener, pkStateMessage.data);
                        break;
                    case CHANNEL_MSG_TYPE_GIFT:
                        GiftMessage giftMessage = gson.fromJson(json, GiftMessage.class);
                        handleGiftMessage(listener, giftMessage);
                        break;
                    case CHANNEL_MSG_TYPE_LEAVE:
                        handleLeaveMessage(listener);
                        break;
                    case CHANNEL_MSG_TYPE_PRODUCT_STATE_CHANGED:
                        ProductStatedChangedMessage productStateChangedMessage =
                                gson.fromJson(json, ProductStatedChangedMessage.class);
                        handleProductStateChangedMessage(listener, productStateChangedMessage);
                        break;
                    case CHANNEL_MSG_TYPE_PRODUCT_STATE_PURCHASED:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void handleChatMessage(@NonNull RtmMessageListener listener, ChatMessage message) {
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmChannelMessageReceived(
                    message.data.fromUserId, message.data.fromUserName, message.data.message));
        } else {
            listener.onRtmChannelMessageReceived(message.data.fromUserId, message.data.fromUserName, message.data.message);
        }
    }

    private void handleNotificationMessage(@NonNull RtmMessageListener listener, NotificationMessage message) {
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmChannelNotification(message.data.total, message.data.list));
        } else {
            listener.onRtmChannelNotification(message.data.total, message.data.list);
        }
    }

    private void handleGiftRankMessage(@NonNull RtmMessageListener listener, GiftRankMessage message) {
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmRoomGiftRankChanged(message.data.total, message.data.list));
        } else {
            listener.onRtmRoomGiftRankChanged(message.data.total, message.data.list);
        }
    }

    private void handleOwnerStateMessage(@NonNull RtmMessageListener listener, OwnerStateMessage message) {
        OwnerStateMessage.OwnerState data = message.data;
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmOwnerStateChanged(data.userId, data.userName, data.uid, data.enableAudio, data.enableVideo));
        } else {
            listener.onRtmOwnerStateChanged(data.userId, data.userName, data.uid, data.enableAudio, data.enableVideo);
        }
    }

    private void handleSeatStateMessage(@NonNull RtmMessageListener listener, SeatStateMessage message) {
        List<SeatStateMessage.SeatStateMessageDataItem> data = message.data;
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmSeatStateChanged(data));
        } else {
            listener.onRtmSeatStateChanged(data);
        }
    }

    private void handlePKMessage(@NonNull RtmMessageListener listener, PKStateMessage.PKStateMessageBody message) {
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmReceivePKEvent(message));
        } else {
            listener.onRtmReceivePKEvent(message);
        }
    }

    private void handleGiftMessage(@NonNull RtmMessageListener listener, GiftMessage message) {
        GiftMessage.GiftMessageData data = message.data;
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmGiftMessage(data.fromUserId, data.fromUserName, data.toUserId, data.toUserName, data.giftId));
        } else {
            listener.onRtmGiftMessage(data.fromUserId, data.fromUserName, data.toUserId, data.toUserName, data.giftId);
        }
    }

    private void handleLeaveMessage(@NonNull RtmMessageListener listener) {
        if (mHandler != null) {
            mHandler.post(listener::onRtmLeaveMessage);
        } else {
            listener.onRtmLeaveMessage();
        }
    }

    private void handleProductStateChangedMessage(@NonNull RtmMessageListener listener,
                                                  ProductStatedChangedMessage message) {
        if (mHandler != null) {
            mHandler.post(() -> listener.onRtmProductStateChanged(message.data.productId, message.data.state));
        } else {
            listener.onRtmProductStateChanged(message.data.productId, message.data.state);
        }
    }

    @Override
    public void onMemberJoined(RtmChannelMember rtmChannelMember) {

    }

    @Override
    public void onMemberLeft(RtmChannelMember rtmChannelMember) {

    }
}
