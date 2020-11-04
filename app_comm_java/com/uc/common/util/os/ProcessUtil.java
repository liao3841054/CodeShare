/**
 * Copyright (C) 2004 - 2015 UCWeb Inc. All Rights Reserved.
 * Description :
 * Create      : 2016/7/12
 * Author      : zhangxun@ucweb.com
 */

package com.uc.common.util.os;

import android.app.ActivityManager;
import android.content.Context;
import android.text.TextUtils;

import com.uc.common.util.io.IOUtil;
import com.uc.common.util.text.StringUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Process Util
 */
public class ProcessUtil {

    private static String sCurrentProcessName;

    /**
     * 获取当前进程名字
     */
    public static String getCurrentProcessName() {
        String processName = sCurrentProcessName;// 临时变量用于防止多次非同步读造成的可见性问题
        if (TextUtils.isEmpty(processName)) {
            processName = getProcessName(android.os.Process.myPid());
            sCurrentProcessName = processName;
        }
        return processName;
    }

    /**
     * get Process Name
     *
     * @param pid
     * @return
     */
    public static String getProcessName(int pid) {
        String result = null;
        ActivityManager am = (ActivityManager) ApplicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps != null) {
            for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
                if (procInfo.pid == pid) {
                    result = procInfo.processName;
                    break;
                }
            }
        }
        if (StringUtil.isEmpty(result)) {
            BufferedReader cmdlineReader = null;
            try {
                cmdlineReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + pid + "/cmdline"), "iso-8859-1"));
                result = cmdlineReader.readLine();
                if (result != null) {
                    result = result.trim();
                }
            } catch (IOException e) {
                // ignore
            } finally {
                IOUtil.safeClose(cmdlineReader);
            }
        }
        if (StringUtil.isEmpty(result)) {
            result = "unknown";
        }
        return result;
    }

    private static Boolean sIsMainProcess;

    public static boolean isMainProcess() {
        Boolean isMainProcess = sIsMainProcess;// 临时变量用于防止多次非同步读造成的竞争问题
        if (isMainProcess == null) {
            isMainProcess = ApplicationContext.getPackageName().equals(getCurrentProcessName());
            sIsMainProcess = isMainProcess;
        }
        return isMainProcess;
    }

    public static boolean isProcessAlive(String process) {
        ActivityManager am = (ActivityManager) ApplicationContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo procInfo : runningApps) {
            return StringUtil.equals(procInfo.processName, process);
        }
        return false;
    }
}
