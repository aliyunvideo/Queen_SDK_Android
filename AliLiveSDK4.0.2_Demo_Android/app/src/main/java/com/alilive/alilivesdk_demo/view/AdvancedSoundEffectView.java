package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.AliLiveConfigListener;
import com.alivc.live.AliLiveConstants;

public class AdvancedSoundEffectView extends FrameLayout {

    private AliLiveConfigView mLiveCofigView;
    private BGMView mLiveBgmView;
    private TextView mLiveClose;
    private RelativeLayout mMusicBg;
    private LinearLayout mOther;
    private TextView mBack;
    private SecondView mModeSecondView;
    private AliLiveConfigListener mAliLiveConfigListener;
    private int mReverbPosition;//混响postion
    private int mChangerPosition;//变声postion

    public AdvancedSoundEffectView(Context context) {
        super(context);
        init(context);
    }

    public AdvancedSoundEffectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AdvancedSoundEffectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_adcanved_sound_effect_config, this, true);
        mLiveCofigView = findViewById(R.id.view_live_config);
        mLiveBgmView = findViewById(R.id.view_live_bgm);
        mLiveClose = findViewById(R.id.iv_sound_back);
        mMusicBg = findViewById(R.id.rl_bakground);
        mOther = findViewById(R.id.ll_other);
        mBack = mLiveBgmView.findViewById(R.id.tv_bg_back);
        mModeSecondView = findViewById(R.id.view_second);
        initListener();
    }

    private void initListener() {
        mLiveClose.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                setVisibility(View.GONE);
            }
        });
        mMusicBg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOther.setVisibility(GONE);
                mLiveBgmView.setVisibility(VISIBLE);
            }
        });
        mBack.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mOther.setVisibility(VISIBLE);
                mLiveBgmView.setVisibility(GONE);
            }
        });

        mLiveCofigView.setOnModelClick(new AliLiveConfigView.OnModelClick() {
            @Override
            public void onClick(String[] mReverbMode, String title, String type) {
                mModeSecondView.setVisibility(VISIBLE);
                mOther.setVisibility(GONE);
                if ("混响".equalsIgnoreCase(type)) {
                    mModeSecondView.setData(mReverbMode, type, mReverbPosition);
                }else{
                    mModeSecondView.setData(mReverbMode, type, mChangerPosition);
                }
                mModeSecondView.setmTitle(title);
            }
        });

        mModeSecondView.setOnItemClickListener(new SecondView.OnClickListener() {
            @Override
            public void onClick(String content, String type,int position) {
                if ("混响".equalsIgnoreCase(type)) {
                    if (mAliLiveConfigListener != null) {
                        mAliLiveConfigListener.onAliLiveReverbMode(getAliLiveReverbMode(content));
                    }
                    mReverbPosition = position;
                    ((TextView) mLiveCofigView.findViewById(R.id.tv_reverb_mode)).setText(content);
                } else if ("变声".equalsIgnoreCase(type)) {
                    if (mAliLiveConfigListener != null) {
                        ((TextView) mLiveCofigView.findViewById(R.id.view_change).findViewById(R.id.tv_voice_changer_mode)).setText(content);
                        mAliLiveConfigListener.onAliLiveVoiceChangerMode(getAliLiveVoiceChangerMode(content));
                    }
                    mChangerPosition = position;
                }
            }

            @Override
            public void onBack() {
                mModeSecondView.setVisibility(GONE);
                mOther.setVisibility(VISIBLE);
            }
        });

    }

    public void setAliLiveConfigListener(AliLiveConfigListener listener) {
        this.mAliLiveConfigListener = listener;
    }

    public BGMView getBGMView() {
        return mLiveBgmView;
    }

    public AliLiveConfigView getLiveConfigView() {
        return mLiveCofigView;
    }

    private AliLiveConstants.AliLiveReverbMode getAliLiveReverbMode(String showText) {
        switch (showText) {
            case "无效果":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Off;
            case "人声 |":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Vocal_I;
            case "人声 ||":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Vocal_II;
            case "澡堂":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Bathroom;
            case "明亮小房间":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Small_Room_Bright;
            case "黑暗小房间":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Small_Room_Dark;
            case "中等房间":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Medium_Room;
            case "大房间":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Large_Room;
            case "教堂走廊":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Church_Hall;
            case "大教堂":
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Cathedral;
            default:
                return AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Off;
        }
    }

    private AliLiveConstants.AliLiveVoiceChangerMode getAliLiveVoiceChangerMode(String showText) {
        switch (showText) {
            case "无效果":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_OFF;
            case "老人":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_Oldman;
            case "男孩":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_Babyboy;
            case "女孩":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_Babygirl;
            case "机器人":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_Robot;
            case "大魔王":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_Daimo;
            case "KTV":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_Ktv;
            case "回声":
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_Echo;
            default:
                return AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_OFF;
        }
    }
}
