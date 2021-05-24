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
import com.alilive.alilivesdk_demo.listener.OnItemClickListener;
import java.util.List;

/**
 * data:2020-08-30
 */
public class ApplyMicNoticeView extends FrameLayout {
    private RecyclerView mRecyclerView;
    private NoticeListAdapter mAdapter;
    private TextView mPersonNumberTv;
    public ApplyMicNoticeView(@NonNull Context context) {
        this(context, null);
    }

    public ApplyMicNoticeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ApplyMicNoticeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.view_apply_mic_notice, this, true);
        initRecyclerView(view);
    }

    private void initRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.rv_apply_mic_notice);
        mPersonNumberTv = view.findViewById(R.id.tv_person_number);
        mAdapter = new NoticeListAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ApplyMicNoticeView.this.getContext()));
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mPersonNumberTv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setVisibility(View.GONE);
            }
        });
    }
    public void setUserList(List<NoticeListAdapter.NoticeItemInfo> mUserList) {
        mAdapter.setUserList(mUserList);
        mPersonNumberTv.setText(String.format("%d人申请连麦",mUserList.size()));
        Log.e("TAG", "连麦申请"+mUserList.size());
    }
    public void setOnItemClickListener(
        OnItemClickListener onItemClickListener) {
        mAdapter.setOnItemClickListener(onItemClickListener);
    }

    public void notifyChanged(){
        mAdapter.notifyDataSetChanged();
    }
}
