package com.alilive.alilivesdk_demo.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public abstract class BaseLazyFragment extends Fragment {



//    初始化布局文件
    protected abstract int initLayout();

//    findViewById
    protected abstract void findViewById(View view);

    // 表示开始加载数据, 但不表示数据加载已经完成
    public abstract void loadDataStart();




    protected Context mContext;

    /**
     * view是否初始化
     */
    private boolean isInitView = false;

    /**
     * 第一次调用
     */
    private boolean firstOne = true;

    private View mRootView;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (mRootView != null) {
            return mRootView;
        }
        mRootView =  inflater.inflate(initLayout(), container, false);
        findViewById(mRootView);
        isInitView = true;
        return mRootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        lazy(getUserVisibleHint());
    }

    private void lazy(boolean visibleOrHidden) {
        if (isInitView && visibleOrHidden) {
            if (firstOne) {
                firstOne = false;
                loadDataStart();
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        lazy(isVisibleToUser);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mContext = null;
    }
}