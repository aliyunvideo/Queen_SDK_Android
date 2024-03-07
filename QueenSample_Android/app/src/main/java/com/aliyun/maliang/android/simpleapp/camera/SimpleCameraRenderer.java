package com.aliyun.maliang.android.simpleapp.camera;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.SystemClock;
import android.util.Log;

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
    private long mLastRequestUptimeMillis = 0l;

    protected FrameDrawer mFrameOesDrawer;
    protected FrameDrawer mFrameGlTextureDrawer;

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
                mLastRequestUptimeMillis = SystemClock.uptimeMillis();
            }
        });
        mCamera.setPreviewTexture(mSurfaceTexture);
        mCamera.startPreview();

        mCameraPreviewWidth = mCamera.getPrevieWidth();
        mCameraPreviewHeight = mCamera.getPrevieHeight();

        mCamera.setCameraPreviewCallback(new SimpleCamera.ICameraPreviewCallback() {
            @Override
            public void onPreviewFrameAvailableCallback() {
                long updateCost = SystemClock.uptimeMillis() - mLastRequestUptimeMillis;
                if (updateCost > 30l) {
                    // 特殊机型，极端情况下，辅助提升刷新率
                    mGLSurfaceView.requestRender();
                }
            }
        });
    }

    private static int createTexture2D(boolean isOes) {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);

        int target = GLES20.GL_TEXTURE_2D;
        if (isOes) {
            target = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        }

        GLES20.glBindTexture(target, tex[0]);
        GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(target, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(target, GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(target, GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(target, 0);
        return tex[0];
    }

    // Surface创建回调
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mOESTextureId = createTexture2D(true);

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

        // sdk内部默认会根据机型硬件情况来判断打分，若为低端机则会自动默认为开启节能模式，则相应会影响一定功能效果。
        // 例如，磨皮功能固定为强烈模式，自然模式不会开启不可切换。
        // 若不需要该处理，则此处显示告知不要使用节能模式即可
        // QueenRuntime.sPowerSaving = false;

        int updateTextureId = onDrawWithEffectorProcess();

        if (updateTextureId != mOESTextureId) {
            mFrameGlTextureDrawer.draw(transformMatrix, updateTextureId);
        } else {
            // 原本由Queen内部负责绘制到到屏幕，但若此处返回了原始纹理id，则说明内部处理失败，需将原始纹理绘出
            mFrameOesDrawer.draw(transformMatrix, mOESTextureId);
        }
    }

    /***以下四个protected方法，才是美颜特效sdk需要处理的四个方法****/
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
    /***以上四个protected方法，才是美颜特效sdk需要处理的四个方法-end****/

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
