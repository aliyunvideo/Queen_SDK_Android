/*
 *  Copyright (c) 2021 NetEase, Inc.  All rights reserved.
 *  Use of this source code is governed by a MIT license that can be found in the LICENSE file
 */

package com.beautyFaceunity;

import com.beautyFaceunity.entity.Filter;
import com.beautyFaceunity.param.BeautificationParam;

import java.util.ArrayList;

/**
 * 美颜滤镜枚举类
 * <p>
 * Created by tujh on 2018/1/30.
 */
public enum FilterEnum {

    origin(BeautificationParam.ORIGIN, com.beautyFaceunity.R.drawable.demo_icon_origin, com.beautyFaceunity.R.string.origin),

    ziran_1(BeautificationParam.ZIRAN_1, com.beautyFaceunity.R.drawable.demo_icon_natural_1, com.beautyFaceunity.R.string.ziran_1),
    ziran_2(BeautificationParam.ZIRAN_2, com.beautyFaceunity.R.drawable.demo_icon_natural_2, com.beautyFaceunity.R.string.ziran_2),
    ziran_3(BeautificationParam.ZIRAN_3, com.beautyFaceunity.R.drawable.demo_icon_natural_3, com.beautyFaceunity.R.string.ziran_3),
    ziran_4(BeautificationParam.ZIRAN_4, com.beautyFaceunity.R.drawable.demo_icon_natural_4, com.beautyFaceunity.R.string.ziran_4),
    ziran_5(BeautificationParam.ZIRAN_5, com.beautyFaceunity.R.drawable.demo_icon_natural_5, com.beautyFaceunity.R.string.ziran_5),
    ziran_6(BeautificationParam.ZIRAN_6, com.beautyFaceunity.R.drawable.demo_icon_natural_6, com.beautyFaceunity.R.string.ziran_6),
    ziran_7(BeautificationParam.ZIRAN_7, com.beautyFaceunity.R.drawable.demo_icon_natural_7, com.beautyFaceunity.R.string.ziran_7),
    ziran_8(BeautificationParam.ZIRAN_8, com.beautyFaceunity.R.drawable.demo_icon_natural_8, com.beautyFaceunity.R.string.ziran_8),

    zhiganhui_1(BeautificationParam.ZHIGANHUI_1, com.beautyFaceunity.R.drawable.demo_icon_texture_gray1, com.beautyFaceunity.R.string.zhiganhui_1),
    zhiganhui_2(BeautificationParam.ZHIGANHUI_2, com.beautyFaceunity.R.drawable.demo_icon_texture_gray2, com.beautyFaceunity.R.string.zhiganhui_2),
    zhiganhui_3(BeautificationParam.ZHIGANHUI_3, com.beautyFaceunity.R.drawable.demo_icon_texture_gray3, com.beautyFaceunity.R.string.zhiganhui_3),
    zhiganhui_4(BeautificationParam.ZHIGANHUI_4, com.beautyFaceunity.R.drawable.demo_icon_texture_gray4, com.beautyFaceunity.R.string.zhiganhui_4),
    zhiganhui_5(BeautificationParam.ZHIGANHUI_5, com.beautyFaceunity.R.drawable.demo_icon_texture_gray5, com.beautyFaceunity.R.string.zhiganhui_5),
    zhiganhui_6(BeautificationParam.ZHIGANHUI_6, com.beautyFaceunity.R.drawable.demo_icon_texture_gray6, com.beautyFaceunity.R.string.zhiganhui_6),
    zhiganhui_7(BeautificationParam.ZHIGANHUI_7, com.beautyFaceunity.R.drawable.demo_icon_texture_gray7, com.beautyFaceunity.R.string.zhiganhui_7),
    zhiganhui_8(BeautificationParam.ZHIGANHUI_8, com.beautyFaceunity.R.drawable.demo_icon_texture_gray8, com.beautyFaceunity.R.string.zhiganhui_8),

    mitao_1(BeautificationParam.MITAO_1, com.beautyFaceunity.R.drawable.demo_icon_peach1, com.beautyFaceunity.R.string.mitao_1),
    mitao_2(BeautificationParam.MITAO_2, com.beautyFaceunity.R.drawable.demo_icon_peach2, com.beautyFaceunity.R.string.mitao_2),
    mitao_3(BeautificationParam.MITAO_3, com.beautyFaceunity.R.drawable.demo_icon_peach3, com.beautyFaceunity.R.string.mitao_3),
    mitao_4(BeautificationParam.MITAO_4, com.beautyFaceunity.R.drawable.demo_icon_peach4, com.beautyFaceunity.R.string.mitao_4),
    mitao_5(BeautificationParam.MITAO_5, com.beautyFaceunity.R.drawable.demo_icon_peach5, com.beautyFaceunity.R.string.mitao_5),
    mitao_6(BeautificationParam.MITAO_6, com.beautyFaceunity.R.drawable.demo_icon_peach6, com.beautyFaceunity.R.string.mitao_6),
    mitao_7(BeautificationParam.MITAO_7, com.beautyFaceunity.R.drawable.demo_icon_peach7, com.beautyFaceunity.R.string.mitao_7),
    mitao_8(BeautificationParam.MITAO_8, com.beautyFaceunity.R.drawable.demo_icon_peach8, com.beautyFaceunity.R.string.mitao_8),

    bailiang_1(BeautificationParam.BAILIANG_1, com.beautyFaceunity.R.drawable.demo_icon_bailiang1, com.beautyFaceunity.R.string.bailiang_1),
    bailiang_2(BeautificationParam.BAILIANG_2, com.beautyFaceunity.R.drawable.demo_icon_bailiang2, com.beautyFaceunity.R.string.bailiang_2),
    bailiang_3(BeautificationParam.BAILIANG_3, com.beautyFaceunity.R.drawable.demo_icon_bailiang3, com.beautyFaceunity.R.string.bailiang_3),
    bailiang_4(BeautificationParam.BAILIANG_4, com.beautyFaceunity.R.drawable.demo_icon_bailiang4, com.beautyFaceunity.R.string.bailiang_4),
    bailiang_5(BeautificationParam.BAILIANG_5, com.beautyFaceunity.R.drawable.demo_icon_bailiang5, com.beautyFaceunity.R.string.bailiang_5),
    bailiang_6(BeautificationParam.BAILIANG_6, com.beautyFaceunity.R.drawable.demo_icon_bailiang6, com.beautyFaceunity.R.string.bailiang_6),
    bailiang_7(BeautificationParam.BAILIANG_7, com.beautyFaceunity.R.drawable.demo_icon_bailiang7, com.beautyFaceunity.R.string.bailiang_7),

    fennen_1(BeautificationParam.FENNEN_1, com.beautyFaceunity.R.drawable.demo_icon_fennen1, com.beautyFaceunity.R.string.fennen_1),
    fennen_2(BeautificationParam.FENNEN_2, com.beautyFaceunity.R.drawable.demo_icon_fennen2, com.beautyFaceunity.R.string.fennen_2),
    fennen_3(BeautificationParam.FENNEN_3, com.beautyFaceunity.R.drawable.demo_icon_fennen3, com.beautyFaceunity.R.string.fennen_3),
    //    fennen_4(BeautificationParam.FENNEN_4, R.drawable.demo_icon_fennen4, R.string.fennen_4),
    fennen_5(BeautificationParam.FENNEN_5, com.beautyFaceunity.R.drawable.demo_icon_fennen5, com.beautyFaceunity.R.string.fennen_5),
    fennen_6(BeautificationParam.FENNEN_6, com.beautyFaceunity.R.drawable.demo_icon_fennen6, com.beautyFaceunity.R.string.fennen_6),
    fennen_7(BeautificationParam.FENNEN_7, com.beautyFaceunity.R.drawable.demo_icon_fennen7, com.beautyFaceunity.R.string.fennen_7),
    fennen_8(BeautificationParam.FENNEN_8, com.beautyFaceunity.R.drawable.demo_icon_fennen8, com.beautyFaceunity.R.string.fennen_8),

    lengsediao_1(BeautificationParam.LENGSEDIAO_1, com.beautyFaceunity.R.drawable.demo_icon_lengsediao1, com.beautyFaceunity.R.string.lengsediao_1),
    lengsediao_2(BeautificationParam.LENGSEDIAO_2, com.beautyFaceunity.R.drawable.demo_icon_lengsediao2, com.beautyFaceunity.R.string.lengsediao_2),
    lengsediao_3(BeautificationParam.LENGSEDIAO_3, com.beautyFaceunity.R.drawable.demo_icon_lengsediao3, com.beautyFaceunity.R.string.lengsediao_3),
    lengsediao_4(BeautificationParam.LENGSEDIAO_4, com.beautyFaceunity.R.drawable.demo_icon_lengsediao4, com.beautyFaceunity.R.string.lengsediao_4),
    //    lengsediao_5(BeautificationParam.LENGSEDIAO_5, R.drawable.demo_icon_lengsediao5, R.string.lengsediao_5),
    //    lengsediao_6(BeautificationParam.LENGSEDIAO_6, R.drawable.demo_icon_lengsediao6, R.string.lengsediao_6),
    lengsediao_7(BeautificationParam.LENGSEDIAO_7, com.beautyFaceunity.R.drawable.demo_icon_lengsediao7, com.beautyFaceunity.R.string.lengsediao_7),
    lengsediao_8(BeautificationParam.LENGSEDIAO_8, com.beautyFaceunity.R.drawable.demo_icon_lengsediao8, com.beautyFaceunity.R.string.lengsediao_8),
    //    lengsediao_9(BeautificationParam.LENGSEDIAO_9, R.drawable.demo_icon_lengsediao9, R.string.lengsediao_9),
    //    lengsediao_10(BeautificationParam.LENGSEDIAO_10, R.drawable.demo_icon_lengsediao10, R.string.lengsediao_10),
    lengsediao_11(BeautificationParam.LENGSEDIAO_11, com.beautyFaceunity.R.drawable.demo_icon_lengsediao11, com.beautyFaceunity.R.string.lengsediao_11),

    nuansediao_1(BeautificationParam.NUANSEDIAO_1, com.beautyFaceunity.R.drawable.demo_icon_nuansediao1, com.beautyFaceunity.R.string.nuansediao_1),
    nuansediao_2(BeautificationParam.NUANSEDIAO_2, com.beautyFaceunity.R.drawable.demo_icon_nuansediao2, com.beautyFaceunity.R.string.nuansediao_2),
    //    nuansediao_3(BeautificationParam.NUANSEDIAO_3, R.drawable.demo_icon_nuansediao3, R.string.nuansediao_3),

    gexing_1(BeautificationParam.GEXING_1, com.beautyFaceunity.R.drawable.demo_icon_gexing1, com.beautyFaceunity.R.string.gexing_1),
    gexing_2(BeautificationParam.GEXING_2, com.beautyFaceunity.R.drawable.demo_icon_gexing2, com.beautyFaceunity.R.string.gexing_2),
    gexing_3(BeautificationParam.GEXING_3, com.beautyFaceunity.R.drawable.demo_icon_gexing3, com.beautyFaceunity.R.string.gexing_3),
    gexing_4(BeautificationParam.GEXING_4, com.beautyFaceunity.R.drawable.demo_icon_gexing4, com.beautyFaceunity.R.string.gexing_4),
    gexing_5(BeautificationParam.GEXING_5, com.beautyFaceunity.R.drawable.demo_icon_gexing5, com.beautyFaceunity.R.string.gexing_5),
    //    gexing_6(BeautificationParam.GEXING_6, R.drawable.demo_icon_gexing6, R.string.gexing_6),
    gexing_7(BeautificationParam.GEXING_7, com.beautyFaceunity.R.drawable.demo_icon_gexing7, com.beautyFaceunity.R.string.gexing_7),
    //    gexing_8(BeautificationParam.GEXING_8, R.drawable.demo_icon_gexing8, R.string.gexing_8),
    //    gexing_9(BeautificationParam.GEXING_9, R.drawable.demo_icon_gexing9, R.string.gexing_9),
    gexing_10(BeautificationParam.GEXING_10, com.beautyFaceunity.R.drawable.demo_icon_gexing10, com.beautyFaceunity.R.string.gexing_10),
    gexing_11(BeautificationParam.GEXING_11, com.beautyFaceunity.R.drawable.demo_icon_gexing11, com.beautyFaceunity.R.string.gexing_11),

    xiaoqingxin_1(BeautificationParam.XIAOQINGXIN_1, com.beautyFaceunity.R.drawable.demo_icon_xiaoqingxin1, com.beautyFaceunity.R.string.xiaoqingxin_1),
    //    xiaoqingxin_2(BeautificationParam.XIAOQINGXIN_2, R.drawable.demo_icon_xiaoqingxin2, R.string.xiaoqingxin_2),
    xiaoqingxin_3(BeautificationParam.XIAOQINGXIN_3, com.beautyFaceunity.R.drawable.demo_icon_xiaoqingxin3, com.beautyFaceunity.R.string.xiaoqingxin_3),
    xiaoqingxin_4(BeautificationParam.XIAOQINGXIN_4, com.beautyFaceunity.R.drawable.demo_icon_xiaoqingxin4, com.beautyFaceunity.R.string.xiaoqingxin_4),
    //    xiaoqingxin_5(BeautificationParam.XIAOQINGXIN_5, R.drawable.demo_icon_xiaoqingxin5, R.string.xiaoqingxin_5),
    xiaoqingxin_6(BeautificationParam.XIAOQINGXIN_6, com.beautyFaceunity.R.drawable.demo_icon_xiaoqingxin6, com.beautyFaceunity.R.string.xiaoqingxin_6),

    heibai_1(BeautificationParam.HEIBAI_1, com.beautyFaceunity.R.drawable.demo_icon_heibai1, com.beautyFaceunity.R.string.heibai_1),
    heibai_2(BeautificationParam.HEIBAI_2, com.beautyFaceunity.R.drawable.demo_icon_heibai2, com.beautyFaceunity.R.string.heibai_2),
    heibai_3(BeautificationParam.HEIBAI_3, com.beautyFaceunity.R.drawable.demo_icon_heibai3, com.beautyFaceunity.R.string.heibai_3),
    heibai_4(BeautificationParam.HEIBAI_4, com.beautyFaceunity.R.drawable.demo_icon_heibai4, com.beautyFaceunity.R.string.heibai_4);
    //    heibai_5(BeautificationParam.HEIBAI_5, R.drawable.demo_icon_heibai5, R.string.heibai_5);

    private final String name;
    private final int iconId;
    private final int nameId;

    FilterEnum(String name, int iconId, int nameId) {
        this.name = name;
        this.iconId = iconId;
        this.nameId = nameId;
    }

    public static ArrayList<Filter> getFiltersByFilterType() {
        FilterEnum[] values = FilterEnum.values();
        ArrayList<Filter> filters = new ArrayList<>(values.length);
        for (FilterEnum value : values) {
            filters.add(value.create());
        }
        return filters;
    }

    public Filter create() {
        return new Filter(name, iconId, nameId);
    }
}
