package com.uc.common.util.collection;

import java.util.Collection;


public class CollectionUtil {

    /**
     * An easy way to test a Collection is empty or not.
     *
     * @param container
     * @return {@code true} if the container is empty
     */
    public static boolean isEmpty(Collection<?> container) {
        return container == null || container.isEmpty();
    }

    /**
     *
     * @param inputCollection
     * @param predicate
     * @param outputCollection 当inputCollection和predicate都不为null时, 不能传null
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R extends Collection<? super T>> R select(Collection<? extends T> inputCollection, Predicate<? super T> predicate, R outputCollection) {
        if (inputCollection != null && predicate != null) {
            for (final T item : inputCollection) {
                if (predicate.evaluate(item)) {
                    outputCollection.add(item);
                }
            }
        }
        return outputCollection;
    }

    /*package*/ static <T> Collection<T> cast(Iterable<T> iterable) {
        return (Collection<T>) iterable;
    }
}
