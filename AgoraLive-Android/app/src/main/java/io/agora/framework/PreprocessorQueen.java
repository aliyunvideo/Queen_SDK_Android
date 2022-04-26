package io.agora.framework;

import android.content.Context;
import android.opengl.GLES20;

import com.aliyun.queen.IQueenRender;
import com.aliyun.queen.QueenRender;
import com.aliyun.queen.param.QueenParamHolder;

import io.agora.capture.video.camera.CameraVideoChannel;
import io.agora.capture.video.camera.VideoCaptureFrame;
import io.agora.framework.modules.channels.VideoChannel;
import io.agora.framework.modules.processors.IPreprocessor;

public class PreprocessorQueen implements IPreprocessor, CameraVideoChannel.OnCameraStateListener {
    public interface OnFirstFrameListener {
        void onFirstFrame();
    }

    public interface OnFuEffectBundleLoadedListener {
        void onFuEffectBundleLoaded();
    }

    public final static int MSG_EFFECT_BUNDLE_COMPLETE = 1;

    private final static String TAG = PreprocessorQueen.class.getSimpleName();
    private final static int ANIMOJI_COUNT = 2;

    public static final float DEFAULT_BLUR_VALUE = 0.7f;
    public static final float DEFAULT_WHITEN_VALUE = 0.3f;
    public static final float DEFAULT_CHEEK_VALUE = 0f;
    public static final float DEFAULT_EYE_VALUE = 0.4f;

    private IQueenRender mQueenRenderer;
    private Context mContext;
    private boolean mEnabled;

    private OnFuEffectBundleLoadedListener mBundleListener;
    private OnFirstFrameListener mFirstFrameListener;

    public PreprocessorQueen(Context context) {
        mContext = context;
    }

    @Override
    public VideoCaptureFrame onPreProcessFrame(VideoCaptureFrame outFrame, VideoChannel.ChannelContext context) {
        if (mQueenRenderer == null || !mEnabled) {
            return outFrame;
        }

        int processTextureId = mQueenRenderer.onTextureProcess(outFrame.textureId, true,
                outFrame.textureTransform, outFrame.format.getWidth(), outFrame.format.getHeight());

        // The texture is transformed to texture2D by beauty module.
        if (outFrame.textureId != processTextureId) {
            // 纹理id发生变化，说明已处理成功，则修改为新纹理id即可
            outFrame.textureId = processTextureId;
            outFrame.format.setTexFormat(GLES20.GL_TEXTURE_2D);
        }
        return outFrame;
    }

    @Override
    public void initPreprocessor() {
        mQueenRenderer = new QueenRender.Builder().build();
        mQueenRenderer.onTextureCreate(mContext);

        // Enable beautification by default
        mEnabled = true;
    }

    @Override
    public void enablePreProcess(boolean enabled) {
        mEnabled = enabled;
    }

    @Override
    public void releasePreprocessor(VideoChannel.ChannelContext context) {
        if (mQueenRenderer != null) {
            mQueenRenderer.onTextureDestroy();
        }
    }

    @Override
    public void setBlurValue(float blur) {
        QueenParamHolder.getQueenParam().basicBeautyRecord.skinBuffingLeverParam = 2;//BeautyFilterMode.kBMSkinBuffing_Strong;
        QueenParamHolder.getQueenParam().basicBeautyRecord.skinBuffingParam = blur;
    }

    @Override
    public void setWhitenValue(float whiten) {
        QueenParamHolder.getQueenParam().basicBeautyRecord.skinWhitingParam = whiten;
    }

    @Override
    public void setCheekValue(float cheek) {
        QueenParamHolder.getQueenParam().faceShapeRecord.cutFaceParam = cheek;
    }

    @Override
    public void setEyeValue(float eye) {
        QueenParamHolder.getQueenParam().faceShapeRecord.bigEyeParam = eye;
    }

    @Override
    public void onFrameFrame() {
        if (mFirstFrameListener != null) {
            mFirstFrameListener.onFirstFrame();
        }
    }

    public void setOnBundleLoadedListener(PreprocessorFaceUnity.OnFuEffectBundleLoadedListener listener) {

    }

    public void setOnFirstFrameListener(PreprocessorFaceUnity.OnFirstFrameListener listener) {

    }

    public boolean FUAuthenticated() {
        return true;
    }

    public void setOnBundleLoadedListener(OnFuEffectBundleLoadedListener listener) {
//        mBundleListener = listener;
    }

    public void setOnFirstFrameListener(OnFirstFrameListener listener) {
//        mFirstFrameListener = listener;
    }
}
