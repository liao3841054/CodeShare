package com.uc.common.util.os;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewParent;
import android.widget.AbsListView;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/3.
 */

public class ViewUtil {
    private static final String SCROLLBAR_DRAWABLE_SET_VERTICAL_THUMB = "setVerticalThumbDrawable";
    private static final String SCROLLBAR_DRAWABLE_SET_VERTICAL_TRACK = "setVerticalTrackDrawable";
    private static final String SCROLLBAR_DRAWABLE_SET_HORIZONTAL_THUMB = "setHorizontalThumbDrawable";
    private static final String SCROLLBAR_DRAWABLE_SET_HORIZONTAL_TRACK = "setHorizontalTrackDrawable";

    public static boolean setScrollbarVerticalThumbDrawable(View view, Drawable newDrawable) {
        return setScrollbarDrawable(view, newDrawable, SCROLLBAR_DRAWABLE_SET_VERTICAL_THUMB);
    }

    public static boolean setScrollbarVerticalTrackDrawable(View view, Drawable newDrawable) {
        return setScrollbarDrawable(view, newDrawable, SCROLLBAR_DRAWABLE_SET_VERTICAL_TRACK);
    }

    public static boolean setScrollbarHorizontalThumbDrawable(View view, Drawable newDrawable) {
        return setScrollbarDrawable(view, newDrawable, SCROLLBAR_DRAWABLE_SET_HORIZONTAL_THUMB);
    }

    public static boolean setScrollbarHorizontalTrackDrawable(View view, Drawable newDrawable) {
        return setScrollbarDrawable(view, newDrawable, SCROLLBAR_DRAWABLE_SET_HORIZONTAL_TRACK);
    }

    private static boolean setScrollbarDrawable(View view, Drawable newDrawable, String methodName) {
        try {
            Object scrollBarDrawable = getScrollBarDrawableObject(view);
            if (scrollBarDrawable != null) {
                Class<?> scrollBarDrawableClass = scrollBarDrawable.getClass();
                Method setVerticalThumbDrawableMethod = scrollBarDrawableClass.getMethod(methodName, Drawable.class);
                setVerticalThumbDrawableMethod.invoke(scrollBarDrawable, newDrawable);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            //
            return false;
        }
    }

    private static Object getScrollBarDrawableObject(View view) {
        try {
            Class<?> viewClass = View.class;
            Field viewScrollCacheField = viewClass.getDeclaredField("mScrollCache");
            viewScrollCacheField.setAccessible(true);
            Object scrollCache = viewScrollCacheField.get(view);
            if (scrollCache == null) return null;

            Class<?> scrollabilityCacheClass = scrollCache.getClass();
            Field scrollBarDrawableField = scrollabilityCacheClass.getDeclaredField("scrollBar");
            scrollBarDrawableField.setAccessible(true);
            Object scrollBarDrawable = scrollBarDrawableField.get(scrollCache);
            return scrollBarDrawable;
        } catch (Exception e) {
            //
            return null;
        }
    }

    private final static String EDGEGLOW_LEFT = "mEdgeGlowLeft";
    private final static String EDGEGLOW_TOP = "mEdgeGlowTop";
    private final static String EDGEGLOW_RIGHT = "mEdgeGlowRight";
    private final static String EDGEGLOW_BOTTOM = "mEdgeGlowBottom";

    @Deprecated
    public static boolean setEdgeEffectDrawable(ScrollView view, Drawable edgeDrawable, Drawable glowDrawable) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return false;
        }
        Class<?> viewClass = ScrollView.class;
        boolean result = true;
        result &= setEdgeEffectDrawable(view, viewClass, EDGEGLOW_TOP, edgeDrawable, glowDrawable);
        result &= setEdgeEffectDrawable(view, viewClass, EDGEGLOW_BOTTOM, edgeDrawable, glowDrawable);
        return result;
    }

    @Deprecated
    public static boolean setEdgeEffectDrawable(HorizontalScrollView view, Drawable edgeDrawable, Drawable glowDrawable) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return false;
        }
        Class<?> viewClass = HorizontalScrollView.class;
        boolean result = true;
        result &= setEdgeEffectDrawable(view, viewClass, EDGEGLOW_LEFT, edgeDrawable, glowDrawable);
        result &= setEdgeEffectDrawable(view, viewClass, EDGEGLOW_RIGHT, edgeDrawable, glowDrawable);
        return result;
    }

    @Deprecated
    public static boolean setEdgeEffectDrawable(AbsListView view, Drawable edgeDrawable, Drawable glowDrawable) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            return false;
        }
        Class<?> viewClass = AbsListView.class;
        boolean result = true;
        result &= setEdgeEffectDrawable(view, viewClass, EDGEGLOW_TOP, edgeDrawable, glowDrawable);
        result &= setEdgeEffectDrawable(view, viewClass, EDGEGLOW_BOTTOM, edgeDrawable, glowDrawable);
        return result;
    }

    /**
     * Android 5.0起这种修改方法不再有效，也不需要了
     */
    private static boolean setEdgeEffectDrawable(View view, Class<?> viewClass, String edgeName, Drawable edgeDrawable, Drawable glowDrawable) {
        if (glowDrawable == null || edgeDrawable == null) {
            return false;
        }
        try {
            Field edgeEffectField = viewClass.getDeclaredField(edgeName);
            edgeEffectField.setAccessible(true);
            Object edgeEffect = edgeEffectField.get(view);

            Class<?> edgeEffectClass = edgeEffect.getClass();

            Field mEdgeField = edgeEffectClass.getDeclaredField("mEdge");
            mEdgeField.setAccessible(true);
            mEdgeField.set(edgeEffect, edgeDrawable);

            Field mGlowField = edgeEffectClass.getDeclaredField("mGlow");
            mGlowField.setAccessible(true);
            mGlowField.set(edgeEffect, glowDrawable);
            return true;
        } catch (Exception e) {
            //
            return false;
        }
    }

    public static void setCursorDrawable(TextView textView, int cursorDrawableResId, Drawable newDrawable) {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                setCursorDrawableRes(textView, cursorDrawableResId);
                Object scrollBarDrawableArray = getCursorDrawableArrayObject(textView);
                if (scrollBarDrawableArray == null) return;
                Array.set(scrollBarDrawableArray, 0, newDrawable);
                Array.set(scrollBarDrawableArray, 1, newDrawable);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private static void setCursorDrawableRes(TextView textView, int drawableResId) {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                Class<?> textViewClass = android.widget.TextView.class;
                Field cursorDrawableResField = textViewClass.getDeclaredField("mCursorDrawableRes");
                cursorDrawableResField.setAccessible(true);
                cursorDrawableResField.set(textView, drawableResId);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private static Object getCursorDrawableArrayObject(TextView textView) {
        if (Build.VERSION.SDK_INT >= 11) {
            try {
                Class<?> textViewClass = android.widget.TextView.class;
                Field textViewEditorField = textViewClass.getDeclaredField("mEditor");
                textViewEditorField.setAccessible(true);
                Object editor = textViewEditorField.get(textView);
                if (editor == null) return null;

                Class<?> editorClass = editor.getClass();
                Field cursorDrawableArrayField = editorClass.getDeclaredField("mCursorDrawable");
                cursorDrawableArrayField.setAccessible(true);
                Object scrollBarDrawableArray = cursorDrawableArrayField.get(editor);
                return scrollBarDrawableArray;
            } catch (Exception e) {
                //
                return null;
            }
        }
        return null;
    }

    public static boolean getChildLocationRelativeToAncestors(View ancestors, View child, int[] outLoc) {
        if (child == null || ancestors == null || outLoc == null)
            return false;
        outLoc[0] = child.getLeft();
        outLoc[1] = child.getTop();
        ViewParent parent = child.getParent();
        while (parent instanceof View && parent != ancestors) {
            View view = (View) parent;
            outLoc[0] += view.getLeft();
            outLoc[1] += view.getTop();
            parent = view.getParent();
        }
        if (parent == ancestors) {
            return true;
        } else {
            return false;
        }
    }

    // SAFE_STATIC_VAR
    private static Paint paint;
    // SAFE_STATIC_VAR
    private static TextPaint textPaint;

    public static String cutStringForWidth(String target, int textsize,
                                           int limitedWidth, TextUtils.TruncateAt ellipsisType) {
        if (paint == null) {
            paint = new Paint();
        }
        if (textPaint == null) {
            textPaint = new TextPaint();
        }
        if (target == null) {
            return null;
        }
        if (target.length() == 0) {
            return target;
        }
        if (limitedWidth <= 0) {
            return null;
        }
        paint.setTextSize(textsize);
        textPaint.set(paint);
        target = String.valueOf(TextUtils.ellipsize(target, textPaint, limitedWidth, ellipsisType));
        return target;
    }

    /**
     * 获取文本宽度
     */
    public static int getStringWidth(float textSize, String srcStr) {
        if (textSize > 0 && srcStr != null) {
            if (paint == null) {
                paint = new Paint();
            }
            paint.setTextSize(textSize);
            return (int) paint.measureText(srcStr);
        }
        return 0;
    }
}
