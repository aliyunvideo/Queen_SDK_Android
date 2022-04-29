package im.zego.CustomerVideoCapture.queen.param;

import com.aliyun.android.libqueen.models.MakeupType;
import im.zego.CustomerVideoCapture.queen.param.QueenParam;

import java.util.HashMap;

public class QueenParamFactory {

    public static final int ID_FEATURE_NONE = -1;

    private static final String STICKER_DIR_PATH = "sticker/";

//    临时组合模式,先hardcode资源名
    private static final String LUT_1_PATH = "lookups/ly1.png";
    private static final String LUT_7_PATH = "lookups/ly7.png";
    private static final String LUT_27_PATH = "lookups/lz27.png";
    private static final String LUT_8_PATH = "lookups/lz8.png";
    private static final String LUT_23_PATH = "lookups/lz23.png";
    private static final String LUT_5_PATH = "lookups/lz5.png";
    private static final String LUT_10_PATH = "lookups/lz10.png";

    private static final String FACE_MAKEUP_TYPE_MITAO_PATH = "makeup/mitao.png";
    private static final String FACE_MAKEUP_TYPE_YUANQISHAONV_PATH = "makeup/yuanqishaonv.png";
    private static final String FACE_MAKEUP_TYPE_YOUYA_PATH = "makeup/youya.png";
    private static final String FACE_MAKEUP_TYPE_MEIHUO_PATH = "makeup/meihuo.png";
    private static final String FACE_MAKEUP_TYPE_JICHU_PATH = "makeup/jichu.png";

    private static void initBeautyRecord(QueenParam.BasicBeautyRecord basicBeautyRecord) {
        basicBeautyRecord.enableFaceBuffing = true;
        basicBeautyRecord.faceBuffingPouchParam = 0.6f; //去眼袋[0,1]
        basicBeautyRecord.faceBuffingNasolabialFoldsParam = 0.6f; //去法令纹[0,1]
        basicBeautyRecord.faceBuffingWhiteTeeth = 0.2f; //白牙[0,1]
        basicBeautyRecord.faceBuffingBrightenEye = 0.96f; //亮眼[0,1]
        basicBeautyRecord.faceBuffingLipstick = 0.15f; // 滤镜美妆：口红[0,1]
        basicBeautyRecord.enableFaceBuffingLipstick = false; // 滤镜美妆：口红开关
        basicBeautyRecord.faceBuffingBlush = 0.15f; // 滤镜美妆：腮红[0,1]
        basicBeautyRecord.faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
        basicBeautyRecord.faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]
        basicBeautyRecord.enableSkinWhiting = true;
        basicBeautyRecord.enableSkinBuffing = true;
        basicBeautyRecord.enableSkinRed = true;
    }

    // 默认值为通用场景
    public static QueenParam getDefaultScenesParam() {
        return Scenes.getScenes(Scenes.ID_GENERAL);
    }

    public static QueenParam getOriginalScenesParam() {
        return Scenes.getScenes(Scenes.ID_ORIGINAL);
    }

    ////////////////////////////////场景的类别///////////////////////////////////////////
    public static class Scenes {
        private static final int ID_ORIGINAL = ID_FEATURE_NONE;     // 原貌
        private static final int ID_GENERAL = 1;   // 通用模式
        private static final int ID_ONLINE = 2;    // 电商模式
        private static final int ID_RECREATION = 3;// 生活娱乐
        private static final int ID_EDUCATION = 4; // 教育

        public static HashMap<Integer, QueenParam> sScenesCaches = new HashMap<>(4);
        static {
            initScenesParams();
        }

        public static QueenParam getScenes(int id) {
            return sScenesCaches.get(Integer.valueOf(id));
        }

        public static void resetAllScenes() {
            sScenesCaches.clear();
            BeautyParams.resetAllParams();
            FaceShapeParams.resetAllParams();
            BodyShapeParams.resetAllParams();

            initScenesParams();
        }

        private static void initScenesParams() {
            sScenesCaches.put(ID_ORIGINAL, createScenesOriginal());
            sScenesCaches.put(ID_GENERAL, createScenesGeneral());
            sScenesCaches.put(ID_ONLINE, createScenesOnline());
            sScenesCaches.put(ID_RECREATION, createScenesRecreation());
            sScenesCaches.put(ID_EDUCATION, createScenesEducation());
        }

        private static QueenParam createScenesOriginal() {
            QueenParam sScenesOriginal = new QueenParam();
            sScenesOriginal.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_ORIGINAL);
            sScenesOriginal.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_ORIGIN);
            return sScenesOriginal;
        }

        // 通用:美颜-基础;美型-精致;美妆-原貌;滤镜-7;贴纸-原貌
        private static QueenParam createScenesGeneral() {
            QueenParam sScenesGeneral = new QueenParam();
            sScenesGeneral.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_SIMPLE);
            // 美型-精致
            sScenesGeneral.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_DELICATE);
            sScenesGeneral.faceShapeRecord.enableFaceShape = true;

            // 美妆
            sScenesGeneral.faceMakeupRecord.enableFaceMakeup = false;
            // 贴纸
            sScenesGeneral.stickerRecord.stickerEnable = false;

            return sScenesGeneral;
        }

        // 电商:美颜-少女;美型-可爱;美妆-夜店妆;滤镜-23;贴纸-20
        private static QueenParam createScenesOnline()  {
            QueenParam sScenesOnline = new QueenParam();
            sScenesOnline.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_SHAONV);
            // 美型-可爱
            sScenesOnline.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_CUTE);

            // 滤镜8
            sScenesOnline.lutRecord.lutEnable = true;
            sScenesOnline.lutRecord.lutPath = LUT_23_PATH;
            sScenesOnline.lutRecord.lutParam = 0.34f;

            // 美妆
            sScenesOnline.faceMakeupRecord.enableFaceMakeup = true;
//            OnFaceMakeupAction.updateComposeMakeup(sScenesOnline.faceMakeupRecord, 0);//夜店妆
            sScenesOnline.faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = null;
            sScenesOnline.faceMakeupRecord.makeupResourcePath[1] = "makeup/highlight/highlight.2.13.png";
            sScenesOnline.faceMakeupRecord.makeupResourcePath[3] = "makeup/mouth_yaochun/shiliuhong.3.3.png";
            sScenesOnline.faceMakeupRecord.makeupResourcePath[5] = "makeup/eyebrow/biaozhunmei.2.3.png";
            sScenesOnline.faceMakeupRecord.makeupResourcePath[6] = "makeup/blush_color7/shaonv.2.3.png";
            sScenesOnline.faceMakeupRecord.makeupResourcePath[7] = "makeup/eyeshadow/fangtangfen.3.3.png";
            sScenesOnline.faceMakeupRecord.makeupResourcePath[8] = "makeup/eyeliner_292929/xiaoyemao.2.3.png";
            sScenesOnline.faceMakeupRecord.makeupResourcePath[9] = "makeup/eyelash/ziran.2.3.png";

            // 贴纸20
            sScenesOnline.stickerRecord.stickerEnable = true;
            sScenesOnline.stickerRecord.stickerPath = STICKER_DIR_PATH + 22;

            return sScenesOnline;
        }

        // 娱乐:美颜-魅惑;美型-网红;美妆-活泼妆;滤镜-5;贴纸-12
        private static QueenParam createScenesRecreation() {
            QueenParam sScenesRecreation = new QueenParam();
            sScenesRecreation.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_MEIHUO);
            // 美型-网红
            sScenesRecreation.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_WANGHONG);

            // 滤镜5
            sScenesRecreation.lutRecord.lutEnable = true;
            sScenesRecreation.lutRecord.lutPath = LUT_5_PATH;
            sScenesRecreation.lutRecord.lutParam = 0.65f;

            // 美妆
            sScenesRecreation.faceMakeupRecord.enableFaceMakeup = true;
//            OnFaceMakeupAction.updateComposeMakeup(sScenesRecreation.faceMakeupRecord, 1);//活泼妆
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = null;
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[1] = "makeup/highlight/highlight.2.13.png";
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[3] = "makeup/mouth_wumian/chidousha.3.3.png";
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[5] = "makeup/eyebrow/liuyemei.2.3.png";
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[6] = "makeup/blush_color4/chayi.2.3.png";
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[7] = "makeup/eyeshadow/fangtangfen.3.3.png";
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[8] = "makeup/eyeliner_292929/juanqiao.2.3.png";
            sScenesRecreation.faceMakeupRecord.makeupResourcePath[9] = "makeup/eyelash/lingdong.2.3.png";

            // 贴纸12
            sScenesRecreation.stickerRecord.stickerEnable = true;
            sScenesRecreation.stickerRecord.stickerPath = STICKER_DIR_PATH + 12;

            return sScenesRecreation;
        }

        // 教育:美颜-流行;美型-优雅;美妆-微醺妆;滤镜-27;贴纸-原貌
        private static QueenParam createScenesEducation()  {
            QueenParam sScenesEducation = new QueenParam();
            sScenesEducation.basicBeautyRecord = BeautyParams.getParams(BeautyParams.ID_FASHION);
            // 美型-优雅
            sScenesEducation.faceShapeRecord = FaceShapeParams.getParams(FaceShapeParams.TAG_SHAPE_GRACE);

            // 滤镜27
            sScenesEducation.lutRecord.lutEnable = true;
            sScenesEducation.lutRecord.lutPath = LUT_27_PATH;
            sScenesEducation.lutRecord.lutParam = 0.25f;

            // 美妆-微醺妆
            sScenesEducation.faceMakeupRecord.enableFaceMakeup = true;
//            OnFaceMakeupAction.updateComposeMakeup(sScenesEducation.faceMakeupRecord, 2);//微醺妆
            sScenesEducation.faceMakeupRecord.makeupResourcePath[MakeupType.kMakeupWhole] = null;
            sScenesEducation.faceMakeupRecord.makeupResourcePath[1] = "makeup/highlight/highlight.2.13.png";
            sScenesEducation.faceMakeupRecord.makeupResourcePath[3] = "makeup/mouth_wumian/jiangguose.3.3.png";
            sScenesEducation.faceMakeupRecord.makeupResourcePath[5] = "makeup/eyebrow/juanyanmei.2.3.png";
            sScenesEducation.faceMakeupRecord.makeupResourcePath[6] = "makeup/blush_color7/chunqing.2.3.png";
            sScenesEducation.faceMakeupRecord.makeupResourcePath[7] = "makeup/eyeshadow/yuanqicheng.3.3.png";
            sScenesEducation.faceMakeupRecord.makeupResourcePath[8] = "makeup/eyeliner_292929/wenrou.2.3.png";
            sScenesEducation.faceMakeupRecord.makeupResourcePath[9] = "makeup/eyelash/jichu.2.3.png";

            // 贴纸12
            sScenesEducation.stickerRecord.stickerEnable = true;
            sScenesEducation.stickerRecord.stickerPath = STICKER_DIR_PATH + 62;

            return sScenesEducation;
        }
    }
    ////////////////////////////////场景的类别 end///////////////////////////////////////////


    ////////////////////////////////美颜的类别///////////////////////////////////////////
    public static class BeautyParams {
        public static final int ID_ORIGINAL = ID_FEATURE_NONE;     // 原貌
        public static final int ID_SIMPLE = 2;
        public static final int ID_FASHION = 3;
        public static final int ID_MEIHUO = 4;
        public static final int ID_KEAI = 5;
        public static final int ID_SHAONV = 6;

        public static HashMap<Integer, QueenParam.BasicBeautyRecord> sParamsCaches = new HashMap<>(6);
        static {
            initParams();
        }

        public static QueenParam.BasicBeautyRecord getParams(int id) {
            return sParamsCaches.get(Integer.valueOf(id));
        }

        public static void resetAllParams() {
            sParamsCaches.clear();
            initParams();
        }

        public static void initParams() {
            sParamsCaches.put(ID_ORIGINAL, createModeOriginal());
            sParamsCaches.put(ID_SIMPLE, createModeSimple());
            sParamsCaches.put(ID_FASHION, createModeFashion());
            sParamsCaches.put(ID_MEIHUO, createModeMeiHuo());
            sParamsCaches.put(ID_KEAI, createModeKeAi());
            sParamsCaches.put(ID_SHAONV, createModeShaoNv());
        }

        private static QueenParam.BasicBeautyRecord createModeOriginal() {
            QueenParam.BasicBeautyRecord sModeOriginal = new QueenParam.BasicBeautyRecord();
            sModeOriginal.clear();
            return sModeOriginal;
        }

        // 基础模式
        private static QueenParam.BasicBeautyRecord createModeSimple() {
            QueenParam.BasicBeautyRecord sModeSimple = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeSimple);
            sModeSimple.skinWhitingParam = 0.32f; // 美白
            sModeSimple.skinBuffingParam = 0.72f;  // 磨皮
            sModeSimple.skinSharpenParam = 0.1f;  // 锐化
            sModeSimple.skinRedParam = 0.25f;      // 红润
            sModeSimple.faceBuffingNasolabialFoldsParam = 0.63f; //去法令纹[0,1]
            return sModeSimple;
        }

        // 流行模式
        private static QueenParam.BasicBeautyRecord createModeFashion() {
            QueenParam.BasicBeautyRecord sModeFashion = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeFashion);
            sModeFashion.skinWhitingParam = 0.26f; // 美白
            sModeFashion.skinBuffingParam = 0.65f;  // 磨皮
            sModeFashion.skinSharpenParam = 0.10f;  // 锐化
            sModeFashion.skinRedParam = 0.48f;      // 红润
            sModeFashion.faceBuffingPouchParam = 0.75f;      // 眼袋
            sModeFashion.faceBuffingNasolabialFoldsParam = 0.73f; //去法令纹[0,1]
            sModeFashion.faceBuffingWhiteTeeth = 0.54f; //白牙[0,1]
            sModeFashion.faceBuffingBrightenEye = 0.96f; //亮眼[0,1]
            sModeFashion.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
            sModeFashion.faceBuffingLipstick = 0.66f; // 滤镜美妆：口红[0,1]
            sModeFashion.faceBuffingLipstickColorParams = 0.0f;
            sModeFashion.faceBuffingLipstickGlossParams = 0.35f;
            sModeFashion.faceBuffingLipstickBrightnessParams = 0.0f;
            sModeFashion.faceBuffingBlush = 0.68f; // 滤镜美妆：腮红[0,1]
            sModeFashion.faceBuffingWrinklesParam = 0.20f; // 祛皱纹[0,1]
            sModeFashion.faceBuffingBrightenFaceParam = 0.30f; // 祛暗沉[0,1]

            return sModeFashion;
        }

        private static QueenParam.BasicBeautyRecord createModeMeiHuo() {
            QueenParam.BasicBeautyRecord sModeMeiHuo = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeMeiHuo);
            sModeMeiHuo.skinWhitingParam = 0.36f; // 美白
            sModeMeiHuo.skinBuffingParam = 0.68f;  // 磨皮
            sModeMeiHuo.skinSharpenParam = 0.13f;  // 锐化
            sModeMeiHuo.skinRedParam = 0.75f;      // 红润
            sModeMeiHuo.faceBuffingPouchParam = 0.84f;      // 眼袋
            sModeMeiHuo.faceBuffingNasolabialFoldsParam = 0.78f; //去法令纹[0,1]
            sModeMeiHuo.faceBuffingWhiteTeeth = 0.72f; //白牙[0,1]
            sModeMeiHuo.faceBuffingBrightenEye = 0.89f; //亮眼[0,1]
            sModeMeiHuo.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
            sModeMeiHuo.faceBuffingLipstick = 0.82f; // 滤镜美妆：口红[0,1]
            sModeMeiHuo.faceBuffingLipstickColorParams = 0.f;
            sModeMeiHuo.faceBuffingLipstickGlossParams = 1.0f;
            sModeMeiHuo.faceBuffingLipstickBrightnessParams = 0.62f;
            sModeMeiHuo.faceBuffingBlush = 0.50f; // 滤镜美妆：腮红[0,1]
            sModeMeiHuo.faceBuffingWrinklesParam = 0.40f; // 祛皱纹[0,1]
            sModeMeiHuo.faceBuffingBrightenFaceParam = 0.40f; // 祛暗沉[0,1]

            return sModeMeiHuo;
        }

        private static QueenParam.BasicBeautyRecord createModeKeAi() {
            QueenParam.BasicBeautyRecord sModeKeAi = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeKeAi);
            sModeKeAi.skinWhitingParam = 0.36f; // 美白
            sModeKeAi.skinBuffingParam = 0.56f;  // 磨皮
            sModeKeAi.skinSharpenParam = 0.20f;  // 锐化
            sModeKeAi.skinRedParam = 0.12f;      // 红润
            sModeKeAi.faceBuffingPouchParam = 0.84f;      // 眼袋
            sModeKeAi.faceBuffingNasolabialFoldsParam = 0.88f; //去法令纹[0,1]
            sModeKeAi.faceBuffingWhiteTeeth = 0.72f; //白牙[0,1]
            sModeKeAi.faceBuffingBrightenEye = 0.89f; //亮眼[0,1]
            sModeKeAi.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
            sModeKeAi.faceBuffingLipstick = 0.32f; // 滤镜美妆：口红[0,1]
            sModeKeAi.faceBuffingLipstickColorParams = 0.f;
            sModeKeAi.faceBuffingLipstickGlossParams = 1.0f;
            sModeKeAi.faceBuffingLipstickBrightnessParams = 0.2f;
            sModeKeAi.faceBuffingBlush = 0.80f; // 滤镜美妆：腮红[0,1]
            sModeKeAi.faceBuffingWrinklesParam = 0.40f; // 祛皱纹[0,1]
            sModeKeAi.faceBuffingBrightenFaceParam = 0.40f; // 祛暗沉[0,1]
            sModeKeAi.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
            sModeKeAi.faceBuffingLipstick = 0.62f; // 滤镜美妆：口红[0,1]
            return sModeKeAi;
        }

        private static QueenParam.BasicBeautyRecord createModeShaoNv() {
            QueenParam.BasicBeautyRecord sModeShaoNv = new QueenParam.BasicBeautyRecord();
            initBeautyRecord(sModeShaoNv);
            sModeShaoNv.skinWhitingParam = 0.40f; // 美白
            sModeShaoNv.skinBuffingParam = 0.82f;  // 磨皮
            sModeShaoNv.skinSharpenParam = 0.10f;  // 锐化
            sModeShaoNv.skinRedParam = 0.56f;      // 红润
            sModeShaoNv.faceBuffingWhiteTeeth = 0.60f;  // 白牙

            sModeShaoNv.faceBuffingPouchParam = 0.8f; //去眼袋[0,1]
            sModeShaoNv.faceBuffingNasolabialFoldsParam = 0.9f; //去法令纹[0,1]
            sModeShaoNv.faceBuffingWhiteTeeth = 0.2f; //白牙[0,1]
            sModeShaoNv.faceBuffingBrightenEye = 0.8f; //亮眼[0,1]
            sModeShaoNv.faceBuffingBlush = 0.35f; // 滤镜美妆：腮红[0,1]
            sModeShaoNv.faceBuffingWrinklesParam = 0.0f; // 祛皱纹[0,1]
            sModeShaoNv.faceBuffingBrightenFaceParam = 0.0f; // 祛暗沉[0,1]
            sModeShaoNv.enableFaceBuffingLipstick = true; // 滤镜美妆：口红开关
            sModeShaoNv.faceBuffingLipstick = 0.46f; // 滤镜美妆：口红[0,1]
            sModeShaoNv.faceBuffingLipstickColorParams = -0.42f;
            sModeShaoNv.faceBuffingLipstickGlossParams = 0.35f;
            sModeShaoNv.faceBuffingLipstickBrightnessParams = 0.0f;
            return sModeShaoNv;
        }

    }
    ////////////////////////////////美颜的类别 end///////////////////////////////////////////

    ////////////////////////////////美型的类别///////////////////////////////////////////
    public static class FaceShapeParams {
        public static final String TAG_SHAPE_ORIGIN = "close";
        public static final String TAG_SHAPE_AUTO = "auto";
        public static final String TAG_SHAPE_GRACE = "grace";
        public static final String TAG_SHAPE_DELICATE = "delicate";
        public static final String TAG_SHAPE_WANGHONG = "wanghong";
        public static final String TAG_SHAPE_CUTE = "cute";
        public static final String TAG_SHAPE_BABY = "baby";

        public static HashMap<String, QueenParam.FaceShapeRecord> sParamsCaches = new HashMap<>(6);
        static {
            initParams();
        }

        public static QueenParam.FaceShapeRecord getParams(String tag) {
            return sParamsCaches.get(tag);
        }

        public static void resetAllParams() {
            sParamsCaches.clear();
            initParams();
        }

        public static void initParams() {
            sParamsCaches.put(TAG_SHAPE_ORIGIN, createOriginShape());
            sParamsCaches.put(TAG_SHAPE_AUTO, createAutoShape());
            sParamsCaches.put(TAG_SHAPE_GRACE, createGraceShape());
            sParamsCaches.put(TAG_SHAPE_DELICATE, createDelicateShape());
            sParamsCaches.put(TAG_SHAPE_WANGHONG, createWangHongShape());
            sParamsCaches.put(TAG_SHAPE_CUTE, createCuteShape());
            sParamsCaches.put(TAG_SHAPE_BABY, createBabyShape());
        }

        // 原貌
        private static QueenParam.FaceShapeRecord createOriginShape() {
            QueenParam.FaceShapeRecord sOriginShapeRecord = new QueenParam.FaceShapeRecord();
            sOriginShapeRecord.enableFaceShape = false;
            return sOriginShapeRecord;
        }

        // 智能
        private static QueenParam.FaceShapeRecord createAutoShape() {
            QueenParam.FaceShapeRecord sAutoFaceShapeRecord = new QueenParam.FaceShapeRecord();
            sAutoFaceShapeRecord.enableFaceShape = true;
            sAutoFaceShapeRecord.enableAutoFaceShape = true;
            return sAutoFaceShapeRecord;
        }

        // 优雅
        private static QueenParam.FaceShapeRecord createGraceShape() {
            QueenParam.FaceShapeRecord sGraceFaceShapeRecord = new QueenParam.FaceShapeRecord();
            sGraceFaceShapeRecord.enableFaceShape = true;
            sGraceFaceShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(33);
            sGraceFaceShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(58);
            sGraceFaceShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatReverseParam(17);
            sGraceFaceShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatReverseParam(7);
            sGraceFaceShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(52);
            sGraceFaceShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sGraceFaceShapeRecord.mouthWidthParam = QueenParam.FaceShapeRecord.formatReverseParam(18);
            sGraceFaceShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sGraceFaceShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);

            return sGraceFaceShapeRecord;
        }

        // 精致
        private static QueenParam.FaceShapeRecord createDelicateShape() {
            QueenParam.FaceShapeRecord sDelicateShapeRecord = new QueenParam.FaceShapeRecord();
            sDelicateShapeRecord.enableFaceShape = true;
            sDelicateShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(22);
            sDelicateShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(24);
            sDelicateShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(32);
            sDelicateShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(12);
            sDelicateShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(17);
            sDelicateShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(43);
            sDelicateShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(53);
            sDelicateShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatReverseParam(27);
            sDelicateShapeRecord.nosewingParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(64);

            return sDelicateShapeRecord;
        }

        // 网红
        private static QueenParam.FaceShapeRecord createWangHongShape() {
            QueenParam.FaceShapeRecord sWangHongShapeRecord = new QueenParam.FaceShapeRecord();
            sWangHongShapeRecord.enableFaceShape = true;
            sWangHongShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(32);
            sWangHongShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(42);
            sWangHongShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(75);
            sWangHongShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(24);
            sWangHongShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(27);
            sWangHongShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(63);
            sWangHongShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(65);
            sWangHongShapeRecord.thinJawParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(42);
            sWangHongShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(22);
            sWangHongShapeRecord.nosewingParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(68);

            return sWangHongShapeRecord;
        }

        // 可爱
        private static QueenParam.FaceShapeRecord createCuteShape() {
            QueenParam.FaceShapeRecord sCuteShapeRecord = new QueenParam.FaceShapeRecord();
            sCuteShapeRecord.enableFaceShape = true;
            sCuteShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(32);
            sCuteShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(45);
            sCuteShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(58);
            sCuteShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(16);
            sCuteShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(17);
            sCuteShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(43);
            sCuteShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(53);
            sCuteShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatReverseParam(27);
            sCuteShapeRecord.nosewingParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(64);
            sCuteShapeRecord.thinJawParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(38);
            sCuteShapeRecord.higherJawParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(20);

            return sCuteShapeRecord;
        }

        // 婴儿
        private static QueenParam.FaceShapeRecord createBabyShape() {
            QueenParam.FaceShapeRecord sBabyFaceShapeRecord = new QueenParam.FaceShapeRecord();
            sBabyFaceShapeRecord.enableFaceShape = true;
            sBabyFaceShapeRecord.cutFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(56);
            sBabyFaceShapeRecord.thinFaceParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(6);
            sBabyFaceShapeRecord.longFaceParam = QueenParam.FaceShapeRecord.formatReverseParam(27);
            sBabyFaceShapeRecord.lowerJawParam = QueenParam.FaceShapeRecord.formatReverseParam(-10);
            sBabyFaceShapeRecord.bigEyeParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(16);
            sBabyFaceShapeRecord.thinNoseParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sBabyFaceShapeRecord.mouthWidthParam = QueenParam.FaceShapeRecord.formatReverseParam(-8);
            sBabyFaceShapeRecord.thinMandibleParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);
            sBabyFaceShapeRecord.cutCheekParam = QueenParam.FaceShapeRecord.formatFaceShapeParam(0);

            return sBabyFaceShapeRecord;
        }
    }
    ////////////////////////////////美颜的类别 end///////////////////////////////////////////

    ////////////////////////////////美体的类别 begin///////////////////////////////////////////
    public static class BodyShapeParams {
        public static final String TAG_SHAPE_CLOSE = "close";
        public static final String TAG_SHAPE_OPENED = "opened";

        public static HashMap<String, QueenParam.BodyShapeRecord> sParamsCaches = new HashMap<>();

        static {
            initParams();
        }

        public static QueenParam.BodyShapeRecord getParams(String tag) {
            return sParamsCaches.get(tag);
        }

        public static void resetAllParams() {
            sParamsCaches.clear();
            initParams();
        }

        public static void initParams() {
            sParamsCaches.put(TAG_SHAPE_CLOSE, createCloseShape());
            sParamsCaches.put(TAG_SHAPE_OPENED, createOpenShape());
        }

        // 关闭
        private static QueenParam.BodyShapeRecord createCloseShape() {
            QueenParam.BodyShapeRecord shapeRecord = new QueenParam.BodyShapeRecord();
            shapeRecord.enableBodyShape = false;
            return shapeRecord;
        }
        // 开启
        private static QueenParam.BodyShapeRecord createOpenShape() {
            QueenParam.BodyShapeRecord shapeRecord = new QueenParam.BodyShapeRecord();
            shapeRecord.enableBodyShape = true;
            shapeRecord.longLagParam = 0.0f;
            shapeRecord.fullBodyParam = 1.0f;
            shapeRecord.thinLagParam = 0.0f;
            shapeRecord.smallHeadParam = 0.0f;
            return shapeRecord;
        }
    }
    ////////////////////////////////美体的类别 end///////////////////////////////////////////
}
