package com.uc.common.util.device;

import android.app.Activity;
import android.app.ActivityManager;
import android.text.TextUtils;

import com.uc.common.util.io.IOUtil;
import com.uc.common.util.os.ApplicationContext;
import com.uc.common.util.text.StringUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/16.
 */

public class MemoryUtil {
    private static final String MEMORY_INFO_PATH = "/proc/meminfo";
    private static final String MEMORY_OCUPIED_INFO_PATH = "/proc/self/status";
    private static final String MEMORY_INFO_TAG_VM_RSS = "VmRSS:";
    private static final String MEMORY_INFO_TAG_VM_DATA = "VmData:";

    // SAFE_STATIC_VAR
    private static boolean sHasInitTotalMemory = false;
    // SAFE_STATIC_VAR
    private static long sTotalMemory = 0;
    /**
     * @return device total memory (KB)
     */
    public static long getTotalMemory() {
        if (sHasInitTotalMemory) {
            return sTotalMemory;
        }

        final int bufferSize = 8192; //设置一个缓存大小
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(MEMORY_INFO_PATH);
            br = new BufferedReader(fr, bufferSize);
            String memory = br.readLine(); // 读取meminfo第一行，系统总内存大小  , 得到类似"MemTotal:  204876 kB"的string
            if (memory != null) {
                String[] arrayOfString = memory.split("\\s+");
                if (arrayOfString != null && arrayOfString.length > 1 && arrayOfString[1] != null) {
                    sTotalMemory = Long.parseLong(arrayOfString[1].trim());// 获得系统总内存，单位是KB
                }
            }
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        } finally {
            IOUtil.safeClose(fr);
            IOUtil.safeClose(br);
        }

        if (sTotalMemory < 0) {
            sTotalMemory = 0;
        }
        sHasInitTotalMemory = true;
        return sTotalMemory;
    }

    /**
     * @return device free memory (KB)
     */
    public static int getFreeMemory() {
        int memory = 0;
        FileInputStream is = null;
        try{
            File fpath = new File(MEMORY_INFO_PATH);
            if (!fpath.exists()) {
                return memory;
            }

            final int BUFFER_LENGTH = 1024;
            byte buffer [] = new byte[BUFFER_LENGTH];
            is = new FileInputStream(fpath);
            int length = is.read(buffer);
            length = length >= BUFFER_LENGTH ? BUFFER_LENGTH : length;
            buffer[length - 1] = '\0';
            String str = new String(buffer);

            memory += getMemorySizeFromMemInfo(str, "MemFree:");
            memory += getMemorySizeFromMemInfo(str, "Buffers:");
            memory += getMemorySizeFromMemInfo(str, "Cached:");

        } catch (Throwable ta) {
//            ExceptionHandler.processFatalException(ta);
        } finally {
            IOUtil.safeClose(is);
        }

        return memory < 0 ? 0 : memory;
    }

    private static int getMemorySizeFromMemInfo(String infoStr, String keyWord) {

        if (StringUtil.isEmptyWithTrim(infoStr) || StringUtil.isEmptyWithTrim(keyWord)) {
            return 0;
        }

        int memSize = 0;
        int idxStart = infoStr.indexOf(keyWord);
        if (idxStart >= 0) {
            idxStart += keyWord.length();
            int idxEnd = infoStr.indexOf("kB", idxStart);
            if (idxEnd >= 0) {
                String mem = infoStr.substring(idxStart, idxEnd).trim();
                memSize = Integer.parseInt(mem);
            }
        }
        return memSize;
    }

    /**
     * @return 通过/proc/<pid>/status读取程序占用的物理内存总量（RSS） 单位：Kbyte
     */
    public static int getOcupiedRssMemory() {
        return getOcupiedMemory(MEMORY_INFO_TAG_VM_RSS);
    }

    /**
     * @return Size of "data" segment (KB)
     */
    public static int getOcupiedDataMemory() {
        return getOcupiedMemory(MEMORY_INFO_TAG_VM_DATA);
    }


    private static int getOcupiedMemory(String tag) {
        if (TextUtils.isEmpty(tag)) {
            return 0;
        }

        int ocupied = 0;
        FileInputStream is = null;
        try {
            File file = new File(MEMORY_OCUPIED_INFO_PATH);
            if(!file.exists()) {
                return ocupied;
            }

            final int BUFFER_LENGTH = 1000;
            byte buffer[] = new byte[BUFFER_LENGTH];
            is = new FileInputStream(file);
            int length = is.read(buffer);
            buffer[length] = '\0';
            String str = new String(buffer);
            int idxStart = str.indexOf(tag); //找到tag所在的位置
            if (idxStart >= 0) {
                idxStart += 7;
                int idxEnd = str.indexOf("kB", idxStart); //找到第一个kB的位置
                if (idxEnd >= 0) {
                    String memory = str.substring(idxStart, idxEnd).trim();
                    ocupied = Integer.parseInt(memory);
                }
            }
        } catch (Throwable ta) {
//            ExceptionHandler.processFatalException(ta);
        } finally {
            IOUtil.safeClose(is);
        }

        return ocupied;
    }

    /**
     * 通过Runtime和Debug类获取程序占用的Java堆内存大小
     *
     * @return 单位：byte
     */
    public static long getJavaHeapSize() {
        return Runtime.getRuntime().totalMemory();
    }

    public static long getAvailableMemory() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) ApplicationContext.getSystemService(Activity.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        long availableMegs = mi.availMem / 1048576L;
        return availableMegs;
    }
}
