/**
 * ****************************************************************************
 * Copyright (C) 2005-2014 UCWEB Inc. All rights reserved
 * Description:随机数字帮助类
 * Creation, 16-12-8, ww98911@alibaba-inc.com, Create the file
 * *****************************************************************************
 */
package com.uc.common.util.lang;

import java.util.Random;

public class RandomUtil {
    private static final Random RANDOM = new Random();

    public static boolean nextBoolean() {
        return RANDOM.nextBoolean();
    }

    public static byte[] nextBytes(final int count) {
        checkArgument(count >= 0, "Count cannot be negative.");
        final byte[] result = new byte[count];
        RANDOM.nextBytes(result);
        return result;
    }

    public static int nextInt(final int startInclusive, final int endExclusive) {
        checkArgument(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        checkArgument(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endExclusive) {
            return startInclusive;
        }

        return startInclusive + RANDOM.nextInt(endExclusive - startInclusive);
    }

    /**
     * <p> Returns a random int within 0 - Integer.MAX_VALUE </p>
     * @return
     */
    public static int nextInt() {
        return nextInt(0, Integer.MAX_VALUE);
    }

    public static long nextLong(final long startInclusive, final long endExclusive) {
        checkArgument(endExclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        checkArgument(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endExclusive) {
            return startInclusive;
        }

        return (long) nextDouble(startInclusive, endExclusive);
    }

    /**
     * <p> Returns a random long within 0 - Long.MAX_VALUE </p>
     */
    public static long nextLong() {
        return nextLong(0, Long.MAX_VALUE);
    }

    public static double nextDouble(final double startInclusive, final double endInclusive) {
        checkArgument(endInclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        checkArgument(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endInclusive) {
            return startInclusive;
        }

        return startInclusive + ((endInclusive - startInclusive) * RANDOM.nextDouble());
    }

    /**
     * <p> Returns a random double within 0 - Double.MAX_VALUE </p>
     */
    public static double nextDouble() {
        return nextDouble(0, Double.MAX_VALUE);
    }

    public static float nextFloat(final float startInclusive, final float endInclusive) {
        checkArgument(endInclusive >= startInclusive, "Start value must be smaller or equal to end value.");
        checkArgument(startInclusive >= 0, "Both range values must be non-negative.");

        if (startInclusive == endInclusive) {
            return startInclusive;
        }

        return startInclusive + ((endInclusive - startInclusive) * RANDOM.nextFloat());
    }

    /**
     * <p> Returns a random float within 0 - Float.MAX_VALUE </p>
     */
    public static float nextFloat() {
        return nextFloat(0, Float.MAX_VALUE);
    }

    private static void checkArgument(boolean cond, String desc) {
        if (!cond) {
            throw new IllegalArgumentException(desc);
        }
    }
}