package me.lake.librestreaming.sample.hardfilter;

import android.content.Context;
import android.opengl.GLES20;

import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.Texture2D;
import com.aliyun.android.libqueen.exception.InitializationException;
import com.aliyun.android.libqueen.models.BeautyFilterType;
import com.aliyun.android.libqueen.models.BeautyParams;
import com.aliyun.android.libqueen.models.BlendType;
import com.aliyun.android.libqueen.models.FaceShapeType;
import com.aliyun.android.libqueen.models.MakeupType;

import java.nio.FloatBuffer;

import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import me.lake.librestreaming.sample.StreamRuntime;

/**
 * @Author jiufen.hy
 * @Date 2021/6/30 13:27
 * @content
 */
public class QueenGPUImageFilter extends GPUImageFilter {

    private Context mContext;
    private QueenEngine engine;
    private Texture2D mOutTexture;
    private float[] transformMatrix = StreamRuntime.transformMatrix;

    public QueenGPUImageFilter(Context context) {
        super();
        this.mContext = context;
    }

    @Override
    public void onInitialized() {
        initWithOutTexture();
    }

    /**
     * 使用自定义纹理承载美颜结果纹理
     */
    private void initWithOutTexture() {
        if (engine != null) {
            engine.release();
            mOutTexture = null;
        }
        try {
            engine = new QueenEngine(mContext, false);

            String sign = LicenseHelper.getPackageSignature();
        } catch (InitializationException e) {
            e.printStackTrace();
        }
        engine.enableDebugLog();
    }

    @Override
    public void onDestroy() {
        if (engine != null) {
            engine.release();
            mOutTexture = null;
        }
    }

    @Override
    public void onDraw(final int textureId, final FloatBuffer cubeBuffer,
                       final FloatBuffer textureBuffer) {

        int targetTextureId = doDrawBeautyFilter(textureId);
        // 需要重新绑定一次fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, StreamRuntime.sCurBindTargetFrameBuffer);
        doDrawGL(targetTextureId, cubeBuffer, textureBuffer);
    }

    // 绘制美颜相关,若绘制不成功,则返回原始纹理id
    private int doDrawBeautyFilter(int cameraTexture) {
        // 设置输入纹理，用于美颜流程的渲染
        // 第四个参数表示输入纹理是否为OES类型的纹理
        engine.setInputTexture(cameraTexture, mOutputWidth, mOutputHeight, false);
        // 高级美颜功能,需要人脸识别,此处需要跑算法
        engine.updateInputTextureBufferAndRunAlg(StreamRuntime.inputAngle, StreamRuntime.outAngle, StreamRuntime.flipAxis, false);

        if (mOutTexture == null) {
            mOutTexture = engine.autoGenOutTexture();
            engine.updateOutTexture(mOutTexture.getTextureId(), mOutputWidth, mOutputHeight);
        }

        GLES20.glClearColor(0, 0f, 0.8f, 0.6f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        doWriteBeautyParam();
        // 渲染到当前窗口，如证书校验失败或者全部特效功能关闭，则SDK不会执行渲染操作
        int retCode = engine.renderTexture(transformMatrix);

        int targetTextureId = retCode == 0 ? mOutTexture.getTextureId() : cameraTexture;
        return targetTextureId;
    }

    // 绘制到gl
    private void doDrawGL(int targetTextureId, FloatBuffer cubeBuffer, FloatBuffer textureBuffer) {
        GLES20.glUseProgram(mGLProgId);
        runPendingOnDrawTasks();
        if (!isInitialized()) {
            return;
        }
        cubeBuffer.position(0);
        GLES20.glEnableVertexAttribArray(mGLAttribPosition);
        // 设置值
        GLES20.glVertexAttribPointer(mGLAttribPosition, 2, GLES20.GL_FLOAT, false, 0, cubeBuffer);

        // 激活与绑定纹理单元
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, targetTextureId);
        GLES20.glUniform1i(mGLUniformTexture, 0);

        // 设置值
        GLES20.glEnableVertexAttribArray(mGLAttribTextureCoordinate);
        textureBuffer.position(0);
        GLES20.glVertexAttribPointer(mGLAttribTextureCoordinate, 2, GLES20.GL_FLOAT, false, 0,
                textureBuffer);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        GLES20.glDisableVertexAttribArray(mGLAttribPosition);
        GLES20.glDisableVertexAttribArray(mGLAttribTextureCoordinate);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    // 美颜参数相关配置
    private void doWriteBeautyParam() {
        //美白开关
        engine.enableBeautyType(BeautyFilterType.kSkinWhiting, true);
        //美白参数 [0,1]
        engine.setBeautyParam(
                BeautyParams.kBPSkinWhitening,
                0.3f
        );
        //磨皮/锐化 开关
        engine.enableBeautyType(BeautyFilterType.kSkinBuffing, true);
        engine.setBeautyParam(BeautyParams.kBPSkinBuffing, 0.6f);
        engine.setBeautyParam(BeautyParams.kBPSkinSharpen, 0.3f);

        //高级美颜开关
        engine.enableBeautyType(BeautyFilterType.kFaceBuffing, true);
        //去法令纹[0,1]
        engine.setBeautyParam(BeautyParams.kBPNasolabialFolds, 1.0f);
        //去眼袋[0,1]
        engine.setBeautyParam(BeautyParams.kBPPouch, 1.0f);
        //白牙[0,1]
        engine.setBeautyParam(BeautyParams.kBPWhiteTeeth, 1.0f);
        //滤镜美妆：口红[0,1]
        engine.setBeautyParam(BeautyParams.kBPLipstick, 1.0f);
        // 滤镜美妆：口红色相[-0.5,0.5]，需配合饱和度、明度使用，参考颜色如下：土红(-0.125)、粉红(-0.1)、复古红(0.0)、紫红(-0.2)、正红(-0.08)、橘红(0.0)、紫色(-0.42)、橘色(0.125)、黄色(0.25)
        engine.setBeautyParam(BeautyParams.kBPLipstickColorParam, -0.125f);
        // 滤镜美妆：口红饱和度[0,1]，需配合色相、明度使用，参考颜色如下：土红(0.25)、粉红(0.125)、复古红(1.0)、紫红(0.35)、正红(1.0)、橘红(0.35)、紫色(0.35)、橘色(0.25)、黄色(0.45)
        engine.setBeautyParam(BeautyParams.kBPLipstickGlossParam, 0.25f);
        // 滤镜美妆：口红明度[0,1]，需配合色相、饱和度使用，参考颜色如下：土红(0.4)、粉红(0.0)、复古红(0.2)、紫红(0.0)、正红(0.0)、橘红(0.0)、紫色(0.0)、橘色(0.0)、黄色(0.0)
        engine.setBeautyParam(BeautyParams.kBPLipstickBrightnessParam, 0.4f);

        //基于assets的相对路径，如"sticker/baiyang"
        engine.addMaterial("sticker/5");
    }

}
