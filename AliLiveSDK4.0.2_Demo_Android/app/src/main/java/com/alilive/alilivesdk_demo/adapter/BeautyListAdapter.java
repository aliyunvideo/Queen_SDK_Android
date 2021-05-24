package com.alilive.alilivesdk_demo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.bean.BeautyItemData;
import com.alilive.alilivesdk_demo.listener.BeautyClickAndSlideListener;


import java.util.ArrayList;
import java.util.List;

public class BeautyListAdapter extends RecyclerView.Adapter {
    private BeautyClickAndSlideListener clickListener;
    private boolean isItemHide = false;
    private boolean isEnableFocus = true;
//    设置switch是否互斥
    private boolean mSwitchMutex = true;
    private int mSelectPosition = -1;

    private boolean[] mSwitchList = {false,false,false};

    public void setClickListener(BeautyClickAndSlideListener clickListener) {
        this.clickListener = clickListener;
    }

    private List<BeautyItemData> mListDatas = new ArrayList<>();

    public void setData(List<BeautyItemData> data){
        if(data == null){
            return;
        }
        mListDatas = data;
        notifyDataSetChanged();
    }
    public void setSwitchMutex(boolean mutex){
        mSwitchMutex = mutex;

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.live_recycle_beauty_item_view, parent, false);
        BeautyHolder buttonHolder = new BeautyHolder(view);
        return buttonHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
//        position 从 0开始
        final BeautyHolder viewHolder= (BeautyHolder) holder;
        viewHolder.itemName.setText(mListDatas.get(position).getName());
        if (position==mSelectPosition&&isEnableFocus){
            viewHolder.mParent.setSelected(true);
        }else {
            viewHolder.mParent.setSelected(false);
        }
        viewHolder.mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(clickListener != null){
                    clickListener.onButtonClick("",0,mListDatas.get(position).getName(),position);
                }
                if (mSelectPosition != position&&isEnableFocus){
                    mSelectPosition = position;
                    notifyDataSetChanged();
                }
            }
        });
        switch (mListDatas.get(position).getType()){
            case NULL:
                viewHolder.itemBar.setVisibility(View.GONE);
                viewHolder.itemValue.setVisibility(View.GONE);
                viewHolder.mSwitch.setVisibility(View.GONE);
                break;
            case SEEKBAR:
                viewHolder.mSwitch.setVisibility(View.GONE);
                viewHolder.itemBar.setVisibility(View.VISIBLE);
                viewHolder.itemValue.setVisibility(View.VISIBLE);
                if(mListDatas.get(position).isNegative()){
                    viewHolder.itemBar.setProgress((mListDatas.get(position).getValue()+100) / 2);
                } else {
                    viewHolder.itemBar.setProgress(mListDatas.get(position).getValue());
                }
                viewHolder.itemBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        float value = 0;
                        if(mListDatas.get(position).isNegative()){
                            value =(progress * 2 - 100) / 100.00f;
                            viewHolder.itemValue.setText(value+"");
                        } else {
                            value = progress / 100.00f;
                            viewHolder.itemValue.setText(progress / 100.00f+"");
                        }
                        if(clickListener != null){
                            clickListener.onProgressChanged("",0,mListDatas.get(position).getName(),value);
                        }
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                if(mListDatas.get(position).isNegative()){
                    viewHolder.itemValue.setText((float)(mListDatas.get(position).getValue()/100.00f)+"");
                } else {
                    viewHolder.itemValue.setText((float)(mListDatas.get(position).getValue()/100.00f)+"");
                }
                break;
            case SWITCH:
                viewHolder.itemBar.setVisibility(View.GONE);
                viewHolder.itemValue.setVisibility(View.GONE);
                if(mListDatas.get(position).equals("<空>")){
                    viewHolder.mSwitch.setVisibility(View.GONE);
                } else {
                    viewHolder.mSwitch.setVisibility(View.VISIBLE);
                }
//                if(mSwitchMutex){
//                    viewHolder.mSwitch.setChecked(mSwitchList[position]);
//                }
                viewHolder.mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(clickListener != null){
//                            turnOffSwitch();
//                            mSwitchList[position] = isChecked;
                            clickListener.onSwitchChanged("",0,mListDatas.get(position).getName(),isChecked);
                        }
                    }
                });
                break;
            default:
                break;
        }
    }
    @Override
    public int getItemCount() {
        return mListDatas.size();
    }

    private class BeautyHolder extends RecyclerView.ViewHolder {
        private RelativeLayout mParent;
        private TextView itemName;
        private SeekBar itemBar;
        private TextView itemValue;
        private Switch mSwitch;
        public BeautyHolder(View itemView) {
            super(itemView);
            mParent = itemView.findViewById(R.id.beauty_item_parent);
            itemName = itemView.findViewById(R.id.beauty_item_text);
            itemBar = itemView.findViewById(R.id.beauty_item_seekbar);
            itemValue = itemView.findViewById(R.id.beauty_item_value);
            mSwitch = itemView.findViewById(R.id.beauty_item_switch);
        }
    }

    private void turnOffSwitch(){
        for (int i =0 ;i<mSwitchList.length;i++){
            mSwitchList[i] = false;
        }
    }
    public void enableFocus(boolean focus){
        isEnableFocus = focus;
    }

}


