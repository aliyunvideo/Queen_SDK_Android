package com.aliyun.maliang.android.simpleapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.aliyun.maliang.android.simpleapp.CameraV1GLSurfaceView.CameraV1GLSurfaceView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraV1GLSurfaceView mGLSurfaceView;
    private int mCameraId;
    private CameraV1 mCamera;
    private static String TAG = "CameraV1GLSurfaceViewActivity";

    private static final int PERMISSION_REQUEST_CODE = 1000;

    private OrientationEventListener mOrientationEventListener;

    private boolean isDestroyed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        boolean checkResult = PermissionUtils.checkPermissionsGroup(this, PermissionUtils.PERMISSION_CAMERA);
        if (!checkResult) {
            PermissionUtils.requestPermissions(this, PermissionUtils.PERMISSION_CAMERA, PERMISSION_REQUEST_CODE);
        } else {
            initGlSurfaceView();
        }
//        android.util.Log.e("TEST_QUEEN", "===license=" + LicenseHelper.getPackageSignature());
    }

    private void initGlSurfaceView() {
        FrameLayout background = new FrameLayout(this);
        background.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

        mGLSurfaceView = new CameraV1GLSurfaceView(this);
        mGLSurfaceView.setLayoutParams(new ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        //设置相机前后
        mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
//        mCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;
        mCamera = new CameraV1(this);
        mGLSurfaceView.init(mCamera, false, this);

        if (!mCamera.openCamera(1280,720, mCameraId)) {
            return;
        }

        background.addView(mGLSurfaceView);

        CameraRightPanel cameraRightPanel = new CameraRightPanel(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.RIGHT;
        cameraRightPanel.setLayoutParams(params);
        cameraRightPanel.setOnClickListenerProxy(this);
        background.addView(cameraRightPanel);

        setContentView(background);

        mOrientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mCamera != null) {
                    if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                        return;
                    }
                    orientation = (orientation + 45) / 90 * 90;

                    mCamera.setDeviceOrientation(orientation, ActivityUtil.getDegrees(MainActivity.this));
                }
            }
        };
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
                initGlSurfaceView();
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
        builder.setMessage(getString(R.string.app_name) + "需要访问 \"摄像头\" 和 \"外部存储器\",否则会影响绝大部分功能使用, 请到 \"应用信息 -> 权限\" 中设置！");
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

    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
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

        if (mOrientationEventListener != null) {
            mOrientationEventListener.disable();
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
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
        if (mOrientationEventListener != null && mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
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

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switchCamera();
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
    }
}