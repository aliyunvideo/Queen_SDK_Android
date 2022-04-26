package io.agora.vlive.protocol.model.body;

public class PurchaseProductBody {
    public String productId;
    public int count;

    public PurchaseProductBody(String productId, int count) {
        this.productId = productId;
        this.count = count;
    }
}
