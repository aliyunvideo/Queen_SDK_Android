package com.tencent.trtc.thirdbeauty;

import com.aliyun.android.libqueen.models.BlendType;
import com.aliyun.android.libqueen.models.MakeupType;

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
        public boolean enableSkinWhiting = true;
        public float skinWhitingParam = 0.3f; // 美白[0,1]

        public boolean enableSkinBuffing = true;
        public float skinBuffingParam = 0.6f; // 磨皮[0,1]
        public float skinSharpenParam = 0.3f; // 锐化[0,1]

        public boolean enableFaceBuffing = true;
        public float faceBuffingPouchParam = 0.6f; //去眼袋[0,1]
        public float faceBuffingNasolabialFoldsParam = 0.6f; //去法令纹[0,1]
        public float faceBuffingWhiteTeeth = 0.0f; //白牙[0,1]
        public float faceBuffingLipstick = 0.0f; // 滤镜美妆：口红[0,1]
        public float faceBuffingBlush = 0.0f; // 滤镜美妆：腮红[0,1]

        @Override
        public String toString() {
            return "BasicBeautyRecord{" +
                    "enableSkinWhiting=" + enableSkinWhiting +
                    ", skinWhitingParam=" + skinWhitingParam +
                    ", enableSkinBuffing=" + enableSkinBuffing +
                    ", skinBuffingParam=" + skinBuffingParam +
                    ", skinSharpenParam=" + skinSharpenParam +
                    ", enableFaceBuffing=" + enableFaceBuffing +
                    ", faceBuffingPouchParam=" + faceBuffingPouchParam +
                    ", faceBuffingNasolabialFoldsParam=" + faceBuffingNasolabialFoldsParam +
                    ", faceBuffingWhiteTeeth=" + faceBuffingWhiteTeeth +
                    ", faceBuffingLipstick=" + faceBuffingLipstick +
                    ", faceBuffingBlush=" + faceBuffingBlush +
                    '}';
        }
    }
    public BasicBeautyRecord basicBeautyRecord = new BasicBeautyRecord();

    public static class LUTRecord {
        public boolean lutEnable = false;
        public String lutPath = "lookups/怀旧色卡.png"; // 滤镜色卡路径
        public float lutParam = 0.8f; // 滤镜强度[0,1]

        @Override
        public String toString() {
            return "LUTRecord{" +
                    "lutEnable=" + lutEnable +
                    ", lutPath='" + lutPath + '\'' +
                    ", lutParam=" + lutParam +
                    '}';
        }
    }
    public LUTRecord lutRecord = new LUTRecord();

    public static class StickerRecord {
        public boolean stickerEnable = false;
        public String stickerPath = "sticker/tuanzhang"; // 贴纸路径
        public List<String> usingStickerPathList = new ArrayList<>(); //设置新的贴纸之后需要去掉旧的资源，这里做备份

        @Override
        public String toString() {
            return "StickerRecord{" +
                    "stickerEnable=" + stickerEnable +
                    ", stickerPath='" + stickerPath + '\'' +
                    '}';
        }
    }
    public StickerRecord stickerRecord = new StickerRecord();

    public static class FaceShapeRecord {
        public boolean enableFaceShape = false;

        public float cutCheekParam      = 0.0f; //颧骨[0,1]
        public float cutFaceParam       = 0.0f; //削脸[0,1]
        public float thinFaceParam      = 0.0f; //瘦脸[0,1]
        public float longFaceParam      = 0.0f; //脸长[0,1]
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
        public float thinNoseParam      = 0.0f; //瘦鼻[0,1]
        public float nosewingParam      = 0.0f; //鼻翼[0,1]
        public float nasalHeightParam   = 0.0f; //鼻长[-1,1]
        public float noseTipHeightParam = 0.0f; //鼻头长[-1,1]
        public float mouthWidthParam    = 0.0f; //唇宽[-1,1]
        public float mouthSizeParam     = 0.0f; //嘴唇大小[-1,1]
        public float mouthHighParam     = 0.0f; //唇高[-1,1]
        public float philtrumParam      = 0.0f; //人中[-1,1]

        public static float formatFaceShapeParam(int param) {
            return param / 100.0f;
        }
        public static float formatReverseParam(int param) {
            return param / 100.0f * -1f;
        }

        @Override
        public String toString() {
            return "FaceShapeRecord{" +
                    "enableFaceShape=" + enableFaceShape +
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
                    '}';
        }
    }
    public static FaceShapeRecord sNoneFaceShapeRecord = new FaceShapeRecord();
    static {
        sNoneFaceShapeRecord.enableFaceShape = false;
    }
    public static FaceShapeRecord sCustomFaceShapeRecord = new FaceShapeRecord();
    static {
        sCustomFaceShapeRecord.enableFaceShape = true;
        sCustomFaceShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(33);
        sCustomFaceShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(22);
        sCustomFaceShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(17);
        sCustomFaceShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(7);
        sCustomFaceShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(33);
        sCustomFaceShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(30);
        sCustomFaceShapeRecord.noseTipHeightParam = FaceShapeRecord.formatFaceShapeParam(10);
        sCustomFaceShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(18);
        sCustomFaceShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sCustomFaceShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }
    // 优雅
    public static FaceShapeRecord sGraceFaceShapeRecord = new FaceShapeRecord();
    static {
        sGraceFaceShapeRecord.enableFaceShape = true;
        sGraceFaceShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(33);
        sGraceFaceShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(22);
        sGraceFaceShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(17);
        sGraceFaceShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(7);
        sGraceFaceShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(33);
        sGraceFaceShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sGraceFaceShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(18);
        sGraceFaceShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sGraceFaceShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }
    // 精致
    public static FaceShapeRecord sDelicateShapeRecord = new FaceShapeRecord();
    static {
        sDelicateShapeRecord.enableFaceShape = true;
        sDelicateShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(6);
        sDelicateShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(22);
        sDelicateShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(10);
        sDelicateShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(33);
        sDelicateShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(0);
        sDelicateShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sDelicateShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(0);
        sDelicateShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sDelicateShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }
    // 网红
    public static FaceShapeRecord sWangHongShapeRecord = new FaceShapeRecord();
    static {
        sWangHongShapeRecord.enableFaceShape = true;
        sWangHongShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(33);
        sWangHongShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(5);
        sWangHongShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(2);
        sWangHongShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(2);
        sWangHongShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(16);
        sWangHongShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sWangHongShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(12);
        sWangHongShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sWangHongShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }
    // 可爱
    public static FaceShapeRecord sCuteShapeRecord = new FaceShapeRecord();
    static {
        sCuteShapeRecord.enableFaceShape = true;
        sCuteShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(17);
        sCuteShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(22);
        sCuteShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(16);
        sCuteShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(-3);
        sCuteShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(33);
        sCuteShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sCuteShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(-8);
        sCuteShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sCuteShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }
    // 婴儿
    public static FaceShapeRecord sBabyFaceShapeRecord = new FaceShapeRecord();
    static {
        sBabyFaceShapeRecord.enableFaceShape = true;
        sBabyFaceShapeRecord.cutFaceParam = FaceShapeRecord.formatFaceShapeParam(15);
        sBabyFaceShapeRecord.thinFaceParam = FaceShapeRecord.formatFaceShapeParam(6);
        sBabyFaceShapeRecord.longFaceParam = FaceShapeRecord.formatReverseParam(27);
        sBabyFaceShapeRecord.lowerJawParam = FaceShapeRecord.formatReverseParam(-10);
        sBabyFaceShapeRecord.bigEyeParam = FaceShapeRecord.formatFaceShapeParam(16);
        sBabyFaceShapeRecord.thinNoseParam = FaceShapeRecord.formatFaceShapeParam(0);
        sBabyFaceShapeRecord.mouthWidthParam = FaceShapeRecord.formatReverseParam(-8);
        sBabyFaceShapeRecord.thinMandibleParam = FaceShapeRecord.formatFaceShapeParam(0);
        sBabyFaceShapeRecord.cutCheekParam = FaceShapeRecord.formatFaceShapeParam(0);
    }
    public FaceShapeRecord faceShapeRecord = sNoneFaceShapeRecord;

    public static class FaceMakeupRecord {
        public boolean enableFaceMakeup = false;

        public String[] makeupResourcePath = new String[MakeupType.kMakeupMax];
        public int[] makeupBlendType = new int[MakeupType.kMakeupMax];
        public float[] makeupAlpha = new float[MakeupType.kMakeupMax];

        public FaceMakeupRecord() {
            for (int i = 0; i < MakeupType.kMakeupMax; i++) {
                makeupAlpha[i] = 0.6f;
            }
            makeupBlendType[MakeupType.kMakeupMouth] = BlendType.kBlendSoftLight;
            makeupBlendType[MakeupType.kMakeupHighlight] = BlendType.kBlendOverlay;

//            makeupResourcePath[MakeupType.kMakeupWhole] = "makeup/梅子妆.png";
//            makeupResourcePath[MakeupType.kMakeupMouth] = "makeup/mouth.png";
//            makeupResourcePath[MakeupType.kMakeupBlush] = "makeup/blush_黛紫腮红.png";
//            makeupResourcePath[MakeupType.kMakeupEyeball] = "makeup/eyeball.png";
//            makeupResourcePath[MakeupType.kMakeupEyeBrow] = "makeup/eye_brow.png";
//            makeupResourcePath[MakeupType.kMakeupHighlight] = "makeup/highlight.png";
        }

        @Override
        public String toString() {
            return "FaceMakeupRecord{" +
                    "enableFaceMakeup=" + enableFaceMakeup +
                    ", makeupResourcePath=" + Arrays.toString(makeupResourcePath) +
                    ", makeupBlendType=" + Arrays.toString(makeupBlendType) +
                    ", makeupAlpha=" + Arrays.toString(makeupAlpha) +
                    '}';
        }
    }
    public FaceMakeupRecord faceMakeupRecord = new FaceMakeupRecord();

    public String serialize() {
        return "";
    }

    public void deserialize() {

    }

    @Override
    public String toString() {
        return "QueenParam{" +
                "basicBeautyRecord=" + basicBeautyRecord +
                ", lutRecord=" + lutRecord +
                ", stickerRecord=" + stickerRecord +
                ", faceShapeRecord=" + faceShapeRecord +
                ", faceMakeupRecord=" + faceMakeupRecord +
                '}';
    }
}
