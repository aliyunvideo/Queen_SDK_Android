package com.aliyun.maliang.android.simpleapp;

import android.content.Intent;
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
import com.aliyunsdk.queen.menu.QueenBeautyMenu;
import com.aliyunsdk.queen.menu.QueenMenuPanel;
import com.aliyunsdk.queen.param.QueenParamHolder;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final int REQUEST_CODE_SELECT_IMAGE = 1000;

    private boolean isDestroyed = false;

    private MainViewSurfacePanel mMainSurfacePanel;

//    private BeautyImagePanel mMainImagePanel;
    private BeautyImageTextureV2Panel mMainImagePanel;

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
        setContentView(background);

        if (AppRuntime.IMAGE_MODE) {
            // 图片模式
//            mMainImagePanel = new BeautyImagePanel(this);
            mMainImagePanel = new BeautyImageTextureV2Panel(this);
            View view = mMainImagePanel.onCreateImagePanel();
            background.addView(view);
        } else {
            // 视频预览模式
            mMainSurfacePanel = new MainViewSurfacePanel(this);
            // 创建采用纹理进行特效处理的方式
        SimpleCameraRenderer renderer = new CameraV1TextureRenderer();
            // 创建采用数据buffer进行特效处理的方式
//         SimpleCameraRenderer renderer = new CameraV2BufferRenderer();
            // 创建采用纹理渲染+buffer更新算法进行特效处理的方式
//        SimpleCameraRenderer renderer = new CameraV3TextureAndBufferRenderer();
//        SimpleCameraRenderer renderer = new CameraV4TextureRenderer();
//        SimpleCameraRenderer renderer = new CameraV5TextureAndBufferRenderer();
//            SimpleCameraRenderer renderer = new CameraV6AIOOesTextureRenderer();
            View mainSurfaceView = mMainSurfacePanel.createSurfaceView(renderer);

            background.addView(mainSurfaceView);
        }

        // 添加右侧操控栏
        MainViewRightPanel cameraRightPanel = new MainViewRightPanel(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        cameraRightPanel.setOnClickListenerProxy(this);
        background.addView(cameraRightPanel, params);

        // 添加底部通用菜单
        QueenMenuPanel menuPanel = initMenuView(background);
        // 增加菜单组件参数变化监听
        menuPanel.setParamChangeListener(mMainImagePanel);
    }

    private QueenMenuPanel initMenuView(ViewGroup parentView) {
        // 注意：菜单面板，如果采用资源动态下载的方式，而非内置资源，则需要自行设置资源下载路径地址。
        // 不要使用菜单组件自带的默认下载地址，因为默认地址，仅供阿里云Demo进行功能展示使用，不稳定不可靠，存在随时下线风险，请务必请自行设置资源下载地址。
        // 资源包，请见项目根目录Resource下；务必在菜单组件初始化前设置，设置方式如下：
        // QueenMaterial.getInstance().setMaterialUrl(QueenMaterial.MaterialType.STICKER, "https://diy.server/sticker.zip");

        // 老式用法
        // BeautyMenuPanel menuPanel = new BeautyMenuPanel(this);

        // 新式用法
        QueenMenuPanel menuPanel = QueenBeautyMenu.getPanel(this);
        final FrameLayout.LayoutParams menuParams = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        menuParams.gravity = Gravity.BOTTOM;
        parentView.addView(menuPanel, menuParams);
        // 隐藏多余不可用的功能选项
        menuPanel.onHideValidFeatures();
        // 隐藏copyright显示
        menuPanel.onHideCopyright();
        return menuPanel;
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

        if (mMainImagePanel != null) {
            mMainImagePanel.onPause();
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

        if (mMainImagePanel != null) {
            mMainImagePanel.onResume();
        }

        QueenCameraHelper.get().onResume();
    }

    @Override
    protected void onDestroy() {
        if (mMainSurfacePanel != null) {
            mMainSurfacePanel.onDestroy();
        }
        if (mMainImagePanel != null) {
            mMainImagePanel.onDestroy();
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
        } else if (v.getId() == R.id.btnSwitchQueen && mMainImagePanel != null) {
            mMainImagePanel.onParamChange();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE) {
            if (mMainImagePanel != null) {
                mMainImagePanel.onUpdateNewData(data);
            }
        }
    }
}