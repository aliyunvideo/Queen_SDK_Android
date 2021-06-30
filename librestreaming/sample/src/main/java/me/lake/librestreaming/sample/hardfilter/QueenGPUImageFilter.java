package me.lake.librestreaming.sample.hardfilter;

import android.content.Context;
import android.opengl.GLES20;

import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.Texture2D;
import com.taobao.android.libqueen.exception.InitializationException;
import com.taobao.android.libqueen.models.BeautyFilterType;
import com.taobao.android.libqueen.models.BeautyParams;
import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.FaceShapeType;
import com.taobao.android.libqueen.models.MakeupType;

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
        engine.enableBeautyType(BeautyFilterType.kSkinWhiting, true);//美白开关
        engine.setBeautyParam(BeautyParams.kBPSkinWhitening, 0.8f);  //美白 [0,1]
        engine.setBeautyParam(BeautyParams.kBPSkinRed, 0.8f);  //红润 [0,1]

        engine.enableBeautyType(BeautyFilterType.kSkinBuffing, true);//磨皮开关
        engine.setBeautyParam(BeautyParams.kBPSkinBuffing, 0.8f);  //磨皮 [0,1]
        engine.setBeautyParam(BeautyParams.kBPSkinSharpen, 0.6f);  //锐化 [0,1]

        // 滤镜
        engine.enableBeautyType(BeautyFilterType.kLUT, true);
        //设置滤镜资源路径，基于assets的相对路径，如“/lookups/lookup_1.png”
        engine.setFilter("lookups/lz5.png");
        //滤镜强度
        engine.setBeautyParam(BeautyParams.kBPLUT, 0.8f);

        // 美型开关，其中第二个参数是功能开关，第三个参数为调试开关
        engine.enableBeautyType(BeautyFilterType.kFaceShape, true, false);
        engine.updateFaceShape(FaceShapeType.typeThinFace, 0.88f);
        engine.updateFaceShape(FaceShapeType.typeBigEye, 0.82f);
        engine.updateFaceShape(FaceShapeType.typeNosewing, 0.33f);
        engine.updateFaceShape(FaceShapeType.typeThinNose, 0.88f);
        engine.updateFaceShape(FaceShapeType.typeThinJaw, 0.3f);

        // 第二个参数是开关，第三个参数是调试开关
        engine.enableBeautyType(BeautyFilterType.kMakeup, true, false);
        // 设置美妆素材
        // 第一个参数是美妆类型
        // 第二个参数是素材文件路径,基于assets的相对路径，如"/makeup/蜜桃妆.png"
        // 第三个参数是素材与人脸的融合类型，第四个参数是保留参数
        String[] path = {"makeup/mitao.png"};
        engine.setMakeupImage(MakeupType.kMakeupWhole, path, BlendType.kBlendNormal, 15);
        // 设置美妆素材透明度
        // 第二个参数是透明度，第三个参数是保留参数
        engine.setMakeupAlpha(MakeupType.kMakeupWhole, 0.6f, 0.3f);

        // 贴纸12
        engine.addMaterial("sticker/12");

    }

}
