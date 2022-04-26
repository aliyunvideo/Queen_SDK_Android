package io.agora.vlive.agora.rtm.model;

import java.util.List;

public class GiftRankMessage extends AbsRtmMessage {
    public GiftRank data;

    public static class GiftRank {
        public int total;
        public List<GiftRankItem> list;
    }

    public static class GiftRankItem {
        public String userId;
        public String userName;
        public String avatar;
    }
}
