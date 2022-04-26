package io.agora.vlive.agora.rtm;

import java.util.List;
import java.util.Map;

import io.agora.rtm.RtmChannelAttribute;
import io.agora.rtm.RtmChannelMember;
import io.agora.vlive.agora.rtm.model.GiftRankMessage;
import io.agora.vlive.agora.rtm.model.NotificationMessage;
import io.agora.vlive.agora.rtm.model.PKInvitationMessage;
import io.agora.vlive.agora.rtm.model.PKStateMessage;
import io.agora.vlive.agora.rtm.model.SeatStateMessage;

public interface RtmMessageListener {
    void onRtmConnectionStateChanged(int state, int reason);

    void onRtmTokenExpired();

    void onRtmPeersOnlineStatusChanged(Map<String, Integer> map);

    void onRtmMemberCountUpdated(int memberCount);

    void onRtmAttributesUpdated(List<RtmChannelAttribute> attributeList);

    void onRtmMemberJoined(RtmChannelMember rtmChannelMember);

    void onRtmMemberLeft(RtmChannelMember rtmChannelMember);

    void onRtmSeatInvited(String userId, String userName, int index);

    void onRtmSeatApplied(String userId, String userName, int index);

    void onRtmInvitationAccepted(long processId, String userId, String userName, int index);

    void onRtmApplicationAccepted(long processId, String userId, String userName, int index);

    void onRtmInvitationRejected(long processId, String userId, String userName, int index);

    void onRtmApplicationRejected(long processId, String userId, String userName, int index);

    void onRtmOwnerForceLeaveSeat(String userId, String userName, int index);

    void onRtmHostLeaveSeat(String userId, String userName, int index);

    void onRtmPkReceivedFromAnotherHost(String userId, String userName, String roomId);

    void onRtmPkAcceptedByTargetHost(String userId, String userName, String roomId);

    void onRtmPkRejectedByTargetHost(String userId, String userName, String roomId);

    void onRtmChannelMessageReceived(String peerId, String nickname, String content);

    void onRtmChannelNotification(int total, List<NotificationMessage.NotificationItem> list);

    void onRtmRoomGiftRankChanged(int total, List<GiftRankMessage.GiftRankItem> list);

    void onRtmOwnerStateChanged(String userId, String userName, int uid, int enableAudio, int enableVideo);

    void onRtmSeatStateChanged(List<SeatStateMessage.SeatStateMessageDataItem> data);

    void onRtmReceivePKEvent(PKStateMessage.PKStateMessageBody messageData);

    void onRtmGiftMessage(String fromUserId, String fromUserName, String toUserId, String toUserName, int giftId);

    void onRtmProductPurchased(String productId, int count);

    void onRtmProductStateChanged(String productId, int state);

    void onRtmLeaveMessage();
}
