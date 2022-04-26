package io.agora.vlive.protocol.model.request;

public class SeatInteractionRequest extends Request {
    public String token;
    public String roomId;
    public String userId;
    public int no;
    public int type;

    public SeatInteractionRequest(String token, String roomId,
                                  String userId, int no, int type) {
        this.token = token;
        this.roomId = roomId;
        this.userId = userId;
        this.no = no;
        this.type = type;
    }
}
