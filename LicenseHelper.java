package com.aliyun.maliang.android.simpleapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import java.security.MessageDigest;

public class LicenseHelper {

    public static String getPackageSignature(Context context) {
        String signature = "";
        if (null != context) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), PackageManager.GET_SIGNATURES);
                Signature[] signs = packageInfo.signatures;
                if ((null != signs) && (0 < signs.length)) {
                    signature = hexdigest(signs[0].toByteArray());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return signature;
    }

    private static final char[] HEX_DIGIT = {48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};

    private static String hexdigest(byte[] paramArrayOfByte) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(paramArrayOfByte);
            byte[] arrayOfByte = localMessageDigest.digest();
            char[] arrayOfChar = new char[32];
            int i = 0;
            int j = 0;
            while (true) {
                if (i >= 16) {
                    return new String(arrayOfChar);
                }
                int k = arrayOfByte[i];
                int m = j + 1;
                arrayOfChar[j] = HEX_DIGIT[(0xF & k >>> 4)];
                j = m + 1;
                arrayOfChar[m] = HEX_DIGIT[(k & 0xF)];
                i++;
            }
        } catch (Exception localException) {
            localException.printStackTrace();
        }
        return null;
    }

}
