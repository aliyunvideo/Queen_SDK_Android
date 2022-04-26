package io.agora.vlive.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;

public class RtcStatsView extends RelativeLayout {
    private static final float LINE_EXTRA = 10f;
    private static final float LINE_MULTIPLY = 1f;

    private AppCompatTextView mTextView;
    private AppCompatImageView mClose;
    private String mStatsFormat;
    private int mCloseSize;

    public RtcStatsView(Context context) {
        super(context);
        init();
    }

    public RtcStatsView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mCloseSize = getResources().getDimensionPixelSize(R.dimen.live_stats_close_button_size);

        mTextView = new AppCompatTextView(getContext());
        mTextView.setLineSpacing(LINE_EXTRA, LINE_MULTIPLY);
        addView(mTextView);
        mStatsFormat = getResources().getString(R.string.rtc_stats_format);

        mClose = new AppCompatImageView(getContext());
        RelativeLayout.LayoutParams closeLayoutParams =
                new RelativeLayout.LayoutParams(mCloseSize, mCloseSize);
        closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        closeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        addView(mClose, closeLayoutParams);
        mClose.setScaleType(ImageView.ScaleType.FIT_XY);
        mClose.setImageResource(R.drawable.icon_close_gray);
    }

    public void setLocalStats(float rxRate, float rxLoss, float txRate, float txLoss) {
        String stats = String.format(mStatsFormat, rxRate, rxLoss, txRate, txLoss);
        mTextView.setText(stats);
    }

    public void setCloseListener(View.OnClickListener listener) {
        mClose.setOnClickListener(listener);
    }

}
