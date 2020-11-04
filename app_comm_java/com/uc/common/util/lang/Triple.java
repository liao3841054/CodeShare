package com.uc.common.util.lang;

/**
 * Created by wx107452@alibaba-inc.com on 2017/6/1.
 */

public class Triple<F, S, T> {
    public final F first;
    public final S second;
    public final T third;

    public Triple(F first, S second, T third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Triple) {
            Triple<?, ?, ?> other = (Triple<?, ?, ?>) obj;
            return obJequals(first, other.first) && obJequals(second, other.second) && obJequals(third, other.third);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return (first == null ? 0 : first.hashCode()) ^ (second == null ? 0 : second.hashCode()) ^ (third == null ? 0 : third.hashCode());
    }

    @Override
    public String toString() {
        return "Triple{" + String.valueOf(first) + " " + String.valueOf(second) + " " + String.valueOf(third) + "}";
    }

    private static boolean obJequals(Object a, Object b) {
        return (a == b) || (a != null && a.equals(b));
    }
}
