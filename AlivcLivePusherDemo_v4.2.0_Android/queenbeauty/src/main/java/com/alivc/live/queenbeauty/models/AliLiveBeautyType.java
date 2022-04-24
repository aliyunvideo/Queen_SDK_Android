package com.alivc.live.queenbeauty.models;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import androidx.annotation.IntDef;

import static com.alivc.live.queenbeauty.models.AliLiveBeautyType.kAliLiveFaceBuffing;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyType.kAliLiveFaceShape;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyType.kAliLiveHSV;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyType.kAliLiveLUT;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyType.kAliLiveMakeup;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyType.kAliLiveSkinBuffing;
import static com.alivc.live.queenbeauty.models.AliLiveBeautyType.kAliLiveSkinWhiting;

/**
 * 美颜滤镜类型
 */
@Retention(RetentionPolicy.SOURCE)
@IntDef({kAliLiveSkinBuffing,
        kAliLiveFaceBuffing,
        kAliLiveMakeup,
        kAliLiveFaceShape,
        kAliLiveSkinWhiting,
        kAliLiveHSV,
        kAliLiveLUT,
})
public @interface AliLiveBeautyType {
    /**
     * 磨皮、锐化
     */
    int kAliLiveSkinBuffing = 0;

    /**
     * 脸部磨皮（去眼袋、法令纹）
     */
    int kAliLiveFaceBuffing = 1;

    /**
     * 美妆
     */
    int kAliLiveMakeup = 2;

    /**
     * 美型
     */
    int kAliLiveFaceShape = 3;

    /**
     * 美白
     */
    int kAliLiveSkinWhiting = 4;

    /**
     * 色相饱和度明度
     */
    int kAliLiveHSV = 5;

    /**
     * 滤镜
     */
    int kAliLiveLUT = 6;
}
