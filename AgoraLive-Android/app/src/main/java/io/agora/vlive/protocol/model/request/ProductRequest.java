package io.agora.vlive.protocol.model.request;

public class ProductRequest {
    public String token;
    public String roomId;
    public String productId;
    public int count;
    public int state;

    public ProductRequest(String token, String roomId,
                          String productId, int count, int state) {
        this.token = token;
        this.roomId = roomId;
        this.productId = productId;
        this.count = count;
        this.state = state;
    }
}
