package com.alivc.live.queenbeauty.models;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType.kAliLiveMakeupBlush;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType.kAliLiveMakeupEyeBrow;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType.kAliLiveMakeupEyeball;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType.kAliLiveMakeupHighlight;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType.kAliLiveMakeupMax;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType.kAliLiveMakeupMouth;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyMakeupType.kAliLiveMakeupWhole;

/**
 * 美妆参数
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({kAliLiveMakeupWhole,
        kAliLiveMakeupHighlight,
        kAliLiveMakeupEyeball,
        kAliLiveMakeupMouth,
        kAliLiveMakeupEyeBrow,
        kAliLiveMakeupBlush,
        kAliLiveMakeupMax,
})
public @interface AliLiveBeautyMakeupType {
    /**
     * 整妆
     */
    int kAliLiveMakeupWhole = 0;

    /**
     * 高光
     */
    int kAliLiveMakeupHighlight = 1;

    /**
     * 美瞳
     */
    int kAliLiveMakeupEyeball = 2;

    /**
     * 口红
     */
    int kAliLiveMakeupMouth = 3;

    /**
     * 眼妆
     */
    int kAliLiveMakeupEyeBrow = 5;

    /**
     * 腮红
     */
    int kAliLiveMakeupBlush = 6;

    /**
     * 最大值
     */
    int kAliLiveMakeupMax = 7;
}
