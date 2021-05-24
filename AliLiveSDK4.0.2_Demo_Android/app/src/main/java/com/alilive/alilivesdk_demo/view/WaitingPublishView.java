package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.alilive.alilivesdk_demo.R;

/**
 * 等待连接
 *
 * @author kaijia.ljy
 */
public class WaitingPublishView extends FrameLayout {

    private OnClickListener onClickListener;

    public WaitingPublishView(@NonNull Context context) {
        this(context, null);
    }

    public WaitingPublishView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaitingPublishView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_waiting_publish, this, true);
        view.findViewById(R.id.btn_waiting_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(v.getContext());
            }
        });
    }

    private void showDialog(Context context) {
        DialogView dialogView = new DialogView(context);
        dialogView.setContent("正在申请连麦，你确定取消吗？");
        dialogView.setConfirmOnClickListener(new DialogView.OnConfirmClickListener() {
            @Override
            public void onClick() {
                if (onClickListener != null) {
                    onClickListener.onCancelClick();
                }
            }
        });
        dialogView.show();
    }

    public void setClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onCancelClick();
    }

}
