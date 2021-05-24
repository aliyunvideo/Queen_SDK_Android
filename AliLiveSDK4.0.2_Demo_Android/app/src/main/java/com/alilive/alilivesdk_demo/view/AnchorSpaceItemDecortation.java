package com.alilive.alilivesdk_demo.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.alilive.alilivesdk_demo.utils.DensityUtils;

class AnchorSpaceItemDecortation extends RecyclerView.ItemDecoration {
    private int space;//声明间距 //使用构造函数定义间距
    private Context mContext;
    public AnchorSpaceItemDecortation(int space,Context context) {
        this.space = space;
        this.mContext=context;
    }
    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        //获得当前item的位置
        int position = parent.getChildAdapterPosition(view);
        if(position==0){
            outRect.left= DensityUtils.dip2px(mContext,28);
        }
        outRect.right = this.space;
    }
}
