package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 按照databuffer的回调方式进行处理
 */
public class CameraV2BufferRenderer extends SimpleCameraRenderer {

    protected void onCreateEffector(Context context) {
        mQueenEffecter = new QueenBeautyEffecter();
        mQueenEffecter.onInitEngine(context);
    }

    protected int onDrawWithEffectorProcess() {
        // 更新美颜特效参数
        QueenParamHolder.writeParamToEngine(mQueenEffecter.getEngine(), false);
        // 设置抠图参数进行Y轴翻转，否则抠图mask会翻转过来
        mQueenEffecter.engine.setSegmentInfoFlipY(true);

        int in = QueenCameraHelper.get().inputAngle;
        int out = QueenCameraHelper.get().outAngle;
        int flip = QueenCameraHelper.get().flipAxis;
        // 此处没有使用buffer进行绘制的示例，还是用的纹理表示。
        int updateTextureId = mQueenEffecter.processTexture(mOESTextureId, true,
                transformMatrix, mCameraPreviewWidth, mCameraPreviewHeight,
                in, out, flip);

        return updateTextureId;
    }

    @Override
    protected void onReleaseEffector() {
        // 释放Engine
        mQueenEffecter.onReleaseEngine();
    }

    // 非必需步骤调用，只有显示画面，与纹理传入的size不一致时，才需要设置ViewportSize，
    // 否则，默认使用输入纹理的size
    @Override
    protected void onSetViewportSize(int left, int bottom, int width, int height) {
        mQueenEffecter.setOutViewportSize(left, bottom, width, height);
    }
}
