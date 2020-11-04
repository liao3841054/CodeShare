/*
 * Copyright (C) 2004 - 2017 UCWeb Inc. All Rights Reserved.
 * Description : ILogger
 *
 * Creation    : 2017-04-27
 * Author      : wx107452@alibaba-inc.com
 */
package com.uc.common.util.log;


public interface ILogger {
    int log(int priority, String tag, String msg);

    String getStackTraceString(Throwable tr);
}
