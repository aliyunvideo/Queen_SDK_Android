package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.adapter.NoticeListAdapter;

import com.alilive.alilivesdk_demo.adapter.OnLineListAdapter;
import com.alilive.alilivesdk_demo.socket.OnLineRoomListBean;
import com.alilive.alilivesdk_demo.socket.UserInfo;

import java.util.List;

/**
 * data:2020/9/27
 */
public class OnLineRoomView  extends FrameLayout {
    TextView mTvTittle;
    private RecyclerView mRecyclerView;
    private OnLineListAdapter mAdapter;
    private OnLineListAdapter.OnItemClickListener onItemClickListener;
    public OnLineRoomView(@NonNull Context context) {
        this(context, null);
    }

    public OnLineRoomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OnLineRoomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_online_room, this, true);
        mTvTittle = view.findViewById(R.id.tv_tittle);
        mTvTittle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                OnLineRoomView.this.setVisibility(GONE);
            }
        });
        initRecyclerView(view);
        setBackgroundResource(R.color.color_background_black_alpha_30);
    }
    private void initRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.rv_apply_mic_notice);
        mAdapter = new OnLineListAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(OnLineRoomView.this.getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

    }
    public void setRoomList(List<OnLineRoomListBean.OnlineRoomBean> mRoomList) {
        mAdapter.setRoomList(mRoomList);
    }
    public void setOnItemClickListener(OnLineListAdapter.OnItemClickListener onItemClickListener){
        mAdapter.setOnItemClickListener(onItemClickListener);
    }

}
