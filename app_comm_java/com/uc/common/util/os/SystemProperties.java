/**
 * Copyright (C) 2004 - 2015 UCWeb Inc. All Rights Reserved.
 * Description : SystemProperties,替代系统SystemProperties。
 * 反射系统API调用。
 * Creation    :  2016-10-11
 * Author      : andrewlu.lzw
 */

package com.uc.common.util.os;

import java.lang.reflect.Method;

public final class SystemProperties {
    private static Class<?> sSystemProperties;
    private static Method sGetMethod;
    private static Method sGetBooleanMethod;

    private static Class ensureClassInited() throws Exception {
        if (sSystemProperties == null) {
            sSystemProperties = Class.forName("android.os.SystemProperties");
        }
        return sSystemProperties;
    }

    public static String get(String key){
        return get(key,"");
    }

    public static String get(String key, String defValue) {
        try {
            ensureClassInited();
            if (sGetMethod == null) {
                sGetMethod = sSystemProperties.getDeclaredMethod("get", String.class, String.class);
            }
            return (String) sGetMethod.invoke(null, key, defValue);
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
            return defValue;
        }
    }

    public static boolean getBoolean(String key, boolean defValue) {
        try {
            ensureClassInited();
            if (sGetBooleanMethod == null) {
                sGetBooleanMethod = sSystemProperties.getDeclaredMethod("getBoolean", String.class, boolean.class);
            }
            return (Boolean) sGetBooleanMethod.invoke(null, key, defValue);
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
            return defValue;
        }
    }
}
