package com.aliyun.maliang.android.simpleapp.queen.render;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.aliyun.maliang.android.simpleapp.FileUtils;
import com.aliyun.maliang.android.simpleapp.SurfaceView.FrameTexture2DGlDrawer;
import com.aliyun.maliang.android.simpleapp.queen.QueenCameraHelper;
import com.aliyun.maliang.android.simpleapp.queen.params.QueenParamHolder;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.Texture2D;

/**
 * 模拟调用Queen时，并非直接将相机预览OES数据直接进行回调，而是经过处理后，改用Texture2D纹理进行调用。
 * 美颜或其他需要对展示画面进行后期处理的，均在本Render中指定回调接口中进行处理.
 * 本render用于实现Queen将处理后的纹理渲染到当前画布，适用于，能直接获取到相机数据，且返回相机OES纹理的场景，
 * 处理后的画面数据，直接渲染到当前纹理中
 */
public class QueenBeautyV3Renderer extends QueenBeautyRenderer {

    private static final String TAG = "CameraV1Renderer";
    private Texture2D mOutTexture;

    private FrameTexture2DGlDrawer mOutTextureFrameGlDrawer;

    QueenEngine engine;

    @Override
    protected void step1ReadyQueenEngine(Context context) {
        Log.i("QueenBeautyV3Renderer", "step1ReadyQueenEngine@" + Thread.currentThread().getId());
        try {
            // 注意，此处是将纹理直接显示上屏，也就是说由QueenEngine进行绘制显示出来，Queen直接render之后即可看到最后效果
            engine = new QueenEngine(context, false);
            engine.enableDebugLog();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mOutTextureFrameGlDrawer = new FrameTexture2DGlDrawer();
    }

    @Override
    protected void step2SetScreenViewport(int left, int bottom, int width, int height) {
        Log.i("QueenBeautyV3Renderer", "step2SetScreenViewport@" + Thread.currentThread().getId());
        engine.setScreenViewport(left, bottom, width, height);
    }

    @Override
    protected void step3Draw1UpdateTextureAndWriteParamToQueenEngine(int textureId, boolean isOesTexture, int width, int height) {
        Log.i("QueenBeautyV3Renderer", "step3Draw1UpdateTextureAndWriteParamToQueenEngine@" + Thread.currentThread().getId() + "[width: " + width + ", height: " + height + "]");
        engine.setInputTexture(textureId, width, height, isOesTexture);
        QueenParamHolder.writeParamToQueenEngine(engine);

        if (mOutTexture == null) {
            // 是否让Queen保持原纹理方向输出
            mOutTexture = engine.autoGenOutTexture(true);
            Log.i("QueenBeautyV3Renderer", "step3Draw2UpdateBufferToQueenEngine_IF_NEED@" + Thread.currentThread().getId() + " ---1 [w: " + mOutTexture.getSize().x + ", h: " + mOutTexture.getSize().y + "]");
        }
    }

    @Override
    protected void step3Draw2UpdateBufferToQueenEngine_IF_NEED(byte[] imageData, int format, int width, int height) {
        // 方式一：根据回调的bytebuffer数据来更新，人脸检测数据
//        engine.updateInputDataAndRunAlg(imageData, format, width, height,
//                0, QueenCameraHelper.get().inputAngle, QueenCameraHelper.get().outAngle, QueenCameraHelper.get().flipAxis);

        // 方式二：根据当前纹理数据到更新
        engine.updateInputTextureBufferAndRunAlg(QueenCameraHelper.get().inputAngle, QueenCameraHelper.get().outAngle,
                QueenCameraHelper.get().flipAxis, false);
    }

    @Override
    protected int step3Draw3Render(float[] matrix) {
        int result = engine.renderTexture(matrix);

        // QueenEngine的结果是渲染到outTexture上，因此，此处还需要将结果绘制出来
        mOutTextureFrameGlDrawer.draw(matrix, mOutTexture.getTextureId());
        return result;
    }

    @Override
    protected void step4ReleaseQueenEngine() {
        super.step4ReleaseQueenEngine();
        Log.i("QueenBeautyV3Renderer", "step4ReleaseQueenEngine@" + Thread.currentThread().getId());
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

    @Override
    protected boolean captureFrame(String filePath) {

        Bitmap outBitmap  = mOutTexture.readToBitmap();

        int rotateAngle = QueenCameraHelper.get().inputAngle % 360;
        Log.i("QueenBeautyV3Renderer", "captureFrame [w: " + outBitmap.getWidth() + ", h: " + outBitmap.getHeight() +
                ", inputAngle: " + QueenCameraHelper.get().inputAngle + ", outputAngle: " + QueenCameraHelper.get().outAngle + ", rotateAngle: " + rotateAngle +
                ", frontCamera: " + QueenCameraHelper.get().isFrontCamera() + "]");
        outBitmap = FileUtils.bitmapFlipAndRotate(outBitmap, rotateAngle, false, QueenCameraHelper.get().isFrontCamera());
        return FileUtils.saveToFile(outBitmap, filePath, Bitmap.CompressFormat.PNG, 100);
    }
}
