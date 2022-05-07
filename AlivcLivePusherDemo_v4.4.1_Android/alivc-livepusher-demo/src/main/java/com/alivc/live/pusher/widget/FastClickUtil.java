package com.alivc.live.pusher.widget;

public class FastClickUtil {
    private static long lastClickTime;
    //两次点击按钮之间的点击间隔不能少于1000毫秒
    public synchronized static boolean isFastClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < 1000) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
}
