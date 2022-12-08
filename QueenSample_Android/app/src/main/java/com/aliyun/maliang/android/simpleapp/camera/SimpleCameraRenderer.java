package com.aliyun.maliang.android.simpleapp.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.aliyun.android.libqueen.QueenUtil;
import com.aliyun.maliang.android.simpleapp.utils.FpsHelper;
import com.aliyunsdk.queen.param.QueenRuntime;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

/**
 * Surface.Renderer，用于相机预览数据的回调
 * 美颜或其他需要对展示画面进行后期处理的，均在本Render中指定回调接口中进行处理.
 * 本render用于实现Queen将处理后的纹理渲染到当前画布，适用于，能直接获取到相机数据，且返回相机OES纹理的场景，
 * 处理后的画面数据，直接渲染到当前纹理中
 */
public class SimpleCameraRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "CameraRenderer";
    private Context mContext;
    protected SurfaceTexture mSurfaceTexture;
    private SimpleCameraGLSurfaceView mGLSurfaceView;
    protected SimpleCamera mCamera;

    private FrameDrawer mFrameOesDrawer;
    private FrameDrawer mFrameGlTextureDrawer;

    protected int mOESTextureId = -1;
    protected float[] transformMatrix = null;

    protected int mCameraPreviewWidth = 0;
    protected int mCameraPreviewHeight = 0;

    public void init(SimpleCameraGLSurfaceView glSurfaceView, SimpleCamera camera, Context context) {
        mContext = context;
        mCamera = camera;

        mGLSurfaceView = glSurfaceView;
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
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

        mCameraPreviewWidth = mCamera.getPrevieWidth();
        mCameraPreviewHeight = mCamera.getPrevieHeight();
    }

    // Surface创建回调
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = QueenUtil.createTexture2D(true);

        // 初始化SurfaceTexture
        initSurfaceTexture();

        mFrameOesDrawer = new FrameDrawer(true);
        mFrameGlTextureDrawer = new FrameDrawer(false);

        onCreateEffector(mContext);
    }

    // Surface画面大小方向发生改变时的回调，需要同步进行QueenEngine的调整处理.
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

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

                // 非必要，只有显示画面，与纹理传入的size不一致时，才需要设置ViewportSize，
                // 否则，默认使用输入纹理的size
                if (isLandscape) {
                    onSetViewportSize(offsetH, offsetW, h, w);
                } else {
                    onSetViewportSize(offsetW, offsetH, w, h);
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

        // 关闭美颜特效处理，便于对比原始画面与开启美颜特效画面
        if (!QueenRuntime.isEnableQueen) {
            mFrameOesDrawer.draw(transformMatrix, mOESTextureId);
            return;
        }

        int updateTextureId = onDrawWithEffectorProcess();

        if (updateTextureId != mOESTextureId) {
            mFrameGlTextureDrawer.draw(transformMatrix, updateTextureId);
        } else {
            // 原本由Queen内部负责绘制到到屏幕，但若此处返回了原始纹理id，则说明内部处理失败，需将原始纹理绘出
            mFrameOesDrawer.draw(transformMatrix, mOESTextureId);
        }
    }

    protected void onCreateEffector(Context context) {
    }

    protected void onSetViewportSize(int left, int bottom, int width, int height) {
        mFrameOesDrawer.setViewport(left, bottom, width, height);
        mFrameGlTextureDrawer.setViewport(left, bottom, width, height);
    }

    protected int onDrawWithEffectorProcess() {
        return mOESTextureId;
    }

    protected void onReleaseEffector() {

    }


    public void reBindCamera(SimpleCamera camera) {
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

        onReleaseEffector();
    }
}
