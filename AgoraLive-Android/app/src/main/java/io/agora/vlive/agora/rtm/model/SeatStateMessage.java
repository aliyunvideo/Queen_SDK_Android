package io.agora.vlive.agora.rtm.model;

import java.util.List;

public class SeatStateMessage extends AbsRtmMessage {
    public List<SeatStateMessageDataItem>  data;

    public static class SeatStateMessageDataItem {
        public SeatState seat;
        public UserState user;
    }

    public static class SeatState {
        public int no;
        public int state;
    }

    public static class UserState {
        public String userId;
        public String userName;
        public int uid;
        public int enableAudio;
        public int enableVideo;
    }
}
