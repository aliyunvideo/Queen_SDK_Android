package im.zego.CustomerVideoCapture;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import im.zego.CustomerVideoCapture.aveencoder.AVCEncoder;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoVideoEncodedFrameFormat;
import im.zego.zegoexpress.entity.ZegoTrafficControlInfo;
import im.zego.zegoexpress.entity.ZegoVideoEncodedFrameParam;


/**
 * VideoCaptureFromCamera3
 * 实现从摄像头采集数据并传给ZEGO SDK.
 * 采用码流方式传递数据，即传递编码后的视频数据
 *  
 * Collect data from camera and send it to ZEGO SDK.
 * Use bit stream to transfer encoded video data
 */
@TargetApi(21)
public class VideoCaptureFromCamera3 extends ZegoVideoCaptureCallback implements Camera.PreviewCallback, TextureView.SurfaceTextureListener {
    private static final String TAG = "VideoCaptureFromCamera";
    private static final int CAMERA_STOP_TIMEOUT_MS = 7000;

    private Camera mCam = null;
    private Camera.CameraInfo mCamInfo = null;
    // Use back camera
    // 默认为后置摄像头
    int mFront = 0;
    // Resolution width
    // 预设分辨率宽
    int mWidth = 640;
    // Resolution height
    // 预设分辨率高
    int mHeight = 480;
    // Frame Rate
    // 预设采集帧率
    int mFrameRate = 15;
    // No rotation
    // 默认不旋转
    int mRotation = 0;

    private TextureView mView = null;
    private SurfaceTexture mTexture = null;

    // Arbitrary queue depth.  Higher number means more memory allocated & held,
    // lower number means more sensitivity to processing time in the client (and
    // potentially stalling the capturer if it runs out of buffers to write to).
    private static final int NUMBER_OF_CAPTURE_BUFFERS = 3;
    private final Set<byte[]> queuedBuffers = new HashSet<byte[]>();
    private int mFrameSize = 0;

    private HandlerThread mThread = null;
    private volatile Handler cameraThreadHandler = null;
    private final AtomicBoolean isCameraRunning = new AtomicBoolean();
    private final Object pendingCameraRestartLock = new Object();
    private volatile boolean pendingCameraRestart = false;

    private ByteBuffer mEncodedBuffer;
    private AVCEncoder mAVCEncoder = null;
    private Context context;

    public VideoCaptureFromCamera3(Activity context) {
        super();
        this.context=context;

        mThread = new HandlerThread("camera-cap");
        mThread.start();

        // 创建camera异步消息处理handler
        // Create a camera asynchronous message processing handler
        cameraThreadHandler = new Handler(mThread.getLooper());
    }

    /**
     * 初始化资源，必须实现
     * Initialization of resources must be achieved
     */
    @Override
    public void onStart(ZegoPublishChannel channel) {
        Log.i(TAG, "onStart");

        if (mAVCEncoder == null) {
            // Check whether the device supports I420
            // 检测设备是否支持编码I420数据
            boolean isSupport = AVCEncoder.isSupportI420();
            if (isSupport){
                // Create AVC encoder
                // 创建编码器
                mAVCEncoder = new AVCEncoder(mWidth, mHeight);
                mAVCEncoder.setRotation(90);
                // allocate buffer for encoder
                // 为编码器分配内存
                mEncodedBuffer = ByteBuffer.allocateDirect(mWidth*mHeight*3/2);
                // Start the encoder
                // 启动编码器
                mAVCEncoder.startEncoder();
            } else {
                Log.e("Zego","This demo don't support color formats other than I420.");
                Toast.makeText(context, "The current device does not support encoding I420 data, please try another device", Toast.LENGTH_SHORT).show();
            }
        }

        startCapture();
    }

    // 停止推流时，ZEGO SDK 调用 stopCapture 通知外部采集设备停止采集，必须实现
    // When stopping pushing, the ZEGO SDK calls stopCapture to notify the external collection device to stop collection, which must be implemented
    @Override
    public void onStop(ZegoPublishChannel channel) {
        Log.i(TAG, "onStop");
        // 停止camera采集任务
        stopCapture();
        mThread.quit();
        mThread = null;

        // 停止编码器并释放编码器资源
        if (mAVCEncoder != null) {
            mAVCEncoder.stopEncoder();
            mAVCEncoder.releaseEncoder();
        }
        printCount = 0;
    }

    @Override
    public void onEncodedDataTrafficControl(ZegoTrafficControlInfo trafficControlInfo, ZegoPublishChannel channel) {
        if (mAVCEncoder != null) {
            MediaFormat mMediaFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, trafficControlInfo.width, trafficControlInfo.height);
            mMediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible); //COLOR_FormatYUV420PackedSemiPlanar
            mMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, trafficControlInfo.bitrate);
            mMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, trafficControlInfo.fps);
            mMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 2);
            mAVCEncoder.setMediaFormat(mMediaFormat);
        }
    }

    // When publish starts, ZEGO SDK calls startCapture to notify the external capture device to start working, which must be implemented
    // 开始推流时，ZEGO SDK 调用 startCapture 通知外部采集设备开始工作，必须实现
    protected int startCapture() {
        if (isCameraRunning.getAndSet(true)) {
            Log.e(TAG, "Camera has already been started.");
            return 0;
        }

        final boolean didPost = maybePostOnCameraThread(new Runnable() {
            @Override
            public void run() {
                // Create camera
                // 创建camera
                createCamOnCameraThread();
                // Start camera
                // 启动camera
                startCamOnCameraThread();
            }
        });

        return 0;
    }

    // When stopping publish, ZEGO SDK calls stopCapture to notify the external capture device to stop capture, which must be implemented
    // 停止推流时，ZEGO SDK 调用 stopCapture 通知外部采集设备停止采集，必须实现
    protected int stopCapture() {
        Log.d(TAG, "stopCapture");
        final CountDownLatch barrier = new CountDownLatch(1);
        final boolean didPost = maybePostOnCameraThread(new Runnable() {
            @Override public void run() {
                // Stop camera
                // 停止camera
                stopCaptureOnCameraThread(true /* stopHandler */);
                // 释放camera资源
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
        printCount = 0;

        Log.d(TAG, "stopCapture done");

        return 0;
    }

    // set frame rate
    // 设置采集帧率
    protected int setFrameRate(int framerate) {
        mFrameRate = framerate;
        // Update the capture frame rate of the camera
        // 更新camera的采集帧率
        updateRateOnCameraThread(framerate);
        return 0;
    }

    // set view width and height
    // 设置视图宽高
    protected int setResolution(int width, int height) {
        mWidth = width;
        mHeight = height;
        // It is necessary to restart camera after modifying resolution.
        // 修改视图宽高后需要重启camera
        restartCam();
        return 0;
    }

    // Switch between front and rear cameras
    // 前后摄像头的切换
    protected int setFrontCam(int bFront) {
        mFront = bFront;
        // You need to restart the camera after switching the camera
        // 切换摄像头后需要重启camera
        restartCam();
        return 0;
    }

    // Set view for preview
    // 设置展示视图
    public void setView(final View view) {
        if (mView != null) {
            if (mView.getSurfaceTextureListener().equals(this)) {
                mView.setSurfaceTextureListener(null);
            }
            mView = null;
            mTexture = null;
        }
        mView = (TextureView) view;
        if (mView != null) {
            // 设置SurfaceTexture相关回调监听
            mView.setSurfaceTextureListener(VideoCaptureFromCamera3.this);
            if (mView.isAvailable()) {
                mTexture = mView.getSurfaceTexture();
            }
        }
    }

    // Set the rotation direction when collecting
    // 设置采集时的旋转方向
    protected int setCaptureRotation(int nRotation) {
        mRotation = nRotation;
        return 0;
    }

    // Update the capture frame rate of the camera
    // 更新camera的采集帧率
    private int updateRateOnCameraThread(final int framerate) {
        checkIsOnCameraThread();
        if (mCam == null) {
            return 0;
        }

        mFrameRate = framerate;

        Camera.Parameters parms = mCam.getParameters();
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            if ((entry[0] == entry[1]) && entry[0] == mFrameRate * 1000) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                break;
            }
        }

        int[] realRate = new int[2];
        parms.getPreviewFpsRange(realRate);
        if (realRate[0] == realRate[1]) {
            mFrameRate = realRate[0] / 1000;
        } else {
            mFrameRate = realRate[1] / 2 / 1000;
        }

        try {
            mCam.setParameters(parms);
        } catch (Exception ex) {
            Log.i(TAG, "vcap: update fps -- set camera parameters error with exception\n");
            ex.printStackTrace();
        }
        return 0;
    }

    // Check whether CameraThread is running
    // 检查CameraThread是否正常运行
    private void checkIsOnCameraThread() {
        if (cameraThreadHandler == null) {
            Log.e(TAG, "Camera is not initialized - can't check thread.");
        } else if (Thread.currentThread() != cameraThreadHandler.getLooper().getThread()) {
            throw new IllegalStateException("Wrong thread");
        }
    }

    // Refresh UI
    // 控制UI刷新
    private boolean maybePostOnCameraThread(Runnable runnable) {
        return cameraThreadHandler != null && isCameraRunning.get()
                && cameraThreadHandler.postAtTime(runnable, this, SystemClock.uptimeMillis());
    }

    // Create camera
    // 创建camera
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

        // 获取欲设置camera的索引号
        int nFacing = (mFront != 0) ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK;

        if (mCam != null) {
            // 已打开camera
            return 0;
        }

        // find camera
        mCamInfo = new Camera.CameraInfo();
        // Get the number of camera
        // 获取设备上camera的数目
        int nCnt = Camera.getNumberOfCameras();
        // Get index of chosen camera and start the camera
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

        // Cannot find camera
        // 没找到欲设置的camera
        if (mCam == null) {
            Log.i(TAG, "[WARNING] no camera found, try default\n");
            // Try to start default camera
            // 先试图打开默认camera
            mCam = Camera.open();

            if (mCam == null) {
                Log.i(TAG, "[ERROR] no camera found\n");
                return -1;
            }
        }

        boolean bSizeSet = false;
        Camera.Parameters parms = mCam.getParameters();

        // hardcode
        Camera.Size psz = mCam.new Size(640, 480);

        // Set size of camera capture view
        // 设置camera的采集视图size
        parms.setPreviewSize(psz.width, psz.height);
        mWidth = psz.width;
        mHeight = psz.height;

        // get supported frame rate range then set preview frame rate range.
        // 获取camera支持的帧率范围，并设置预览帧率范围
        List<int[]> supported = parms.getSupportedPreviewFpsRange();

        for (int[] entry : supported) {
            if ((entry[0] == entry[1]) && entry[0] == mFrameRate * 1000) {
                parms.setPreviewFpsRange(entry[0], entry[1]);
                break;
            }
        }

        // Get real frame rate
        // 获取camera的实际帧率
        int[] realRate = new int[2];
        parms.getPreviewFpsRange(realRate);
        if (realRate[0] == realRate[1]) {
            mFrameRate = realRate[0] / 1000;
        } else {
            mFrameRate = realRate[1] / 2 / 1000;
        }

        // If we do not enable this feature to improve camera performance, it may cause distortion problems in preview on some phones.
        // 不启用提高MediaRecorder录制摄像头视频性能的功能，可能会导致在某些手机上预览界面变形的问题
        parms.setRecordingHint(false);
        // Set focus mode of camera.
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

        // Set camera parameters
        // 设置camera的参数
        try {
            mCam.setParameters(parms);
        } catch (Exception ex) {
            Log.i(TAG, "vcap: set camera parameters error with exception\n");
            ex.printStackTrace();
        }

        Camera.Parameters actualParm = mCam.getParameters();
        mWidth = actualParm.getPreviewSize().width;
        mHeight = actualParm.getPreviewSize().height;
        Log.i(TAG, "[WARNING] vcap: focus mode " + actualParm.getFocusMode());

        createPool();

        int result;
        if (mCamInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (mCamInfo.orientation + mRotation) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (mCamInfo.orientation - mRotation + 360) % 360;
        }
        // 设置预览图像的转方向
        mCam.setDisplayOrientation(result);

        return 0;
    }

    // Allocate memory for the camera to store collected data
    // 为camera分配内存存放采集数据
    private void createPool() {
        queuedBuffers.clear();
        mFrameSize = mWidth * mHeight * 3 / 2;
        for (int i = 0; i < NUMBER_OF_CAPTURE_BUFFERS; ++i) {
            final ByteBuffer buffer = ByteBuffer.allocateDirect(mFrameSize);
            queuedBuffers.add(buffer.array());
            // 减少camera预览时的内存占用
            mCam.addCallbackBuffer(buffer.array());
        }
    }

    // Start Camera
    // 启动camera
    private int startCamOnCameraThread() {
        checkIsOnCameraThread();
        if (!isCameraRunning.get() || mCam == null) {
            Log.e(TAG, "startPreviewOnCameraThread: Camera is stopped");
            return 0;
        }

        if (mTexture == null) {
            return -1;
        }

        try {
            mCam.setPreviewTexture(mTexture);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Assign a buffer address before starting the camera preview, the purpose is to reuse the memory
        // 在打开摄像头预览前先分配一个buffer地址，目的是为了后面内存复用
        mCam.setPreviewCallbackWithBuffer(this);
        // Start camera preview
        // 启动camera预览
        mCam.startPreview();
        return 0;
    }

    // stop camera capture
    // 停止camera采集
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
            // stop camera preview
            // 停止camera预览
            mCam.stopPreview();
            mCam.setPreviewCallbackWithBuffer(null);
        }
        queuedBuffers.clear();
        return 0;
    }

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

    private int printCount = 0;
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.ms");

    // Preview frame callback
    // 预览视频帧回调
    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        checkIsOnCameraThread();
        if (!isCameraRunning.get()) {
            Log.e(TAG, "onPreviewFrame: Camera is stopped");
            return;
        }

        if (!queuedBuffers.contains(data)) {
            // |data| is an old invalid buffer.
            return;
        }

        final ZegoExpressEngine zegoExpressEngine = ZegoExpressEngine.getEngine();
        if (zegoExpressEngine == null) {
            return;
        }

        if (mAVCEncoder != null) {
            // Encoder related information
            // 编码器相关信息
            ZegoVideoEncodedFrameParam zegoVideoEncodedFrameParam = new ZegoVideoEncodedFrameParam();
            ZegoVideoEncodedFrameFormat frameFormat = ZegoVideoEncodedFrameFormat.AnnexB;
            zegoVideoEncodedFrameParam.format = frameFormat;
            // Android端的编码类型必须选用 ZegoVideoCodecTypeAVCANNEXB
            // For Android, encoded type must be ZegoVideoCodecTypeAVCANNEXB
            zegoVideoEncodedFrameParam.width = mWidth;
            zegoVideoEncodedFrameParam.height = mHeight;

            // calculate current ns time.
            // 计算当前的纳秒时间
            long now = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                now = SystemClock.elapsedRealtimeNanos();
            } else {
                now = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
            }

            // Convert the video data in NV21 format to I420 format
            // 将NV21格式的视频数据转为I420格式的
            byte[] i420bytes = NV21ToI420(data, mWidth, mHeight);
            // Provide video frame data and timestamp for the encoder
            // 为编码器提供视频帧数据和时间戳
            mAVCEncoder.inputFrameToEncoder(i420bytes, now);

            // Get the encoded video data, return null when the encoding is not completed
            // 取编码后的视频数据，编码未完成时返回null
            AVCEncoder.TransferInfo transferInfo = mAVCEncoder.pollFrameFromEncoder();

            // Encoding complete
            // 编码完成
            if (transferInfo != null) {
                if (mEncodedBuffer != null && transferInfo.inOutData.length > mEncodedBuffer.capacity()) {
                    mEncodedBuffer = ByteBuffer.allocateDirect(transferInfo.inOutData.length);
                }

                mEncodedBuffer.clear();
                // Store the encoded data in ByteBuffer
                // 将编码后的数据存入ByteBuffer中
                mEncodedBuffer.put(transferInfo.inOutData, 0, transferInfo.inOutData.length);

                // Transfer the encoded video data to the ZEGO SDK.
                // It is necessary to inform the SDK whether the currently transferred frame is a video key frame, and the timestamp of the current video frame
                // 将编码后的视频数据传给ZEGO SDK，需要告知SDK当前传递帧是否为视频关键帧，以及当前视频帧的时间戳
                if (zegoExpressEngine != null) {
                    zegoVideoEncodedFrameParam.isKeyFrame = transferInfo.isKeyFrame;
                    zegoExpressEngine.sendCustomVideoCaptureEncodedData(mEncodedBuffer, transferInfo.inOutData.length, zegoVideoEncodedFrameParam, transferInfo.timeStmp);
                }
                // Print the time when the encoded data was first passed to the SDK
                // 打印第一次传递编码数据给SDK的时间
                if (printCount == 0) {
                    Date date = new Date(System.currentTimeMillis());
                    Log.d("Zego", "encode data transfer time: " + simpleDateFormat.format(date));
                    printCount++;
                }
            }
        }
        // Reuse memory for camera preview
        // 实现camera预览时的内存复用
        camera.addCallbackBuffer(data);
    }

    // TextureView.SurfaceTextureListener 回调
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mTexture = surface;
        // Start capture
        // 启动采集
        startCapture();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        mTexture = surface;
        // Restart the camera if view size changed
        // 视图size变化时重启camera
        restartCam();
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mTexture = null;
        // 停止采集
        stopCapture();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

    // The camera collects data in NV21 format, the encoder needs data in I420 format, and a format conversion is performed here
    // camera采集的是NV21格式的数据，编码器需要I420格式的数据，此处进行一个格式转换
    public static byte[] NV21ToI420(byte[] data, int width, int height) {
        byte[] ret = new byte[width*height*3/2];
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 7, total);
        for (int i=total+7; i<data.length; i+=2) {
            bufferV.put(data[i]);
            if (i+1 < data.length) {
                bufferU.put(data[i+1]);
            }
        }

        return ret;
    }
}
