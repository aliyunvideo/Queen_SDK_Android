package io.agora.vlive.protocol.model.request;

public class ModifyUserStateRequest extends RoomRequest {
    public String userId;
    public int enableAudio;
    public int enableVideo;
    public int enableChat;

    public ModifyUserStateRequest(String token, String roomId, String userId,
                                  int enableAudio, int enableVideo, int enableChat) {
        this.token = token;
        this.roomId = roomId;
        this.userId = userId;
        this.enableAudio = enableAudio;
        this.enableVideo = enableVideo;
        this.enableChat = enableChat;
    }
}
