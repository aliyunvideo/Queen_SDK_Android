package io.agora.vlive.protocol.model.body;

public class UserRequestBody {
    String userName;
    String avatar;

    public UserRequestBody(String name, String avatar) {
        this.userName = name;
        this.avatar = avatar;
    }
}
