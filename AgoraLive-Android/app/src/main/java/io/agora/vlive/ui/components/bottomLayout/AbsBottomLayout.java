package io.agora.vlive.ui.components.bottomLayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;

import io.agora.vlive.R;

public abstract class AbsBottomLayout extends RelativeLayout implements View.OnClickListener {
    public static final int ROLE_AUDIENCE = 1;
    public static final int ROLE_HOST = 2;
    public static final int ROLE_OWNER = 3;

    protected AppCompatImageView closeBtn;
    protected AppCompatImageView moreBtn;
    protected AppCompatImageView fun1Btn;
    protected AppCompatImageView fun2Btn;
    protected AppCompatTextView inputText;
    protected int role = ROLE_AUDIENCE;

    private BottomButtonListener mListener;

    public interface BottomButtonListener {
        void onLiveBottomLayoutShowMessageEditor();
        void onFun1ButtonClicked(int role);
        void onFun2ButtonClicked(int role);
        void onMoreButtonClicked();
        void onCloseButtonClicked();
    }

    public AbsBottomLayout(Context context) {
        super(context);
        init(getLayoutResource());
    }

    public AbsBottomLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(getLayoutResource());
    }

    public abstract int getLayoutResource();

    public abstract void setRole(int role);

    public void init(int layoutRes) {
        LayoutInflater.from(getContext()).inflate(layoutRes, this, true);
        inputText = findViewById(R.id.live_bottom_message_input_hint);
        fun1Btn = findViewById(R.id.live_bottom_btn_fun1);
        fun2Btn = findViewById(R.id.live_bottom_btn_fun2);
        moreBtn = findViewById(R.id.live_bottom_btn_more);
        closeBtn = findViewById(R.id.live_bottom_btn_close);

        if (fun1Btn == null || fun2Btn == null ||
                inputText == null || moreBtn == null || closeBtn == null) {
            throw new RuntimeException("Layout does not have necessary view elements!");
        }

        inputText.setOnClickListener(this);
        fun1Btn.setOnClickListener(this);
        fun2Btn.setOnClickListener(this);
        moreBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (mListener != null) {
            switch (view.getId()) {
                case R.id.live_bottom_message_input_hint:
                    mListener.onLiveBottomLayoutShowMessageEditor();
                    break;
                case R.id.live_bottom_btn_fun1:
                    mListener.onFun1ButtonClicked(role);
                    break;
                case R.id.live_bottom_btn_fun2:
                    mListener.onFun2ButtonClicked(role);
                    break;
                case R.id.live_bottom_btn_more:
                    mListener.onMoreButtonClicked();
                    break;
                case R.id.live_bottom_btn_close:
                    mListener.onCloseButtonClicked();
                    break;
            }
        }
    }

    public void setBottomLayoutListener(BottomButtonListener listener) {
        mListener = listener;
    }
}
