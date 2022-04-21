package com.aliyun.maliang.android.simpleapp.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.TextView;

public class FpsHelper {

    private long mLastDrawSystemClock = 0L;
    private long mCurrentDrawTimes = 0L;
    private long mLastDrawTimes = 0L;
    private TextView mFpsView = null;

    private static FpsHelper instance = new FpsHelper();
    public static FpsHelper get() { return instance; }

    private FpsHelper() {}

    public void setFpsView(TextView textView) {
        mFpsView = textView;
        startLoop();
    }

    private void startLoop() {
        Message message = mHandler.obtainMessage(1);
        mHandler.sendMessage(message);
    }

    //更新fps显示
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    long curDrawSystemClock = SystemClock.elapsedRealtime();
                    long curDrawTimes = mCurrentDrawTimes;
                    long drawTimes = curDrawTimes - mLastDrawTimes;
                    long drawCostTime = curDrawSystemClock - mLastDrawSystemClock;
                    long fps = drawTimes * 1000 / drawCostTime;
                    mLastDrawTimes = curDrawTimes;
                    mLastDrawSystemClock = curDrawSystemClock;

                    if(mFpsView != null){
                        mFpsView.setText("fps:" + fps);
                    } else {
                        Log.i("queen_sample_fps", "fps: " + fps);
                    }
                    this.sendMessageDelayed(this.obtainMessage(1),480);
                    break;
                default:
                    break;
            }
        }
    };

    public void updateDrawTimes() {
        ++mCurrentDrawTimes;
    }

    public void release() {
        if (mHandler != null) {
            mHandler.removeMessages(1);
        }
    }

}
