package com.uc.common.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by wx107452@alibaba-inc.com on 2017/5/9.
 */

public class ListUtil {
    public static <E> ArrayList<E> asArrayList(E...array) {
        if (array == null) {
            return new ArrayList<E>();
        }
        ArrayList<E> list = new ArrayList<>(computeArrayListCapacity(array.length));
        Collections.addAll(list, array);
        return list;
    }

    public static <T> ArrayList<T> asArrayList(Collection<? extends T> source) {
        if (CollectionUtil.isEmpty(source)) {
            return new ArrayList<T>();
        }
        ArrayList<T> list = new ArrayList<>(computeArrayListCapacity(source.size()));
        list.addAll(source);
        return list;
    }

    public static <E> ArrayList<E> select(final Collection<? extends E> inputCollection,
                                     final Predicate<? super E> predicate) {
        return CollectionUtil.select(inputCollection, predicate, new ArrayList<E>(inputCollection.size()));
    }

    private static int computeArrayListCapacity(int arraySize) {
        int value = arraySize + 5 + arraySize / 10;// 随便算的一个值, 略大于arraySize
        if (value < 0) {
            value = arraySize;
        }
        return value;
    }

    /**
     * 去掉数组中的空元素
     * @param l
     */
    public static void trim(List l){
        if(l == null || l.size() <= 0){
            return;
        }
        for(int i = 0; i < l.size() ;i++){
            if(l.get(i) == null){
                l.remove(i);
                i = i -1;
            }
        }
    }
    /*package*/ static <T> List<T> cast(Iterable<T> iterable) {
        return (List<T>) iterable;
    }
}
