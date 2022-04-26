package io.agora.vlive.protocol.model.request;

public class RoomListRequest extends Request {
    public String token;
    public String nextId;
    public int count;
    public int type;
    public Integer pkState;

    public RoomListRequest(String token, String nextId, int count,
                           int type, Integer pkState) {
        this.token = token;
        this.nextId = nextId;
        this.count = count;
        this.type = type;
        this.pkState = pkState;
    }
}
