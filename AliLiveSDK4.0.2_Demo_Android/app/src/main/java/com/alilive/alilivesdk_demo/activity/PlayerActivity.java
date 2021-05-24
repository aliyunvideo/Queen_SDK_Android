package com.alilive.alilivesdk_demo.activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.view.AnchorButtonListView;
import com.alilive.alilivesdk_demo.wheel.widget.CommonDialog;
import com.alilive.alilivesdk_demo.wheel.widget.TextFormatUtil;
import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.source.UrlSource;
import com.cicada.player.utils.Logger;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.google.zxing.activity.CaptureActivity;
import com.alilive.alilivesdk_demo.listener.ButtonClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 直播拉流界面
 */
public class PlayerActivity extends AppCompatActivity implements SurfaceHolder.Callback,ButtonClickListener  {
    private static final String TAG = "PlayerActivity";
    private AnchorButtonListView mButtonListView;
    private SurfaceView mSurfaceView;
    private String mPullUrl = "";
    private AliPlayer mAliPlayer;
    private EditText mPullUrlET;//拉流地址
    private Button mPullStartBtn;//开始拉流btn
    private Button mScanBtn;//扫码
    private boolean isStopPullFlag = false;//是否点击过停止播放
    private boolean isPulling = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_player);
        initPlayer();
        initView();

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

    private void initPlayer() {
        Logger.getInstance(this).enableConsoleLog(true);
        Logger.getInstance(this).setLogLevel(Logger.LogLevel.AF_LOG_LEVEL_TRACE);
        mAliPlayer = AliPlayerFactory.createAliPlayer(this.getApplicationContext());
        mAliPlayer.setAutoPlay(true);
        mAliPlayer.setScaleMode(IPlayer.ScaleMode.SCALE_ASPECT_FILL);
        mAliPlayer.setOnErrorListener(new IPlayer.OnErrorListener() {
            @Override
            public void onError(ErrorInfo errorInfo) {
                mSurfaceView.setVisibility(View.GONE);//隐藏播放视图
                stopPull();//停止pull
                Toast toast = Toast.makeText(PlayerActivity.this, errorInfo.getMsg(), Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    private void initView() {
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        mButtonListView = findViewById(R.id.live_buttonlistview);
        List<String> data = new ArrayList<>();
        data.addAll(Constants.getPlayActivityButtonList());
        mButtonListView.setData(data);
        mButtonListView.setClickListener(this);
        mButtonListView.setVisibility(View.GONE);
        mSurfaceView.getHolder().addCallback(this);
        mScanBtn = findViewById(R.id.player_scan_image);
        mScanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCaptureActivityForResult();
            }
        });
        findViewById(R.id.pull_common_btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mPullUrlET = (EditText) findViewById(R.id.pull_common_push_url);
        mPullStartBtn = (Button)findViewById(R.id.pull_common_start_btn);
        mPullStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPull(mPullUrlET.getText().toString());
            }
        });
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

    private void stopPull() {
        stopPlay();
    }

    private void startPull(String pull) {
        if (TextUtils.isEmpty(pull)) {
            if(!isPulling) {
                showTipDialog("提示", "拉流地址不存在...");
            }
            else{
                showTipDialog("提示", "正在拉流中...");
            }
        } else {
            startPlay();
        }
    }

    private void startCaptureActivityForResult() {
        Intent intent = new Intent(PlayerActivity.this, CaptureActivity.class);
        startActivityForResult(intent, CaptureActivity.REQ_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CaptureActivity.REQ_CODE:
                switch (resultCode) {
                    case RESULT_OK:
                        mPullUrl = data.getStringExtra(CaptureActivity.INTENT_EXTRA_KEY_QR_SCAN);
                        startPlay();
                        break;
                    case RESULT_CANCELED:

                        break;
                    default:
                        break;
                }
            default:
                break;
        }
    }

    public void startPlay() {
        isPulling = true;
        mSurfaceView.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(mPullUrl)) {
            Toast.makeText(this, "拉流地址为空", Toast.LENGTH_SHORT).show();
            return;
        }
        UrlSource source = new UrlSource();
        source.setUri(mPullUrl);

        if (mAliPlayer != null) {
            mAliPlayer.setDataSource(source);
            mAliPlayer.prepare();
        }
        mButtonListView.setVisibility(View.VISIBLE);
    }

    /**
     * 停止播放
     */
    public void stopPlay() {
        if (mAliPlayer != null) {
            mAliPlayer.stop();
        }
    }

    @Override
    protected void onDestroy() {
        if (mAliPlayer != null) {
            stopPlay();
            mAliPlayer.setSurface(null);
            mAliPlayer.release();
            mAliPlayer = null;
        }
        super.onDestroy();
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

    @Override
    public void onButtonClick(String message, int position) {
        switch (message) {
            case "结束观看":
                if (!isStopPullFlag) {
                    stopPull();
                    isStopPullFlag = true;
                } else {
                    startPull(mPullUrl);
                    isStopPullFlag = false;
                }
                break;
            default:
                break;
        }
    }
}
