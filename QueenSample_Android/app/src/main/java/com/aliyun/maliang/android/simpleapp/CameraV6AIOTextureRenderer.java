package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

import com.aliyun.android.libqueen.aio.QueenBeautyInterface;
import com.aliyun.android.libqueen.aio.IBeautyParamsHolder;
import com.aliyun.android.libqueen.aio.QueenBeautyWrapper;
import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 按照纹理的回调方式进行处理，直接使用QueenEngine实现
 */
public class CameraV6AIOTextureRenderer extends SimpleCameraRenderer {
    private QueenBeautyInterface mBeautyImpl;

    protected void onCreateEffector(Context context) {
        mBeautyImpl = new QueenBeautyWrapper();
        mBeautyImpl.init(context);
        mBeautyImpl.setBeautyParams(new IBeautyParamsHolder() {
            @Override
            public void onWriteParamsToBeauty(Object o) {
                QueenParamHolder.writeParamToEngine(o);
            }
        });
    }

    protected int onDrawWithEffectorProcess() {
        int in = QueenCameraHelper.get().inputAngle;
        int out = QueenCameraHelper.get().outAngle;
        int flip = QueenCameraHelper.get().flipAxis;
        int updateTextureId = processTextureInner(mOESTextureId, true,
                transformMatrix, mCameraPreviewWidth, mCameraPreviewHeight,
                in, out, flip);

        return updateTextureId;
    }

    @Override
    protected void onReleaseEffector() {
        // 释放Engine
        if (mBeautyImpl != null) {
            mBeautyImpl.release();
            mBeautyImpl = null;
        }
    }

    // 非必需步骤调用，只有显示画面，与纹理传入的size不一致时，才需要设置ViewportSize，
    // 否则，默认使用输入纹理的size
    @Override
    protected void onSetViewportSize(int left, int bottom, int width, int height) {
        super.onSetViewportSize(left, bottom, width, height);
//        mQueenEngine.setScreenViewport(left, bottom, width, height);
    }

    private int processTextureInner(int textureId, boolean isOesTexture, float[] matrix, int width, int height, int inputAngle, int outAngle, int flipAxis) {
        return mBeautyImpl.onProcessTexture(textureId, isOesTexture, matrix, width, height , inputAngle, outAngle, flipAxis);
    }
}
