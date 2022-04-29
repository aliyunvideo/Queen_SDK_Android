package im.zego.CustomerVideoCapture.queen.surface;

import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;

import im.zego.CustomerVideoCapture.queen.data.SimpleBytesBufPool;

public class SampleRender implements IGLESRender, Camera.PreviewCallback, SurfaceTexture.OnFrameAvailableListener {

    SimpleBytesBufPool mBytesBufPool;
    FrameDrawer mFrameOesDrawer;

    public void onSurfaceAvailableSize(int width, int height) {
        int byteSize = width * height * ImageFormat.getBitsPerPixel(ImageFormat.NV21)/8;
        mBytesBufPool = new SimpleBytesBufPool(3, byteSize);
    }

    public void configCameraPreview(android.hardware.Camera camera) {
        camera.addCallbackBuffer(mBytesBufPool.reusedBuffer());
        camera.setPreviewCallbackWithBuffer(this);
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        mBytesBufPool.updateBuffer(bytes);
        camera.addCallbackBuffer(mBytesBufPool.reusedBuffer());
    }

    @Override
    public void onSurfaceCreated() {
        mFrameOesDrawer = new FrameDrawer(true);

        mRenderCallback.onSurfaceCreatedGL();
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        mRenderCallback.onSurfaceChangedGL(width, height);
    }

    private float[] transformMatrix = new float[16];
    @Override
    public void onDrawFrame() {
        // TODO:
        byte[] lastFrame = mBytesBufPool.getLastBuffer();

        // DO SOMETHING
        int textureId = mRenderCallback.onDrawFramGL(lastFrame, transformMatrix);
        mFrameOesDrawer.draw(transformMatrix, textureId, true);

        if (lastFrame != null) {
            mBytesBufPool.releaseBuffer(lastFrame);
        }
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        mBytesBufPool.clear();
        mRenderCallback.onSurfaceDestroyGL();
    }

    private IRenderCallback mRenderCallback;
    public void setRenderCallback(IRenderCallback callback) {
        mRenderCallback = callback;
    }

    private IRenderNotify mRenderNotify;
    public void setRenderNotify(IRenderNotify notify) {
        mRenderNotify = notify;
    }

    public interface IRenderNotify {
        void onDrawFrameStart(float[] matrix);
    }

    public interface IRenderCallback {
        void onSurfaceCreatedGL();
        int onDrawFramGL(byte[] data, float[] transform);
        void onSurfaceChangedGL(int width, int height);
        void onSurfaceDestroyGL();
    }
}
