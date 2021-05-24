package com.alilive.alilivesdk_demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.AliLiveConfigListener;
import com.alilive.alilivesdk_demo.listener.AliLiveVoiceChangeListener;
import com.alilive.alilivesdk_demo.listener.BGMClickListener;
import com.alilive.alilivesdk_demo.view.AdvancedSoundEffectView;
import com.alilive.alilivesdk_demo.view.AliLiveConfigView;
import com.alilive.alilivesdk_demo.view.AnchorButtonListView;
import com.alilive.alilivesdk_demo.view.BGMView;
import com.alilive.alilivesdk_demo.view.DataView;
import com.alilive.alilivesdk_demo.view.VoiceChangerView;
import com.alilive.alilivesdk_demo.wheel.widget.CommonDialog;
import com.alilive.alilivesdk_demo.wheel.widget.TextFormatUtil;
import com.alivc.live.AliLiveBeautyManager;
import com.alivc.live.AliLiveCallback;
import com.alivc.live.AliLiveConfig;
import com.alivc.live.AliLiveConstants;
import com.alivc.live.AliLiveEngine;
import com.alivc.live.AliLiveError;
import com.alivc.live.AliLiveRTMPConfig;
import com.alivc.live.AliLiveRenderView;
import com.alivc.live.bean.AliLiveLocalVideoStats;
import com.alivc.live.bean.AliLiveRemoteAudioStats;
import com.alivc.live.bean.AliLiveRemoteVideoStats;
import com.alivc.live.bean.AliLiveResult;
import com.alivc.live.bean.AliLiveStats;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.listener.BeautyClickAndSlideListener;
import com.alilive.alilivesdk_demo.listener.ButtonClickListener;
import com.alilive.alilivesdk_demo.utils.BeautyManager;
import com.alilive.alilivesdk_demo.utils.OrientationDetector;
import com.alilive.alilivesdk_demo.utils.PhoneStateManger;
import com.alilive.alilivesdk_demo.utils.ThreadUtils;
import com.alilive.alilivesdk_demo.utils.ToastUtils;
import com.alilive.alilivesdk_demo.utils.URLDialog;
import com.alilive.alilivesdk_demo.view.AliPushConfigView;
import com.alilive.alilivesdk_demo.view.BeautyView;
import com.alilive.alilivesdk_demo.view.FocusView;
import com.google.zxing.activity.CaptureActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;


public class PushActivity extends AppCompatActivity implements ButtonClickListener, BeautyClickAndSlideListener, AliPushConfigView.OnSaveConfigListener {
    private static final int BGM_CURRENT_POSITION_WHAT = 0x0001;

    private AnchorButtonListView mButtonListView;
    private BeautyView mBeautyView;
    private BGMView mBGMView;
    private AdvancedSoundEffectView mAdvanceSoundEffectView;
    private AliLiveConfigView mAliLiveConfigView;
    private FrameLayout mContainer;
    private TextView mShowMessage;
    //    直播类
    private AliLiveEngine mAliLiveEngine;
    //    直播的surfaceView
    private AliLiveRenderView mAliLiveRenderView;

    //    调节参数页面
    private AliPushConfigView mAliPushConfigView;

    //    是否静音
    private boolean isMute;

    private FocusView mFocusView;

    private AliLiveBeautyManager mAliLiveBeautyManager;

    private AliLiveConfig mAliLiveConfig;

    //    美颜开关是否开启
    private boolean isBeautyOpen = true;

    //    方向传感器
    private OrientationDetector mOrientationDetector;
    //    当前方向
    private int mCUrrentPosition = 0;
    /**
     * 当前是否横屏
     */
    private boolean isLandscape = false;

    private String mCurrentPushUrl;

    /**
     * 电话监听
     */
    private PhoneStateManger mPhoneStateManger;

    /**
     * 是否正在预览
     */
    private boolean isPreviewing = false;

    /**
     * 是否更新BGM当前播放进度
     */
    private boolean mEnableUpdateBGMCurrentPosition = true;
    /**
     * 背景音乐是否开启循环
     */
    boolean mIsBGMLoop = false;
    /**
     * BGM是否播放过
     */
    private boolean mBGMhasPlay = false;

    private BeautyManager mBeautyManager;
    private MyHandler mHandler;
    private VoiceChangerView mVoiceChanger;
    private Button mScanBtn;//扫码
    private EditText mPushUrlEt;//输入的推流url
    private AliLiveLocalVideoStats maliLiveLocalVideoStats;
    private DataView mDataView;
    private boolean isMuteFlag= false;//是否点击过静音播放


    private static class MyHandler extends Handler {

        private WeakReference<PushActivity> weakReference;

        public MyHandler(PushActivity pushActivity) {
            weakReference = new WeakReference<>(pushActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            PushActivity pushActivity = weakReference.get();
            if (pushActivity == null) {
                return;
            }
            switch (msg.what) {
                case BGM_CURRENT_POSITION_WHAT:
                    pushActivity.startGetBGMPosition();
                    break;
            }
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.live_push);
        initOrientationDetector();
        mHandler = new MyHandler(this);
        mBeautyManager = new BeautyManager();
        initViews();
        initLiveSDK();
        startPreview();//开启本地预览
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && mAliLiveEngine != null) {
            isLandscape = true;
//            当前屏幕为横屏
            if (mCUrrentPosition == 90) {
                mAliLiveEngine.setDeviceOrientationMode(AliLiveConstants.AliLiveOrientationMode.AliLiveOrientationModeLandscapeRight);
            } else {
                mAliLiveEngine.setDeviceOrientationMode(AliLiveConstants.AliLiveOrientationMode.AliLiveOrientationModeLandscapeLeft);
            }
        } else if (mAliLiveEngine != null) {
//            当前屏幕为竖屏
            isLandscape = false;
            mAliLiveEngine.setDeviceOrientationMode(AliLiveConstants.AliLiveOrientationMode.AliLiveOrientationModePortrait);
        }
        Log.e("onConfigurationChanged", "mCUrrentPosition:" + mCUrrentPosition + "\norientation:" + newConfig.orientation);
    }


    @Override
    protected void onStop() {
        super.onStop();
        //pausePush内部会关闭美颜
        if (mAliLiveEngine != null) {
            mAliLiveEngine.pausePush();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //resumePush会恢复之前的美颜。
        if (mAliLiveEngine != null) {
            mAliLiveEngine.resumePush();
        }
        //如果在onResume的时候跟onPause中间改了美颜的状态，那就需要再恢复一下目标状态。
        onBeautyOpen(isBeautyOpen);
    }

    private void initOrientationDetector() {
        mOrientationDetector = new OrientationDetector(PushActivity.this);
        mOrientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                int orientation = mOrientationDetector.getOrientation();

                if ((orientation >= 35) && (orientation < 135)) {
                    if (isLandscape && mCUrrentPosition != 90 && mAliLiveEngine != null) {
                        mAliLiveEngine.setDeviceOrientationMode(AliLiveConstants.AliLiveOrientationMode.AliLiveOrientationModeLandscapeRight);
                    }
                    mCUrrentPosition = 90;
                } else if ((orientation >= 200) && (orientation < 335)) {
                    if (isLandscape && mCUrrentPosition != 270 && mAliLiveEngine != null) {
                        mAliLiveEngine.setDeviceOrientationMode(AliLiveConstants.AliLiveOrientationMode.AliLiveOrientationModeLandscapeLeft);
                    }
                    mCUrrentPosition = 270;
                } else {
                    mCUrrentPosition = 0;
                }

            }
        });
    }

    private void initLiveSDK() {
        if (mAliLiveConfig == null) {
            AliLiveRTMPConfig rtmpConfig = new AliLiveRTMPConfig();
            rtmpConfig.videoInitBitrate = 1000;
            rtmpConfig.videoTargetBitrate = 1500;
            rtmpConfig.videoMinBitrate = 600;
            mAliLiveConfig = new AliLiveConfig(rtmpConfig);
            mAliLiveConfig.videoFPS = 20;
            mAliLiveConfig.videoPushProfile = AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_540P;
            mAliLiveConfig.enableHighDefPreview = false;
        }
        // TODO: 此处填写httpdns服务的accountId
        mAliLiveConfig.accountId = Constants.HTTP_DNS_ACCOUNT_ID;

        mAliLiveConfig.extra = Constants.LIVE_EXTRA_INFO;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_push, options);
        if (bitmap != null) {
            mAliLiveConfig.pauseImage = bitmap;
        }
        Log.d("PushActivity", "sdk version " + AliLiveEngine.getSdkVersion());
        mAliLiveEngine = AliLiveEngine.create(PushActivity.this, mAliLiveConfig);
        mAliLiveBeautyManager = mAliLiveEngine.getBeautyManager();
        mBeautyManager.setmAliLiveBeautyManager(mAliLiveBeautyManager);
        mAliPushConfigView.setmAliLiveConfig(mAliLiveConfig);
        mAliLiveEngine.setStatsCallback(statsCallback);
        mAliLiveEngine.setRtsCallback(rtsCallback);
        mAliLiveEngine.setStatusCallback(statusCallback);
        mAliLiveEngine.setNetworkCallback(networkCallback);

        mAliLiveEngine.setVidePreProcessDelegate(new AliLiveCallback.AliLiveVideoPreProcessCallback() {
            @Override
            public int onTexture(int textureId, int width, int height, int rotate, int i4) {
                Log.e("PushActivity", "onTexture: " + textureId + " --- " + width + " --- " + height + " --- " + i4);
                return textureId;
            }

            @Override
            public void onTextureDestroy() {
                Log.e("PushActivity", "onTexture: ");
            }

            @Override
            public void onVideoData(long l, long l1, long l2, AliLiveConstants.AliLiveImageFormat aliLiveImageFormat, int i, int i1, int i2, int i3, int i4, int i5) {
                Log.e("PushActivity", "onVideoData: " + l + " --- " + l1 + " --- " + l2 + " --- " + i + " --- " + i1 + " --- "
                        + i2 + " --- " + i3 + " --- " + i4 + " --- " + i5);
            }
        });
    }

    private AliLiveCallback.StatusCallback statusCallback = new AliLiveCallback.StatusCallback() {

        @Override
        public void onLiveSdkError(AliLiveEngine aliLiveEngine, final AliLiveError aliLiveError) {
            if (aliLiveError.errorCode == AliLiveError.AliLiveSdkErrorCodePushError) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(PushActivity.this, "推流失败：" + aliLiveError.errorDescription);
                    }
                });
            }
        }

        @Override
        public void onLiveSdkWarning(AliLiveEngine aliLiveEngine, int i) {

        }

        @Override
        public void onPreviewStarted(AliLiveEngine aliLiveEngine) {
            Log.d("statusCallback====", "onPreviewStarted");
            isPreviewing = true;
        }

        @Override
        public void onPreviewStopped(AliLiveEngine aliLiveEngine) {
            Log.d("statusCallback====", "onPreviewStopped");
            isPreviewing = false;
        }

        @Override
        public void onFirstVideoFramePreviewed(AliLiveEngine aliLiveEngine) {
        }

        @Override
        public void onLivePushStarted(AliLiveEngine aliLiveEngine) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(PushActivity.this, "推流成功");
                }
            });
        }

        @Override
        public void onLivePushStopped(AliLiveEngine aliLiveEngine) {

        }

        @Override
        public void onAudioFocusChanged(int focusChange) {
            if (focusChange < 0) {
                //loss,比如来电话了
                if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
                    mAliLiveEngine.setMute(true);
                }
            } else if (focusChange > 0) {
                //gain，比如电话挂断了
                if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
                    mAliLiveEngine.setMute(false);
                }
            } else {

            }
        }

        @Override
        public void onBGMStateChanged(AliLiveEngine publisher, final AliLiveConstants.AliLiveAudioPlayingStateCode playState,
                                      final AliLiveConstants.AliLiveAudioPlayingErrorCode errorCode) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int bgmCurrentPosition = mAliLiveEngine.getBGMCurrentPosition();
                    if (playState == AliLiveConstants.AliLiveAudioPlayingStateCode.AliLiveAudioPlayingStarted) {
                        mBGMView.setBGMDuration(mAliLiveEngine.getBGMDuration());
                        mEnableUpdateBGMCurrentPosition = true;
                        mBGMView.setBGMCurrentPosition(bgmCurrentPosition);
                        startGetBGMPosition();
                    } else if (playState == AliLiveConstants.AliLiveAudioPlayingStateCode.AliLiveAudioPlayingBuffering
                            || playState == AliLiveConstants.AliLiveAudioPlayingStateCode.AliLiveAudioPlayingPaused) {
                        mEnableUpdateBGMCurrentPosition = false;
                    } else if (playState == AliLiveConstants.AliLiveAudioPlayingStateCode.AliLiveAudioPlayingBufferingEnd
                            || playState == AliLiveConstants.AliLiveAudioPlayingStateCode.AliLiveAudioPlayingResumed) {
                        mEnableUpdateBGMCurrentPosition = true;
                        mBGMView.setBGMCurrentPosition(bgmCurrentPosition);
                    } else if (playState == AliLiveConstants.AliLiveAudioPlayingStateCode.AliLiveAudioPlayingEnded && mIsBGMLoop) {
                        mEnableUpdateBGMCurrentPosition = true;
                        mBGMView.setBGMCurrentPosition(bgmCurrentPosition);
                    } else {
                        mBGMView.setBGMCurrentPosition(bgmCurrentPosition);
                        mEnableUpdateBGMCurrentPosition = false;
                        stopGetBGMPosition();
                    }
                    if (errorCode != AliLiveConstants.AliLiveAudioPlayingErrorCode.AliLiveAudioPlayingNoError) {
                        ToastUtils.showToast(PushActivity.this, "onBGMStateChanged:" + playState + " errorcode " + errorCode);
                    }
                }
            });
        }
    };

    private AliLiveCallback.StatsCallback statsCallback = new AliLiveCallback.StatsCallback() {
        @Override
        public void onLiveTotalStats(AliLiveStats aliLiveStats) {
        }

        @Override
        public void onLiveLocalVideoStats(AliLiveLocalVideoStats aliLiveLocalVideoStats) {
            maliLiveLocalVideoStats=aliLiveLocalVideoStats;
        }

        @Override
        public void onLiveRemoteVideoStats(AliLiveRemoteVideoStats aliLiveRemoteVideoStats) {
        }

        @Override
        public void onLiveRemoteAudioStats(AliLiveRemoteAudioStats aliLiveRemoteAudioStats) {
        }
    };

    private AliLiveCallback.RtsCallback rtsCallback = new AliLiveCallback.RtsCallback() {
        @Override
        public void onSubscribeResult(AliLiveResult aliLiveResult, String s) {

        }

        @Override
        public void onUnSubscribeResult(AliLiveResult aliLiveResult, String s) {

        }

        @Override
        public void onFirstPacketReceivedWithUid(String s) {

        }

        @Override
        public void onFirstRemoteVideoFrameDrawn(String s, AliLiveConstants.AliLiveVideoTrack aliLiveVideoTrack) {

        }
    };

    private AliLiveCallback.NetworkCallback networkCallback = new AliLiveCallback.NetworkCallback() {
        @Override
        public void onNetworkStatusChange(final AliLiveConstants.AliLiveNetworkStatus status) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(PushActivity.this, "网络状态改变 = " + status.name());
                }
            });
        }

        @Override
        public void onNetworkPoor() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(PushActivity.this, "弱网");
                }
            });
        }

        @Override
        public void onConnectRecovery() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(PushActivity.this, "网络恢复");
                }
            });
        }

        @Override
        public void onReconnectStart() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(PushActivity.this, "开始重连");
                }
            });
        }

        @Override
        public void onConnectionLost() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(PushActivity.this, "网络断开");
                }
            });
        }

        @Override
        public void onReconnectStatus(final boolean success) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (success) {
                        ToastUtils.showToast(PushActivity.this, "重连成功");
                    } else {
                        ToastUtils.showToast(PushActivity.this, "重连失败");
                    }

                }
            });
        }
    };


    private void initViews() {
        mButtonListView = findViewById(R.id.live_buttonlistview);
        mContainer = findViewById(R.id.push_container);
        mBeautyView = findViewById(R.id.live_beauty_view);
        mAdvanceSoundEffectView = findViewById(R.id.live_advance_sound_effect);
        mBGMView = mAdvanceSoundEffectView.getBGMView();
        mAliLiveConfigView = mAdvanceSoundEffectView.getLiveConfigView();
        mVoiceChanger = findViewById(R.id.live_voice_changer);
        mAliPushConfigView = findViewById(R.id.live_configview);
        mShowMessage = findViewById(R.id.live_url_message);
        mAliPushConfigView.setmOnSaveConfigListener(this);
        mDataView=findViewById(R.id.ll_data);
        mBeautyView.setClickListener(this);
        List<String> data = new ArrayList<>();
        data.addAll(Constants.getPushActivityButtonList());
        mButtonListView.setData(data);
        mButtonListView.setClickListener(this);
        mScanBtn = findViewById(R.id.scan_image);//扫码
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCaptureActivityForResult();
            }
        });
        mPushUrlEt = findViewById(R.id.push_url);//push url

        //BGM
        mBGMView.setBGMClickListener(new BGMClickListener() {

            @Override
            public void onPlayClick(String path, boolean push, boolean loop) {
                mBGMhasPlay = true;
                mIsBGMLoop = loop;
                mAliLiveEngine.playBGM(path, push, loop);
            }

            @Override
            public void onResumeClick() {
                mAliLiveEngine.resumeBGM();
            }

            @Override
            public void onPauseClick() {
                mAliLiveEngine.pauseBGM();
            }

            @Override
            public void onStopClick() {
                mBGMhasPlay = false;
                mAliLiveEngine.stopBGM();
            }

            @Override
            public void onSeek(int position) {
                mAliLiveEngine.setBGMPosition(position);
            }

            @Override
            public void onVolume(int volume) {
                mAliLiveEngine.setBGMVolume(volume);
            }

            @Override
            public void onPushSwitchChanged(String path, boolean push, boolean loop) {
                if (mBGMhasPlay) {
                    mAliLiveEngine.playBGM(path, push, loop);
                }
            }

            @Override
            public void onLoopSwitchChanged(String path, boolean push, boolean loop) {
                if (mBGMhasPlay) {
                    mIsBGMLoop = loop;
                    mAliLiveEngine.playBGM(path, push, loop);
                }

            }
        });

        //变声模式
        mVoiceChanger.setAliLiveVoiceChangeListener(new AliLiveVoiceChangeListener() {
            @Override
            public void onAliLiveVoiceChangerMode(AliLiveConstants.AliLiveVoiceChangerMode voiceChangerMode) {
                mAliLiveEngine.setVoiceChangerMode(voiceChangerMode);
            }
        });

        mAdvanceSoundEffectView.setAliLiveConfigListener(new AliLiveConfigListener() {
            @Override
            public void onEarbackChanged(boolean enableEarback) {
                mAliLiveEngine.enableEarBack(enableEarback);
            }

            @Override
            public void onEarbackVolume(int volume) {
                mAliLiveEngine.setEarBackVolume(volume);
            }

            @Override
            public void onPicthValue(float value) {
                mAliLiveEngine.setPicthValue(value);
            }

            @Override
            public void onAliLiveReverbMode(AliLiveConstants.AliLiveReverbMode reverbMode) {
                mAliLiveEngine.setReverbMode(reverbMode);
            }

            @Override
            public void onAliLiveVoiceChangerMode(AliLiveConstants.AliLiveVoiceChangerMode voiceChangerMode) {
                mAliLiveEngine.setVoiceChangerMode(voiceChangerMode);
            }
        });

        //参数配置
        mAliLiveConfigView.setAliLiveConfigListener(new AliLiveConfigListener() {
            @Override
            public void onEarbackChanged(boolean enableEarback) {
                mAliLiveEngine.enableEarBack(enableEarback);
            }

            @Override
            public void onEarbackVolume(int volume) {
                mAliLiveEngine.setEarBackVolume(volume);
            }

            @Override
            public void onPicthValue(float value) {
                mAliLiveEngine.setPicthValue(value);
            }

            @Override
            public void onAliLiveReverbMode(AliLiveConstants.AliLiveReverbMode reverbMode) {
                mAliLiveEngine.setReverbMode(reverbMode);
            }

            @Override
            public void onAliLiveVoiceChangerMode(AliLiveConstants.AliLiveVoiceChangerMode voiceChangerMode) {
                mAliLiveEngine.setVoiceChangerMode(voiceChangerMode);
            }
        });
        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    /**
     * 创建输入URL的dialog
     */
    private void createDialog() {
        new URLDialog().createAndShowDialog("输入URL", PushActivity.this, new URLDialog.OnUrlInputListener() {
            @Override
            public void getUrl(String url) {
                startPublish(url);
            }
        });

    }

    /**
     * 初始化对焦点击事件
     */
    private void initFocus() {
        /**
         * 焦点focus
         */
        if (mAliLiveRenderView == null) {
            return;
        }
        if (mFocusView == null) {
            mFocusView = new FocusView(PushActivity.this);
            mFocusView.setPadding(10, 10, 10, 10);
            addSubView(mFocusView);
        }

        final GestureDetector gestureDetector = new GestureDetector(PushActivity.this,
                new GestureDetector.SimpleOnGestureListener() {
                    @Override
                    public boolean onSingleTapUp(MotionEvent e) {
                        if (mAliLiveEngine == null) {
                            return true;
                        }
                        AliLiveEngine.AliLivePoint aliLivePoint = new AliLiveEngine.AliLivePoint();
                        aliLivePoint.x = (int) e.getX();
                        aliLivePoint.y = (int) e.getY();
                        Log.d("mAliLiveEngine----", mAliLiveEngine.isCameraFocusPointSupported() + "----" + mAliLiveEngine.isCameraExposurePointSupported());
                        Log.d("mAliLiveEngine----", e.getX() + "----" + e.getY());
                        mAliLiveEngine.setCameraFocusPoint(aliLivePoint);
//                        mAliLiveEngine.setCameraExposurePoint(aliLivePoint);
                        mFocusView.showView();
                        mFocusView.setLocation(e.getRawX(), e.getRawY());
                        return true;
                    }
                });
        mAliLiveRenderView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getPointerCount() == 1) {
                    gestureDetector.onTouchEvent(event);
                }
                return true;
            }
        });
    }

    /**
     * 获取BGM当前播放位置
     */
    private void startGetBGMPosition() {
        if (mAliLiveEngine != null && mBGMView != null) {
            if (mEnableUpdateBGMCurrentPosition) {
                int bgmCurrentPosition = mAliLiveEngine.getBGMCurrentPosition();
                mBGMView.setBGMCurrentPosition(bgmCurrentPosition);
            }
        }
        if (mHandler != null) {
            mHandler.sendEmptyMessageDelayed(BGM_CURRENT_POSITION_WHAT, 500);
        }
    }

    private void stopGetBGMPosition() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private void addSubView(View view) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mContainer.addView(view, params);//添加到布局中
    }


    /**
     * 接听电话监听
     */
    private void initPhoneStateManger() {
        if (mPhoneStateManger == null) {
            mPhoneStateManger = new PhoneStateManger(this);
            mPhoneStateManger.registPhoneStateListener();
            mPhoneStateManger.setOnPhoneStateChangeListener(new PhoneStateManger.OnPhoneStateChangeListener() {

                @Override
                public void stateIdel() {
                    // 挂断
                    if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
                        mAliLiveEngine.setMute(false);
                    }
                }

                @Override
                public void stateOff() {
                    // 接听
                    if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
                        mAliLiveEngine.setMute(true);
                    }
                }

                @Override
                public void stateRinging() {
                    // 响铃
                    if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
                        mAliLiveEngine.setMute(true);
                    }
                }
            });
        }

    }

    /**
     * 开启预览
     */
    private void startPreview() {
        if (isPreviewing) {
            return;
        }
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
//        开启方向传感器的监听
        if (mOrientationDetector != null && mOrientationDetector.canDetectOrientation()) {
            mOrientationDetector.enable();
        }
        if (mAliLiveRenderView == null) {
            mAliLiveRenderView = mAliLiveEngine.createRenderView(false);
            addSubView(mAliLiveRenderView);
            initFocus();
            mAliLiveEngine.setPreviewMode(AliLiveConstants.AliLiveRenderMode.AliLiveRenderModeAuto, AliLiveConstants.AliLiveRenderMirrorMode.AliLiveRenderMirrorModeOnlyFront);
        }
        mAliLiveEngine.startPreview(mAliLiveRenderView);

        if (isBeautyOpen) {
            onBeautyOpen(true);
            mAliLiveBeautyManager.enable(AliLiveBeautyManager.EnableType.Basic);
            mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.SkinBuffing_SkinBuffing, 0.5f);
            mAliLiveBeautyManager.setBeautyParam(AliLiveBeautyManager.BeautyParam.SkinWhiting_SkinWhiting, 0.4f);
            if (mBeautyManager != null) {
                mBeautyManager.resumeParams();
            }
        }
    }

    private void startPublish(String url) {
        startPreview();
        mButtonListView.setButtonEnable("开始推流", false);
        mButtonListView.setButtonEnable("停止推流", true);
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mAliLiveEngine.isPublishing() && url.equals(mCurrentPushUrl)) {
            return;
        }
        mCurrentPushUrl = url;
        mShowMessage.setVisibility(View.VISIBLE);
        mShowMessage.setText("当前push url : " + mCurrentPushUrl);
        if (mAliLiveEngine.isPublishing()) {
            mAliLiveEngine.stopPush();
            mAliLiveEngine.startPush(mCurrentPushUrl);
        } else {
            mAliLiveEngine.startPush(mCurrentPushUrl);
        }
    }

    private void stopPreview() {
        mButtonListView.setButtonEnable("本地预览", true);
        mButtonListView.setButtonEnable("关闭预览", false);
        if (mAliLiveEngine != null) {
            mAliLiveEngine.stopPreview();
        }
        if (mOrientationDetector != null) {
            mOrientationDetector.disable();
        }

        if (mAliLiveRenderView != null) {
            mAliLiveRenderView.setVisibility(View.GONE);
            mAliLiveRenderView.setVisibility(View.VISIBLE);
        }
    }

    private void stopPublish() {
        mButtonListView.setButtonEnable("开始推流", true);
        mButtonListView.setButtonEnable("停止推流", false);
        if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
            mAliLiveEngine.stopPush();
        }
    }

    private void destroyEngine() {
        stopPreview();
        stopPublish();
        if (mAliLiveEngine != null) {
            mAliLiveEngine.destroy();
            mAliLiveEngine = null;
        }
    }

    /**
     * 开启扫码页面
     */
    private void startCaptureActivityForResult() {
        stopPreview();
        Intent intent = new Intent(PushActivity.this, CaptureActivity.class);
        startActivityForResult(intent, CaptureActivity.REQ_CODE);
    }

    private void showTipDialog(String tittle, String msg) {
        CommonDialog dialog = new CommonDialog(this);
        dialog.setDialogTitle(tittle);
        dialog.setDialogContent(msg);
        dialog.setConfirmButton(TextFormatUtil.getTextFormat(this, R.string.liveroom_btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    /**
     * 监听url的扫码结果
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CaptureActivity.REQ_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    startPublish(data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN));
                    break;
                case RESULT_CANCELED:
                    startPublish(mCurrentPushUrl);
                    break;
                default:
                    break;
            }
        }

    }


    /**
     * 监听返回键
     */
    @Override
    public void onBackPressed() {
        if (mAliPushConfigView.getVisibility() == View.VISIBLE) {
            mAliPushConfigView.setVisibility(View.GONE);
        } else if (mBeautyView.getVisibility() == View.VISIBLE) {
            mBeautyView.setVisibility(View.GONE);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyEngine();
        mAliLiveBeautyManager.destroy();
        if (mOrientationDetector != null) {
            mOrientationDetector.setOrientationChangedListener(null);
        }
        if (mPhoneStateManger != null) {
            mPhoneStateManger.setOnPhoneStateChangeListener(null);
            mPhoneStateManger.unRegistPhoneStateListener();
            mPhoneStateManger = null;
        }
        stopGetBGMPosition();
    }


    /**
     * 美颜美型的交互页面
     */
    @Override
    public void onButtonClick(String pageName, int pageIndex, String message, int position) {
        onBeautyOpen(isBeautyOpen);
        if (message.equals("关闭页面")) {
            mBeautyView.setVisibility(View.GONE);
        }
        if (!isBeautyOpen) {
            return;
        }
        if (mBeautyManager != null) {
            mBeautyManager.onButtonClick(pageName, pageIndex, message, position);
        }
    }

    @Override
    public void onProgressChanged(String pageName, int pageIndex, String message, float position) {
        onBeautyOpen(isBeautyOpen);
        if (!isBeautyOpen) {
            return;
        }
        if (mBeautyManager != null) {
            mBeautyManager.onProgressChanged(pageName, pageIndex, message, position);
        }
    }

    @Override
    public void onSwitchChanged(String pageName, int pageIndex, String message, boolean isCheck) {
        onBeautyOpen(isBeautyOpen);
        if (!isBeautyOpen) {
            return;
        }
        if (mBeautyManager != null) {
            mBeautyManager.onSwitchChanged(pageName, pageIndex, message, isCheck);
        }
    }


    @Override
    public void onPageSwitch(String pageName, int pageIndex, boolean isCheck) {
        onBeautyOpen(isBeautyOpen);
        if (!isBeautyOpen) {
            return;
        }
        if (mBeautyManager != null) {
            mBeautyManager.onPageSwitch(pageName, pageIndex, isCheck);
        }
    }


    //    设置页面的保存
    @Override
    public void onSaveClick() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PushActivity.this);
        builder.setTitle("设置参数后请重新开始推流")
                .setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //点击确定后重新开始推流，点击取消不停止画面
                destroyEngine();

                initLiveSDK();

                startPreview();
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startPublish(mCurrentPushUrl);
                    }
                }, 1000);
            }
        });
        builder.show();


    }


    //    设置页面的美颜开关是否开启
    @Override
    public void onBeautyOpen(boolean isOpen) {
        isBeautyOpen = isOpen;
        mAliLiveBeautyManager.enable(isOpen ? AliLiveBeautyManager.EnableType.Basic : AliLiveBeautyManager.EnableType.Off);
    }

    @Override
    public void onButtonClick(String message, int position) {
        switch (message) {
            case "开始推流":
                mCurrentPushUrl = mPushUrlEt.getText().toString();
                if (TextUtils.isEmpty(mCurrentPushUrl)) {
                    showTipDialog("提示", "推流地址不存在...");
                } else {
                    startPublish(mCurrentPushUrl);
                }
                break;
            case "停止推流":
                stopPublish();
                break;
            case "美颜":
                if (mBeautyView.getVisibility() == View.VISIBLE) {
                    mBeautyView.setVisibility(View.GONE);
                } else {
                    mBeautyView.setVisibility(View.VISIBLE);
//                    mButtonListView.hideItems(true);
                }
                break;
            case "调节参数":
                mAliPushConfigView.setVisibility(View.VISIBLE);
                break;
            case "扫码":
                startCaptureActivityForResult();
                break;
            case "静音":
                if(!isMuteFlag) {
                    if (mAliLiveEngine != null) {
                        mAliLiveEngine.setMute(true);
                    }
                    Toast toast = Toast.makeText(this, "已静音", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    isMuteFlag = true;
                }else{
                    if (mAliLiveEngine != null) {
                        mAliLiveEngine.setMute(false);
                    }
                    Toast toast = Toast.makeText(this, "取消静音", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    isMuteFlag = false;
                }
                break;
            case "摄像头":
                if (mAliLiveEngine != null) {
                    mAliLiveEngine.switchCamera();
                }
                break;
            case "音效":
                if (mAdvanceSoundEffectView.getVisibility() == View.VISIBLE) {
                    mAdvanceSoundEffectView.setVisibility(View.GONE);
                } else {
                    mAdvanceSoundEffectView.setVisibility(View.VISIBLE);
                }
                break;
            case "数据指标":
                if (mDataView.getVisibility() == View.VISIBLE) {
                    mDataView.setVisibility(View.GONE);
                    mButtonListView.setButtonEnable("数据指标", true);
                } else {
                    mDataView.setVisibility(View.VISIBLE);
                    mButtonListView.setButtonEnable("数据指标", false);
                    if (maliLiveLocalVideoStats != null) {
                        ((TextView) mDataView.findViewById(R.id.tv_data1)).setText("发送码率:" + (int)(maliLiveLocalVideoStats.sentBitrate/1024) + "kbps");
                        ((TextView) mDataView.findViewById(R.id.tv_data2)).setText("发送帧率:" + maliLiveLocalVideoStats.sentFps + "fps");
                        ((TextView) mDataView.findViewById(R.id.tv_data3)).setText("编码帧率:" + maliLiveLocalVideoStats.encodeFps);
                    } else {
                        mDataView.setVisibility(View.GONE);
                        Toast.makeText(this, "没有数据信息", Toast.LENGTH_SHORT).show();
                    }
                }
                break;

            default:
                break;
        }
    }
}
