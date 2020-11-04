/*
 * Copyright (C) 2004 - 2017 UCWeb Inc. All Rights Reserved.
 * Description : Logger
 *
 * Creation    : 2017-04-27
 * Author      : wx107452@alibaba-inc.com
 */
package com.uc.common.util.log;

import android.util.Log;

import com.uc.common.util.text.StringUtil;

public class Logger {
    private final static char LF = '\n';

    public final static int LOG_VERBOSE = Log.VERBOSE; // verbose级别
    public final static int LOG_DEBUG = Log.DEBUG; // debug级别
    public final static int LOG_INFO = Log.INFO; // info 级别
    public final static int LOG_WARNING = Log.WARN; // warning 级别
    public final static int LOG_ERROR = Log.ERROR; // error级别

    private static ILogger sLoggerImpl;

    public static void setLoggerImpl(ILogger logger) {
        sLoggerImpl = logger;
    }

    public static String getStackTraceString(Throwable tr) {
        final ILogger impl = sLoggerImpl;
        if (impl != null) {
            return impl.getStackTraceString(tr);
        } else {
            return null;
        }
    }

    public static int v(String tag, String msg) {
        return log(LOG_VERBOSE, tag, msg);
    }


    public static int v(String tag, String msg, Throwable tr) {
        return log(LOG_VERBOSE, tag, msg + LF + getStackTraceString(tr));
    }

    public static int d(String tag, String msg) {
        return log(LOG_DEBUG, tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return log(LOG_DEBUG, tag, msg + LF + getStackTraceString(tr));
    }

    public static int i(String tag, String msg) {
        return log(LOG_INFO, tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return log(LOG_INFO, tag, msg + LF + getStackTraceString(tr));
    }

    public static int w(String tag, String msg) {
        return log(LOG_WARNING, tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return log(LOG_WARNING, tag, msg + LF + getStackTraceString(tr));
    }

    public static int e(String tag, String msg) {
        return log(LOG_ERROR, tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return log(LOG_ERROR, tag, msg + LF + getStackTraceString(tr));
    }

    private static int log(int priority, String tag, String msg) {
        final ILogger impl = sLoggerImpl;
        if (impl != null) {
            return impl.log(priority, tag, msg);
        }
        return 0;
    }

    public static void logLongStr(String tag, String str) {
        if (StringUtil.isEmpty(str)) {
            return;
        }
        final int maxLength = 4000;
        if (str.length() <= maxLength) {
            d(tag, str);
            return;
        }
        int index = 0;
        String sub;
        while (index < str.length()) {
            // java的字符不允许指定超过总的长度end
            if (str.length() <= index + maxLength) {
                sub = str.substring(index);
            } else {
                sub = str.substring(index, index + maxLength);
            }

            index += maxLength;
            d(tag, sub);
        }
    }
}
