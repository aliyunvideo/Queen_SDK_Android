package com.alilive.alilivesdk_demo.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.OnVideoItemClickListener;
import com.alilive.alilivesdk_demo.socket.VideoUserInfo;
import com.alilive.alilivesdk_demo.utils.SPUtils;
import com.alilive.alilivesdk_demo.view.DialogView;
import com.alivc.live.AliLiveRenderView;

import java.util.List;

/**
 * data:2020-08-30
 */
public class VideoListAdapter extends RecyclerView.Adapter {

    private List<VideoUserInfo> mUserList;
    private OnVideoItemClickListener onItemClickListener;

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent,
            false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        viewHolder.flContainer.removeAllViews();
        final VideoUserInfo userInfo = mUserList.get(position);
        if (userInfo.getRenderView() != null) {
            AliLiveRenderView renderView = userInfo.getRenderView();
            if (renderView != null) {
                ViewParent parent = userInfo.getRenderView().getParent();
                if (parent != null && parent instanceof ViewGroup) {
                    ((ViewGroup) parent).removeView(renderView);
                }
                FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
                viewHolder.flContainer.addView(renderView, params);//添加到布局中
            }
        }
        if (userInfo.fromAnchorPage){
            viewHolder.btnSwitchCamera.setVisibility(View.GONE);
            viewHolder.btnExitVideo.setVisibility(View.GONE);
        }else if (SPUtils.isMe(String.valueOf(userInfo.getUserId()))){
            viewHolder.btnSwitchCamera.setVisibility(View.VISIBLE);
            viewHolder.btnExitVideo.setVisibility(View.VISIBLE);
        }

        viewHolder.btnSwitchCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null){
                    onItemClickListener.onItemClick();
                }
            }
        });
        viewHolder.btnExitVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null){
                    String content = userInfo.fromAnchorPage? String.format("你确定要结束与%s的互动连麦吗？",userInfo.getUserName()) : "你确定要结束互动连麦吗？";
                    showExitDialog(v.getContext(), content, userInfo);
                }
            }
        });
    }

    private void showExitDialog(Context context, String content, final VideoUserInfo userInfo) {
        DialogView dialogView = new DialogView(context);
        dialogView.setContent(content);
        dialogView.setConfirmOnClickListener(new DialogView.OnConfirmClickListener() {
            @Override
            public void onClick() {
                if (onItemClickListener != null) {
                    onItemClickListener.onExitClick(userInfo);
                }
            }
        });
        dialogView.show();
    }

    @Override
    public int getItemCount() {
        if (mUserList !=null){
            return mUserList.size();
        }
        return 0;
    }

    public void setOnItemClickListener(OnVideoItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        private FrameLayout flContainer;
        private ImageView btnSwitchCamera;
        private TextView btnExitVideo;

        public MyViewHolder(View itemView) {
            super(itemView);
            flContainer = itemView.findViewById(R.id.fl_container);
            btnSwitchCamera = itemView.findViewById(R.id.btn_switch_camera);
            btnExitVideo = itemView.findViewById(R.id.btn_exit_video);
        }
    }

    public void setUserList(List<VideoUserInfo> data) {
        mUserList = data;
        notifyDataSetChanged();
    }
}
