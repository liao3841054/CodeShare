package com.uc.common.util.lang;

import com.uc.common.util.concurrent.ThreadManager;

/**
 * Created by wx107452@alibaba-inc.com on 2017/4/26.
 */

public class GcUtil {
    private static class GcRunable implements Runnable {
        @Override
        public void run() {
            System.gc();
        }
    }

    private static Runnable sGcRunnable = new GcRunable();

    public static void gc(long delayMillis) {
        ThreadManager.postDelayed(ThreadManager.THREAD_BACKGROUND, sGcRunnable, delayMillis);
    }

    public static void cancelGc() {
        ThreadManager.removeRunnable(sGcRunnable);
    }
}
