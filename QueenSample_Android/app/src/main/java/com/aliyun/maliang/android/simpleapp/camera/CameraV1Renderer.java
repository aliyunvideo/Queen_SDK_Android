package com.aliyun.maliang.android.simpleapp.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.aliyun.android.libqueen.ImageFormat;
import com.aliyun.android.libqueen.QueenUtil;
import com.aliyun.maliang.android.simpleapp.utils.FpsHelper;
import com.aliyunsdk.queen.param.QueenRuntime;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Surface.Renderer，用于相机预览数据的回调
 * 美颜或其他需要对展示画面进行后期处理的，均在本Render中指定回调接口中进行处理.
 * 本render用于实现Queen将处理后的纹理渲染到当前画布，适用于，能直接获取到相机数据，且返回相机OES纹理的场景，
 * 处理后的画面数据，直接渲染到当前纹理中
 */
public class CameraV1Renderer implements GLSurfaceView.Renderer {

    private static final String TAG = "CameraRenderer";
    private Context mContext;
    protected SurfaceTexture mSurfaceTexture;
    private CameraV1GLSurfaceView mGLSurfaceView;
    private FrameDrawer mFrameOesDrawer;
    private FrameDrawer mFrameGlTextureDrawer;
    protected CameraV1 mCamera;
    protected int mOESTextureId = -1;
    protected float[] transformMatrix = null;
    private byte[] mCameraBytes;

    // 是否需要将Queen的结果直接渲染绘制出来，一般都是不需要的，业务交给Queen处理完后，Queen返回处理完后的纹理id给到业务层，业务自行决定是否绘制或交给其他业务处理，例如RTC、直播sdk等。。
    // 极简单的业务情况下，才由Queen来直接将渲染后就直接渲染上屏，但这里还是展示两种例子。
    private boolean mIsRenderDirect2Draw = false;
    private CameraTextureObserver mTextureObserver;

    public void init(CameraV1GLSurfaceView glSurfaceView, CameraV1 camera, Context context) {
        mContext = context;
        mGLSurfaceView = glSurfaceView;
        mCamera = camera;
    }

    // Surface创建回调
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = QueenUtil.createTexture2D(true);

        // 初始化SurfaceTexture
        initSurfaceTexture();

        mFrameOesDrawer = new FrameDrawer(true);
        mFrameGlTextureDrawer = new FrameDrawer(false);

        mTextureObserver = new CameraTextureObserver(mIsRenderDirect2Draw);
        mTextureObserver.onTextureCreated(mContext);
    }

    // Surface画面大小方向发生改变时的回调，需要同步进行QueenEngine的调整处理.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        if (mTextureObserver == null) {
            return;
        }

        float cameraRatio = 1280 / 720.0f;
        final boolean isLandscape = width > height;
        if (isLandscape) {
            int tmp = width;    //强缺转成portrait计算
            width = height;
            height = tmp;
        }

        if (width > 0) {
            { //camera会旋转，所以camera与screen的长短边是对应的
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
                    mTextureObserver.onTextureChanged(offsetH, offsetW, h, w);
                } else {
                    mTextureObserver.onTextureChanged(offsetW, offsetH, w, h);
                }
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        FpsHelper.get().updateDrawTimes();

        if (mSurfaceTexture != null) {
            mSurfaceTexture.updateTexImage();
            if (transformMatrix == null) {
                transformMatrix = new float[16];
            }
            mSurfaceTexture.getTransformMatrix(transformMatrix);
        }

        if (!QueenRuntime.isEnableQueen) {
            mFrameOesDrawer.draw(transformMatrix, mOESTextureId);
            return;
        }

        if (mTextureObserver != null) {
            mCameraBytes = mCamera.getLastUpdateCameraPixels();
            if (mCameraBytes != null) {
                int updateTextureId = mTextureObserver.onTextureUpdated(mOESTextureId, true,
                        transformMatrix, mCameraBytes, ImageFormat.NV21,
                        mCamera.getPrevieWidth(), mCamera.getPrevieHeight());
                if (!mIsRenderDirect2Draw) {
                    mFrameGlTextureDrawer.draw(transformMatrix, updateTextureId);
                } else if (updateTextureId == mOESTextureId) {
                    // 原本由Queen内部负责绘制到到屏幕，但若此处返回了原始纹理id，则说明内部处理失败，需将原始纹理绘出
                    mFrameOesDrawer.draw(transformMatrix, mOESTextureId);
                }
                mCamera.releaseData(mCameraBytes);
            }
        }
    }

    private void initSurfaceTexture() {
        if (mCamera == null || mGLSurfaceView == null) {
            Log.i(TAG, "mCamera or mGLSurfaceView is null!");
            return;
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
    }

    public void reBindCamera(CameraV1 camera) {
        mCamera = camera;
        mCamera.setPreviewTexture(mSurfaceTexture);
    }

    public void release() {
        releaseGLResource();
        mCamera.relase();
        mCamera = null;
    }

    public void releaseGLResource() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        mOESTextureId = -1;

        if (mTextureObserver != null) {
            mTextureObserver.onTextureDestroy();
        }
    }
}
