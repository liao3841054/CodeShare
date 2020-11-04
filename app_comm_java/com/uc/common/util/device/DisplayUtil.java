package com.uc.common.util.device;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.uc.common.util.os.ApplicationContext;
import com.uc.common.util.reflect.ReflectionHelper;


public final class DisplayUtil {
    private static double sPhysicalScreenSize = 0;
    private static boolean sHasInitScreenSize = false;

    public static int getDensityDpi() {
        return ApplicationContext.getDisplayMetrics().densityDpi;
    }

    public static float getDensity() {
        return ApplicationContext.getDisplayMetrics().density;
    }

    public static float getScaleDensity() {
        return ApplicationContext.getDisplayMetrics().scaledDensity;
    }

    public static float getXdpi() {
        return ApplicationContext.getDisplayMetrics().xdpi;
    }

    public static float getYdpi() {
        return ApplicationContext.getDisplayMetrics().ydpi;
    }

    /**
     * 获取屏幕的宽度, 不一定是手机的短边
     * @return
     */
    public static int getScreenWidth() {
        return ApplicationContext.getDisplayMetrics().widthPixels;
    }

    /**
     * 获取屏幕的高度, 不一定是手机的长边
     * @return
     */
    public static int getScreenHeight() {
        return ApplicationContext.getDisplayMetrics().heightPixels;
    }

    public static int getScreenOrientation() {
        return ApplicationContext.getResources().getConfiguration().orientation;
    }

    /**
     * @return The screen backlight brightness between 0 and 255.
     */
    public static int getSystemBrightness() {
        int brightness = 0;
        ContentResolver contentResolver = ApplicationContext.getContentResolver();
        try {
            brightness = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, -1);
        } catch (Exception e) {
//            ExceptionHandler.processFatalException(e);
        }
        return brightness;
    }

    /**
     * 获取设备的短边
     * @return
     */
    public static int getDeviceWidth() {
        DisplayMetrics dm = ApplicationContext.getDisplayMetrics();
        return  Math.min(dm.widthPixels, dm.heightPixels);
    }

    /**
     * 获取设备的长边
     * @return
     */
    public static int getDeviceHeight() {
        DisplayMetrics dm = ApplicationContext.getDisplayMetrics();
        return  Math.max(dm.widthPixels, dm.heightPixels);
    }

    public static float getRefreshRate() {
        final WindowManager wm = (WindowManager) ApplicationContext.getSystemService(Context.WINDOW_SERVICE);
        float rate = wm.getDefaultDisplay().getRefreshRate();
        if (Math.abs(rate - 0.0f) < 0.1f) {
            rate = 60 * 1.0f;
        }

        return rate;
    }

    public static float convertSpToPixels(float sp) {
        return sp * getScaleDensity();
    }

    public static int convertDipToPixels(float dips) {
        return (int) (dips * getDensity() + 0.5f);
    }

    /**
     * 单位inch
     * @return
     */
    public static double getScreenInchSize() {
        if (sHasInitScreenSize) {
            return sPhysicalScreenSize;
        }
        WindowManager wm = (WindowManager) ApplicationContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            ReflectionHelper.invokeMethod(display, "getRealMetrics", new Class[] {DisplayMetrics.class}, new Object[] {dm});
        } else {
            // 工具库不支持4.0以下
            display.getMetrics(dm);
        }
        int widthPixel = dm.widthPixels;
        int heightPixel = dm.heightPixels;
        double screenWidth = (double) widthPixel / dm.xdpi;
        double screenHeight = (double) heightPixel / dm.ydpi;
        sPhysicalScreenSize = Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2));
        sHasInitScreenSize = true;
        return sPhysicalScreenSize;
    }
}
