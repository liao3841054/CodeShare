/**
 * ****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File			: 2013-8-1
 * 
 * Description	: NetworkUtil.java
 * 
 * Creation		: 2013-8-1 
 * Author		: zhanhx@ucweb.com 
 * History		: Creation, 2013-8-1, zhanhx, Create the file
 * 				: Modification, 2014-07-22, zhangyf merge NetworkDetector & NetworkUtil
 * ****************************************************************************
 */

package com.uc.common.util.net;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.uc.common.util.concurrent.ThreadManager;
import com.uc.common.util.lang.NumberUtil;
import com.uc.common.util.log.Logger;
import com.uc.common.util.os.ApplicationContext;
import com.uc.common.util.reflect.ReflectionHelper;
import com.uc.common.util.text.StringUtil;

import java.util.List;

public class NetworkUtil {
    /**
     * 接入点类型：
     * 0：未知(默认)
     * 1：2G
     * 2：2.5G
     * 3：2.75G
     * 4：3G
     * 5：wifi接入点
     * 6：4G
     * -1: 无网络
     **/
    public static final int NETWORK_CLASS_UNKNOWN = 0;
    public static final int NETWORK_CLASS_2G = 1;
    public static final int NETWORK_CLASS_2_5G = 2;
    public static final int NETWORK_CLASS_2_75G = 3;
    public static final int NETWORK_CLASS_3G = 4;
    public static final int NETWORK_CLASS_WIFI = 5;
    public static final int NETWORK_CLASS_4G = 6;
    public static final int NETWORK_CLASS_NO_NETWORK = -1; // 无网络或网络未连接

    /**
     * 接入点名称
     */
    public static final String NETWORK_CLASS_NAME_NO_NETWORK  = "-1";
    public static final String NETWORK_CLASS_NAME_UNKNOWN = "0";
    public static final String NETWORK_CLASS_NAME_WIFI = "WIFI";
    public static final String NETWORK_CLASS_NAME_2G = "2G";
    public static final String NETWORK_CLASS_NAME_2_5G = "2.5G";
    public static final String NETWORK_CLASS_NAME_2_75G = "2.75G";
    public static final String NETWORK_CLASS_NAME_3G = "3G";
    public static final String NETWORK_CLASS_NAME_4G = "4G";
    public static final String NETWORK_CLASS_NAME_UNKNOWN_PREFIX = "UNKNOWN";

    // 这部分在低版本的SDK中没有定义,但高版本有定义,
    // 由于取值与其它常量不冲突,因此直接在这里定义
    private static final int NETWORK_TYPE_GSM = 16;
    private static final int NETWORK_TYPE_TD_SCDMA = 17;
    private static final int NETWORK_TYPE_IWLAN = 18;
    // 以下部分为高版本的隐藏API定义
    private static final int NETWORK_TYPE_LTE_CA = 19;

    private volatile static NetworkInfo mCachedNetworkInfo;
    private volatile static BroadcastReceiver mNetworkChangeReceiver;


    /**
     * 推荐的网络类型判断方法
     * 
     * @return 返回值可以枚举：{@link #NETWORK_CLASS_UNKNOWN}、{@link #NETWORK_CLASS_2G}、
     *         {@link #NETWORK_CLASS_2_5G}、{@link #NETWORK_CLASS_2_75G}、
     *         {@link #NETWORK_CLASS_3G}、{@link #NETWORK_CLASS_4G}、
     *         {@link #NETWORK_CLASS_WIFI}、{@link #NETWORK_CLASS_NO_NETWORK}
     */
    public static int getNetworkClass() {
        int networkClass = getNetworkClassImpl();
        switch (networkClass) {
            case NETWORK_CLASS_2G:
            case NETWORK_CLASS_2_5G:
            case NETWORK_CLASS_2_75G:
            case NETWORK_CLASS_3G:
            case NETWORK_CLASS_4G:
            case NETWORK_CLASS_WIFI:
            case NETWORK_CLASS_NO_NETWORK:
                return networkClass;
            case NETWORK_CLASS_UNKNOWN:
            default:
                return NETWORK_CLASS_UNKNOWN; // 高版本新增的网络类型会归入这个类别
        }
    }

    /**
     * @note 推荐的获取网络类型名称的方法。可以用于统计或后台业务，不能用于字符串比较。
     *       对于高版本新增的网络类型，会返回{@value #NETWORK_CLASS_NAME_UNKNOWN_PREFIX}拼接数字的形式
     */
    public static String getNetworkClassName() {
        int networkClass = getNetworkClassImpl();
        switch (networkClass) {
            case NETWORK_CLASS_2G:
                return NETWORK_CLASS_NAME_2G;
            case NETWORK_CLASS_2_5G:
                return NETWORK_CLASS_NAME_2_5G;
            case NETWORK_CLASS_2_75G:
                return NETWORK_CLASS_NAME_2_75G;
            case NETWORK_CLASS_3G:
                return NETWORK_CLASS_NAME_3G;
            case NETWORK_CLASS_4G:
                return NETWORK_CLASS_NAME_4G;
            case NETWORK_CLASS_WIFI:
                return NETWORK_CLASS_NAME_WIFI;
            case NETWORK_CLASS_NO_NETWORK:
                return NETWORK_CLASS_NAME_NO_NETWORK;
            case NETWORK_CLASS_UNKNOWN:
                return NETWORK_CLASS_NAME_UNKNOWN;
            default:
                return NETWORK_CLASS_NAME_UNKNOWN_PREFIX + networkClass;
        }
    }

    /**
     * @return 网络是否连通。
     * @note 等同于 ({@link #getNetworkClass()} != {@link #NETWORK_CLASS_NO_NETWORK})
     */
    public static boolean isNetworkConnected() {
        return getActiveNetworkInfo() != null;
    }

    public static boolean isWifiNetwork() {
        return getNetworkClass() == NETWORK_CLASS_WIFI;
    }

    /**
     * 是否是移动网络
     */
    public static boolean isMobileNetwork() {
        int networkClass = getNetworkClass();
        return networkClass != NETWORK_CLASS_WIFI && networkClass != NETWORK_CLASS_UNKNOWN
                && networkClass != NETWORK_CLASS_NO_NETWORK;
    }

    /**
     * @see NetworkInfo#getType()
     * @return 如果当前无activeNetwork,返回 -1
     */
    public static int getCurrentNetworkType() {
        NetworkInfo info = getActiveNetworkInfo();
        if (null == info) {
            return -1;
        }
        return info.getType();
    }

    /**
     * @see NetworkInfo#getTypeName()
     * @return 如果当前无activityNetwork，返回 ""
     */
    public static String getCurrentNetworkTypeName() {
        NetworkInfo info = getActiveNetworkInfo();
        String name = null;

        if (null != info) {
            name = info.getTypeName();
        }

        if (null == name){
            name = "";
        }

        return name;
    }

    public static boolean isWifiTurnOn() {
        WifiManager wifiManager = (WifiManager) ApplicationContext.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager == null) {
            return false;
        }
        return wifiManager.isWifiEnabled();
    }

    public static boolean isMobileTurnOn() {
        ConnectivityManager conMgr = (ConnectivityManager) ApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (conMgr == null) {
            return false ;
        }
        return (Boolean) ReflectionHelper.invokeMethod(conMgr, "getMobileDataEnabled", null, null);
    }

    /**
     * 获取网络信号强度（目前只做wifi）
     * @return 一个0到-100的区间值，其中0到-50表示信号最好，-50到-70表示信号偏差，小于-70表示最差，有可能连接不上或者掉线。
     */
    public static int getNetworkRssi() {
        final WifiManager wm = (WifiManager) ApplicationContext.getSystemService(Context.WIFI_SERVICE);
        if (wm != null) {
            WifiInfo info = wm.getConnectionInfo();
            if (info != null) {
                return info.getRssi();
            }
        }
        return -1;
    }

    public static String getWiFiBSSID() {
        String bssid = null;
        try {
            WifiManager wifi = (WifiManager) ApplicationContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null) {
                bssid = info.getBSSID();
            }
        } catch (Exception e) {
            // ignore
        }

        return bssid;
    }

    public static String getWiFiSSID() {
        String ssid = null;
        try {
            WifiManager wifi = (WifiManager) ApplicationContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            if (info != null) {
                ssid = info.getSSID();
            }
        } catch (Exception e) {
            // ignore
        }

        return ssid;
    }

    /**
     * 判断当前wifi是否有加密（安全）
     * @return true(加密网络) or false(非加密网络)
     */
    public static boolean checkCurWifiIsSecurity() {
        try {
            WifiManager wifiService = (WifiManager) ApplicationContext.getSystemService(Context.WIFI_SERVICE);

            WifiConfiguration wifiConfig = null;
            List<WifiConfiguration> wifiList = wifiService.getConfiguredNetworks();

            if (wifiList != null) {
                int listNum = wifiList.size();
                for (int i = 0; i < listNum; i++) {
                    WifiConfiguration conf = wifiList.get(i);
                    if (conf.status == WifiConfiguration.Status.CURRENT) {
                        wifiConfig = conf;
                        break;
                    }
                }

                if (wifiConfig == null) {
                    for (int i = 0; i < listNum; i++) {
                        WifiConfiguration conf = wifiList.get(i);
                        String confStr = conf.toString();
                        String label = "LinkAddresses: [";
                        int off = confStr.indexOf(label);
                        if (off > 0) {
                            off += label.length();
                            if (confStr.indexOf("]", off) > off) {
                                wifiConfig = conf;
                                break;
                            }
                        }
                    }
                }
            }

            if (wifiConfig != null) {
                if (!StringUtil.isEmptyWithTrim(wifiConfig.preSharedKey)
                        || !StringUtil.isEmptyWithTrim(wifiConfig.wepKeys[0])
                        || !StringUtil.isEmptyWithTrim(wifiConfig.wepKeys[1])
                        || !StringUtil.isEmptyWithTrim(wifiConfig.wepKeys[2])
                        || !StringUtil.isEmptyWithTrim(wifiConfig.wepKeys[3])) {

                    return true;
                }
            }
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        }
        return false;
    }


    /**
     * 此方法作为网络类型判断的核心，在TelephonyManager.getNetworkClass(int)基础上进行细化， <b>只用于
     * {@link #getNetworkClass()}和{@link #getNetworkClassName()}的实现，其他地方不要使用</b>
     * 
     * @return 返回值除了{@link #NETWORK_CLASS_UNKNOWN}、{@link #NETWORK_CLASS_2G}、
     *         {@link #NETWORK_CLASS_2_5G}、{@link #NETWORK_CLASS_2_75G}、
     *         {@link #NETWORK_CLASS_3G}、{@link #NETWORK_CLASS_4G}、
     *         {@link #NETWORK_CLASS_WIFI}、{@link #NETWORK_CLASS_NO_NETWORK}
     *         外，对于移动网络未被列举的类型， 一律返回{@link NetworkInfo#getSubtype()}的值
     */
    private static int getNetworkClassImpl() {
        NetworkInfo activeNetwork = getActiveNetworkInfo();
        if (activeNetwork == null) {
            return NETWORK_CLASS_NO_NETWORK;
        }

        int netType = activeNetwork.getType();
        if (netType == ConnectivityManager.TYPE_WIFI) {
            return NETWORK_CLASS_WIFI;
        }

        int netSubType = activeNetwork.getSubtype();
        switch (netSubType) {
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_IDEN:
            case NETWORK_TYPE_GSM:
                return NETWORK_CLASS_2G;
            case TelephonyManager.NETWORK_TYPE_GPRS:
                return NETWORK_CLASS_2_5G;
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return NETWORK_CLASS_2_75G;
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_TD_SCDMA:
                return NETWORK_CLASS_3G;
            case TelephonyManager.NETWORK_TYPE_LTE:
            case NETWORK_TYPE_IWLAN:
            case NETWORK_TYPE_LTE_CA:
                return NETWORK_CLASS_4G;
            case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                return NETWORK_CLASS_UNKNOWN;
            default:
                return netSubType; // 有可能是高版本新增的网络类型，如果发现了再补充到上面的switch-case中
        }
    }

    public static NetworkInfo getActiveNetworkInfo() {
        registerNetworkChangeReceiver();
        if (!ThreadManager.isMainThread()) { // 考虑到准确性要求与主线程性能，允许子线程查询并更新缓存
            mCachedNetworkInfo = getActiveNetworkInfoImpl();
        }
        return mCachedNetworkInfo;
    }

    private static boolean isNetworkConnected(NetworkInfo networkInfo) {
        return networkInfo != null && networkInfo.isConnected();
    }

    private static void registerNetworkChangeReceiver() {
        if (mNetworkChangeReceiver == null) {
            synchronized (NetworkUtil.class) {
                if (mNetworkChangeReceiver == null) {
                    mCachedNetworkInfo = getActiveNetworkInfoImpl(); // 首次需要主动赋值
                    mNetworkChangeReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            // 在主线程进行以避免数据不一致，但网络频繁变化时可能造成卡顿
                            mCachedNetworkInfo = getActiveNetworkInfoImpl();
                        }
                    };
                    // Android 7.0起不允许在AnidroidManifest里面静态注册这个广播，但运行时注册仍然是允许的
                    IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
                    ApplicationContext.get().registerReceiver(mNetworkChangeReceiver, intentFilter);
                }
            }
        }
    }

    /**
     * 获得当前使用网络的信息<br/>
     * 即是连接的网络，如果用系统的api得到的activeNetwork为null<br/>
     * 我们还会一个个去找，以适配一些机型上的问题
     *
     * @warn 同步查询当前可用网络接入点，由于是耗时行为，尽量不要直接调用此方法
     */
    private static NetworkInfo getActiveNetworkInfoImpl() {
        NetworkInfo activeNetwork = null;
        try {
            ConnectivityManager cm = (ConnectivityManager) ApplicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) {
                Logger.w("NetworkUtil", "ConnectivityManager==null");
                return null;
            }

            activeNetwork = cm.getActiveNetworkInfo();
            if (isNetworkConnected(activeNetwork)) {
                return activeNetwork;
            }

            // 当前无可用连接,或者没有连接,尝试取所有网络再进行判断一次
            activeNetwork = null;
            NetworkInfo[] allNetworks = cm.getAllNetworkInfo();
            if (allNetworks != null) {
                for (NetworkInfo networkInfo : allNetworks) {
                    if (isNetworkConnected(networkInfo)) {
                        activeNetwork = networkInfo;
                        break;
                    }
                }
            }
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        }

        return activeNetwork;
    }

    public static String getProxyHost() {
        String proxyHost = null;

        // Proxy
        if (Build.VERSION.SDK_INT >= 11) {
            // Build.VERSION_CODES.ICE_CREAM_SANDWICH IS_ICS_OR_LATER
            proxyHost = System.getProperty("http.proxyHost");
        } else {
            proxyHost = android.net.Proxy.getHost(ApplicationContext.get());

            // wifi proxy is unreachable in Android2.3 or lower version
            if (isWifiNetwork() && proxyHost != null && proxyHost.indexOf("10.0.0") != -1) {
                proxyHost = "";
            }
        }

        return proxyHost;
    }

    public static int getProxyPort() {
        int proxyPort = 80;

        // Proxy
        if (Build.VERSION.SDK_INT >= 11) {
            // Build.VERSION_CODES.ICE_CREAM_SANDWICH IS_ICS_OR_LATER
            String portStr = System.getProperty("http.proxyPort");
            proxyPort = NumberUtil.toInt(portStr, -1);
        } else {
            String proxyHost = null;
            proxyHost = android.net.Proxy.getHost(ApplicationContext.get());
            proxyPort = android.net.Proxy.getPort(ApplicationContext.get());

            // wifi proxy is unreachable in Android2.3 or lower version
            if (isWifiNetwork() && proxyHost != null && proxyHost.indexOf("10.0.0") != -1) {
                proxyPort = -1;
            }
        }

        return proxyPort;
    }
}
