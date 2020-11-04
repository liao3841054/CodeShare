/**
 *****************************************************************************
 * Copyright (C) 2005-2015 UCWEB Corporation. All rights reserved
 * File        : AssertUtil.java
 *
 * Description : AssertUtil.java
 *
 * Creation    : 2014-10-24
 * Author      : guozm@ucweb.com
 * History     : Creation, 2014-10-24, guozm, Create the file
 *****************************************************************************
 */
package com.uc.common.util.lang;

import android.os.Looper;

import com.uc.common.util.text.StringUtil;

public class AssertUtil {

    public interface IAssert {
        void assertDie(String msg);
    }

    private static IAssert sAssertImpl;

    public static void setAssertImpl(IAssert assertImpl) {
        sAssertImpl = assertImpl;
    }

    public static final void mustOk(boolean aOk) {
        mustOk(aOk, null);
    }

    public static final void mustOk(boolean aOk, Object aErrMsg) {
        if (!aOk) {
            if (null != aErrMsg) {
                assertDie(aErrMsg.toString());
            } else {
                assertDie();
            }
        }
    }

    public static void mustNotNull(Object aObj) {
        mustNotNull(aObj, null);
    }

    public static void mustNotNull(Object aObj, String aErrMsg) {
        mustOk(null != aObj, aErrMsg);
    }

    public static void fail(String aMsg) {
        mustOk(false, aMsg);
    }

    public static void fail() {
        fail(null);
    }

    public static void mustInNonUiThread() {
        mustInNonUiThread(null);
    }
    public static void mustInUiThread() {
        mustInUiThread(null);
    }

    public static void mustInNonUiThread(String aErrMsg) {
        mustOk(!isMainThread(), aErrMsg);
    }

    public static void mustInUiThread(String aErrMsg) {
        mustOk(isMainThread(), aErrMsg);
    }

    private static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static void mustNotEmpty(String aStr) {
        mustOk(StringUtil.isNotEmpty(aStr));
    }
    
    private static void assertDie() {
        assertDie(null);
    }
    
    private static void assertDie(String msg) {
        final IAssert impl = sAssertImpl;
        if (impl != null) {
            impl.assertDie(msg);
        }
    }
}

