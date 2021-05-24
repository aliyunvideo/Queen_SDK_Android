package com.alilive.alilivesdk_demo.utils;

import android.text.TextUtils;

import com.alivc.live.AliLiveBeautyManager;
import com.alilive.alilivesdk_demo.listener.BeautyClickAndSlideListener;

import java.util.HashMap;
import java.util.Map;

public class BeautyManager implements BeautyClickAndSlideListener {
    private AliLiveBeautyManager mAliLiveBeautyManager;
    private String mStickersPath;
    private String mFilterPath;
    private HashMap<AliLiveBeautyManager.BeautyType,Boolean> mPageSwitchParams;
    private HashMap<AliLiveBeautyManager.BeautyParam,Float> mBeautyParams;
    private HashMap<AliLiveBeautyManager.BeautyParam,String[]> mMakeUpParams;
    /**
     * 是否正在使用口红特效
     */
    private boolean isUseLipstick = false;

    public BeautyManager() {
        mPageSwitchParams = new HashMap<>();
        mBeautyParams = new HashMap<>();
        mMakeUpParams = new HashMap<>();
    }

    public AliLiveBeautyManager getmAliLiveBeautyManager() {
        return mAliLiveBeautyManager;
    }

    public void setmAliLiveBeautyManager(AliLiveBeautyManager mAliLiveBeautyManager) {
        this.mAliLiveBeautyManager = mAliLiveBeautyManager;
    }


    /**
     * 美颜美型的交互页面
     * */
    @Override
    public void onButtonClick(String pageName,int pageIndex, String message, int position) {
        if(mAliLiveBeautyManager == null){
            return;
        }
        //disable race beauty
        /*
        String[] path;
        switch (message){
            case "<空>":
                updateMaterial(false,mStickersPath);
                break;
            case "蝙蝠":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/bianfu");
                break;
            case "胡子":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/bizihuxu");
                break;
            case "小鸡球球":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/rlgood");
                break;
            case "白猫":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/cat");
                break;
            case "黄柠檬热到融化":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/hnmrdrh");
                break;
            case "粉红喵星人":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/mxr");
                break;
            case "撒娇喵喵":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/saojiaomiaomiao");
                break;
            case "森林精灵":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/senlinjingling");
                break;
            case "简约-毛帽子":
                updateMaterial(true,CopyFileUtil.getFileDir() + "sticker/simpleCap");
                break;
            case "优雅妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/优雅妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "元气少女妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/元气少女妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "基础妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/基础妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "奶橘妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/奶橘妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "杏粉妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/杏粉妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "梅子妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/梅子妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "活力妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/活力妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "蜜桃妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/蜜桃妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "魅惑妆":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/魅惑妆.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Whole, 0.8f, 0.2f);
                break;
            case "美瞳":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/eyeball.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball, 0.8f, 0.2f);
                break;
            case "美瞳2":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/eyeball2.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball, 0.8f, 0.2f);
                break;
            case "美瞳3":
                path = new String[1];
                path[0] = CopyFileUtil.getFileDir() + "makeup/eyeball3.png";
                mMakeUpParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball,path);
                mAliLiveBeautyManager.setBeautyParamMakeupImage(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball,path, AliLiveBeautyManager.BlendType.Multiply, 30 );
                mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball, 0.8f, 0.2f);
                break;
            case "原始滤镜":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/原始滤镜色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            case "超脱":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/超脱色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            case "纯真":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/纯真色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            case "怀旧":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/怀旧色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            case "蓝调":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/蓝调色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            case "浪漫":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/浪漫色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            case "清新":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/清新色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            case "元气":
                mFilterPath = CopyFileUtil.getFileDir() + "lookups/元气色卡.png";
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
                break;
            default:
                break;
        }
        */
    }

    @Override
    public void onProgressChanged(String pageName,int pageIndex,String message, float position) {
        if(mAliLiveBeautyManager == null){
            return;
        }
        switch (message){
            //android 的磨皮阈值增大，1.5的时候效果最好
            case "磨皮":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.SkinBuffing_SkinBuffing, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.SkinBuffing_SkinBuffing, position);
                break;
                /*
            case "锐化":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.SkinBuffing_SkinSharpen, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.SkinBuffing_SkinSharpen, position);
                break;
                */
            case "美白":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.SkinWhiting_SkinWhiting, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.SkinWhiting_SkinWhiting, position);
                break;
                /*
            case "去眼袋":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceBuffing_Pouch, position );
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceBuffing_Pouch, position);
                break;
            case "去法令纹":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceBuffing_NasolabialFolds, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceBuffing_NasolabialFolds, position);
                break;
            case "色卡滤镜增强":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.LUT_LUT, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, position);
                break;
            case "颧骨":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_CutCheek, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_CutCheek, position);
                break;
            case "削脸":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_CutFace, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_CutFace, position);
                break;
            case "瘦脸":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_ThinFace, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_ThinFace, position);
                break;
            case "脸长":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_LongFace, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_LongFace, position);
                break;
            case "下巴缩短":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_LowerJaw, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_LowerJaw, position);
                break;
            case "下巴拉长":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_HigherJaw, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_HigherJaw, position);
                break;
            case "瘦下巴":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_ThinJaw, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_ThinJaw, position);
                break;
            case "瘦下颌":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_ThinMandible, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_ThinMandible, position);
                break;
            case "大眼":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_BigEye, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_BigEye, position);
                break;
            case "眼角1":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_EyeAngle1, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_EyeAngle1, position);
                break;
            case "眼距":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_Canthus, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_Canthus, position);
                break;
            case "拉宽眼距":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_Canthus1, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_Canthus1, position);
                break;
            case "眼角2":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_EyeAngle2, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_EyeAngle2, position);
                break;
            case "眼睛高度":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_EyeTDAngle, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_EyeTDAngle, position);
                break;
            case "瘦鼻":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_ThinNose, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_ThinNose, position);
                break;
            case "鼻翼":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_Nosewing, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_Nosewing, position);
                break;
            case "鼻长":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_NasalHeight, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_NasalHeight, position);
                break;
            case "鼻头长":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_NoseTipHeight, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_NoseTipHeight, position);
                break;
            case "唇宽":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_MouthWidth, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_MouthWidth, position);
                break;
            case "嘴唇大小":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_MouthSize, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_MouthSize, position);
                break;
            case "唇高":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_MouthHigh, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_MouthHigh, position);
                break;
            case "人中":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceShape_Philtrum, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceShape_Philtrum, position);
                break;
            case "最大值":
                mBeautyParams.put(AliLiveBeautyManager.BeautyParam.FaceMakeup_Max, position);
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.FaceMakeup_Max, position);
                break;
                */
            default:
                break;
        }
    }

    @Override
    public void onSwitchChanged(String pageName,int pageIndex,String message, boolean isCheck) {
        if(mAliLiveBeautyManager == null){
            return;
        }
    }
    private void updateMaterial(boolean isCheck , String path){
        //disable race beauty
        /*
        if (isCheck) {
            if(!TextUtils.isEmpty(mStickersPath)){
                mAliLiveBeautyManager.removeBeautyParam(AliLiveBeautyManager.BeautyParam.Material,mStickersPath);
                mStickersPath = null;
            }
            if(!TextUtils.isEmpty(path)){
                mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.Material, path);
                mStickersPath = path;
            }
        } else {
            mStickersPath = null;
            mAliLiveBeautyManager.removeBeautyParam(AliLiveBeautyManager.BeautyParam.Material, path);
        }
        */
    }

    @Override
    public void onPageSwitch(String pageName,int pageIndex, boolean isCheck) {
        //disable race beauty
        /*
        if(mAliLiveBeautyManager == null){
            return;
        }
        switch (pageIndex){
            case 1:
                mAliLiveBeautyManager.enableBeautyType(AliLiveBeautyManager.BeautyType.FaceMakeup, isCheck);
                mPageSwitchParams.put(AliLiveBeautyManager.BeautyType.FaceMakeup,isCheck);
                break;
            case 2:
                mAliLiveBeautyManager.enableBeautyType(AliLiveBeautyManager.BeautyType.FaceShape, isCheck);
                mPageSwitchParams.put(AliLiveBeautyManager.BeautyType.FaceShape,isCheck);
                break;
            case 3:
                mAliLiveBeautyManager.enableBeautyType(AliLiveBeautyManager.BeautyType.LUT, isCheck);
                mPageSwitchParams.put(AliLiveBeautyManager.BeautyType.LUT,isCheck);
                break;
            default:
                break;

        }
         */
    }


    /**
     * 恢复记录的美颜参数
     */
    public void resumeParams(){
        /*
        for (Map.Entry<AliLiveBeautyManager.BeautyType, Boolean> map : mPageSwitchParams.entrySet()) {
            mAliLiveBeautyManager.enableBeautyType(map.getKey(),map.getValue());
        }

        if(!TextUtils.isEmpty(mFilterPath)){
            mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, null);
            mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.LUT_LUT, mFilterPath);
        }
        if (!TextUtils.isEmpty(mStickersPath)){
            updateMaterial(true,mStickersPath);
        }
        */
        for (Map.Entry<AliLiveBeautyManager.BeautyType, Boolean> map : mPageSwitchParams.entrySet()) {
            AliLiveBeautyManager.BeautyType type = map.getKey();
            if (type == AliLiveBeautyManager.BeautyType.SkinBuffing ||
                type == AliLiveBeautyManager.BeautyType.SkinWhiting) {
                mAliLiveBeautyManager.enable(AliLiveBeautyManager.EnableType.Basic);
            }
        }
        for (Map.Entry<AliLiveBeautyManager.BeautyParam, Float> map : mBeautyParams.entrySet()) {
            mAliLiveBeautyManager.setBeautyParam(map.getKey(), map.getValue());
        }
        /*
        for (Map.Entry<AliLiveBeautyManager.BeautyParam, String[]> map : mMakeUpParams.entrySet()) {
            mAliLiveBeautyManager.setBeautyParamMakeupImage(map.getKey(), map.getValue(), AliLiveBeautyManager.BlendType.Multiply, 30 );
            mAliLiveBeautyManager.setBeautyParamMakeupAlpha(AliLiveBeautyManager.BeautyParam.FaceMakeup_Eyeball, 0.8f, 0.2f);
        }
        */
    }






}
