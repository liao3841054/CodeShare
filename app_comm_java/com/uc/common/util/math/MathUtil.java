/**
 * ****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File			: 2013-5-16
 * <p>
 * Description	: MathUtil.java
 * <p>
 * Creation		: 2013-5-16
 * Author		: zhanhx@ucweb.com
 * History		: Creation, 2013-5-16, zhanhx, Create the file
 * ****************************************************************************
 */

package com.uc.common.util.math;

public final class MathUtil {
    /**
     * 如果 value 在 [lowerBound, upperBound] 区间内, 返回value
     * 如果value < lowerBound, 返回lowerBound
     * 如果value > upperBound, 返回upperBound
     * @param value 值
     * @param lowerBound 下界
     * @param upperBound 上界
     * @return
     */
    public static int correctRange(int value, int lowerBound, int upperBound) {
        if (lowerBound > upperBound) {
            throw new IllegalArgumentException("lowerBound <= upperBound");
        }
        if (value < lowerBound) {
            return lowerBound;
        } else if (value > upperBound) {
            return upperBound;
        } else {
            return value;
        }
    }

    public static boolean rangeIn(int val, int min, int max) {
        return val <= max && val >= min;
    }

    public static boolean rangeIn(long val, long min, long max) {
        return val <= max && val >= min;
    }
}
