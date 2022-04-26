package io.agora.vlive.protocol.model.body;

public class CreateRoomRequestBody {
    String roomName;
    int type;
    String virtualAvatar;

    public CreateRoomRequestBody(String roomName, int type, String avatar) {
        this.roomName = roomName;
        this.type = type;
        this.virtualAvatar = avatar;
    }
}
