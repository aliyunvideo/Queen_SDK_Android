package com.alilive.alilivesdk_demo.adapter;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.ButtonClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonListAdapter extends RecyclerView.Adapter {
    private ButtonClickListener clickListener;
    private boolean isItemHide = false;

    public void setClickListener(ButtonClickListener clickListener) {
        this.clickListener = clickListener;
        mButtonEnableMap = new HashMap<>();
    }

    private List<String> mListDatas = new ArrayList<>();
    private List<String> mOriginalDatas = new ArrayList<>();
    private Map<String,Boolean> mButtonEnableMap;
    public void setData(List<String> data){

        if(data == null){
            return;
        }
        mOriginalDatas = data;
        mListDatas.clear();
        mListDatas.addAll(data);
        if(mListDatas.size() % 4 == 0){
            mListDatas.add("");
            mListDatas.add("");
            mListDatas.add("");
            mListDatas.add("收起菜单");
        } else if(mListDatas.size() % 4 == 1){
            mListDatas.add("");
            mListDatas.add("");
            mListDatas.add("收起菜单");
        } else if(data.size() % 4 == 2){
            mListDatas.add("");
            mListDatas.add("收起菜单");
        } else if(mListDatas.size() % 4 == 3){
            mListDatas.add("收起菜单");
        }
        notifyDataSetChanged();
    }
    public void addItem(String item){
        if (mOriginalDatas!=null&&!mOriginalDatas.contains(item)){
            mOriginalDatas.add(item);
           setData(mOriginalDatas);
        }
    }
    public void removeItem(String item){
        if (mOriginalDatas!=null&&mOriginalDatas.contains(item)){
            mOriginalDatas.remove(item);
            setData(mOriginalDatas);
        }
    }
    public List<String> getData(){
        return mListDatas;
    }
    public void hideItems(boolean isItemHide){
        this.isItemHide = isItemHide;
        if(isItemHide){
            mListDatas.set(mListDatas.size()-1,"展示面板");
        } else {
            mListDatas.set(mListDatas.size()-1,"收起菜单");
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_recycle_button_item_view, parent, false);
        ButtonHolder buttonHolder = new ButtonHolder(view);
        return buttonHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
//        position 从 0开始
        ButtonHolder viewHolder= (ButtonHolder) holder;
        viewHolder.textView.setText(mListDatas.get(position));
        if(isItemHide && position < mListDatas.size()-1){
            viewHolder.textView.setClickable(false);
            viewHolder.textView.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.textView.setClickable(true);
            viewHolder.textView.setVisibility(View.VISIBLE);
        }
        if(TextUtils.isEmpty(mListDatas.get(position))){
            viewHolder.textView.setClickable(false);
            viewHolder.textView.setVisibility(View.INVISIBLE);
        }
        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null){
                    clickListener.onButtonClick(mListDatas.get(position),position);
                }
            }
        });
        boolean enable = true;
        if (mButtonEnableMap.containsKey(mListDatas.get(position))){
            enable = mButtonEnableMap.get(mListDatas.get(position));
        }
        viewHolder.textView.setEnabled(enable);
    }
    @Override
    public int getItemCount() {
        return mListDatas.size();
    }

    private class ButtonHolder extends RecyclerView.ViewHolder {
        private TextView textView;
        public ButtonHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.vip_item_text);
        }
    }
    public void setButtonEnable(String buttonName,boolean enable){
        mButtonEnableMap.put(buttonName,enable);
        int position = -1;
        for (int i = 0; i < mListDatas.size(); i++) {
            if (mListDatas.get(i).equals(buttonName)){
                position = i;
            }
        }
        if (position>=0){
            notifyItemChanged(position);
        }
    }
    public void changeButtonName(String oldName,String newName){
        int position = -1;
        for (int i = 0; i < mListDatas.size(); i++) {
            if (mListDatas.get(i).equals(oldName)){
                position = i;
            }
        }
        if (position>=0){
            mListDatas.set(position,newName);
            notifyItemChanged(position);
        }

    }

}


