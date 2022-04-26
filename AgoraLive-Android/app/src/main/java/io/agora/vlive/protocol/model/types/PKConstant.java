package io.agora.vlive.protocol.model.types;

public class PKConstant {
    public static final int PK_BEHAVIOR_INVITE = 1;
    public static final int PK_BEHAVIOR_ACCEPT = 2;
    public static final int PK_BEHAVIOR_REJECT = 3;
    public static final int PK_BEHAVIOR_TIMEOUT = 4;

    public static final int PK_EVENT_END = 0;
    public static final int PK_EVENT_START = 1;
    public static final int PK_EVENT_RANK_CHANGED = 2;

    public static final int PK_STATE_IDLE = 0;
    public static final int PK_STATE_PK = 1;

    public static final int PK_RESULT_LOSE = 0;
    public static final int PK_RESULT_WIN = 1;
    public static final int PK_RESULT_DRAW = 2;
}
