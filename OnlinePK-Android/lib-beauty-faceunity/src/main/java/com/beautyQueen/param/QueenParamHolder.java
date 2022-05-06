package com.beautyQueen.param;

import android.text.TextUtils;

import com.aliyun.android.libqueen.Algorithm;
import com.aliyun.android.libqueen.QueenEngine;
import com.aliyun.android.libqueen.models.AlgType;
import com.aliyun.android.libqueen.models.BeautyFilterType;
import com.aliyun.android.libqueen.models.BeautyParams;
import com.aliyun.android.libqueen.models.BlendType;
import com.aliyun.android.libqueen.models.BodyShapeType;
import com.aliyun.android.libqueen.models.FaceShapeType;
import com.aliyun.android.libqueen.models.MakeupType;

import java.util.Iterator;
import java.util.List;

public class QueenParamHolder {
    /**
     * 参数加权配置
     */
    private interface QueenParamWeight {
        float FACE_SHAPE_PARAM = 1.0f;
        float FACE_MAKEUP_ALPHA = 1.0f;
        float FACE_LUT_PARAM = 1.0f;
    }

    private static QueenParam sQueenParam;

    public static QueenParam getQueenParam() {
        if (null == sQueenParam) {
            sQueenParam = QueenParamFactory.getDefaultScenesParam();
        }
        return sQueenParam;
    }

    public static void setQueenParam(QueenParam queenParam) {
        sQueenParam = queenParam;
    }

    public static void writeParamToEngine(QueenEngine queenEngine, boolean canUseAsyncAlg) {
//        Log.e("lzx_queen", "writeParamToEngine : " + getQueenParam());
        if (null != queenEngine) {
            queenEngine.enableDetectPointDebug(AlgType.kFaceDetect, QueenRuntime.sFaceDetectDebug);

            queenEngine.setGreenScreen(
                    getQueenParam().segmentRecord.enableGreenSegment || getQueenParam().segmentRecord.enableBlueSegment  ? getQueenParam().segmentRecord.greenSegmentBackgroundPath : "",
                    getQueenParam().segmentRecord.enableBlueSegment,
                    getQueenParam().segmentRecord.enableBlueSegment ? getQueenParam().segmentRecord.blueSegmentThreshold : getQueenParam().segmentRecord.greenSegmentThreshold,
                    getQueenParam().segmentRecord.enableBlueSegment ? getQueenParam().segmentRecord.enableBlueSegmentAutoThreshold : getQueenParam().segmentRecord.enableGreenSegmentAutoThreshold
            );

            queenEngine.enableBeautyType(BeautyFilterType.kBTAutoFilter, getQueenParam().basicBeautyRecord.enableAutoFiltering);//自动美颜开关

            // 选择实景抠像性能模式，需要在开启实景抠像功能前先设置好
            queenEngine.setSegmentPerformanceMode(QueenParam.SegmentRecord.segmentPerformanceMode);

            // AI抠图背景虚化/背景透明
            queenEngine.enableBeautyType(BeautyFilterType.kBTBackgroundProcess, getQueenParam().segmentRecord.enableAiSegment);
            if (getQueenParam().segmentRecord.enableAiSegment) {
                queenEngine.setSegmentBackgroundProcessType(getQueenParam().segmentRecord.backgroundProcessType);
            }

            queenEngine.setAISegmentForegroundPadding(getQueenParam().segmentRecord.aiSegmentForegroundPadding);
            queenEngine.setAlgAsych(AlgType.kBokehAiSegment, canUseAsyncAlg && getQueenParam().segmentRecord.aiSegmentAsync);

            // AI抠图背景资源设置
            addMaterial(queenEngine, true, getQueenParam().segmentRecord.aiSegmentBackgroundPath,
                    getQueenParam().segmentRecord.usingAiSegmentBackgroundPathList);

            // 美颜
            queenEngine.enableBeautyType(BeautyFilterType.kSkinWhiting, getQueenParam().basicBeautyRecord.enableSkinWhiting);//美白开关
            if (!getQueenParam().basicBeautyRecord.enableAutoFiltering && getQueenParam().basicBeautyRecord.enableSkinWhiting) {
                queenEngine.setBeautyParam(BeautyParams.kBPSkinWhitening, getQueenParam().basicBeautyRecord.skinWhitingParam);  //美白 [0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPSkinRed,
                        getQueenParam().basicBeautyRecord.enableSkinRed ? getQueenParam().basicBeautyRecord.skinRedParam : 0.0f);  //红润 [0,1]
            }

            queenEngine.enableBeautyType(BeautyFilterType.kSkinBuffing, getQueenParam().basicBeautyRecord.enableSkinBuffing, getQueenParam().basicBeautyRecord.skinBuffingLeverParam);//磨皮开关
            if (!getQueenParam().basicBeautyRecord.enableAutoFiltering && getQueenParam().basicBeautyRecord.enableSkinBuffing) {
                queenEngine.setBeautyParam(BeautyParams.kBPSkinBuffing, getQueenParam().basicBeautyRecord.skinBuffingParam);  //磨皮 [0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPSkinSharpen, getQueenParam().basicBeautyRecord.skinSharpenParam);  //锐化 [0,1]
            }

            queenEngine.enableBeautyType(BeautyFilterType.kFaceBuffing, getQueenParam().basicBeautyRecord.enableFaceBuffing); //高级美颜开关
            if (getQueenParam().basicBeautyRecord.enableFaceBuffing) {
                queenEngine.setBeautyParam(BeautyParams.kBPNasolabialFolds, getQueenParam().basicBeautyRecord.faceBuffingNasolabialFoldsParam); //去法令纹[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPPouch, getQueenParam().basicBeautyRecord.faceBuffingPouchParam); //去眼袋[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPWhiteTeeth, getQueenParam().basicBeautyRecord.faceBuffingWhiteTeeth); //白牙[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPLipstick,
                        getQueenParam().basicBeautyRecord.enableFaceBuffingLipstick ?
                        getQueenParam().basicBeautyRecord.faceBuffingLipstick : 0.0f
                ); //滤镜美妆：口红[0,1]
                if (getQueenParam().basicBeautyRecord.enableFaceBuffingLipstick) {
                    queenEngine.setBeautyParam(BeautyParams.kBPLipstickColorParam, getQueenParam().basicBeautyRecord.faceBuffingLipstickColorParams);
                    queenEngine.setBeautyParam(BeautyParams.kBPLipstickGlossParam, getQueenParam().basicBeautyRecord.faceBuffingLipstickGlossParams);
                    queenEngine.setBeautyParam(BeautyParams.kBPLipstickBrightnessParam, getQueenParam().basicBeautyRecord.faceBuffingLipstickBrightnessParams);
                }
                queenEngine.setBeautyParam(BeautyParams.kBPBrightenEye, getQueenParam().basicBeautyRecord.faceBuffingBrightenEye); //亮眼[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPBlush, getQueenParam().basicBeautyRecord.faceBuffingBlush); //滤镜美妆：腮红[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPWrinkles, getQueenParam().basicBeautyRecord.faceBuffingWrinklesParam); //去皱纹[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPBrightenFace, getQueenParam().basicBeautyRecord.faceBuffingBrightenFaceParam); //去暗沉[0,1]
            }

            // 美型
            queenEngine.enableBeautyType(BeautyFilterType.kFaceShape, getQueenParam().faceShapeRecord.enableFaceShape, QueenRuntime.sFaceShapeDebug, getQueenParam().faceShapeRecord.faceShapeMode);
            queenEngine.enableBeautyType(BeautyFilterType.kBTAutoFaceShape, getQueenParam().faceShapeRecord.enableAutoFaceShape);//智能美型开关
            if (getQueenParam().faceShapeRecord.enableFaceShape) {
                queenEngine.updateFaceShape(FaceShapeType.typeCutCheek     , getQueenParam().faceShapeRecord.cutCheekParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeCutFace      , getQueenParam().faceShapeRecord.cutFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeThinFace     , getQueenParam().faceShapeRecord.thinFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeLongFace     , getQueenParam().faceShapeRecord.longFaceParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeLowerJaw     , getQueenParam().faceShapeRecord.lowerJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeHigherJaw    , getQueenParam().faceShapeRecord.higherJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeThinJaw      , getQueenParam().faceShapeRecord.thinJawParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeThinMandible , getQueenParam().faceShapeRecord.thinMandibleParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeBigEye       , getQueenParam().faceShapeRecord.bigEyeParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeEyeAngle1    , getQueenParam().faceShapeRecord.eyeAngle1Param * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeCanthus      , getQueenParam().faceShapeRecord.canthusParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeCanthus1     , getQueenParam().faceShapeRecord.canthus1Param * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeEyeAngle2    , getQueenParam().faceShapeRecord.eyeAngle2Param * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeEyeTDAngle   , getQueenParam().faceShapeRecord.eyeTDAngleParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeThinNose     , getQueenParam().faceShapeRecord.thinNoseParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeNosewing     , getQueenParam().faceShapeRecord.nosewingParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeNasalHeight  , getQueenParam().faceShapeRecord.nasalHeightParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeNoseTipHeight, getQueenParam().faceShapeRecord.noseTipHeightParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeMouthWidth   , getQueenParam().faceShapeRecord.mouthWidthParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeMouthSize    , getQueenParam().faceShapeRecord.mouthSizeParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeMouthHigh    , getQueenParam().faceShapeRecord.mouthHighParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typePhiltrum     , getQueenParam().faceShapeRecord.philtrumParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeHairLine     , getQueenParam().faceShapeRecord.hairLineParam * QueenParamWeight.FACE_SHAPE_PARAM);
                queenEngine.updateFaceShape(FaceShapeType.typeSmile        , getQueenParam().faceShapeRecord.smailParam * QueenParamWeight.FACE_SHAPE_PARAM);
            }

            // 美体
            queenEngine.enableBeautyType(BeautyFilterType.kBodyShape, getQueenParam().bodyShapeRecord.enableBodyShape, QueenRuntime.sBodyShapeDebug);
            if (getQueenParam().bodyShapeRecord.enableBodyShape) {
                queenEngine.updateBodyShape(BodyShapeType.kFullBody, getQueenParam().bodyShapeRecord.fullBodyParam);
                queenEngine.updateBodyShape(BodyShapeType.kSmallHead, getQueenParam().bodyShapeRecord.smallHeadParam);
                queenEngine.updateBodyShape(BodyShapeType.kThinLag, getQueenParam().bodyShapeRecord.thinLagParam);
                queenEngine.updateBodyShape(BodyShapeType.kLongLag, getQueenParam().bodyShapeRecord.longLagParam);
                queenEngine.updateBodyShape(BodyShapeType.kLongNeck, getQueenParam().bodyShapeRecord.longNeckParam);
                queenEngine.updateBodyShape(BodyShapeType.kThinWaist, getQueenParam().bodyShapeRecord.thinWaistParam);
                queenEngine.updateBodyShape(BodyShapeType.kEnhanceBreast, getQueenParam().bodyShapeRecord.enhanceBreastParam);
                queenEngine.updateBodyShape(BodyShapeType.kThinArm, getQueenParam().bodyShapeRecord.thinArmParam);
            }

            // 人体区域分割
            queenEngine.enableBeautyType(BeautyFilterType.kBodySegment, getQueenParam().bodyShapeRecord.enableBodySegment);

            // 美妆
            queenEngine.enableBeautyType(BeautyFilterType.kMakeup, getQueenParam().faceMakeupRecord.enableFaceMakeup, QueenRuntime.sFaceMakeupDebug, getQueenParam().faceMakeupRecord.mMakeupMode);
            if (getQueenParam().faceMakeupRecord.enableFaceMakeup) {
                for (int makeupType = 0; makeupType < MakeupType.kMakeupMax; makeupType++) {
                    String makeUpResourcePath = getQueenParam().faceMakeupRecord.makeupResourcePath[makeupType];
                    if (!TextUtils.isEmpty(makeUpResourcePath)) {
                        String[] path = new String[] {makeUpResourcePath};
                        int blendType = getQueenParam().faceMakeupRecord.makeupBlendType[makeupType];
                        float alpha = getQueenParam().faceMakeupRecord.makeupAlpha[makeupType];
                        queenEngine.setMakeupImage(makeupType, path, blendType, 15);
                        queenEngine.setMakeupAlpha(makeupType,
                                alpha * QueenParamWeight.FACE_MAKEUP_ALPHA,
                                (1.0f-alpha) * QueenParamWeight.FACE_MAKEUP_ALPHA);
                    } else {
                        String[] path = new String[] {};
                        queenEngine.setMakeupImage(makeupType, path, BlendType.kBlendNormal, 15);
                    }
                }
            }

            // 滤镜
            queenEngine.enableBeautyType(BeautyFilterType.kLUT, getQueenParam().lutRecord.lutEnable);
            if (getQueenParam().lutRecord.lutEnable) {
                queenEngine.setFilter(getQueenParam().lutRecord.lutPath); //设置滤镜
                queenEngine.setBeautyParam(BeautyParams.kBPLUT, getQueenParam().lutRecord.lutParam * QueenParamWeight.FACE_LUT_PARAM); //滤镜强度
            }

            // 贴纸
            addMaterial(queenEngine, getQueenParam().stickerRecord.stickerEnable, getQueenParam().stickerRecord.stickerPath,
                    QueenParam.StickerRecord.usingStickerPathList);

            // 人脸检测
            switchAlgorithm(queenEngine, getQueenParam().faceDetectRecord, AlgType.kFaceDetect);
            // 人脸属性检测
            getQueenParam().faceAttribDetectRecord.enable = QueenRuntime.sFaceAttribDetectDebug;
            switchAlgorithm(queenEngine, getQueenParam().faceAttribDetectRecord, AlgType.kFaceAttribDetect);
            // 手势
            switchAlgorithm(queenEngine, getQueenParam().gestureRecord, AlgType.kAAiGestureDetect);
            queenEngine.enableDetectPointDebug(AlgType.kAAiGestureDetect, QueenRuntime.sHandDetectDebug);
            // 人体骨骼点检测
            switchAlgorithm(queenEngine, getQueenParam().bodyDetectRecord, AlgType.kBokehAiBodyDetect);
            queenEngine.enableDetectPointDebug(AlgType.kBokehAiBodyDetect, QueenRuntime.sBodyDetectDebug);

            switchAlgorithm(queenEngine, getQueenParam().faceExpressionRecord, AlgType.kFaceExpressionDetect);

            // AR隔空写字
            queenEngine.setArWriting(getQueenParam().sArWritingRecord.enable, getQueenParam().sArWritingRecord.mode);
            queenEngine.enableDetectPointDebug(AlgType.kAAiArWritingDetect, QueenRuntime.sArWritingDetectDebug);

            queenEngine.enableBeautyType(BeautyFilterType.kHairColor, getQueenParam().hairRecord.enable);
            if (getQueenParam().hairRecord.enable) {
                queenEngine.setHairColor(
                  getQueenParam().hairRecord.colorRed,
                  getQueenParam().hairRecord.colorGreen,
                  getQueenParam().hairRecord.colorBlue);
            }

            QueenParam.autoFaceShapeRecord.enable = getQueenParam().faceShapeRecord.enableAutoFaceShape;
            switchAlgorithm(queenEngine, QueenParam.autoFaceShapeRecord, AlgType.kQueenAutoFaceShape);
        }
    }

    private static void addMaterial(QueenEngine queenEngine, boolean functionEnabled, String materialPath, List<String> usingList) {
        if (functionEnabled) {
            boolean hadNotAdded = usingList == null || !usingList.contains(materialPath);
            if (!TextUtils.isEmpty(materialPath) && hadNotAdded) {
                queenEngine.addMaterial(materialPath);
                usingList.add(materialPath);
            }
        }

        if (usingList != null && usingList.size() > 0) {
            Iterator<String> iterator = usingList.iterator();
            while (iterator.hasNext()) {
                String usingStickerPath = iterator.next();
                if (!functionEnabled || !TextUtils.equals(usingStickerPath, materialPath)) {
                    queenEngine.removeMaterial(usingStickerPath);
                    iterator.remove();
                }
            }
        }
    }

    private static void switchAlgorithm(QueenEngine queenEngine, QueenParam.AlgorithmRecord algorithmRecord, int algId) {
        String queenEngineId = String.valueOf(queenEngine.hashCode());
        // TODO: 此处有问题，此处在P40手机上调试发现，engine释放后重新创建，engineHandler是相同的，
        // 待查
        if (algorithmRecord.getAlgorithm() != null
//                && algorithmRecord.getAlgorithm().getEngineHandle() != queenEngine.getEngineHandler()
                    && !algorithmRecord.getAlgorithm().getName().equals(queenEngineId)
        ) {
            algorithmRecord.setAlgorithm(null);
            algorithmRecord.setHasRegistered(false);
        }
        if (algorithmRecord.enable != algorithmRecord.isHasRegistered()) {
            if (algorithmRecord.enable) {
                Algorithm algorithm = new Algorithm(queenEngine.getEngineHandler(), queenEngineId, algId);
                algorithm.registerAlgCallBack(algorithmRecord.getAlgListener());
                algorithmRecord.setHasRegistered(true);
                algorithmRecord.setAlgorithm(algorithm);
            } else {
                if (algorithmRecord.getAlgorithm() != null) {
                    algorithmRecord.getAlgorithm().unRegisterAlgCallBack();
                }
                algorithmRecord.setHasRegistered(false);
                algorithmRecord.setAlgorithm(null);
            }
        }
    }

    public static void relaseQueenParams() {
        QueenParamFactory.Scenes.resetAllScenes();
        setQueenParam(QueenParamFactory.getDefaultScenesParam());
    }

}
