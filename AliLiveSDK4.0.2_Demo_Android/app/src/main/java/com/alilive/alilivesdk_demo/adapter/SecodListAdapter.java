package com.alilive.alilivesdk_demo.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.alilive.alilivesdk_demo.R;

/**
 * data:2020-08-30
 */
public class SecodListAdapter extends RecyclerView.Adapter {

    private String[] mUserList;
    private OnClickItemListener onItemClickListener;
    private int mCheckPos;
    public void setOnItemClickListener(OnClickItemListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_second, parent,
            false);
        MyViewHolder viewHolder = new MyViewHolder(view);
        return viewHolder;
    }

    public void setmUserList(String[] mUserList,int checkPos) {
        this.mUserList = mUserList;
        mCheckPos=checkPos;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        MyViewHolder viewHolder = (MyViewHolder) holder;
        viewHolder.mName.setText(mUserList[position]);
        if(position==mCheckPos){
            viewHolder.mCheck.setVisibility(View.VISIBLE);
        }else {
            viewHolder.mCheck.setVisibility(View.GONE);
        }
        viewHolder.mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCheckPos=position;
                notifyDataSetChanged();
                if(onItemClickListener!=null){
                    onItemClickListener.onClick(mUserList[position],position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        if (mUserList !=null){
            return mUserList.length;
        }
        return 0;
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        private ImageView mCheck;
        private TextView mName;
        private RelativeLayout mParent;

        public MyViewHolder(View itemView) {
            super(itemView);
            mName = itemView.findViewById(R.id.tv_name);
            mCheck = itemView.findViewById(R.id.iv_check);
            mParent=itemView.findViewById(R.id.rl_parent);
        }
    }

    public interface  OnClickItemListener{
        public  void onClick(String content,int position);
    }
}
