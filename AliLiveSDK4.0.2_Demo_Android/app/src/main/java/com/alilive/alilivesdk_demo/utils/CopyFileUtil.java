/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package com.alilive.alilivesdk_demo.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AutoCompleteTextView;


import com.alivc.live.utils.AssertUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


/**
 * 录制assets资源解压
 */
public class CopyFileUtil {

    private static final String TAG = "CopyFileUtil";
    private static String SD_DIR ;
    public final static String QU_NAME = "race_res";
    public final static String BGM_NAME = "bgm";
    public static String QU_DIR ;

    public static String getFileDir(){
        if(TextUtils.isEmpty(QU_DIR)){
            QU_DIR  = SD_DIR ;
        }

        if(TextUtils.isEmpty(QU_DIR) ){
            QU_DIR = "race_res/";
        }

        return QU_DIR;
    }

    private static void copySelf(Context cxt, String root) {
        try {
            String[] files = cxt.getAssets().list(root);
            if (files.length > 0) {
                File subdir = new File(SD_DIR + root);
                if (!subdir.exists()) {
                    subdir.mkdirs();
                }
                for (String fileName : files) {
                    if (new File(SD_DIR + root + File.separator + fileName).exists()) {
                        continue;
                    }
                    copySelf(cxt, root + "/" + fileName);
                }
            } else {
                Log.d(TAG, "copy...." + SD_DIR + root);
                OutputStream myOutput = new FileOutputStream(SD_DIR + root);
                InputStream myInput = cxt.getAssets().open(root);
                byte[] buffer = new byte[1024 * 8];
                int length = myInput.read(buffer);
                while (length > 0) {
                    myOutput.write(buffer, 0, length);
                    length = myInput.read(buffer);
                }

                myOutput.flush();
                myInput.close();
                myOutput.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void copyAll(Context cxt) {
        SD_DIR = getExtFileDir(cxt);
        QU_DIR = SD_DIR;
        AssertUtils.copyFilesFromAssets(cxt, QU_NAME, QU_DIR);
    }

    public static void copyBGMFile(Context cxt){
        String path = cxt.getExternalFilesDir(BGM_NAME) + File.separator;
        AssertUtils.copyFilesFromAssets(cxt,BGM_NAME,path);
    }

    public static String getBGMFile(Context cxt){
        return cxt.getExternalFilesDir(BGM_NAME) + File.separator;
    }

    private static String getExtFileDir(Context cxt) {
        return cxt.getExternalFilesDir(QU_NAME) + File.separator;
    }

    public static void unZip(String srcDir) {
        File[] files = new File(srcDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                if (name != null && name.endsWith(".zip")) {
                    return true;
                }
                return false;
            }
        });
        if (files == null) {
            return;
        }
        for (final File file : files) {
            int len = file.getAbsolutePath().length();
            if (!new File(file.getAbsolutePath().substring(0, len - 4)).exists()) {
                try {
                    unZipFolder(file.getAbsolutePath(), srcDir);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public static void unZipFolder(String zipFileString, String outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName = "";
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                folder.mkdirs();
            } else {

                File file = new File(outPathString + File.separator + szName);
                file.createNewFile();
                // get the output stream of the file
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                // read (len) bytes into buffer
                while ((len = inZip.read(buffer)) != -1) {
                    // write (len) byte from buffer at the position 0
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }
}
