/*
 * Copyright (C) 2004 - 2017 UCWeb Inc. All Rights Reserved.
 * Description : Logger Logcat 实现
 *
 * Creation    : 2017-04-27
 * Author      : wx107452@alibaba-inc.com
 */
package com.uc.common.util.log;

import android.util.Log;


/*package*/ class LogcatLoggerImpl implements ILogger {
    @Override
    public int log(int priority, String tag, String msg) {
        if (tag == null || msg == null) {
            return 0;
        }
        return Log.println(priority, tag, msg);
    }

    @Override
    public String getStackTraceString(Throwable tr) {
        return Log.getStackTraceString(tr);
    }
}
