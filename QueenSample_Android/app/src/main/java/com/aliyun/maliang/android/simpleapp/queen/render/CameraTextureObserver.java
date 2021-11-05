package com.aliyun.maliang.android.simpleapp.queen.render;

import android.content.Context;

import com.aliyun.maliang.android.simpleapp.SurfaceView.CameraRenderer;
import com.aliyun.maliang.android.simpleapp.queen.QueenCameraHelper;

/**
 * 本类模拟常见sdk的纹理回调接口，即纹理开始创建/画面更新/画面结束三个步骤的回调，这三个步骤是多数sdk支持第三方美颜所必须提供的三个接口回调。
 * （某些sdk可能会合并其中项，如将创建放在更新接口回调中，需用户自行判断某些对象是否初始化过，来区分是否第一次回调）
 *
 * 对应几个步骤中，Queen-engine的使用，需要做哪些事情，可参见QueenBeautyRenderer中的说明。
 * 此处简单来说，也是分为四个步骤：1.准备Queen-engine;2.设置screenViewPort;3.设置美颜参数、传入画面数据、开始绘制;4.退出销毁Queen-engine
 */
public class CameraTextureObserver implements CameraRenderer.ITextureObserver {

    QueenBeautyRenderer mBeautyQueenRender;

    public CameraTextureObserver() {
        mBeautyQueenRender = new QueenBeautyV1Renderer();
    }

    @Override
    public void onTextureCreated(Context context) {
        // [Queen-STEP1]Queen使用第一步：在纹理ready首次回调时，初始化创建QueenEngine
        mBeautyQueenRender.step1ReadyQueenEngine(context);
    }

    @Override
    public void onTextureChanged(int left, int bottom, int width, int height) {
        mBeautyQueenRender.step2SetScreenViewport(left, bottom, width, height);
    }

    @Override
    public int onTextureUpdated(int textureId, boolean isOesTexture, float[] matrix, byte[] imageData, int format, int width, int height) {
        // 注释：注意，此处由于已知传入的imageData是直接从Camera中获取的原始数据，此处的width/height即是该画面帧数据的宽/高。
        // 而Android的Camera数据，都是旋转90度后的画面，非直接显示看到的正向数据，而纹理是直接显示的画面纹理，
        // 因此，此处为保证与纹理的宽高一致，需要进行旋转，也即w-h调换
        int w = QueenCameraHelper.get().isLandscape() ? width : height;
        int h = QueenCameraHelper.get().isLandscape() ? height : width;
        mBeautyQueenRender.step3Draw1UpdateTextureAndWriteParamToQueenEngine(textureId, isOesTexture, w, h);

        mBeautyQueenRender.step3Draw2UpdateBufferToQueenEngine_IF_NEED(imageData, format, width, height);

        int retCode = mBeautyQueenRender.step3Draw3Render(matrix);
        return retCode;
    }

    @Override
    public void onTextureDestroy() {
        mBeautyQueenRender.step4ReleaseQueenEngine();
    }
}
