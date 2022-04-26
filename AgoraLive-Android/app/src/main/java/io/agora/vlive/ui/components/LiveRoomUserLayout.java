package io.agora.vlive.ui.components;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.util.List;
import java.util.Locale;

import io.agora.vlive.R;
import io.agora.vlive.protocol.model.response.EnterRoomResponse;
import io.agora.vlive.utils.UserUtil;

public class LiveRoomUserLayout extends RelativeLayout {
    private static final int MAX_ICON_COUNT = 4;

    public interface UserLayoutListener {
        void onUserLayoutShowUserList(View view);
    }

    private int mHeight;
    private int mIconSize;
    private int mIconMargin;
    private AppCompatTextView mCountText;
    private RelativeLayout mIconLayout;
    private View mNotification;

    private UserLayoutListener mListener;

    public LiveRoomUserLayout(Context context) {
        super(context);
    }

    public LiveRoomUserLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void init(boolean lightMode) {
        mHeight = getResources().getDimensionPixelSize(R.dimen.live_name_pad_height);
        mIconSize = getResources().getDimensionPixelSize(R.dimen.live_participant_layout_height);
        mIconMargin = getResources().getDimensionPixelSize(R.dimen.live_participant_margin_end);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View layout = inflater.inflate(lightMode ?
                R.layout.live_participant_layout_light :
                R.layout.live_participant_layout, this, true);
        mIconLayout = layout.findViewById(R.id.icon_layout);
        mCountText = layout.findViewById(R.id.live_participant_count_text);

        layout.findViewById(R.id.live_participant_total_layout)
            .setOnClickListener(view -> {
                if (mListener != null) mListener.onUserLayoutShowUserList(view);
            });

        mNotification = findViewById(R.id.notification_point);
    }

    public void init() {
        init(false);
    }

    public void setUserLayoutListener(UserLayoutListener listener) {
        mListener = listener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(width, mHeight);
        int heightSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, heightSpec);
    }

    public void reset(int total, List<EnterRoomResponse.RankInfo> rankUsers) {
        String value = countToString(total);
        mCountText.setText(value);
        setUserIcons(rankUsers);
    }

    public void reset(List<EnterRoomResponse.RankInfo> rankUsers) {
        setUserIcons(rankUsers);
    }

    public void reset(int total) {
        String value = countToString(total);
        mCountText.setText(value);
    }

    private String countToString(int number) {
        if (number <  1e3f) {
            return String.valueOf(number);
        } else if (number < 1e6f) {
            int quotient = (int) (number / 1e3f);
            return String.format(Locale.getDefault(), "%dK", quotient);
        } else if (number < 1e9f) {
            int quotient = (int) (number / 1e6f);
            return String.format(Locale.getDefault(), "%dM", quotient);
        } else {
            int quotient = (int) (number / 1e9f);
            return String.format(Locale.getDefault(), "%dB", quotient);
        }
    }

    private void setUserIcons(List<EnterRoomResponse.RankInfo> rankUsers) {
        if (mIconLayout.getChildCount() > 0) {
            mIconLayout.removeAllViews();
        }

        if (rankUsers == null) return;

        int id = 0;
        for (int i = 0; i < rankUsers.size(); i++) {
            if (i >= MAX_ICON_COUNT) break;
            EnterRoomResponse.RankInfo info = rankUsers.get(i);
            setIconResource(info.userId, id++);
        }
    }

    private void setIconResource(String userId, int referenceId) {
        int resId = UserUtil.getUserProfileIcon(userId);
        RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(),
                BitmapFactory.decodeResource(getResources(), resId));
        drawable.setCircular(true);

        RelativeLayout.LayoutParams params = new
                RelativeLayout.LayoutParams(mIconSize, mIconSize);
        params.rightMargin = mIconMargin;
        if (referenceId > 0) {
            params.addRule(RelativeLayout.LEFT_OF, referenceId);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        }

        AppCompatImageView imageView = new AppCompatImageView(getContext());
        imageView.setId(referenceId + 1);
        imageView.setImageDrawable(drawable);
        mIconLayout.addView(imageView, params);
    }

    public void showNotification(boolean show) {
        if (mNotification != null) {
            int visibility = show ? VISIBLE : GONE;
            mNotification.setVisibility(visibility);
        }
    }

    public boolean notificationShown() {
        return mNotification != null &&
                mNotification.getVisibility() == VISIBLE;
    }
}
