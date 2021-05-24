package com.alilive.alilivesdk_demo.listener;

import com.alilive.alilivesdk_demo.adapter.NoticeListAdapter;
/**
 * data:2020-08-30
 */
public interface OnItemClickListener {
    void onItemClick(NoticeListAdapter.NoticeItemInfo userInfo, boolean isAccept);
}
