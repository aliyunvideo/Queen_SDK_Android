package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.AliLiveConfigListener;
import com.alilive.alilivesdk_demo.wheel.base.WheelItem;
import com.alilive.alilivesdk_demo.wheel.dialog.ColumnWheelDialog;
import com.alivc.live.AliLiveConstants;

import org.w3c.dom.Text;

public class AliLiveConfigView extends FrameLayout {

    /**
     * 混响模式
     */
    private String[] mReverbMode = {"无效果","人声 |","人声 ||",
            "澡堂","明亮小房间","黑暗小房间",
            "中等房间","大房间","教堂走廊","大教堂"};

    /**
     * 变声模式
     */
    private String[] mVoiceChangerMode = {"无效果","老人","男孩",
            "女孩","机器人","大魔王","KTV","回声"};
    /**
     * 耳返开关
     */
    private Switch mEarBackSwitch;
    /**
     * 耳返音量调节
     */
    private SeekBar mEarbackVolumeSeekBar;
//    private TextView mEarbackVolumeTextView;
    /**
     * 耳返音量调节
     */
    private SeekBar mPichVolumeSeekBar;
    private TextView mPichVolumeTextView;
    /**
     * 混响
     */
    private TextView mReverbModeTextView;

    private AliLiveConstants.AliLiveReverbMode mAliLiveReverbMode = AliLiveConstants.AliLiveReverbMode.AliLiveReverbMode_Off;
    private ColumnWheelDialog<WheelItem> mDialog;
    private WheelItem[] types;
    private AliLiveConfigListener mAliLiveConfigListener;
    private Button mLiveConfigSubmitButton;
    private VoiceChangerView mVoiceChangeView;

    private OnModelClick onModelClick;
    private TextView mVoiceModeTextview;

    public void setOnModelClick(OnModelClick onModelClick) {
        this.onModelClick = onModelClick;
    }

    public AliLiveConfigView(Context context) {
        super(context);
        init(context);
    }

    public AliLiveConfigView(Context context,AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AliLiveConfigView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_live_config,this,true);
        initView();
        initListener();
    }

    private void initView(){
        mEarBackSwitch = findViewById(R.id.switch_ear_back);
        mEarbackVolumeSeekBar = findViewById(R.id.seekbar_earback_volume);
//        mEarbackVolumeTextView = findViewById(R.id.tv_ear_back_volume);
        mPichVolumeSeekBar = findViewById(R.id.seekbar_pich_volume);
        mPichVolumeTextView = findViewById(R.id.tv_pich_volume);
        mReverbModeTextView = findViewById(R.id.tv_reverb_mode);
        mLiveConfigSubmitButton = findViewById(R.id.live_config_submit);
        mVoiceChangeView=findViewById(R.id.view_change);
        mEarbackVolumeSeekBar.setEnabled(mEarBackSwitch.isChecked());
        mVoiceModeTextview= (TextView) mVoiceChangeView.findViewById(R.id.tv_voice_changer_mode);
    }

    private void initListener(){
        //耳返开关
        mEarBackSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                mEarbackVolumeSeekBar.setEnabled(checked);
                if(mAliLiveConfigListener != null){
                    mAliLiveConfigListener.onEarbackChanged(checked);
                }
            }
        });

        //耳返音量调节
        mEarbackVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
//                    mEarbackVolumeTextView.setText(progress + "");
                    if(mAliLiveConfigListener != null){
                        mAliLiveConfigListener.onEarbackVolume(progress);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //音调高低
        mPichVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    if(progress <= 5){
                        seekBar.setProgress(5);
                    }
                    float pichValue = seekBar.getProgress() / 10.0f;
                    mPichVolumeTextView.setText(pichValue + "");
                    if(mAliLiveConfigListener != null){
                        mAliLiveConfigListener.onPicthValue(pichValue);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //混响
        mReverbModeTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                types = getTypes(mReverbMode);
//                createDialog(types,"选择混响模式",mReverbModeTextView,0);
                if(onModelClick!=null){
                    onModelClick.onClick(mReverbMode,"混响效果","混响");
                }
            }
        });

        //变声
        mVoiceModeTextview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                types = getTypes(mReverbMode);
//                createDialog(types,"选择混响模式",mReverbModeTextView,0);
                if(onModelClick!=null){
                    onModelClick.onClick(mVoiceChangerMode,"变声效果","变声");
                }
            }
        });

//button
        mLiveConfigSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAliLiveConfigListener != null){
                    mAliLiveConfigListener.onAliLiveReverbMode(mAliLiveReverbMode);
                }
                setVisibility(GONE);
            }
        });


    }

    public void setAliLiveConfigListener(AliLiveConfigListener listener){
        this.mAliLiveConfigListener = listener;
        mVoiceChangeView.setmAliLiveVoiceConfigListener(mAliLiveConfigListener);
    }

    /**
     * 显示滚轮显示页面
     * */
    private ColumnWheelDialog createDialog(WheelItem[] types, String title, final TextView textView, final int index){
        mDialog = new ColumnWheelDialog<>(getContext());
        mDialog.show();
        mDialog.setItems(types);
        mDialog.setTitle(title);
        mDialog.setCancelButton("取消", null);
        mDialog.setOKButton("确定", new ColumnWheelDialog.OnClickCallBack<WheelItem>() {

            @Override
            public boolean callBack(View v, @Nullable WheelItem item0) {
                textView.setText(item0.getShowText());
                switch (index){
                    case 0://混响
                        mAliLiveReverbMode = getAliLiveReverbMode(item0.getShowText());
                        if(mAliLiveConfigListener != null){
                            mAliLiveConfigListener.onAliLiveReverbMode(mAliLiveReverbMode);
                        }
                        break;
                    default:
                        break;

                }
                return false;
            }

        });
        return mDialog;
    }

    private WheelItem[] getTypes(String[] items){
        WheelItem[] wheelItems = new WheelItem[items.length];
        for(int i = 0; i < items.length; i++){
            WheelItem wheelItem = new WheelItem(items[i]);
            wheelItems[i] = wheelItem;
        }
        return wheelItems;
    }

    private AliLiveConstants.AliLiveReverbMode getAliLiveReverbMode(String showText) {
        switch (showText){
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

    interface OnModelClick{
        public void onClick(String[] mReverbMode,String title,String type);
    }


}
