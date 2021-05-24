package com.alilive.alilivesdk_demo.view;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.alilive.alilivesdk_demo.activity.AnchorActivity;
import com.alivc.live.AliLiveConfig;
import com.alivc.live.AliLiveConstants;
import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.wheel.base.WheelItem;
import com.alilive.alilivesdk_demo.wheel.dialog.ColumnWheelDialog;


/**
 * data:2020-08-18
 */
public class AliPushConfigView extends FrameLayout implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    /**
     * AliLiveCameraPositionFont
     *
     *
     */

    private TextView mTvCameraConfig;
    private Switch mSwitchBeauty;
    private Switch mSwitchAudioLink;
    private Switch mSwitchAutoFocus;
    /**
     * AliLiveVideoProfile_720P
     */
    private TextView mTvVideoResolution;
    /**
     * 15
     */
    private EditText mEtVideoFps;
    private Switch mSwitchVideoHardCoding;
    private Switch mSwitchVideoHardEncode;
    private Switch mSwitchAudioHardCoding;
    private Switch mSwitchVideoPreProcess;
    private Switch mSwitchAudioPreProcess;

    private Switch mSwitchHDPriview;
    /**
     * AliLiveVideoProfile_720P
     */
    private TextView mTvVideoGopSize;
    /**
     * 1000
     */
    private EditText mEtVideoInitialBitrate;
    /**
     * 1500
     */
    private EditText mEtVideoTargetBitrate;
    /**
     * 1500
     */
    private EditText mEtVideoMinBitrate;
    /**
     * AliLiveVideoProfile_720P
     */
    private TextView mTvAudioChannelCount;
    /**
     * AliLiveVideoProfile_720P
     */
    private TextView mTvAudioSampleRate;
    /**
     * AliLiveVideoProfile_720P
     */
    private TextView mTvAudioCoding;
    /**
     * 1500
     */
    private EditText mEtPushReconnectCount;
    /**
     * 1500
     */
    private EditText mEtPushReconnectTime;
    /**
     * 保存
     */
    private TextView mSaveButton;


    private AliLiveConfig mAliLiveConfig;

    private ColumnWheelDialog<WheelItem> mDialog;

    private WheelItem[] types;

    private String[] mCameraConfig = {"AliLiveCameraPositionFront","AliLiveCameraPositionBack"};
    private String[] mVideoResolution = {"180P","360P","480P","540P","720P","1080P"};
    private String[] mVideoGopSize = {"1s","2s","3s",
    "4s","5s"};
    private String[] mAudioChannelCount = {"单声道","双声道"};
    private String[] mAudioSampleRate = {"16000HZ","32000HZ","44100HZ","48000HZ"};
    private String[] mAudioCoding = {"AAC","HE_AAC","HE_AAC_V2"};

    private OnSaveConfigListener mOnSaveConfigListener;
    private SecondView mSecondView;
    private LinearLayout mParentView;

    public AliPushConfigView(@NonNull Context context) {
        super(context);
        initView();
    }

    public AliPushConfigView(@NonNull Context context,
                             @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public AliPushConfigView(@NonNull Context context,
                             @Nullable AttributeSet attrs,
                             int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    public void setmOnSaveConfigListener(OnSaveConfigListener mOnSaveConfigListener) {
        this.mOnSaveConfigListener = mOnSaveConfigListener;
    }

    public AliLiveConfig getmAliLiveConfig() {
        return mAliLiveConfig;
    }

    public void setmAliLiveConfig(AliLiveConfig mAliLiveConfig) {
        this.mAliLiveConfig = mAliLiveConfig;
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_push_config, this, true);
        mTvCameraConfig = (TextView)findViewById(R.id.tv_camera_config);
        mSwitchBeauty = (Switch)findViewById(R.id.switch_beauty);
        mSwitchAudioLink = (Switch)findViewById(R.id.switch_audio_link);
        mSwitchAutoFocus = (Switch)findViewById(R.id.switch_auto_focus);
        mSwitchVideoPreProcess = findViewById(R.id.switch_video_pre_process);
        mSwitchAudioPreProcess = findViewById(R.id.switch_audio_pre_process);
        mTvVideoResolution = (TextView)findViewById(R.id.tv_video_resolution);
        mEtVideoFps = (EditText)findViewById(R.id.et_video_fps);
        mSwitchHDPriview = (Switch) findViewById(R.id.switch_hdpreview);
        mSwitchVideoHardCoding = (Switch)findViewById(R.id.switch_video_hard_coding);
        mSwitchVideoHardEncode = findViewById(R.id.switch_video_hard_decode);
        mSwitchAudioHardCoding = (Switch)findViewById(R.id.switch_audio_hard_coding);
        mTvVideoGopSize = (TextView)findViewById(R.id.tv_video_gop_size);
        mEtVideoInitialBitrate = (EditText)findViewById(R.id.et_video_initial_bitrate);
        mEtVideoTargetBitrate = (EditText)findViewById(R.id.et_video_target_bitrate);
        mEtVideoMinBitrate = (EditText)findViewById(R.id.et_video_min_bitrate);
        mTvAudioChannelCount = (TextView)findViewById(R.id.tv_audio_channel_count);
        mTvAudioSampleRate = (TextView)findViewById(R.id.tv_audio_sample_rate);
        mTvAudioCoding = (TextView)findViewById(R.id.tv_audio_coding);
        mEtPushReconnectCount = (EditText)findViewById(R.id.et_push_reconnect_count);
        mEtPushReconnectTime = (EditText)findViewById(R.id.et_push_reconnect_time);
        mSaveButton = (TextView) findViewById(R.id.live_config_submit);
        mSecondView=(SecondView)findViewById(R.id.ll_seondView);
        mParentView=(LinearLayout)findViewById(R.id.ll_parent);

        mSwitchBeauty.setOnCheckedChangeListener(this);
        mSwitchAudioLink.setOnCheckedChangeListener(this);
        mSwitchAutoFocus.setOnCheckedChangeListener(this);
        mSwitchVideoHardCoding.setOnCheckedChangeListener(this);
        mSwitchVideoHardEncode.setOnCheckedChangeListener(this);
        mSwitchAudioHardCoding.setOnCheckedChangeListener(this);
        mSwitchHDPriview.setOnCheckedChangeListener(this);
        mSwitchVideoPreProcess.setOnCheckedChangeListener(this);
        mSwitchAudioPreProcess.setOnCheckedChangeListener(this);

        mTvCameraConfig.setOnClickListener(this);
        mTvVideoResolution.setOnClickListener(this);
        mTvVideoGopSize.setOnClickListener(this);
        mTvAudioChannelCount.setOnClickListener(this);
        mTvAudioSampleRate.setOnClickListener(this);
        mTvAudioCoding.setOnClickListener(this);
        mSaveButton.setOnClickListener(this);

        mSecondView.setOnItemClickListener(new SecondView.OnClickListener() {
            @Override
            public void onClick(String content, String type,int position) {
                if("推流分辨率".equalsIgnoreCase(type)){
                    mTvVideoResolution.setText(content);
                    mAliLiveConfig.videoPushProfile = getAliLiveVideoPushProfile(content);
                } else if("视频编码GOP".equalsIgnoreCase(type)){
                    mTvVideoGopSize.setText(content);
                    mAliLiveConfig.videoGopSize = getAliLivePushVideoEncodeGOP(content);
                } else if("音频采集声道数".equalsIgnoreCase(type)){
                    mTvAudioChannelCount.setText(content);
                    mAliLiveConfig.rtmpConfig.audioChannel = geAudioChannelCount(content);
                }else if("音频采样率".equalsIgnoreCase(type)){
                    mTvAudioSampleRate.setText(content);
                    mAliLiveConfig.rtmpConfig.audioSampleRate = geAliLivePushAudioSampleRate(content);
                }else if("音频编码格式".equalsIgnoreCase(type)){
                    mTvAudioCoding.setText(content);
                    mAliLiveConfig.rtmpConfig.audioEncoderProfile = geAliLiveAudioEncoderProfile(content);
                }
            }

            @Override
            public void onBack() {
                  mSecondView.setVisibility(GONE);
                  mParentView.setVisibility(VISIBLE);
            }
        });
    }


    /**
     * 显示滚轮显示页面
     * */
    private ColumnWheelDialog createDialog(WheelItem[] types, String title, final TextView textView,final int index){
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
                    case 0:
                        mAliLiveConfig.cameraPosition = getAliLiveCameraPosition(item0.getShowText());
                        break;
                    case 1:
                        mAliLiveConfig.videoPushProfile = getAliLiveVideoPushProfile(item0.getShowText());
                        break;
                    case 2:
                        mAliLiveConfig.videoGopSize = getAliLivePushVideoEncodeGOP(item0.getShowText());
                        break;
                    case 3:
                        mAliLiveConfig.rtmpConfig.audioChannel = geAudioChannelCount(item0.getShowText());
                        break;
                    case 4:
                        mAliLiveConfig.rtmpConfig.audioSampleRate = geAliLivePushAudioSampleRate(item0.getShowText());
                        break;
                    case 5:
                        mAliLiveConfig.rtmpConfig.audioEncoderProfile = geAliLiveAudioEncoderProfile(item0.getShowText());
                        break;
                    default:
                        break;

                }
                return false;
            }
        });
        return mDialog;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tv_camera_config:
                types = getTypes(mCameraConfig);
                createDialog(types,"选择前后摄像头",mTvCameraConfig,0);
                break;
            case R.id.tv_video_resolution:
                mParentView.setVisibility(GONE);
                mSecondView.setVisibility(VISIBLE);
                mSecondView.setData(mVideoResolution,"推流分辨率",-1);
                mSecondView.setmTitle("推流分辨率");
                break;
            case R.id.tv_video_gop_size:
                mParentView.setVisibility(GONE);
                mSecondView.setVisibility(VISIBLE);
                mSecondView.setData(mVideoGopSize,"视频编码GOP",-1);
                mSecondView.setmTitle("视频编码GOP");
                break;
            case R.id.tv_audio_channel_count:
                mParentView.setVisibility(GONE);
                mSecondView.setVisibility(VISIBLE);
                mSecondView.setData(mAudioChannelCount,"音频采集声道数",-1);
                mSecondView.setmTitle("音频采集声道数");
                break;
            case R.id.tv_audio_sample_rate:
                mParentView.setVisibility(GONE);
                mSecondView.setVisibility(VISIBLE);
                mSecondView.setData(mAudioSampleRate,"音频采样率",-1);
                mSecondView.setmTitle("音频采样率");
                break;
            case R.id.tv_audio_coding:
                mParentView.setVisibility(GONE);
                mSecondView.setVisibility(VISIBLE);
                mSecondView.setData(mAudioCoding,"音频编码格式",-1);
                mSecondView.setmTitle("音频编码格式");
                break;
            case R.id.live_config_submit:
                hideSoftKeyBoard((Activity) getContext());
                mAliLiveConfig.videoFPS = Integer.parseInt(TextUtils.isEmpty(mEtVideoFps.getText().toString()) ? "20" : mEtVideoFps.getText().toString());
                mAliLiveConfig.rtmpConfig.videoInitBitrate = Integer.parseInt(TextUtils.isEmpty(mEtVideoInitialBitrate.getText().toString()) ? "0" : mEtVideoInitialBitrate.getText().toString());
                mAliLiveConfig.rtmpConfig.videoTargetBitrate = Integer.parseInt(TextUtils.isEmpty(mEtVideoTargetBitrate.getText().toString()) ? "0" : mEtVideoTargetBitrate.getText().toString());
                mAliLiveConfig.rtmpConfig.videoMinBitrate = Integer.parseInt(TextUtils.isEmpty(mEtVideoMinBitrate.getText().toString()) ? "0" : mEtVideoMinBitrate.getText().toString());
                if(mAliLiveConfig.rtmpConfig.videoTargetBitrate<mAliLiveConfig.rtmpConfig.videoMinBitrate){
                    Toast.makeText(this.getContext(), "最小码率大于目标码率，请重新调整", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAliLiveConfig.rtmpConfig.autoReconnectRetryCount = Integer.parseInt(TextUtils.isEmpty(mEtPushReconnectCount.getText().toString()) ? "5" : mEtPushReconnectCount.getText().toString());
                mAliLiveConfig.rtmpConfig.autoReconnectRetryInterval = Integer.parseInt(TextUtils.isEmpty(mEtPushReconnectTime.getText().toString()) ? "1000" : mEtPushReconnectTime.getText().toString());
                this.setVisibility(GONE);
                if(mOnSaveConfigListener != null){
                    mOnSaveConfigListener.onSaveClick();
                }
                break;
            default:
                break;
        }


    }

    private AliLiveConstants.AliLiveCameraPosition getAliLiveCameraPosition(String string){
        switch (string){
            case "AliLiveCameraPositionFront":
                return AliLiveConstants.AliLiveCameraPosition.AliLiveCameraPositionFront;
            case "AliLiveCameraPositionBack":
                return AliLiveConstants.AliLiveCameraPosition.AliLiveCameraPositionBack;
            default:
                return AliLiveConstants.AliLiveCameraPosition.AliLiveCameraPositionFront;
        }
    }

    private AliLiveConstants.AliLiveVideoPushProfile getAliLiveVideoPushProfile(String string){
        switch (string){
            case "180P":
                mEtVideoTargetBitrate.setText("550");
                mEtVideoMinBitrate.setText("120");
                mEtVideoInitialBitrate.setText("300");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_180P;
            case "360P":
                mEtVideoTargetBitrate.setText("1000");
                mEtVideoMinBitrate.setText("300");
                mEtVideoInitialBitrate.setText("600");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_360P;
            case "480P":
                mEtVideoTargetBitrate.setText("1200");
                mEtVideoMinBitrate.setText("300");
                mEtVideoInitialBitrate.setText("800");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_480P;
            case "540P":
                mEtVideoTargetBitrate.setText("1500");
                mEtVideoMinBitrate.setText("600");
                mEtVideoInitialBitrate.setText("1000");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_540P;
            case "720P":
                mEtVideoTargetBitrate.setText("2000");
                mEtVideoMinBitrate.setText("600");
                mEtVideoInitialBitrate.setText("1500");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_720P;
            case "1080P":
                mEtVideoTargetBitrate.setText("3500");
                mEtVideoMinBitrate.setText("1200");
                mEtVideoInitialBitrate.setText("2000");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_1080P;
            case "AliLiveVideoPushProfile_Max":
                mEtVideoTargetBitrate.setText("3500");
                mEtVideoMinBitrate.setText("1200");
                mEtVideoInitialBitrate.setText("2000");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoPushProfile_Max;
            default:
                mEtVideoTargetBitrate.setText("1500");
                mEtVideoMinBitrate.setText("600");
                mEtVideoInitialBitrate.setText("1000");
                return AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_540P;
        }
    }


    private AliLiveConstants.AliLivePushVideoEncodeGOP getAliLivePushVideoEncodeGOP(String string){
        switch (string){
            case "1s":
                return AliLiveConstants.AliLivePushVideoEncodeGOP.AliLivePushVideoEncodeGOP_1;
            case "2s":
                return AliLiveConstants.AliLivePushVideoEncodeGOP.AliLivePushVideoEncodeGOP_2;
            case "3s":
                return AliLiveConstants.AliLivePushVideoEncodeGOP.AliLivePushVideoEncodeGOP_3;
            case "4s":
                return AliLiveConstants.AliLivePushVideoEncodeGOP.AliLivePushVideoEncodeGOP_4;
            case "5s":
                return AliLiveConstants.AliLivePushVideoEncodeGOP.AliLivePushVideoEncodeGOP_5;
            default:
                return AliLiveConstants.AliLivePushVideoEncodeGOP.AliLivePushVideoEncodeGOP_2;
        }
    }


    private AliLiveConstants.AliLivePushAudioChannel geAudioChannelCount(String string){
        switch (string){
            case "AliLivePushAudioChannel_1":
                return AliLiveConstants.AliLivePushAudioChannel.AliLivePushAudioChannel_1;
            case "AliLivePushAudioChannel_2":
                return AliLiveConstants.AliLivePushAudioChannel.AliLivePushAudioChannel_2;
            default:
                return AliLiveConstants.AliLivePushAudioChannel.AliLivePushAudioChannel_2;
        }
    }


    private AliLiveConstants.AliLivePushAudioSampleRate geAliLivePushAudioSampleRate(String string){
        switch (string){
            case "16000HZ":
                return AliLiveConstants.AliLivePushAudioSampleRate.AliLivePushAudioSampleRate16000;
            case "32000HZ":
                return AliLiveConstants.AliLivePushAudioSampleRate.AliLivePushAudioSampleRate32000;
            case "44100HZ":
                return AliLiveConstants.AliLivePushAudioSampleRate.AliLivePushAudioSampleRate44100;
            case "48000HZ":
                return AliLiveConstants.AliLivePushAudioSampleRate.AliLivePushAudioSampleRate48000;
            default:
                return AliLiveConstants.AliLivePushAudioSampleRate.AliLivePushAudioSampleRate16000;
        }
    }

    private AliLiveConstants.AliLiveAudioEncoderProfile geAliLiveAudioEncoderProfile(String string){
        switch (string){
            case "AAC":
                return AliLiveConstants.AliLiveAudioEncoderProfile.AliLiveAudioEncoderProfile_AAC_LC;
            case "HE_AAC":
                return AliLiveConstants.AliLiveAudioEncoderProfile.AliLiveAudioEncoderProfile_HE_AAC;
            case "HE_AAC_V2":
                return AliLiveConstants.AliLiveAudioEncoderProfile.AliLiveAudioEncoderProfile_HE_AAC_V2;
            default:
                return AliLiveConstants.AliLiveAudioEncoderProfile.AliLiveAudioEncoderProfile_AAC_LC;
        }
    }

    private WheelItem[] getTypes(String[] items){
        WheelItem[] wheelItems = new WheelItem[items.length];
        for(int i = 0; i < items.length; i++){
            WheelItem wheelItem = new WheelItem(items[i]);
            wheelItems[i] = wheelItem;
        }
        return wheelItems;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.switch_beauty:
                if(mOnSaveConfigListener != null){
                    mOnSaveConfigListener.onBeautyOpen(isChecked);
                }
                break;
            case R.id.switch_hdpreview:
                 mAliLiveConfig.enableHighDefPreview = isChecked;
                break;
            case R.id.switch_video_pre_process:
                if(isChecked){
                    mAliLiveConfig.customPreProcessMode |= AliLiveConfig.CUSTOM_MODE_VIDEO_PREPROCESS;
                }else{
                    mAliLiveConfig.customPreProcessMode ^= AliLiveConfig.CUSTOM_MODE_VIDEO_PREPROCESS;
                }
                break;
            case R.id.switch_audio_pre_process:
                if(isChecked){
                    mAliLiveConfig.customPreProcessMode |= AliLiveConfig.CUSTOM_MODE_AUDIO_PREPROCESS;
                }else{
                    mAliLiveConfig.customPreProcessMode ^= AliLiveConfig.CUSTOM_MODE_AUDIO_PREPROCESS;
                }
                break;
            case R.id.switch_audio_link:
                mAliLiveConfig.audioOnly = isChecked;
                break;
            case R.id.switch_auto_focus:
                mAliLiveConfig.autoFocus = isChecked;
                break;
            case R.id.switch_video_hard_coding:
                mAliLiveConfig.enableVideoEncoderHWAcceleration = isChecked;
                break;
            case R.id.switch_video_hard_decode:
                mAliLiveConfig.enableVideoDecoderHWAcceleration = isChecked;
                break;
            case R.id.switch_audio_hard_coding:
                mAliLiveConfig.rtmpConfig.enableAudioHWAcceleration = isChecked;
                break;

        }
    }

    public interface OnSaveConfigListener{
        void onSaveClick();
        void onBeautyOpen(boolean isOpen);
    }

    /**
     * 隐藏软键盘
     */
    private void hideSoftKeyBoard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
