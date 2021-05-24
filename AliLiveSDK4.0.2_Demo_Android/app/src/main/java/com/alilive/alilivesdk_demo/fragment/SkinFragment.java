package com.alilive.alilivesdk_demo.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;


import com.alilive.alilivesdk_demo.R;
import com.alilive.alilivesdk_demo.adapter.BeautyListAdapter;
import com.alilive.alilivesdk_demo.bean.BeautyItemData;
import com.alilive.alilivesdk_demo.bean.Constants;
import com.alilive.alilivesdk_demo.listener.BeautyClickAndSlideListener;

import java.util.List;

/**
 * 美肌
 * */
public class SkinFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private BeautyListAdapter mBeautyListAdapter;
    private BeautyClickAndSlideListener mClickAndSlideListener;
    private Switch mSwitch;



    public void setClickListener(BeautyClickAndSlideListener clickListener) {
        this.mClickAndSlideListener = clickListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.live_skin_fragment, container, false);
        findViewById(view);
        loadDataStart();
        return view;
    }



    protected void findViewById(View view) {
        mRecyclerView = view.findViewById(R.id.live_skin_recycle);
        mSwitch = view.findViewById(R.id.live_skin_switch);
    }

    public void loadDataStart() {
        mBeautyListAdapter = new BeautyListAdapter();
        mBeautyListAdapter.enableFocus(false);
        List<BeautyItemData> strings = Constants.getBeautySkinNameList();
        mBeautyListAdapter.setData(strings);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));
        mRecyclerView.setAdapter(mBeautyListAdapter);
        mRecyclerView.setItemAnimator(null);
//        去除下拉阴影
        mRecyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        mBeautyListAdapter.setClickListener(new BeautyClickAndSlideListener() {
            @Override
            public void onButtonClick(String pageName,int pageIndex,String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      message, int position) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onButtonClick("美肌",0,message,position);
                }
            }

            @Override
            public void onProgressChanged(String pageName,int pageIndex,String message, float position) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onProgressChanged("美肌",0,message,position);
                }
            }

            @Override
            public void onSwitchChanged(String pageName,int pageIndex,String message, boolean isCheck) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onSwitchChanged("美肌",0,message,isCheck);
                }
            }

            @Override
            public void onPageSwitch(String pageName,int pageIndex, boolean isCheck) {

            }
        });
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(mClickAndSlideListener != null){
                    mClickAndSlideListener.onPageSwitch("美肌",0,isChecked);
                }
            }
        });
    }

    public void setData(){
        if(mBeautyListAdapter!=null&&mRecyclerView!=null) {
            mBeautyListAdapter.setData(Constants.getBeautySkinNameList());
            mBeautyListAdapter.notifyDataSetChanged();
        }
    }
}
