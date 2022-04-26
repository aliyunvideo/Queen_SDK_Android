package io.agora.vlive.agora.rtm.model;

import java.util.List;

public class NotificationMessage extends AbsRtmMessage {
    public Notification data;

    public static class Notification {
        public int total;
        public List<NotificationItem> list;
    }

    public static class NotificationItem {
        public static final int NOTIFICATION_LEAVE_ROOM = 0;
        public static final int NOTIFICATION_ENTER_ROOM = 1;

        public String userId;
        public String userName;
        public int role;
        public int state;
    }
}
