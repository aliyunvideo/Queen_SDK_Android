package io.agora.vlive.ui.components;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;
import io.agora.vlive.protocol.model.types.PKConstant;

public class PkLayout extends LinearLayout {
    private static final int TIMER_TICK_PERIOD = 1000;

    private AppCompatTextView mLeftPoint;
    private AppCompatTextView mRightPoint;
    private RelativeLayout mHostVideoContainer;
    private RelativeLayout mLeftFrameLayout;
    private FrameLayout mRightFrameLayout;
    private RelativeLayout mRightVideoContainer;
    private AppCompatImageView mToOtherRoomBtn;
    private AppCompatTextView mRemainsText;
    private AppCompatTextView mOtherHostName;

    private int mResultIconWidth;

    private AppCompatImageView mPkResultImage;

    private long mTimerStopTimestamp;
    private Handler mTimerHandler;
    private CountDownRunnable mCountDownRunnable = new CountDownRunnable();

    private class CountDownRunnable implements Runnable {
        @Override
        public void run() {
            long current = System.currentTimeMillis();
            mRemainsText.setText(timestampToCountdown(mTimerStopTimestamp - current));
            mTimerHandler.postDelayed(mCountDownRunnable, TIMER_TICK_PERIOD);
        }
    }

    public PkLayout(Context context) {
        super(context);
        init();
    }

    public PkLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Resources resources = getResources();
        mResultIconWidth = resources.getDimensionPixelSize(R.dimen.live_pk_result_icon_size);

        LayoutInflater.from(getContext()).inflate(
                R.layout.pk_video_layout, this, true);
        mHostVideoContainer = findViewById(R.id.pk_host_video_layout_container);
        mRightVideoContainer = findViewById(R.id.pk_host_video_layout_right_container);
        mLeftPoint = findViewById(R.id.pk_progress_left_text);
        mRightPoint = findViewById(R.id.pk_progress_right_text);
        mLeftFrameLayout = findViewById(R.id.pk_host_video_layout_left_container);
        mRightFrameLayout = findViewById(R.id.pk_host_video_layout_right);
        mToOtherRoomBtn = findViewById(R.id.pk_video_layout_enter_other_room_btn);
        mRemainsText = findViewById(R.id.pk_host_remaining_time_text);
        mOtherHostName = findViewById(R.id.pk_video_layout_other_host_name);
    }

    public void setHost(boolean isHost) {
        mToOtherRoomBtn.setVisibility(isHost ? View.GONE : View.VISIBLE);
    }

    public void setOnClickGotoPeerChannelListener(View.OnClickListener listener) {
        mToOtherRoomBtn.setOnClickListener(listener);
    }

    public void setPoints(int localPoint, int remotePoint) {
        if (localPoint < 0 || remotePoint < 0) {
            return;
        }

        int localWeight;
        int remoteWeight;
        if (localPoint == 0 && remotePoint == 0) {
            localWeight = 1;
            remoteWeight = 1;
        } else if (localPoint == 0) {
            localWeight = 10;
            remoteWeight = 90;
        } else if (remotePoint == 0) {
            localWeight = 90;
            remoteWeight = 10;
        } else {
            localWeight = localPoint;
            remoteWeight = remotePoint;
        }

        setWeight(mLeftPoint, localWeight);
        setWeight(mRightPoint, remoteWeight);

        mLeftPoint.setText(String.valueOf(localPoint));
        mRightPoint.setText(String.valueOf(remotePoint));
    }

    public void setWeight(AppCompatTextView textView, int weight) {
        LinearLayout.LayoutParams params =
                (LinearLayout.LayoutParams) textView.getLayoutParams();
        params.weight = weight;
        textView.setLayoutParams(params);
    }

    public RelativeLayout getLeftVideoLayout() {
        return mLeftFrameLayout;
    }

    public FrameLayout getRightVideoLayout() {
        return mRightFrameLayout;
    }

    public void setPKHostName(String name) {
        mOtherHostName.setText(name);
    }

    public void startCountDownTimer(long remaining) {
        mTimerStopTimestamp = System.currentTimeMillis() + remaining;
        mTimerHandler = new Handler(getContext().getMainLooper());
        mTimerHandler.post(() -> mRemainsText.setText(timestampToCountdown(remaining)));
        mTimerHandler.postDelayed(this::stopCountDownTimer, remaining);
        mTimerHandler.postDelayed(mCountDownRunnable, TIMER_TICK_PERIOD);
    }

    public void stopCountDownTimer() {
        if (mTimerHandler != null) {
            mTimerHandler.removeCallbacksAndMessages(null);
        }
    }

    private String timestampToCountdown(long remaining) {
        if (remaining <= 0) return "00:00";
        long seconds = remaining / 1000;
        long minute = seconds / 60;
        int remainSecond = (int) seconds % 60;
        String minuteString = minute < 10 ? "0" + minute : "" + minute;
        String secondString = remainSecond < 10 ? "0" + remainSecond : "" + remainSecond;
        return minuteString + ":" + secondString;
    }

    /**
     * Set PK result of current PK session.
     * @param result pk result of current room owner.
     */
    public void setResult(int result) {
        mPkResultImage = new AppCompatImageView(getContext());
        if (result == PKConstant.PK_RESULT_LOSE || result == PKConstant.PK_RESULT_WIN) {
            mPkResultImage.setImageResource(R.drawable.icon_pk_result_win);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    mResultIconWidth, mResultIconWidth);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            RelativeLayout container = result == PKConstant.PK_RESULT_LOSE
                    ? mRightVideoContainer
                    : mLeftFrameLayout;
            container.addView(mPkResultImage, params);
        } else if (result == PKConstant.PK_RESULT_DRAW) {
            mPkResultImage.setImageResource(R.drawable.icon_pk_result_draw);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    mResultIconWidth, mResultIconWidth);
            params.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            mHostVideoContainer.addView(mPkResultImage, params);
        }
    }

    public void removeResult() {
        if (mPkResultImage == null) {
            return;
        }

        ViewGroup parent = (ViewGroup) mPkResultImage.getParent();
        if (parent != null) {
            parent.removeView(mPkResultImage);
            mPkResultImage = null;
        }
    }
}
