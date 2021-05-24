package com.alilive.alilivesdk_demo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * 单例Toast工具类
 *
 * 1.解决toast排队的问题
 * 2.修复Toast在android 7.1手机上的BadTokenException
 * 3.兼容位置、时长、stringId
 */
public class ToastUtils {

    private static Toast toast;
    /**
     * Android原生Toast的显示，主要解决点多少就提示多少次的问题
     */
    public static void showToast(Context context, String content){

        if (toast == null){
            toast = Toast.makeText(context, content, Toast.LENGTH_SHORT);
        } else {
            toast.setText(content);
        }
        toast.show();
    }

}


