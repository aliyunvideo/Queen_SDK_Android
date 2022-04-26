package io.agora.vlive.protocol.model.response;

import java.util.List;

public class GiftRankResponse extends Response {
    public int total;
    public List<GiftInfo> data;

    public static class GiftInfo {
        public String userId;
        public String userName;
        public String avatar;
    }
}
