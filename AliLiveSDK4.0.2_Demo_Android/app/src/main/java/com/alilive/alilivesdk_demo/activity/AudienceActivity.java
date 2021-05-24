package com.alilive.alilivesdk_demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.listener.BeautyClickAndSlideListener;
import com.alilive.alilivesdk_demo.listener.ButtonClickListener;
import com.alilive.alilivesdk_demo.listener.OnVideoItemClickListener;
import com.alilive.alilivesdk_demo.socket.ApproveMicNotice;
import com.alilive.alilivesdk_demo.socket.NotifyPublish;
import com.alilive.alilivesdk_demo.socket.Payload;
import com.alilive.alilivesdk_demo.socket.RoomInfo;
import com.alilive.alilivesdk_demo.socket.SocketConstants;
import com.alilive.alilivesdk_demo.socket.VideoUserInfo;
import com.alilive.alilivesdk_demo.socket.WebSocketCallBack;
import com.alilive.alilivesdk_demo.socket.WebSocketHandler;
import com.alilive.alilivesdk_demo.utils.BeautyManager;
import com.alilive.alilivesdk_demo.utils.OrientationDetector;
import com.alilive.alilivesdk_demo.utils.ThreadUtils;
import com.alilive.alilivesdk_demo.utils.ToastUtils;
import com.alilive.alilivesdk_demo.view.AnchorButtonListView;
import com.alilive.alilivesdk_demo.view.AudienceButtonListView;
import com.alilive.alilivesdk_demo.view.BeautyView;
import com.alilive.alilivesdk_demo.view.ButtonListView;
import com.alilive.alilivesdk_demo.view.DataView;
import com.alilive.alilivesdk_demo.view.VideoListView;
import com.alilive.alilivesdk_demo.view.WaitingPublishView;
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
import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.source.UrlSource;
import com.cicada.player.utils.Logger;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_ROOM_ID;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_ROOM_IP;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_USER_ID;
import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_USER_NAME;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPROVE_MIC_NOTICE;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_JOIN_ROOM;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_LEAVE_ROOM;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_NOTIFY_PUBLISH;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_PUBLISH;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_UNPUBLISH;
import static com.alilive.alilivesdk_demo.socket.SocketConstants.CMD_APPLY_MIC;
import static com.alivc.live.AliLiveConfig.CUSTOM_MODE_VIDEO_PREPROCESS;

/**
 * 观众端
 */
public class AudienceActivity extends AppCompatActivity implements View.OnClickListener,
        ButtonClickListener, SurfaceHolder.Callback, WebSocketCallBack, Handler.Callback,
        BeautyClickAndSlideListener {
    private static final String TAG = "AudienceActivity";
    private static final int UDP_BLOCKED = 20059;//udp不通
    private static final int CDN_BLOCKED = 805306367;//cdn节点崩溃
    private AliLiveConfig mAliLiveConfig;
    private TextView mTvRoomTittle;
    private TextView mTvStatus;
    private AliPlayer mAliPlayer;
    private AudienceButtonListView mButtonListView;
    private WaitingPublishView layoutWaiting;
    private Handler handler = new Handler(this);
    private WebSocketHandler mSocketHandler;
    private List<String> mButtonDataList;
    private BeautyView mBeautyView;
    private FrameLayout previewContainer;
    private ViewGroup layoutPreview;
    private String mRoomIp;
    private int mRoomId;
    private int mUserId;
    private String mUserName;
    private RoomInfo mRoomInfo;
    private FrameLayout mContainer;
    private VideoListView mVideoListView;
    private List<VideoUserInfo> mPullUrlList;
    private boolean isPublishing;
    private VideoUserInfo mLocalVideoInfo;
    private VideoUserInfo mAnchorVideoInfo;
    // 美颜开关是否开启
    private boolean isBeautyOpen = true;
    private BeautyManager mBeautyManager;
    private AliLiveBeautyManager mAliLiveBeautyManager;
    // 方向传感器
    private OrientationDetector mOrientationDetector;

    // 直播类
    private AliLiveEngine mAliLiveEngine;

    // 当前方向
    private int mCurrentPosition = 0;
    private boolean hasJoinChatRoom = false;
    /**
     * 当前是否横屏
     */
    private boolean isLandscape = false;
    private DataView mDataView;
    private AliLiveLocalVideoStats maliLiveLocalVideoStats;
    private boolean isApprove = false;
    private FrameLayout mySelfViewContainer;
    private LinearLayout mySelfLayout;
    private ImageView mSwitchCamera;//切换摄像头
    private Button mCancleMicBtn;//取消连麦按钮
    private String mFlvPullUrl;//udp降级flv

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initOrientationDetector();
        setContentView(R.layout.activity_audience);
        initData();
        initView();

    }

    private void initData() {
        //获取传入数据
        Intent intent = getIntent();
        mRoomIp = intent.getStringExtra(EXTRA_ROOM_IP);
        mRoomId = intent.getIntExtra(EXTRA_ROOM_ID, 0);
        mUserId = intent.getIntExtra(EXTRA_USER_ID, 0);
        mUserName = intent.getStringExtra(EXTRA_USER_NAME);

        mSocketHandler = WebSocketHandler.getInstance();
        mSocketHandler.setSocketIOCallBack(this);
        mSocketHandler.connect(mRoomIp);

        mPullUrlList = new ArrayList<>();
    }

    private void initView() {
        mTvRoomTittle = (TextView) findViewById(R.id.tv_room_tittle);
        mTvRoomTittle.setText(String.valueOf(mRoomId));
        mTvStatus = (TextView) findViewById(R.id.tv_status);
        mButtonListView = (AudienceButtonListView) findViewById(R.id.live_buttonlistview);
        mContainer = findViewById(R.id.push_container);
        mButtonDataList = new ArrayList<>();
        mButtonDataList.addAll(Constants.getAudienceButtonList());
        mButtonListView.setData(mButtonDataList);
        mButtonListView.setClickListener(this);
        mVideoListView = findViewById(R.id.video_list_view);
        mBeautyView = findViewById(R.id.live_beauty_view);
        layoutPreview = findViewById(R.id.layout_preview);
        previewContainer = findViewById(R.id.preview_container);
        mySelfViewContainer = findViewById(R.id.myself_container);
        mSwitchCamera = findViewById(R.id.btn_switch_camera);
        mSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAliLiveEngine != null) {
                    mAliLiveEngine.switchCamera();
                }
            }
        });
        mCancleMicBtn = findViewById(R.id.btn_cancle_mic);
        mCancleMicBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelPublish();
            }
        });
        mySelfLayout = findViewById(R.id.myself_layout);
        findViewById(R.id.btn_preview_apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutPreview.setVisibility(View.GONE);
                previewContainer.setVisibility(View.GONE);
                previewContainer.removeAllViews();
                mButtonListView.hideItems(false);
                isPreviewing = false;//预览不显示flag
                mButtonListView.setButtonEnable("连麦", false);
                if (!isPublishing) {
                    layoutWaiting.setVisibility(View.VISIBLE);
                    mSocketHandler.send(getSendMessage(CMD_APPLY_MIC));

                } else {
                    Toast.makeText(v.getContext(), "正在连麦中，请不要重复申请...", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.btn_preview_switch_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAliLiveEngine.switchCamera();
            }
        });
        layoutWaiting = findViewById(R.id.view_waiting_publish);
        layoutWaiting.setClickListener(new WaitingPublishView.OnClickListener() {
            @Override
            public void onCancelClick() {
                cancelPublish();
            }
        });
        mBeautyView.setClickListener(this);

        findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mDataView = findViewById(R.id.ll_data);
    }

    private void initPlayer() {
        Logger.getInstance(this).enableConsoleLog(true);
        Logger.getInstance(this).setLogLevel(Logger.LogLevel.AF_LOG_LEVEL_TRACE);
        mAliPlayer = AliPlayerFactory.createAliPlayer(this.getApplicationContext());
        mAliPlayer.setAutoPlay(true);
        mAliPlayer.setOnErrorListener(new IPlayer.OnErrorListener() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                if (errorInfo.getCode().getValue() == UDP_BLOCKED) {//udp不通，降级到flv模式
                    UrlSource source = new UrlSource();
                    if (!TextUtils.isEmpty(mFlvPullUrl)) {
                        source.setUri(mFlvPullUrl);
                        if (mAliPlayer != null) {
                            mAliPlayer.setDataSource(source);
                            mAliPlayer.prepare();
                        }
                    }
                } else if (errorInfo.getCode().getValue() == CDN_BLOCKED) {//cdn节点崩溃处理
                    showNetExceptionDialog();
                } else {
                    CommonDialog dialog = new CommonDialog(AudienceActivity.this);
                    dialog.setDialogTitle("播放器出错");
                    dialog.setDialogContent(errorInfo.getMsg());
                    dialog.setConfirmButton(TextFormatUtil.getTextFormat(AudienceActivity.this, R.string.liveroom_btn_cancle), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setCancelButton(TextFormatUtil.getTextFormat(AudienceActivity.this, R.string.liveroom_play_refresh), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            startPlayRTMP();
                        }
                    });
                    dialog.show();
                }
            }
        });
        mAliPlayer.setScaleMode(IPlayer.ScaleMode.SCALE_ASPECT_FILL);
        mAliPlayer.setOnRenderingStartListener(new IPlayer.OnRenderingStartListener() {
            @Override
            public void onRenderingStart() {
                removeOtherSurfaceView();
                if (mAnchorVideoInfo != null) {
                    if (mAnchorVideoInfo.getRtcPullUrl() != null) {
                        stopPull(mAnchorVideoInfo.getRtcPullUrl());
                    }
                }
                stopPull(new ArrayList<VideoUserInfo>());
                // 看之前逻辑player开始显示的时候停止拉rts流，暂时先在此处销毁liveengine
                // 该逻辑后续需要优化
                if (mAliLiveEngine != null) {
                    mAliLiveEngine.destroy();
                    mAliLiveEngine = null;
                }
            }
        });

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onButtonClick(String message, int position) {
        switch (message) {
            case "离开房间":
                finish();
                break;
            case "连麦":
                if (mRoomInfo != null && mRoomInfo.isPublished()) {
                    mButtonListView.hideItems(true);
                    initLiveSDK();
                    layoutPreview.setVisibility(View.VISIBLE);
                    startPreview();
                } else {
                    Toast.makeText(this, "主播还未上线，请稍后...", Toast.LENGTH_SHORT).show();
                }
                break;
            case "取消连麦":
                cancelPublish();
                break;
            case "美颜":
                if (!isPublishing) {
                    Toast.makeText(this, "尚未连麦，暂不支持美颜调节", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (mBeautyView.getVisibility() == View.VISIBLE) {
                    mBeautyView.setVisibility(View.GONE);
                } else {
                    mBeautyView.setVisibility(View.VISIBLE);
                    mButtonListView.hideItems(true);
                }

                break;
            case "数据指标":
                if (maliLiveLocalVideoStats == null) {
                    Toast.makeText(AudienceActivity.this, "没有数据信息", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(AudienceActivity.this, "没有数据信息", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            default:
                break;
        }
    }

    private void cancelPublish() {
        if (isPublishing) {
            layoutPreview.setVisibility(View.GONE);
            stopPreview();
            stopPublish();
            startPlayRTMP();
        }
        layoutWaiting.setVisibility(View.INVISIBLE);
        mSocketHandler.send(getSendMessage(CMD_UNPUBLISH));
        mButtonListView.hideItems(false);
        mButtonListView.setButtonEnable("连麦", true);
        mySelfViewContainer.removeAllViews();
        mySelfViewContainer.setVisibility(View.GONE);
        mySelfLayout.setVisibility(View.GONE);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mAliPlayer != null) {
            mAliPlayer.setSurface(holder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (mAliPlayer != null) {
            mAliPlayer.surfaceChanged();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mAliPlayer != null) {
            mAliPlayer.setSurface(null);
        }
    }

    @Override
    public void onOpen() {
        joinRoom();
    }

    @Override
    public void onMessage(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    JSONObject object = new JSONObject(msg);
                    String cmd = object.getString("cmd");
                    JSONObject payloadObject = object.getJSONObject("payload");
                    Gson gson = new Gson();
                    switch (cmd) {
                        case CMD_JOIN_ROOM:
                            Type type = new TypeToken<Payload<RoomInfo>>() {
                            }.getType();
                            Payload<RoomInfo> joinRoomPayload = gson.fromJson(payloadObject.toString(), type);
                            mRoomInfo = joinRoomPayload.getData();
                            if(mRoomInfo!=null&&mRoomInfo.getFlvPullUrl()!=null) {
                                mFlvPullUrl = mRoomInfo.getFlvPullUrl();
                            }
                            if (joinRoomPayload.getCode() == 0 && mRoomInfo != null) {
                                if (!mRoomInfo.isAllowMic()) {
                                    mButtonListView.setButtonEnable("连麦", false);
                                }
//                                if (mRoomInfo.isAllowMic()) {
//                                    mButtonListView.addItem("申请连麦");
//                                    mButtonListView.addItem("取消连麦");
//                                }
                                if (mRoomInfo.isPublished()) {
                                    startPlayRTMP();
                                } else {
                                    showTipDialog("提示", "主播还未上麦，请稍候...");
                                }
                                mSocketHandler.startHeartbeat(mUserId, mRoomInfo.getToken());
                                joinChatRoom();
                                mTvStatus.setText("加入房间成功");
                            } else {
                                mTvStatus.setText("加入房间失败");
                                showTipDialog("加入房间失败", joinRoomPayload.getCodeMsg());
                            }
                            break;
                        case CMD_APPROVE_MIC_NOTICE:
                            Type noticeType = new TypeToken<Payload<ApproveMicNotice>>() {
                            }.getType();
                            Payload<ApproveMicNotice> approvePayload = gson.fromJson(payloadObject.toString(), noticeType);
                            ApproveMicNotice notice = approvePayload.getData();
                            if (notice.isApprove()) {
                                //开始推流
                                startPublish(notice.getRtcPushUrl());
                                //开始拉主播大流
                                if (mAnchorVideoInfo == null) {
                                    mAnchorVideoInfo = new VideoUserInfo();
                                    mAnchorVideoInfo.setUserId(notice.getAnchorUserId());
                                }

                                mAnchorVideoInfo.setRtcPullUrl(notice.getAnchorRtcPullUrl());
                                startPull(mAnchorVideoInfo.getRtcPullUrl(), true);

                                List<VideoUserInfo> urlList = notice.getRtcPullUrls();
                                //开始拉其他观众的小流
                                if (urlList != null && urlList.size() > 0) {
                                    int size = urlList.size();
                                    for (int i = 0; i < size; i++) {
                                        VideoUserInfo url = urlList.get(i);
                                        if (!mPullUrlList.contains(url)) {
                                            mPullUrlList.add(url);
                                        }
                                    }
                                }
                                startPull();
                                previewContainer.removeAllViews();
                                previewContainer.setVisibility(View.GONE);
                            } else {
                                Toast.makeText(AudienceActivity.this, "连麦申请被拒接", Toast.LENGTH_SHORT).show();
                                previewContainer.setVisibility(View.GONE);
                                layoutWaiting.setVisibility(View.GONE);
                                mButtonListView.setButtonEnable("连麦", true);
                            }

                            break;
                        case CMD_NOTIFY_PUBLISH:

                            Type noticePublishType = new TypeToken<Payload<NotifyPublish>>() {
                            }.getType();
                            Payload<NotifyPublish> notivePayload = gson.fromJson(payloadObject.toString(), noticePublishType);
                            NotifyPublish noticePublish = notivePayload.getData();
                            //处理主播下麦
                            if (TextUtils.isEmpty(noticePublish.getAnchorRtcPullUrl()) && TextUtils.isEmpty(noticePublish.getAnchorRtmpPullUrl())) {
                                if (mRoomInfo != null && mRoomInfo.isPublished()) {
                                    showTipDialog("提示", "主播下麦");
                                    mRoomInfo.setIsPublished(false);
                                    if (isPublishing) {
                                        stopPull(mAnchorVideoInfo.getRtcPullUrl());
                                        stopPull(new ArrayList<VideoUserInfo>());
                                        stopPreview();
                                        stopPublish();
                                    } else {
                                        stopPlayRTMP();
                                    }
                                }
                            } else {
                                //处理主播上麦
                                if (mRoomInfo != null && !mRoomInfo.isPublished()) {
                                    mRoomInfo.setRtmpPullUrl(noticePublish.getAnchorRtmpPullUrl());
                                    mRoomInfo.setRtsPullUrl(noticePublish.getAnchorRtsPullUrl());
                                    mRoomInfo.setIsPublished(true);
                                    startPlayRTMP();
                                    isPublishing = false;

                                } else if (!mRoomInfo.getRtmpPullUrl().equals(noticePublish.getAnchorRtmpPullUrl())) {
                                    mRoomInfo.setRtmpPullUrl(noticePublish.getAnchorRtmpPullUrl());
                                    mRoomInfo.setRtsPullUrl(noticePublish.getAnchorRtsPullUrl());
                                    startPlayRTMP();
                                }
                            }
                            if (!isPublishing) {
                                return;
                            }
                            List<VideoUserInfo> urlList = noticePublish.getRtcPullUrls();
                            if (urlList == null) {
                                return;
                            }
                            //移除自己
                            Iterator<VideoUserInfo> iterator = urlList.iterator();
                            while (iterator.hasNext()) {
                                VideoUserInfo userInfo = iterator.next();
                                if (userInfo.getUserId() == mUserId) {
                                    iterator.remove();
                                }
                            }
                            stopPull(urlList);

                            if (urlList != null && urlList.size() > 0) {
                                int size = urlList.size();
                                for (int i = 0; i < size; i++) {
                                    VideoUserInfo url = urlList.get(i);
                                    if (!mPullUrlList.contains(url)) {
                                        mPullUrlList.add(url);
                                        Log.e("WebSocketHandler", "add url:" + url);
                                    }
                                }
                            }
                            startPull();
                            if (noticePublish.getType() == 1) {
                                if (mAnchorVideoInfo != null) {//主要为了切换网络后，要重新订阅主播的RtcPullUrl
                                    mAnchorVideoInfo.setRtcPullUrl(noticePublish.getAnchorRtcPullUrl());
                                    startPull(mAnchorVideoInfo.getRtcPullUrl(), true);
                                }
                            }
                            mySelfViewContainer.removeAllViews();
                            mySelfViewContainer.addView(mLocalVideoInfo.getRenderView());
                            mySelfViewContainer.setVisibility(View.VISIBLE);
                            mySelfLayout.setVisibility(View.VISIBLE);

                            break;
                        case CMD_APPLY_MIC:
                            JSONObject payload = object.getJSONObject("payload");
                            int code = payload.getInt("code");
                            String message = payload.getString("codeMsg");
                            if (code != 1009) {
                                layoutWaiting.setVisibility(View.GONE);
                                if(mRoomInfo.isAllowMic()) {
                                    mButtonListView.setButtonEnable("连麦", true);
                                }
                                Toast.makeText(AudienceActivity.this, message, Toast.LENGTH_SHORT).show();
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
        mSocketHandler.send(getSendMessage(CMD_JOIN_ROOM));
    }

    private void joinChatRoom() {
        if (mRoomInfo == null) {
            Log.d(TAG, "mRoomInfo is null");
            return;
        }
    }

    private String getSendMessage(String cmd) {
        JSONObject data = new JSONObject();
        try {
            data.put("userId", mUserId);
            data.put("roomId", mRoomId);
            data.put("userName", mUserName);
            data.put("role", "audience");
            data.put("roomId", mRoomId);
            data.put("version", AliLiveEngine.getSdkVersion());
            if (mRoomInfo != null) {
                data.put("token", mRoomInfo.getToken());
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

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private void addSubView(View view) {
        Log.e(SocketConstants.TAG, "addSubView11111");
        ViewParent parent = view.getParent();
        if (parent != null && parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(view);
        }
        mContainer.removeAllViews();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        mContainer.addView(view, 0, params);//添加到布局中
    }

    /**
     * 是否正在预览
     */
    private boolean isPreviewing = false;

    private void startPublish(String url) {
        initLiveSDK();
        startPreview();

        if (TextUtils.isEmpty(url)) {
            return;
        }
        isPublishing = true;
        mAliLiveEngine.startPush(url);

    }

    private void stopPublish() {
        Log.d(TAG, "stopPublish");
        isPublishing = false;
        if (mAliLiveEngine != null) {
            if (mAliLiveEngine.isPublishing()) {
                mAliLiveEngine.stopPush();
            }
        }
    }

    /**
     * 开启预览
     */
    private void startPreview() {
        if (isPreviewing) {
            return;
        }
        // 开启方向传感器的监听
        if (mOrientationDetector != null && mOrientationDetector.canDetectOrientation()) {
            mOrientationDetector.enable();
        }
        if (mLocalVideoInfo == null) {
            AliLiveRenderView aliLiveRenderView = mAliLiveEngine.createRenderView(true);
            mLocalVideoInfo = new VideoUserInfo();
            mLocalVideoInfo.setLocalUser(true);
            mLocalVideoInfo.setUserId(mUserId);
            mLocalVideoInfo.setUserName(mUserName);
            mLocalVideoInfo.setRenderView(aliLiveRenderView);
        }
        previewContainer.removeAllViews();
        previewContainer.addView(mLocalVideoInfo.getRenderView());

        mAliLiveEngine.startPreview(mLocalVideoInfo.getRenderView());
        if (isBeautyOpen) {
            onBeautyOpen(true);
            mAliLiveBeautyManager.enable(AliLiveBeautyManager.EnableType.Basic);
            if (mBeautyManager != null) {
                mBeautyManager.resumeParams();
            }
        }
    }

    private void stopPreview() {
        Log.d(TAG, "stopPreview");
        initLiveSDK();
        mAliLiveEngine.stopPreview();
        if (mOrientationDetector != null) {
            mOrientationDetector.disable();
        }
        //去除右侧小窗口列表中的预览画面
        Iterator<VideoUserInfo> iterator = mPullUrlList.iterator();
        while (iterator.hasNext()) {
            VideoUserInfo remoteUserInfo = iterator.next();
            if (remoteUserInfo.isLocalUser()) {
                iterator.remove();
            }
        }
        mVideoListView.setUserList(mPullUrlList);
    }

    private void initLiveSDK() {
        Log.d(TAG, "initLiveSDK");
        if (mAliLiveEngine == null) {

            AliLiveRTMPConfig rtmpConfig = new AliLiveRTMPConfig();
            rtmpConfig.videoInitBitrate = 1000;
            rtmpConfig.videoTargetBitrate = 1500;
            rtmpConfig.videoMinBitrate = 600;

            mAliLiveConfig = new AliLiveConfig(rtmpConfig);
            mAliLiveConfig.videoPushProfile = AliLiveConstants.AliLiveVideoPushProfile.AliLiveVideoProfile_540P;
            mAliLiveConfig.enableHighDefPreview = false;
            mAliLiveConfig.customPreProcessMode = CUSTOM_MODE_VIDEO_PREPROCESS;
            mAliLiveConfig.videoFPS = 20;
            // TODO: 此处填写httpdns服务的accountId
            mAliLiveConfig.accountId = Constants.HTTP_DNS_ACCOUNT_ID;
            mAliLiveConfig.extra = Constants.LIVE_EXTRA_INFO;
            mBeautyManager = new BeautyManager();
            mAliLiveEngine = AliLiveEngine.create(AudienceActivity.this, mAliLiveConfig);
            mAliLiveBeautyManager = mAliLiveEngine.getBeautyManager();
            mBeautyManager.setmAliLiveBeautyManager(mAliLiveBeautyManager);
            mAliLiveEngine.setStatsCallback(statsCallback);
            mAliLiveEngine.setRtsCallback(rtsCallback);
            mAliLiveEngine.setStatusCallback(statusCallback);
            mAliLiveEngine.setNetworkCallback(networkCallback);
            mAliLiveEngine.setPreviewMode(AliLiveConstants.AliLiveRenderMode.AliLiveRenderModeAuto, AliLiveConstants.AliLiveRenderMirrorMode.AliLiveRenderMirrorModeOnlyFront);
            Log.d(TAG, "initLiveSDK end");
        }
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE && mAliLiveEngine != null) {
            //            当前屏幕为横屏
            isLandscape = true;
            if (mCurrentPosition == 90) {
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

    private void initOrientationDetector() {
        mOrientationDetector = new OrientationDetector(AudienceActivity.this);
        mOrientationDetector.setOrientationChangedListener(new OrientationDetector.OrientationChangedListener() {
            @Override
            public void onOrientationChanged() {
                int orientation = mOrientationDetector.getOrientation();
                if ((orientation >= 35) && (orientation < 135)) {
                    if (isLandscape && mCurrentPosition != 90 && mAliLiveEngine != null) {
                        mAliLiveEngine.setDeviceOrientationMode(AliLiveConstants.AliLiveOrientationMode.AliLiveOrientationModeLandscapeRight);
                    }
                    mCurrentPosition = 90;
                } else if ((orientation >= 200) && (orientation < 335)) {
                    if (isLandscape && mCurrentPosition != 270 && mAliLiveEngine != null) {
                        mAliLiveEngine.setDeviceOrientationMode(AliLiveConstants.AliLiveOrientationMode.AliLiveOrientationModeLandscapeLeft);
                    }
                    mCurrentPosition = 270;
                } else {
                    mCurrentPosition = 0;
                }
            }
        });
    }

    /**
     * 停止拉流
     *
     * @param urlList 正在推流用户，需要停止除正在推流之外用户的
     */
    private void stopPull(List<VideoUserInfo> urlList) {

        if (mPullUrlList == null || mPullUrlList.size() == 0) {
            return;
        }
        Log.d(TAG, "stopPull");
        Iterator<VideoUserInfo> iterator = mPullUrlList.iterator();
        while (iterator.hasNext()) {
            VideoUserInfo remoteUserInfo = iterator.next();
            if (!remoteUserInfo.isLocalUser()) {
                if (urlList == null || !urlList.contains(remoteUserInfo)) {
                    if (remoteUserInfo.getRtcPullUrl() != null) {
                        stopPull(remoteUserInfo.getRtcPullUrl());
                        Log.d(TAG, "stopPull" + remoteUserInfo.getRtcPullUrl());
                        iterator.remove();
                        mVideoListView.setUserList(mPullUrlList);
                    }
                }
            }

        }
    }

    private void stopPull(String pullUrl) {
        if(mAliLiveEngine!=null) {
            mAliLiveEngine.unSubscribeStream(pullUrl);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAliLiveEngine != null) {
            if (mAliLiveEngine.isPublishing()) {
                mSocketHandler.send(getSendMessage(CMD_UNPUBLISH));
                mAliLiveEngine.stopPreview();
                mAliLiveEngine.stopPush();
            }
            mAliLiveEngine.destroy();
        }
        if (mOrientationDetector != null) {
            mOrientationDetector.setOrientationChangedListener(null);
        }
        if (mOrientationDetector != null) {
            mOrientationDetector.setOrientationChangedListener(null);
        }
        if (mAliPlayer != null) {
            mAliPlayer.stop();
            mAliPlayer.setSurface(null);
            mAliPlayer.release();
            mAliPlayer = null;
        }
        if (mSocketHandler != null) {
            mSocketHandler.send(getSendMessage(CMD_LEAVE_ROOM));
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
                        ToastUtils.showToast(AudienceActivity.this, "推流失败:" + aliLiveError.errorDescription);
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
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSocketHandler.send(getSendMessage(CMD_PUBLISH));
                    ToastUtils.showToast(AudienceActivity.this, "开始推流");
                }
            });
        }

        @Override
        public void onLivePushStopped(AliLiveEngine aliLiveEngine) {
            Log.e(SocketConstants.TAG, "onLivePushStopped");
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
            }
        }

        @Override
        public void onBGMStateChanged(AliLiveEngine publisher, final AliLiveConstants.AliLiveAudioPlayingStateCode playState,
                                      final AliLiveConstants.AliLiveAudioPlayingErrorCode errorCode) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (errorCode != AliLiveConstants.AliLiveAudioPlayingErrorCode.AliLiveAudioPlayingNoError) {
                        ToastUtils.showToast(AudienceActivity.this, "onBGMStateChanged:" + playState + " errorcode " + errorCode);
                    }
                }
            });
        }
    };

    private AliLiveCallback.StatsCallback statsCallback = new AliLiveCallback.StatsCallback() {
        @Override
        public void onLiveTotalStats(AliLiveStats aliLiveStats) {
            Log.d(TAG, "onLiveTotalStats");
        }

        @Override
        public void onLiveLocalVideoStats(AliLiveLocalVideoStats aliLiveLocalVideoStats) {
            Log.d(TAG, "onLiveLocalVideoStats");
            maliLiveLocalVideoStats = aliLiveLocalVideoStats;
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
            Log.e("WebSocketHandler", "onSubscribeResult");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (aliLiveResult.statusCode == AliLiveConstants.AliLiveResultStatusType.AliLiveResultStatusTypeSuccess) {
                        if (mAnchorVideoInfo != null && mAnchorVideoInfo.equals(new VideoUserInfo(s))) {
                            stopPlayRTMP();
                            if (mAnchorVideoInfo.getRenderView() == null) {
                                mAnchorVideoInfo.setRenderView(mAliLiveEngine.createRenderView(false));
                            }
                            if (mVideoListView != null) {
                                mVideoListView.setOnItemClickListener(new OnVideoItemClickListener() {
                                    @Override
                                    public void onItemClick() {
                                        if (mAliLiveEngine != null) {
                                            mAliLiveEngine.switchCamera();
                                        }
                                    }

                                    @Override
                                    public void onExitClick(VideoUserInfo userInfo) {
                                        cancelPublish();
                                    }
                                });
                            }
                            addSubView(mAnchorVideoInfo.getRenderView());
                            mAliLiveEngine.renderRemoteStreamWithView(mAnchorVideoInfo.getRenderView(), s);
                            mVideoListView.setUserList(mPullUrlList);
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
                        String errorDescription = "";
                        int errorCode = 0;
                        if (aliLiveResult.error != null) {
                            errorCode = aliLiveResult.error.errorCode;
                            errorDescription = aliLiveResult.error.errorDescription;
                        }
                        //  Toast.makeText(AudienceActivity.this, "拉流失败", Toast.LENGTH_SHORT).show();
//                        Toast.makeText(AudienceActivity.this, "拉流失败 url " + s + ",errorcode:"+ errorCode + "," + errorDescription,
//                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        @Override
        public void onUnSubscribeResult(final AliLiveResult aliLiveResult, final String s) {
            Log.e("WebSocketHandler", "onUnSubscribeResult:" + s);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (aliLiveResult.statusCode == AliLiveConstants.AliLiveResultStatusType.AliLiveResultStatusTypeSuccess) {
                        Toast.makeText(AudienceActivity.this, "拉流停止", Toast.LENGTH_SHORT).show();
                    } else {
                        String errorDescription = "";
                        int errorCode = -1;
                        if (aliLiveResult.error != null) {
                            errorCode = aliLiveResult.error.errorCode;
                            errorDescription = aliLiveResult.error.errorDescription;
                        }
                        Toast.makeText(AudienceActivity.this, "拉流停止失败" + s + ", errorcode:" + errorCode + ", " + errorDescription, Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }

        @Override
        public void onFirstPacketReceivedWithUid(String s) {

        }

        @Override
        public void onFirstRemoteVideoFrameDrawn(String s, AliLiveConstants.AliLiveVideoTrack aliLiveVideoTrack) {
            Log.e("WebSocketHandler", "onFirstRemoteVideoFrameDrawn:" + s);
            removeOtherSurfaceView();

        }
    };

    private void removeOtherSurfaceView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int childCount = mContainer.getChildCount();
                if (childCount > 1) {
                    mContainer.removeViews(1, childCount - 1);
                }
            }
        });
    }

    private void showTipDialog(String tittle, String msg) {
        CommonDialog dialog = new CommonDialog(this);
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

    /**
     * 开始拉流
     */
    private void startPull() {
        Log.e("WebSocketHandler", "startPull");
        if (mPullUrlList != null && mPullUrlList.size() > 0) {
            for (VideoUserInfo urlBean : mPullUrlList) {
                if (!urlBean.isPulling()) {
                    startPull(urlBean.getRtcPullUrl(), true);
                    urlBean.setPulling(true);
                    Log.e("WebSocketHandler", "startPull:" + urlBean.getRtcPullUrl());
                }
            }
        }
    }

    private void startPull(String pullUrl, boolean isSubBigVideo) {
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
        layoutWaiting.setVisibility(View.INVISIBLE);
        mAliLiveEngine.subscribeStream(pullUrl);
    }

    private SurfaceView mSurfaceView;

    /**
     * 开始播放rtmp的流
     */
    private void startPlayRTMP() {
        //mTvStatus.setText("开始拉取RTMP流");
        if (mAliPlayer == null) {
            initPlayer();
            mSurfaceView = new SurfaceView(AudienceActivity.this);
            mSurfaceView.getHolder().addCallback(AudienceActivity.this);
        }
        addSubView(mSurfaceView);
        UrlSource source = new UrlSource();
        if (mRoomInfo != null) {
            if (!TextUtils.isEmpty(mRoomInfo.getRtsPullUrl())) {//优先播放rts
                source.setUri(mRoomInfo.getRtsPullUrl());
            } else if (!TextUtils.isEmpty(mRoomInfo.getRtmpPullUrl())) {
                source.setUri(mRoomInfo.getRtmpPullUrl());
            } else {
                showTipDialog(getString(R.string.prompt), getString(R.string.pull_url_empty));
                return;
            }
            if (mAliPlayer != null) {
                mAliPlayer.setDataSource(source);
                mAliPlayer.prepare();
            }
        }
    }

    private void stopPlayRTMP() {
        if (mAliPlayer != null) {
            mAliPlayer.stop();
        }
    }

    /**
     * 美颜美型的交互页面
     */
    @Override
    public void onButtonClick(String pageName, int pageIndex, String message, int position) {
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
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
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
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
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
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
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
        onBeautyOpen(isBeautyOpen);
        if (!isBeautyOpen) {
            return;
        }
        if (mBeautyManager != null) {
            mBeautyManager.onPageSwitch(pageName, pageIndex, isCheck);
        }
    }

    public void onBeautyOpen(boolean isOpen) {
        if (mAliLiveEngine == null) {
            initLiveSDK();
        }
        isBeautyOpen = isOpen;
        mAliLiveBeautyManager.enable(isOpen ? AliLiveBeautyManager.EnableType.Basic : AliLiveBeautyManager.EnableType.Off);
    }

    @Override
    public boolean handleMessage(@NonNull Message msg) {
        return false;
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
                }
            });
        }

        @Override
        public void onConnectRecovery() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onReconnectStart() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onConnectionLost() {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showNetExceptionDialog();//网络切换
                }
            });
        }

        @Override
        public void onReconnectStatus(final boolean success) {
            ThreadUtils.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (success) {
                        ToastUtils.showToast(AudienceActivity.this, "重连成功");
                    } else {
                        ToastUtils.showToast(AudienceActivity.this, "重连失败");
                    }
                }
            });
        }
    };

    /**
     * 网络异常弹窗
     */
    private void showNetExceptionDialog() {
        CommonDialog dialog = new CommonDialog(AudienceActivity.this);
        dialog.setDialogTitle(getString(R.string.network_exception_title));
        dialog.setDialogContent(getString(R.string.network_exception_desc));
        dialog.setConfirmButton(TextFormatUtil.getTextFormat(AudienceActivity.this, R.string.liveroom_btn_cancle), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }
}
