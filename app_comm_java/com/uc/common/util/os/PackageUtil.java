/**
 *****************************************************************************
 * Copyright (C) 2005-2015 UCWEB Corporation. All rights reserved
 * File        : PackageUtil.java
 *
 * Description : 统一处理判断包是否安装的逻辑，减少获取包信息的IPC调用次数，提升性能
 *
 * Creation    : 2015.2.28
 * Author      : caisq@ucweb.com
 * History     :
 *****************************************************************************
 **/

package com.uc.common.util.os;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;

import com.uc.common.util.concurrent.ThreadManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PackageUtil {
    private static final String ANDROID_CALENDAR_PACKAGE_NAME = "com.android.calendar";
    private static InstalledReceiver mInstalledReceiver = new InstalledReceiver();
    private static PackageUtil mInstance;
    private static List<PackageInfo> mPackageInfoList;
    private static final Object mSyncObj = new Object();
    
    private static class InstalledReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")
                || intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")) {
                
                ThreadManager.execute(new Runnable() {
                    @Override
                    public void run() {
                        updateAllInstalledPackageInfo();
                    }
                });
            }
        }
    }

    public static synchronized PackageUtil getInstance() {
        if (mInstance == null) {
            mInstance = new PackageUtil();
            
            updateAllInstalledPackageInfo();
            registerReceiver(ApplicationContext.get(), mInstalledReceiver);
        }
        
        return mInstance;
    }
    
    private static void registerReceiver(Context context, BroadcastReceiver installedReceiver) {
        if (context == null || installedReceiver == null) return;
        
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");  
          
        context.registerReceiver(installedReceiver, filter);  
    }

    private static void updateAllInstalledPackageInfo() {
        PackageManager packageManager = ApplicationContext.get().getPackageManager();
        synchronized (mSyncObj) {
            try {
                mPackageInfoList = packageManager.getInstalledPackages(0);
            } catch (Throwable t) {
//                ExceptionHandler.processFatalException(t);
            }
        }
    }
    
    /**
     * 
     * @param packageName
     * @return 返回查询包的PackageInfo，如果返回null表示这个包未安装，返回非null则表示这个包已经安装
     */
    public boolean isInstalled(String packageName) {
        return getPackageInfo(packageName) != null;
    }
    
    public PackageInfo getPackageInfo(String packageName) {
        if (packageName == null || mPackageInfoList == null) return null;
        
        synchronized (mSyncObj) {
            for (int i = 0; i < mPackageInfoList.size(); i++) {  
                PackageInfo info = (PackageInfo)mPackageInfoList.get(i);  
                if (packageName.equals(info.packageName)) {
                    return info;
                }  
            }
        }
        return null;
    }
    
    public PackageInfo getPackageInfo(String packageName, int flags) {
        if (flags == 0) {
            return getPackageInfo(packageName);
        }
        
        try {
            PackageManager packageManager = ApplicationContext.get().getPackageManager();
            PackageInfo info = packageManager.getPackageInfo(packageName, flags);
            return info;
        } catch (NameNotFoundException e) {
//            ExceptionHandler.processFatalException(e);
        }
        
        return null;
    }
    
    public List<PackageInfo> getAllInstalledPackageInfo() {
        ArrayList<PackageInfo> list = null;
        synchronized (mSyncObj) {
            int size = mPackageInfoList != null ? mPackageInfoList.size() : 0;
            list = new ArrayList<PackageInfo>(size);
            if (mPackageInfoList != null) {
                for (PackageInfo p : mPackageInfoList) {
                    if (p != null) {
                        list.add(p);
                    }
                }
            }
        }
        return list;
    }

    public int getVersionCode() throws NameNotFoundException {
        PackageInfo info = getPackageInfo(ApplicationContext.get().getPackageName());
        if (info == null) {
            throw new NameNotFoundException();
        }
        return info.versionCode;
    }

    /**
     * Get the version name from AndroidManifest.
     * 
     * @return null if fail to fetch.
     */
    public String getVersionName() throws NameNotFoundException
    {
        PackageInfo info =  getPackageInfo(ApplicationContext.get().getPackageName());
        if (info == null) {
            throw new NameNotFoundException();
        }
        return info.versionName;
    }

    /**
     * 获取首次安装的时间戳
     * @return
     */
    public long getFirstInstallTime(){
        PackageInfo info = getPackageInfo(ApplicationContext.get().getPackageName());
        if (info == null) {
            return -1;
        }
        return info.firstInstallTime;
    }

    public long getLastUpdateTime() {
        PackageInfo info = getPackageInfo(ApplicationContext.get().getPackageName());
        if (info == null) {
            return -1;
        }
        return info.lastUpdateTime;
    }

    public boolean isSystemPackage(PackageInfo packageInfo) {
        if (null == packageInfo) {
            return false;
        }

        PackageInfo calendarPackage = getPackageInfo(ANDROID_CALENDAR_PACKAGE_NAME);
        if (calendarPackage != null && calendarPackage.firstInstallTime == packageInfo.firstInstallTime) {
            return true;
        }
        if (packageInfo.applicationInfo != null && (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Return application icon bitmap by package name.
     *
     * @param packageName Request application package name.
     * @return If the application got by package name is valid, then return the icon,
     * else return null.
     */
    public static Bitmap getInstalledAppIcon(String packageName) {
        if (packageName == null || "".equals(packageName.trim())) {
            return null;
        }

        PackageManager pm = ApplicationContext.getPackageManager();
        try {
            Drawable drawable = pm.getApplicationIcon(packageName);
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            }
        } catch (Throwable e) {
//            ExceptionHandler.processFatalException(e);
        }
        return null;
    }

    public static boolean installApkFile(String filePath) {
        if (filePath == null || "".equals(filePath.trim())) {
            return false;
        }

        try {
            File apkFile = new File(filePath);
            if (!apkFile.exists()) {
                return false;
            }

            Intent i = new Intent();
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            i.setAction(Intent.ACTION_VIEW);
            i.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            ApplicationContext.get().startActivity(i);
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
            return false;
        }
        return true;
    }

    public static boolean uninstallPackage(String packageName) {
        if (packageName == null || "".equals(packageName.trim())) {
            return false;
        }

        try {
            Uri packageURI = Uri.parse("package:" + packageName);
            Intent i = new Intent(Intent.ACTION_DELETE, packageURI);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ApplicationContext.get().startActivity(i);
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
            return false;
        }
        return true;
    }

    /**
     * 应用安装位置
     */
    public static final int INSTALL_LOCATION_INTERNAL = 0;
    public static final int INSTALL_LOCATION_EXTERNAL = 1; // app2sd
    public static final int INSTALL_LOCATION_ADOPTABLE_STORAGE = 2;
    public static final int INSTALL_LOCATION_UNKNOWN = 3;

    public static int getInstallLocation() {
        ApplicationInfo info = ApplicationContext.get().getApplicationInfo();
        if ((info.flags & ApplicationInfo.FLAG_EXTERNAL_STORAGE) == 0) {
            return INSTALL_LOCATION_INTERNAL;
        }
        if (info.dataDir.startsWith(Environment.getDataDirectory().getAbsolutePath())) {
            return INSTALL_LOCATION_EXTERNAL;
        }
        if (info.dataDir.startsWith("/mnt/expand/")) { // hardcode, see Environment#getDataDirectory(String)
            return INSTALL_LOCATION_ADOPTABLE_STORAGE;
        }
        return INSTALL_LOCATION_UNKNOWN;
    }

    public static final int APK_STATE_NOT_INSTALL = 0;
    public static final int APK_STATE_INSTALLED = 1;
    public static final int APK_STATE_UPGRADE = 2;

    public int getApkInstallState(String apkFilePath) {
        int state = APK_STATE_NOT_INSTALL;
        PackageManager packageManager = ApplicationContext.get().getPackageManager();
        PackageInfo packageInfo = packageManager.getPackageArchiveInfo(apkFilePath, 0);


        if (packageInfo != null) {
            String packageName = packageInfo.packageName;
            PackageInfo installPackageInfo = getPackageInfo(packageName);
            if (installPackageInfo != null) {
                state = APK_STATE_INSTALLED;
                int tempVersionCode = packageInfo.versionCode;    //文件解析出来的version code
                int installVersionCode = installPackageInfo.versionCode;
                if (installVersionCode < tempVersionCode) {
                    state = APK_STATE_UPGRADE;
                }
            }
        }

        return state;
    }
}
