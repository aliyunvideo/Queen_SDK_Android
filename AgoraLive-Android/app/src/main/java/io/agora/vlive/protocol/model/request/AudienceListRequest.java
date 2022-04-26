package io.agora.vlive.protocol.model.request;

public class AudienceListRequest extends Request {
    public static final int TYPE_ALL = 1;
    public static final int TYPE_AUDIENCE = 2;

    public static final int DEFAULT_COUNT = 10;

    public String token;
    public String roomId;
    public String nextId;
    public int count;
    public int type;

    public AudienceListRequest(String token, String roomId, String nextId) {
        this(token, roomId, nextId, DEFAULT_COUNT, TYPE_ALL);
    }

    public AudienceListRequest(String token, String roomId, String nextId, int type) {
        this(token, roomId, nextId, DEFAULT_COUNT, type);
    }

    public AudienceListRequest(String token, String roomId,
                               String nextId, int count, int type) {
        this.token = token;
        this.roomId = roomId;
        this.nextId = nextId;
        this.count = count;
        this.type = type;
    }
}
