package com.uc.common.util.lang;

import com.uc.common.util.text.StringUtil;

/**
 * ****************************************************************************
 * Copyright (C) 2005-2016 UCWEB Corporation. All rights reserved
 * File : 2016-10-09
 * <p>
 * 基本类型数据解析器，内部捕获{@link NumberFormatException}
 * </p>
 * Creation : 2016-10-09
 * Author   : chong3.lc3@alibaba-inc
 * History  : Creation, 2015-10-09, chong3.lc3, Create the file
 * ****************************************************************************
 */
public class NumberUtil {

    public static int toInt(final String value) {
        return toInt(value, 0);
    }

    public static int toInt(String value, int defaultValue) {
        int ret = defaultValue;
        if (StringUtil.isNotEmpty(value)) {
            final boolean tryHex = startsWith0x(value);
            try {
                if (tryHex) {
                    value = value.substring(2);
                    ret = Integer.parseInt(value, 16);
                } else {
                    ret = Integer.parseInt(value);
                }
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return ret;
    }

    public static long toLong(final String value) {
        return toLong(value, 0L);
    }

    public static long toLong(String value, long defaultValue) {
        long ret = defaultValue;
        if (StringUtil.isNotEmpty(value)) {
            final boolean tryHex = startsWith0x(value);
            try {
                if (tryHex) {
                    value = value.substring(2);
                    ret = Long.parseLong(value, 16);
                } else {
                    ret = Long.parseLong(value);
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    private static boolean startsWith0x(final String text) {
        return text.startsWith("0x");
    }

    public static float toFloat(String value, float defaultValue) {
        float ret = defaultValue;
        try {
            ret = Float.parseFloat(value);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static double toDouble(String value, double defaultValue) {
        double ret = defaultValue;
        try {
            ret = Double.parseDouble(value);
        }
        catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return ret;
    }

    public static double toDouble(String value) {
        return toDouble(value, 0.0);
    }

    public static Integer[] toIntegerArray(int[] intArray) {
        if (ArrayUtil.isEmpty(intArray)) {
            return null;
        }
        Integer[] ret = new Integer[intArray.length];
        for (int i = 0; i < intArray.length; i++) {
            ret[i] = intArray[i];
        }
        return ret;
    }

    public static int byteArrayBEToInt(byte[] bytes) {
        return bytes[3] & 0xff |
                (bytes[2] & 0xff) << 8 |
                (bytes[1] & 0xff) << 16 |
                (bytes[0] & 0xff) << 24;
    }

    public static byte[] intToByteArrayBE(int i) {
        return new byte[]{
                (byte) (i >>> 24),
                (byte) (i >>> 16),
                (byte) (i >>> 8),
                (byte) i};
    }

    /**
     * 判断字符串是否是只由0-9组成
     */
    public static boolean isIntOrLong(final String text) {
        if (StringUtil.isEmpty(text)) {// 空字符串肯定不是整形
            return false;
        }
        int index = 0;
        char ch = text.charAt(index);
        if (ch < '0') {// 第一个可能是符号
            if (ch == '-' || ch == '+') {
                index++;
            } else {// 第一个既不是数字也不是符号, 肯定不是整形
                return false;
            }
        }
        final int len = text.length();
        while (index < len) {
            ch = text.charAt(index);
            if (ch < '0' || ch > '9') {// 后面的只要不是数字, 就不是整形
                return false;
            }
            index++;
        }
        return true;
    }
}
