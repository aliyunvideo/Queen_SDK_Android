package com.aliyun.maliang.android.simpleapp.copyparams;

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

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.List;

public class QueenParamProHolder {
    /**
     * 参数加权配置
     */
    private interface QueenParamWeight {
        float FACE_SHAPE_PARAM = 1.0f;
        float FACE_MAKEUP_ALPHA = 1.0f;
        float FACE_LUT_PARAM = 1.0f;
    }

    // NOTE: 此处的QueenParam包名，修改为实际的QueenParam包名
    private static QueenParam sQueenParam;

    public static QueenParam getQueenParam() {
        if (null == sQueenParam) {
            setQueenParam(createBasicBeautyParam());
        }
        return sQueenParam;
    }

    public static void setQueenParam(QueenParam queenParam) {
        sQueenParam = queenParam;
    }

    private static void cacheEngineReference(final QueenEngine engine) {
        WeakReference<QueenEngine> engineWeakRef = QueenRuntime.queenEngienRef;
        if (engineWeakRef != null) {
            QueenEngine engineRef = engineWeakRef.get();
            if (engineRef == null || engineRef.hashCode() != engine.hashCode()) {
                QueenRuntime.queenEngienRef = new WeakReference<>(engine);
            }
        } else {
            QueenRuntime.queenEngienRef = new WeakReference<>(engine);
        }
    }

    public static void writeParamToEngine(Object objEngine) {
        writeParamToEngine(objEngine, false);
    }

    public static void writeParamToEngine(Object objEngine, boolean canUseAsyncAlg) {
        if (null != objEngine) {
            QueenEngine queenEngine = (QueenEngine) objEngine;
            cacheEngineReference(queenEngine);

            queenEngine.enableDetectPointDebug(AlgType.kFaceDetect, QueenRuntime.sFaceDetectDebug);

            queenEngine.setPowerSaving(QueenRuntime.sPowerSaving);

            queenEngine.setGreenScreen(
                    getQueenParam().segmentRecord.enableGreenSegment || getQueenParam().segmentRecord.enableBlueSegment  ? getQueenParam().segmentRecord.greenSegmentBackgroundPath : "",
                    getQueenParam().segmentRecord.enableBlueSegment,
                    getQueenParam().segmentRecord.enableBlueSegment ? getQueenParam().segmentRecord.blueSegmentThreshold : getQueenParam().segmentRecord.greenSegmentThreshold,
                    getQueenParam().segmentRecord.enableBlueSegment ? getQueenParam().segmentRecord.enableBlueSegmentAutoThreshold : getQueenParam().segmentRecord.enableGreenSegmentAutoThreshold,
                    getQueenParam().segmentRecord.greenSegmentBgProcessType
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

            if (getQueenParam().segmentRecord.aiSegmentBackgroundImagePath != null
                    || getQueenParam().segmentRecord.usingAiSegmentBackgroundImagePath != null) {
                // AI抠图设置静态图片背景资源
                setAISegmentBackgroundImage(queenEngine, getQueenParam().segmentRecord);
            } else {
                // AI抠图设置动态图片背景资源
                setAISegmentMaterial(queenEngine, getQueenParam().segmentRecord.aiSegmentBackgroundMaterial,
                        getQueenParam().segmentRecord.usingAiSegmentBackgroundMaterialList);
            }

            // 美颜
            queenEngine.enableBeautyType(BeautyFilterType.kSkinWhiting, getQueenParam().basicBeautyRecord.enableSkinWhiting);//美白开关
            if (!getQueenParam().basicBeautyRecord.enableAutoFiltering && getQueenParam().basicBeautyRecord.enableSkinWhiting) {
                queenEngine.setBeautyParam(BeautyParams.kBPSkinWhitening, getQueenParam().basicBeautyRecord.skinWhitingParam);  //美白 [0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPSkinRed,
                        getQueenParam().basicBeautyRecord.enableSkinRed ? getQueenParam().basicBeautyRecord.skinRedParam : 0.0f);  //红润 [0,1]
            }

            // 马赛克效果
            queenEngine.enableBeautyType(BeautyFilterType.kBTEffectMosaicing, getQueenParam().faceEffectsRecord.enableMosaicing);
            queenEngine.setBeautyParam(BeautyParams.kBPEffects_Mosaicing, getQueenParam().faceEffectsRecord.mosaicingSizeParam);

            queenEngine.enableBeautyType(BeautyFilterType.kHSV, getQueenParam().basicBeautyRecord.enableHSV);
            queenEngine.setBeautyParam(BeautyParams.kBPHSV_SATURATION, getQueenParam().basicBeautyRecord.hsvSaturationParam);  // 饱和度
            queenEngine.setBeautyParam(BeautyParams.kBPHSV_CONTRAST, getQueenParam().basicBeautyRecord.hsvContrastParam);    // 对比度

            queenEngine.enableBeautyType(BeautyFilterType.kSkinBuffing, getQueenParam().basicBeautyRecord.enableSkinBuffing,
                    getQueenParam().basicBeautyRecord.skinBuffingLeverParam);//磨皮开关
            if (!getQueenParam().basicBeautyRecord.enableAutoFiltering && getQueenParam().basicBeautyRecord.enableSkinBuffing) {
                queenEngine.setBeautyParam(BeautyParams.kBPSkinBuffing, getQueenParam().basicBeautyRecord.skinBuffingParam);  //磨皮 [0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPSkinSharpen, getQueenParam().basicBeautyRecord.skinSharpenParam);  //锐化 [0,1]
            }

            queenEngine.enableBeautyType(BeautyFilterType.kFaceBuffing, getQueenParam().basicBeautyRecord.enableFaceBuffing); //高级美颜开关
            if (getQueenParam().basicBeautyRecord.enableFaceBuffing) {
                queenEngine.setBeautyParam(BeautyParams.kBPNasolabialFolds, getQueenParam().basicBeautyRecord.faceBuffingNasolabialFoldsParam); //去法令纹[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPPouch, getQueenParam().basicBeautyRecord.faceBuffingPouchParam); //去眼袋[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPNeck, getQueenParam().basicBeautyRecord.faceBuffingNeck); //去颈纹[0,1]
                queenEngine.setBeautyParam(BeautyParams.kBPForehead, getQueenParam().basicBeautyRecord.faceBuffingForehead); //去抬头纹[0,1]
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
            queenEngine.enableBeautyType(BeautyFilterType.kFaceShape, getQueenParam().faceShapeRecord.enableFaceShape, false, getQueenParam().faceShapeRecord.faceShapeMode);
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
//                queenEngine.updateFaceShape(FaceShapeType.typeEyelid        , getQueenParam().faceShapeRecord.eyelidParam * QueenParamWeight.FACE_SHAPE_PARAM);
            }

            // 美体
            queenEngine.enableBeautyType(BeautyFilterType.kBodyShape, getQueenParam().bodyShapeRecord.enableBodyShape, false);
            if (getQueenParam().bodyShapeRecord.enableBodyShape) {
                queenEngine.updateBodyShape(BodyShapeType.kFullBody, getQueenParam().bodyShapeRecord.fullBodyParam);
                queenEngine.updateBodyShape(BodyShapeType.kSmallHead, getQueenParam().bodyShapeRecord.smallHeadParam);
                queenEngine.updateBodyShape(BodyShapeType.kThinLeg, getQueenParam().bodyShapeRecord.thinLagParam);
                queenEngine.updateBodyShape(BodyShapeType.kLongLeg, getQueenParam().bodyShapeRecord.longLagParam);
                queenEngine.updateBodyShape(BodyShapeType.kLongNeck, getQueenParam().bodyShapeRecord.longNeckParam);
                queenEngine.updateBodyShape(BodyShapeType.kThinWaist, getQueenParam().bodyShapeRecord.thinWaistParam);
                queenEngine.updateBodyShape(BodyShapeType.kEnhanceBreast, getQueenParam().bodyShapeRecord.enhanceBreastParam);
                queenEngine.updateBodyShape(BodyShapeType.kThinArm, getQueenParam().bodyShapeRecord.thinArmParam);
            }

            // 人体区域分割
            queenEngine.enableBeautyType(BeautyFilterType.kBodySegment, getQueenParam().bodyShapeRecord.enableBodySegment);

            // 美妆
            queenEngine.enableBeautyType(BeautyFilterType.kMakeup, getQueenParam().faceMakeupRecord.enableFaceMakeup, false, getQueenParam().faceMakeupRecord.mMakeupMode);
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

            // 人脸检测-人脸框及角度YPR的回调
            switchAlgorithm(queenEngine, getQueenParam().faceDetectRecord, AlgType.kFaceDetect);
            // 人脸属性检测
            getQueenParam().faceAttribDetectRecord.enable = false;
            switchAlgorithm(queenEngine, getQueenParam().faceAttribDetectRecord, AlgType.kFaceAttribDetect);
            // 手势
            switchAlgorithm(queenEngine, getQueenParam().gestureRecord, AlgType.kAAiGestureDetect);
            queenEngine.enableDetectPointDebug(AlgType.kAAiGestureDetect, false);
            // 人体骨骼点检测
            switchAlgorithm(queenEngine, getQueenParam().bodyDetectRecord, AlgType.kBokehAiBodyDetect);
            queenEngine.enableDetectPointDebug(AlgType.kBokehAiBodyDetect, false);

            queenEngine.enableDetectPointDebug(AlgType.kQueenObjectDetect, false);

            switchAlgorithm(queenEngine, getQueenParam().bodyPoseDetectRecord, AlgType.kQueenAIBodySportDetect);
            queenEngine.resetBodySportPoseDetectCount(getQueenParam().bodyPoseDetectRecord.isResetSportCount());
            queenEngine.setBodyPoseDetectType(getQueenParam().bodyPoseDetectRecord.detectType);

            switchAlgorithm(queenEngine, getQueenParam().faceExpressionRecord, AlgType.kFaceExpressionDetect);

            // 专注力检测
            switchAlgorithm(queenEngine, getQueenParam().concentrationRecord, AlgType.kQueenConcentrationDetect);

            // 异常行为检测
            switchAlgorithm(queenEngine, getQueenParam().abnormalActionRecord, AlgType.kQueenAbnormalActionDetect);

            // 活体检测
            switchAlgorithm(queenEngine, getQueenParam().livingHumanDetectRecord, AlgType.kQueenLivingHumanDetect);

            // AR隔空写字
            queenEngine.enableBeautyType(BeautyFilterType.kBTEffectARWriting, getQueenParam().sArWritingRecord.enable,
                    getQueenParam().sArWritingRecord.mode);
//            queenEngine.setArWriting(getQueenParam().sArWritingRecord.enable, getQueenParam().sArWritingRecord.mode);
            queenEngine.enableDetectPointDebug(AlgType.kAAiArWritingDetect, false);
//            queenEngine.cleanScreen4ArWriting();


            queenEngine.enableBeautyType(BeautyFilterType.kHairColor, getQueenParam().hairRecord.enable);
            if (getQueenParam().hairRecord.enable) {
                queenEngine.setHairColor(
                  getQueenParam().hairRecord.colorRed,
                  getQueenParam().hairRecord.colorGreen,
                  getQueenParam().hairRecord.colorBlue);
            }

            getQueenParam().autoFaceShapeRecord.enable = getQueenParam().faceShapeRecord.enableAutoFaceShape;
            switchAlgorithm(queenEngine, getQueenParam().autoFaceShapeRecord, AlgType.kQueenAutoFaceShape);
        }
    }

    private static void setAISegmentBackgroundImage(QueenEngine queenEngine, QueenParam.SegmentRecord segmentRecord) {
        // 判断资源重复
        if (segmentRecord.usingAiSegmentBackgroundImagePath != null
                && segmentRecord.usingAiSegmentBackgroundImagePath.equals(segmentRecord.aiSegmentBackgroundImagePath))
            return;

        if (segmentRecord.aiSegmentBackgroundImagePath == null
                && segmentRecord.usingAiSegmentBackgroundImagePath == null) {
            return;
        }

        queenEngine.setAISegmentBackgroundImage(segmentRecord.aiSegmentBackgroundImagePath, false, true);

        segmentRecord.usingAiSegmentBackgroundImagePath = segmentRecord.aiSegmentBackgroundImagePath;
    }

    private static void setAISegmentMaterial(QueenEngine queenEngine, String materialPath, List<String> usingList) {
        if (!TextUtils.isEmpty(materialPath)) {
            // 避免重复设置
            if (usingList != null && usingList.contains(materialPath)) {
                return;
            }
            queenEngine.addMaterial(materialPath, -1);
            usingList.add(materialPath);
        }

        // 移除掉废弃使用的素材
        if (usingList != null && usingList.size() > 0) {
            Iterator<String> iterator = usingList.iterator();
            while (iterator.hasNext()) {
                String usingStickerPath = iterator.next();
                if (!TextUtils.equals(usingStickerPath, materialPath)) {
                    queenEngine.removeMaterial(usingStickerPath);
                    iterator.remove();
                }
            }
        }
    }

    private static void addMaterial(QueenEngine queenEngine, boolean functionEnabled, String materialPath, List<String> usingList) {
        if (functionEnabled) {
            if (!TextUtils.isEmpty(materialPath)) {
                queenEngine.addMaterial(materialPath);

                boolean hadNotAdded = usingList == null || !usingList.contains(materialPath);
                if (hadNotAdded) {
                    usingList.add(materialPath);
                }
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
        String queenEngineId = queenEngine.hashCodeStr();
        // TODO: 此处有问题，此处在P40手机上调试发现，engine释放后重新创建，engineHandler是相同的，
        // 待查
        if (algorithmRecord.getAlgorithm() != null) {
            Algorithm thisAlgorithm = (Algorithm) (algorithmRecord.getAlgorithm());
            if (!thisAlgorithm.getName().equals(queenEngineId)) {
                algorithmRecord.setAlgorithm(null);
                algorithmRecord.setHasRegistered(false);
            }
        }
        if (algorithmRecord.enable != algorithmRecord.isHasRegistered()) {
            if (algorithmRecord.enable) {
                Algorithm algorithm = new Algorithm(queenEngine.getEngineHandler(), queenEngineId, algId);
                algorithm.setAlgInfo(algorithmRecord.mAlgorithmInfo);
                Object objListener = algorithmRecord.getAlgListener();
                if (objListener == null) {
                    objListener = new Algorithm.OnAlgDetectListener() {
                        @Override
                        public int onAlgDetectFinish(int algId, Object algResult) {
                            return 0;
                        }
                    };
                    algorithmRecord.setAlgListener(objListener);
                }

                algorithm.registerAlgCallBack((Algorithm.OnAlgDetectListener)objListener);
                algorithmRecord.setHasRegistered(true);
                algorithmRecord.setAlgorithm(algorithm);
            } else {
                if (algorithmRecord.getAlgorithm() != null) {
                    Algorithm thisAlgorithm = (Algorithm)algorithmRecord.getAlgorithm();
                    thisAlgorithm.setAlgInfo(algorithmRecord.mAlgorithmInfo);
                    thisAlgorithm.unRegisterAlgCallBack();
                }
                algorithmRecord.setHasRegistered(false);
                algorithmRecord.setAlgorithm(null);
            }
        }
    }

    public static void relaseQueenParams() {

    }

    private static QueenParam createBasicBeautyParam() {
        QueenParam beautyParam = new QueenParam();

        QueenParam.BasicBeautyRecord basicBeautyRecord = new QueenParam.BasicBeautyRecord(0);
        basicBeautyRecord.enableSkinWhiting = true;
        basicBeautyRecord.enableSkinBuffing = true;
        basicBeautyRecord.enableSkinRed = true;
        basicBeautyRecord.enableHSV = true;
        basicBeautyRecord.enableFaceBuffing = true;
        basicBeautyRecord.skinWhitingParam = 0.32f; // 美白
        basicBeautyRecord.skinBuffingParam = 0.72f;  // 磨皮
        basicBeautyRecord.skinSharpenParam = 0.1f;  // 锐化

        beautyParam.basicBeautyRecord = basicBeautyRecord;
        return beautyParam;
    }
}
