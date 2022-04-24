package com.alivc.live.queenbeauty.injector;

import com.alivc.live.beautyui.bean.BeautyItemBean;
import com.alivc.live.beautyui.bean.BeautyTabBean;
import com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType;
import com.alivc.live.queenbeauty.R;

import java.util.ArrayList;

import static com.alivc.live.queenbeauty.constant.QueenConstant.ITEM_NAME_NONE;
import static com.alivc.live.queenbeauty.constant.QueenConstant.SUB_TAB_INDEX_MAKEUP_BLUSH;
import static com.alivc.live.queenbeauty.constant.QueenConstant.SUB_TAB_INDEX_MAKEUP_WHOLE;
import static com.alivc.live.queenbeauty.constant.QueenConstant.SUB_TAB_NAME_MAKEUP_BLUSH;
import static com.alivc.live.queenbeauty.constant.QueenConstant.SUB_TAB_NAME_MAKEUP_WHOLE;
import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_INDEX_MAKEUP;
import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_NAME_MAKEUP;

public class QueenMakeupDataInjector {
    /// *** 美妆 *** ///
    private static final String ITEM_NAME_MAKEUP_EYEBALL = "美瞳";
    private static final String ITEM_NAME_MAKEUP_HIGHLIGHT = "高光";
    private static final String ITEM_NAME_MAKEUP_MOUTH = "口红";
    private static final String ITEM_NAME_MAKEUP_WHOLE = "整妆";
    private static final String ITEM_NAME_MAKEUP_EYE_BROW = "眼妆";
    private static final String ITEM_NAME_MAKEUP_BLUSH = "腮红";

    //    private static final String ITEM_NAME_MAKEUP_POUCH = "眼袋";
    //    private static final String ITEM_NAME_MAKEUP_WHITE_TEETH = "白牙";
    //    private static final String ITEM_NAME_MAKEUP_NASOLABIAL_FOLDS = "法令纹";

    private static final String SUB_ITEM_NAME_MAKEUP_BLUSH_POWDER_PINK = "嫩粉";
    private static final String SUB_ITEM_NAME_MAKEUP_BLUSH_PINK = "桃红";
    private static final String SUB_ITEM_NAME_MAKEUP_BLUSH_CHERRY = "樱桃";
    private static final String SUB_ITEM_NAME_MAKEUP_BLUSH_ORANGE = "蜜橘";
    private static final String SUB_ITEM_NAME_MAKEUP_BLUSH_VIOLET = "黛紫";

    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_BASIC = "基础";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_VIGOUR = "活力";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_YOUNG = "元气";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_APRICOT = "杏粉";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_PEACH = "蜜桃";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_ORANGE = "奶橘";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_PLUM = "梅子";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_ELEGANT = "优雅";
    private static final String SUB_ITEM_NAME_MAKEUP_WHOLE_CHARM = "魅惑";

    public static BeautyTabBean getBeautyMakeupTabBean() {
        return new BeautyTabBean(TAB_INDEX_MAKEUP, TAB_NAME_MAKEUP, true, true, getBeautyMakeupItemBeans());
    }

    private static ArrayList<BeautyItemBean> getBeautyMakeupItemBeans() {
        ArrayList<BeautyItemBean> itemBeans = new ArrayList<BeautyItemBean>();

        BeautyItemBean makeupWhole = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_MAKEUP_WHOLE, R.drawable.icon_beauty_makeup_whole_disable, R.drawable.icon_beauty_makeup_whole_enable,
                getBeautyMakeupWholeSubTabBean());

        BeautyItemBean makeupHighlight = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupHighlight, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_MAKEUP_HIGHLIGHT, R.drawable.icon_beauty_makeup_highlight_disable, R.drawable.icon_beauty_makeup_highlight_enable,
                false);
        makeupHighlight.setMaterialPath("race_res/makeup/highlight.png");

        BeautyItemBean makeupEyeball = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupEyeball, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_MAKEUP_EYEBALL, R.drawable.icon_beauty_makeup_eyeball_disable, R.drawable.icon_beauty_makeup_eyeball_enable,
                false);
        makeupEyeball.setMaterialPath("race_res/makeup/eyeball.png");

        BeautyItemBean makeupMouth = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupMouth, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_MAKEUP_MOUTH, R.drawable.icon_beauty_makeup_mouth_disable, R.drawable.icon_beauty_makeup_mouth_enable,
                false);
        makeupMouth.setMaterialPath("race_res/makeup/mouth.png");

        BeautyItemBean makeupEyeBrow = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupEyeBrow, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_MAKEUP_EYE_BROW, R.drawable.icon_beauty_makeup_eye_brow_disable, R.drawable.icon_beauty_makeup_eye_brow_enable,
                false);
        makeupEyeBrow.setMaterialPath("race_res/makeup/eye_brow.png");

        BeautyItemBean makeupBlush = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupBlush, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_MAKEUP_BLUSH, R.drawable.icon_beauty_makeup_blush_disable, R.drawable.icon_beauty_makeup_blush_enable,
                getBeautyMakeupBlushSubTabBean());
//
//        BeautyItemBean beautyPouch = new BeautyItemBean(AliLiveBeautyParams.kAliLiveParamsBPPouch, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
//                ITEM_NAME_MAKEUP_POUCH, R.drawable.icon_beauty_makeup_pouch_disable, R.drawable.icon_beauty_makeup_pouch_enable,
//                false);
//        BeautyItemBean beautyWhiteTeeth = new BeautyItemBean(AliLiveBeautyParams.kAliLiveParamsBPWhiteTeeth, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
//                ITEM_NAME_MAKEUP_WHITE_TEETH, R.drawable.icon_beauty_makeup_white_teeth_disable, R.drawable.icon_beauty_makeup_white_teeth_enable,
//                false);
//        BeautyItemBean beautyNasolabialFolds = new BeautyItemBean(AliLiveBeautyParams.kAliLiveParamsBPNasolabialFolds, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
//                ITEM_NAME_MAKEUP_NASOLABIAL_FOLDS, R.drawable.icon_beauty_makeup_nasolabial_folds_disable, R.drawable.icon_beauty_makeup_nasolabial_folds_enable,
//                false);

        itemBeans.add(makeupWhole);
        itemBeans.add(makeupHighlight);
        itemBeans.add(makeupEyeball);
        itemBeans.add(makeupMouth);
        itemBeans.add(makeupEyeBrow);
        itemBeans.add(makeupBlush);
//
//        itemBeans.add(beautyPouch);
//        itemBeans.add(beautyWhiteTeeth);
//        itemBeans.add(beautyNasolabialFolds);

        return itemBeans;
    }

    private static BeautyTabBean getBeautyMakeupBlushSubTabBean() {
        ArrayList<BeautyItemBean> itemBeans = new ArrayList<BeautyItemBean>();

        BeautyItemBean makeupBlushNone = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupBlush, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_NONE, R.drawable.icon_beauty_item_none_disable, R.drawable.icon_beauty_item_none_enable,
                false);

        BeautyItemBean makeupBlushPowderPink = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupBlush, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_BLUSH_POWDER_PINK, R.drawable.icon_beauty_makeup_blush_powder_pink_disable, R.drawable.icon_beauty_makeup_blush_powder_pink_enable,
                false);
        makeupBlushPowderPink.setMaterialPath("race_res/makeup/blush_嫩粉腮红.png");

        BeautyItemBean makeupBlushOrange = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupBlush, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_BLUSH_ORANGE, R.drawable.icon_beauty_makeup_blush_orange_disable, R.drawable.icon_beauty_makeup_blush_orange_enable,
                false);
        makeupBlushOrange.setMaterialPath("race_res/makeup/blush_蜜橘腮红.png");

        BeautyItemBean makeupBlushViolet = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupBlush, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_BLUSH_VIOLET, R.drawable.icon_beauty_makeup_blush_violet_disable, R.drawable.icon_beauty_makeup_blush_violet_enable,
                false);
        makeupBlushViolet.setMaterialPath("race_res/makeup/blush_黛紫腮红.png");

        BeautyItemBean makeupBlushPink = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupBlush, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_BLUSH_PINK, R.drawable.icon_beauty_makeup_blush_pink_disable, R.drawable.icon_beauty_makeup_blush_pink_enable,
                false);
        makeupBlushPink.setMaterialPath("race_res/makeup/blush_桃红腮红.png");

        BeautyItemBean makeupBlushCherry = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupBlush, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_BLUSH_CHERRY, R.drawable.icon_beauty_makeup_blush_cherry_disable, R.drawable.icon_beauty_makeup_blush_cherry_enable,
                false);
        makeupBlushCherry.setMaterialPath("race_res/makeup/blush_樱桃腮红.png");

        itemBeans.add(makeupBlushNone);
        itemBeans.add(makeupBlushPowderPink);
        itemBeans.add(makeupBlushOrange);
        itemBeans.add(makeupBlushViolet);
        itemBeans.add(makeupBlushPink);
        itemBeans.add(makeupBlushCherry);

        return new BeautyTabBean(SUB_TAB_INDEX_MAKEUP_BLUSH, SUB_TAB_NAME_MAKEUP_BLUSH, itemBeans);
    }

    private static BeautyTabBean getBeautyMakeupWholeSubTabBean() {
        ArrayList<BeautyItemBean> itemBeans = new ArrayList<BeautyItemBean>();

        BeautyItemBean makeupWholeNone = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                ITEM_NAME_NONE, R.drawable.icon_beauty_item_none_disable, R.drawable.icon_beauty_item_none_enable,
                false);

        BeautyItemBean makeupWholeVigour = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_VIGOUR, R.drawable.icon_beauty_face_shape_whole_vigour_disable, R.drawable.icon_beauty_face_shape_whole_vigour_enable,
                false);
        makeupWholeVigour.setMaterialPath("race_res/makeup/活力妆.png");

        BeautyItemBean makeupWholeApricot = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_APRICOT, R.drawable.icon_beauty_face_shape_whole_apricot_disable, R.drawable.icon_beauty_face_shape_whole_apricot_enable,
                false);
        makeupWholeApricot.setMaterialPath("race_res/makeup/杏粉妆.png");

        BeautyItemBean makeupWholeOrange = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_ORANGE, R.drawable.icon_beauty_face_shape_whole_orange_disable, R.drawable.icon_beauty_face_shape_whole_orange_enable,
                false);
        makeupWholeOrange.setMaterialPath("race_res/makeup/奶橘妆.png");

        BeautyItemBean makeupWholePeach = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_PEACH, R.drawable.icon_beauty_face_shape_whole_peach_disable, R.drawable.icon_beauty_face_shape_whole_peach_enable,
                false);
        makeupWholePeach.setMaterialPath("race_res/makeup/蜜桃妆.png");

        BeautyItemBean makeupWholeElegant = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_ELEGANT, R.drawable.icon_beauty_face_shape_whole_elegant_disable, R.drawable.icon_beauty_face_shape_whole_elegant_enable,
                false);
        makeupWholeElegant.setMaterialPath("race_res/makeup/优雅妆.png");

        BeautyItemBean makeupWholeCharm = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_CHARM, R.drawable.icon_beauty_face_shape_whole_charm_disable, R.drawable.icon_beauty_face_shape_whole_charm_enable,
                false);
        makeupWholeCharm.setMaterialPath("race_res/makeup/魅惑妆.png");

        BeautyItemBean makeupWholePlum = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_PLUM, R.drawable.icon_beauty_face_shape_whole_plum_disable, R.drawable.icon_beauty_face_shape_whole_plum_enable,
                false);
        makeupWholePlum.setMaterialPath("race_res/makeup/梅子妆.png");

        BeautyItemBean makeupWholeBasic = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_BASIC, R.drawable.icon_beauty_face_shape_whole_basic_disable, R.drawable.icon_beauty_face_shape_whole_basic_enable,
                false);
        makeupWholeBasic.setMaterialPath("race_res/makeup/基础妆.png");

        BeautyItemBean makeupWholeYoung = new BeautyItemBean(AliLiveBeautyMakeupType.kAliLiveMakeupWhole, BeautyItemBean.BeautyType.BEAUTY_MAKEUP,
                SUB_ITEM_NAME_MAKEUP_WHOLE_YOUNG, R.drawable.icon_beauty_face_shape_whole_young_disable, R.drawable.icon_beauty_face_shape_whole_young_enable,
                false);
        makeupWholeYoung.setMaterialPath("race_res/makeup/元气少女妆.png");

        itemBeans.add(makeupWholeNone);
        itemBeans.add(makeupWholeVigour);
        itemBeans.add(makeupWholeApricot);
        itemBeans.add(makeupWholeOrange);
        itemBeans.add(makeupWholePeach);
        itemBeans.add(makeupWholeElegant);
        itemBeans.add(makeupWholeCharm);
        itemBeans.add(makeupWholePlum);
        itemBeans.add(makeupWholeBasic);
        itemBeans.add(makeupWholeYoung);

        return new BeautyTabBean(SUB_TAB_INDEX_MAKEUP_WHOLE, SUB_TAB_NAME_MAKEUP_WHOLE, itemBeans);
    }
}
