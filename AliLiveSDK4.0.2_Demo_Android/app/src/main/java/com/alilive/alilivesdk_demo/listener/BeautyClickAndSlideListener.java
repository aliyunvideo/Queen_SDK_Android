package com.alilive.alilivesdk_demo.listener;

public interface BeautyClickAndSlideListener {
    void onButtonClick(String pageName,int pageIndex,String message, int position);
    void onProgressChanged(String pageName,int pageIndex,String message, float position);
    void onSwitchChanged(String pageName,int pageIndex,String message, boolean isCheck);
    void onPageSwitch(String pageName,int pageIndex, boolean isCheck);
}
