package im.zego.CustomerVideoCapture;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import im.zego.CustomerVideoCapture.aveencoder.AVCEncoder;
import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoVideoEncodedFrameFormat;
import im.zego.zegoexpress.constants.ZegoVideoFrameFormat;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoTrafficControlInfo;
import im.zego.zegoexpress.entity.ZegoVideoEncodedFrameParam;
import im.zego.zegoexpress.entity.ZegoVideoFrameParam;

public class VideoCaptureFromImage3 extends ZegoVideoCaptureCallback {
        // Thread for drawing
        private Thread thread;
        // Control whether the thread is running
        private boolean isRunning;
        // Canvas for drawing
        private Canvas canvas;
        // Paint for drawing
        private Paint paint;
        // View to show the preview
        public TextureView view;
        public Context context;
        // Store the data for publishing
        ByteBuffer buf;
        public ZegoExpressEngine engine;
        // Store the data for publishing
        byte[] buffer;
        byte[] nv12;
        // Help to control the position of picture
        int mX = 0;
        int mY = 0;
        // Size of publishing stream view
        int height = 720;
        int width = 720;
        // Size of picture
        int logoWidth = 180;
        int logoHeight = 180;
        int length = (int) Math.ceil(width*height*1.5);
        // Store the data of picture
        byte[] pictureData;
        // Size of preview
        int previewWidth;
        int previewHeight;
        private AVCEncoder mAVCEncoder = null;
        public VideoCaptureFromImage3(Context context, ZegoExpressEngine engine) {
            this.context = context;
            this.engine = engine;
        }

        @Override
        public void onStart(ZegoPublishChannel channel) {
            init();
            isRunning = true;
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (isRunning) {
                        draw();
                        // Draw every second
                        long startMs = System.currentTimeMillis();
                        long endMs = System.currentTimeMillis();
                        long needTime = 800;
                        long usedTime = endMs - startMs;
                        if (usedTime < needTime) {
                            try {
                                Thread.sleep(needTime - usedTime);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            thread.start();
        }

        @Override
        public void onStop(ZegoPublishChannel channel) {
            // Stop the thread
            isRunning = false;
            // 停止编码器并释放编码器资源
            if (mAVCEncoder != null) {
                mAVCEncoder.stopEncoder();
                mAVCEncoder.releaseEncoder();
                mAVCEncoder = null;
            }
        }

        @Override
        public void setView(View view){
            // Set view and preview size
            this.view = (TextureView) view;
            this.previewWidth = view.getWidth();
            this.previewHeight = view.getHeight();
        }
        private void init() {
            paint = new Paint(Paint.ANTI_ALIAS_FLAG);

            if (mAVCEncoder == null) {
                // 检测设备是否支持编码I420数据
                boolean isSupport = AVCEncoder.isSupportI420();
                if (isSupport) {
                    // 创建编码器
                    // Create AVC encoder
                    mAVCEncoder = new AVCEncoder(width, height);
                    // 启动编码器
                    // Start the encoder
                    mAVCEncoder.startEncoder();
                } else {
                    Log.e("Zego", "This demo don't support color formats other than I420.");
                    Toast.makeText(context, "The current device does not support encoding I420 data, please try another device", Toast.LENGTH_SHORT).show();
                }
            }
        }

        private void draw() {
            try {
                canvas = view.lockCanvas();
                if (canvas != null) {
                    // draw the preview
                    drawPreview(canvas);
                    // draw YUV file
                    drawYUV();
                    // put byte[] into ByteBuffer
                    if (buf == null) {
                        buf = ByteBuffer.allocateDirect(buffer.length);
                    }

                    if (mAVCEncoder != null) {
                        // Encoder related information
                        // 编码器相关信息
                        ZegoVideoEncodedFrameParam zegoVideoEncodedFrameParam = new ZegoVideoEncodedFrameParam();
                        ZegoVideoEncodedFrameFormat frameFormat = ZegoVideoEncodedFrameFormat.AnnexB;
                        zegoVideoEncodedFrameParam.format = frameFormat;
                        // Android端的编码类型必须选用 ZegoVideoCodecTypeAVCANNEXB
                        // For Android, encoded type must be ZegoVideoCodecTypeAVCANNEXB
                        zegoVideoEncodedFrameParam.width = width;
                        zegoVideoEncodedFrameParam.height = height;

                        // calculate current ns time.
                        // 计算当前的纳秒时间
                        long now = 0;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                            now = SystemClock.elapsedRealtimeNanos();
                        } else {
                            now = TimeUnit.MILLISECONDS.toNanos(SystemClock.elapsedRealtime());
                        }

                        nv12 = new byte[buffer.length];
                        // Convert video data in NV21 format to NV12 format
                        // 将NV21格式的视频数据转为NV12格式的
                        nv12 = NV21ToI420(buffer, width, height);
                        // Provide video frame data and timestamp for the encoder
                        // 为编码器提供视频帧数据和时间戳
                        mAVCEncoder.inputFrameToEncoder(nv12, now);

                        // Get the encoded video data, return null when the encoding is not completed
                        // 取编码后的视频数据，编码未完成时返回null
                        AVCEncoder.TransferInfo transferInfo = mAVCEncoder.pollFrameFromEncoder();

                        // Encoding complete
                        // 编码完成
                        if (transferInfo != null) {
                            if (buf != null && transferInfo.inOutData.length > buf.capacity()) {
                                buf = ByteBuffer.allocateDirect(transferInfo.inOutData.length);
                            }
                            buf.clear();
                            // Store the encoded data in ByteBuffer
                            // 将编码后的数据存入ByteBuffer中
                            buf.put(transferInfo.inOutData);
                            buf.flip();
                            // Transfer the encoded video data to the ZEGO SDK.
                            // It is necessary to inform the SDK whether the currently transferred frame is a video key frame, and the timestamp of the current video frame
                            // 将编码后的视频数据传给ZEGO SDK，需要告知SDK当前传递帧是否为视频关键帧，以及当前视频帧的时间戳
                            final ZegoExpressEngine zegoExpressEngine = ZegoExpressEngine.getEngine();
                            if (zegoExpressEngine != null) {
                                zegoVideoEncodedFrameParam.isKeyFrame = transferInfo.isKeyFrame;
                                zegoExpressEngine.sendCustomVideoCaptureEncodedData(buf, transferInfo.inOutData.length, zegoVideoEncodedFrameParam, transferInfo.timeStmp);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    view.unlockCanvasAndPost(canvas);
                }
            }
        }
        public void drawPreview(Canvas canvas){
            canvas.drawColor(Color.BLACK);
            Bitmap bitmap = createBitmapFromAsset();
            float scaleWidth = ((float) previewWidth) / width;
            float scaleHeight = ((float) previewHeight) / height;
            canvas.scale(scaleWidth,scaleHeight);
            canvas.drawBitmap(bitmap, mX*logoWidth, mY*logoHeight, paint);
        }
        // 从资源区获取图片位图
        // Get picture bitmap from resource area
        private Bitmap createBitmapFromAsset() {
            Bitmap bitmap = null;
            try {
                AssetManager assetManager = context.getAssets();
                InputStream is = assetManager.open("logo.png");
                bitmap = BitmapFactory.decodeStream(is);
                if (bitmap != null) {
                    System.out.println("测试一:width=" + bitmap.getWidth() + " ,height=" + bitmap.getHeight());
                } else {
                    System.out.println("bitmap == null");
                }
            } catch (Exception e) {
                System.out.println("异常信息:" + e.toString());
            }
            return bitmap;
        }

        public void drawYUV(){
            try {
                buffer = new byte[length];
                // Set background color to black
                for (int i=0;i<width*height;i++){
                    buffer[i] = 0;
                }
                for (int i=width*height;i<length;i++){
                    buffer[i] = 127;
                }
                // Read YUV file
                AssetManager assetManager = context.getAssets();
                InputStream is = assetManager.open("logo.yuv");
                pictureData = new byte[is.available()];
                is.read(pictureData);
                is.close();
                // Update the picture position
                mX = (mX + 1) % 4;
                if (mX == 0) {
                    mY = (mY + 1) % 4;
                }
                // Draw YUV file
                for (int n=0; n<logoHeight;n++) {
                    for (int j = 0;j<logoWidth;j++){
                        buffer[(n+mY*logoHeight)*width+mX*logoWidth+j] = pictureData[n*logoWidth+j];
                    }
                }
                for (int n=0; n<logoHeight/2;n++) {
                    for (int j = 0;j< logoWidth;j++){
                        buffer[width*height+(mY*logoHeight/2+n)*width+mX*logoWidth+j] = pictureData[logoHeight*logoWidth+n*logoWidth+j];
                    }
                }
            } catch (Exception e) {
                System.out.println("异常信息:" + e.toString());
            }
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
