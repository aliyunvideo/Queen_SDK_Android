package im.zego.CustomerVideoCapture;

import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;
import android.view.View;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import im.zego.CustomerVideoCapture.ve_gl.EglBase;
import im.zego.CustomerVideoCapture.ve_gl.EglBase14;
import im.zego.CustomerVideoCapture.ve_gl.GlRectDrawer;
import im.zego.CustomerVideoCapture.ve_gl.GlUtil;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoPublishChannel;

/**
 * VideoCaptureFromCamera2
 * Collecting data from camera. Need to inherit from ZegoVideoCaptureCallback
 */

public class VideoCaptureFromCamera2 extends ZegoVideoCaptureCallback implements
        SurfaceTexture.OnFrameAvailableListener,
        TextureView.SurfaceTextureListener {
    private static final String TAG = "VideoCaptureFromCamera2";
    private static final int CAMERA_STOP_TIMEOUT_MS = 7000;

    private Camera mCam = null;
    private Camera.CameraInfo mCamInfo = null;
    // Initial values of camera related parameters
    // 相关参数的初始值
    private int mFront = 0;
    private int mCameraWidth = 640;
    private int mCameraHeight = 480;
    private int mCaptureWidth = 0;
    private int mCaptureHeight = 0;
    private int mViewWidth = 0;
    private int mViewHeight = 0;
    private int mViewMode = 0;
    private int mFrameRate = 15;
    private int mDisplayRotation = 0;
    private int mImageRotation = 0;

    // Related variables for frame buffer objects (FBO)
    // 用于帧缓冲区对象（FBO）的相关变量
    private EglBase mDummyContext = null;
    private GlRectDrawer mDummyDrawer = null;
    private boolean mIsEgl14 = false;
    private int mInputTextureId = 0;
    private SurfaceTexture mInputSurfaceTexture = null;
    private float[] mInputMatrix = new float[16];
    private int mTextureId = 0;
    private int mFrameBufferId = 0;
    private int mCaptureID = 0;

    //  texture transformation matrix
    // 纹理变换矩阵
    private float[] mIdentityMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f};

    private HandlerThread mThread = null;
    private volatile Handler cameraThreadHandler = null;
    private final AtomicBoolean isCameraRunning = new AtomicBoolean();
    private final Object pendingCameraRestartLock = new Object();
    private volatile boolean pendingCameraRestart = false;

    // Variables for preview
    // 用于展示预览图的相关变量
    private boolean mIsPreview = true;
    private EglBase previewEglBase = null;
    private GlRectDrawer previewDrawer = null;
    private float[] mPreviewMatrix = new float[16];

    private boolean mIsCapture = true;
    private TextureView mTextureView = null;

    public VideoCaptureFromCamera2(){

        mThread = new HandlerThread("camera-cap");
        mThread.start();
        // create asynchronous message processing handler for camera
        cameraThreadHandler = new Handler(mThread.getLooper());
    }

    protected void finalize() {
        mThread.quit();
        mThread = null;
    }

    @Override
    public void onStart(ZegoPublishChannel channel) {
        Log.d(TAG, "allocateAndStart");

        final CountDownLatch barrier = new CountDownLatch(1);
        cameraThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                mDummyContext = EglBase.create(null, EglBase.CONFIG_PIXEL_BUFFER);

                try {
                    // Create Surface
                    mDummyContext.createDummyPbufferSurface();
                    // 绑定 eglContext、eglDisplay、eglSurface
                    // Bind eglContext、eglDisplay、eglSurface
                    mDummyContext.makeCurrent();
                    //创建绘制类，用于FBO
                    // Create Drawer for frame buffer objects
                    mDummyDrawer = new GlRectDrawer();
                    mDummyContext.detachCurrent();
                } catch (RuntimeException e) {
                    // Clean up before rethrowing the exception.
                    mDummyContext.releaseSurface();
                    e.printStackTrace();
                    throw e;
                }

                mIsEgl14 = EglBase14.isEGL14Supported();
                mInputTextureId = GlUtil.generateTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES);
                mInputSurfaceTexture = new SurfaceTexture(mInputTextureId);
                // Set video frame callback
                // 设置视频帧回调监听
                mInputSurfaceTexture.setOnFrameAvailableListener(VideoCaptureFromCamera2.this);

                barrier.countDown();
            }
        });
        try {
            barrier.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startCapture();
    }

    @Override
    public void onStop(ZegoPublishChannel channel){
        stopCapture();

        if (cameraThreadHandler != null) {
            final CountDownLatch barrier = new CountDownLatch(1);
            cameraThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Destroy surface for preview
                    // 销毁用于屏幕显示的surface（预览）
                    releasePreviewSurface();

                    if (previewDrawer != null) {
                        previewDrawer.release();
                        previewDrawer = null;
                    }
                    if (mTextureView != null) {
                        if (mTextureView.getSurfaceTextureListener().equals(VideoCaptureFromCamera2.this)) {
                            mTextureView.setSurfaceTextureListener(null);
                        }
                        mTextureView = null;
                    }

                    mInputSurfaceTexture.release();
                    mInputSurfaceTexture = null;

                    // Bind eglContext、eglDisplay、eglSurface
                    // 绑定eglContext、eglDisplay、eglSurface
                    mDummyContext.makeCurrent();
                    if (mInputTextureId != 0) {
                        int[] textures = new int[] {mInputTextureId};
                        GLES20.glDeleteTextures(1, textures, 0);
                        mInputTextureId = 0;
                    }

                    if (mTextureId != 0) {
                        int[] textures = new int[]{mTextureId};
                        GLES20.glDeleteTextures(1, textures, 0);
                        mTextureId = 0;
                    }

                    if (mFrameBufferId != 0) {
                        int[] frameBuffers = new int[]{mFrameBufferId};
                        GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
                        mFrameBufferId = 0;
                    }

                    mDummyDrawer = null;
                    mDummyContext.detachCurrent();
                    mDummyContext.release();
                    mDummyContext = null;

                    barrier.countDown();
                }
            });
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * Release resources. Need to be implemented
     * 释放资源，必须实现
     */

    // When the user starts publishing, Zego SDK will call startCapture() to notify external equipments.
    // This method needs to be implemented
    // 开始推流时，ZEGO SDK 调用 startCapture 通知外部采集设备开始工作，必须实现
    protected void startCapture() {
        Log.d(TAG, "startCapture");
        mIsCapture = true;
        startCamera();
    }

    // When the user stop publishing, Zego SDK wiil call stopCapture() to notify extrnal equipments.
    // This method needs to be implemented
    // 停止推流时，ZEGO SDK 调用 stopCapture 通知外部采集设备停止采集，必须实现
    protected void stopCapture() {
        Log.d(TAG, "stopCapture");
        mIsCapture = false;
        stopCamera();
    }

    protected int startCamera() {
        if (isCameraRunning.getAndSet(true)) {
            Log.e(TAG, "Camera has already been started.");
            return 0;
        }

        maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                // * Create and Start Cam
                createCamOnCameraThread();
                startCamOnCameraThread();
            }
        });

        return 0;
    }

    protected int stopCamera() {
//        if (mIsPreview || mIsCapture) {
//            return 0;
//        }

        final CountDownLatch barrier = new CountDownLatch(1);
        final boolean didPost = maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                stopCaptureOnCameraThread(true /* stopHandler */);
                releaseCam();
                barrier.countDown();
            }
        });
        if (!didPost) {
            Log.e(TAG, "Calling stopCapture() for already stopped camera.");
            return 0;
        }
        try {
            if (!barrier.await(CAMERA_STOP_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                Log.e(TAG, "Camera stop timeout");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "stopCapture done");

        return 0;
    }

    @Override
    // Set view for preview
     public void setView( View view) {
        if (view instanceof TextureView) {
            setRendererView((TextureView) view);
        }

    }

    private void checkIsOnCameraThread() {
        if (cameraThreadHandler == null) {
            Log.e(TAG, "Camera is not initialized - can't check thread.");
        } else if (Thread.currentThread() != cameraThreadHandler.getLooper().getThread()) {
            throw new IllegalStateException("Wrong thread");
        }
    }

    private boolean maybePostOnCameraThread(Runnable runnable) {
        return cameraThreadHandler != null && isCameraRunning.get()
                && cameraThreadHandler.postAtTime(runnable, this, SystemClock.uptimeMillis());
    }

    // Create Camera
    private int createCamOnCameraThread() {
        checkIsOnCameraThread();
        if (!isCameraRunning.get()) {
            Log.e(TAG, "startCaptureOnCameraThread: Camera is stopped");
            return 0;
        }

        Log.i(TAG, "board: " + Build.BOARD);
        Log.i(TAG, "device: " + Build.DEVICE);
        Log.i(TAG, "manufacturer: " + Build.MANUFACTURER);
        Log.i(TAG, "brand: " + Build.BRAND);
        Log.i(TAG, "model: " + Build.MODEL);
        Log.i(TAG, "product: " + Build.PRODUCT);
        Log.i(TAG, "sdk: " + Build.VERSION.SDK_INT);

        // get the camera index for setting
        // 获取欲设置camera的索引号
        int nFacing = (mFront != 0) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;

        if (mCam != null) {
            // It means the camera has been started
            return 0;
        }

        mCamInfo = new Camera.CameraInfo();
        // Get the number of camera
        // 获取camera的数目
        int nCnt = Camera.getNumberOfCameras();

        // Get the chosen camera index and start that camera
        // 得到欲设置camera的索引号并打开camera
        for (int i = 0; i < nCnt; i++) {
            Camera.getCameraInfo(i, mCamInfo);
            if (mCamInfo.facing == nFacing) {
                try {
                    mCam = Camera.open(i);
                } catch (RuntimeException e) {
                    mCam = null;
                }
                break;
            }
        }

        // Can not find setting camera
        // 没找到欲设置的camera
        if (mCam == null) {
            Log.i(TAG, "[WARNING] no camera found, try default\n");
            // Try to start default camera
            // 先试图打开默认camera
            try {
                mCam = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "[ERROR] open camera failed\n");
                return -1;
            }

            if (mCam == null) {
                //AppLogger.getInstance().e(com.zego.videocapture.videocapture.VideoCaptureFromCamera2.class, "open camera failed, please check system camera status!");
                Log.i(TAG, "[ERROR] no camera found\n");
                return -1;
            }
        }

        Camera.Parameters parms = mCam.getParameters();
        // 获取camera首选的size
        Camera.Size psz = parms.getPreferredPreviewSizeForVideo();

        if (psz == null) {
            psz = mCam.new Size(640, 480);
        }

        // Set preview view size for camera
        // 设置camera的采集视图size
        parms.setPreviewSize(psz.width, psz.height);
        mCameraWidth = psz.width;
        mCameraHeight = psz.height;

        // set preview FPS range
        // 获取camera支持的帧率范围，并设置预览帧率范围
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            if ((entry[0] == entry[1]) && entry[0] == mFrameRate * 1000) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                break;
            }
        }

        // Get real fps of camera
        // 获取camera的实际帧率
        int[] realRate = new int[2];
        parms.getPreviewFpsRange(realRate);
        if (realRate[0] == realRate[1]) {
            mFrameRate = realRate[0] / 1000;
        } else {
            mFrameRate = realRate[1] / 2 / 1000;
        }

        // Set focus mode of camera
        // 设置camera的对焦模式
        boolean bFocusModeSet = false;
        for (String mode : parms.getSupportedFocusModes()) {
            if (mode.compareTo(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO) == 0) {
                try {
                    parms.setFocusMode(mode);
                    bFocusModeSet = true;
                    break;
                } catch (Exception ex) {
                    Log.i(TAG, "[WARNING] vcap: set focus mode error (stack trace followed)!!!\n");
                    ex.printStackTrace();
                }
            }
        }
        if (!bFocusModeSet) {
            Log.i(TAG, "[WARNING] vcap: focus mode left unset !!\n");
        }

        try {
            // 设置camera的参数
            mCam.setParameters(parms);
        } catch (Exception ex) {
            Log.i(TAG, "vcap: set camera parameters error with exception\n");
            ex.printStackTrace();
        }

        Camera.Parameters actualParm = mCam.getParameters();
        mCameraWidth = actualParm.getPreviewSize().width;
        mCameraHeight = actualParm.getPreviewSize().height;
        Log.i(TAG, "[WARNING] vcap: focus mode " + actualParm.getFocusMode());

        int result;
        if (mCamInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCamInfo.orientation + mDisplayRotation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (mCamInfo.orientation - mDisplayRotation + 360) % 360;
        }
        // set Orientation
        // 设置预览图像的转方向
        mCam.setDisplayOrientation(result);
        mImageRotation = result;

        // Bind eglContext、eglDisplay、eglSurface
        // 绑定eglContext、eglDisplay、eglSurface
        mDummyContext.makeCurrent();;
        Log.d(TAG, "ImageRotation:" + mImageRotation);
        if (mTextureId != 0) {
            int[] textures = new int[]{mTextureId};
            GLES20.glDeleteTextures(1, textures, 0);
            mTextureId = 0;
        }

        if (mFrameBufferId != 0) {
            int[] frameBuffers = new int[]{mFrameBufferId};
            GLES20.glDeleteFramebuffers(1, frameBuffers, 0);
            mFrameBufferId = 0;
        }
        mDummyContext.detachCurrent();
        return 0;
    }

    // 启动camera
    private int startCamOnCameraThread() {
        checkIsOnCameraThread();
        if (!isCameraRunning.get() || mCam == null) {
            Log.e(TAG, "startPreviewOnCameraThread: Camera is stopped");
            return 0;
        }

        if (mInputSurfaceTexture == null) {
            Log.e(TAG, "mInputSurfaceTexture == null");
            return -1;
        }

        try {
            // 设置预览SurfaceTexture
            mCam.setPreviewTexture(mInputSurfaceTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Start camera preview
        // 启动camera预览
        mCam.startPreview();
        Log.e(TAG, "startPreview success");

        return 0;
    }

    // Stop camera capture
    // camera停止采集
    private int stopCaptureOnCameraThread(boolean stopHandler) {
        checkIsOnCameraThread();
        Log.d(TAG, "stopCaptureOnCameraThread");

        if (stopHandler) {
            // Clear the cameraThreadHandler first, in case stopPreview or
            // other driver code deadlocks. Deadlock in
            // android.hardware.Camera._stopPreview(Native Method) has
            // been observed on Nexus 5 (hammerhead), OS version LMY48I.
            // The camera might post another one or two preview frames
            // before stopped, so we have to check |isCameraRunning|.
            // Remove all pending Runnables posted from |this|.
            isCameraRunning.set(false);
            cameraThreadHandler.removeCallbacksAndMessages(this /* token */);
        }

        if (mCam != null) {
            // 停止camera预览
            mCam.stopPreview();
        }
        return 0;
    }

    // Restart Camera
    // 重启camera
    private int restartCam() {
        synchronized (pendingCameraRestartLock) {
            if (pendingCameraRestart) {
                // Do not handle multiple camera switch request to avoid blocking
                // camera thread by handling too many switch request from a queue.
                Log.w(TAG, "Ignoring camera switch request.");
                return 0;
            }
            pendingCameraRestart = true;
        }

        final boolean didPost = maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                stopCaptureOnCameraThread(false);
                releaseCam();
                createCamOnCameraThread();
                startCamOnCameraThread();
                synchronized (pendingCameraRestartLock) {
                    pendingCameraRestart = false;
                }
            }
        });

        if (!didPost) {
            synchronized (pendingCameraRestartLock) {
                pendingCameraRestart = false;
            }
        }

        return 0;
    }

    // 释放camera
    private int releaseCam() {
        // * release cam
        if (mCam != null) {
            mCam.release();
            mCam = null;
        }

        // * release cam info
        mCamInfo = null;
        return 0;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        if (cameraThreadHandler != null) {
            cameraThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    releasePreviewSurface();
                }
            });
        }
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        if (cameraThreadHandler != null) {
            final CountDownLatch barrier = new CountDownLatch(1);
            cameraThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Destroy preview surface
                    // 销毁用于屏幕显示的surface（预览）
                    releasePreviewSurface();
                    barrier.countDown();
                }
            });
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    // Set rendering view
    // 设置渲染视图
    public int setRendererView(TextureView view) {
        if (cameraThreadHandler == null) {
            // 设置Texture.SurfaceTextureListener回调监听
            doSetRendererView(view);
        } else {
            final CountDownLatch barrier = new CountDownLatch(1);
            final TextureView temp = view;
            cameraThreadHandler.post(new Runnable() {
                @Override
                public void run() {
                    doSetRendererView(temp);
                    barrier.countDown();
                }
            });
            try {
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return 0;
    }

    // Set Texture.SurfaceTextureListener Callback
    // 设置Texture.SurfaceTextureListener回调监听
    private void doSetRendererView(TextureView temp) {
        if (mTextureView != null) {
            if (mTextureView.getSurfaceTextureListener().equals(VideoCaptureFromCamera2.this)) {
                mTextureView.setSurfaceTextureListener(null);
            }
            releasePreviewSurface();
        }

        mTextureView = temp;
        if (mTextureView != null) {
            mTextureView.setSurfaceTextureListener(VideoCaptureFromCamera2.this);
        }
    }

    // set Preview
    // 设置预览视图
    private void attachTextureView() {
        if (previewEglBase.hasSurface()) {
            return;
        }

        if (!mTextureView.isAvailable()) {
            return;
        }

        mViewWidth = mTextureView.getWidth();
        mViewHeight = mTextureView.getHeight();
        try {
            // Create EGLSurface
            // 创建EGLSurface
            previewEglBase.createSurface(mTextureView.getSurfaceTexture());
        } catch (RuntimeException e) {
            e.printStackTrace();
            releasePreviewSurface();
            mViewWidth = 0;
            mViewHeight = 0;
        }
    }

    // Draw picture data
    private void draw(int textureId, int width, int height, float[] texMatrix) {
        if (previewEglBase == null) {
            previewEglBase = EglBase.create(mDummyContext.getEglBaseContext(), EglBase.CONFIG_RGBA);
        }

        if (mTextureView != null) {
            attachTextureView();
        }

        if (previewEglBase == null || !previewEglBase.hasSurface()) {
            return ;
        }

        if (previewDrawer == null) {
            previewDrawer = new GlRectDrawer();
        }
        try {
            // Bind eglContext、eglDisplay、eglSurface
            // 绑定eglContext、eglDisplay、eglSurface
            previewEglBase.makeCurrent();

            int scaleWidth = mViewWidth;
            int scaleHeight = mViewHeight;

            System.arraycopy(texMatrix, 0, mPreviewMatrix, 0, 16);
            if (mViewMode == 0) {
                if (mViewHeight * width <= mViewWidth * height) {
                    scaleWidth = mViewHeight * width / height;
                } else {
                    scaleHeight = mViewWidth * height / width;
                }
            } else if (mViewMode == 1) {
                if (mViewHeight * width <= mViewWidth * height) {
                    scaleHeight = mViewWidth * height / width;
                } else {
                    scaleWidth = mViewHeight * width / height;
                }
                float fWidthScale = (float)mViewWidth / scaleWidth;
                float fHeightScale = (float)mViewHeight / scaleHeight;
                Matrix.scaleM(mPreviewMatrix, 0, fWidthScale, fHeightScale, 1.0f);
                Matrix.translateM(mPreviewMatrix, 0, (1.0f - fWidthScale) / 2.0f, (1.0f - fHeightScale) / 2.0f, 1.0f);

                scaleWidth = mViewWidth;
                scaleHeight = mViewHeight;
            }

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            Matrix.rotateM(mPreviewMatrix, 0, 180, 0, 0, 1);
            Matrix.translateM(mPreviewMatrix, 0, -1, -1, 0);
            previewDrawer.drawRgb(textureId, mPreviewMatrix, width, height,
                        (mViewWidth - scaleWidth) / 2,
                        (mViewHeight - scaleHeight) / 2,
                    scaleWidth, scaleHeight);

            // Exchange the rendered buffer to display
            // 交换渲染好的buffer 去显示
            previewEglBase.swapBuffers();



            if (mIsCapture) {
                long now = SystemClock.elapsedRealtime();
                // 将图片数据传给ZEGO SDK，包括时间戳
                // Pass image data to ZEGO SDK, including timestamp
                final ZegoExpressEngine zegoExpressEngine = ZegoExpressEngine.getEngine();
                if (zegoExpressEngine != null) {
                    zegoExpressEngine.sendCustomVideoCaptureTextureData(textureId, width, height, now);
                }
            }

            // 分离当前eglContext
            previewEglBase.detachCurrent();

        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    // Destroy surface
    // 销毁用于屏幕显示的surface（预览）
    private void releasePreviewSurface() {
        if (previewEglBase == null) {
            return ;
        }

        if (previewEglBase.hasSurface()) {
            // Bind eglContext、eglDisplay、eglSurface
            // 绑定eglContext、eglDisplay、eglSurface
            previewEglBase.makeCurrent();

            if (previewDrawer != null) {
                previewDrawer = null;
            }

            previewEglBase.releaseSurface();
            previewEglBase.detachCurrent();
        }

        previewEglBase.release();
        previewEglBase = null;
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        if (surfaceTexture == null || mDummyContext == null) {
            return;
        }

        // Bind eglContext、eglDisplay、eglSurface
        // 绑定eglContext、eglDisplay、eglSurface
        mDummyContext.makeCurrent();
        surfaceTexture.updateTexImage();
        long timestamp = surfaceTexture.getTimestamp();
        surfaceTexture.getTransformMatrix(mInputMatrix);

        // Do preprocessing here such as camera 360 sdk

        // Correct the image display orientation
        // 纠正图像展示方向
        int width = mCameraWidth;
        int height = mCameraHeight;
        if (mImageRotation == 90 || mImageRotation == 270) {
            int temp = width;
            width = height;
            height = temp;
        }

        if (mTextureId == 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            mCaptureID = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);
            // Generate 2D texture
            // 生成2D纹理
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        }
            if (mTextureId == 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            mTextureId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);
            // Generate 2D texture
            // 生成2D纹理
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            // Create a frame buffer object, draw the texture to the frame buffer and return the buffer index.
            // 创建帧缓冲对象，绘制纹理到帧缓冲区并返回缓冲区索引
            mFrameBufferId = GlUtil.generateFrameBuffer(mTextureId);
            Log.e("zego","生成的mFrameBufferId="+mFrameBufferId);
        } else {
                // Bind frame buffer
                // 绑定帧缓冲区
                GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBufferId);
        }
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        // Draw OES texture
        // 绘制OES texture
        Matrix.rotateM(mInputMatrix, 0, 180, 0, 0, 1);
        Matrix.translateM(mInputMatrix, 0, -1, -1, 0);
        mDummyDrawer.drawOes(mInputTextureId, mInputMatrix,
                width, height, 0, 0, width, height);
        // Bind frame buffer
        // 解邦帧缓冲区
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        // Draw picture data
        // 绘制
        if (mIsPreview) {
            draw(mTextureId, width, height, mIdentityMatrix);
        }
        mDummyContext.detachCurrent();
    }
}
