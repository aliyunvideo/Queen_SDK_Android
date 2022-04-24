package com.aliyun.queen;

import android.content.Context;

import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.Texture2D;
import com.aliyun.queen.IQueenRender;
import com.aliyun.queen.QueenRender;
import com.aliyun.queen.utils.QueenCameraHelper;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 本类总结了使用Queen-engine过程中，分别需要设置的几个步骤环节，大致分为四个步骤.
 * 客户应用不必按此严格分开不同函数调用，可合并在一起，如step3可在同一个回调接口中完成，但必须要确保有这几个步骤。
 * 使用步骤：
 * 1.准备Queen-engine;
 * 2.设置screenViewPort;
 * 3.设置美颜参数、传入画面数据、开始绘制;
 * 4.退出销毁Queen-engine
 *
 * 其中第3步骤中，如果只是基础美颜，不需要高级美颜功能，step3Draw2UpdateBufferToQueenEngine_IF_NEED可以不必调用
 *
 */
/**
 * 模拟调用Queen时，并非直接将相机预览OES数据直接进行回调，而是经过处理后，改用Texture2D纹理进行调用。
 * 美颜或其他需要对展示画面进行后期处理的，均在本Render中指定回调接口中进行处理.
 * 本render用于实现Queen将处理后的纹理渲染到当前画布，适用于，能直接获取到相机数据，且返回相机OES纹理的场景，
 * 处理后的画面数据，直接渲染到当前纹理中
 */
public class QueenRendererImpl implements IQueenRender {

    QueenRender.Builder mBuilder;
    private Texture2D mOutTexture;
    QueenEngine engine;

    public QueenRendererImpl(QueenRender.Builder builder) {
        mBuilder = builder;
    }

    @Override
    public void onTextureCreate(Context context) {
        try {
            // 注意，此处是将纹理直接显示上屏，也就是说由QueenEngine进行绘制显示出来，Queen直接render之后即可看到最后效果
            engine = new QueenEngine(context, mBuilder.isDraw2Screen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onTextureSizeChanged(int left, int bottom, int width, int height) {
        engine.setScreenViewport(left, bottom, width, height);
    }

    @Override
    public int onTextureProcess(int textureId, float[] matrix, int width, int height) {
        return onTextureProcess(textureId, false, matrix, width, height);
    }

    @Override
    public int onTextureProcess(int textureId, boolean isOesTexture, float[] matrix, int width, int height) {
        int w = isOesTexture ? width : height;
        int h = isOesTexture ? height : width;
        engine.setInputTexture(textureId, w, h, isOesTexture);

        QueenParamHolder.writeParamToEngine(engine, false);

        if (mOutTexture == null) {
            // 是否让Queen保持原纹理方向输出
            mOutTexture = engine.autoGenOutTexture(true);
        }

        // 方式二：根据当前纹理数据到更新
        engine.updateInputTextureBufferAndRunAlg(QueenCameraHelper.get().inputAngle, QueenCameraHelper.get().outAngle,
                QueenCameraHelper.get().flipAxis, false);

        int result = engine.renderTexture(matrix);
        // 处理不成功，则返回原始纹理id
        if (result != 0) {
            return textureId;
        } else {
            return mOutTexture.getTextureId();
        }
    }

    @Override
    public int onTextureProcess(int textureId, boolean isOesTexture, float[] matrix, byte[] imageData, int format, int width, int height) {
//        int w = QueenCameraHelper.get().isLandscape() ? width : height;
//        int h = QueenCameraHelper.get().isLandscape() ? height : width;
        // 而Android的Camera数据，都是旋转90度后的画面，非直接显示看到的正向数据，而纹理是直接显示的画面纹理，
        // 因此，此处为保证与纹理的宽高一致，需要进行旋转，也即w-h调换
        int w = isOesTexture ? height : width;
        int h = isOesTexture ? width : height;
        engine.setInputTexture(textureId, w, h, isOesTexture);

        QueenParamHolder.writeParamToEngine(engine, false);

        if (mOutTexture == null) {
            // 是否让Queen保持原纹理方向输出
            mOutTexture = engine.autoGenOutTexture(true);
        }

        // 方式一：根据回调的bytebuffer数据来更新，人脸检测数据
        engine.updateInputDataAndRunAlg(imageData, format, width, height,
                0, QueenCameraHelper.get().inputAngle, QueenCameraHelper.get().outAngle, QueenCameraHelper.get().flipAxis);

        int result = engine.renderTexture(matrix);
        // 处理不成功，则返回原始纹理id
        if (result != 0) {
            return textureId;
        } else {
            return mOutTexture.getTextureId();
        }
    }

    @Override
    public void onTextureDestroy() {
        if (engine != null) {
            engine.release();
            engine = null;
        }
        if (mOutTexture != null) {
            mOutTexture.release();
            mOutTexture = null;
        }
        QueenParamHolder.relaseQueenParams();
    }
}
