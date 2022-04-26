package io.agora.vlive.agora.rtm.model;

public class SeatInteractionMessage extends AbsRtmMessage {
    public int version;
    public SeatInteractionInfo data;

    public class SeatInteractionInfo {
        public int no;
        public int type;
        public long processId;
        public SeatInteractionFromUser fromUser;
    }

    public class SeatInteractionFromUser {
        public String userId;
        public String userName;
        public int uid;
        public int role;
    }
}