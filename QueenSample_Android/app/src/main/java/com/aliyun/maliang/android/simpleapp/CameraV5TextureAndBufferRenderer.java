//package com.aliyun.maliang.android.simpleapp;
//
//import android.content.Context;
//
//import com.aliyun.android.libqueen.aio.BeautyImageFormat;
//import com.aliyun.android.libqueen.aio.BeautyInterface;
//import com.aliyun.android.libqueen.aio.IBeautyParamsHolder;
//import com.aliyun.android.libqueen.aio.QueenBeautyWrapper;
//import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
//import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
//import com.aliyunsdk.queen.menu.QueenParamsWrapper;
//
///**
// * 按照纹理的回调方式，同时算法，采用buffer的方式进行处理，效率最高
// */
//public class CameraV5TextureAndBufferRenderer extends SimpleCameraRenderer {
//    private BeautyInterface mQueenEffector;
//
//    protected void onCreateEffector(Context context) {
//        mQueenEffector = new QueenBeautyWrapper();
//        mQueenEffector.init(context, 0l);
//        mQueenEffector.setBeautyParams(new IBeautyParamsHolder() {
//            @Override
//            public void onWriteParamsToBeauty(Object o) {
//                QueenParamsWrapper.writeParams(o);
////                QueenParamHolder.writeParamToEngine((com.aliyun.android.libqueen.QueenEngine) engine, false);
////                com.aliyun.android.libqueen.aio.QueenEngine queenEngine = (com.aliyun.android.libqueen.aiobasic.QueenEngine)engine;
////                // 美白开关
////                queenEngine.enableBeautyType(4, true);
////                queenEngine.setBeautyParam(3, 0.5f);  //美白 [0,1]
////                queenEngine.setBeautyParam(14,0.3f);  //红润 [0,1]
////                //磨皮开关
////                queenEngine.enableBeautyType(0, true, 2);   // 夸张磨皮
////                queenEngine.setBeautyParam(1, 0.8f);  //磨皮 [0,1]
////                queenEngine.setBeautyParam(2, 0.3f);  //锐化 [0,1]
//            }
//        });
//
//    }
//
//    protected int onDrawWithEffectorProcess() {
//        int updateTextureId = mOESTextureId;
//        // buffer数据来源，主要直接来自camera
//        byte[] buffer = mCamera.getLastUpdateCameraPixels();
//        if (buffer != null) {
//            int in = QueenCameraHelper.get().inputAngle;
//            int out = QueenCameraHelper.get().outAngle;
//            int flip = QueenCameraHelper.get().flipAxis;
//            updateTextureId = mQueenEffector.onProcessTextureAndBuffer(mOESTextureId, true,
//                    mCameraPreviewWidth, mCameraPreviewHeight,
//                    in, out,
//                    buffer, BeautyImageFormat.NV21);
//        }
//
//        return updateTextureId;
//    }
//
//    @Override
//    protected void onReleaseEffector() {
//        // 释放Engine
//        mQueenEffector.release();
//    }
//}
