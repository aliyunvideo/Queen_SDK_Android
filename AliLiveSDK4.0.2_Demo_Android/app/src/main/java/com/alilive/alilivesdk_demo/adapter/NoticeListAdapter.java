package com.alilive.alilivesdk_demo.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.OnItemClickListener;
import com.alilive.alilivesdk_demo.socket.UserInfo;
import com.alilive.alilivesdk_demo.view.MicNoticeStatusView;

import java.util.List;

/**
 * data:2020-08-30
 */
public class NoticeListAdapter extends RecyclerView.Adapter {

    private List<NoticeItemInfo> mUserList;


    private OnItemClickListener onItemClickListener;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_notice, parent,
            false);
        NoticeViewHolder viewHolder = new NoticeViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final NoticeItemInfo noticeItemInfo = mUserList.get(position);
        NoticeViewHolder viewHolder = (NoticeViewHolder)holder;
        viewHolder.mTvUserName.setText(noticeItemInfo.userInfo.getUserName());
        if (noticeItemInfo.micNoticeStatus == MicNoticeStatusView.STATUS_PREPARE){
            viewHolder.micNoticeStatusView.setPrepare();
        }else if (noticeItemInfo.micNoticeStatus == MicNoticeStatusView.STATUS_CONNECTING){
            viewHolder.micNoticeStatusView.setConnecting();
        }else if (noticeItemInfo.micNoticeStatus == MicNoticeStatusView.STATUS_CONNECTED){
            viewHolder.micNoticeStatusView.setConnected();
        }else if (noticeItemInfo.micNoticeStatus == MicNoticeStatusView.STATUS_REFUSED){
            viewHolder.micNoticeStatusView.setRefused();
        }
        viewHolder.micNoticeStatusView.setOnItemClickListener(new MicNoticeStatusView.NoticeItemClickListener() {
            @Override
            public void onItemClick(boolean isAccept) {
                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(noticeItemInfo, isAccept);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mUserList!=null){
            return mUserList.size();
        }
        return 0;
    }
    public void setUserList(List<NoticeItemInfo> mUserList) {
        this.mUserList = mUserList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(
        OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private static class NoticeViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView mTvUserName;

        MicNoticeStatusView micNoticeStatusView;

        NoticeViewHolder(View view) {
            super(view);
            this.view = view;
            this.mTvUserName = (TextView)view.findViewById(R.id.tv_user_name);
            this.micNoticeStatusView = view.findViewById(R.id.mic_notice_status_view);
        }
    }

    public static class NoticeItemInfo{
        public UserInfo userInfo;
        public int micNoticeStatus = MicNoticeStatusView.STATUS_PREPARE;

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj instanceof NoticeItemInfo){
                if(userInfo == null || ((NoticeItemInfo) obj).userInfo == null){
                    return false;
                }
                if (userInfo.getUserId()==((NoticeItemInfo)obj).userInfo.getUserId()){
                    return true;
                }
            }
            return false;
        }
    }
}
