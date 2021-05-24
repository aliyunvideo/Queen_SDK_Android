package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.adapter.VideoListAdapter;
import com.alilive.alilivesdk_demo.listener.OnVideoItemClickListener;
import com.alilive.alilivesdk_demo.socket.VideoUserInfo;

import java.util.List;

/**
 * data:2020-08-30
 */
public class VideoListView extends FrameLayout {
    private VideoListAdapter mAdapter;

    public VideoListView(@NonNull Context context) {
        this(context, null);
    }

    public VideoListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public VideoListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }
    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.live_button_list, this, true);
        initRecyclerView(view);
    }
    private void initRecyclerView(View view) {
        RecyclerView mRecyclerView = view.findViewById(R.id.live_button_recycle);
        mAdapter = new VideoListAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(VideoListView.this.getContext());
        mRecyclerView.setLayoutManager(layoutManager);
        layoutManager.setReverseLayout(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
    }
    public void setUserList(List<VideoUserInfo> data){
        mAdapter.setUserList(data);
    }

    public void setOnItemClickListener(OnVideoItemClickListener onItemClickListener) {
        mAdapter.setOnItemClickListener(onItemClickListener);
    }

}
