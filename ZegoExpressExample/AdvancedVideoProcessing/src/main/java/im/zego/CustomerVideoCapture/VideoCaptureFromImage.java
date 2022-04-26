package im.zego.CustomerVideoCapture;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.SystemClock;
import android.view.TextureView;
import android.view.View;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import im.zego.zegoexpress.ZegoExpressEngine;
import im.zego.zegoexpress.constants.ZegoPublishChannel;
import im.zego.zegoexpress.constants.ZegoVideoFrameFormat;
import im.zego.zegoexpress.constants.ZegoViewMode;
import im.zego.zegoexpress.entity.ZegoVideoFrameParam;

public class VideoCaptureFromImage extends ZegoVideoCaptureCallback {
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
        public VideoCaptureFromImage(Context context, ZegoExpressEngine engine) {
            this.context = context;
            this.engine = engine;
        }

        @Override
        public void onStart(ZegoPublishChannel channel) {
            init();
            engine.setCustomVideoCaptureFillMode(ZegoViewMode.SCALE_TO_FILL);
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
            if(buf!=null){
                buf.clear();
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
                    buf.put(buffer);
                    // Set Video Frame parameters
                    ZegoVideoFrameParam param = new ZegoVideoFrameParam();
                    param.width = 720;
                    param.height = 720;
                    param.format = ZegoVideoFrameFormat.NV21;
                    param.strides[0] = 720;
                    param.strides[1] = 720;
                    long now;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                        now = SystemClock.elapsedRealtime();
                    } else {
                        now = TimeUnit.MILLISECONDS.toMillis(SystemClock.elapsedRealtime());
                    }
                    buf.flip();
                    engine.sendCustomVideoCaptureRawData(buf,buffer.length,param,now);
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
}
