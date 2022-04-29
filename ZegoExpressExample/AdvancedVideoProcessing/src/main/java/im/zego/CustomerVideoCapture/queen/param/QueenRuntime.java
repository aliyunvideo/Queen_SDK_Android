package im.zego.CustomerVideoCapture.queen.param;

import com.aliyun.android.libqueen.QueenEngine;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class QueenRuntime {

    public static boolean isEnableQueen = true;

    public static boolean isRenderSplit = false;

    public static boolean sPowerSaving = false;

    public static boolean sEnableDebugLog = false;
    public static boolean sFaceDetectDebug = false;
    public static boolean sFaceAttribDetectDebug = false;
    public static boolean sHandDetectDebug = false;
    public static boolean sArWritingDetectDebug = false;
    public static boolean sBodyDetectDebug = false;
    public static boolean sFaceShapeDebug = false;
    public static boolean sBodyShapeDebug = false;
    public static boolean sFaceMakeupDebug = false;
    public static boolean sKeepInputTextureDirection = true;
    public static boolean sFrameSynchronized = false;

    public static int sDebugImageLayerIndex = 0;
    public static boolean sImageLayerDebug = false;
    public static boolean sSegmentMaskDebug = false;
    public static boolean sUseAsyncRender = false;

    public static boolean sRecordOriginVideoFrame = false;
    public static AtomicBoolean sIsRecording = new AtomicBoolean(false);

    public static boolean sDisplayUnsupportMenuTab = false;

    public static int sPreviewWidth = 1280;
    public static int sPreviewHeight = 720;

    public static int sCurTextureId = -1;
    public static boolean sIsOesTexture = false;
    public static WeakReference<QueenEngine> queenEngienRef;

    public static final int MODE_VIDEO = 1;
    public static final int MODE_IMAGE = 2;
    public static int sCur_MODE = MODE_VIDEO;

    private static List<IRuntimeStatustListener> sRuntimeListeners;

    public static void registListener(IRuntimeStatustListener listener) {
        if (sRuntimeListeners == null) {
            sRuntimeListeners = new ArrayList<>(5);
        }
        sRuntimeListeners.add(listener);
    }

    public static void unregistListener(IRuntimeStatustListener listener) {
        if (sRuntimeListeners != null && sRuntimeListeners.contains(listener)) {
            sRuntimeListeners.remove(listener);
        }
    }

    public static void notifyStatusUpdated() {
        if (sRuntimeListeners != null) {
            for (IRuntimeStatustListener listener : sRuntimeListeners) {
                listener.onUpdateStatus();
            }
        }
    }

    public interface IRuntimeStatustListener {
        void onUpdateStatus();
    }
}
