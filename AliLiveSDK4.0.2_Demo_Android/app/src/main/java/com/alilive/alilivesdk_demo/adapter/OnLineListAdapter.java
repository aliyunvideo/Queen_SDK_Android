package com.alilive.alilivesdk_demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.socket.OnLineRoomListBean;
import com.alilive.alilivesdk_demo.socket.UserInfo;

import java.util.List;

/**
 * data:2020/9/27
 */
public class OnLineListAdapter extends RecyclerView.Adapter {
    private List<OnLineRoomListBean.OnlineRoomBean> mUserList;
    private OnItemClickListener onItemClickListener;
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_online_room, parent,
            false);
        OnLineRoomViewHolder viewHolder = new OnLineRoomViewHolder(view);
        return viewHolder;
    }
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final OnLineRoomListBean.OnlineRoomBean roomBean = mUserList.get(position);
        OnLineRoomViewHolder viewHolder = (OnLineRoomViewHolder)holder;
        viewHolder.mTvUserName.setText(roomBean.getUserName());
        viewHolder.mBtnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (onItemClickListener!=null){
                    onItemClickListener.onItemClick(roomBean);
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
    public void setRoomList(List<OnLineRoomListBean.OnlineRoomBean> mUserList) {
        this.mUserList = mUserList;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private static class OnLineRoomViewHolder extends RecyclerView.ViewHolder {
        View view;
        TextView mTvUserName;
        Button mBtnAccept;

        OnLineRoomViewHolder(View view) {
            super(view);
            this.view = view;
            this.mTvUserName = (TextView)view.findViewById(R.id.tv_room_name);
            this.mBtnAccept = (Button)view.findViewById(R.id.btn_accept);
        }
    }
    public interface OnItemClickListener {
        void onItemClick(OnLineRoomListBean.OnlineRoomBean room);
    }
}
