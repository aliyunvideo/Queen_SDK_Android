package com.alilive.alilivesdk_demo.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.alilive.alilivesdk_demo.activity.AppApplication;

import static com.alilive.alilivesdk_demo.activity.HomeActivity.EXTRA_USER_ID;

/**
 * SP 工具类
 */
public class SPUtils {

    public static final String SP_NAME_DATA = "data";

    public static SharedPreferences getPreferences() {
        return AppApplication.appContext.getSharedPreferences(SP_NAME_DATA, Context.MODE_PRIVATE);
    }

    public static boolean isMe(String userId) {
        SharedPreferences sp = AppApplication.appContext.getSharedPreferences(SP_NAME_DATA, Context.MODE_PRIVATE);
        String localUserId = String.valueOf(sp.getInt(EXTRA_USER_ID,-1));
        return TextUtils.equals(userId, localUserId);
    }
}
