package com.uc.common.util.lang;

public class Boxing {
    /**
     * 获得object指向的Boolean对象的值.直接强转可能出现空指针
     *
     * @param o
     * @return
     */
    public static boolean unboxing(Object o, boolean defoutValue) {
        if (!(o instanceof Boolean)) {
            return defoutValue;
        }
        return (Boolean) o;
    }

    public static int unboxing(Object o, int defoutValue) {
        if (!(o instanceof Integer)) {
            return defoutValue;
        }
        return (Integer) o;
    }

    public static long unboxing(Object o, long defautValue) {
        if (o instanceof Long) {
            return (long) o;
        }
        return defautValue;
    }
}
