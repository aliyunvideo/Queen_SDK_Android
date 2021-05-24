package com.alilive.alilivesdk_demo.listener;

/**
 * 功能模块点击click
 * @param <T>
 */
public interface OnItemFuncClickListener<T> {
    void onItemFuncClick(T bean, int pos);
}