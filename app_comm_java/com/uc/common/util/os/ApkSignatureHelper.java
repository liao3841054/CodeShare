/**
 *******************************************************************
 * Copyright (C) 2005-2014 UC Mobile Limited. All Rights Reserved
 * Description : apk 与 app获取签名辅助类
 * Creation    : 2015-01-09
 * Author      : chenzp@ucweb.com
 * History     : 
 *               Creation, 2015-01-09, chenzp, Create the file
 *******************************************************************
 **/

package com.uc.common.util.os;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

public class ApkSignatureHelper {
    public static String getApkFileSignature(String apkFile) {
        if (null == apkFile) {
            return null;
        }

        String ret = null;

        try{
            PackageInfo packageInfo = ApplicationContext.get().getPackageManager().getPackageArchiveInfo(apkFile,
                    PackageManager.GET_SIGNATURES);
            if (null != packageInfo) {
                Signature[] signs = packageInfo.signatures;
                if ((null != signs) && (0 < signs.length)) {
                    ret = signs[0].toCharsString();
                }
            }
        } catch(Exception e) {
            //DO NOTHING.
        }

        return ret;
    }

    public static String getInstalledAppSignature(String packageName) {
        if (null == packageName) {
            return null;
        }

        String ret = null;
        PackageInfo packageInfo = PackageUtil.getInstance().getPackageInfo(packageName,
                    PackageManager.GET_SIGNATURES);

        if (null != packageInfo) {
            Signature[] signs = packageInfo.signatures;
            ret = signs[0].toCharsString();
        }

        return ret;
    }
}
