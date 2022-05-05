package im.zego.CustomerVideoCapture.queen.surface;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.TextureView;

import com.aliyun.android.libqueen.QueenUtil;

public class GLESTextureView extends TextureView {
    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;
    private GLESThread mGLThread;
    private SampleRender mRenderer;

    private int mRendererMode = RENDERMODE_WHEN_DIRTY;

    private TextureView.SurfaceTextureListener mSurfaceObserver;

    private SurfaceTextureListener mTextureListenerInner;

    private RenderListener mRenderListener;

    private SampleRender.IRenderCallback mRenderCallbackOut;

    private SurfaceTexture mOESPreviewTexture = null;
    private int mOESTextureId = -1;

    public GLESTextureView(Context context) {
        super(context);
        init();
    }

    public GLESTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mRenderer = new SampleRender();
        mRenderListener = new RenderListener();
        mRenderer.setRenderCallback(mRenderListener);

        mTextureListenerInner = new SurfaceTextureListenerInner();
        setSurfaceTextureListener(mTextureListenerInner);
    }

    private float[] mOesTransformMatrix = new float[16];

    public SurfaceTexture getPreviewTexture() {
        return mOESPreviewTexture;
    }

    public int getPreviewOESTextureId() {
        return mOESTextureId;
    }

    public float[] getPreviewTransformMatrix() {
        return mOesTransformMatrix;
    }

    public void setSurfaceObserver(SurfaceTextureListener listener) {
        mSurfaceObserver = listener;
    }


    public void setRenderCallback(SampleRender.IRenderCallback callback) {
        mRenderCallbackOut = callback;
    }

    public void configCameraPreview(android.hardware.Camera camera) {
        mRenderer.configCameraPreview(camera);
    }

    /**
     * 渲染模式是循环刷新，或者按需刷新
     */
    public void setRenderMode(int mode) {
        mRendererMode = mode;
    }

    /**
     * Request that the renderer render a frame. This method is typically used when the render mode has been set to {@link #RENDERMODE_WHEN_DIRTY}, so
     * that frames are only rendered on demand. May be called from any thread. Must not be called before a renderer has been set.
     */
    public void requestRender() {
        if (mRendererMode != RENDERMODE_WHEN_DIRTY) {
            return;
        }
        mGLThread.requestRender();
    }

    public void onResume() {
        if (mGLThread != null) {
            mGLThread.onResume();
        }
    }

    public void onPause() {
        if (mGLThread != null) {
            mGLThread.onPause();
        }
    }

    public void onDestroy() {
        mOESPreviewTexture = null;
        if (mGLThread != null) {
            mGLThread.onDestroy();
        }
    }

    private final class SurfaceTextureListenerInner implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mGLThread = new GLESThread(surface, mRenderer);
            mGLThread.setRenderMode(mRendererMode);
            mGLThread.start();
            mGLThread.onSurfaceChanged(width, height);

            mRenderer.onSurfaceAvailableSize(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if (mGLThread != null) {
                mGLThread.onDestroy();
            }
            mSurfaceObserver.onSurfaceTextureDestroyed(surface);
            return true;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mGLThread.onSurfaceChanged(width, height);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    private final class RenderListener implements SampleRender.IRenderCallback {

        @Override
        public void onSurfaceCreatedGL() {
            mOESTextureId = QueenUtil.createTexture2D(true);
            mOESPreviewTexture = new SurfaceTexture(mOESTextureId);
            mOESPreviewTexture.setOnFrameAvailableListener(new SurfaceTexture.OnFrameAvailableListener() {
                @Override
                public void onFrameAvailable(SurfaceTexture surfaceTexture) {
                    // 需要通知渲染线程，去更新画面数据
                    requestRender();
                }
            });

            mRenderCallbackOut.onSurfaceCreatedGL();
        }

        @Override
        public int onDrawFramGL(byte[] data, float[] matrix) {
            // 更新显示纹理，可以继续更新下一帧
            mOESPreviewTexture.updateTexImage();
            mOESPreviewTexture.getTransformMatrix(mOesTransformMatrix);
            for (int i = 0; i<matrix.length; ++i) {
                matrix[i] = mOesTransformMatrix[i];
            }

            if (data == null) {
                mRenderer.updateRenderOesTexture(true);
                return mOESTextureId;
            }

            // 回调到外面做处理
            int textureId = mRenderCallbackOut.onDrawFramGL(data, mOesTransformMatrix);
            mRenderer.updateRenderOesTexture(textureId == mOESTextureId);
            return textureId;
        }

        @Override
        public void onSurfaceChangedGL(int width, int height) {
            mRenderCallbackOut.onSurfaceChangedGL(width, height);
        }

        @Override
        public void onSurfaceDestroyGL() {
            mRenderCallbackOut.onSurfaceDestroyGL();
        }
    }

}
