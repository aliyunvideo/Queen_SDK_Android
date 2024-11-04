package com.aliyun.maliang.android.simpleapp;

import android.content.Context;

import com.aliyun.android.libqueen.Algorithm;
import com.aliyun.android.libqueen.QueenBeautyEffector;
import com.aliyun.android.libqueen.QueenConfig;
import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.models.AlgInputMode;
import com.aliyun.android.libqueen.models.AlgType;
import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.utils.DebugHelper;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyunsdk.queen.param.QueenParamHolder;

/**
 * 按照纹理的回调方式进行处理，直接使用QueenEngine实现
 */
public class CameraV1TextureRenderer extends SimpleCameraRenderer {
    private QueenEngine mQueenEngine;

    protected void onCreateEffector(Context context) {
        try {
            if (AppRuntime.DEBUG_IS_ALG_AUTO_MODE) {
                // 如果算法自动检测输入方向模式，则用QueenConfig进行初始化。
                // 注意开启自动检测模式，性能会下降，因为需要每帧都去检测，有额外运算
                QueenConfig queenConfig = new QueenConfig();
//                queenConfig.enableDebugLog = true;
//                queenConfig.algInputMode = AlgInputMode.kModeAutomatic;
                mQueenEngine = new QueenEngine(context, queenConfig);
            } else {
                mQueenEngine = new QueenEngine(context);
            }
        } catch (Exception e) { e.printStackTrace(); }

        DebugHelper.afterInitEngine(mQueenEngine);
    }

    protected int onDrawWithEffectorProcess() {
        // 更新美颜特效参数，参数修改，在菜单组件中已完成交互
        QueenParamHolder.writeParamToEngine(mQueenEngine, false);
        // 设置抠图参数进行Y轴翻转，否则抠图mask会翻转过来
        if (mQueenEngine != null) {
            mQueenEngine.setSegmentInfoFlipY(true);
            // 调试时可用，开启log日志
            if (AppRuntime.DEBUG_ENABLE_LOG) {
                mQueenEngine.enableDebugLog();
            }
            // 调试时可用，开启人脸点位
            if (AppRuntime.DEBUG_SHOW_FACE_POINTS) {
                mQueenEngine.enableDetectPointDebug(AlgType.kFaceDetect, true);
            }
        }

        int in = QueenCameraHelper.get().inputAngle;
        int out = QueenCameraHelper.get().outAngle;
        int flip = QueenCameraHelper.get().flipAxis;
        int updateTextureId = processTextureInner(mQueenEngine, mOESTextureId, true,
                transformMatrix, mCameraPreviewWidth, mCameraPreviewHeight,
                in, out, flip);

        // 调试时可用，可用于查看处理前后画面对比
//        DebugHelper.afterProcessEngine(mQueenEngine, mOESTextureId, true, mCameraPreviewWidth, mCameraPreviewHeight);

        return updateTextureId;
    }

    @Override
    protected void onReleaseEffector() {
        // 释放Engine
        if (mQueenEngine != null) {
            mQueenEngine.release();
            mQueenEngine = null;
        }
    }

    // 非必需步骤调用，只有显示画面，与纹理传入的size不一致时，才需要设置ViewportSize，
    // 否则，默认使用输入纹理的size
    @Override
    protected void onSetViewportSize(int left, int bottom, int width, int height) {
        super.onSetViewportSize(left, bottom, width, height);
        mQueenEngine.setScreenViewport(left, bottom, width, height);
    }

    private int processTextureInner(QueenEngine engine, int textureId, boolean isOesTexture, float[] matrix, int width, int height, int inputAngle, int outAngle, int flipAxis) {
        int w = isOesTexture ? height : width;
        int h = isOesTexture ? width : height;
        engine.setInputTexture(textureId, w, h, isOesTexture);

        // 输出纹理id是否已生成
        int outTextId = mQueenEngine.getAutoGenOutTextureId();
        if (outTextId <= 0) {
            // 是否让Queen保持原纹理方向输出
            boolean keepInputDirection = isOesTexture;
            engine.autoGenOutTexture(keepInputDirection);
        }

        // 根据当前纹理数据到更新
        engine.updateInputTextureBufferAndRunAlg(inputAngle, outAngle, flipAxis, false);
        int result = matrix != null ? engine.renderTexture(matrix) : engine.render();
        // 处理不成功，则返回原始纹理id
        if (result != 0) {
            return textureId;
        } else {
            return mQueenEngine.getAutoGenOutTextureId();
        }
    }
}
