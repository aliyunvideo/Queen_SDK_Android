package com.aliyun.maliang.android.simpleapp.camera;

import android.content.Context;

import com.aliyun.queen.IQueenRender;
import com.aliyun.queen.QueenRender;

/**
 * 本类模拟常见sdk的纹理回调接口，即纹理开始创建/画面更新/画面结束三个步骤的回调，这三个步骤是多数sdk支持第三方美颜所必须提供的三个接口回调。
 * （某些sdk可能会合并其中项，如将创建放在更新接口回调中，需用户自行判断某些对象是否初始化过，来区分是否第一次回调）
 *
 * 对应几个步骤中，Queen-engine的使用，需要做哪些事情，可参见QueenBeautyRenderer中的说明。
 * 此处简单来说，也是分为四个步骤：1.准备Queen-engine;2.设置screenViewPort;3.设置美颜参数、传入画面数据、开始绘制;4.退出销毁Queen-engine
 */
public class CameraTextureObserver {

    IQueenRender mQueenRender;

    public CameraTextureObserver(boolean toScreen) {
        mQueenRender = new QueenRender.Builder()
                .setDraw2Screen(toScreen)
                .build();
    }

    public void onTextureCreated(Context context) {
        mQueenRender.onTextureCreate(context);
    }

    public void onTextureChanged(int left, int bottom, int width, int height) {
        mQueenRender.onTextureSizeChanged(left, bottom, width, height);
    }

    public int onTextureUpdated(int textureId, boolean isOesTexture, float[] matrix, byte[] imageData, int format, int width, int height) {
        // 注释：注意，此处由于已知传入的imageData是直接从Camera中获取的原始数据，此处的width/height即是该画面帧数据的宽/高，而非纹理的宽高。
        // 而Android的Camera数据，都是旋转90度后的画面，非直接显示看到的正向数据，而纹理是直接显示的画面纹理，
        // 因此，此处为保证与纹理的宽高一致，需要进行旋转，也即w-h调换
//        int w = QueenCameraHelper.get().isLandscape() ? width : height;
//        int h = QueenCameraHelper.get().isLandscape() ? height : width;
        return mQueenRender.onTextureProcess(textureId, isOesTexture, matrix, imageData, format, width, height);
    }

    public void onTextureDestroy() {
        mQueenRender.onTextureDestroy();
    }
}
