package com.alilive.alilivesdk_demo.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.widget.EditText;

public class URLDialog {
    public void createAndShowDialog(String title,Context context, final OnUrlInputListener mOnUrlInputListener){
        final EditText inputServer = new EditText(context);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setView(inputServer)
                .setNegativeButton("取消", null);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if(mOnUrlInputListener != null){
                    mOnUrlInputListener.getUrl(inputServer.getText().toString());
                }
            }
        });
        builder.show();
    }
    public interface OnUrlInputListener{
        void getUrl(String url);
    }
}
