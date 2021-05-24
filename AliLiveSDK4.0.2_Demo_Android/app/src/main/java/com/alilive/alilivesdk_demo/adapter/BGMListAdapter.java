package com.alilive.alilivesdk_demo.adapter;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.bean.MusicBean;

import java.util.List;

public class BGMListAdapter extends RecyclerView.Adapter<BGMListAdapter.BGMViewHolder> {


    private List<MusicBean> mData;
    private OnItemClickListener mOnItemClickListener;

    @NonNull
    @Override
    public BGMViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_recycle_bgm_item_view, parent, false);
        BGMViewHolder bgmViewHolder = new BGMViewHolder(view);
        return bgmViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull BGMViewHolder holder, final int position) {
        MusicBean musicBean = mData.get(position);
        boolean local = musicBean.isLocal();
        if(local){
            //本地文件展示全路径
            holder.mBGMTitleTextView.setText(musicBean.getPath());
        }else{
            holder.mBGMTitleTextView.setText(musicBean.getName());
        }

        holder.mBGMTitleTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mOnItemClickListener != null){
                    mOnItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public void setData(List<MusicBean> mPathList) {
        this.mData = mPathList;
        notifyDataSetChanged();
    }

    public static class BGMViewHolder extends RecyclerView.ViewHolder {

        private TextView mBGMTitleTextView;

        public BGMViewHolder(View itemView) {
            super(itemView);
            mBGMTitleTextView = itemView.findViewById(R.id.tv_bgm_title);
        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }
}
