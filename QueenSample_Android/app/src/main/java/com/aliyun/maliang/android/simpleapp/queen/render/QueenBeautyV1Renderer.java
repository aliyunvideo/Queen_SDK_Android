package com.aliyun.maliang.android.simpleapp.queen.render;

import android.content.Context;

import com.aliyun.maliang.android.simpleapp.SurfaceView.CameraRenderer;
import com.aliyun.maliang.android.simpleapp.queen.QueenCameraHelper;
import com.aliyun.maliang.android.simpleapp.queen.params.QueenParamHolder;
import com.taobao.android.libqueen.ImageFormat;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.Texture2D;

/**
 * Surface.Renderer，用于相机预览数据的回调
 * 美颜或其他需要对展示画面进行后期处理的，均在本Render中指定回调接口中进行处理.
 * 本render用于实现Queen将处理后的纹理渲染到当前画布，适用于，能直接获取到相机数据，且返回相机OES纹理的场景，
 * 处理后的画面数据，直接渲染到当前纹理中
 */
public class QueenBeautyV1Renderer extends QueenBeautyRenderer {

    private static final String TAG = "QueenBeautyV1Renderer";

    QueenEngine engine;

    @Override
    protected void step1ReadyQueenEngine(Context context) {
        try {
            // 注意，此处是将纹理直接显示上屏，也就是说由QueenEngine进行绘制显示出来，Queen直接render之后即可看到最后效果
            engine = new QueenEngine(context, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void step2SetScreenViewport(int left, int bottom, int width, int height) {
        engine.setScreenViewport(left, bottom, width, height);
    }

    @Override
    protected void step3Draw1UpdateTextureAndWriteParamToQueenEngine(int textureId, boolean isOesTexture, int width, int height) {
        if (engine != null) {
            engine.setInputTexture(textureId, width, height, isOesTexture);
        }

        QueenParamHolder.writeParamToQueenEngine(engine);
    }

    @Override
    protected void step3Draw2UpdateBufferToQueenEngine_IF_NEED(byte[] imageData, int format, int width, int height) {
        // 此处有两种方式，一种用相机返回的当前帧数据bytebuffer直接进行处理, 另一种为，直接采用当前纹理，由QueenEngine内部从纹理中dump出bytebuffer数据进行处理。
        // 第一种方式，可能存在某些sdk回调的当前帧数据和真实当前显示帧画面数据不一致的问题，系对应sdk的bug，非QueenEngine的bug
        // 第二种方式，在个别低端机或特殊机型场景下，不排除会存在dump数据过慢问题，但通常都是在个位数ms级别以内。
        // 在sdk回调能获取到当前帧数据buffer且数据正确的情况下，推荐采用第一种方式，性能会更高一点;
        // 如果不便获取buffer场景，可用第二种方式进行接入。
        // 方式一：根据回调的bytebuffer数据来更新，人脸检测数据
        engine.updateInputDataAndRunAlg(imageData, format, width, height,
                0, QueenCameraHelper.get().inputAngle, QueenCameraHelper.get().outAngle, QueenCameraHelper.get().flipAxis);

        // 方式二：根据当前纹理数据到更新
//        engine.updateInputTextureBufferAndRunAlg(QueenCameraHelper.get().inputAngle, QueenCameraHelper.get().outAngle,
//                QueenCameraHelper.get().flipAxis, false);
    }

    @Override
    protected int step3Draw3Render(float[] matrix) {
        return engine.renderTexture(matrix);
    }

    @Override
    protected void step4ReleaseQueenEngine() {
        if (engine != null) {
            engine.release();
            engine = null;
        }
        QueenParamHolder.relaseQueenParams();
    }
}
