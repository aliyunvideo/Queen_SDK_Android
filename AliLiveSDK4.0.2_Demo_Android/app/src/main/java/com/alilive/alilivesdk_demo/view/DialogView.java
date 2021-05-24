package com.alilive.alilivesdk_demo.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;

public class DialogView extends Dialog {

    private TextView textConfirm;
    private TextView textCancel;
    private TextView textContent;
    private String content;
    private String confirm;
    private String cancel;
    private OnCancelClickListener cancelClickListener;
    private OnConfirmClickListener confirmClickListener;

    public DialogView(@NonNull Context context) {
        this(context, R.style.DialogView);
    }

    public DialogView(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public interface OnCancelClickListener {
        void onClick();
    }

    public interface OnConfirmClickListener {
        void onClick();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_dialog);
        textConfirm = findViewById(R.id.text_confirm);
        textCancel = findViewById(R.id.text_cancel);
        textContent = findViewById(R.id.text_content);

        initData();
        initEvent();
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        if (content != null) {
            textContent.setText(content);
        }
        if (confirm != null) {
            textConfirm.setText(confirm);
        }
        if (cancel != null) {
            textCancel.setText(cancel);
        }
    }

    private void initEvent() {
        textConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                if (confirmClickListener != null) {
                    confirmClickListener.onClick();
                }
            }
        });
        textCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
                if (cancelClickListener != null) {
                    cancelClickListener.onClick();
                }
            }
        });
    }

    public void setContent(String message) {
        content = message;
    }

    public void setCancelClickListener(OnCancelClickListener listener) {
        setCancelOnClickListener(null, listener);
    }

    public void setCancelOnClickListener(String str, OnCancelClickListener listener) {
        if (!TextUtils.isEmpty(str)) {
            cancel = str;
        }
        this.cancelClickListener = listener;
    }

    public void setConfirmOnClickListener(OnConfirmClickListener listener) {
        setConfirmOnClickListener(null, listener);
    }

    public void setConfirmOnClickListener(String str, OnConfirmClickListener listener) {
        if (!TextUtils.isEmpty(str)) {
            confirm = str;
        }
        this.confirmClickListener = listener;
    }

    private void dismissDialog() {
        if (this.isShowing()) {
            this.dismiss();
        }
    }

}
