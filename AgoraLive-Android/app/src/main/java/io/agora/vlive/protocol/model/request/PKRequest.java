package io.agora.vlive.protocol.model.request;

public class PKRequest extends Request {
    public String token;
    public String myRoomId;
    public String targetRoomId;
    public int type;

    public PKRequest(String token, String myRoomId, String targetRoomId, int type) {
        this.token = token;
        this.myRoomId = myRoomId;
        this.targetRoomId = targetRoomId;
        this.type = type;
    }
}
