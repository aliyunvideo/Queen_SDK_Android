package com.aliyun.maliang.android.simpleapp.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import androidx.annotation.RequiresApi;

import android.provider.Settings;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.aliyun.maliang.android.simpleapp.R;

/**
 * 检查权限/权限数组
 * request权限
 */
public class PermissionUtils {

    public static final String[] PERMISSION_MANIFEST = {
        Manifest.permission.CAMERA,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static final String[] PERMISSION_STORAGE = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static final String[] PERMISSION_CAMERA = {
        Manifest.permission.CAMERA,
//        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * 无权限时对应的提示内容
     */
    public static final int[] NO_PERMISSION_TIP = {
        R.string.alivc_common_no_camera_permission,
    };

    private static final int PERMISSION_REQUEST_CODE = 1000;

    public static boolean checkAndRunPermissionsGroup(Activity activity, String[] permissions) {
        boolean checkResult = PermissionUtils.checkPermissionsGroup(activity, permissions);
        if (!checkResult) {
            PermissionUtils.requestPermissions(activity, PermissionUtils.PERMISSION_CAMERA, PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    public static boolean onHandlePermissionRequest(final Activity activity, int requestCode, int[] grantResults) {
        boolean isAllGranted = false;
        if (requestCode == PERMISSION_REQUEST_CODE) {
            isAllGranted = true;
            // 判断是否所有的权限都已经授予了
            for (int grant : grantResults) {
                if (grant != PackageManager.PERMISSION_GRANTED) {
                    isAllGranted = false;
                    break;
                }
            }
            if (!isAllGranted) {
                // 弹出对话框告诉用户需要权限的原因, 并引导用户去应用权限管理中手动打开权限按钮
                showPermissionDialog(activity);
            }
        }
        return isAllGranted;
    }


    //系统授权设置的弹框
    private static void showPermissionDialog(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(activity.getString(R.string.app_name) + "需要访问 \"摄像头\" 和 \"外部存储器\",否则会影响绝大部分功能使用, 请到 \"应用信息 -> 权限\" 中设置！");
        builder.setPositiveButton("去设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                activity.startActivity(intent);
            }
        });
        builder.setCancelable(false);
        builder.setNegativeButton("暂不设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog openAppDetDialog = builder.create();
        if (null != openAppDetDialog && !openAppDetDialog.isShowing()) {
            openAppDetDialog.show();
        }
    }



    /**
     * 检查多个权限
     *
     * 检查权限
     * @param permissions 权限数组
     * @param context Context
     * @return true 已经拥有所有check的权限 false存在一个或多个未获得的权限
     */
    private static boolean checkPermissionsGroup(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (!checkPermission(context, permission)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查单个权限
     * @param context Context
     * @param permission 权限
     * @return boolean
     */
    private static boolean checkPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 申请权限
     * @param activity Activity
     * @param permissions 权限数组
     * @param requestCode 请求码
     */
    public static void requestPermissions(Activity activity, String[] permissions, int requestCode) {
        // 先检查是否已经授权
        if (!checkPermissionsGroup(activity, permissions)) {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    /**
     * 通过AppOpsManager判断小米手机授权情况
     *
     * @return boolean
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static boolean checkXiaomi(Context context, String[] opstrArrays) {
        AppOpsManager appOpsManager = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        String packageName = context.getPackageName();
        for (String opstr : opstrArrays) {
            int locationOp = appOpsManager.checkOp(opstr, Binder.getCallingUid(), packageName);
            if (locationOp == AppOpsManager.MODE_IGNORED) {
                return false;
            }
        }

        return true;
    }

    /**
     * 没有权限的提示
     * @param context Context
     * @param tip 对于的提示 {@link #NO_PERMISSION_TIP}
     */
    public static void showNoPermissionTip(Context context, String tip) {
        Toast.makeText(context, tip, Toast.LENGTH_LONG).show();
    }

}
