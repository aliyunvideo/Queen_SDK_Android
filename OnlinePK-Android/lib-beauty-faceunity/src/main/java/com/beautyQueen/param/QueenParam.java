package com.beautyQueen.param;

import com.aliyun.android.libqueen.Algorithm;
import com.aliyun.android.libqueen.models.BackgroundProcessType;
import com.aliyun.android.libqueen.models.BeautyFilterMode;
import com.aliyun.android.libqueen.models.BlendType;
import com.aliyun.android.libqueen.models.MakeupType;
import com.aliyun.android.libqueen.models.SegmentPerformanceMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 默认参数
 */
public class QueenParam {
    public static int deFormatParam(float param) {
        return (int) (param * 100);
    }
    public static float formatParam(int param) {
        return param / 100.0f;
    }
    public static class BasicBeautyRecord {
        public boolean enableAutoFiltering = false;
        
        public boolean enableSkinWhiting = false;
        public float skinWhitingParam = 0.3f; // 美白[0,1]
        public boolean enableSkinRed = false;
        public float skinRedParam = 0.3f; // 红润[0,1]

        public boolean enableSkinBuffing = false;
        public float skinBuffingParam = 0.6f; // 磨皮[0,1]
        public float skinSharpenParam = 0.2f; // 锐化[0,1]
        public int skinBuffingLeverParam = BeautyFilterMode.kBMSkinBuffing_Natural;  // 磨皮等级，高保真，磨皮不重

        public boolean enableFaceBuffing = false;
        public float faceBuffingPouchParam = 0.6f; //去眼袋[0,1]
        public float faceBuffingNasolabialFoldsParam = 0.6f; //去法令纹[0,1]
        public float faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
        public float faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]
        public float faceBuffingWhiteTeeth = 0.2f; //白牙[0,1]
        public float faceBuffingBrightenEye = 0.2f; //亮眼[0,1]
        public float faceBuffingLipstick = 0.15f; // 滤镜美妆：口红[0,1]
        public boolean enableFaceBuffingLipstick = false; // 滤镜美妆：口红开关
        public float faceBuffingBlush = 0.15f; // 滤镜美妆：腮红[0,1]

        public float faceBuffingLipstickColorParams = 0.0f; //滤镜美妆：口红色相[-0.5,0.5]
        public float faceBuffingLipstickGlossParams = 0.0f; //滤镜美妆：口红饱和度[0,1]
        public float faceBuffingLipstickBrightnessParams = 0.0f; //滤镜美妆：口红明度[0,1]


        public void clear() {
            skinWhitingParam = 0f; // 美白[0,1]
            skinRedParam = 0f; // 红润[0,1]
            skinBuffingParam = 0f; // 磨皮[0,1]
            skinSharpenParam = 0f; // 锐化[0,1]
            faceBuffingPouchParam = 0f; //去眼袋[0,1]
            faceBuffingNasolabialFoldsParam = 0f; //去法令纹[0,1]
            faceBuffingWhiteTeeth = 0f; //白牙[0,1]
            faceBuffingBrightenEye = 0f; //亮眼[0,1]
            faceBuffingLipstick = 0f; // 滤镜美妆：口红[0,1]
            faceBuffingBlush = 0f; // 滤镜美妆：腮红[0,1]
            faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
            faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]
        }

        @Override
        public String toString() {
            return "BasicBeautyRecord{\n" +
                    "enableAutoFiltering=" + enableAutoFiltering +
                    ",\nenableSkinWhiting=" + enableSkinWhiting +
                    ",\nskinWhitingParam=" + skinWhitingParam +
                    ",\nenableSkinRed=" + enableSkinRed +
                    ", skinRedParam=" + skinRedParam +
                    ", enableSkinBuffing=" + enableSkinBuffing +
                    ", skinBuffingLeverParam=" + skinBuffingLeverParam +
                    ", skinBuffingParam=" + skinBuffingParam +
                    ", skinSharpenParam=" + skinSharpenParam +
                    ", enableFaceBuffing=" + enableFaceBuffing +
                    ", faceBuffingPouchParam=" + faceBuffingPouchParam +
                    ", faceBuffingNasolabialFoldsParam=" + faceBuffingNasolabialFoldsParam +
                    ", faceBuffingWrinklesParam=" + faceBuffingWrinklesParam +
                    ", faceBuffingBrightenFaceParam=" + faceBuffingBrightenFaceParam +
                    ", faceBuffingWhiteTeeth=" + faceBuffingWhiteTeeth +
                    ", faceBuffingBrightenEye=" + faceBuffingBrightenEye +
                    ", faceBuffingLipstick=" + faceBuffingLipstick +
                    ", enableFaceBuffingLipstick=" + enableFaceBuffingLipstick +
                    ", faceBuffingBlush=" + faceBuffingBlush +
                    ", faceBuffingLipstickColorParams=" + faceBuffingLipstickColorParams +
                    ", faceBuffingLipstickGlossParams=" + faceBuffingLipstickGlossParams +
                    ", faceBuffingLipstickBrightnessParams=" + faceBuffingLipstickBrightnessParams +
                    "\n}";
        }
    }
    public BasicBeautyRecord basicBeautyRecord = new BasicBeautyRecord();

    public static class LUTRecord {
        public boolean lutEnable = false;
        public String lutPath; // 滤镜色卡路径
        public float lutParam = 0.8f; // 滤镜强度[0,1]

        @Override
        public String toString() {
            return "LUTRecord{\n" +
                    "lutEnable=" + lutEnable +
                    ",\nlutPath='" + lutPath + '\'' +
                    ",\nlutParam=" + lutParam +
                    "\n}";
        }
    }
    public LUTRecord lutRecord = new LUTRecord();

    public static class StickerRecord {
        public boolean stickerEnable = false;
        public String stickerPath; // 贴纸路径
        public static List<String> usingStickerPathList = new ArrayList<>(); //设置新的贴纸之后需要去掉旧的资源，这里做备份

        @Override
        public String toString() {
            return "StickerRecord{\n" +
                    " stickerEnable=" + stickerEnable +
                    ",\n stickerPath='" + stickerPath +
                    "\n}";
        }
    }
    public StickerRecord stickerRecord = new StickerRecord();

    public static class FaceShapeRecord {
        public boolean enableFaceShape = false;
        public boolean enableAutoFaceShape = false;

        public int faceShapeMode = BeautyFilterMode.kBMFaceShape_Main;

        public float cutCheekParam      = 0.0f; //颧骨[0,1]
        public float cutFaceParam       = 0.0f; //削脸[0,1]
        public float thinFaceParam      = 0.0f; //瘦脸[0,1]
        public float longFaceParam      = 0.0f; //脸长[-1,1]
        public float lowerJawParam      = 0.0f; //下巴缩短[-1,1]
        public float higherJawParam     = 0.0f; //下巴拉长[-1,1]
        public float thinJawParam       = 0.0f; //瘦下巴[0,1]
        public float thinMandibleParam  = 0.0f; //瘦下颌[0,1]
        public float bigEyeParam        = 0.0f; //大眼[0,1]
        public float eyeAngle1Param     = 0.0f; //眼角1[0,1]
        public float canthusParam       = 0.0f; //眼距[-1,1]
        public float canthus1Param      = 0.0f; //拉宽眼距[-1,1]
        public float eyeAngle2Param     = 0.0f; //眼角2[-1,1]
        public float eyeTDAngleParam    = 0.0f; //眼睛高度[-1,1]
        public float thinNoseParam      = 0.0f; //瘦鼻[-1,1]
        public float nosewingParam      = 0.0f; //鼻翼[-1,1]
        public float nasalHeightParam   = 0.0f; //鼻长[-1,1]
        public float noseTipHeightParam = 0.0f; //鼻头长[-1,1]
        public float mouthWidthParam    = 0.0f; //唇宽[-1,1]
        public float mouthSizeParam     = 0.0f; //嘴唇大小[-1,1]
        public float mouthHighParam     = 0.0f; //唇高[-1,1]
        public float philtrumParam      = 0.0f; //人中[-1,1]
        public float hairLineParam      = 0.0f; //发际线[-1,1]
        public float smailParam         = 0.0f; //嘴角上扬(微笑)[0,1]

        public static float formatFaceShapeParam(int param) {
            return param / 100.0f;
        }
        public static float formatReverseParam(int param) {
            return param / 100.0f * -1f;
        }

        @Override
        public String toString() {
            return "FaceShapeRecord{\n" +
                    "enableFaceShape=" + enableFaceShape +
                    ",\nfaceShapeMode=" + faceShapeMode +
                    ", cutCheekParam=" + cutCheekParam +
                    ", cutFaceParam=" + cutFaceParam +
                    ", thinFaceParam=" + thinFaceParam +
                    ", longFaceParam=" + longFaceParam +
                    ", lowerJawParam=" + lowerJawParam +
                    ", higherJawParam=" + higherJawParam +
                    ", thinJawParam=" + thinJawParam +
                    ", thinMandibleParam=" + thinMandibleParam +
                    ", bigEyeParam=" + bigEyeParam +
                    ", eyeAngle1Param=" + eyeAngle1Param +
                    ", canthusParam=" + canthusParam +
                    ", canthus1Param=" + canthus1Param +
                    ", eyeAngle2Param=" + eyeAngle2Param +
                    ", eyeTDAngleParam=" + eyeTDAngleParam +
                    ", thinNoseParam=" + thinNoseParam +
                    ", nosewingParam=" + nosewingParam +
                    ", nasalHeightParam=" + nasalHeightParam +
                    ", noseTipHeightParam=" + noseTipHeightParam +
                    ", mouthWidthParam=" + mouthWidthParam +
                    ", mouthSizeParam=" + mouthSizeParam +
                    ", mouthHighParam=" + mouthHighParam +
                    ", philtrumParam=" + philtrumParam +
                    ", hairLineParam=" + hairLineParam +
                    ", smailParam=" + smailParam +
                    "\n}";
        }
    }
    public FaceShapeRecord faceShapeRecord = new FaceShapeRecord();

    public static class BodyShapeRecord {
        public boolean enableBodyShape = false;
        public boolean enableBodySegment = false;

        public float fullBodyParam      = 0.0f; // 瘦全身[-1,1]
        public float longLagParam      = 0.0f; // 长腿[-1,1]
        public float smallHeadParam      = 0.0f; // 小头[-1,1]
        public float thinLagParam      = 0.0f; // 瘦腿[-1,1]
        public float longNeckParam      = 0.0f; // 脖子[-1,1]
        public float thinWaistParam      = 0.0f; // 瘦腰[-1,1]
        public float enhanceBreastParam      = 0.0f; // 丰胸[-1,1]
        public float thinArmParam      = 0.0f; // 手臂[-1,1]

        @Override
        public String toString() {
            return "BodyShapeRecord{" +
                    "enableBodyShape=" + enableBodyShape +
                    ", enableBodySegment=" + enableBodySegment +
                    ", fullBodyParam=" + fullBodyParam +
                    ", longLagParam=" + longLagParam +
                    ", smallHeadParam=" + smallHeadParam +
                    ", thinLagParam=" + thinLagParam +
                    ", longNeckParam=" + longNeckParam +
                    ", thinWaistParam=" + thinWaistParam +
                    ", enhanceBreastParam=" + enhanceBreastParam +
                    ", thinArmParam=" + thinArmParam +
                    '}';
        }
    }
    public BodyShapeRecord bodyShapeRecord = new BodyShapeRecord();

    public static class FaceMakeupRecord {
        public boolean enableFaceMakeup = false;

        public int composeMakeupItemId = -1;
        public String[] makeupResourcePath = new String[MakeupType.kMakeupMax+1];   // TODO: 此处新增了卧蚕的处理
        public int[] makeupBlendType = new int[MakeupType.kMakeupMax];
        public float[] makeupAlpha = new float[MakeupType.kMakeupMax];
        public int mMakeupMode = BeautyFilterMode.kBMFaceMakeup_Baseline;

        public FaceMakeupRecord() {
            for (int i = 0; i < MakeupType.kMakeupMax; i++) {
                makeupAlpha[i] = 0.5f;
            }
            makeupAlpha[MakeupType.kMakeupWhole] = 0.85f;
            makeupAlpha[MakeupType.kMakeupBlush] = 0.5f;
            makeupAlpha[MakeupType.kMakeupEyeShadow] = 0.8f;
            makeupAlpha[MakeupType.kMakeupEyeliner] = 0.75f;
            makeupAlpha[MakeupType.kMakeupEyelash] = 0.75f;
            makeupAlpha[MakeupType.kMakeupEyeBrow] = 0.3f;
            makeupAlpha[MakeupType.kMakeupMouth] = 0.8f;
            makeupAlpha[MakeupType.kMakeupHighlight] = 0.9f;

            makeupBlendType[MakeupType.kMakeupMouth] = BlendType.kBlendLabMix;
            makeupBlendType[MakeupType.kMakeupEyeBrow] = BlendType.kBlendLabMix;
            makeupBlendType[MakeupType.kMakeupBlush] = BlendType.kBlendLabMix;
            makeupBlendType[MakeupType.kMakeupHighlight] = BlendType.kBlendOverlay;
            makeupBlendType[MakeupType.kMakeupWocan] = BlendType.kBlendCurve;
            makeupBlendType[MakeupType.kMakeupEyeball] = BlendType.kBlendLighten;
        }

        @Override
        public String toString() {
            return "FaceMakeupRecord{\n" +
                    " enableFaceMakeup=" + enableFaceMakeup +
                    ",\n makeupMode=" + mMakeupMode +
                    ",\n makeupBlendType=" + Arrays.toString(makeupBlendType) +
                    ",\n makeupAlpha=" + Arrays.toString(makeupAlpha) +
                    ",\n makeupResourcePath=" + Arrays.toString(makeupResourcePath) +
                    "\n}";
        }
    }
    public FaceMakeupRecord faceMakeupRecord = new FaceMakeupRecord();

    public static class SegmentRecord {
        public boolean enableGreenSegment = false;
        public boolean enableBlueSegment = false;
        public String greenSegmentBackgroundPath = "background/xiaomanyao.jpeg";
//        public String greenSegmentBackgroundPath = "background/mamba_p.jpg";
        public float greenSegmentThreshold = 1.0f;
        public float blueSegmentThreshold = 1.0f;
        public boolean enableGreenSegmentAutoThreshold = true;
        public boolean enableBlueSegmentAutoThreshold = true;
        private static final float MIN_THRESHOLD = 1.0f;
        private static final float MAX_THRESHOLD = 10.0f;

        public boolean enableAiSegment = false;
        public boolean aiSegmentAsync = false;
        public int aiSegmentForegroundPadding = 0;
        @BackgroundProcessType
        public int backgroundProcessType = BackgroundProcessType.kBackgroundBlur;
        @SegmentPerformanceMode
        public static int segmentPerformanceMode = SegmentPerformanceMode.Auto;

        public String aiSegmentBackgroundPath;
        public List<String> usingAiSegmentBackgroundPathList = new ArrayList<>(); //设置新的资源之后需要去掉旧的资源，这里做备份

        public static float formatThresholdParam(int threshold) {
            return MIN_THRESHOLD + (MAX_THRESHOLD-MIN_THRESHOLD) * threshold / 100.0f;
        }

        public static int deFormatThresholdParam(float threshold) {
            return (int) ((threshold - MIN_THRESHOLD) * 100.0f / (MAX_THRESHOLD - MIN_THRESHOLD));
        }

        public static int formatForegroundPaddingParam(int foregroundPadding) {
            return (int) (15.0 * foregroundPadding / 100);
        }

        public static int deFormatForegroundPaddingParam(float foregroundPadding) {
            return (int) (foregroundPadding * 100.0f / 15);
        }

        @Override
        public String toString() {
            return "SegmentRecord{\n" +
                    "enableGreenSegment=" + enableGreenSegment +
                    ", enableBlueSegment=" + enableBlueSegment +
                    ", greenSegmentBackgroundPath='" + greenSegmentBackgroundPath + '\'' +
                    ", greenSegmentThreshold=" + greenSegmentThreshold +
                    ", blueSegmentThreshold=" + blueSegmentThreshold +
                    ", enableGreenSegmentAutoThreshold=" + enableGreenSegmentAutoThreshold +
                    ", enableBlueSegmentAutoThreshold=" + enableBlueSegmentAutoThreshold +
                    ", enableAiSegment=" + enableAiSegment +
                    ", aiSegmentAsync=" + aiSegmentAsync +
                    ", aiSegmentForegroundPadding=" + aiSegmentForegroundPadding +
                    ", backgroundProcessType=" + backgroundProcessType +
                    ", segmentPerformanceMode=" + segmentPerformanceMode +
                    ", aiSegmentBackgroundPath='" + aiSegmentBackgroundPath + '\'' +
                    ", usingAiSegmentBackgroundPathList=" + usingAiSegmentBackgroundPathList +
                    "\n}";
        }
    }
    public SegmentRecord segmentRecord = new SegmentRecord();

    public static class HairRecord {
        public boolean enable = false;
        public float colorRed = 0.0f;
        public float colorGreen = 0.0f;
        public float colorBlue = 0.0f;
    }
    public HairRecord hairRecord = new HairRecord();

    // 算法回调结果
    public static AlgorithmRecord faceDetectRecord = new AlgorithmRecord();
    public static AlgorithmRecord gestureRecord = new AlgorithmRecord();
    public static AlgorithmRecord bodyDetectRecord = new AlgorithmRecord();
    public static AlgorithmRecord faceAttribDetectRecord = new AlgorithmRecord();
    public static AlgorithmRecord faceExpressionRecord = new AlgorithmRecord();
    public static AlgorithmRecord autoFaceShapeRecord = new AlgorithmRecord();

    public static class AlgorithmRecord {
        public boolean enable = false;
        private boolean mHasRegistered = false;
        private Algorithm mAlgorithm = null;
        protected Algorithm.OnAlgDetectListener mAlgListener = null;
        public boolean isHasRegistered() {
            return mHasRegistered;
        }
        public void setHasRegistered(boolean hasRegistered) {
            this.mHasRegistered = hasRegistered;
        }
        public void setAlgorithm(Algorithm algorithm) {
            this.mAlgorithm = algorithm;
        }
        public Algorithm getAlgorithm() {
            return mAlgorithm;
        }
        public void setAlgListener(Algorithm.OnAlgDetectListener algListener) {
            this.mAlgListener = algListener;
        }
        public Algorithm.OnAlgDetectListener getAlgListener() {
            if (null == mAlgListener) {
                mAlgListener = new Algorithm.OnAlgDetectListener() {
                    @Override
                    public int onAlgDetectFinish(int algId, Object algResult) {
                        return 0;
                    }
                };
            }
            return mAlgListener;
        }
    }

    @Override
    public String toString() {
        return basicBeautyRecord.toString() +
                "\n=======================\n" + faceShapeRecord.toString() +
                "\n=======================\n" + faceMakeupRecord.toString() +
                "\n=======================\n" + lutRecord.toString() +
                "\n=======================\n" + stickerRecord.toString() +
                "\n=======================\n" + bodyShapeRecord.toString() +
                "\n=======================\n" + segmentRecord.toString()
                ;
    }

    public static ArWritingRecord sArWritingRecord = new ArWritingRecord();
    public static class ArWritingRecord {
        public boolean enable = false;
        public int mode = 1;
    }
}
