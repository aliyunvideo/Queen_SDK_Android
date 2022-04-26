package io.agora.vlive.protocol.model.request;

public class SendGiftRequest extends Request {
    public String token;
    public String roomId;
    public int giftId;
    public int count;

    public SendGiftRequest(String token, String roomId, int giftId) {
        this.token = token;
        this.roomId = roomId;
        this.giftId = giftId;
        this.count = 1;
    }
}
