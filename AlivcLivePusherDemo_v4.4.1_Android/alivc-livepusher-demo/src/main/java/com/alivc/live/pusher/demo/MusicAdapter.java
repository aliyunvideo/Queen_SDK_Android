package com.alivc.live.pusher.demo;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import android.util.Log;

import java.util.ArrayList;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder> {

    private Context mContext;

    private MusicDialog.OnItemClick mOnItemClick = null;

    private ArrayList<MusicInfo> musicList = new ArrayList<>();

    private int mPosition = 1;

    private boolean mIsLoop = true;

    public MusicAdapter(Context context) {
        this.mContext = context;
        MusicInfo info = new MusicInfo(mContext.getResources().getString(R.string.no_music), "", "", "");
        musicList.add(info);
        ArrayList<MusicInfo> list = Common.getResource();
        musicList.addAll(list);
        MusicInfo info1 = new MusicInfo(mContext.getResources().getString(R.string.internet_music), "", "", "http://docs-aliyun.cn-hangzhou.oss.aliyun-inc.com/assets/attach/51991/cn_zh/1511776743437/JUST%202017.mp3");
        musicList.add(info1);
    }

    @Override
    public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.music_item_info, parent, false);
        MusicViewHolder holder = new MusicViewHolder(itemView);
        holder.tvMusicName = (TextView) itemView.findViewById(R.id.music_name);
        holder.tvPlayTime = (TextView) itemView.findViewById(R.id.play_time);
        holder.tvTotalTime = (TextView) itemView.findViewById(R.id.total_time);
        holder.tvTime = (LinearLayout) itemView.findViewById(R.id.time);
        holder.tvMusicCheck = (ImageView) itemView.findViewById(R.id.music_check);
        holder.tvLoop = (ImageView) itemView.findViewById(R.id.loop);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MusicViewHolder holder, final int position) {
        MusicInfo musicInfo = musicList.get(position);
        if(position == 0) {
            holder.tvTime.setVisibility(View.GONE);
        }
        if(mPosition == position) {
            holder.tvMusicCheck.setVisibility(View.VISIBLE);
            holder.tvMusicName.setTextColor(mContext.getResources().getColor(R.color.text_green));
            if(position != 0) {
                holder.tvLoop.setVisibility(mIsLoop ? View.VISIBLE : View.GONE);
            }
        } else {
            holder.tvMusicCheck.setVisibility(View.INVISIBLE);
            holder.tvMusicName.setTextColor(mContext.getResources().getColor(R.color.text_black));
            holder.tvLoop.setVisibility(View.INVISIBLE);
        }
        if (musicInfo != null) {
            holder.tvMusicName.setText(musicInfo.getMusicName());
            if(musicInfo.getPlayTime() != null && !musicInfo.getPlayTime().isEmpty()) {
                holder.tvPlayTime.setText(musicInfo.getPlayTime());
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }

            if(musicInfo.getTotalTime() != null && !musicInfo.getTotalTime().isEmpty()) {
                holder.tvTotalTime.setText(musicInfo.getTotalTime());
            } else {
                holder.tvTime.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("MusicAdapter", "onclick position is " + position);
                    if(mOnItemClick != null) {
                        mOnItemClick.onItemClick(musicList.get(position), position);
                    }
                    mPosition = position;
                    notifyDataSetChanged();

                }
            });
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder{
        TextView tvMusicName;
        TextView tvPlayTime;
        TextView tvTotalTime;
        LinearLayout tvTime;
        ImageView tvMusicCheck;
        ImageView tvLoop;

        public MusicViewHolder(View itemView) {
            super(itemView);
        }
    }

    public static class MusicInfo {
        private String mMusicName;
        private String mPlayTime;
        private String mTotalTime;
        private String mPath;

        public MusicInfo() {

        }
        public MusicInfo(String musicName, String playTime, String totalTime, String path) {
            mPlayTime = playTime;
            mTotalTime = totalTime;
            mMusicName = musicName;
            mPath = path;
        }

        public String getPlayTime() {
            return mPlayTime;
        }

        public void setPlayTime(String playTime) {
            mPlayTime = playTime;
        }

        public String getTotalTime() {
            return mTotalTime;
        }

        public void setTotalTime(String totalTime) {
            mTotalTime = totalTime;
        }

        public String getMusicName() {
            return mMusicName;
        }

        public void setMusicName(String musicName) {
            mMusicName = musicName;
        }

        public String getPath() {
            return mPath;
        }

        public void setPath(String path) {
            mPath = path;
        }
    }

    public void setOnItemClick(MusicDialog.OnItemClick onItemClick) {
        mOnItemClick = onItemClick;
    }

    public void updateItemView(MusicViewHolder holder, int position, boolean isLoop) {

        if(holder != null) {
            holder.tvLoop.setVisibility(isLoop ? View.VISIBLE : View.GONE);
            mIsLoop = isLoop;
        }
    }

    public void updateProgress(MusicViewHolder holder, long progress, long totalTime) {
        if(holder != null && mPosition != 0) {
            holder.tvTime.setVisibility(View.VISIBLE);
            holder.tvPlayTime.setText(Common.getTime(progress));
            holder.tvTotalTime.setText(Common.getTime(totalTime));
        }
    }

    public void startDefaultMusic() {
        if(mOnItemClick != null && musicList.size() > 0) {
            mOnItemClick.onItemClick(musicList.get(1), 1);
        }
    }
}
