package com.aliyun.maliang.android.simpleapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.aliyun.maliang.android.simpleapp.queen.QueenRuntime;

public class CameraRightPanel extends FrameLayout implements View.OnClickListener {

    private OnClickListener mOnClickListenerProxy;

    public CameraRightPanel(Context context) {
        super(context);

        initView();

//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.WRAP_CONTENT,
//                ViewGroup.LayoutParams.MATCH_PARENT
//        );
//        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//        setLayoutParams(params);
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
