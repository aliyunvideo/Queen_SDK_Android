package com.aliyun.maliang.android.simpleapp;

import com.taobao.android.libqueen.models.BlendType;
import com.taobao.android.libqueen.models.MakeupType;

import static com.aliyun.maliang.android.simpleapp.QueenParamHolder.getQueenParam;

public class OnModeStyleAction {

    // TODO: 临时组合模式,先hardcode资源名
    private static final String LUT_1_PATH = "lookups/lookup_1.png";
    private static final String LUT_27_PATH = "lookups/lookup_27.png";
    private static final String LUT_8_PATH = "lookups/lookup_8.png";
    private static final String LUT_5_PATH = "lookups/lookup_5.png";
    private static final String LUT_10_PATH = "lookups/lookup_10.png";

    private static final String FACE_MAKEUP_TYPE_MITAO_PATH = "makeup/mitao.png";
    private static final String FACE_MAKEUP_TYPE_YOUYA_PATH = "makeup/youya.png";
    private static final String FACE_MAKEUP_TYPE_YUANQISHAONV_PATH = "makeup/yuanqishaonv.png";

    public static void doAcitonBase() {
        // 全局都要
        getQueenParam().basicBeautyRecord.enableFaceBuffing = true;
        getQueenParam().basicBeautyRecord.faceBuffingPouchParam = 0.6f; //去眼袋[0,1]
        getQueenParam().basicBeautyRecord.faceBuffingNasolabialFoldsParam = 0.6f; //去法令纹[0,1]
        getQueenParam().basicBeautyRecord.faceBuffingWhiteTeeth = 0.2f; //白牙[0,1]
        getQueenParam().basicBeautyRecord.faceBuffingBrightenEye = 0.2f; //亮眼[0,1]
        getQueenParam().basicBeautyRecord.faceBuffingLipstick = 0.15f; // 滤镜美妆：口红[0,1]
        getQueenParam().basicBeautyRecord.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
        getQueenParam().basicBeautyRecord.faceBuffingBlush = 0.15f; // 滤镜美妆：腮红[0,1]
    }

    // 美颜模式-基础
    public static void doActionForSimple() {
        doAcitonBase();

        getQueenParam().basicBeautyRecord.enableSkinWhiting = true;
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinWhitingParam = 0.15f; // 美白
        getQueenParam().basicBeautyRecord.skinBuffingParam = 0.48f;  // 磨皮
        getQueenParam().basicBeautyRecord.skinSharpenParam = 0.1f;  // 锐化
        getQueenParam().basicBeautyRecord.skinRedParam = 0.1f;      // 红润
        // 滤镜1
        getQueenParam().lutRecord.lutEnable = true;
        getQueenParam().lutRecord.lutPath = LUT_1_PATH;

        // 美型
        getQueenParam().faceShapeRecord.enableFaceShape = false;

        // 美妆
        getQueenParam().faceMakeupRecord.enableFaceMakeup = false;
        // 贴纸
        getQueenParam().stickerRecord.stickerEnable = false;
    }

    // 美颜模式-流行
    public static void doActionForFashion() {
        doAcitonBase();

        // 基础
        getQueenParam().basicBeautyRecord.enableSkinWhiting = true;
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinWhitingParam = 0.16f; // 美白
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinBuffingParam = 0.58f;  // 磨皮
        getQueenParam().basicBeautyRecord.skinSharpenParam = 0.12f;  // 锐化
        getQueenParam().basicBeautyRecord.skinRedParam = 0.1f;      // 红润
        // 滤镜1
        getQueenParam().lutRecord.lutEnable = true;
        getQueenParam().lutRecord.lutPath = LUT_27_PATH;
        // 美型
        getQueenParam().faceShapeRecord.enableFaceShape = true;
        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;

        // 美妆
        getQueenParam().faceMakeupRecord.enableFaceMakeup = false;
        // 贴纸
        getQueenParam().stickerRecord.stickerEnable = false;
    }

    // 美颜模式-魅惑
    public static void doActionForMakeup() {
        doAcitonBase();

        getQueenParam().basicBeautyRecord.enableSkinWhiting = true;
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinWhitingParam = 0.16f; // 美白
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinBuffingParam = 0.58f;  // 磨皮
        getQueenParam().basicBeautyRecord.skinSharpenParam = 0.12f;  // 锐化
        getQueenParam().basicBeautyRecord.skinRedParam = 0.1f;      // 红润
        // 滤镜8
        getQueenParam().lutRecord.lutEnable = true;
        getQueenParam().lutRecord.lutPath = LUT_8_PATH;
        // 美型
        getQueenParam().faceShapeRecord.enableFaceShape = true;
        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;
        // 美妆
        getQueenParam().faceMakeupRecord.enableFaceMakeup = true;
        getQueenParam().faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_MITAO_PATH;
        getQueenParam().faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendNormal;

        // 贴纸
        getQueenParam().stickerRecord.stickerEnable = false;
    }

    // 美颜模式-可爱
    public static void doActionForKEAI() {
        doAcitonBase();

        getQueenParam().basicBeautyRecord.enableSkinWhiting = true;
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinWhitingParam = 0.16f; // 美白
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinBuffingParam = 0.58f;  // 磨皮
        getQueenParam().basicBeautyRecord.skinSharpenParam = 0.12f;  // 锐化
        getQueenParam().basicBeautyRecord.skinRedParam = 0.1f;      // 红润
        // 滤镜10
        getQueenParam().lutRecord.lutEnable = true;
        getQueenParam().lutRecord.lutPath = LUT_5_PATH;
        // 美型
        getQueenParam().faceShapeRecord.enableFaceShape = true;
        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;
        // 美妆
        getQueenParam().faceMakeupRecord.enableFaceMakeup = true;
        getQueenParam().faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_YOUYA_PATH;
        getQueenParam().faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendNormal;
        // 贴纸12
        getQueenParam().stickerRecord.stickerEnable = true;
        getQueenParam().stickerRecord.stickerPath = "sticker/12";

    }

    // 美颜模式-少女
    public static void doAcitonForSHAONV() {
        doAcitonBase();

        getQueenParam().basicBeautyRecord.enableSkinWhiting = true;
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinWhitingParam = 0.16f; // 美白
        getQueenParam().basicBeautyRecord.enableSkinBuffing = true;
        getQueenParam().basicBeautyRecord.skinBuffingParam = 0.58f;  // 磨皮
        getQueenParam().basicBeautyRecord.skinSharpenParam = 0.12f;  // 锐化
        getQueenParam().basicBeautyRecord.skinRedParam = 0.1f;      // 红润
        // 滤镜10
        getQueenParam().lutRecord.lutEnable = true;
        getQueenParam().lutRecord.lutPath = LUT_10_PATH;
        // 美型
        getQueenParam().faceShapeRecord.enableFaceShape = true;
        getQueenParam().faceShapeRecord.thinFaceParam = 0.88f;
        getQueenParam().faceShapeRecord.bigEyeParam = 0.82f;
        getQueenParam().faceShapeRecord.nosewingParam = 0.33f;
        getQueenParam().faceShapeRecord.thinNoseParam = 0.88f;
        getQueenParam().faceShapeRecord.thinJawParam = 0.3f;
        // 美妆
        getQueenParam().faceMakeupRecord.enableFaceMakeup = true;
        getQueenParam().faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = FACE_MAKEUP_TYPE_YUANQISHAONV_PATH;
        getQueenParam().faceMakeupRecord.makeupBlendType[MakeupType.kMakeupWhole] = BlendType.kBlendNormal;
        // 贴纸3
        getQueenParam().stickerRecord.stickerEnable = true;
        getQueenParam().stickerRecord.stickerPath = "sticker/3";
    }
}