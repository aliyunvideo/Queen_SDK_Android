package com.aliyun.maliang.android.simpleapp.image;

import static android.opengl.GLSurfaceView.RENDERMODE_WHEN_DIRTY;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;

import com.aliyun.android.libqueen.QueenConfig;
import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.Texture2D;
import com.aliyun.android.libqueen.models.AlgType;
import com.aliyun.maliang.android.simpleapp.camera.FrameDrawer;
import com.aliyunsdk.queen.param.QueenParamHolder;
import com.aliyunsdk.queen.param.QueenRuntime;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Surface.Renderer，用于图片模式预览数据的回调
 * 美颜或其他需要对展示画面进行后期处理的，均在本Render中指定回调接口中进行处理.
 * 本render用于实现Queen将处理后的纹理渲染到当前画布，适用于，能直接获取到相机数据，且返回相机OES纹理的场景，
 * 处理后的画面数据，直接渲染到当前纹理中
 */
public class SimpleImageRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "ImageRenderer";
    private Context mContext;
    protected SurfaceTexture mSurfaceTexture;
    private SimpleImageGLSurfaceView mGLSurfaceView;
    private long mLastRequestUptimeMillis = 0l;

    private QueenEngine mQueenEngine;
    protected FrameDrawer mFrameGlTextureDrawer;
    private Texture2D mBitmapInputTexture = null;
    private int mBitmapWidth = 0, mBitmapHeight = 0;
    private Bitmap mBitmapInput = null;
    private boolean mBitmapHadChanged = false;
    private float[] mBitmapVertextFloats = null;        // 图片适配后的顶点坐标

    private int mSurfaceVisibleWidth = 0, mSurfaceVisibleHeight = 0;
    private int mSurfaceDefaultTextureId = 0;

    protected float[] transformMatrix = null;

    public void init(SimpleImageGLSurfaceView glSurfaceView, Context context) {
        mContext = context;

        mGLSurfaceView = glSurfaceView;
        mGLSurfaceView.setRenderer(this);
        mGLSurfaceView.setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    public void updateInputBmp(Bitmap bitmap) {
        mBitmapHadChanged = true;

        mBitmapInput = bitmap;
        mBitmapWidth = mBitmapInput.getWidth();
        mBitmapHeight = mBitmapInput.getHeight();
        // 因为每张图片的尺寸不同，和surfaceview的区域尺寸也不同，若不作处理，图片会被拉伸到surfaceview的尺寸，引起图片变形。
        // 因此需要计算图片尺寸与surfaceview的映射关系，此处采用居中显示处理
         mBitmapVertextFloats = calculateVertices();
    }

    private void updateInputBmpInGLThread() {
        if (mBitmapHadChanged) {
            if (mBitmapInputTexture != null) {
                mBitmapInputTexture.release();
            }
            mBitmapInputTexture = new Texture2D(mQueenEngine.getEngineHandler());
            mBitmapInputTexture.initWithBitmap(mBitmapInput);
        }
    }

    // 计算顶点坐标转换，避免图片被拉伸变形
    private float[] calculateVertices() {
        int surfaceWidth = mSurfaceVisibleWidth;
        int surfaceHeight = mSurfaceVisibleHeight;
        float surfaceRatio = (float) surfaceWidth / surfaceHeight;
        float imageRatio = (float) mBitmapWidth / mBitmapHeight;

        float scale = 1.0f;
        // 计算缩放比例
        if (imageRatio > surfaceRatio) {
            scale = (float) surfaceWidth / mBitmapWidth;
        } else {
            scale = (float) surfaceHeight / mBitmapHeight;
        }

        float newWidth = mBitmapWidth * scale / surfaceWidth;
        float newHeight = mBitmapHeight * scale / surfaceHeight;

        // 生成顶点坐标
        return new float[] {
                -newWidth, -newHeight,
                newWidth, -newHeight,
                -newWidth,  newHeight,
                newWidth,  newHeight
        };
    }

    public void requestRender() {
        mGLSurfaceView.requestRender();
    }

    private void initSurfaceTexture() {
        if (mGLSurfaceView == null) {
            Log.i(TAG, "mGLSurfaceView is null!");
            return;
        }
        mSurfaceDefaultTextureId = createTexture2D(false);
        mSurfaceTexture = new SurfaceTexture(mSurfaceDefaultTextureId);
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
        // 初始化SurfaceTexture
        initSurfaceTexture();

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
                    mSurfaceVisibleWidth = h;
                    mSurfaceVisibleHeight = w;
                } else {
                    onSetViewportSize(offsetW, offsetH, w, h);
                    mSurfaceVisibleWidth = w;
                    mSurfaceVisibleHeight = h;
                }
            }
        }
    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        if (mSurfaceTexture != null) {
//            if (transformMatrix == null) {
//                transformMatrix = new float[16];
//            }
//            mSurfaceTexture.getTransformMatrix(transformMatrix);
//        }

        updateInputBmpInGLThread();

        int updateTextureId = onDrawWithEffectorProcess();
        mFrameGlTextureDrawer.draw(transformMatrix, updateTextureId, mBitmapVertextFloats);
    }

    protected void onCreateEffector(Context context) {
        try {
            QueenConfig config = new QueenConfig();
            config.withContext = true;
            config.withNewGlThread = false;
            if (Build.VERSION.SDK_INT >= 21) {
                config.shareGlContext = EGL14.eglGetCurrentContext().getNativeHandle();
            } else {
                config.shareGlContext = EGL14.eglGetCurrentContext().getHandle();
            }
            mQueenEngine = new QueenEngine(context, config);
        } catch (Exception e) { e.printStackTrace(); }
    }

    protected int onDrawWithEffectorProcess() {
        if (mBitmapInputTexture == null) return mSurfaceDefaultTextureId;

        if (!QueenRuntime.isEnableQueen) {
            return mBitmapInputTexture.getTextureId();
        }

        // 更新美颜特效参数，参数修改，在菜单组件中已完成交互
        QueenParamHolder.writeParamToEngine(mQueenEngine, false);
        // 设置抠图参数进行Y轴翻转，否则抠图mask会翻转过来
        if (mQueenEngine != null) {
            mQueenEngine.setSegmentInfoFlipY(true);
        }

        mQueenEngine.enableDetectPointDebug(AlgType.kFaceDetect, true);

        int in = 0;// TODO：此处需要根据业务自身进行调整，默认图片是正向显示。
        int updateTextureId = processTextureInner(mQueenEngine, mBitmapInputTexture.getTextureId(),
                transformMatrix, mBitmapWidth, mBitmapHeight,
                in, 0, 0);

        return updateTextureId;
    }

    protected void onReleaseEffector() {
        // TODO: 此处需要在gl线程中执行
        // 释放Engine
        if (mQueenEngine != null) {
            mQueenEngine.release();
            mQueenEngine = null;
        }
        if (mBitmapInputTexture != null) {
            mBitmapInputTexture.release();
            mBitmapInputTexture = null;
        }
    }

    protected void onSetViewportSize(int left, int bottom, int width, int height) {
        mQueenEngine.setScreenViewport(left, bottom, width, height);
        mFrameGlTextureDrawer.setViewport(left, bottom, width, height);
    }

    private int processTextureInner(QueenEngine engine, int textureId, float[] matrix, int width, int height, int inputAngle, int outAngle, int flipAxis) {
        int w = width;
        int h = height;
        engine.setInputTexture(textureId, w, h, false);

        // 输出纹理id是否已生成
        int outTextId = mQueenEngine.getAutoGenOutTextureId();
        if (outTextId <= 0) {
            // 是否让Queen保持原纹理方向输出
            boolean keepInputDirection = true;
            engine.autoGenOutTexture(keepInputDirection);
        }

        // 根据当前纹理数据到更新
        engine.updateInputTextureBufferAndRunAlg(inputAngle, outAngle, flipAxis, false);
        int result = matrix != null ? engine.renderTexture(matrix) : engine.render();
        // 处理不成功，则返回原始纹理id
        if (result != 0) {
            return textureId;
        } else {
            return mQueenEngine.getAutoGenOutTextureId();
        }
    }
    /***以上四个protected方法，才是美颜特效sdk需要处理的四个方法-end****/

    public void release() {
        if (mSurfaceTexture != null) {
            mSurfaceTexture.release();
            mSurfaceTexture = null;
        }
        onReleaseEffector();
    }

}
