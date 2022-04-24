package com.alivc.live.queenbeauty.injector;

import com.alivc.live.beautyui.bean.BeautyItemBean;
import com.alivc.live.beautyui.bean.BeautyTabBean;
import com.alivc.live.queenbeauty.R;

import java.util.ArrayList;

import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_INDEX_LUT;
import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_NAME_LUT;

public class QueenLookupDataInjector {
    /// *** 滤镜 *** ///
    private static final String ITEM_NAME_LUT_LOOKUP1 = "美白";
    private static final String ITEM_NAME_LUT_LOOKUP2 = "初恋";
    private static final String ITEM_NAME_LUT_LOOKUP3 = "清爽";
    private static final String ITEM_NAME_LUT_LOOKUP4 = "非凡";
    private static final String ITEM_NAME_LUT_LOOKUP5 = "动人";
    private static final String ITEM_NAME_LUT_LOOKUP6 = "萌系";
    private static final String ITEM_NAME_LUT_LOOKUP7 = "日系";
    //    private static final String ITEM_NAME_LUT_LOOKUP8 = "深秋";
    //    private static final String ITEM_NAME_LUT_LOOKUP9 = "曲奇";
    //    private static final String ITEM_NAME_LUT_LOOKUP10 = "去掉";
    private static final String ITEM_NAME_LUT_LOOKUP11 = "年华";
    private static final String ITEM_NAME_LUT_LOOKUP12 = "单纯";
    //    private static final String ITEM_NAME_LUT_LOOKUP13 = "去掉";
    //    private static final String ITEM_NAME_LUT_LOOKUP14 = "去掉";
    //    private static final String ITEM_NAME_LUT_LOOKUP15 = "去掉";
    //    private static final String ITEM_NAME_LUT_LOOKUP16 = "知秋";
    //    private static final String ITEM_NAME_LUT_LOOKUP17 = "去掉";
    private static final String ITEM_NAME_LUT_LOOKUP18 = "蔷薇";
    //    private static final String ITEM_NAME_LUT_LOOKUP19 = "去掉";
    //    private static final String ITEM_NAME_LUT_LOOKUP20 = "清冷";
    private static final String ITEM_NAME_LUT_LOOKUP21 = "安静";
    //    private static final String ITEM_NAME_LUT_LOOKUP22 = "去掉";
    //    private static final String ITEM_NAME_LUT_LOOKUP23 = "活泼";
    //    private static final String ITEM_NAME_LUT_LOOKUP24 = "黑白";
    private static final String ITEM_NAME_LUT_LOOKUP25 = "严肃";
    //    private static final String ITEM_NAME_LUT_LOOKUP26 = "去掉";
    private static final String ITEM_NAME_LUT_LOOKUP27 = "日光";

    //    private static final String ITEM_NAME_LUT_ALI_WHITE = "阿里白";
    private static final String ITEM_NAME_LUT_YOUNG = "元气";
    private static final String ITEM_NAME_LUT_REMEMBER = "怀旧";
    private static final String ITEM_NAME_LUT_ROMANTIC = "浪漫";
    private static final String ITEM_NAME_LUT_FRESH = "清新";
    private static final String ITEM_NAME_LUT_PURE = "纯真";
    private static final String ITEM_NAME_LUT_BLUES = "蓝调";
    private static final String ITEM_NAME_LUT_ORIGINAL = "超脱";

    public static BeautyTabBean getBeautyLookupTabBean() {
        return new BeautyTabBean(TAB_INDEX_LUT, TAB_NAME_LUT, false, true, getBeautyLookupItemBeans());
    }

    private static ArrayList<BeautyItemBean> getBeautyLookupItemBeans() {
        ArrayList<BeautyItemBean> itemBeans = new ArrayList<BeautyItemBean>();

        BeautyItemBean lookup1 = new BeautyItemBean(1, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP1, R.drawable.icon_beauty_lookup_lookup1_disable, R.drawable.icon_beauty_lookup_lookup1_enable, false);
        lookup1.setMaterialPath("race_res/lookups/lookup_1.png");

        BeautyItemBean lookup2 = new BeautyItemBean(2, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP2, R.drawable.icon_beauty_lookup_lookup2_disable, R.drawable.icon_beauty_lookup_lookup2_enable, false);
        lookup2.setMaterialPath("race_res/lookups/lookup_2.png");

        BeautyItemBean lookup3 = new BeautyItemBean(3, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP3, R.drawable.icon_beauty_lookup_lookup3_disable, R.drawable.icon_beauty_lookup_lookup3_enable, false);
        lookup3.setMaterialPath("race_res/lookups/lookup_3.png");

        BeautyItemBean lookup4 = new BeautyItemBean(4, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP4, R.drawable.icon_beauty_lookup_lookup4_disable, R.drawable.icon_beauty_lookup_lookup4_enable, false);
        lookup4.setMaterialPath("race_res/lookups/lookup_4.png");

        BeautyItemBean lookup5 = new BeautyItemBean(5, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP5, R.drawable.icon_beauty_lookup_lookup5_disable, R.drawable.icon_beauty_lookup_lookup5_enable, false);
        lookup5.setMaterialPath("race_res/lookups/lookup_5.png");

        BeautyItemBean lookup6 = new BeautyItemBean(6, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP6, R.drawable.icon_beauty_lookup_lookup6_disable, R.drawable.icon_beauty_lookup_lookup6_enable, false);
        lookup6.setMaterialPath("race_res/lookups/lookup_6.png");

        BeautyItemBean lookup7 = new BeautyItemBean(7, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP7, R.drawable.icon_beauty_lookup_lookup7_disable, R.drawable.icon_beauty_lookup_lookup7_enable, false);
        lookup7.setMaterialPath("race_res/lookups/lookup_7.png");

//        BeautyItemBean lookup8 = new BeautyItemBean(8, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP8, R.drawable.icon_beauty_lookup_lookup8_disable, R.drawable.icon_beauty_lookup_lookup8_enable, false);
//        lookup8.setMaterialPath("race_res/lookups/lookup_8.png");
//
//        BeautyItemBean lookup9 = new BeautyItemBean(9, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP9, R.drawable.icon_beauty_lookup_lookup9_disable, R.drawable.icon_beauty_lookup_lookup9_enable, false);
//        lookup9.setMaterialPath("race_res/lookups/lookup_9.png");

//        BeautyItemBean lookup10 = new BeautyItemBean(10, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP10, R.drawable.icon_beauty_lookup_lookup10_disable, R.drawable.icon_beauty_lookup_lookup10_enable, false);
//        lookup10.setMaterialPath("race_res/lookups/lookup_10.png");

        BeautyItemBean lookup11 = new BeautyItemBean(11, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP11, R.drawable.icon_beauty_lookup_lookup11_disable, R.drawable.icon_beauty_lookup_lookup11_enable, false);
        lookup11.setMaterialPath("race_res/lookups/lookup_11.png");

        BeautyItemBean lookup12 = new BeautyItemBean(12, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP12, R.drawable.icon_beauty_lookup_lookup12_disable, R.drawable.icon_beauty_lookup_lookup12_enable, false);
        lookup12.setMaterialPath("race_res/lookups/lookup_12.png");

//        BeautyItemBean lookup13 = new BeautyItemBean(13, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP13, R.drawable.icon_beauty_lookup_lookup13_disable, R.drawable.icon_beauty_lookup_lookup13_enable, false);
//        lookup13.setMaterialPath("race_res/lookups/lookup_13.png");
//
//        BeautyItemBean lookup14 = new BeautyItemBean(14, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP14, R.drawable.icon_beauty_lookup_lookup14_disable, R.drawable.icon_beauty_lookup_lookup14_enable, false);
//        lookup14.setMaterialPath("race_res/lookups/lookup_14.png");
//
//        BeautyItemBean lookup15 = new BeautyItemBean(15, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP15, R.drawable.icon_beauty_lookup_lookup15_disable, R.drawable.icon_beauty_lookup_lookup15_enable, false);
//        lookup15.setMaterialPath("race_res/lookups/lookup_15.png");

//        BeautyItemBean lookup16 = new BeautyItemBean(16, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP16, R.drawable.icon_beauty_lookup_lookup16_disable, R.drawable.icon_beauty_lookup_lookup16_enable, false);
//        lookup16.setMaterialPath("race_res/lookups/lookup_16.png");

//        BeautyItemBean lookup17 = new BeautyItemBean(17, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP17, R.drawable.icon_beauty_lookup_lookup17_disable, R.drawable.icon_beauty_lookup_lookup17_enable, false);
//        lookup17.setMaterialPath("race_res/lookups/lookup_17.png");

        BeautyItemBean lookup18 = new BeautyItemBean(18, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP18, R.drawable.icon_beauty_lookup_lookup18_disable, R.drawable.icon_beauty_lookup_lookup18_enable, false);
        lookup18.setMaterialPath("race_res/lookups/lookup_18.png");

//        BeautyItemBean lookup19 = new BeautyItemBean(19, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP19, R.drawable.icon_beauty_lookup_lookup19_disable, R.drawable.icon_beauty_lookup_lookup19_enable, false);
//        lookup19.setMaterialPath("race_res/lookups/lookup_19.png");

//        BeautyItemBean lookup20 = new BeautyItemBean(20, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP20, R.drawable.icon_beauty_lookup_lookup20_disable, R.drawable.icon_beauty_lookup_lookup20_enable, false);
//        lookup20.setMaterialPath("race_res/lookups/lookup_20.png");

        BeautyItemBean lookup21 = new BeautyItemBean(21, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP21, R.drawable.icon_beauty_lookup_lookup21_disable, R.drawable.icon_beauty_lookup_lookup21_enable, false);
        lookup21.setMaterialPath("race_res/lookups/lookup_21.png");

//        BeautyItemBean lookup22 = new BeautyItemBean(22, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP22, R.drawable.icon_beauty_lookup_lookup22_disable, R.drawable.icon_beauty_lookup_lookup22_enable, false);
//        lookup22.setMaterialPath("race_res/lookups/lookup_22.png");

//        BeautyItemBean lookup23 = new BeautyItemBean(23, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP23, R.drawable.icon_beauty_lookup_lookup23_disable, R.drawable.icon_beauty_lookup_lookup23_enable, false);
//        lookup23.setMaterialPath("race_res/lookups/lookup_23.png");

//        BeautyItemBean lookup24 = new BeautyItemBean(24, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP24, R.drawable.icon_beauty_lookup_lookup24_disable, R.drawable.icon_beauty_lookup_lookup24_enable, false);
//        lookup24.setMaterialPath("race_res/lookups/lookup_24.png");

        BeautyItemBean lookup25 = new BeautyItemBean(25, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP25, R.drawable.icon_beauty_lookup_lookup25_disable, R.drawable.icon_beauty_lookup_lookup25_enable, false);
        lookup25.setMaterialPath("race_res/lookups/lookup_25.png");

//        BeautyItemBean lookup26 = new BeautyItemBean(26, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP26, R.drawable.icon_beauty_lookup_lookup26_disable, R.drawable.icon_beauty_lookup_lookup26_enable, false);
//        lookup26.setMaterialPath("race_res/lookups/lookup_26.png");

        BeautyItemBean lookup27 = new BeautyItemBean(27, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_LOOKUP27, R.drawable.icon_beauty_lookup_lookup27_disable, R.drawable.icon_beauty_lookup_lookup27_enable, false);
        lookup27.setMaterialPath("race_res/lookups/lookup_27.png");

//        BeautyItemBean lookupAliWhite = new BeautyItemBean(28, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_ALI_WHITE, false);
//        lookupAliWhite.setMaterialPath("race_res/lookups/ali_white.png");
        BeautyItemBean lookupYoung = new BeautyItemBean(29, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_YOUNG, R.drawable.icon_beauty_lookup_young_disable, R.drawable.icon_beauty_lookup_young_enable, false);
        lookupYoung.setMaterialPath("race_res/lookups/元气色卡.png");

        BeautyItemBean lookupRemember = new BeautyItemBean(30, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_REMEMBER, R.drawable.icon_beauty_lookup_remember_disable, R.drawable.icon_beauty_lookup_remember_enable, false);
        lookupRemember.setMaterialPath("race_res/lookups/怀旧色卡.png");

        BeautyItemBean lookupRomantic = new BeautyItemBean(31, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_ROMANTIC, R.drawable.icon_beauty_lookup_romantic_disable, R.drawable.icon_beauty_lookup_romantic_enable, false);
        lookupRomantic.setMaterialPath("race_res/lookups/浪漫色卡.png");

        BeautyItemBean lookupFresh = new BeautyItemBean(32, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_FRESH, R.drawable.icon_beauty_lookup_fresh_disable, R.drawable.icon_beauty_lookup_fresh_enable, false);
        lookupFresh.setMaterialPath("race_res/lookups/清新色卡.png");

        BeautyItemBean lookupPure = new BeautyItemBean(33, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_PURE, R.drawable.icon_beauty_lookup_pure_disable, R.drawable.icon_beauty_lookup_pure_enable, false);
        lookupPure.setMaterialPath("race_res/lookups/纯真色卡.png");

        BeautyItemBean lookupBlue = new BeautyItemBean(34, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_BLUES, R.drawable.icon_beauty_lookup_blue_disable, R.drawable.icon_beauty_lookup_blue_enable, false);
        lookupBlue.setMaterialPath("race_res/lookups/蓝调色卡.png");

        BeautyItemBean lookupOriginal = new BeautyItemBean(35, BeautyItemBean.BeautyType.BEAUTY_LUT, ITEM_NAME_LUT_ORIGINAL, R.drawable.icon_beauty_lookup_original_disable, R.drawable.icon_beauty_lookup_original_enable, false);
        lookupOriginal.setMaterialPath("race_res/lookups/超脱色卡.png");

        itemBeans.add(lookup1);
        itemBeans.add(lookup2);
        itemBeans.add(lookup3);
        itemBeans.add(lookup4);
        itemBeans.add(lookup5);
        itemBeans.add(lookup6);
        itemBeans.add(lookup7);
//        itemBeans.add(lookup8);
//        itemBeans.add(lookup9);
//        itemBeans.add(lookup10);
        itemBeans.add(lookup11);
        itemBeans.add(lookup12);
//        itemBeans.add(lookup13);
//        itemBeans.add(lookup14);
//        itemBeans.add(lookup15);
//        itemBeans.add(lookup16);
//        itemBeans.add(lookup17);
        itemBeans.add(lookup18);
//        itemBeans.add(lookup19);
//        itemBeans.add(lookup20);
        itemBeans.add(lookup21);
//        itemBeans.add(lookup22);
//        itemBeans.add(lookup23);
//        itemBeans.add(lookup24);
        itemBeans.add(lookup25);
//        itemBeans.add(lookup26);
        itemBeans.add(lookup27);

//        itemBeans.add(lookupAliWhite);
        itemBeans.add(lookupYoung);
        itemBeans.add(lookupRemember);
        itemBeans.add(lookupRomantic);
        itemBeans.add(lookupFresh);
        itemBeans.add(lookupPure);
        itemBeans.add(lookupBlue);
        itemBeans.add(lookupOriginal);

        return itemBeans;
    }
}
