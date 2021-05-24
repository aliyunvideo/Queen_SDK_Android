package com.alilive.alilivesdk_demo.ViewHolder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
/**
 * BaseViewHolder
 */
public abstract class BaseViewHolder<T> extends RecyclerView.ViewHolder {

    public BaseViewHolder(View itemView) {
        super(itemView);
    }

    public abstract void bindView(T bean, int pos);

    public static View inflateItemView(Context context, ViewGroup parent, int rid) {
        return LayoutInflater.from(context).inflate(rid, parent, false);
    }
}

