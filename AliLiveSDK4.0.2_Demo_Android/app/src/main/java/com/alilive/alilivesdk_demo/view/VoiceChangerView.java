package com.alilive.alilivesdk_demo.view;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.AliLiveConfigListener;
import com.alilive.alilivesdk_demo.listener.AliLiveVoiceChangeListener;
import com.alilive.alilivesdk_demo.wheel.base.WheelItem;
import com.alilive.alilivesdk_demo.wheel.dialog.ColumnWheelDialog;
import com.alivc.live.AliLiveConstants;

public class VoiceChangerView extends FrameLayout {
    /**
     * 变声
     */
    private TextView mVoiceChangerModeTextView;
    private ColumnWheelDialog<WheelItem> mDialog;
    private AliLiveConstants.AliLiveVoiceChangerMode mAliLiveVoiceChangerMode = AliLiveConstants.AliLiveVoiceChangerMode.AliLiveVoiceChanger_OFF;
    private Button mSubmitButton;
    private AliLiveVoiceChangeListener mAliLiveVoiceChangeListener;
    private AliLiveConfigListener mAliLiveVoiceConfigListener;


    public void setmAliLiveVoiceConfigListener(AliLiveConfigListener mAliLiveVoiceConfigListener) {
        this.mAliLiveVoiceConfigListener = mAliLiveVoiceConfigListener;
    }

    public VoiceChangerView(Context context) {
        super(context);
        init(context);
    }

    public VoiceChangerView(Context context,AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public VoiceChangerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context){
        LayoutInflater.from(context).inflate(R.layout.view_voice_changer_config,this,true);
        mSubmitButton = findViewById(R.id.live_config_submit);
        mVoiceChangerModeTextView = findViewById(R.id.tv_voice_changer_mode);
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAliLiveVoiceChangeListener != null){
                    mAliLiveVoiceChangeListener.onAliLiveVoiceChangerMode(mAliLiveVoiceChangerMode);
                }
                setVisibility(View.GONE);
            }
        });
    }

    private AliLiveConstants.AliLiveVoiceChangerMode getAliLiveVoiceChangerMode(String showText) {
        switch (showText){
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

    public void setAliLiveVoiceChangeListener(AliLiveVoiceChangeListener listener){
        this.mAliLiveVoiceChangeListener = listener;
    }
}
