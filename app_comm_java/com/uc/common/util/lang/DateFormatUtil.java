package com.uc.common.util.lang;

import com.uc.common.util.concurrent.ThreadManager;

import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Created by wx107452@alibaba-inc.com on 2017/4/26.
 */

public class DateFormatUtil {

    private static HashMap<String, SimpleDateFormat> mSimpleDateFormatCache = new HashMap<String, SimpleDateFormat>();

    public static SimpleDateFormat getSimpleDateFormat(String format) {
        if (!ThreadManager.isMainThread()) {
            return new SimpleDateFormat(format);
        }

        SimpleDateFormat sdf = mSimpleDateFormatCache.get(format);
        if (sdf == null) {
            sdf = new SimpleDateFormat(format);
            mSimpleDateFormatCache.put(format, sdf);
        }

        return sdf;
    }

}
