package com.netease.biz_live.yunxin.live.floatplay

import android.os.Build
import com.netease.biz_live.yunxin.live.floatplay.FloatWindowPermissionManager
import android.content.Intent
import android.app.Activity
import com.netease.yunxin.kit.alog.ALog
import android.app.AppOpsManager
import android.content.Context
import android.net.Uri
import android.os.Binder
import android.provider.Settings
import android.util.Log
import com.netease.biz_live.yunxin.live.floatplay.FloatWindowPermissionManager.checkOp
import java.lang.Exception

/**
 * 悬浮窗权限管理
 */
object FloatWindowPermissionManager {
    const val TAG = "FloatWindowPermissionManager"

    /***
     * @note
     * 检查悬浮窗开启权限
     *
     * @return 用户是否打开悬浮框权限
     */
    fun isFloatWindowOpAllowed(context: Context): Boolean {
        return try {
            when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                    Settings.canDrawOverlays(context.applicationContext)
                }
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> {
                    checkOp(context, 24) //24表示AppOpsManager中的悬浮窗权限为24 hide api
                }
                else -> {
                    true
                }
            }
        } catch (ignore: Throwable) {
            FloatPlayLogUtil.log(TAG,"ignore:$ignore")
            false
        }
    }

    /**
     * @note 悬浮窗开启权限
     */
    fun requestFloatWindowPermission(context: Context): Boolean {
        try {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            intent.data = Uri.parse("package:" + context.packageName)
            if (context !is Activity) {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            FloatPlayLogUtil.log(TAG, "requestFloatWindowPermission Exception")
            return false
        }
        return true
    }

    private fun checkOp(context: Context, op: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val manager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                val method = AppOpsManager::class.java.getDeclaredMethod(
                    "checkOp",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                return AppOpsManager.MODE_ALLOWED == method.invoke(
                    manager,
                    op,
                    Binder.getCallingUid(),
                    context.packageName
                ) as Int
            } catch (e: Exception) {
                FloatPlayLogUtil.log(TAG, Log.getStackTraceString(e))
            }
        }
        return true
    }
}