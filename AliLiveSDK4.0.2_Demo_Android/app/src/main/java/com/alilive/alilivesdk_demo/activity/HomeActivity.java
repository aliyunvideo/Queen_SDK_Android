package com.alilive.alilivesdk_demo.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.utils.CopyFileUtil;
import com.alilive.alilivesdk_demo.utils.PermissionUtils;
import com.alilive.alilivesdk_demo.utils.ThreadUtils;
import com.alivc.live.AliLiveEngine;
import java.io.File;

/**
 * 首页
 */
public class HomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int PERMISSION_REQUEST_CODE = 1001;
    String[] permission = {
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public static final String EXTRA_ROOM_IP = "roomIp";
    public static final String EXTRA_ROOM_ID = "roomId";
    public static final String EXTRA_USER_ID = "userId";
    public static final String EXTRA_USER_NAME = "userName";
    public static final String EXTRA_ALLOW_MIC = "allowMic";

    private LinearLayout mLiveRoomEnterLayout;
    private LinearLayout mLivePushLayout;
    private LinearLayout mLivePullCommonPullLayout;
    private LinearLayout mLivePullRtcLayout;
    private TextView mVersionTV;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!isTaskRoot()) {
            finish();
            return;
        }
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.live_activity_main);
        initView();
        initData();
        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, permission);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, permission, PERMISSION_REQUEST_CODE);
        }

        ThreadUtils.runOnSubThread(new Runnable() {
            @Override
            public void run() {
                String bgmFile = CopyFileUtil.getBGMFile(HomeActivity.this);
                File file = new File(bgmFile);
                if(file.exists() && file.isDirectory()){
                    File[] files = file.listFiles();
                    for (File childFile : files) {
                        childFile.delete();
                    }
                }
                CopyFileUtil.copyBGMFile(HomeActivity.this);
            }
        });
    }
    private void initView() {
        mLiveRoomEnterLayout = (LinearLayout) findViewById(R.id.room_enter_layout);
        mLiveRoomEnterLayout.setOnClickListener(this);
        mLivePushLayout = (LinearLayout) findViewById(R.id.push_enter_layout);
        mLivePushLayout.setOnClickListener(this);
        mLivePullCommonPullLayout = (LinearLayout) findViewById(R.id.pull_common_enter_layout);
        mLivePullCommonPullLayout.setOnClickListener(this);
        mLivePullRtcLayout = (LinearLayout) findViewById(R.id.pull_rtc_enter_layout);
        mLivePullRtcLayout.setOnClickListener(this);
        mVersionTV =  (TextView) findViewById(R.id.version_desc_tv);
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mVersionTV.setText("V"+AliLiveEngine.getSdkVersion());//版本号
    }

    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.room_enter_layout:
                intent = new Intent(HomeActivity.this, RoomEnterActivity.class);
                startActivity(intent);
                break;
            case R.id.push_enter_layout:
                intent = new Intent(HomeActivity.this, PushActivity.class);
                startActivity(intent);
                break;
            case R.id.pull_rtc_enter_layout:
                intent = new Intent(HomeActivity.this, PullTestActivity.class);
                startActivity(intent);
                break;
            case R.id.pull_common_enter_layout:
                intent = new Intent(HomeActivity.this, PlayerActivity.class);
                startActivity(intent);
                break;
            default:
                break;
    
        }

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean isAllGranted = true;

            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (isAllGranted) {
                // 如果所有的权限都授予了
            } else {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                showPermissionDialog();
            }
        }
    }

    

    //系统授权设置的弹框
    AlertDialog openAppDetDialog = null;

    private void showPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.app_name)
            + "需要访问 \"外部存储器读写权限\",否则会影响视频下载的功能使用, 请到 \"应用信息 -> 权限\" 中设置！");
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("暂不设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //finish();
            }
        });
        if (null == openAppDetDialog) {
            openAppDetDialog = builder.create();
        }
        if (null != openAppDetDialog && !openAppDetDialog.isShowing()) {
            openAppDetDialog.show();
        }
    }

}
