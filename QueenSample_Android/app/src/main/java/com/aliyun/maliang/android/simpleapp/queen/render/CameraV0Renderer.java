//package com.aliyun.maliang.android.simpleapp.queen.render;
//
//import android.content.Context;
//import android.graphics.SurfaceTexture;
//import android.opengl.GLES20;
//import android.opengl.GLSurfaceView;
//import android.util.Log;
//
//import com.aliyun.maliang.android.simpleapp.CameraV1;
//import com.aliyun.maliang.android.simpleapp.FpsHelper;
//import com.aliyun.maliang.android.simpleapp.SurfaceView.CameraGLSurfaceView;
//import com.aliyun.maliang.android.simpleapp.SurfaceView.FrameOesGlDrawer;
//import com.aliyun.maliang.android.simpleapp.SurfaceView.FrameTexture2DGlDrawer;
//import com.aliyun.maliang.android.simpleapp.queen.params.QueenParamHolder;
//import com.aliyun.maliang.android.simpleapp.queen.params.QueenRuntime;
//import com.taobao.android.libqueen.ImageFormat;
//import com.taobao.android.libqueen.QueenEngine;
//import com.taobao.android.libqueen.QueenUtil;
//import com.taobao.android.libqueen.Texture2D;
//
//import javax.microedition.khronos.egl.EGLConfig;
//import javax.microedition.khronos.opengles.GL10;
//
//
//public class CameraV0Renderer implements GLSurfaceView.Renderer {
//
//    private static final String TAG = "CameraV2Renderer";
//    private Context mContext;
//    private int mOESTextureId = -1;
//    private Texture2D mOutTexture;
//    private SurfaceTexture mSurfaceTexture;
//    private float[] transformMatrix = new float[16];
//    private CameraGLSurfaceView mGLSurfaceView;
//    private CameraV1 mCamera;
//    private  byte[] mCameraBytes;
//
//    QueenEngine engine;
//    private FrameOesGlDrawer mOESFrameGlDrawer;
//    private FrameTexture2DGlDrawer mTexture2DFrameGlDrawer;
//
//    private boolean mTestOutRect = false;
//    // 是否需要Queen将处理后的纹理渲染到当前画布
//    private boolean mUseQueenRenderToScreen = true;
//    // 是否让Queen保持原纹理方向输出
//    private boolean mQueenKeepInputDirection = true;
//    // 是否让Queen使用纹理执行算法
//    private boolean mUseTextureBuffer = false;
//
//    public void init(CameraGLSurfaceView glSurfaceView, CameraV1 camera, Context context) {
//        mContext = context;
//        mGLSurfaceView = glSurfaceView;
//        mCamera = camera;
//        mCameraBytes = null;
//    }
//
//    @Override
//    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
//        mOESTextureId = QueenUtil.createTexture2D(true);
//        initSurfaceTexture();
//
//        // 使用自定义纹理承载美颜结果纹理
//        initWithOutTexture();
//        mOESFrameGlDrawer = new FrameOesGlDrawer();
//        mTexture2DFrameGlDrawer = new FrameTexture2DGlDrawer();
//
//        if(mTestOutRect) {
//            int len = mCamera.getPrevieHeight();
//            int offset = (mCamera.getPrevieWidth() - mCamera.getPrevieHeight()) / 2;
//            if(mCamera.isLandscape()) {
//                engine.setOutputRect(offset, 0, len, len);
//            } else {
//                engine.setOutputRect(0, offset, len, len);
//            }
//        }
//    }
//
//    private void initWithOutTexture() {
//        try {
//            engine = new QueenEngine(mContext, true);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        engine.setInputTexture(mOESTextureId, mCamera.getCameraOutWidth(),mCamera.getCameraOutHeight(),true);
//        mOutTexture = engine.autoGenOutTexture(mQueenKeepInputDirection);
//    }
//
//    @Override
//    public void onSurfaceChanged(GL10 gl, int width, int height) {
//        float cameraRatio = 1280 / 720.0f;
//        final boolean isLandscape = width > height;
//        if (isLandscape) {
//            int tmp = width;    //强缺转成portrait计算
//            width = height;
//            height = tmp;
//        }
//
//        if (width > 0 && engine != null) {
//            if (mTestOutRect) { //设置了outRect，保证OutRect完整展示. 而此时画面width是：
//                int bottom = (height - width ) / 2;
//                if (isLandscape) {
//                    engine.setScreenViewport(bottom, 0, width, width);
//                } else {
//                    engine.setScreenViewport(0, bottom, width, width );
//                }
//            } else { //camera会旋转，所以camera与screen的长短边是对应的
//
//                float screenRatio =  height / (float) width;
//
//                int w = width, h = height, offsetW = 0, offsetH = 0;
//
//                if (screenRatio >= cameraRatio) {   //屏幕更窄长，裁掉carema一部分短边内容使fit(长边fit, 短边 = l * 720 / 1280, offset = -(s - ScreenS) / 2
//                    w = (int) (height / cameraRatio);
//                    offsetW = (width - w) / 2;
//                } else {    //裁掉camera一部分长边内容，使用其更粗短fit
//                    h = (int)(width * cameraRatio);
//                    offsetH = (height - h) / 2;
//                }
//
//                if (isLandscape) {
//                    engine.setScreenViewport(offsetH, offsetW, h, w);
//                } else {
//                    engine.setScreenViewport(offsetW, offsetH, w, h);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onDrawFrame(GL10 gl) {
//        FpsHelper.get().updateDrawTimes();
//
//        if (mSurfaceTexture != null) {
//            mSurfaceTexture.updateTexImage();
//            mSurfaceTexture.getTransformMatrix(transformMatrix);
//        }
//
//        if (!QueenRuntime.isEnableQueen || engine == null) {
//            mOESFrameGlDrawer.draw(transformMatrix, mOESTextureId);
//            return;
//        }
//
//        updateInputDataToQueen();
//
//        QueenParamHolder.writeParamToQueenEngine(engine);
//
//        GLES20.glClearColor(0, 0, 0, 0);
//        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
//        int retCode = engine.renderTexture(transformMatrix);
//
//        if (retCode == -9 || retCode == -10) {
//            mOESFrameGlDrawer.draw(transformMatrix, mOESTextureId);
//        } else if (retCode == 0 && !mUseQueenRenderToScreen) {
//            mTexture2DFrameGlDrawer.draw(
//                    mQueenKeepInputDirection ? transformMatrix : null,
//                    mOutTexture.getTextureId());
//        }
//    }
//
//    private void updateInputDataToQueen() {
//        if (mUseTextureBuffer) {
//            engine.updateInputTextureBufferAndRunAlg(
//                    mCamera.inputAngle, mCamera.outAngle,
//                    mCamera.flipAxis, false);
//        } else {
//            mCameraBytes = mCamera.getLastUpdateCameraPixels();
//            if (mCameraBytes != null) {
//                // 说明没有新的数据,则不必再去更新人脸引擎去刷新,减少运算消耗
//                //人脸检测数据
//                engine.updateInputDataAndRunAlg(mCameraBytes, ImageFormat.NV21, mCamera.getPrevieWidth(), mCamera.getPrevieHeight(), 0, mCamera.inputAngle, mCamera.outAngle, mCamera.flipAxis);
//                mCamera.releaseData(mCameraBytes);
//            }
//        }
//    }
//
//    public boolean initSurfaceTexture() {
//        if (mCamera == null || mGLSurfaceView == null) {
//            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
//            return false;
//        }
//        mSurfaceTexture = new SurfaceTexture(mOESTextureId);
//        mSurfaceTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
//            @Override
//            public void onFrameAvailable(SurfaceTexture surfaceTexture) {
//                mGLSurfaceView.requestRender();
//            }
//        });
//        mCamera.setPreviewTexture(mSurfaceTexture);
//        mCamera.startPreview();
//        return true;
//    }
//
//    public void reBindCamera(CameraV1 camera) {
//        mCamera = camera;
//        mCamera.setPreviewTexture(mSurfaceTexture);
//    }
//
//    public void release() {
//        releaseGLResource();
//        mCamera.relase();
//        mCamera = null;
//    }
//
//    public void releaseGLResource() {
//        if (mSurfaceTexture != null) {
//            mSurfaceTexture.release();
//            mSurfaceTexture = null;
//        }
//        mOESTextureId = -1;
//        if (engine != null) {
//            engine.release();
//            engine = null;
//        }
//        if (mOutTexture != null) {
//            mOutTexture.release();
//            mOutTexture = null;
//        }
//        QueenParamHolder.relaseQueenParams();
//    }
//}
