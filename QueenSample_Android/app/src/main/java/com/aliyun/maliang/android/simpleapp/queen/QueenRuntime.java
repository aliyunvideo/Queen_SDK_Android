package com.aliyun.maliang.android.simpleapp.queen;

import com.taobao.android.libqueen.QueenEngine;

import java.lang.ref.WeakReference;

public class QueenRuntime {

    public static boolean isEnableQueen = true;

    public static boolean sPowerSaving = false;

    public static boolean sFaceDetectDebug = false;
    public static boolean sFaceShapeDebug = false;
    public static boolean sFaceMakeupDebug = false;
    public static boolean sKeepInputTextureDirection = true;
    public static boolean sFrameSynchronized = false;

    public static int sDebugImageLayerIndex = 0;
    public static boolean sImageLayerDebug = false;
    public static boolean sSegmentMaskDebug = false;

    public static int sCurTextureId = -1;
    public static WeakReference<QueenEngine> queenEngienRef;

    public static final int MODE_VIDEO = 1;
    public static final int MODE_IMAGE = 2;
    public static int sCur_MODE = MODE_VIDEO;


}
