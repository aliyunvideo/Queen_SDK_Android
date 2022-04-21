package com.aliyun.maliang.android.simpleapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.aliyun.maliang.android.simpleapp.utils.FpsHelper;
import com.aliyunsdk.queen.param.QueenRuntime;

public class MainViewRightPanel extends FrameLayout implements View.OnClickListener {

    private OnClickListener mOnClickListenerProxy;

    public MainViewRightPanel(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        View panelLayout = LayoutInflater.from(getContext()).inflate(R.layout.layout_camera_right_panel, this);
        panelLayout.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        View btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnSwitchCamera.setOnClickListener(this);

        View btnSwitchQueen = findViewById(R.id.btnSwitchQueen);
        btnSwitchQueen.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() != MotionEvent.ACTION_UP) {
                    // 开启了重置
                    QueenRuntime.isEnableQueen = false;
                } else {
                    // 释放了重置
                    QueenRuntime.isEnableQueen = true;
                }
                return true;
            }
        });

        FpsHelper.get().setFpsView(getFpsTextView());
    }

    private TextView getFpsTextView() {
        return findViewById(R.id.textviewFps);
    }

    public void setOnClickListenerProxy(OnClickListener mOnClickListenerProxy) {
        this.mOnClickListenerProxy = mOnClickListenerProxy;
    }

    @Override
    public void onClick(View v) {
        if (null != mOnClickListenerProxy) {
            mOnClickListenerProxy.onClick(v);
        }
    }
}
