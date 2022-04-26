package io.agora.vlive.protocol.model.response;

public class RefreshTokenResponse extends Response {
    public TokenData data;

    public class TokenData {
        public String rtcToken;
        public String rtmToken;
    }
}
