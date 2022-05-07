package com.aliyun.maliang.android.simpleapp;

import android.hardware.Camera;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.maliang.android.simpleapp.camera.CameraV1GLSurfaceView;
import com.aliyun.maliang.android.simpleapp.camera.CameraV1;
import com.aliyun.queen.QueenCameraHelper;
import com.aliyun.maliang.android.simpleapp.utils.FpsHelper;
import com.aliyun.maliang.android.simpleapp.utils.PermissionUtils;
import com.aliyunsdk.queen.menu.BeautyMenuPanel;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraV1GLSurfaceView mGLSurfaceView;
    private int mCameraId;
    private CameraV1 mCamera;
    private boolean isDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        boolean checkResult = PermissionUtils.checkAndRunPermissionsGroup(this, PermissionUtils.PERMISSION_CAMERA);
        if (checkResult) {
            initGlSurfaceView();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        boolean granted = PermissionUtils.onHandlePermissionRequest(this, requestCode, grantResults);
        if (granted) {
            initGlSurfaceView();
        }
    }

    private void initGlSurfaceView() {
        FrameLayout background = new FrameLayout(this);
        background.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mGLSurfaceView = new CameraV1GLSurfaceView(this);
        mGLSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        //设置相机前后
        QueenCameraHelper.get().initOrientation(this);
        mCameraId = QueenCameraHelper.get().getCameraId();
        mCamera = new CameraV1(this);
        mGLSurfaceView.init(mCamera, this);

        if (!mCamera.openCamera(1280,720, mCameraId)) {
            return;
        }

        // 添加相机预览界面
        background.addView(mGLSurfaceView);

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

        if (mGLSurfaceView != null) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (mCamera != null) {
                        mCamera.stopPreview();
                        mCamera.releaseCamera();
                        mCamera = null;
                    }

                    mGLSurfaceView.releaseGLResource();
                    mGLSurfaceView.post(new Runnable() {
                        @Override
                        public void run() {
                            mGLSurfaceView.onPause();
                        }
                    });
                }
            });
        }

        QueenCameraHelper.get().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isDestroyed) {
            return;
        }

        if (mCamera != null) {
            try {
                mCamera.startPreview();
            } catch (RuntimeException exception) {
                mCamera.reOpenPreview(1280,720, mCameraId);
                mGLSurfaceView.reBindCamera(mCamera);
                mCamera.startPreview();
            }

        }
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onResume();
        }
        QueenCameraHelper.get().onResume();
    }

    @Override
    protected void onDestroy() {
        mGLSurfaceView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (mCamera != null) {
                    mCamera.stopPreview();
                    mCamera.releaseCamera();
                    mCamera = null;
                }

                mGLSurfaceView.release();
                mGLSurfaceView.post(new Runnable() {
                    @Override
                    public void run() {
                        mGLSurfaceView.onPause();
                        mGLSurfaceView = null;
                    }
                });
            }
        });
        isDestroyed = true;
        FpsHelper.get().release();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btnSwitchCamera) {
            switchCamera();
        }
    }

    private void switchCamera() {
        if (mCameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        } else {
            mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
        }

        mCamera.stopPreview();
        mCamera.reOpenPreview(1280,720, mCameraId);
        mGLSurfaceView.reBindCamera(mCamera);
        mCamera.startPreview();
        QueenCameraHelper.get().setCameraId(mCameraId);
    }
}