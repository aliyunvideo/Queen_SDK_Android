package com.alivc.live.queenbeauty.injector;

import com.alivc.live.beautyui.bean.BeautyItemBean;
import com.alivc.live.beautyui.bean.BeautyTabBean;
import com.alivc.live.queenbeauty.R;

import java.util.ArrayList;

import static com.alivc.live.queenbeauty.constant.QueenConstant.ITEM_NAME_NONE;
import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_INDEX_MATERIAL;
import static com.alivc.live.queenbeauty.constant.QueenConstant.TAB_NAME_MATERIAL;

public class QueenStickerDataInjector {
    /// *** 贴纸 *** ///
    private static final String ITEM_NAME_STICKER_BAIYANG = "白羊";
    //    private static final String ITEM_NAME_STICKER_BANDIANGOU = "斑点狗";
    private static final String ITEM_NAME_STICKER_BAZIHU = "八字胡";
    //    private static final String ITEM_NAME_STICKER_BIANHUAN = "变换";
    //    private static final String ITEM_NAME_STICKER_BIAOYAN = "表演";
    //    private static final String ITEM_NAME_STICKER_BIXIN = "比心";
    private static final String ITEM_NAME_STICKER_BUOU = "布偶";
    //    private static final String ITEM_NAME_STICKER_CAHAN = "擦汗";
    //    private static final String ITEM_NAME_STICKER_CAIHONG = "彩虹";
    //    private static final String ITEM_NAME_STICKER_CHABEI = "茶杯";
    //    private static final String ITEM_NAME_STICKER_CHUNV = "处女座";
    //    private static final String ITEM_NAME_STICKER_CHUSHI = "厨师";
    //    private static final String ITEM_NAME_STICKER_CO = "com";
    //    private static final String ITEM_NAME_STICKER_DAKANG = "GDP";
    //    private static final String ITEM_NAME_STICKER_DAOFENG = "炮舰";
    //    private static final String ITEM_NAME_STICKER_DUANJIAO = "断角";
    private static final String ITEM_NAME_STICKER_EMOJI = "小黄脸";
    //    private static final String ITEM_NAME_STICKER_ERJI = "耳机";
    //    private static final String ITEM_NAME_STICKER_FENSEHUZI = "粉色胡子";
    //    private static final String ITEM_NAME_STICKER_GAOBAI = "告白";
    //    private static final String ITEM_NAME_STICKER_GUO = "背锅";
    //    private static final String ITEM_NAME_STICKER_GUSHI = "故事";
    //    private static final String ITEM_NAME_STICKER_HAI = "嗨起来";
    //    private static final String ITEM_NAME_STICKER_HONGSHENG = "红绳";
    private static final String ITEM_NAME_STICKER_HOUZI = "猴子";
    private static final String ITEM_NAME_STICKER_HUXU = "胡须";
    //    private static final String ITEM_NAME_STICKER_JINNIU = "金牛座";
    //    private static final String ITEM_NAME_STICKER_JUXIE = "巨蟹座";
    //    private static final String ITEM_NAME_STICKER_KPI = "kpi";
    //    private static final String ITEM_NAME_STICKER_KUQIAN = "哭钱";
    //    private static final String ITEM_NAME_STICKER_LAODONG = "劳动";
    //    private static final String ITEM_NAME_STICKER_LIWUNE = "礼物呢";
    //    private static final String ITEM_NAME_STICKER_MAJIANG = "麻将";
    private static final String ITEM_NAME_STICKER_MAOERDUO = "猫耳朵";
    //    private static final String ITEM_NAME_STICKER_MAOQIU = "毛球";
    //    private static final String ITEM_NAME_STICKER_MIANHUATANG = "棉花糖";
    //    private static final String ITEM_NAME_STICKER_MOJIE = "摩羯座";
    //    private static final String ITEM_NAME_STICKER_NANPENGYOU = "男朋友";
    //    private static final String ITEM_NAME_STICKER_NIHONG = "霓虹";

    //    private static final String ITEM_NAME_STICKER_PIAOCHONG = "瓢虫";
    //    private static final String ITEM_NAME_STICKER_QUMOSHI = "驱魔师";
    private static final String ITEM_NAME_STICKER_SHESHOU = "射手座";
    private static final String ITEM_NAME_STICKER_SHOUHUIHUZI = "手绘胡子";
    //    private static final String ITEM_NAME_STICKER_SHUICAIHUZI = "圣诞麋鹿";
    //    private static final String ITEM_NAME_STICKER_SHUICAIHUZI1572 = "圣诞萌鼠";
    //    private static final String ITEM_NAME_STICKER_SHUICAIHUZI1573 = "圣诞帽";
    //    private static final String ITEM_NAME_STICKER_SHUICAIHUZI1574 = "圣诞树";
    //    private static final String ITEM_NAME_STICKER_SHUICAIHUZI1575 = "新年快乐";
    //    private static final String ITEM_NAME_STICKER_SHUICAIHUZI2433 = "圣诞胡子";
    //    private static final String ITEM_NAME_STICKER_TOUSHI = "圣诞头饰";
    //    private static final String ITEM_NAME_STICKER_TUANZHANG = "团长";
    private static final String ITEM_NAME_STICKER_TUZI = "兔子";
    //    private static final String ITEM_NAME_STICKER_WOTONGYI = "我同意";
    //    private static final String ITEM_NAME_STICKER_XIAMU = "召唤";
    //    private static final String ITEM_NAME_STICKER_XIEYAN = "邪眼";
    //    private static final String ITEM_NAME_STICKER_YANJING = "眼镜";
    //    private static final String ITEM_NAME_STICKER_YANZHUANG = "眼妆";
    //    private static final String ITEM_NAME_STICKER_YOUNAI = "羞涩";


    /// *** 贴纸 *** ///
    public static BeautyTabBean getBeautyStickerTabBean() {
        return new BeautyTabBean(TAB_INDEX_MATERIAL, TAB_NAME_MATERIAL, false, false, getBeautyStickerItemBeans());
    }

    private static ArrayList<BeautyItemBean> getBeautyStickerItemBeans() {
        ArrayList<BeautyItemBean> itemBeans = new ArrayList<BeautyItemBean>();

        BeautyItemBean stickerNone = new BeautyItemBean(-1, BeautyItemBean.BeautyType.BEAUTY_STICKER,
                ITEM_NAME_NONE, R.drawable.icon_beauty_item_none_disable, R.drawable.icon_beauty_item_none_enable,
                false);

        BeautyItemBean stickerBaiyang = new BeautyItemBean(0, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_BAIYANG, R.drawable.icon_beauty_sticker_baiyang, false);
        stickerBaiyang.setMaterialPath("race_res/sticker/baiyang");

//        BeautyItemBean stickerBandiangou = new BeautyItemBean(1, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_BANDIANGOU);
//        stickerBandiangou.setMaterialPath("race_res/sticker/bandiangou");
//
        BeautyItemBean stickerBazihu = new BeautyItemBean(2, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_BAZIHU, R.drawable.icon_beauty_sticker_bazihu, false);
        stickerBazihu.setMaterialPath("race_res/sticker/bazihu");

//        BeautyItemBean stickerBianhuan = new BeautyItemBean(3, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_BIANHUAN);
//        stickerBianhuan.setMaterialPath("race_res/sticker/bianhuan");
//
//        BeautyItemBean stickerBiaoyan = new BeautyItemBean(4, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_BIAOYAN);
//        stickerBiaoyan.setMaterialPath("race_res/sticker/biaoyan");
//
//        BeautyItemBean stickerBixin = new BeautyItemBean(5, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_BIXIN);
//        stickerBixin.setMaterialPath("race_res/sticker/bixin");
//
        BeautyItemBean stickerBuou = new BeautyItemBean(6, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_BUOU, R.drawable.icon_beauty_sticker_buou, false);
        stickerBuou.setMaterialPath("race_res/sticker/buou");

//        BeautyItemBean stickerCahan = new BeautyItemBean(7, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_CAHAN);
//        stickerCahan.setMaterialPath("race_res/sticker/cahan");
//
//        BeautyItemBean stickerCaihong = new BeautyItemBean(8, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_CAIHONG);
//        stickerCaihong.setMaterialPath("race_res/sticker/caihong");
//
//        BeautyItemBean stickerChabei = new BeautyItemBean(9, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_CHABEI);
//        stickerChabei.setMaterialPath("race_res/sticker/chabei");
//
//        BeautyItemBean stickerChunv = new BeautyItemBean(10, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_CHUNV);
//        stickerChunv.setMaterialPath("race_res/sticker/chunv");
//
//        BeautyItemBean stickerChushi = new BeautyItemBean(11, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_CHUSHI);
//        stickerChushi.setMaterialPath("race_res/sticker/chushi");
//
//        BeautyItemBean stickerCo = new BeautyItemBean(12, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_CO);
//        stickerCo.setMaterialPath("race_res/sticker/co");
//
//        BeautyItemBean stickerDakang = new BeautyItemBean(13, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_DAKANG);
//        stickerDakang.setMaterialPath("race_res/sticker/dakang");
//
//        BeautyItemBean stickerDaofeng = new BeautyItemBean(14, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_DAOFENG);
//        stickerDaofeng.setMaterialPath("race_res/sticker/daofeng");
//
//        BeautyItemBean stickerDuanjiao = new BeautyItemBean(15, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_DUANJIAO);
//        stickerDuanjiao.setMaterialPath("race_res/sticker/duanjiao");
//
        BeautyItemBean stickerEmoji = new BeautyItemBean(16, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_EMOJI, R.drawable.icon_beauty_sticker_emoji, false);
        stickerEmoji.setMaterialPath("race_res/sticker/emoji");
//
//        BeautyItemBean stickerErji = new BeautyItemBean(17, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_ERJI);
//        stickerErji.setMaterialPath("race_res/sticker/erji");
//
//        BeautyItemBean stickerFensehuzi = new BeautyItemBean(18, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_FENSEHUZI);
//        stickerFensehuzi.setMaterialPath("race_res/sticker/fensehuzi");
//
//        BeautyItemBean stickerGaobai = new BeautyItemBean(19, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_GAOBAI);
//        stickerGaobai.setMaterialPath("race_res/sticker/gaobai");
//
//        BeautyItemBean stickerGuo = new BeautyItemBean(20, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_GUO);
//        stickerGuo.setMaterialPath("race_res/sticker/guo");
//
//        BeautyItemBean stickerGushi = new BeautyItemBean(21, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_GUSHI);
//        stickerGushi.setMaterialPath("race_res/sticker/gushi");
//
//        BeautyItemBean stickerHai = new BeautyItemBean(22, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_HAI);
//        stickerHai.setMaterialPath("race_res/sticker/hai");
//
//        BeautyItemBean stickerHongsheng = new BeautyItemBean(23, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_HONGSHENG);
//        stickerHongsheng.setMaterialPath("race_res/sticker/hongsheng");
//
        BeautyItemBean stickerHouzi = new BeautyItemBean(24, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_HOUZI, R.drawable.icon_beauty_sticker_houzi, false);
        stickerHouzi.setMaterialPath("race_res/sticker/houzi");

        BeautyItemBean stickerHuxu = new BeautyItemBean(25, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_HUXU, R.drawable.icon_beauty_sticker_huxu, false);
        stickerHuxu.setMaterialPath("race_res/sticker/huxu");

//        BeautyItemBean stickerJinniu = new BeautyItemBean(26, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_JINNIU);
//        stickerJinniu.setMaterialPath("race_res/sticker/jinniu");
//
//        BeautyItemBean stickerJuxie = new BeautyItemBean(27, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_JUXIE);
//        stickerJuxie.setMaterialPath("race_res/sticker/juxie");
//
//        BeautyItemBean stickerKpi = new BeautyItemBean(28, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_KPI);
//        stickerKpi.setMaterialPath("race_res/sticker/kpi");
//
//        BeautyItemBean stickerKuqian = new BeautyItemBean(29, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_KUQIAN);
//        stickerKuqian.setMaterialPath("race_res/sticker/kuqian");
//
//        BeautyItemBean stickerLaodong = new BeautyItemBean(30, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_LAODONG);
//        stickerLaodong.setMaterialPath("race_res/sticker/laodong");
//
//        BeautyItemBean stickerLiwune = new BeautyItemBean(31, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_LIWUNE);
//        stickerLiwune.setMaterialPath("race_res/sticker/liwune");
//
//        BeautyItemBean stickerMajiang = new BeautyItemBean(32, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_MAJIANG);
//        stickerMajiang.setMaterialPath("race_res/sticker/majiang");
//
        BeautyItemBean stickerMaoerduo = new BeautyItemBean(33, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_MAOERDUO, R.drawable.icon_beauty_sticker_maoerduo, false);
        stickerMaoerduo.setMaterialPath("race_res/sticker/maoerduo");

//        BeautyItemBean stickerMaoqiu = new BeautyItemBean(34, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_MAOQIU);
//        stickerMaoqiu.setMaterialPath("race_res/sticker/maoqiu");
//
//        BeautyItemBean stickerMianhuatang = new BeautyItemBean(35, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_MIANHUATANG);
//        stickerMianhuatang.setMaterialPath("race_res/sticker/mianhuatang");
//
//        BeautyItemBean stickerMojie = new BeautyItemBean(36, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_MOJIE);
//        stickerMojie.setMaterialPath("race_res/sticker/mojie");
//
//        BeautyItemBean stickerNanpengyou = new BeautyItemBean(37, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_NANPENGYOU);
//        stickerNanpengyou.setMaterialPath("race_res/sticker/nanpengyou");
//
//        BeautyItemBean stickerNihong = new BeautyItemBean(38, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_NIHONG);
//        stickerNihong.setMaterialPath("race_res/sticker/nihong");

//        BeautyItemBean stickerPiaochong = new BeautyItemBean(39, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_PIAOCHONG, R.drawable.icon_beauty_sticker_piaochong, false);
//        stickerPiaochong.setMaterialPath("race_res/sticker/piaochong");
//
//        BeautyItemBean stickerQumoshi = new BeautyItemBean(40, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_QUMOSHI, R.drawable.icon_beauty_sticker_qumoshi, false);
//        stickerQumoshi.setMaterialPath("race_res/sticker/qumoshi");
//
        BeautyItemBean stickerSheshou = new BeautyItemBean(41, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHESHOU, R.drawable.icon_beauty_sticker_sheshou, false);
        stickerSheshou.setMaterialPath("race_res/sticker/sheshou");

        BeautyItemBean stickerShouhuihuzi = new BeautyItemBean(42, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHOUHUIHUZI, R.drawable.icon_beauty_sticker_shouhuihuzi, false);
        stickerShouhuihuzi.setMaterialPath("race_res/sticker/shouhuihuzi");

//        BeautyItemBean stickerShuicaihuzi = new BeautyItemBean(43, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHUICAIHUZI, R.drawable.icon_beauty_sticker_shuicaihuzi, false);
//        stickerShuicaihuzi.setMaterialPath("race_res/sticker/shuicaihuzi");

//        BeautyItemBean stickerShuicaihuzi1572 = new BeautyItemBean(44, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHUICAIHUZI1572, false);
//        stickerShuicaihuzi1572.setMaterialPath("race_res/sticker/shuicaihuzi_1572");
//
//        BeautyItemBean stickerShuicaihuzi1573 = new BeautyItemBean(45, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHUICAIHUZI1573, false);
//        stickerShuicaihuzi1573.setMaterialPath("race_res/sticker/shuicaihuzi_1573");

//        BeautyItemBean stickerShuicaihuzi1574 = new BeautyItemBean(46, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHUICAIHUZI1574, R.drawable.icon_beauty_sticker_shuicaihuzi1574, false);
//        stickerShuicaihuzi1574.setMaterialPath("race_res/sticker/shuicaihuzi_1574");
//
//        BeautyItemBean stickerShuicaihuzi1575 = new BeautyItemBean(47, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHUICAIHUZI1575, false);
//        stickerShuicaihuzi1575.setMaterialPath("race_res/sticker/shuicaihuzi_1575");

//        BeautyItemBean stickerShuicaihuzi2433 = new BeautyItemBean(48, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_SHUICAIHUZI2433, R.drawable.icon_beauty_sticker_shuicaihuzi2433, false);
//        stickerShuicaihuzi2433.setMaterialPath("race_res/sticker/shuicaihuzi_2433");

//        BeautyItemBean stickerToushi = new BeautyItemBean(49, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_TOUSHI, R.drawable.icon_beauty_sticker_toushi, false);
//        stickerToushi.setMaterialPath("race_res/sticker/toushi");
//
//        BeautyItemBean stickerTuanzhang = new BeautyItemBean(50, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_TUANZHANG, R.drawable.icon_beauty_sticker_tuanzhang, false);
//        stickerTuanzhang.setMaterialPath("race_res/sticker/tuanzhang");

        BeautyItemBean stickerTuzi = new BeautyItemBean(51, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_TUZI, R.drawable.icon_beauty_sticker_tuzi, false);
        stickerTuzi.setMaterialPath("race_res/sticker/tuzi");

//        BeautyItemBean stickerWotongyi = new BeautyItemBean(52, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_WOTONGYI, R.drawable.icon_beauty_sticker_wotongyi, false);
//        stickerWotongyi.setMaterialPath("race_res/sticker/wotongyi");
//
//        BeautyItemBean stickerXiamu = new BeautyItemBean(53, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_XIAMU, R.drawable.icon_beauty_sticker_xiamu, false);
//        stickerXiamu.setMaterialPath("race_res/sticker/xiamu");
//
//        BeautyItemBean stickerXieyan = new BeautyItemBean(54, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_XIEYAN, R.drawable.icon_beauty_sticker_xieyan, false);
//        stickerXieyan.setMaterialPath("race_res/sticker/xieyan");
//
//        BeautyItemBean stickerYanjing = new BeautyItemBean(55, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_YANJING, R.drawable.icon_beauty_sticker_yanjing, false);
//        stickerYanjing.setMaterialPath("race_res/sticker/yanjing");
//
//        BeautyItemBean stickerYanzhuang = new BeautyItemBean(56, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_YANZHUANG, R.drawable.icon_beauty_sticker_yanzhuang, false);
//        stickerYanzhuang.setMaterialPath("race_res/sticker/yanzhuang");
//
//        BeautyItemBean stickerYounai = new BeautyItemBean(57, BeautyItemBean.BeautyType.BEAUTY_STICKER, ITEM_NAME_STICKER_YOUNAI, R.drawable.icon_beauty_sticker_younai, false);
//        stickerYounai.setMaterialPath("race_res/sticker/younai");

        itemBeans.add(stickerNone);

        itemBeans.add(stickerBaiyang);
//        itemBeans.add(stickerBandiangou);
        itemBeans.add(stickerBazihu);
//        itemBeans.add(stickerBianhuan);
//        itemBeans.add(stickerBiaoyan);
//        itemBeans.add(stickerBixin);
        itemBeans.add(stickerBuou);
//        itemBeans.add(stickerCahan);
//        itemBeans.add(stickerCaihong);
//        itemBeans.add(stickerChabei);
//        itemBeans.add(stickerChunv);
//        itemBeans.add(stickerChushi);
//        itemBeans.add(stickerCo);
//        itemBeans.add(stickerDakang);
//        itemBeans.add(stickerDaofeng);
//        itemBeans.add(stickerDuanjiao);
        itemBeans.add(stickerEmoji);
//        itemBeans.add(stickerErji);
//        itemBeans.add(stickerFensehuzi);
//        itemBeans.add(stickerGaobai);
//        itemBeans.add(stickerGuo);
//        itemBeans.add(stickerGushi);
//        itemBeans.add(stickerHai);
//        itemBeans.add(stickerHongsheng);
        itemBeans.add(stickerHouzi);
        itemBeans.add(stickerHuxu);
//        itemBeans.add(stickerJinniu);
//        itemBeans.add(stickerJuxie);
//        itemBeans.add(stickerKpi);
//        itemBeans.add(stickerKuqian);
//        itemBeans.add(stickerLaodong);
//        itemBeans.add(stickerLiwune);
//        itemBeans.add(stickerMajiang);
        itemBeans.add(stickerMaoerduo);
//        itemBeans.add(stickerMaoqiu);
//        itemBeans.add(stickerMianhuatang);
//        itemBeans.add(stickerMojie);
//        itemBeans.add(stickerNanpengyou);
//        itemBeans.add(stickerNihong);

//        itemBeans.add(stickerPiaochong);
//        itemBeans.add(stickerQumoshi);
        itemBeans.add(stickerSheshou);
        itemBeans.add(stickerShouhuihuzi);
//        itemBeans.add(stickerShuicaihuzi);
//        itemBeans.add(stickerShuicaihuzi1572);
//        itemBeans.add(stickerShuicaihuzi1573);
//        itemBeans.add(stickerShuicaihuzi1574);
//        itemBeans.add(stickerShuicaihuzi1575);
//        itemBeans.add(stickerShuicaihuzi2433);
//        itemBeans.add(stickerToushi);
//        itemBeans.add(stickerTuanzhang);
        itemBeans.add(stickerTuzi);
//        itemBeans.add(stickerWotongyi);
//        itemBeans.add(stickerXiamu);
//        itemBeans.add(stickerXieyan);
//        itemBeans.add(stickerYanjing);
//        itemBeans.add(stickerYanzhuang);
//        itemBeans.add(stickerYounai);

        return itemBeans;
    }

}
