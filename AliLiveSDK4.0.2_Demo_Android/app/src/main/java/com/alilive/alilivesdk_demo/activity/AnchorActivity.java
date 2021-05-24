package com.alilive.alilivesdk_demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.adapter.NoticeListAdapter;
import com.alilive.alilivesdk_demo.adapter.OnLineListAdapter;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.listener.AliLiveConfigListener;
import com.alilive.alilivesdk_demo.listener.AliLiveVoiceChangeListener;
import com.alilive.alilivesdk_demo.listener.BGMClickListener;
import com.alilive.alilivesdk_demo.listener.BeautyClickAndSlideListener;
import com.alilive.alilivesdk_demo.listener.ButtonClickListener;
import com.alilive.alilivesdk_demo.listener.OnItemClickListener;
import com.alilive.alilivesdk_demo.listener.OnVideoItemClickListener;
import com.alilive.alilivesdk_demo.socket.ApplyPkNoticeBean;
import com.alilive.alilivesdk_demo.socket.ApprovePkNoticeBean;
import com.alilive.alilivesdk_demo.socket.NotifyPublish;
import com.alilive.alilivesdk_demo.socket.OnLineRoomListBean;
import com.alilive.alilivesdk_demo.socket.Payload;
import com.alilive.alilivesdk_demo.socket.RoomInfo;
import com.alilive.alilivesdk_demo.socket.SocketConstants;
import com.alilive.alilivesdk_demo.socket.UserInfo;
import com.alilive.alilivesdk_demo.socket.VideoUserInfo;
import com.alilive.alilivesdk_demo.socket.WebSocketCallBack;
import com.alilive.alilivesdk_demo.socket.WebSocketHandler;
import com.alilive.alilivesdk_demo.utils.BeautyManager;
import com.alilive.alilivesdk_demo.utils.OrientationDetector;
import com.alilive.alilivesdk_demo.utils.ScreenUtil;
import com.alilive.alilivesdk_demo.utils.ThreadUtils;
import com.alilive.alilivesdk_demo.utils.ToastUtils;
import com.alilive.alilivesdk_demo.view.AdvancedSoundEffectView;
import com.alilive.alilivesdk_demo.view.AliLiveConfigView;
import com.alilive.alilivesdk_demo.view.AliPushConfigView;
import com.alilive.alilivesdk_demo.view.AnchorButtonListView;
import com.alilive.alilivesdk_demo.view.ApplyMicNoticeView;
import com.alilive.alilivesdk_demo.view.BGMView;
import com.alilive.alilivesdk_demo.view.BeautyView;
import com.alilive.alilivesdk_demo.view.DataView;
import com.alilive.alilivesdk_demo.view.FocusView;
import com.alilive.alilivesdk_demo.view.MicNoticeStatusView;
import com.alilive.alilivesdk_demo.view.OnLineRoomView;
import com.alilive.alilivesdk_demo.view.VideoListView;
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
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.taobao.android.libqueen.QueenEngine;
import com.taobao.android.libqueen.Texture2D;
import com.taobao.android.libqueen.exception.InitializationException;
import com.taobao.android.libqueen.models.BeautyFilterType;
import com.taobao.android.libqueen.models.BeautyParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static android.opengl.GLES20.GL_FRAMEBUFFER;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_ALLOW_MIC;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_ROOM_ID;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_ROOM_IP;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_USER_ID;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_USER_NAME;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPLY_MIC_NOTICA;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPLY_PK;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPLY_PK_NOTICE;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPROVE_MIC;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPROVE_PK;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPROVE_PK_NOTICE;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_CANCEL_PK;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_CANCEL_PK_NOTICE;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_GET_ROOM_LIST;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_JOIN_ROOM;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_LEAVE_ROOM;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_NOTIFY_PUBLISH;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_PUBLISH;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_REFRESH_USER_STREAM_URL;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_UNPUBLISH;
import static com.alivc.live.AliLiveConfig.CUSTOM_MODE_VIDEO_PREPROCESS;

/**
 * 主播端
 */
public class AnchorActivity extends AppCompatActivity implements ButtonClickListener,
        WebSocketCallBack, BeautyClickAndSlideListener,
        AliPushConfigView.OnSaveConfigListener {

    private static final int BGM_CURRENT_POSITION_WHAT = 0x0001;

    private static final String TAG = "AnchorActivity";
    private TextView mTvRoomTittle;
    private TextView mTvStatus;
    private AnchorButtonListView mButtonListView;
    private FrameLayout mContainer, mContainerPK1, mContainerPK2;
    private LinearLayout mContainerPk;
    private ApplyMicNoticeView mNoticeView;
    private OnLineRoomView mOnlineRoomView;
    private BeautyView mBeautyView;
    private FocusView mFocusView;
    private RoomInfo mRoomInfo;
    private String mRoomIp;
    private int mRoomId;
    private int mUserId;
    private String mUserName;
    private boolean isAllowMicLink;
    //    调节参数页面
    private AliPushConfigView mAliPushConfigView;

    private List<VideoUserInfo> mPullUrlList;
    //    美颜开关是否开启
    private boolean isBeautyOpen = true;

    /**
     * 是否正在预览
     */
    private boolean isPreviewing = false;
    /**
     * 当前推流地址
     */
    private String mCurrentPushUrl;

    private WebSocketHandler mSocketHandler;

    private AliLiveConfig mAliLiveConfig;
    //    直播类
    private AliLiveEngine mAliLiveEngine;
    private BeautyManager mBeautyManager;
    private AliLiveBeautyManager mAliLiveBeautyManager;
    //    方向传感器
    private OrientationDetector mOrientationDetector;
    //    当前方向
    private int mCUrrentPosition = 0;
    /**
     * 当前是否横屏
     */
    private boolean isLandscape = false;
    //    直播的surfaceView
    private AliLiveRenderView mAliLiveRenderView;
    private AliLiveRenderView mPkRenderView;
    private List<NoticeListAdapter.NoticeItemInfo> mApplyMicUserList;
    private VideoListView mVideoListView;
    private VoiceChangerView mVoiceChanger;
    /**
     * 是否加入im聊天室
     */
    private boolean hasJoinChatRoom = false;
    /**
     * 是否更新BGM当前播放进度
     */
    private boolean mEnableUpdateBGMCurrentPosition = true;
    private BGMView mBGMView;
    private AdvancedSoundEffectView mAdvanceSoundEffectView;
    private AliLiveConfigView mAliLiveConfigView;
    /**
     * 背景音乐是否开启循环
     */
    boolean mIsBGMLoop = false;
    private MyHandler mHandler;
    /**
     * BGM是否播放过
     */
    private boolean mBGMhasPlay = false;

    //连麦人数
    private RelativeLayout mNoticeNumberLl;
    private TextView mNoticeNumberTv;
    private CommonDialog dialog;
    private ImageView mIvpk;
    private Button mendPkBtn;//结束pk
    private DataView mDataView;
    private AliLiveLocalVideoStats maliLiveLocalVideoStats;
    private boolean isMuteFlag = false;//是否点击过静音播放
    private boolean isConnectMicIng = false;//正在连麦中
    private boolean isReconnect = false;

    private static class MyHandler extends Handler {

        private WeakReference<AnchorActivity> weakReference;

        public MyHandler(AnchorActivity anchorActivity) {
            weakReference = new WeakReference<>(anchorActivity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            AnchorActivity anchorActivity = weakReference.get();
            if (anchorActivity == null) {
                return;
            }
            switch (msg.what) {
                case BGM_CURRENT_POSITION_WHAT:
                    anchorActivity.startGetBGMPosition();
                    break;
            }
        }
    }

    private String mPkPullUrl;
    private boolean isPkApplying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new MyHandler(this);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_anchor);
        initOrientationDetector();
        initData();
        initView();
        initLiveSDK();
    }

    private void initOrientationDetector() {
        mOrientationDetector = new OrientationDetector(AnchorActivity.this);
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && mAliLiveEngine != null) {
            //            当前屏幕为横屏
            isLandscape = true;
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

    }

    private void initData() {
        //获取传入数据
        Intent intent = getIntent();
        mRoomIp = intent.getStringExtra(EXTRA_ROOM_IP);
        mRoomId = intent.getIntExtra(EXTRA_ROOM_ID, 0);
        mUserId = intent.getIntExtra(EXTRA_USER_ID, 0);
        mUserName = intent.getStringExtra(EXTRA_USER_NAME);
        isAllowMicLink = intent.getBooleanExtra(EXTRA_ALLOW_MIC, false);
        mSocketHandler = WebSocketHandler.getInstance();
        mSocketHandler.setSocketIOCallBack(this);
        Log.e(TAG, "mRoomIp" + mRoomIp);
        mSocketHandler.connect(mRoomIp);
        mPullUrlList = new ArrayList<>();
    }

    private void initView() {
        mVideoListView = findViewById(R.id.video_list_view);
        mVideoListView.setOnItemClickListener(new OnVideoItemClickListener() {
            @Override
            public void onItemClick() {

            }

            @Override
            public void onExitClick(final VideoUserInfo userInfo) {
                mSocketHandler.send(getSendMessage(CMD_UNPUBLISH, false, userInfo.getUserId()));
            }
        });
        mTvRoomTittle = (TextView) findViewById(R.id.tv_room_tittle);
        mTvRoomTittle.setText(String.valueOf(mRoomId));
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mButtonListView = (AnchorButtonListView) findViewById(R.id.live_buttonlistview);
        mContainer = findViewById(R.id.push_container);
        mContainerPk = findViewById(R.id.ll_container_pk);
        mContainerPK1 = findViewById(R.id.fl_container_pk1);
        mContainerPK2 = findViewById(R.id.fl_container_pk2);
        mIvpk = (ImageView) findViewById(R.id.iv_pk);
        int screenWidth = ScreenUtil.getScreenWidth(this);
        final int containerHeight = Math.round((float) screenWidth * 8 / 9);
        ViewGroup.LayoutParams layoutParams1 = mContainerPK1.getLayoutParams();
        layoutParams1.height = containerHeight;
        mContainerPK1.setLayoutParams(layoutParams1);
        ViewGroup.LayoutParams layoutParams2 = mContainerPK1.getLayoutParams();
        layoutParams2.height = containerHeight;
        mContainerPK2.setLayoutParams(layoutParams2);
        mNoticeView = findViewById(R.id.notice_view);
        mOnlineRoomView = findViewById(R.id.online_room_view);
        mBeautyView = findViewById(R.id.live_beauty_view);
        mAdvanceSoundEffectView = findViewById(R.id.live_advance_sound_effect);
        mBGMView = mAdvanceSoundEffectView.getBGMView();
        mAliLiveConfigView = mAdvanceSoundEffectView.getLiveConfigView();
        mVoiceChanger = findViewById(R.id.live_voice_changer);
        mBeautyView.setClickListener(this);
        List<String> data = new ArrayList<>();
        data.addAll(Constants.getAnchorActivityButtonList());
        mButtonListView.setData(data);
        mButtonListView.setClickListener(this);
        mButtonListView.setButtonEnable("PK主播", false);
        mDataView = findViewById(R.id.ll_data);

        mNoticeView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(NoticeListAdapter.NoticeItemInfo noticeItemInfo, boolean isAccept) {
                if (mPkPullUrl == null) {
                    mSocketHandler.send(getSendMessage(CMD_APPROVE_MIC, isAccept, noticeItemInfo.userInfo.getUserId()));
//                    mApplyMicUserList.remove(noticeItemInfo);
                    noticeItemInfo.micNoticeStatus = isAccept ? MicNoticeStatusView.STATUS_CONNECTING : MicNoticeStatusView.STATUS_REFUSED;
                    mNoticeView.notifyChanged();
                    if (mApplyMicUserList.size() == 0) {
                        mNoticeView.setVisibility(View.GONE);
                        mNoticeNumberLl.setVisibility(View.GONE);
                    }
                    if(isAccept){
                        isConnectMicIng = true;
                    }
                    //TODO sw 测试
                    mNoticeView.setVisibility(View.GONE);

                } else {
                    Toast.makeText(AnchorActivity.this, "正在PK中，暂不能连麦", Toast.LENGTH_SHORT).show();
                }

            }
        });
        mOnlineRoomView.setOnItemClickListener(new OnLineListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(OnLineRoomListBean.OnlineRoomBean room) {
                //  2020/9/27 申请pk
                mOnlineRoomView.setVisibility(View.GONE);
                mSocketHandler.send(getSendMessage(CMD_APPLY_PK, room.getUserId(), room.getRoomId()));
                mButtonListView.hideItems(false);
                isPkApplying = true;
            }
        });

        mAliPushConfigView = findViewById(R.id.live_configview);
        mAliPushConfigView.setmOnSaveConfigListener(this);

        mNoticeNumberLl = findViewById(R.id.ll_notice_number);
        mNoticeNumberTv = findViewById(R.id.tv_notice_num);
        mNoticeNumberLl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNoticeView != null && mApplyMicUserList.size() > 0) {
                    mNoticeView.setVisibility(View.VISIBLE);
                    mNoticeView.setUserList(mApplyMicUserList);
                    mButtonListView.hideItems(true);
                }
            }
        });

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

        findViewById(R.id.tv_transparent).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOnlineRoomView.getVisibility() == View.VISIBLE) {
                    mOnlineRoomView.setVisibility(View.GONE);
                    mButtonListView.hideItems(false);
                }

                if (mNoticeView.getVisibility() == View.VISIBLE) {
                    mNoticeView.setVisibility(View.GONE);
                    mButtonListView.hideItems(false);
                }
            }
        });

        mendPkBtn = (Button) findViewById(R.id.tv_endpk);
        mendPkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showStopPkDialog();
            }
        });

    }

    private QueenEngine engine;
    private Texture2D mOutTexture;

    private void initLiveSDK() {
        if (mAliLiveEngine == null) {
            if (mAliLiveConfig == null) {
                AliLiveRTMPConfig rtmpConfig = new AliLiveRTMPConfig();
                rtmpConfig.videoInitBitrate = 1000;
                rtmpConfig.videoTargetBitrate = 1500;
                rtmpConfig.videoMinBitrate = 600;
                mAliLiveConfig = new AliLiveConfig(rtmpConfig);
                mAliLiveConfig.videoPushProfile = AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_540P;
                mAliLiveConfig.videoFPS = 20;
                mAliLiveConfig.enableHighDefPreview = false;
                mAliPushConfigView.setmAliLiveConfig(mAliLiveConfig);
            }
            // TODO: 此处填写httpdns服务的accountId
            mAliLiveConfig.accountId = Constants.HTTP_DNS_ACCOUNT_ID;
            mAliLiveConfig.extra = Constants.LIVE_EXTRA_INFO;

            mAliLiveConfig.customPreProcessMode = CUSTOM_MODE_VIDEO_PREPROCESS;

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.background_push, options);
            if (bitmap != null) {
                mAliLiveConfig.pauseImage = bitmap;
            }
            mBeautyManager = new BeautyManager();
            mAliLiveEngine = AliLiveEngine.create(AnchorActivity.this, mAliLiveConfig);
            mAliLiveBeautyManager = mAliLiveEngine.getBeautyManager();
            mBeautyManager.setmAliLiveBeautyManager(mAliLiveBeautyManager);
            mAliLiveEngine.setStatsCallback(statsCallback);
            mAliLiveEngine.setRtsCallback(rtsCallback);
            mAliLiveEngine.setStatusCallback(statusCallback);
            mAliLiveEngine.setNetworkCallback(networkCallback);
            mAliLiveEngine.setVidePreProcessDelegate(new AliLiveCallback.AliLiveVideoPreProcessCallback() {
                @Override
                public int onTexture(int textureId, int width, int height, int rotate, int i4) {
                    // 绑定更新当前gl环境
                    int[] oldFboId = new int[1];
                    GLES20.glGetIntegerv(GLES20.GL_FRAMEBUFFER_BINDING, IntBuffer.wrap(oldFboId));

                    // 更新输入纹理id
                    engine.setInputTexture(textureId, width, height, false);

                    // 更新设置美颜美型相关参数
                    engine.enableBeautyType(BeautyFilterType.kSkinBuffing, true);//磨皮开关
                    engine.setBeautyParam(BeautyParams.kBPSkinBuffing, 0.9f);  //磨皮 [0,1]
                    engine.setBeautyParam(BeautyParams.kBPSkinSharpen, 0.6f);  //锐化 [0,1]

                    // 指定处理后的画面更新到新的纹理id上
                    if (mOutTexture == null) {
                        mOutTexture = engine.autoGenOutTexture();
                    }
                    engine.updateOutTexture(mOutTexture.getTextureId(), width, height);

                    // 开始渲染画面
                    int result = engine.render();
                    // 绑定更新到当前gl环境
                    GLES20.glBindFramebuffer(GL_FRAMEBUFFER, oldFboId[0]);
                    // 返回更新后的纹理id
                    return mOutTexture.getTextureId();
                }

                @Override
                public void onTextureDestroy() {
                    Log.e("AnchorActivity", "onTexture: ");
                }

                @Override
                public void onVideoData(long l, long l1, long l2, AliLiveConstants.AliLiveImageFormat aliLiveImageFormat, int i, int i1, int i2, int i3, int i4, int i5) {
//                    // 更新视频帧数据
//                    // 原API对应参数定义为:
//                    // long dataFrameY, long dataFrameU, long dataFrameV, AliLiveConstants.AliLiveImageFormat format, int width, int height, int strideY, int strideU, int strideV, int rotate
//                    // 此处需要做数据格式转化, 输入帧图片流
//                    engine.updateInputDataAndRunAlg(
//                            imageData, // 帧图片流
//                            ImageFormat.NV21, // 帧图片流格式
//                            imageWidth, // 帧图片宽度
//                            imageHeight, // 帧图片高度
//                            0, // 用于检测的图像的跨度(以像素为单位),即每行的字节数, 默认情况下设为 0
//                            mCamera.inputAngle, // 当前输入帧图片需旋转的角度，计算方式参考Sample工程
//                            mCamera.outAngle, // 算法输出结果所需旋转的角度，计算方式参考Sample工程
//                            mCamera.flipAxis // 输出数据的xy轴翻转处理,0为不旋转,1为x轴翻转,2为y轴翻转
//                    );
                }
            });

            try {
                engine = new QueenEngine(this,false);
            } catch (
                    InitializationException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onButtonClick(String message, int position) {
        switch (message) {
            case "开始推流":
                if (mRoomInfo == null) {
                    return;
                }
                mButtonListView.setButtonEnable("开始推流", false);
                mButtonListView.setButtonEnable("调节参数", false);
                mTvStatus.setText("尝试推流..");
                if (mRoomInfo.isAllowMic()) {
                    startPublish(mRoomInfo.getRtcPushUrl());
                } else {
                    startPublish(mRoomInfo.getRtmpPushUrl());
                }
                break;
            case "摄像头":
                if (mAliLiveEngine != null) {
                    mAliLiveEngine.switchCamera();
                }
                break;
            case "静音":
                if (!isMuteFlag) {
                    if (mAliLiveEngine != null) {
                        mAliLiveEngine.setMute(true);
                    }
                    Toast toast = Toast.makeText(this, "已静音", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    isMuteFlag = true;
                } else {
                    if (mAliLiveEngine != null) {
                        mAliLiveEngine.setMute(false);
                    }
                    Toast toast = Toast.makeText(this, "取消静音", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    isMuteFlag = false;
                }
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
                if (!mAliLiveEngine.isPublishing()) {
                    mAliPushConfigView.setVisibility(View.VISIBLE);
                }
                break;
            case "PK":
                mSocketHandler.send(getSendMessage(CMD_GET_ROOM_LIST, true, 0));
                break;
            case "停止PK":
                showStopPkDialog();
                break;
            case "离开房间":
                finish();
                break;
            case "变声模式":
                if (mVoiceChanger.getVisibility() == View.VISIBLE) {
                    mVoiceChanger.setVisibility(View.GONE);
                } else {
                    mVoiceChanger.setVisibility(View.VISIBLE);
                    mButtonListView.hideItems(true);

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
                if (maliLiveLocalVideoStats == null) {
                    Toast.makeText(AnchorActivity.this, "没有数据信息", Toast.LENGTH_SHORT).show();
                    return;
                }
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
                        Toast.makeText(AnchorActivity.this, "没有数据信息", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
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
            mHandler.sendEmptyMessageDelayed(BGM_CURRENT_POSITION_WHAT, 1000);
        }
    }

    private void stopGetBGMPosition() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onOpen() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                joinRoom();
            }
        });

    }

    @Override
    public void onMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "msg =" + msg);

                    JSONObject object = new JSONObject(msg);
                    String cmd = object.getString("cmd");
                    Gson gson = new Gson();
                    JSONObject payloadObject = object.getJSONObject("payload");
                    int code = payloadObject.getInt("code");
                    String message = payloadObject.getString("codeMsg");
                    switch (cmd) {
                        case CMD_JOIN_ROOM:
                            Type joinRoomType = new TypeToken<Payload<RoomInfo>>() {
                            }.getType();
                            Payload<RoomInfo> joinRoomPayload = gson.fromJson(payloadObject.toString(), joinRoomType);
                            mRoomInfo = joinRoomPayload.getData();
                            if (joinRoomPayload.getCode() == 0 && mRoomInfo != null) {
                                startPreview();
                                mTvStatus.setText("加入房间成功");
                                joinChatRoom();
                                mSocketHandler.startHeartbeat(mUserId, mRoomInfo.getToken());
                                if(isReconnect) {
                                    if (mRoomInfo.isAllowMic()) {
                                        startPublish(mRoomInfo.getRtcPushUrl());
                                    } else {
                                        startPublish(mRoomInfo.getRtmpPushUrl());
                                    }
                                }
                            } else {
                                mTvStatus.setText("加入房间失败");
                                showTipDialog("进入房间失败", joinRoomPayload.getCodeMsg());
                            }
                            break;
                        case CMD_APPLY_MIC_NOTICA:
                            Type type = new TypeToken<Payload<UserInfo>>() {
                            }.getType();
                            Payload<UserInfo> payload = gson.fromJson(payloadObject.toString(), type);
                            if (payload.getCode() == 0) {
                                UserInfo userInfo = payload.getData();
                                if (userInfo != null) {
                                    if (mApplyMicUserList == null) {
                                        mApplyMicUserList = new ArrayList<>();
                                    }
                                    if (mApplyMicUserList != null && mApplyMicUserList.size() > 0) {
                                        if(isConnectMicIng) {
                                            mSocketHandler.send(getSendMessage(CMD_APPROVE_MIC, false, userInfo.getUserId()));//目前只支持1v1连麦，后期支持1vN
                                        }
                                        else{
                                            setNoticeListAdapter(userInfo);
                                        }
                                    } else {
                                        setNoticeListAdapter(userInfo);
                                    }
                                }
                            } else {
                                Toast.makeText(AnchorActivity.this, "连麦申请", Toast.LENGTH_SHORT).show();
                            }


                            break;
                        case CMD_NOTIFY_PUBLISH:

                            Type noticeType = new TypeToken<Payload<NotifyPublish>>() {
                            }.getType();
                            Payload<NotifyPublish> approvePayload = gson.fromJson(payloadObject.toString(), noticeType);
                            Log.e(TAG, "CMD_NOTIFY_PUBLISH payload=" + payloadObject.toString());
                            NotifyPublish notice = approvePayload.getData();
                            List<VideoUserInfo> urlList = notice.getRtcPullUrls();
                            if(urlList!=null){
                                if(urlList.size()==0){
                                    isConnectMicIng = false;
                                }
                            }
                            stopPull(urlList);
                            if (urlList != null && urlList.size() > 0) {
                                int size = urlList.size();
                                for (int i = 0; i < size; i++) {
                                    VideoUserInfo url = urlList.get(i);
                                    if (!mPullUrlList.contains(url)) {
                                        url.fromAnchorPage = true;
                                        mPullUrlList.add(url);
                                        Log.e("WebSocketHandler", "add url:" + url);
                                    }


                                    //连麦成功，状态设置
                                    if (mApplyMicUserList != null) {
                                        for (NoticeListAdapter.NoticeItemInfo noticeItemInfo : mApplyMicUserList) {
                                            if (noticeItemInfo.micNoticeStatus != MicNoticeStatusView.STATUS_CONNECTING) {
                                                continue;
                                            }
                                            if (noticeItemInfo.userInfo.getUserId() == url.getUserId()) {
                                                noticeItemInfo.micNoticeStatus = MicNoticeStatusView.STATUS_CONNECTED;
                                            }
                                        }
                                        mNoticeView.notifyChanged();
                                        mNoticeView.setVisibility(View.GONE);
                                        mNoticeNumberLl.setVisibility(View.GONE);
                                    }
                                }
                            }
                            mButtonListView.hideItems(false);
                            startPull();
                            break;
                        case CMD_APPROVE_MIC:
                            if ((code == 1006)) {
                                Toast.makeText(AnchorActivity.this, "观众已离开房间", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case CMD_GET_ROOM_LIST:
                            Type onLineRoomListType = new TypeToken<Payload<OnLineRoomListBean>>() {
                            }.getType();
                            Payload<OnLineRoomListBean> onLineRoomListPayload = gson.fromJson(payloadObject.toString(), onLineRoomListType);
                            if (code == 0) {
                                OnLineRoomListBean onLineRoomListBean = onLineRoomListPayload.getData();
                                List<OnLineRoomListBean.OnlineRoomBean> onlineRoomList = onLineRoomListBean.getOnlineRoomList();
                                if (onlineRoomList != null && onlineRoomList.size() > 0) {
                                    mOnlineRoomView.setRoomList(onlineRoomList);
                                    mOnlineRoomView.setVisibility(View.VISIBLE);
                                    //隐藏按钮
                                    mButtonListView.hideItems(true);
                                } else {
                                    Toast.makeText(AnchorActivity.this, "没有其他主播在线", Toast.LENGTH_SHORT).show();
                                }

                            } else {
                                Toast.makeText(AnchorActivity.this,
                                        "提示：" + message, Toast.LENGTH_SHORT)
                                        .show();
                            }
                            break;
                        case CMD_APPLY_PK:
                            if ((code != 0)) {
                                Toast.makeText(AnchorActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case CMD_APPLY_PK_NOTICE:
                            Type applyPkNoticeType = new TypeToken<Payload<ApplyPkNoticeBean>>() {
                            }.getType();
                            Payload<ApplyPkNoticeBean> applyPkNoticePayload = gson.fromJson(payloadObject.toString(), applyPkNoticeType);
                            ApplyPkNoticeBean applyPkNoticeBean = applyPkNoticePayload.getData();
                            if (isPkApplying) {
                                mSocketHandler.send(getSendMessage(CMD_APPROVE_PK, false, applyPkNoticeBean.getFromUserId(), applyPkNoticeBean.getFromRoomId()));
                            } else {
                                showApplyPkDialog(applyPkNoticeBean);
                            }

                            break;
                        case CMD_APPROVE_PK_NOTICE:
                            Type approvePkNoticeType = new TypeToken<Payload<ApprovePkNoticeBean>>() {
                            }.getType();
                            Payload<ApprovePkNoticeBean> approvePkNoticePayload = gson.fromJson(payloadObject.toString(), approvePkNoticeType);
                            ApprovePkNoticeBean approvePkNoticeBean = approvePkNoticePayload.getData();
                            isPkApplying = false;
                            if (approvePkNoticeBean.isApprove()) {
                                // 开始连麦
                                startPk(approvePkNoticeBean.getRtcPullUrl());
                            } else {
                                Toast toast = Toast.makeText(AnchorActivity.this, "对方拒绝了本次PK申请", Toast.LENGTH_SHORT);
                                toast.setGravity(Gravity.CENTER, 0, 0);
                                toast.show();
                            }
                            break;
                        case CMD_CANCEL_PK_NOTICE:
                            stopPk(false);
                            break;
                        case CMD_APPROVE_PK:
                            if (code != 0) {
                                Toast.makeText(AnchorActivity.this, "建立Pk失败", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case CMD_REFRESH_USER_STREAM_URL:
                            if (code == 0) {//刷新url信令
                                Type joinRoomRefreshType = new TypeToken<Payload<RoomInfo>>() {
                                }.getType();
                                Payload<RoomInfo> joinRoomRefreshPayload = gson.fromJson(payloadObject.toString(), joinRoomRefreshType);
                                mRoomInfo = joinRoomRefreshPayload.getData();
                                if(!TextUtils.isEmpty(mRoomInfo.getRtcPushUrl())) {
                                        startPublish(mRoomInfo.getRtcPushUrl());
                                }else{
                                   if(!TextUtils.isEmpty(mRoomInfo.getRtmpPullUrl())){
                                       startPublish(mRoomInfo.getRtmpPushUrl());
                                   }
                                }
                                Toast.makeText(AnchorActivity.this, "RefreshUserStreamUrl", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        default:
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    @Override
    public void onClose() {

    }

    @Override
    public void onConnectError(Throwable t) {


    }

    public void joinRoom() {
        mTvStatus.setText("开始加入房间...");
        mSocketHandler.send(getSendMessage("JoinRoom", isAllowMicLink, 0));
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
            //mAliLiveBeautyManager.enableBeautyType(AliLiveBeautyManager.BeautyType.SkinBuffing, true);
            //mAliLiveBeautyManager.enableBeautyType(AliLiveBeautyManager.BeautyType.SkinWhiting, true);
            //mAliLiveBeautyManager.enableBeautyType(AliLiveBeautyManager.BeautyType.FaceBuffing, true); //disable race beauty
            if (mBeautyManager != null) {
                mBeautyManager.resumeParams();
            }
        }
    }

    /**
     * 初始化对焦点击事件
     **/
    private void initFocus() {
        /**
         * 焦点focus
         */
        if (mAliLiveRenderView == null) {
            return;
        }
        if (mFocusView == null) {
            mFocusView = new FocusView(AnchorActivity.this);
            mFocusView.setPadding(10, 10, 10, 10);
            addSubView(mFocusView);
        }

        final GestureDetector gestureDetector = new GestureDetector(AnchorActivity.this,
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
     * 开始推流
     *
     * @param url 推流地址
     */
    private void startPublish(String url) {
        Log.e(SocketConstants.TAG, "startPublish");
        startPreview();
        if (TextUtils.isEmpty(url)) {
            return;
        }
        if (mAliLiveEngine.isPublishing() && url.equals(mCurrentPushUrl)) {
            return;
        }
        mCurrentPushUrl = url;
        if (mAliLiveEngine.isPublishing()) {
            mAliLiveEngine.stopPush();
            mAliLiveEngine.startPush(mCurrentPushUrl);
        } else {
            mAliLiveEngine.startPush(mCurrentPushUrl);
        }
    }

    /**
     * 结束预览
     */
    private void stopPreview() {
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
        mAliLiveEngine.stopPreview();
        if (mOrientationDetector != null) {
            mOrientationDetector.disable();
        }
    }

    @Override
    public void onSaveClick() {
        //点击确定后重新开始推流，点击取消不停止画面
        boolean isPreview = isPreviewing;
        boolean isPublish = false;
        if (isPreview) {
            stopPreview();
        }
        if (mAliLiveEngine != null) {
            isPublish = mAliLiveEngine.isPublishing();
            if (isPublish) {
                mAliLiveEngine.stopPush();
            }
            mAliLiveEngine.destroy();
            mAliLiveEngine = null;
        }
        initLiveSDK();
        if (isPreview) {
            startPreview();
        }
        if (isPublish) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startPublish(mCurrentPushUrl);
                }
            }, 1000);
        }
    }

    @Override
    public void onBeautyOpen(boolean isOpen) {
        isBeautyOpen = isOpen;
        mAliLiveBeautyManager.enable(isOpen ? AliLiveBeautyManager.EnableType.Basic : AliLiveBeautyManager.EnableType.Off);
    }

    /**
     * 开始拉流
     */
    private void startPull() {
        Log.e(SocketConstants.TAG, "startPull");
        if (mPullUrlList != null && mPullUrlList.size() > 0) {
            for (VideoUserInfo urlBean : mPullUrlList) {
               if (!urlBean.isPulling()||isReconnect) {
                    mAliLiveEngine.subscribeStream(urlBean.getRtcPullUrl());
                    urlBean.setPulling(true);
               }
            }
        }
    }

    /**
     * 停止拉流
     */
    private void stopPull(List<VideoUserInfo> urlList) {
        if (mPullUrlList == null || mPullUrlList.size() == 0) {
            return;
        }
        Iterator<VideoUserInfo> iterator = mPullUrlList.iterator();
        while (iterator.hasNext()) {
            VideoUserInfo remoteUserInfo = iterator.next();
            if (urlList == null || !urlList.contains(remoteUserInfo)) {
                mAliLiveEngine.unSubscribeStream(remoteUserInfo.getRtcPullUrl());
                iterator.remove();
                mVideoListView.setUserList(mPullUrlList);
            }
        }
        mPullUrlList.size();
    }

    private void startPk(String pullUrl) {
        // 开始pk
        mPkPullUrl = pullUrl;
        mAliLiveEngine.subscribeStream(mPkPullUrl);
        mButtonListView.changeButtonName("PK主播", "停止PK");
        mButtonListView.setButtonEnable("PK", false);
        mendPkBtn.setVisibility(View.VISIBLE);
    }

    private void showPkSurfaceView(boolean show) {
        if (show) {
            if (mPkRenderView == null) {
                mPkRenderView = mAliLiveEngine.createRenderView(false);
            }
            mContainerPk.setVisibility(View.VISIBLE);
            mIvpk.setVisibility(View.VISIBLE);
            addSubView(mContainerPK1, mAliLiveRenderView);
            addSubView(mContainerPK2, mPkRenderView);

        } else {
            mContainerPk.setVisibility(View.GONE);
            mIvpk.setVisibility(View.GONE);
            mContainerPK1.removeAllViews();
            mContainerPK2.removeAllViews();
            addSubView(mContainer, mAliLiveRenderView);
        }
    }

    private void stopPk(boolean sendMessage) {
        isPkApplying = false;
        if (sendMessage) {
            mSocketHandler.send(getSendMessage(CMD_CANCEL_PK));
        }
        mAliLiveEngine.unSubscribeStream(mPkPullUrl);
        mButtonListView.setButtonEnable("PK", true);
        mendPkBtn.setVisibility(View.GONE);
        showPkSurfaceView(false);
        Toast toast = Toast.makeText(AnchorActivity.this, "PK已结束", Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
        mPkPullUrl = null;
    }

    private void showTipDialog(String tittle, String msg) {
        CommonDialog dialog = new CommonDialog(AnchorActivity.this);
        dialog.setDialogTitle(tittle);
        dialog.setDialogContent(msg);
        dialog.setConfirmButton(TextFormatUtil.getTextFormat(this, R.string.liveroom_btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        dialog.show();
    }

    private ApplyPkNoticeBean mLastApplyPkNotice;

    private void showApplyPkDialog(final ApplyPkNoticeBean applyPkNoticeBeanm) {
        if (mLastApplyPkNotice != null) {
            mSocketHandler.send(getSendMessage(CMD_APPROVE_PK, false, mLastApplyPkNotice.getFromUserId(), mLastApplyPkNotice.getFromRoomId()));
        }
        if (dialog != null && dialog.isShowing()) {
            mLastApplyPkNotice = null;
            dialog.dismiss();
        }
        mLastApplyPkNotice = applyPkNoticeBeanm;
        dialog = new CommonDialog(this);
        dialog.setDialogTitle("PK申请");
        dialog.setDialogContent("主播" + applyPkNoticeBeanm.getFromUserName() + "申请与您连麦");
        dialog.setConfirmButton(TextFormatUtil.getTextFormat(this, R.string.liveroom_ask_accept), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startPk(mLastApplyPkNotice.getFromRtcPullUrl());
                mSocketHandler.send(getSendMessage(CMD_APPROVE_PK, true, mLastApplyPkNotice.getFromUserId(), mLastApplyPkNotice.getFromRoomId()));
                mLastApplyPkNotice = null;
                dialog.dismiss();

            }
        });
        dialog.setCancelButton(TextFormatUtil.getTextFormat(this, R.string.liveroom_ask_refuse), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSocketHandler.send(getSendMessage(CMD_APPROVE_PK, false, mLastApplyPkNotice.getFromUserId(), mLastApplyPkNotice.getFromRoomId()));
                mLastApplyPkNotice = null;
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void showStopPkDialog() {
        CommonDialog dialog = new CommonDialog(this);
        dialog.setDialogTitle("提示");
        dialog.setDialogContent("您确定要关闭互动PK吗？");
        dialog.setConfirmButton(TextFormatUtil.getTextFormat(this, R.string.liveroom_btn_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //停止PK
                stopPk(true);
                dialog.dismiss();
            }
        });
        dialog.setCancelButton(TextFormatUtil.getTextFormat(this, R.string.liveroom_btn_cancle), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
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

    private void addSubView(FrameLayout container, View subViewView) {
        ViewParent parent = subViewView.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(subViewView);
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        container.addView(subViewView, params);//添加到布局中
    }

    private String getSendMessage(String cmd) {
        return getSendMessage(cmd, true, 0, 0);
    }

    /**
     * 获取socket发送需要的字符串
     *
     * @param cmd    信令
     * @param allow  是否允许
     * @param userId 允许连麦的时候观众userId
     * @return
     */
    private String getSendMessage(String cmd, boolean allow, int userId) {
        return getSendMessage(cmd, allow, userId, 0);
    }

    private String getSendMessage(String cmd, int toUserId, int toRoomId) {
        return getSendMessage(cmd, true, toUserId, toRoomId);
    }

    private String getSendMessage(String cmd, boolean allow, int userId, int roomId) {

        JSONObject data = new JSONObject();
        try {
            data.put("userId", mUserId);
            data.put("roomId", mRoomId);
            data.put("userName", mUserName);
            data.put("role", "anchor");
            if (mRoomInfo != null) {
                data.put("token", mRoomInfo.getToken());
            }
            switch (cmd) {
                case CMD_JOIN_ROOM:
                    data.put("allowMic", allow);
                    data.put("version", AliLiveEngine.getSdkVersion());
                    break;
                case CMD_APPROVE_MIC:
                    data.put("approve", allow);
                    data.put("approvedUserId", userId);
                    break;
                case CMD_PUBLISH:
                    data.put("publishRtc", allow);
                    break;
                case CMD_UNPUBLISH:
                    break;
                case CMD_LEAVE_ROOM:
                    break;
                case CMD_APPLY_PK:
                    data.put("toRoomId", roomId);
                    data.put("toUserId", userId);
                    break;
                case CMD_APPROVE_PK:
                    data.put("fromRoomId", roomId);
                    data.put("fromUserId", userId);
                    data.put("approve", allow);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String jsonString = "";

        try {
            JSONObject object = new JSONObject();
            object.put("cmd", cmd);
            String uuid = UUID.randomUUID().toString();
            object.put("seq", uuid);
            object.put("data", data);
            jsonString = object.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonString;
    }

    private void joinChatRoom() {
        if (mRoomInfo == null) {
            Log.d(TAG, "mRoomInfo is null");
            return;
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
        } else if (mPkPullUrl != null) {
            showStopPkDialog();
        } else {
            super.onBackPressed();
        }
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
    protected void onStart() {
        super.onStart();
        //resumePush会恢复之前的美颜。
        if (mAliLiveEngine != null) {
            mAliLiveEngine.resumePush();
        }
        //如果在onResume的时候跟onPause中间改了美颜的状态，那就需要再恢复一下目标状态。
       // onBeautyOpen(isBeautyOpen);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAliLiveEngine != null) {
            stopPreview();
            if (mAliLiveEngine.isPublishing()) {
                mAliLiveEngine.stopPush();
            }
            mAliLiveEngine.destroy();
        }
        if (mAliLiveBeautyManager != null) {
            mAliLiveBeautyManager.destroy();
        }
        if (mOrientationDetector != null) {
            mOrientationDetector.setOrientationChangedListener(null);
        }
        if (mSocketHandler != null) {
            mSocketHandler.send(getSendMessage(CMD_UNPUBLISH, isAllowMicLink, 0));
            mSocketHandler.send(getSendMessage(CMD_LEAVE_ROOM, isAllowMicLink, 0));
            mSocketHandler.setSocketIOCallBack(null);
            mSocketHandler.close();
            mSocketHandler = null;
        }
    }

    private AliLiveCallback.StatusCallback statusCallback = new AliLiveCallback.StatusCallback() {
        @Override
        public void onLiveSdkError(AliLiveEngine aliLiveEngine, final AliLiveError aliLiveError) {
            if (aliLiveError.errorCode == AliLiveError.AliLiveSdkErrorCodePushError) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTvStatus.setText("推流失败");
                        mButtonListView.setButtonEnable("调节参数", true);
                        mButtonListView.setButtonEnable("开始推流", true);
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
            Log.e(SocketConstants.TAG, "onFirstVideoFramePreviewed");
        }

        @Override
        public void onLivePushStarted(AliLiveEngine aliLiveEngine) {
            Log.e(SocketConstants.TAG, "onLivePushStarted:" + aliLiveEngine.getPublishUrl());

            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isAllowMicLink) {
                        mButtonListView.setButtonEnable("PK主播", true);
                    }if(!isReconnect) {
                        mSocketHandler.send(getSendMessage("Publish", isAllowMicLink, 0));
                    }
                    mTvStatus.setText("正在直播");

                }
            });
        }

        @Override
        public void onLivePushStopped(AliLiveEngine aliLiveEngine) {

            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvStatus.setText("推流停止..");
                    mButtonListView.setButtonEnable("调节参数", true);
                    mButtonListView.setButtonEnable("开始推流", true);
                    Log.e(SocketConstants.TAG, "onLivePushStopped");
                }
            });
        }

        @Override
        public void onAudioFocusChanged(int focusChange) {
            if (focusChange < 0) {
                //loss,比如来电话了
                if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
                    mAliLiveEngine.setMute(true);
                    isMuteFlag = true;
                }
            } else if (focusChange > 0) {
                //gain，比如电话挂断了
                if (mAliLiveEngine != null && mAliLiveEngine.isPublishing()) {
                    mAliLiveEngine.setMute(false);
                    isMuteFlag = false;
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
                        ToastUtils.showToast(AnchorActivity.this, "onBGMStateChanged:" + playState + " errorcode " + errorCode);
                    }
                }
            });
        }
    };

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

    private AliLiveCallback.StatsCallback statsCallback = new AliLiveCallback.StatsCallback() {
        @Override
        public void onLiveTotalStats(AliLiveStats aliLiveStats) {
            Log.d(TAG, "onLiveTotalStats");
        }

        @Override
        public void onLiveLocalVideoStats(AliLiveLocalVideoStats aliLiveLocalVideoStats) {
            maliLiveLocalVideoStats = aliLiveLocalVideoStats;
            Log.d(TAG, "onLiveLocalVideoStats");
        }

        @Override
        public void onLiveRemoteVideoStats(AliLiveRemoteVideoStats aliLiveRemoteVideoStats) {
            Log.d(TAG, "onLiveRemoteVideoStats");
        }

        @Override
        public void onLiveRemoteAudioStats(AliLiveRemoteAudioStats aliLiveRemoteAudioStats) {
            Log.d(TAG, "onLiveRemoteAudioStats");
        }
    };

    private AliLiveCallback.RtsCallback rtsCallback = new AliLiveCallback.RtsCallback() {
        @Override
        public void onSubscribeResult(final AliLiveResult aliLiveResult, final String s) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (aliLiveResult.statusCode == AliLiveConstants.AliLiveResultStatusType.AliLiveResultStatusTypeSuccess) {
                        if (s.equals(mPkPullUrl)) {
                            showPkSurfaceView(true);
                            mAliLiveEngine.renderRemoteStreamWithView(mPkRenderView, s);
                        } else {
                            int index = mPullUrlList.indexOf(new VideoUserInfo(s));
                            if (index < 0) {
                                Log.e("WebSocketHandler", "mPullUrlList is not contain s:" + s);
                                return;
                            }
                            VideoUserInfo urlBean = mPullUrlList.get(index);
                            AliLiveRenderView renderView;
                            if (urlBean.getRenderView() != null) {
                                renderView = urlBean.getRenderView();
                            } else {
                                renderView = mAliLiveEngine.createRenderView(true);
                                urlBean.setRenderView(renderView);
                            }
                            mAliLiveEngine.renderRemoteStreamWithView(renderView, s);
                            mVideoListView.setUserList(mPullUrlList);
                        }

                    } else {
                        mPullUrlList.remove(new VideoUserInfo(s));
                        Toast.makeText(AnchorActivity.this, "拉流失败", Toast.LENGTH_SHORT).show();
                    }
                }
            });
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

    private void showNoticeView() {
        if (mApplyMicUserList != null && mApplyMicUserList.size() > 0) {
            mNoticeNumberLl.setVisibility(View.VISIBLE);
            mNoticeNumberTv.setText(mApplyMicUserList.size() + "");
        }
    }

    /**
     * 更新发起连麦申请列表
     * @param userInfo
     */
    private void setNoticeListAdapter(UserInfo userInfo){
        NoticeListAdapter.NoticeItemInfo noticeItemInfo = new NoticeListAdapter.NoticeItemInfo();
        noticeItemInfo.userInfo = userInfo;
        if (!mApplyMicUserList.contains(noticeItemInfo)) {
            mApplyMicUserList.add(noticeItemInfo);
        } else {
            for (NoticeListAdapter.NoticeItemInfo noticeItem : mApplyMicUserList) {
                if (noticeItem.userInfo.getUserId() == noticeItemInfo.userInfo.getUserId()) {
                    noticeItem.micNoticeStatus = MicNoticeStatusView.STATUS_PREPARE;
                }
            }
        }
        mNoticeView.notifyChanged();
        if (mNoticeView.getVisibility() != View.VISIBLE) {
            showNoticeView();
        }
    }

    private AliLiveCallback.NetworkCallback networkCallback = new AliLiveCallback.NetworkCallback() {
        @Override
        public void onNetworkStatusChange(final AliLiveConstants.AliLiveNetworkStatus status) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onNetworkPoor() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(AnchorActivity.this, "弱网");
                }
            });
        }

        @Override
        public void onConnectRecovery() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(AnchorActivity.this, "网络恢复");
                }
            });
        }

        @Override
        public void onReconnectStart() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(AnchorActivity.this, "开始重连");
                }
            });
        }

        @Override
        public void onConnectionLost() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    isReconnect = true;
                    if (mAliLiveEngine != null) {//销毁engine
                        if (mAliLiveEngine.isPublishing()) {
                            mAliLiveEngine.stopPush();
                        }
                        mAliLiveEngine.destroy();
                        mAliLiveEngine = null;
                    }
                    stopPreview();
                    mSocketHandler.close();//重建ws
                    mSocketHandler.setSocketIOCallBack(AnchorActivity.this);
                    mSocketHandler.connect(mRoomIp);
                }
            });
        }

        @Override
        public void onReconnectStatus(final boolean success) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (success) {
                        ToastUtils.showToast(AnchorActivity.this, "重连成功");
                    } else {
                        ToastUtils.showToast(AnchorActivity.this, "重连失败");
                    }
                }
            });
        }
    };
}
