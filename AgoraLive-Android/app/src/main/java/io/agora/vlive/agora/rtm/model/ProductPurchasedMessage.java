package io.agora.vlive.agora.rtm.model;

public class ProductPurchasedMessage extends AbsRtmMessage {
    public int productId;
    public int count;
    public FromUser fromUser;

    public static class FromUser {
        public String userId;
        public String userName;
        public int uid;
    }
}
