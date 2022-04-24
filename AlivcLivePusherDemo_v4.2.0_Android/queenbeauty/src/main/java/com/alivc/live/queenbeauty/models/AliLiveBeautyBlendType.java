package com.alivc.live.queenbeauty.models;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendAdd;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendAverage;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendColor;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendColorBurn;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendColorDodge;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendDarken;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendDifference;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendDivide;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendExclusion;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendGlow;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendHardLight;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendHardMix;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendHue;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendLighten;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendLinearBurn;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendLinearDodge;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendLinearLight;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendLuminosity;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendMax;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendMultiply;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendNegation;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendNormal;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendOverlay;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendPhoenix;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendPinLight;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendReflect;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendSaturation;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendScreen;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendSoftLight;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendSubtract;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyBlendType.kAliLiveBlendVividLight;

/**
 * 美妆混合模式
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({kAliLiveBlendNormal,
        kAliLiveBlendLighten,
        kAliLiveBlendDarken,
        kAliLiveBlendMultiply,
        kAliLiveBlendDivide,
        kAliLiveBlendAverage,
        kAliLiveBlendAdd,
        kAliLiveBlendSubtract,
        kAliLiveBlendDifference,
        kAliLiveBlendNegation,
        kAliLiveBlendExclusion,
        kAliLiveBlendScreen,
        kAliLiveBlendOverlay,
        kAliLiveBlendSoftLight,
        kAliLiveBlendHardLight,
        kAliLiveBlendColorDodge,
        kAliLiveBlendColorBurn,
        kAliLiveBlendLinearDodge,
        kAliLiveBlendLinearBurn,
        kAliLiveBlendLinearLight,
        kAliLiveBlendVividLight,
        kAliLiveBlendPinLight,
        kAliLiveBlendHardMix,
        kAliLiveBlendReflect,
        kAliLiveBlendGlow,
        kAliLiveBlendPhoenix,
        kAliLiveBlendHue,
        kAliLiveBlendSaturation,
        kAliLiveBlendLuminosity,
        kAliLiveBlendColor,
        kAliLiveBlendMax,
})
public @interface AliLiveBeautyBlendType {
    /**
     * 正常
     */
    int kAliLiveBlendNormal = 0;

    /**
     * 变亮
     */
    int kAliLiveBlendLighten = 1;

    /**
     * 变暗
     */
    int kAliLiveBlendDarken = 2;

    /**
     * 正片叠底
     */
    int kAliLiveBlendMultiply = 3;

    /**
     * 划分
     */
    int kAliLiveBlendDivide = 4;

    /**
     * 平均
     */
    int kAliLiveBlendAverage = 5;

    /**
     * 线性减淡
     */
    int kAliLiveBlendAdd = 6;

    /**
     * 减去
     */
    int kAliLiveBlendSubtract = 7;

    /**
     * 差值
     */
    int kAliLiveBlendDifference = 8;

    /**
     * 镜像
     */
    int kAliLiveBlendNegation = 9;

    /**
     * 排除
     */
    int kAliLiveBlendExclusion = 10;

    /**
     * 滤色
     */
    int kAliLiveBlendScreen = 11;

    /**
     * 叠加
     */
    int kAliLiveBlendOverlay = 12;

    /**
     * 柔光
     */
    int kAliLiveBlendSoftLight = 13;

    /**
     * 强光
     */
    int kAliLiveBlendHardLight = 14;

    /**
     * 颜色减淡
     */
    int kAliLiveBlendColorDodge = 15;

    /**
     * 颜色加深
     */
    int kAliLiveBlendColorBurn = 16;

    /**
     * 线性减淡
     */
    int kAliLiveBlendLinearDodge = 17;

    /**
     * 线性加深
     */
    int kAliLiveBlendLinearBurn = 18;

    /**
     * 线性光
     */
    int kAliLiveBlendLinearLight = 19;

    /**
     * 亮光
     */
    int kAliLiveBlendVividLight = 20;

    /**
     * 点光
     */
    int kAliLiveBlendPinLight = 21;

    /**
     * 实色混合
     */
    int kAliLiveBlendHardMix = 22;

    /**
     * 反射
     */
    int kAliLiveBlendReflect = 23;

    /**
     * 发光
     */
    int kAliLiveBlendGlow = 24;

    /**
     * 凤凰
     */
    int kAliLiveBlendPhoenix = 25;

    /**
     * 色相
     */
    int kAliLiveBlendHue = 26;

    /**
     * 饱和度
     */
    int kAliLiveBlendSaturation = 27;

    /**
     * 明亮
     */
    int kAliLiveBlendLuminosity = 28;

    /**
     * 颜色
     */
    int kAliLiveBlendColor = 29;

    /**
     * 最大值
     */
    int kAliLiveBlendMax = 999;
}
