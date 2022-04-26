package io.agora.vlive.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.IOException;

import io.agora.vlive.R;

public class LiveHostNameLayout extends RelativeLayout {
    private static final int IMAGE_VIEW_ID = 1 << 4;

    private int mMaxWidth;
    private int mHeight;
    private AppCompatImageView mIconImageView;
    private AppCompatTextView mNameTextView;

    public LiveHostNameLayout(Context context) {
        super(context);
    }

    public LiveHostNameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LiveHostNameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(boolean lightMode) {
        mMaxWidth = getResources().getDimensionPixelSize(R.dimen.live_name_pad_max_width);
        mHeight = getResources().getDimensionPixelSize(R.dimen.live_name_pad_height);

        if (lightMode) {
            setBackgroundResource(R.drawable.round_scalable_gray_transparent_bg);
        } else {
            setBackgroundResource(R.drawable.round_scalable_gray_bg);
        }

        RelativeLayout.LayoutParams params;

        mIconImageView = new AppCompatImageView(getContext());
        mIconImageView.setId(IMAGE_VIEW_ID);
        addView(mIconImageView);
        int iconPadding = getResources().getDimensionPixelSize(R.dimen.live_name_pad_icon_padding);
        params = (RelativeLayout.LayoutParams) mIconImageView.getLayoutParams();
        int iconSize = mHeight - iconPadding * 2;
        params.width = iconSize;
        params.height = iconSize;
        params.leftMargin = iconPadding;
        params.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        mIconImageView.setLayoutParams(params);

        mNameTextView = new AppCompatTextView(getContext());
        addView(mNameTextView);
        params = (RelativeLayout.LayoutParams) mNameTextView.getLayoutParams();
        params.addRule(RelativeLayout.END_OF, IMAGE_VIEW_ID);
        params.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
        params.leftMargin = mHeight / 5;
        params.rightMargin = params.leftMargin;
        params.width = LayoutParams.MATCH_PARENT;
        params.height = LayoutParams.MATCH_PARENT;
        mNameTextView.setLayoutParams(params);

        int textSize = getResources().getDimensionPixelSize(R.dimen.text_size_small);
        mNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        if (lightMode) {
            mNameTextView.setTextColor(Color.BLACK);
        } else {
            mNameTextView.setTextColor(Color.WHITE);
        }

        mNameTextView.setSingleLine(true);
        mNameTextView.setFocusable(true);
        mNameTextView.setFocusableInTouchMode(true);
        mNameTextView.setSelected(true);
        mNameTextView.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        mNameTextView.setMarqueeRepeatLimit(-1);
        mNameTextView.setTextAlignment(TextView.TEXT_ALIGNMENT_GRAVITY);
        mNameTextView.setGravity(Gravity.CENTER);
    }

    public void init() {
        init(false);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(mMaxWidth, mHeight);
        int widthSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
        int heightSpec = MeasureSpec.makeMeasureSpec(mHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthSpec, heightSpec);
    }

    public void setName(String name) {
        mNameTextView.setText(name);
    }

    public void setIcon(Drawable drawable) {
        mIconImageView.setImageDrawable(drawable);
    }

    /**
     * For development only, test fake user icon
     * @param name
     */
    public void setIconResource(String name) {
        RoundedBitmapDrawable drawable = null;
        try {
            drawable = RoundedBitmapDrawableFactory.create(getResources(),
                    getResources().getAssets().open(name));
            drawable.setCircular(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mIconImageView.setImageDrawable(drawable);
    }
}
