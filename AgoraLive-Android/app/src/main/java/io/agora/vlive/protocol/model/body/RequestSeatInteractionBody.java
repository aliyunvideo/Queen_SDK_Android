package io.agora.vlive.protocol.model.body;

public class RequestSeatInteractionBody {
    public int no;
    public int type;

    public RequestSeatInteractionBody(int no, int type) {
        this.no = no;
        this.type = type;
    }
}
