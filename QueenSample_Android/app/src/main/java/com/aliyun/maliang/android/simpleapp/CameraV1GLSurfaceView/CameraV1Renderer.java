package com.aliyun.maliang.android.simpleapp.CameraV1GLSurfaceView;

import android.content.Context;
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
import com.aliyun.maliang.android.simpleapp.queen.QueenEngineHolder;
import com.aliyun.maliang.android.simpleapp.queen.QueenParamHolder;
import com.aliyun.maliang.android.simpleapp.queen.QueenRuntime;
import com.taobao.android.libqueen.ImageFormat;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.QueenUtil;
import com.taobao.android.libqueen.Texture2D;

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
    private FrameGlDrawer mOESFrameGlDrawer;
    private FrameGlDrawer mTexture2DFrameGlDrawer;

    private long mLastDrawSystemClock = 0L;
    private long mCurrentDrawTimes = 0L;
    private long mLastDrawTimes = 0L;
    private TextView mFpsView = null;

    private boolean mTestOutRect = false;
    // 是否需要Queen将处理后的纹理渲染到当前画布
    private boolean mUseQueenRenderToScreen = true;
    // 是否让Queen保持原纹理方向输出
    private boolean mQueenKeepInputDirection = true;
    // 是否让Queen使用纹理执行算法
    private boolean mUseTextureBuffer = false;

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
        mOESFrameGlDrawer = new FrameGlDrawer(true);
        mTexture2DFrameGlDrawer = new FrameGlDrawer(false);

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
            QueenEngineHolder.createQueenEngine(mContext, mUseQueenRenderToScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }

        engine.setInputTexture(mOESTextureId, mCamera.getCameraOutWidth(),mCamera.getCameraOutHeight(),true);
    }

    private void initWithOutTexture() {
        try {
            engine = QueenEngineHolder.createQueenEngine(mContext, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        engine.setInputTexture(mOESTextureId, mCamera.getCameraOutWidth(),mCamera.getCameraOutHeight(),true);
        mOutTexture = engine.autoGenOutTexture(mQueenKeepInputDirection);
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

        if (!QueenRuntime.isEnableQueen || engine == null) {
            mOESFrameGlDrawer.draw(transformMatrix, mOESTextureId);
            return;
        }

        updateInputDataToQueen();

        QueenParamHolder.doAcitonForDemo(engine);

        GLES20.glClearColor(0, 0, 0, 0);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        int retCode = engine.renderTexture(transformMatrix);

        if (retCode == -9 || retCode == -10) {
            mOESFrameGlDrawer.draw(transformMatrix, mOESTextureId);
        } else if (retCode == 0 && !mUseQueenRenderToScreen) {
            mTexture2DFrameGlDrawer.draw(
                    mQueenKeepInputDirection ? transformMatrix : null,
                    mOutTexture.getTextureId());
        }

        ++mCurrentDrawTimes;
    }


    private void updateInputDataToQueen() {
        if (mUseTextureBuffer) {
            engine.updateInputTextureBufferAndRunAlg(
                    mCamera.inputAngle, mCamera.outAngle,
                    mCamera.flipAxis, false);
        } else {
            mCameraBytes = mCamera.getLastUpdateCameraPixels();
            if (mCameraBytes != null) {
                // 说明没有新的数据,则不必再去更新人脸引擎去刷新,减少运算消耗
                //人脸检测数据
                engine.updateInputDataAndRunAlg(mCameraBytes, ImageFormat.NV21, mCamera.getPrevieWidth(), mCamera.getPrevieHeight(), 0, mCamera.inputAngle, mCamera.outAngle, mCamera.flipAxis);
                mCamera.releaseData(mCameraBytes);
            }
        }
    }

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
                    } else {
                        Log.i("queen_sample_fps", "fps: " + fps);
                    }
                    this.sendMessageDelayed(this.obtainMessage(1),480);
                    break;
                default:
                    break;
            }
        }
    };

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
        releaseGLResource();
        mCamera.relase();
        mCamera = null;
        if (mHandler != null) {
            mHandler.removeMessages(1);
        }
    }


    public void releaseGLResource() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mOESTextureId = -1;
        if (engine != null) {
            QueenEngineHolder.releaseEngine(engine);
            engine = null;
        }
        if (mOutTexture != null) {
            mOutTexture.release();
            mOutTexture = null;
        }
        QueenParamHolder.relaseQueenParams();
    }
}
