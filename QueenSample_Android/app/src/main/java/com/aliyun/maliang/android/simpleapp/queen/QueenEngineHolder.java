package com.aliyun.maliang.android.simpleapp.queen;

import android.content.Context;

import com.taobao.android.libqueen.QueenEngine;

public class QueenEngineHolder {

    private static boolean sIsExistInstance = false;
    private static Object sLockObj = new Object();

    private static void sCheckThreadSafely() {
        synchronized (sLockObj) {
            if (sIsExistInstance) {
                try {
                    sLockObj.wait();
                } catch (Exception e) {}
            }
        }
    }

    private static void sReleaseThreadSafely() {
        synchronized (sLockObj) {
            sIsExistInstance = false;
            sLockObj.notify();
        }
    }

    public static QueenEngine createQueenEngine(Context context, boolean toScreen) {
        return createQueenEngine(context, false, toScreen);
    }

    public static QueenEngine createQueenEngine(Context context, boolean withContext, boolean toScreen) {
        sCheckThreadSafely();
        sIsExistInstance = true;
        QueenEngine engine = null;
        try {
            engine = new QueenEngine(context, withContext, toScreen);
        } catch (Exception e) { e.printStackTrace(); }
        return engine;
    }

    public static void releaseEngine(QueenEngine engine) {
        engine.release();
        sReleaseThreadSafely();
    }

}
