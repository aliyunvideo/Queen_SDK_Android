package com.alilive.alilivesdk_demo.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.utils.ThreadUtils;
import com.alilive.alilivesdk_demo.utils.ToastUtils;
import com.alilive.alilivesdk_demo.view.AnchorButtonListView;
import com.alilive.alilivesdk_demo.view.DataView;
import com.alilive.alilivesdk_demo.wheel.widget.CommonDialog;
import com.alilive.alilivesdk_demo.wheel.widget.TextFormatUtil;
import com.alivc.live.AliLiveCallback;
import com.alivc.live.AliLiveConfig;
import com.alivc.live.AliLiveConstants;
import com.alivc.live.AliLiveEngine;
import com.alivc.live.AliLiveRTMPConfig;
import com.alivc.live.AliLiveRenderView;
import com.alivc.live.bean.AliLiveLocalVideoStats;
import com.alivc.live.bean.AliLiveRemoteAudioStats;
import com.alivc.live.bean.AliLiveRemoteVideoStats;
import com.alivc.live.bean.AliLiveResult;
import com.alivc.live.bean.AliLiveStats;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.listener.ButtonClickListener;
import com.alilive.alilivesdk_demo.view.IosStyleSheetDialog;
import com.google.zxing.activity.CaptureActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 测试RTC拉流界面
 */
public class PullTestActivity extends AppCompatActivity implements ButtonClickListener {
    private static final String TAG = "PullTestActivity";

    private AnchorButtonListView mButtonListView;
    private IosStyleSheetDialog mIosStyleSheetDialog;
    private FrameLayout mFlContainer;
    private AliLiveEngine mLiveEngine;
    private int mSelectPosition;
    private Map<Integer, PullUrlEntity> mPullUrlMap;
    private Button mScanBtn;//扫码
    private EditText mPullUrlET;//拉流地址
    private Button mPullStartBtn;//开始拉流btn
    private String mPullUrl = "";//pull url
    private AliLiveLocalVideoStats maliLiveLocalVideoStats;
    private DataView mDataView;
    private boolean isStopPullFlag = false;//是否点击过停止播放
    private boolean isMuteFlag= false;//是否点击过静音播放
    private boolean isPulling = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_pull_test);
        initView();
        initData();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            当前屏幕为横屏
        } else {
//            当前屏幕为竖屏
        }
    }

    private void initData() {
        mPullUrlMap = new HashMap<>();
        AliLiveRTMPConfig rtmpConfig = new AliLiveRTMPConfig();
        AliLiveConfig liveConfig = new AliLiveConfig(rtmpConfig);
        // TODO: 此处填写httpdns服务的accountId
        liveConfig.accountId = Constants.HTTP_DNS_ACCOUNT_ID;
        liveConfig.extra = Constants.LIVE_EXTRA_INFO;
        mLiveEngine = AliLiveEngine.create(this, liveConfig);
        mLiveEngine.setStatsCallback(new AliLiveCallback.StatsCallback() {
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
        });
        mLiveEngine.setRtsCallback(new AliLiveCallback.RtsCallback() {
            @Override
            public void onSubscribeResult(final AliLiveResult aliLiveResult, final String s) {
                Log.e(TAG, "onSubscribeResult:" + s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (aliLiveResult.statusCode == AliLiveConstants.AliLiveResultStatusType.AliLiveResultStatusTypeSuccess) {
                            final int position = getPullUrlPosition(s);
                            if (position < 0) {
                                return;
                            }
                            mPullUrl = mPullUrlMap.get(position).pullUrl;
                            PullUrlEntity pullUrlEntity = mPullUrlMap.get(position);
                            AliLiveRenderView renderView;
                            if (pullUrlEntity.renderView == null) {
                                renderView = mLiveEngine.createRenderView(true);
                                pullUrlEntity.renderView = renderView;
                                final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                                switch (position) {
                                    case 0:
                                        mFlContainer.addView(renderView, params);
                                        break;
                                    default:
                                        break;
                                }

                            } else {
                                renderView = pullUrlEntity.renderView;
                            }
                            mLiveEngine.renderRemoteStreamWithView(renderView, s);
                            Toast.makeText(PullTestActivity.this, "拉流成功" + mSelectPosition,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            String errorDescription = "";
                            int errorCode = 0;
                            if (aliLiveResult.error != null) {
                                errorCode = aliLiveResult.error.errorCode;
                                errorDescription = aliLiveResult.error.errorDescription;
                            }

                            Toast.makeText(PullTestActivity.this, "拉流失败 url " + s + ",errorcode:" + errorCode + "," + errorDescription + mSelectPosition,

                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onUnSubscribeResult(final AliLiveResult aliLiveResult, final String s) {
                Log.e(TAG, "onUnSubscribeResult:" + s);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (aliLiveResult.statusCode == AliLiveConstants.AliLiveResultStatusType.AliLiveResultStatusTypeSuccess) {
                            Toast.makeText(PullTestActivity.this, "拉流停止", Toast.LENGTH_SHORT).show();
                        } else {
                            String errorDescription = "";
                            int errorCode = -1;
                            if (aliLiveResult.error != null) {
                                errorCode = aliLiveResult.error.errorCode;
                                errorDescription = aliLiveResult.error.errorDescription;
                            }
                            Toast.makeText(PullTestActivity.this, "拉流停止失败" + s + ", errorcode:" + errorCode + ", " + errorDescription, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onFirstPacketReceivedWithUid(String s) {
                Log.e(TAG, "onFirstPacketReceivedWithUid");
            }

            @Override
            public void onFirstRemoteVideoFrameDrawn(String s,
                                                     AliLiveConstants.AliLiveVideoTrack aliLiveVideoTrack) {
                Log.e(TAG, "onFirstRemoteVideoFrameDrawn");

            }
        });

        mLiveEngine.setNetworkCallback(new AliLiveCallback.NetworkCallback() {
            @Override
            public void onNetworkStatusChange(final AliLiveConstants.AliLiveNetworkStatus status) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(PullTestActivity.this, "网络状态改变 = " + status.name());
                    }
                });
            }

            @Override
            public void onNetworkPoor() {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(PullTestActivity.this, "弱网");
                    }
                });
            }

            @Override
            public void onConnectRecovery() {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(PullTestActivity.this, "网络恢复");
                    }
                });
            }

            @Override
            public void onReconnectStart() {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(PullTestActivity.this, "开始重连");
                    }
                });
            }

            @Override
            public void onConnectionLost() {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(PullTestActivity.this, "网络断开");
                    }
                });
            }

            @Override
            public void onReconnectStatus(final boolean success) {
                ThreadUtils.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            ToastUtils.showToast(PullTestActivity.this, "重连成功");
                        } else {
                            ToastUtils.showToast(PullTestActivity.this, "重连失败");
                        }

                    }
                });
            }
        });


    }

    private void initView() {
        //初始化组件
        mFlContainer = findViewById(R.id.fl_container);

        mButtonListView = findViewById(R.id.live_buttonlistview);

        List<String> data = new ArrayList<>();
        data.addAll(Constants.getPullActivityButtonList());
        mButtonListView.setData(data);
        mButtonListView.setClickListener(this);
        mButtonListView.setVisibility(View.GONE);
        mPullUrlET = findViewById(R.id.pull_rtc_push_url);
        mPullStartBtn = findViewById(R.id.pull_rtc_start_btn);
        mPullStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(mPullUrlET.getText().toString())) {
                    if(!isPulling) {
                        showTipDialog("提示", "拉流地址不存在...");
                    }else{
                        showTipDialog("提示", "正在拉流中...");
                    }
                    return;
                }else {
                    mPullUrl = mPullUrlET.getText().toString();
                    insertPullUrl(mSelectPosition, mPullUrl);
                    startPull(mPullUrl);
                }
            }
        });
        findViewById(R.id.pull_rtc_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mScanBtn = findViewById(R.id.pull_rtc_scan_image);
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCaptureActivityForResult(mSelectPosition);
            }
        });
        mDataView = findViewById(R.id.ll_data);
    }

    @Override
    public void onButtonClick(String message, int position) {
        switch (message) {
            case "静音":
                if(!isMuteFlag) {
                    mLiveEngine.setPlayoutVolume(0);
                    Toast toast = Toast.makeText(this, "已静音", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    isMuteFlag = true;
                }else{
                    mLiveEngine.setPlayoutVolume(50);
                    Toast toast = Toast.makeText(this, "取消静音", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
                    isMuteFlag = false;
                }
                break;
            case "结束观看":
                if (!isStopPullFlag) {
                    stopPull();
                    isStopPullFlag = true;
                } else {
                    startPull(mPullUrl);
                    isStopPullFlag = false;
                }
                break;
            case "听筒切换":
                switchReceiver();
                Toast toast = Toast.makeText(this, "听筒切换中", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                break;
            default:
                break;
        }
    }

    /**
     * 切换听筒
     */
    private void switchReceiver() {
        boolean isSpeakerOn = mLiveEngine.isEnableSpeakerphone();
        mLiveEngine.enableSpeakerphone(!isSpeakerOn);
    }

    /**
     * 停止拉流
     */
    private void stopPull() {
        mLiveEngine.unSubscribeStream(mPullUrl);
    }

    /**
     * 开始拉流
     */
    private void startPull(String url) {
        if (TextUtils.isEmpty(url)) {
            if(!isPulling) {
                showTipDialog("提示", "拉流地址不存在...");
            }else{
                showTipDialog("提示", "正在拉流中...");
            }
        } else {
            mLiveEngine.subscribeStream(url);
            isPulling = true;
            mButtonListView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 开始扫码
     */
    private void startCaptureActivityForResult(int requestCode) {
        Intent intent = new Intent(PullTestActivity.this, CaptureActivity.class);
        startActivityForResult(intent, requestCode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (resultCode) {
            case RESULT_OK:
                if (requestCode == mSelectPosition) {
                    String pullUrl = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                    mPullUrl = pullUrl;
                    boolean isUsed = hasPullUrlUsed(pullUrl);
                    if (isUsed) {
                        Toast.makeText(PullTestActivity.this, "该拉流地址正在被使用，请选择其他拉流地址", Toast.LENGTH_SHORT).show();
                    } else {
                        insertPullUrl(mSelectPosition, pullUrl);
                        startPull(pullUrl);
                    }
                }
                break;
            case RESULT_CANCELED:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLiveEngine.destroy();
    }

    private void insertPullUrl(int position, String pullUrl) {
        PullUrlEntity entity;
        if (!mPullUrlMap.containsKey(position)) {
            entity = new PullUrlEntity();
            entity.position = position;
            mPullUrlMap.put(position, entity);
        } else {
            entity = mPullUrlMap.get(position);
        }
        entity.pullUrl = pullUrl;
    }

    private int getPullUrlPosition(String pullUrl) {
        int position = -1;
        Iterator<Integer> iterator = mPullUrlMap.keySet().iterator();
        while (iterator.hasNext() && position == -1) {
            int key = iterator.next();
            if (pullUrl.equals(mPullUrlMap.get(key).pullUrl)) {
                position = key;
            }

        }
        return position;
    }

    /**
     * 判断该拉流地址是否正在使用
     *
     * @param url
     * @return
     */
    private boolean hasPullUrlUsed(String url) {
        int position = getPullUrlPosition(url);
        if (position < 0) {
            return false;
        } else {
            return true;
        }
    }

    private static class PullUrlEntity {
        private int position;
        private AliLiveRenderView renderView;
        private String pullUrl;

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
}
