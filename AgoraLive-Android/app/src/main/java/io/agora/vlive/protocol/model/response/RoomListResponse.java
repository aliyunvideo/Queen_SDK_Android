package io.agora.vlive.protocol.model.response;

import java.util.List;

import io.agora.vlive.protocol.model.model.RoomInfo;

public class RoomListResponse extends Response {
    public RoomList data;

    public static class RoomList {
        public int count;
        public int total;
        public String nextId;
        public List<RoomInfo> list;
    }
}
