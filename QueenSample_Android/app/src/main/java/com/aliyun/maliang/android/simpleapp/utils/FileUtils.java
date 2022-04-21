package com.aliyun.maliang.android.simpleapp.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class FileUtils {

    public static final String DCIM_FILE_PATH = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getPath();
    public static final String photoFilePath;
    public static final String exportVideoDir;

    static {
        if (Build.FINGERPRINT.contains("Flyme")
                || Pattern.compile("Flyme", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.MANUFACTURER.contains("Meizu")
                || Build.MANUFACTURER.contains("MeiZu")) {
            photoFilePath = DCIM_FILE_PATH + File.separator + "Camera";
        } else if (Build.FINGERPRINT.contains("vivo")
                || Pattern.compile("vivo", Pattern.CASE_INSENSITIVE).matcher(Build.DISPLAY).find()
                || Build.MANUFACTURER.contains("vivo")
                || Build.MANUFACTURER.contains("Vivo")) {
            photoFilePath = Environment.getExternalStoragePublicDirectory("") + File.separator + "相机";
        } else {
            photoFilePath = DCIM_FILE_PATH + File.separator + "Camera";
        }
        exportVideoDir = DCIM_FILE_PATH + File.separator + "Queen";
        createFileDir(photoFilePath);
        createFileDir(exportVideoDir);
    }

    private static void createFileDir(String path) {
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public static String getAlbumPath() {
        File fileDir = new File(exportVideoDir);
        return fileDir.exists() ? exportVideoDir : photoFilePath;
    }

    public static String getCurrentTimePhotoFileName() {
        SimpleDateFormat timeSdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
        String fileName = "Queen_" + timeSdf.format(new Date()) + ".png";
        return fileName;
    }

    public static String makeAlbumPhotoFileName() {
        return getAlbumPath() + File.separator + getCurrentTimePhotoFileName();
    }

    public static Bitmap bitmapRotateAndFlip(Bitmap originBmp, int degree, boolean flipY, boolean flipX) {
        Matrix matrix = new Matrix();

        if (flipY || flipX) {
            matrix.postScale(
                    flipX ? -1 : 1,
                    flipY ? -1 : 1,
                    originBmp.getWidth() / 2, originBmp.getHeight() / 2);
        }
        if (degree > 0) {
            matrix.postRotate(degree);
        }

        return Bitmap.createBitmap(originBmp, 0, 0, originBmp.getWidth(), originBmp.getHeight(), matrix, true);
    }

    public static Bitmap bitmapFlipAndRotate(Bitmap originBmp, int degree, boolean flipY, boolean flipX) {
        Matrix matrix = new Matrix();

        if (degree > 0) {
            matrix.postRotate(degree);
        }
        if (flipY || flipX) {
            matrix.postScale(
                    flipX ? -1 : 1,
                    flipY ? -1 : 1,
                    originBmp.getWidth() / 2, originBmp.getHeight() / 2);
        }

        return Bitmap.createBitmap(originBmp, 0, 0, originBmp.getWidth(), originBmp.getHeight(), matrix, true);
    }

    public static boolean saveToFile(Bitmap bitmap, String outPath, Bitmap.CompressFormat format, int quality) {
        if (bitmap == null) {
            return false;
        } else {
            try {
                File fileOut = new File(outPath);
                boolean ret;
                if (fileOut.exists()) {
                    ret = fileOut.delete();
                    if (!ret) {
                        Log.e("FileUtils", "delete() FAIL:" + fileOut.getAbsolutePath());
                    }
                }

                if (!fileOut.getParentFile().exists()) {
                    fileOut.getParentFile().mkdirs();
                }

                ret = fileOut.createNewFile();
                if (!ret) {
                    Log.e("FileUtils", "createNewFile() FAIL:" + fileOut.getAbsolutePath());
                }

                FileOutputStream fos = new FileOutputStream(fileOut);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                bitmap.compress(format, quality, bos);
                bitmap.recycle();
                fos.flush();
                if (bos != null) {
                    bos.close();
                }

                return true;
            } catch (Exception var9) {
                var9.printStackTrace();
                return false;
            }
        }
    }
}
