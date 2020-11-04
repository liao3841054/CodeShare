/**
 * ******************************************************************
 * Copyright (C) 2005-2016 UC Mobile Limited. All Rights Reserved
 * Description : 将cpu相关的方法从HardwareUtil中抽离出来
 * Creation    : 2016/2/4
 * Author      : jiangyan.ljy@alibaba-inc.com
 * History     :
 * ******************************************************************
 **/
package com.uc.common.util.device;

import android.os.Build;
import android.text.TextUtils;

import com.uc.common.util.io.IOUtil;
import com.uc.common.util.log.Logger;
import com.uc.common.util.text.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;

public class CpuUtil {
    private static final boolean DEBUG = false;
    private static final String TAG = "CpuUtil";
    private static final String CPU_INFO_CORE_COUNT_FILE_PATH = "/sys/devices/system/cpu/";

    // SAFE_STATIC_VAR
    private static boolean sHasInitCpuCoreCount = false;
    // SAFE_STATIC_VAR
    private static int sCpuCoreCount = 1;
    // SAFE_STATIC_VAR
    private static boolean sHasInitMaxCpuFrequence = false;
    // SAFE_STATIC_VAR
    private static int sMaxCpuFrequence = -1;
    // SAFE_STATIC_VAR
    private static String sCpuArch = null;

    private static boolean sHasInitCpuInfo = false;
    private static String sCpuInfoArch = "";
    private static String sCpuInfoVfp = "";
    private static String sCpuArchit = "";

    public static int getCpuCoreCount() {
        if (sHasInitCpuCoreCount) {
            return sCpuCoreCount;
        }

        final class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                String path = pathname.getName();
                // the pattern is "cpu[0-9]+", regex is slow, so checking char by char.
                if (path.startsWith("cpu")) {
                    for (int i = 3; i < path.length(); i++) {
                        if (!Character.isDigit(path.charAt(i))) {
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            }
        }

        try {
            File dir = new File(CPU_INFO_CORE_COUNT_FILE_PATH);
            File[] files = dir.listFiles(new CpuFilter());
            sCpuCoreCount = files.length;
        } catch(Throwable e) {
//            ExceptionHandler.processFatalException(e);
        }

        if (sCpuCoreCount <= 1) {
            sCpuCoreCount = Runtime.getRuntime().availableProcessors();
        }
        sHasInitCpuCoreCount = true;
        if (DEBUG) {
            Logger.i(TAG, "getCpuCoreCount: " + sCpuCoreCount);
        }
        return sCpuCoreCount;
    }

    public static int getMaxCpuFrequence() {
        if (sHasInitMaxCpuFrequence) {
            return sMaxCpuFrequence;
        }

        final int coreCount = getCpuCoreCount();
        for (int i = 0; i < coreCount; i++) {
            File cpuInfoMaxFreqFile = new File("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
            if (cpuInfoMaxFreqFile.exists()) {
                FileReader fr = null;
                BufferedReader br = null;
                try {
                    fr = new FileReader(cpuInfoMaxFreqFile);
                    br = new BufferedReader(fr);
                    String text = br.readLine();
                    int freqBound = Integer.parseInt(text);
                    if (freqBound > sMaxCpuFrequence) {
                        sMaxCpuFrequence = freqBound;
                    }
                } catch (NumberFormatException e) {
                    // ignore
                } catch (IOException e) {
                    // ignore
                } finally {
                    IOUtil.safeClose(br);
                    IOUtil.safeClose(fr);
                }
            }
        }

        if (sMaxCpuFrequence < 0) {
            sMaxCpuFrequence = 0;
        }
        sHasInitMaxCpuFrequence = true;
        return sMaxCpuFrequence;
    }

    /**
     *
     Processor       : ARMv7 Processor rev 0 (v7l)
     processor       : 0
     BogoMIPS        : 996.14

     processor       : 1
     BogoMIPS        : 996.14

     Features        : swp half thumb fastmult vfp edsp vfpv3 vfpv3d16
     CPU implementer : 0x41
     CPU architecture: 7
     CPU variant     : 0x1
     CPU part        : 0xc09
     CPU revision    : 0

     Hardware        : star
     Revision        : 0000
     Serial          : 0000000000000000
     */
    private static void initCpuInfo() {
        if (sHasInitCpuInfo) {
            return;
        }
        BufferedReader bis = null;
        try {
            bis = new BufferedReader(new FileReader(new File("/proc/cpuinfo")));
            HashMap<String, String> cpuInfoMap = new HashMap<String, String>();
            String line;
            while ((line = bis.readLine()) != null) {
                line = line.trim();
                if (line.length() > 0) {
                    String[] pairs = line.split(":");
                    if (pairs.length > 1) {
                        cpuInfoMap.put(pairs[0].trim(), pairs[1].trim());
                    }
                }
            }

            String processor = cpuInfoMap.get("Processor");
            if(processor != null){
                int index1 = processor.indexOf("(");
                int index2 = processor.lastIndexOf(")");
                int len = index2 - index1;
                if(index1 > 0 && index2 > 0 &&len > 0){
                    sCpuInfoArch = processor.substring(index1+1, index2);
                }else{
                    sCpuInfoArch = "v"+cpuInfoMap.get("CPU architecture");
                }
            }
            sCpuInfoVfp = cpuInfoMap.get("Features");
            sCpuArchit = cpuInfoMap.get("CPU part");
            sHasInitCpuInfo = true;
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        } finally {
            IOUtil.safeClose(bis);
        }
    }

    /**
     *  从/proc/cpuinfo解释arch
     * @return
     */
    public static String getCpuInfoArch(){
        initCpuInfo();
        return sCpuInfoArch;
    }
    /**
     *  从/proc/cpuinfo解释archit
     * @return
     */
    public  static String getCpuInfoArchit(){
        initCpuInfo();
        return sCpuArchit;
    }

    /**
     * 从/proc/cpuinfo解释vfp
     * @return
     */
    public static String getCpuInfoVfp(){
        initCpuInfo();
        return sCpuInfoVfp;
    }

    private static final Object sGetCpuArchLock = new Object();

    /**
     * @ThreadSafe
     * 获取CPU架构信息, 统一返回小写
     * @return if get CPU arch failed, "" will be returned.
     */
    public static String getCpuArch() {
        if (sCpuArch != null) {
            return sCpuArch;
        }
        synchronized (sGetCpuArchLock) {
            if (sCpuArch != null) {
                return sCpuArch;
            }

            /**
             * 4.0-4.1上面Runtime.getRuntime().exec()会导致死锁，对这个区间的版本，仍然采用旧方式判断是否x86
             */
            final int ICE_CREAM_SANDWICH = 14;
            final int JELLY_BEAN = 16;
            if (Build.VERSION.SDK_INT < ICE_CREAM_SANDWICH || Build.VERSION.SDK_INT > JELLY_BEAN) {
                BufferedReader input = null;
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec("getprop ro.product.cpu.abi");
                    input = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    String strAbi = input.readLine();

                    if(strAbi != null && strAbi.contains("x86")) {
                        sCpuArch = "x86";
                    } else if (strAbi != null && strAbi.contains("armeabi-v7a")) {
                        sCpuArch = "armv7";
                    }
                } catch (Throwable e) {
                    // ignore
                } finally {
                    IOUtil.safeClose(input);

                    if (process != null) {
                        process.destroy();
                    }
                }
            }

            if (TextUtils.isEmpty(sCpuArch)) {
                //处理 armv5/6 和 mips，或者4.0-4.1版本，还是利用原来的方法
                try {
                    sCpuArch = System.getProperty("os.arch").toLowerCase(Locale.ENGLISH);
                    if (sCpuArch != null && sCpuArch.contains("i686")) {//对于用就方法获取的cpu架构，x86的值是i686，所以对于此值，会设置为x86
                        sCpuArch = "x86";
                    }
                } catch (Throwable e) {
                    // ignore
                }
            }

            if (sCpuArch == null) {
                sCpuArch = "";
            }
            if (DEBUG) {
                Logger.i(TAG, "getCpuArch: " + sCpuArch);
            }
            return sCpuArch;
        }
    }

    /**
     * 获取CPU架构前缀，即大致类别
     * @return
     */
    public static String getCpuArchPrefix() {
        String strArch = getCpuArch();
        if (strArch.startsWith("armv7")) {
            strArch = "arm7";
        } else if (strArch.startsWith("armv6")) {
            strArch = "arm6";
        } else if (strArch.startsWith("armv5")) {
            strArch = "arm5";
        } else if ("x86".equals(strArch) || "i686".equals(strArch)) {
            strArch = "x86";
        } else if ("mips".equals(strArch)) {
            strArch = "mips";
        }

        return strArch;
    }

    public static boolean isSupportAbi(String targetAbi) {
        if (StringUtil.isEmpty(targetAbi)) {
            return false;
        }
        String[] supportedAbis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        String targetAbiLower = targetAbi.toLowerCase(Locale.ENGLISH);
        for(String abi:supportedAbis){
            if(StringUtil.isNotEmptyWithTrim(abi) && abi.toLowerCase(Locale.ENGLISH).contains(targetAbiLower)){
                return true;
            }
        }
        return false;
    }

    /**
     * cpu架构是否适用armv7的包
     */
    public static boolean isArm7CpuArchCompatible(String strArch) {
        if (StringUtil.isEmpty(strArch)) {
            return false;
        }

        if (strArch.startsWith("armv7")//
                || strArch.startsWith("armv8")//
                || strArch.startsWith("arm64-v8")//
                || strArch.equals("aarch64")) {
            return true;
        }
        return false;
    }

    /**
     * cpu架构是否适用armv6的包
     */
    public static boolean isArm6CpuArchCompatible(String strArch) {
        if (StringUtil.isEmpty(strArch)) {
            return false;
        }

        if (strArch.startsWith("armv6")) {
            return true;
        }
        return false;
    }

    /**
     * cpu架构是否适用armv5的包
     */
    public static boolean isArm5CpuArchCompatible(String strArch) {
        if (StringUtil.isEmpty(strArch)) {
            return false;
        }

        if (strArch.startsWith("armv5")) {
            return true;
        }
        return false;
    }

    /**
     * cpu架构是否适用X86的包
     */
    public static boolean isX86CpuArchCompatible(String strArch) {
        if ("x86".equals(strArch) || "i686".equals(strArch)) {
            return true;
        }
        return false;
    }

    /**
     * 是否是mips cpu架构(不支持此类cpu)
     */
    public static boolean isMipsCpuArch(String strArch) {
        if ("mips".equals(strArch)) {
            return true;
        }
        return false;
    }
}
