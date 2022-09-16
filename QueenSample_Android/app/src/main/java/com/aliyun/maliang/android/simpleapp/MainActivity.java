package com.aliyun.maliang.android.simpleapp;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.maliang.android.simpleapp.camera.SimpleCameraRenderer;
import com.aliyun.maliang.android.simpleapp.view.MainViewRightPanel;
import com.aliyun.maliang.android.simpleapp.view.MainViewSurfacePanel;
import com.aliyun.maliang.android.simpleapp.utils.QueenCameraHelper;
import com.aliyun.maliang.android.simpleapp.utils.FpsHelper;
import com.aliyun.maliang.android.simpleapp.utils.PermissionUtils;
import com.aliyunsdk.queen.menu.BeautyMenuPanel;
import com.aliyunsdk.queen.param.QueenParamHolder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private boolean isDestroyed = false;

    private MainViewSurfacePanel mMainSurfacePanel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        boolean checkResult = PermissionUtils.checkAndRunPermissionsGroup(this, PermissionUtils.PERMISSION_CAMERA);
        if (checkResult) {
            initMainView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = PermissionUtils.onHandlePermissionRequest(this, requestCode, grantResults);
        if (granted) {
            initMainView();
        }
    }

    private void initMainView() {
        FrameLayout background = new FrameLayout(this);
        background.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mMainSurfacePanel = new MainViewSurfacePanel(this);
        // 创建采用纹理进行特效处理的方式
        SimpleCameraRenderer renderer = new CameraV1TextureRenderer();
        // 创建采用数据buffer进行特效处理的方式
        // SimpleCameraRenderer renderer = new CameraV2TextureRenderer();
        // 创建采用纹理渲染+buffer更新算法进行特效处理的方式
//        SimpleCameraRenderer renderer = new CameraV3TextureAndBufferRenderer();
        View mainSurfaceView = mMainSurfacePanel.createSurfaceView(renderer);

        if (mainSurfaceView == null) return;

        // 添加相机预览界面
        background.addView(mainSurfaceView);

        // 添加右侧操控栏
        MainViewRightPanel cameraRightPanel = new MainViewRightPanel(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        cameraRightPanel.setOnClickListenerProxy(this);
        background.addView(cameraRightPanel, params);

        // 添加底部菜单栏
        BeautyMenuPanel beautyMenuPanel = new BeautyMenuPanel(this);
        final FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        menuParams.gravity = Gravity.BOTTOM;
        background.addView(beautyMenuPanel, menuParams);

        setContentView(background);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isDestroyed) {
            return;
        }

        if (mMainSurfacePanel != null) {
            mMainSurfacePanel.onPause();
        }


        QueenCameraHelper.get().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDestroyed) {
            return;
        }

        if (mMainSurfacePanel != null) {
            mMainSurfacePanel.onResume();
        }

        QueenCameraHelper.get().onResume();
    }

    @Override
    protected void onDestroy() {
        if (mMainSurfacePanel != null) {
            mMainSurfacePanel.onDestroy();
        }
        isDestroyed = true;
        FpsHelper.get().release();
        // 释放美颜特效参数
        QueenParamHolder.relaseQueenParams();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSwitchCamera && mMainSurfacePanel != null) {
            mMainSurfacePanel.switchCamera();
        }
    }
}