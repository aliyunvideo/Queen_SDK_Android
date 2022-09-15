package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 按照纹理的回调方式进行处理
 */
public class CameraV1TextureRenderer extends SimpleCameraRenderer {

    protected void onCreateEffector(Context context) {
        mQueenEffecter = new QueenBeautyEffecter();
        mQueenEffecter.onInitEngine(context);
    }

    protected int onDrawWithEffectorProcess() {
        // 更新美颜特效参数
        QueenParamHolder.writeParamToEngine(mQueenEffecter.getEngine(), false);
        // 设置抠图参数进行Y轴翻转，否则抠图mask会翻转过来
        if (mQueenEffecter.getEngine() != null) {
            mQueenEffecter.getEngine().setSegmentInfoFlipY(true);
        }

        int in = QueenCameraHelper.get().inputAngle;
        int out = QueenCameraHelper.get().outAngle;
        int flip = QueenCameraHelper.get().flipAxis;
        int updateTextureId = mQueenEffecter.onProcessOesTexture(mOESTextureId,
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
        mQueenEffecter.onSetOutViewportSize(left, bottom, width, height);
    }
}
