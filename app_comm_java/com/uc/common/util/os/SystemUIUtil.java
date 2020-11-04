package com.uc.common.util.os;

import android.app.Activity;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by wx107452@alibaba-inc.com on 2017/4/26.
 */

public class SystemUIUtil {

    private static int sStatusBarHeight;
    private static boolean sHasCheckStatusBarHeight;

    public static int getStatusBarHeight() {
        if (sHasCheckStatusBarHeight) {
            return sStatusBarHeight;
        }
        int result = -1;
        int resourceId = ApplicationContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = ApplicationContext.getResources().getDimensionPixelSize(resourceId);
        }
        if (result == -1) {
            result = guessStatusBarHeight();
        }
        if (result < 0) {
            result = 0;
        }
        sHasCheckStatusBarHeight = true;
        sStatusBarHeight = result;
        return result;
    }

    private static int guessStatusBarHeight() {
        try {
            final int statusBarHeightDP = 25;
            float density = ApplicationContext.getResources().getDisplayMetrics().density;
            return Math.round(density * statusBarHeightDP);
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        }
        return 0;
    }

    public static void configTransparentStatusBar(Window window) {
        if (window != null) {
            window.setFlags(0x04000000, 0x04000000);
        }
    }

    public static void configTransparentStatusBar(WindowManager.LayoutParams lp) {
        if (lp != null) {
            int flags = 0x04000000;
            int mask = 0x04000000;
            lp.flags = (lp.flags & ~mask) | (flags & mask);
        }
    }


    public static boolean isSystemFullScreen(Activity activity) {
        if (activity == null) {
            return false;
        }
        boolean isSystemFullscreen = false;
        Window window = activity.getWindow();
        if (window != null && window.getAttributes() != null) {
            int flag = window.getAttributes().flags;
            if ((flag & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
                isSystemFullscreen = true;
            } else if ((flag & WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN) {
                isSystemFullscreen = false;
            }
        }
        return isSystemFullscreen;
    }

}
