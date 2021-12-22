package com.aliyun.maliang.android.simpleapp.queen.render;

import android.content.Context;

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
public class QueenBeautyRenderer {
    protected boolean mReleased = false;

    protected void step1ReadyQueenEngine(Context context) {}

    protected void step2SetScreenViewport(int left, int bottom, int width, int height) {}

    protected void step3Draw1UpdateTextureAndWriteParamToQueenEngine(int textureId, boolean isOesTexture, int width, int height){}

    // 高级功能需要，基础美颜不需要
    protected void step3Draw2UpdateBufferToQueenEngine_IF_NEED(byte[] imageData, int format, int width, int height) {}

    protected int step3Draw3Render(float[] matrix) {return -9;}

    protected void step4ReleaseQueenEngine() {
        mReleased = true;
    }

    protected boolean captureFrame(String filePath) {
        return false;
    }

    public boolean isReleased() {
        return mReleased;
    }
}
