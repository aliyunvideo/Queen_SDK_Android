package com.aliyun.maliang.android.simpleapp.CameraV1GLSurfaceView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

import com.aliyun.maliang.android.simpleapp.CameraV1;
import com.aliyun.maliang.android.simpleapp.QueenParamHolder;
import com.taobao.android.libqueen.ImageFormat;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.QueenUtil;
import com.taobao.android.libqueen.Texture2D;
import com.taobao.android.libqueen.exception.InitializationException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class CameraV1Renderer implements GLSurfaceView.Renderer {

    private static final String TAG = "CameraV1Renderer";
    private Context mContext;
    private int mOESTextureId = -1;
    private Texture2D mOutTexture;
    private SurfaceTexture mSurfaceTexture;
    private float[] transformMatrix = new float[16];
    private CameraV1GLSurfaceView mGLSurfaceView;
    private CameraV1 mCamera;
    private  byte[] mCameraBytes;

    QueenEngine engine;
    private FrameGlDrawer mFrameGlDrawer;

    private boolean mTestOutRect = false;

    public void init(CameraV1GLSurfaceView glSurfaceView, CameraV1 camera, boolean isPreviewStarted, Context context) {
        mContext = context;
        mGLSurfaceView = glSurfaceView;
        mCamera = camera;
        mCameraBytes = null;

        Message message = mHandler.obtainMessage(1);
        mHandler.sendMessage(message);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = QueenUtil.createTexture2D(true);
        initSurfaceTexture();


        if (false) {
            // 直接输出到GL视窗
            initCommon();
        } else {
            // 使用自定义纹理承载美颜结果纹理
            initWithOutTexture();
        }
        mFrameGlDrawer = new FrameGlDrawer();

        if(mTestOutRect) {
            int len = mCamera.getPrevieHeight();
            int offset = (mCamera.getPrevieWidth() - mCamera.getPrevieHeight()) / 2;
            if(mCamera.isLandscape()) {
                engine.setOutputRect(offset, 0, len, len);
            } else {
                engine.setOutputRect(0, offset, len, len);
            }
        }
    }

    private void initCommon() {
        try {
            engine = new QueenEngine(mContext,true);
        } catch (InitializationException e) {
            e.printStackTrace();
        }

        engine.setInputTexture(mOESTextureId, mCamera.getCameraOutWidth(),mCamera.getCameraOutHeight(),true);
    }

    private void initWithOutTexture() {
        try {
            engine = new QueenEngine(mContext,true);
        } catch (InitializationException e) {
            e.printStackTrace();
        }
        engine.setInputTexture(mOESTextureId, mCamera.getCameraOutWidth(),mCamera.getCameraOutHeight(),true);
        mOutTexture = engine.autoGenOutTexture();
        engine.updateOutTexture(mOutTexture.getTextureId(), mCamera.getCameraOutWidth(),mCamera.getCameraOutHeight());
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float cameraRatio = 1280 / 720.0f;
        final boolean isLandscape = width > height;
        if (isLandscape) {
            int tmp = width;    //强缺转成portrait计算
            width = height;
            height = tmp;
        }

        if (width > 0 && engine != null) {
            if (mTestOutRect) { //设置了outRect，保证OutRect完整展示. 而此时画面width是：
                int bottom = (height - width ) / 2;
                if (isLandscape) {
                    engine.setScreenViewport(bottom, 0, width, width);
                } else {
                    engine.setScreenViewport(0, bottom, width, width );
                }
            } else { //camera会旋转，所以camera与screen的长短边是对应的

                float screenRatio =  height / (float) width;

                int w = width, h = height, offsetW = 0, offsetH = 0;

                if (screenRatio >= cameraRatio) {   //屏幕更窄长，裁掉carema一部分短边内容使fit(长边fit, 短边 = l * 720 / 1280, offset = -(s - ScreenS) / 2
                    w = (int) (height / cameraRatio);
                    offsetW = (width - w) / 2;
                } else {    //裁掉camera一部分长边内容，使用其更粗短fit
                    h = (int)(width * cameraRatio);
                    offsetH = (height - h) / 2;
                }

                if (isLandscape) {
                    engine.setScreenViewport(offsetH, offsetW, h, w);
                } else {
                    engine.setScreenViewport(offsetW, offsetH, w, h);
                }
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        mCameraBytes = mCamera.getLastUpdateCameraPixels();
        if (mCameraBytes != null) {
            // 说明没有新的数据,则不必再去更新人脸引擎去刷新,减少运算消耗
            //人脸检测数据
            engine.updateInputDataAndRunAlg(mCameraBytes, ImageFormat.NV21, mCamera.getPrevieWidth(), mCamera.getPrevieHeight(), 0, mCamera.inputAngle, mCamera.outAngle, mCamera.flipAxis);
            mCamera.releaseData(mCameraBytes);
        }

        QueenParamHolder.writeParamToEngine(engine);

        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        int retCode = engine.renderTexture(transformMatrix);

        if (retCode == -9 || retCode == -10) {
            mFrameGlDrawer.draw(transformMatrix, mOESTextureId, true);
        }

        ++mCurrentDrawTimes;
    }

    private long mLastDrawSystemClock = 0L;
    private long mCurrentDrawTimes = 0L;
    private long mLastDrawTimes = 0L;
    private TextView mFpsView = null;
    public void setFpsView(TextView textView) {
        mFpsView = textView;
    }

    //更新fps显示
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long curDrawSystemClock = SystemClock.elapsedRealtime();
                    long curDrawTimes = mCurrentDrawTimes;
                    long drawTimes = curDrawTimes - mLastDrawTimes;
                    long drawCostTime = curDrawSystemClock - mLastDrawSystemClock;
                    long fps = drawTimes * 1000 / drawCostTime;
                    mLastDrawTimes = curDrawTimes;
                    mLastDrawSystemClock = curDrawSystemClock;

                    if(mFpsView != null){
                        mFpsView.setText("fps: " + fps);
                    }
                    this.sendMessageDelayed(this.obtainMessage(1),480);
                    break;
                default:
                    break;

            }
        }
    };

    /**
     * 图片美颜处理
     */
    public void testPicture()
    {
        try {
            QueenEngine picEngine = new QueenEngine(mContext,true,false);
            Bitmap imageFace = BitmapFactory.decodeStream(mContext.getAssets().open("face/1.png") );
            picEngine.setInputBitMap(imageFace);
            Texture2D outTexture = picEngine.autoGenOutTexture();

            picEngine.updateInputDataAndRunAlg(imageFace);

            picEngine.render();
            outTexture.saveToFile("/sdcard/xxxxx/aabb.png", Bitmap.CompressFormat.PNG, 100);
            picEngine.release();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean initSurfaceTexture() {
        if (mCamera == null || mGLSurfaceView == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return false;
        }
        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
            @Override
            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                mGLSurfaceView.requestRender();
            }
        });
        mCamera.setPreviewTexture(mSurfaceTexture);
        mCamera.startPreview();
        return true;
    }

    public void reBindCamera(CameraV1 camera) {
        mCamera = camera;
        mCamera.setPreviewTexture(mSurfaceTexture);
    }

    public void release() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mCamera.relase();
        mCamera = null;
        mOESTextureId = -1;
        if (engine != null) {
            engine.release();
        }
        if (mHandler != null) {
            mHandler.removeMessages(1);
        }
        QueenParamHolder.relaseQueenParams();
    }
}
