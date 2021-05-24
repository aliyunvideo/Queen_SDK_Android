package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.listener.ButtonClickListener;
import com.alilive.alilivesdk_demo.adapter.ButtonListAdapter;

import java.util.List;

/**
 * 底部按钮
 * @author xlx
 */
public class ButtonListView extends FrameLayout {
    private RecyclerView mRecyclerView;
    private ButtonListAdapter mButtonListAdapter;
    private ButtonClickListener clickListener;
    private boolean isItemsHide = false;

    public void setClickListener(ButtonClickListener clickListener) {
        this.clickListener = clickListener;
    }


    public ButtonListView(@NonNull Context context) {
        this(context, null);
    }

    public ButtonListView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonListView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setData(List<String> data) {
        mButtonListAdapter.setData(data);
    }
    public void addItem(String item){
        mButtonListAdapter.addItem(item);
    }
    public void hideItems(boolean isItemHide){
        this.isItemsHide = isItemHide;
        mButtonListAdapter.hideItems(isItemHide);
    }
    public boolean isItemsHide(){
        return  isItemsHide;
    }
    private void initView(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.live_button_list, this, true);
        initRecyclerView(view);
    }

    private void initRecyclerView(View view) {
        mRecyclerView = view.findViewById(R.id.live_button_recycle);
        mButtonListAdapter = new ButtonListAdapter();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(ButtonListView.this.getContext()));
        mRecyclerView.setAdapter(mButtonListAdapter);
        mRecyclerView.setItemAnimator(null);
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mButtonListAdapter.setClickListener(new ButtonClickListener() {
            @Override
            public void onButtonClick(String message, int position) {
                if(clickListener != null){
                    clickListener.onButtonClick(message,position);
                }
            }
        });
    }
    public void setButtonEnable(String buttonName,boolean enable){
        if (mButtonListAdapter!=null){
            mButtonListAdapter.setButtonEnable(buttonName,enable);
        }
    }
    public void changeButtonName(String oldName,String newName){
        if (mButtonListAdapter!=null){
            mButtonListAdapter.changeButtonName(oldName,newName);
        }
    }

}
