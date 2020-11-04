package com.uc.common.util.os;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.DisplayMetrics;

import com.uc.common.util.lang.AssertUtil;

public class ApplicationContext {
    private static Context sAppContext;

    public static Resources getResources() {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext.getResources();
    }

    public static AssetManager getAssetManager() {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext.getAssets();
    }

    public static ContentResolver getContentResolver() {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext.getContentResolver();
    }

    public static Object getSystemService(String name) {
        if (null == name) {
            return null;
        }

        return sAppContext.getSystemService(name);
    }

    public static SharedPreferences getSharedPreferences(String name, int mode) {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext.getSharedPreferences(name, mode);
    }

    public static PackageManager getPackageManager() {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext.getPackageManager();
    }

    public static String getPackageName() {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext.getPackageName();
    }

    /**
     * This method return the global object of this application.
     *
     * @return context (this context can use in dialog)
     */
    public static Context get() {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext;
    }

    public static void init(Context context) {
        if (context == null) {
            return;
        }
        sAppContext = context;
    }

    public static DisplayMetrics getDisplayMetrics() {
        AssertUtil.mustNotNull(sAppContext, "initialize context first");
        return sAppContext.getResources().getDisplayMetrics();
    }
}
