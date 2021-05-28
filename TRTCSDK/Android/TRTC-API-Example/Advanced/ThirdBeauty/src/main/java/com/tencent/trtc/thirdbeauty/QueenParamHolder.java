package com.tencent.trtc.thirdbeauty;

import android.text.TextUtils;

import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.models.BeautyFilterType;
import com.taobao.android.libqueen.models.BeautyParams;
import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.FaceShapeType;
import com.taobao.android.libqueen.models.MakeupType;

import java.util.Iterator;

public class QueenParamHolder {
    /**
     * 参数加权配置
     */
    private interface QueenParamWeight {
        float SKIN_WHITING = 1.0f;

        float SKIN_BUFFING = 1.0f;
        float SKIN_SHARPEN = 1.0f;

        float FACE_BUFFING_NASOLABIALFOLDS = 1.0f;
        float FACE_BUFFING_POUCH = 1.0f;
        float FACE_BUFFING_WHITE_TEETH = 1.0f;
        float FACE_BUFFING_LIPSTICK = 1.0f;
        float FACE_BUFFING_BLUSH = 1.0f;

        float FACE_SHAPE_PARAM = 1.0f;

        float FACE_MAKEUP_ALPHA = 1.0f;

        float FACE_LUT_PARAM = 1.0f;
    }

    private static QueenParam sQueenParam;

    public static QueenParam getQueenParam() {
        if (null == sQueenParam) {
            sQueenParam = new QueenParam();
        }
        return sQueenParam;
    }

    public static void writeParamToEngine(QueenEngine mediaChainEngine) {
        if (null != mediaChainEngine) {
            // 美颜
            mediaChainEngine.enableBeautyType(BeautyFilterType.kSkinWhiting, getQueenParam().basicBeautyRecord.enableSkinWhiting);//美白开关
            mediaChainEngine.setBeautyParam(BeautyParams.kBPSkinWhitening, getQueenParam().basicBeautyRecord.skinWhitingParam * QueenParamWeight.SKIN_WHITING);  //美白 [0,1]

            mediaChainEngine.enableBeautyType(BeautyFilterType.kSkinBuffing, getQueenParam().basicBeautyRecord.enableSkinBuffing);//磨皮开关
            mediaChainEngine.setBeautyParam(BeautyParams.kBPSkinBuffing, getQueenParam().basicBeautyRecord.skinBuffingParam * QueenParamWeight.SKIN_BUFFING);  //磨皮 [0,1]
            mediaChainEngine.setBeautyParam(BeautyParams.kBPSkinSharpen, getQueenParam().basicBeautyRecord.skinSharpenParam * QueenParamWeight.SKIN_SHARPEN);  //锐化 [0,1]

            mediaChainEngine.enableBeautyType(BeautyFilterType.kFaceBuffing, getQueenParam().basicBeautyRecord.enableFaceBuffing); //高级美颜开关
            mediaChainEngine.setBeautyParam(BeautyParams.kBPNasolabialFolds, getQueenParam().basicBeautyRecord.faceBuffingNasolabialFoldsParam * QueenParamWeight.FACE_BUFFING_NASOLABIALFOLDS); //去法令纹[0,1]
            mediaChainEngine.setBeautyParam(BeautyParams.kBPPouch, getQueenParam().basicBeautyRecord.faceBuffingPouchParam * QueenParamWeight.FACE_BUFFING_POUCH); //去眼袋[0,1]
            mediaChainEngine.setBeautyParam(BeautyParams.kBPWhiteTeeth, getQueenParam().basicBeautyRecord.faceBuffingWhiteTeeth * QueenParamWeight.FACE_BUFFING_WHITE_TEETH); //白牙[0,1]
            mediaChainEngine.setBeautyParam(BeautyParams.kBPLipstick, getQueenParam().basicBeautyRecord.faceBuffingLipstick * QueenParamWeight.FACE_BUFFING_LIPSTICK); //滤镜美妆：口红[0,1]
            mediaChainEngine.setBeautyParam(BeautyParams.kBPBlush, getQueenParam().basicBeautyRecord.faceBuffingBlush * QueenParamWeight.FACE_BUFFING_BLUSH); //滤镜美妆：腮红[0,1]

            // 美型
            mediaChainEngine.enableBeautyType(BeautyFilterType.kFaceShape, getQueenParam().faceShapeRecord.enableFaceShape,false);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCutCheek     , getQueenParam().faceShapeRecord.cutCheekParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCutFace      , getQueenParam().faceShapeRecord.cutFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinFace     , getQueenParam().faceShapeRecord.thinFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeLongFace     , getQueenParam().faceShapeRecord.longFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeLowerJaw     , getQueenParam().faceShapeRecord.lowerJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeHigherJaw    , getQueenParam().faceShapeRecord.higherJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinJaw      , getQueenParam().faceShapeRecord.thinJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinMandible , getQueenParam().faceShapeRecord.thinMandibleParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeBigEye       , getQueenParam().faceShapeRecord.bigEyeParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeEyeAngle1    , getQueenParam().faceShapeRecord.eyeAngle1Param * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCanthus      , getQueenParam().faceShapeRecord.canthusParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeCanthus1     , getQueenParam().faceShapeRecord.canthus1Param * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeEyeAngle2    , getQueenParam().faceShapeRecord.eyeAngle2Param * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeEyeTDAngle   , getQueenParam().faceShapeRecord.eyeTDAngleParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeThinNose     , getQueenParam().faceShapeRecord.thinNoseParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeNosewing     , getQueenParam().faceShapeRecord.nosewingParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeNasalHeight  , getQueenParam().faceShapeRecord.nasalHeightParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeNoseTipHeight, getQueenParam().faceShapeRecord.noseTipHeightParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeMouthWidth   , getQueenParam().faceShapeRecord.mouthWidthParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeMouthSize    , getQueenParam().faceShapeRecord.mouthSizeParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typeMouthHigh    , getQueenParam().faceShapeRecord.mouthHighParam * QueenParamWeight.FACE_SHAPE_PARAM);
            mediaChainEngine.updateFaceShape(FaceShapeType.typePhiltrum     , getQueenParam().faceShapeRecord.philtrumParam * QueenParamWeight.FACE_SHAPE_PARAM);

//            // 美妆
//            mediaChainEngine.enableBeautyType(BeautyFilterType.kMakeup, getQueenParam().faceMakeupRecord.enableFaceMakeup,false);
//            if (getQueenParam().faceMakeupRecord.enableFaceMakeup) {
//                for (int makeupType = 0; makeupType < MakeupType.kMakeupMax; makeupType++) {
//                    String makeUpResourcePath = getQueenParam().faceMakeupRecord.makeupResourcePath[makeupType];
//                    if (!TextUtils.isEmpty(makeUpResourcePath)) {
//                        String[] path = new String[] {makeUpResourcePath};
//                        int blendType = getQueenParam().faceMakeupRecord.makeupBlendType[makeupType];
//                        float alpha = getQueenParam().faceMakeupRecord.makeupAlpha[makeupType];
//                        mediaChainEngine.setMakeupImage(makeupType, path, blendType, 15);
//                        mediaChainEngine.setMakeupAlpha(makeupType,
//                                alpha * QueenParamWeight.FACE_MAKEUP_ALPHA,
//                                (1.0f-alpha) * QueenParamWeight.FACE_MAKEUP_ALPHA);
//                    } else {
//                        String[] path = new String[] {};
//                        mediaChainEngine.setMakeupImage(makeupType, path, BlendType.kBlendNormal, 15);
//                    }
//                }
//            }

            // 滤镜
            mediaChainEngine.enableBeautyType(BeautyFilterType.kLUT, getQueenParam().lutRecord.lutEnable);
            if (getQueenParam().lutRecord.lutEnable) {
                mediaChainEngine.setFilter(getQueenParam().lutRecord.lutPath); //设置滤镜
                mediaChainEngine.setBeautyParam(BeautyParams.kBPLUT, getQueenParam().lutRecord.lutParam * QueenParamWeight.FACE_LUT_PARAM); //滤镜强度
            }

            // 贴纸
//            boolean enableSticker = getQueenParam().stickerRecord.stickerEnable;
//            String stickerPath = getQueenParam().stickerRecord.stickerPath;
//            if (enableSticker) {
//                if (!TextUtils.isEmpty(stickerPath) && !getQueenParam().stickerRecord.usingStickerPathList.contains(stickerPath)) {
//                    mediaChainEngine.addMaterial(stickerPath);
//                    getQueenParam().stickerRecord.usingStickerPathList.add(stickerPath);
//                }
//            }
//            Iterator<String> iterator = getQueenParam().stickerRecord.usingStickerPathList.iterator();
//            while (iterator.hasNext()) {
//                String usingStickerPath = iterator.next();
//                if (!enableSticker || !TextUtils.equals(usingStickerPath, stickerPath)) {
//                    mediaChainEngine.removeMaterial(usingStickerPath);
//                    iterator.remove();
//                }
//            }
        }
    }

    public static void relaseQueenParams() {
        getQueenParam().stickerRecord.usingStickerPathList.clear();
    }

}
