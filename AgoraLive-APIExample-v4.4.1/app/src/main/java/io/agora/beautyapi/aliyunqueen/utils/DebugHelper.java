package io.agora.beautyapi.aliyunqueen.utils;

import android.graphics.Bitmap;
import android.opengl.EGL14;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.os.Environment;

import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.Texture2D;

import java.io.File;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DebugHelper {
    private static final String TAG = "DEBUG_Queen";
    private static long sCount = 0;
    private static final int PERIOD = 50;

    private static long mInitRunningThreadId = 0;

    public static void afterInitEngine(QueenEngine queenEngine) {
        // 检测当前是否在gl线程
        if (!isEGLEnvOK()) {
            android.util.Log.e(TAG, "######ERROR: Current thread does NOT have a valid GL context######");
        }
        // 首先，开启日志
        queenEngine.enableDebugLog();

        mInitRunningThreadId = Thread.currentThread().getId();
    }

    public static void afterProcessEngine(QueenEngine queenEngine, int orgTextureId, boolean isOesTexture, int w, int h) {
        // 检查是否在同一线程
        long curThreadId = Thread.currentThread().getId();
        if (curThreadId != mInitRunningThreadId) {
//            android.util.Log.e(TAG, "###WARNING: Current thread does NOT EQUAL to init thread, Maybe GL Context is invalid######");
        }

        // TODO：检测当前证书是否有效

        Texture2D outTexture2D = (Texture2D) invokeFiled(queenEngine, "mOutTexture");
        if (outTexture2D == null) return;

        if (++sCount % PERIOD == 0) {
            saveTexture2File(queenEngine.getEngineHandler(), orgTextureId, w, h, isOesTexture, "_before");
            int out_w = outTexture2D.getSize().x;
            int out_h = outTexture2D.getSize().y;
            saveTexture2File(queenEngine.getEngineHandler(), outTexture2D.getTextureId(), out_w, out_h, false, "_after");
        }
    }

    private static boolean isEGLEnvOK() {
        EGLDisplay eglDisplay = EGL14.eglGetCurrentDisplay();
        EGLContext eglContext = EGL14.eglGetCurrentContext();
        EGLSurface eglSurface = EGL14.eglGetCurrentSurface(EGL14.EGL_DRAW);

        if (eglDisplay != EGL14.EGL_NO_DISPLAY &&
                eglContext != EGL14.EGL_NO_CONTEXT &&
                eglSurface != EGL14.EGL_NO_SURFACE) {
            return true;
        }
        return false;
    }

    private static Object invokeFiled(Object object, String filedName) {
        try {
            Class<?> clazz = object.getClass();
            Field field = clazz.getDeclaredField(filedName);
            field.setAccessible(true);
            return field.get(object);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    private static void saveTexture2File(long engineHandler, int texId, int w, int h, boolean isOes, String suffixName) {
        Texture2D texture2D = new Texture2D(engineHandler);
        texture2D.init(texId, w, h, isOes);
        String outputPath = getDabugPngPath(suffixName);
        texture2D.saveToFile(outputPath, Bitmap.CompressFormat.PNG, 100);
        texture2D.release();
        android.util.Log.d(TAG, "View textureId=" + texId + ", w=" + w + ", h=" + h
                + ", for save path=" + outputPath + ", isOes=" + isOes);
    }

    private static String getDabugPngPath(String suffixName) {
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
        SimpleDateFormat timeSdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
//        SimpleDateFormat timeSdf = new SimpleDateFormat("yyyyMMdd_HHmmss_SSS");
        String fileName = "Queen_" + timeSdf.format(new Date()) + suffixName + ".png";
        return path + File.separator + fileName;
    }
}
