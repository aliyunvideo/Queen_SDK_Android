package com.aliyun.maliang.android.simpleapp.copyparams;

import com.aliyun.android.libqueen.QueenEngine;

import java.lang.ref.WeakReference;

public class QueenRuntime {

    public static boolean sFaceDetectDebug = false; // 是否显示人脸关键点

    public static boolean sPowerSaving = false;     // 是否高性能模式
    public static WeakReference<QueenEngine> queenEngienRef;

}
