package com.uc.common.util.collection;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/9.
 */

public interface Predicate<T> {
    boolean evaluate(T object);
}
