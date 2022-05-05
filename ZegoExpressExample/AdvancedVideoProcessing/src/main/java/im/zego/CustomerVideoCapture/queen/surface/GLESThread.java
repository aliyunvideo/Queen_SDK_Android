package im.zego.CustomerVideoCapture.queen.surface;

import android.graphics.SurfaceTexture;
import android.opengl.GLUtils;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

import static android.opengl.EGL14.EGL_CONTEXT_CLIENT_VERSION;
import static android.opengl.EGL14.EGL_OPENGL_ES2_BIT;
import static javax.microedition.khronos.egl.EGL10.EGL_NONE;
import static javax.microedition.khronos.egl.EGL10.EGL_PBUFFER_BIT;
import static javax.microedition.khronos.egl.EGL10.EGL_WINDOW_BIT;

public class GLESThread extends Thread {
    private SurfaceTexture mSurfaceTexture;
    private EGL10 mEgl;
    private EGLDisplay mEglDisplay = EGL10.EGL_NO_DISPLAY;// 显示设备
    private EGLSurface mEglSurface = EGL10.EGL_NO_SURFACE;
    private EGLContext mEglContext = EGL10.EGL_NO_CONTEXT;
    private IGLESRender mRenderer;
    private PendingThreadAider mPendingThreadAider = new PendingThreadAider();
    private boolean mNeedRenderring = true;
    private Object LOCK = new Object();
    private boolean mIsPaused = false;

    public GLESThread(SurfaceTexture surface, IGLESRender renderer) {
        mSurfaceTexture = surface;
        mRenderer = renderer;
    }

    @Override
    public void run() {
        initGLESContext();
        mRenderer.onSurfaceCreated();
        while (mNeedRenderring) {
            mPendingThreadAider.runPendings();

            if (!mNeedRenderring)
                break;

            mRenderer.onDrawFrame();
            mEgl.eglSwapBuffers(mEglDisplay, mEglSurface);
            if (mIsPaused) {
                pauseWhile();
            } else if (mRendererMode == GLESTextureView.RENDERMODE_WHEN_DIRTY) {
                pauseWhile();
            }
        }
        destoryGLESContext();
    }

    private void initGLESContext() {
        mEgl = (EGL10) EGLContext.getEGL();
        mEglDisplay = mEgl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);// 获取显示设备
        if (mEglDisplay == EGL10.EGL_NO_DISPLAY) {
            throw new RuntimeException("eglGetdisplay failed : " + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int[] version = new int[2];
        if (!mEgl.eglInitialize(mEglDisplay, version)) {// //version中存放EGL 版本号&#xff0c;int[0]为主版本号&#xff0c;int[1]为子版本号
            throw new RuntimeException("eglInitialize failed : " + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        // 构造需要的特性列表
        int[] configAttribs = { //
            EGL10.EGL_BUFFER_SIZE, 32,//
                    EGL10.EGL_ALPHA_SIZE, 8, // 指定Alpha大小&#xff0c;以下四项实际上指定了像素格式
                    EGL10.EGL_BLUE_SIZE, 8, // 指定B大小
                    EGL10.EGL_GREEN_SIZE, 8,// 指定G大小
                    EGL10.EGL_RED_SIZE, 8,// 指定RGB中的R大小
                    EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,// 指定渲染api类别,这里或者是硬编码的4或者是EGL14.EGL_OPENGL_ES2_BIT
                    EGL10.EGL_SURFACE_TYPE, EGL_WINDOW_BIT | EGL_PBUFFER_BIT, EGL_NONE// 总是以EGL10.EGL_NONE结尾
        };

        int[] numConfigs = new int[1];
        EGLConfig[] configs = new EGLConfig[1];

        if (!mEgl.eglChooseConfig(mEglDisplay, configAttribs, configs, 1, numConfigs)) {// 获取所有满足attributes的configs,并选择一个
            throw new RuntimeException("eglChooseConfig failed : " + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }

        int[] contextAttribs = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE,};
        mEglContext = mEgl.eglCreateContext(mEglDisplay, configs[0], EGL10.EGL_NO_CONTEXT,
                contextAttribs);// 创建context
        mEglSurface = mEgl.eglCreateWindowSurface(mEglDisplay, configs[0], mSurfaceTexture,// 负责对Android Surface的管理
                null// Surface属性
        );
        if (mEglSurface == EGL10.EGL_NO_SURFACE || mEglContext == EGL10.EGL_NO_CONTEXT) {
            int error = mEgl.eglGetError();
            String exception = GLUtils.getEGLErrorString(error);
            if (error == EGL10.EGL_BAD_NATIVE_WINDOW) {
                throw new RuntimeException("eglCreateWindowSurface returned  EGL_BAD_NATIVE_WINDOW. ");
            }
            exception = GLUtils.getEGLErrorString(mEgl.eglGetError());
            throw new RuntimeException("eglCreateWindowSurface failed : " + exception);
        }

        if (!mEgl.eglMakeCurrent(mEglDisplay, mEglSurface, mEglSurface, mEglContext)) {// 设置为当前的渲染环境
            throw new RuntimeException("eglMakeCurrent failed : " + GLUtils.getEGLErrorString(mEgl.eglGetError()));
        }
    }

    private boolean mDirty = false;
    private void pauseWhile() {
        synchronized (LOCK) {
            if (!mDirty) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                mDirty = false;
            }
        }
    }

    private void destoryGLESContext() {
        if (mEgl != null) {
            mEgl.eglDestroyContext(mEglDisplay, mEglContext);
            mEgl.eglDestroySurface(mEglDisplay, mEglSurface);
            mEgl = null;
        }
        mEglContext = EGL10.EGL_NO_CONTEXT;
        mEglSurface = EGL10.EGL_NO_SURFACE;
    }

    public void onPause() {
        mRenderer.onPause();
        mIsPaused = true;
    }

    public void onResume() {
        mRenderer.onResume();
        mIsPaused = false;
        requestRender();
    }

    public void onSurfaceChanged(final int width, final int height) {
        mPendingThreadAider.addToPending(new Runnable() {
            @Override
            public void run() {
                mRenderer.onSurfaceChanged(width, height);
            }
        });
    }

    private int mRendererMode = GLESTextureView.RENDERMODE_CONTINUOUSLY;

    public void setRenderMode(int mode) {
        mRendererMode = mode;
    }

    public void requestRender() {
        synchronized (LOCK) {
            mDirty = true;
            LOCK.notifyAll();
        }
    }

    public void onDestroy() {
        mPendingThreadAider.addToPending(new Runnable() {
            @Override
            public void run() {
                mRenderer.onDestroy();
                destoryGLESContext();
                mNeedRenderring = false;
            }
        });
    }
}
