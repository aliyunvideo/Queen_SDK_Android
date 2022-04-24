package com.alivc.live.pusher.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private LinearLayout mLivePushLayout;
    private LinearLayout mLivePullCommonPullLayout;
    private LinearLayout mLivePullRtcLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        initView();
        //首次安装按home键置入后台，从桌面图标点击重新启动的问题
        if (!isTaskRoot()) {
            finish();
            return;
        }
        if (!permissionCheck()) {
            if (Build.VERSION.SDK_INT >= 23) {
                ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
            } else {
                showNoPermissionTip(getString(noPermissionTip[mNoPermissionIndex]));
                finish();
            }
        }
    }
    private void initView() {
        mLivePushLayout = (LinearLayout) findViewById(R.id.push_enter_layout);
        mLivePushLayout.setOnClickListener(this);
        mLivePullCommonPullLayout = (LinearLayout) findViewById(R.id.pull_common_enter_layout);
        mLivePullCommonPullLayout.setOnClickListener(this);
        mLivePullRtcLayout = (LinearLayout) findViewById(R.id.pull_enter_layout);
        mLivePullRtcLayout.setOnClickListener(this);
    }

    private int mNoPermissionIndex = 0;
    private final int PERMISSION_REQUEST_CODE = 1;
    private final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET,
    };

    private final int[] noPermissionTip = {
            R.string.no_camera_permission,
            R.string.no_record_bluetooth_permission,
            R.string.no_record_audio_permission,
            R.string.no_read_phone_state_permission,
            R.string.no_write_external_storage_permission,
            R.string.no_read_external_storage_permission,
    };

    private boolean permissionCheck() {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        String permission;
        for (int i = 0; i < permissionManifest.length; i++) {
            permission = permissionManifest[i];
            mNoPermissionIndex = i;
            if (PermissionChecker.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionCheck = PackageManager.PERMISSION_DENIED;
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
    }
    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()) {
            case R.id.push_enter_layout:
                intent = new Intent(MainActivity.this, PushConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.pull_common_enter_layout:
                intent = new Intent(MainActivity.this, VideoRecordConfigActivity.class);
                startActivity(intent);
                break;
            case R.id.pull_enter_layout:
                intent = new Intent(MainActivity.this, PlayerActivity.class);
                startActivity(intent);
                break;
            default:
                break;

        }
    }

    private void showNoPermissionTip(String tip) {
        Toast.makeText(this, tip, Toast.LENGTH_LONG).show();
    }

}
