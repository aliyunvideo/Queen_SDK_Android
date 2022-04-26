package io.agora.vlive.agora.rtm.model;

public class ProductStatedChangedMessage extends AbsRtmMessage {
    public ProductState data;

    public static class ProductState {
        public String productId;
        public int state;
    }
}
