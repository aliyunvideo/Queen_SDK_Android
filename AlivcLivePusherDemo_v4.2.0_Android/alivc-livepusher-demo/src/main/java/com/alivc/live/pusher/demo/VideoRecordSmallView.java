package com.alivc.live.pusher.demo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alivc.live.pusher.AlivcLivePusher;

import java.lang.reflect.Field;
import java.util.List;

public class VideoRecordSmallView extends LinearLayout {

    public static int viewWidth;

    public static int viewHeight;

    private static int statusBarHeight;

    private WindowManager windowManager;

    private WindowManager.LayoutParams mParams;

    private AlivcLivePusher mAlivcLivePusher = null;

    private float xInScreen;

    private float yInScreen;

    private float xDownInScreen;

    private float yDownInScreen;

    private float xInView;

    private float yInView;

    private LinearLayout mRecording;
    private LinearLayout mOpera;
    private TextView mBack;
    private TextView mPrivacy;
    private TextView mCamera;
    private TextView mMic;
    private TextView mMix;
    private ImageView mGo;

    private boolean mPrivacyOn = false;
    private boolean mCameraOn = false;
    private boolean mMuteOn = false;
    private boolean mMixOn = false;

    private Context mContext;
    private int mCurrentTaskId;
    private View mRootView = null;
    private VideoRecordViewManager.CameraOn mCameraOnListener = null;

    private boolean mCameraOpenByMix = false;

    public VideoRecordSmallView(Context context, int currentTaskId) {
        super(context);
        this.mContext = context;
        this.mCurrentTaskId = currentTaskId;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mRootView = LayoutInflater.from(context).inflate(R.layout.record_view_small, this);
        View view = findViewById(R.id.record_window_layout);
        mRecording = (LinearLayout) view.findViewById(R.id.recording_linear);
        mOpera = (LinearLayout) view.findViewById(R.id.opera_linear);
        mBack = (TextView) view.findViewById(R.id.back);
        mPrivacy = (TextView) view.findViewById(R.id.privacy);
        mCamera = (TextView) view.findViewById(R.id.camera);
        mMic = (TextView) view.findViewById(R.id.mic);
        mMix = (TextView) view.findViewById(R.id.mix);
        mGo = (ImageView) view.findViewById(R.id.go);
        mRecording.setOnClickListener(mOnClickListener);
        mOpera.setOnClickListener(mOnClickListener);
        mBack.setOnClickListener(mOnClickListener);
        mPrivacy.setOnClickListener(mOnClickListener);
        mCamera.setOnClickListener(mOnClickListener);
        mMic.setOnClickListener(mOnClickListener);
        mGo.setOnClickListener(mOnClickListener);
        mMix.setOnClickListener(mOnClickListener);
        viewWidth = view.getLayoutParams().width;
        viewHeight = view.getLayoutParams().height;
    }

    public void setVisible(boolean visible)
    {
        if(visible) {
            mRootView.setVisibility(VISIBLE);
        } else {
            mRootView.setVisibility(INVISIBLE);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
                }
                break;
            default:
                break;
        }
        return true;
    }

    public void setParams(AlivcLivePusher pusher, WindowManager.LayoutParams params) {
        mAlivcLivePusher = pusher;
        mParams = params;
    }

    public void setCameraOnListern(VideoRecordViewManager.CameraOn listern)
    {
        mCameraOnListener = listern;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        windowManager.updateViewLayout(this, mParams);
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    private boolean IsForeground(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
        if (tasks != null && !tasks.isEmpty()) {
            ComponentName topActivity = tasks.get(0).topActivity;
            if (topActivity.getPackageName().equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    private OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View view) {
            int id = view.getId();
            if(id == R.id.recording_linear) {
                mRecording.setVisibility(GONE);
                mOpera.setVisibility(VISIBLE);
            } else if(id == R.id.back) {
                if(mContext != null) {
                    if(!IsForeground(mContext) && mCurrentTaskId != 0) {
                        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                        if (am != null) {
                            am.moveTaskToFront(mCurrentTaskId, ActivityManager.MOVE_TASK_WITH_HOME);
                        }
                    } else {
                        Intent intent = new Intent(mContext,VideoRecordConfigActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent);
                    }
                }
            } else if(id == R.id.privacy) {
                if(mAlivcLivePusher != null && mPrivacyOn)
                {
                    try {
                        mAlivcLivePusher.resumeScreenCapture();
                    } catch (Exception e)
                    {

                    }
                    mPrivacy.setText(R.string.privacy_on);
                    mPrivacyOn = false;
                } else if(mAlivcLivePusher != null && !mPrivacyOn)
                {
                    try {
                        mAlivcLivePusher.pauseScreenCapture();
                    } catch (Exception e)
                    {

                    }
                    mPrivacy.setText(R.string.privacy_off);
                    mPrivacyOn = true;
                }
            } else if(id == R.id.camera) {
                if(mAlivcLivePusher != null && !mCameraOn && mCameraOnListener != null)
                {
                    mCameraOnListener.onCameraOn(true);
                    mCameraOn = true;
                    mCamera.setText(R.string.camera_off);
                } else if(mAlivcLivePusher != null && mCameraOn  && mCameraOnListener != null)
                {
                    try {
                        mAlivcLivePusher.stopCameraMix();
                    } catch (Exception e) {
                    }
                    mMixOn = false;
                    mMix.setText(R.string.mix_on);
                    try {
                        mAlivcLivePusher.stopCamera();
                    } catch (Exception e) {
                    }
                    mCameraOnListener.onCameraOn(false);
                    mCameraOn = false;
                    mCamera.setText(R.string.camera_on);
                }

            } else if(id == R.id.mix) {
                if(mAlivcLivePusher != null && !mMixOn)
                {
                    mCameraOpenByMix = true;
                    try {
                        mAlivcLivePusher.startCamera(null);
                    } catch (Exception e) {
                        mCameraOpenByMix = false;
                    }
                    if(mCameraOpenByMix) {
                        mCameraOn = true;
                        mCamera.setText(R.string.camera_off);
                    }
                    //set orientation
                    /*int orientation = getContext().getResources().getConfiguration().orientation;
                    if(orientation == 0) {
                        mAlivcLivePusher.setScreenOrientation(0);
                    } else if(orientation == 2) {
                        mAlivcLivePusher.setScreenOrientation(90);
                    } else if(orientation == 1) {
                        mAlivcLivePusher.setScreenOrientation(270);
                    }*/
                    mAlivcLivePusher.startCameraMix(0.6f,0.08f, 0.3f,0.3f);
                    mMixOn = true;
                    mMix.setText(R.string.mix_off);
                } else if(mAlivcLivePusher != null && mMixOn)
                {
                    mAlivcLivePusher.stopCameraMix();
                    if(mCameraOpenByMix) {
                        mAlivcLivePusher.stopCamera();
                        mCameraOn = false;
                        mCamera.setText(R.string.camera_on);
                    }
                    mMixOn = false;
                    mMix.setText(R.string.mix_on);
                }

            } else if(id == R.id.mic) {
                if(mAlivcLivePusher != null)
                {
                    mMuteOn = !mMuteOn;
                    try {
                        mAlivcLivePusher.setMute(mMuteOn);
                    } catch (Exception e) {
                        
                    }
                    if(mMuteOn)
                    {
                        mMic.setText(R.string.mic_on);
                    } else {
                        mMic.setText(R.string.mic_off);
                    }
                }

            } else if(id == R.id.go) {
                mOpera.setVisibility(GONE);
                mRecording.setVisibility(VISIBLE);
            }
        }
    };
}
