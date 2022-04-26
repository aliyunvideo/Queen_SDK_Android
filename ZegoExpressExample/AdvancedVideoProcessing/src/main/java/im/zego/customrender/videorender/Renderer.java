package im.zego.customrender.videorender;

import android.graphics.SurfaceTexture;
import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;

import java.nio.ByteBuffer;

import im.zego.customrender.ve_gl.GlRectDrawer;
import im.zego.customrender.ve_gl.GlShader;
import im.zego.customrender.ve_gl.GlUtil;

/**
 * Renderer
 * Render class
 * Show how to render RGB and YUV video.
 */






public class Renderer implements TextureView.SurfaceTextureListener {

    private static final String TAG = "RendererView";

    public static final Object lock = new Object();

    private EGLContext eglContext;
    private EGLConfig eglConfig;
    private EGLDisplay eglDisplay;
    private EGLSurface eglSurface = EGL14.EGL_NO_SURFACE;
    private Surface mTempSurface;
    private int viewWidth = 0;
    private int viewHeight = 0;
    private GlShader shader;
    private int textureId = 0;

    String streamID;
    private TextureView textureView;

    // Drawer for processing video data in yuv format
    private GlRectDrawer drawer = null;

    // Drawer for processing video data in RGB format
    private GlRectDrawer rgbDrawer = null;


    // Texture transformation matrix, the image will be displayed upright
    private float[] flipMatrix = new float[]{1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, -1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f};

    // set the streamID of the stream to render
    public void setStreamID(String streamID) {
        this.streamID = streamID;
    }


    /** initialize Renderer
     *
     * @param eglContext Sharing Context of OpenGL
     * @param eglDisplay Common data type associated with display
     * @param eglConfig  Drawing config
     */
    public Renderer(EGLContext eglContext, EGLDisplay eglDisplay, EGLConfig eglConfig) {
        this.eglContext = eglContext;
        this.eglDisplay = eglDisplay;
        this.eglConfig = eglConfig;
    }

    // bind eglContext、eglDisplay、eglSurface
    private void makeCurrent() {
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't make current");
        }
        synchronized (lock) {
            if (!EGL14.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                throw new RuntimeException(
                        "eglMakeCurrent failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            }
        }
    }


    // draw the buffer on current view
    public void draw(VideoRenderHandler.PixelBuffer pixelBuffer) {

        if (textureView != null) {
            attachTextureView();
        } else {
            Log.e(TAG, "draw error view is null");
            return;
        }

        if (pixelBuffer == null || eglSurface == EGL14.EGL_NO_SURFACE) {
            return;
        }

        // Draw the buffer in YUV
        if (pixelBuffer.strides[2] > 0) {

            if (drawer == null) {
                // create a drawer for yuv
                drawer = new GlRectDrawer();
            }

            // bind eglContext、eglDisplay、eglSurface
            makeCurrent();

            // generate and upload the texture
            yuvTextures = uploadYuvData(pixelBuffer.width, pixelBuffer.height, pixelBuffer.strides, pixelBuffer.buffer);

            int[] value = measure(pixelBuffer.width, pixelBuffer.height, viewWidth, viewHeight);
            // render the YUV picture
            drawer.drawYuv(yuvTextures, flipMatrix, pixelBuffer.width, pixelBuffer.height, value[0], value[1], value[2], value[3]);
            // swap rendered buffer to display
            swapBuffers();
            // detach current eglContext
            detachCurrent();
        } else {

            // draw RGB buffer

            // bind eglContext、eglDisplay、eglSurface
            makeCurrent();

            if (textureId == 0) {
                // generate texture
                textureId = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);
            }

            // Select the current texture unit that can be modified by the texture function
            GLES20.glActiveTexture(GLES20.GL_TEXTURE4);

            // Bind texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

            if (rgbDrawer == null) {
                // create drawer for RGB
                rgbDrawer = new GlRectDrawer();
            }

            // Generate 2D textures
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, pixelBuffer.width, pixelBuffer.height, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, pixelBuffer.buffer[0]);
            // Get the plane coordinates and width and height of the displayed image
            int[] value = measure(pixelBuffer.width, pixelBuffer.height, viewWidth, viewHeight);
            // Render RGB image
            rgbDrawer.drawRgb(textureId, flipMatrix, pixelBuffer.width, pixelBuffer.height, value[0], value[1], value[2], value[3]);
            // swap rendered buffer to display
            swapBuffers();
            // detach current eglContext
            detachCurrent();
        }
    }

//    Calculate the actual image width and height and plane coordinates after zooming according to the image data and the width and height of the display view
    private int[] measure(int imageWidth, int imageHeight, int viewWidth, int viewHeight) {
        int[] value = {0, 0, viewWidth, viewHeight};
        float scale;
        scale = (float) viewWidth / (float)imageWidth;
        float height = imageHeight * scale;
        value[1] = (int) (viewHeight - height) / 2;
        value[3] = (int) height;
        return value;
    }

    // set render view
    public int setRendererView(TextureView view) {
        if (view != null && view == textureView) {
            return 0;
        }
        final TextureView temp = view;

        if (textureView != null) {
            if (eglSurface != EGL14.EGL_NO_SURFACE) {
                uninitEGLSurface();
            }
            textureView.setSurfaceTextureListener(null);
            textureView = null;
            if (shader != null) {
                shader.release();
            }
        }

        textureView = temp;
        if (textureView != null) {
            textureView.setSurfaceTextureListener(this);
        }

        return 0;
    }

    // attach the TextureView to EGL Surface
    private void attachTextureView() {

        // Determine whether to cut into the background and then into the foreground, if necessary, re-create the Surface
        if (isTextureAvailable){
            releaseSurface();
            isTextureAvailable = false;
        }

        if (eglSurface != EGL14.EGL_NO_SURFACE
                && eglContext != EGL14.EGL_NO_CONTEXT
                && eglDisplay != EGL14.EGL_NO_DISPLAY)
        {
            return;
        }

        if (!textureView.isAvailable()) {
            return;
        }

        mTempSurface = new Surface(textureView.getSurfaceTexture());
        viewWidth = textureView.getWidth();
        viewHeight = textureView.getHeight();
        try {
            initEGLSurface(mTempSurface);
        } catch (Exception e) {
            viewWidth = 0;
            viewWidth = 0;
        }
    }

    // create EGL Surface
    private void initEGLSurface(Surface surface) {
        try {
            // Both these statements have been observed to fail on rare occasions, see BUG=webrtc:5682.
            createSurface(surface);
            // bind eglContext、eglDisplay、eglSurface
            makeCurrent();
        } catch (RuntimeException e) {
            // Clean up before rethrowing the exception.
            uninitEGLSurface();
            throw e;
        }

        // detach current EGL context
        detachCurrent();
    }

    // detach current EGL context，so that it can be used in another thread.
    private void detachCurrent() {
        synchronized (lock) {
            if (!EGL14.eglMakeCurrent(
                    eglDisplay, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT)) {
                throw new RuntimeException(
                        "eglDetachCurrent failed: 0x" + Integer.toHexString(EGL14.eglGetError()));
            }
        }
    }

    // create EGL Surface
    private void createSurface(Object surface) {
        if (!(surface instanceof Surface) && !(surface instanceof SurfaceTexture)) {
            throw new IllegalStateException("Input must be either a Surface or SurfaceTexture");
        }

        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("Already has an EGLSurface");
        }
        int[] surfaceAttribs = {EGL14.EGL_NONE};

        eglSurface = EGL14.eglCreateWindowSurface(eglDisplay, eglConfig, surface, surfaceAttribs, 0);
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException(
                    "Failed to create window surface: 0x" + Integer.toHexString(EGL14.eglGetError()));
        }
        Log.i(TAG, "createSurface");
    }

    // release EGL Surface
    public void uninitEGLSurface() {
        if (textureId != 0) {
            int[] textures = new int[]{textureId};
            GLES20.glDeleteTextures(1, textures, 0);
            textureId = 0;
        }

        releaseSurface();
        detachCurrent();

        if (mTempSurface != null) {
            mTempSurface.release();
            mTempSurface = null;
        }
    }

    private void releaseSurface() {
        if (eglSurface != EGL14.EGL_NO_SURFACE) {
            //destroy Surface
            EGL14.eglDestroySurface(eglDisplay, eglSurface);
            eglSurface = EGL14.EGL_NO_SURFACE;
        }
    }

    // swap rendered buffer to display
    public void swapBuffers() {
        if (eglSurface == EGL14.EGL_NO_SURFACE) {
            throw new RuntimeException("No EGLSurface - can't swap buffers");
        }
        synchronized (lock) {
            EGL14.eglSwapBuffers(eglDisplay, eglSurface);
        }
    }

    // release Render
    public void uninit() {
        if (textureView != null) {
            textureView.setSurfaceTextureListener(null);
            textureView = null;
        }
        if (shader != null) {
            shader.release();
        }
        if (drawer != null) {
            drawer.release();
        }
        if (rgbDrawer != null) {
            rgbDrawer.release();
        }
        eglContext = null;
        eglDisplay = null;
        eglConfig = null;
        shader = null;
        drawer = null;
        rgbDrawer = null;


    }

    private boolean isTextureAvailable = false;

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        isTextureAvailable = true;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        Log.i(TAG, "onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }


    private ByteBuffer copyBuffer;
    private int[] yuvTextures;
    private ByteBuffer[] packedByteBuffer = new ByteBuffer[3];

    // upload yuv plane
    public int[] uploadYuvData(int width, int height, int[] strides, ByteBuffer[] planes) {
        // save the width of three planes
        final int[] planeWidths = new int[]{width, width / 2, width / 2};
        // Make sure strides are 4 bytes aligned after cropping
        // get the stride of 3 planes
        final int[] destStrides = new int[3];
        for (int i = 0; i < planeWidths.length; i++) {
            if (planeWidths[i] % 4 == 0) {
                destStrides[i] = planeWidths[i];
            } else {
                destStrides[i] = (planeWidths[i] / 4 + 1) * 4;
            }
        }
        // save the height of three planes
        final int[] planeHeights = new int[]{height, height / 2, height / 2};

        // ensure the storage of buffer needed
        int copyCapacityNeeded = 0;
        for (int i = 0; i < 3; ++i) {
            if (strides[i] > planeWidths[i]) {
                copyCapacityNeeded = Math.max(copyCapacityNeeded, planeWidths[i] * planeHeights[i]);
            }
        }
        // allocate storage for buffer
        if (copyCapacityNeeded > 0
                && (copyBuffer == null || copyBuffer.capacity() < copyCapacityNeeded)) {
            copyBuffer = ByteBuffer.allocateDirect(copyCapacityNeeded);
        }
        // generate three textures
        if (yuvTextures == null) {
            yuvTextures = new int[3];
            for (int i = 0; i < 3; i++) {
                yuvTextures[i] = GlUtil.generateTexture(GLES20.GL_TEXTURE_2D);
            }
        }

        // upload three textures
        for (int i = 0; i < 3; ++i) {
            // choose the current texture which can be modified by functions
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + i);
            // bind texture
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yuvTextures[i]);

            // GLES only receives packed data which requires stride = planeWidth.
            if (strides[i] == planeWidths[i]) {
                // This plane is packed data
                packedByteBuffer[i] = planes[i];

            } else {
                copyBuffer.clear();
                planes[i].position(0);
                // Crop the plane, so that it can be regarded as packed data. Cropped data will be saved in copybuffer.
                copyPlane(planes[i], strides[i], copyBuffer, destStrides[i], planeWidths[i], planeHeights[i]);
                packedByteBuffer[i] = copyBuffer;
            }
            packedByteBuffer[i].position(0);

            // generate 2D texture
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, planeWidths[i],
                    planeHeights[i], 0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, packedByteBuffer[i]);
        }
        return yuvTextures;
    }

    // Crop plane according to stride
    public native void copyPlane(ByteBuffer src, int srcStride, ByteBuffer dst, int dstStride, int width, int height);

}
