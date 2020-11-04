/**
 *****************************************************************************
 * Copyright (C) 2005-2014 UCWEB Corporation. All rights reserved
 * File        : DeviceUtil.java
 *
 * Description : For access hardware info.
 *
 * Creation    : 2014.1.7
 * Author      : huangyz@ucweb.com
 * History     : Creation, 2014.1.7, huangyz, Create the file
 *****************************************************************************
 **/
package com.uc.common.util.device;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.opengl.GLES10;
import android.os.Build;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.uc.common.util.log.Logger;
import com.uc.common.util.os.ApplicationContext;
import com.uc.common.util.os.SystemProperties;
import com.uc.common.util.text.StringUtil;

import java.io.File;
import java.net.NetworkInterface;
import java.util.Enumeration;

import javax.microedition.khronos.opengles.GL10;

public class DeviceUtil {
    private static final boolean DEBUG = false;
    private static final String TAG = "DeviceUtil";

    // SAFE_STATIC_VAR
    private static boolean sHasInitedAndroidId = false;
    // SAFE_STATIC_VAR
    private static String sAndroidId = "";
    // SAFE_STATIC_VAR
    private static boolean sHasInitIMEI = false;
    // SAFE_STATIC_VAR
    private static String sIMEI = "";

    private static boolean sHasInitMacAddress = false;
    private static String sMacAddress = "";
    /**
     * @return A 64-bit number (as a hex string) that is randomly generated on the device's first boot and 
     * should remain constant for the lifetime of the device. (The value may change if a factory reset is
     * performed on the device
     */
    public static String getAndroidId() {
        if (sHasInitedAndroidId) {
            return sAndroidId;
        }
        
        try {
            sAndroidId = Settings.Secure.getString(ApplicationContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        }
        
        if (sAndroidId == null) {
            sAndroidId = "";
        }
        sHasInitedAndroidId = true;
        if (DEBUG) {
            Logger.i(TAG, "getAndroidId: " + sAndroidId);
        }
        return sAndroidId;
    }

    /**
     * 获取IMEI, 获取失败返回null
     * @return if get IMEI failed, "" will be returned.
     */
    public static String getIMEI() {
        if (sHasInitIMEI) {
            return sIMEI;
        }
        sIMEI = getIMEIDirect();
        sHasInitIMEI = true;
        if (DEBUG) {
            Logger.i(TAG, "getIMEI: " + sIMEI);
        }
        return sIMEI;
    }

    private static String getIMEIDirect() {
        String imei = null;
        try {
            TelephonyManager telephonyMgr = (TelephonyManager) ApplicationContext
                    .getSystemService(Context.TELEPHONY_SERVICE);
            if (telephonyMgr != null) {
                imei = telephonyMgr.getDeviceId();
            }
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        }
        return imei;
    }

    /**
     * 获取国际移动用户识别码
     * 由于Imsi一般依赖sim卡，所以会发生变化，不允许缓存
     * @return
     */
    public static String getImsi() {
        String imsi = null;
        try {
            TelephonyManager telephonyMgr = (TelephonyManager) ApplicationContext.getSystemService(Context.TELEPHONY_SERVICE);
            if (null != telephonyMgr) {
                imsi = telephonyMgr.getSubscriberId();
            }
        }catch (Exception e){
//                ExceptionHandler.processFatalException(e);
        }
        return imsi;
    }

    /**
     * 获取OpenGL支持的最大纹理尺寸
     * @return
     */
    public static int getGlMaxTextureSize(){
        int[] maxTextureSize = new int[1];
        GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
        return maxTextureSize[0];
    }

    /**
     * 获取系统是否root了
     *
     * @return
     */
    public static boolean hasRoot() {
        File file = new File("system/bin/su");
        if (file.exists()) {
            return true;
        }
        file = new File("system/xbin/su");
        if (file.exists()) {
            return true;
        }
        return false;
    }

    /**
     * @see #acquireCPUWakeLock(Context, WakeLock, String, boolean, int)
     */
    public static WakeLock acquireCPUWakeLock(Context context, WakeLock wakeLock, String wakeLockTag,
            boolean refCounted) {
        return acquireCPUWakeLock(context, wakeLock, wakeLockTag, refCounted, 0);
    }

    /**
     * 获取唤醒锁
     * 如果传入的wakeLock参数为空，则会创建一个并尝试获取锁，返回创建的对象；
     * 如果传入的wakeLock参数不为空，则尝试获取锁，并返回传入的参数
     * @warn 外部调用需要自己加同步锁防止创建多个对象！
     */
    public static WakeLock acquireCPUWakeLock(Context context, WakeLock wakeLock, String wakeLockTag,
                                              boolean refCounted, long timeout) {
        if (wakeLock == null) {
            try {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wakeLockTag);
                wakeLock.setReferenceCounted(refCounted);
            } catch (Throwable e) {
//                ExceptionHandler.processFatalException(e);
            }
        }
        if (wakeLock != null) {
            if (DEBUG) {
                Logger.v(TAG, "Acquiring wakelock: " + wakeLockTag);
            }
            try {
                if (timeout > 0) {
                    wakeLock.acquire(timeout);
                }else {
                    wakeLock.acquire();
                }
            } catch (Throwable e) {
//                ExceptionHandler.processFatalException(e);
            }
        }
        return wakeLock;
    }
    
    public static void releaseCPUWakeLock(WakeLock wakeLock, String wakeLockTag) {
        if (wakeLock != null && wakeLock.isHeld()) {
            if (DEBUG) {
                Logger.v(TAG, "Releasing wakelock: " + wakeLockTag);
            }
            try {
                wakeLock.release();
            } catch (Throwable e) {
//                ExceptionHandler.processFatalException(e);
            }
        }
    }

    public static WakeLock acquireScreenWakeLock(Context context, WakeLock wakeLock, String wakeLockTag) {
        if (wakeLock == null) {
            try {
                PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP
                        | PowerManager.ON_AFTER_RELEASE, wakeLockTag);
            } catch (Throwable e) {
//                ExceptionHandler.processFatalException(e);
            }
        }
        if (wakeLock != null) {
            if (DEBUG) {
                Logger.v(TAG, "Acquiring wakelock: " + wakeLockTag);
            }
            try {
                wakeLock.acquire();
            } catch (Throwable e) {
//                ExceptionHandler.processFatalException(e);
            }
        }
        return wakeLock;


    }

    public static void releaseScreenWakeLock(WakeLock wakeLock, String wakeLockTag) {
        if (wakeLock != null && wakeLock.isHeld()) {
            if (DEBUG) {
                Logger.v(TAG, "Releasing wakelock: " + wakeLockTag);
            }
            try {
                wakeLock.release();
            } catch (Throwable e) {
//                ExceptionHandler.processFatalException(e);
            }
        }
    }

    public static String getMacAddress() {
        if (sHasInitMacAddress) {
            return sMacAddress;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                if (interfaces == null) return "";
                String wifiInterface = SystemProperties.get("wifi.interface", "wlan0");
                while (interfaces.hasMoreElements()) {
                    NetworkInterface iF = interfaces.nextElement();
                    byte[] addr = iF.getHardwareAddress();
                    if (addr == null || addr.length == 0) {
                        continue;
                    }
                    String name = iF.getName();
                    if (!wifiInterface.equalsIgnoreCase(name)) {
                        continue;
                    }
                    StringBuilder buf = new StringBuilder();
                    for (byte b : addr) {
                        buf.append(String.format("%02X:", b));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    String mac = buf.toString();

                    if (!StringUtil.isEmpty(mac)) {
                        sMacAddress = mac;
                        sHasInitMacAddress = true;
                        break;
                    }
                }
            } catch (Exception e) {
//                ExceptionHandler.processSilentException(e);
            }
        }

        if (StringUtil.isEmpty(sMacAddress)) {
            try {
                WifiManager wifi = (WifiManager) ApplicationContext.getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                sMacAddress = info.getMacAddress();
            } catch (Exception e) {
//                ExceptionHandler.processFatalException(e);
            }
        }

        if (sMacAddress == null) {
            sMacAddress = "";
        } else if (!TextUtils.isEmpty(sMacAddress)) {
            sHasInitMacAddress = true;
        }

        return sMacAddress;
    }

    public static int getMnc() {
        return ApplicationContext.getResources().getConfiguration().mnc;
    }

    public static int getMcc() {
        return ApplicationContext.getResources().getConfiguration().mcc;
    }
}
