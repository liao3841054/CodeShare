/**
 ***************************************************************************** 
 * Copyright (C) 2005-2015 UCWEB Corporation. All rights reserved File :
 * 2015-2-5
 * 
 * Description : WeakList.java
 * 
 * Creation : 2015-2-5 Author : zhangsl@ucweb.com History : Creation, 2015-2-5,
 * zhangsl, Create the file
 ***************************************************************************** 
 */
package com.uc.common.util.collection;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * <p>WeakList is an decorator of {@link List} with elements are WeakReference. An
 * element will be removed when it is no longer referenced. referenced. Until
 * now basic operations (adding and removing) are supported.</p>
 * 
 * <p class="note">Note: the implementation of {@code WeakList} is not synchronized.</p>
 * 
 * @author zhangsl
 * 
 * @see WeakReference
 */
public class WeakList<E> {
    private List<WeakReference<E>> mList = null;
    private ReferenceQueue<Object> mDeadRefQueue = new ReferenceQueue<Object>();
    
    /**
     * Constructs a new instance of {@code WeakList} with {@link ArrayList}.
     */
    public WeakList () {
        mList = new ArrayList<WeakReference<E>>();
    }

    /**
     * <p>
     * Returns the number of elements in this {@code WeakList}.
     * </p>
     * 
     * <p class="note">
     * {@code WeakList} will remove none referenced elements, so the result of
     * {@link #size()} may change though you never execute adding or removing
     * operations.
     * </p>
     * 
     * @return the number of elements in this {@code List}.
     */
    public int size() {
        removeDeadRef();
        return mList.size();
    }

    /**
     * <p>
     * Return whether the list is empty.
     * </p>
     * 
     * <p class="note">
     * {@code WeakList} will remove none referenced elements, so the result of
     * {@link #isEmpty()()} may change though you never execute adding or
     * removing operations.
     * </p>
     * 
     * @return
     */
    public boolean isEmpty() {
        removeDeadRef();
        return mList.isEmpty();
    }

    /**
     * Tests whether this {@code WeakList} contains the specified object.
     * 
     * @param o
     *            the object to search for.
     * @return {@code true} if object is an element of this {@code WeakList},
     *         {@code false} otherwise
     */
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    /**
     * Adds the specified object at the end of this {@code WeakList}.
     * 
     * @param e
     *            the object to add.
     * @return <tt>true</tt> (as specified by {@link Collection#add})
     */
    public boolean add(E e) {
        return mList.add(new WeakReference<E>(e, mDeadRefQueue));
    }
    
    /**
     * Same to {@link List#add(int, Object)}
     */
    public void add(int index, E element) {
        WeakReference<E> ref = new WeakReference<E>(element, mDeadRefQueue);
        mList.add(index, ref);
    }
    
    /**
     * Returns the element at the specified position in this list.
     * 
     * @param index
     *            index of the element to return
     * @return element at the specified position in this list
     */
    public E get(int index) {
        WeakReference<E> ref = mList.get(index);
        return ref.get();
    }
    
    /**
     * Removes the first occurrence of the specified object from this
     * {@code WeakList}.
     * 
     * @param o
     *            the object to remove.
     * @return true if this {@code WeakList} was modified by this operation,
     *         false otherwise.
     */
    public boolean remove(Object o) {
        int index = indexOf(o);
        
        if (index >= 0) {
            mList.remove(index);
            return true;
        }
        
        return false;
    }
    
    /**
     * Removes the object at the specified location from this {@code WeakList}.
     * 
     * @param index
     *            the index of the object to remove.
     * @return the removed object.
     */
    public E remove(int index) {
        WeakReference<E> ref = mList.remove(index);
        return ref.get();
    }

    /**
     * Removes all elements from this {@code WeakList}, leaving it empty.
     */
    public void clear() {
        mList.clear();
    }
    
    /**
     * @return an array containing all elements contained in this {@code WeakList}.
     */
    public Object[] toArray() {
        removeDeadRef();
        int size = size();
        Object[] array = new Object[size];
        
        for (int i = 0; i < size; i ++) {
            WeakReference<E> ref = mList.get(i);
            array[i] = ref.get();
        }
        
        return array;
    }

    /**
     * Tests whether this {@code WeakList} contains all objects contained in the
     * specified collection.
     * 
     * @param collection
     *            the collection of objects
     * @return {@code true} if all objects in the specified collection are
     *         elements of this {@code WeakList}, {@code false} otherwise.
     */
    public boolean containsAll(Collection<?> collection) {
        removeDeadRef();
        boolean contain = true;
        @SuppressWarnings("unchecked")
        Iterator<? extends E> it = (Iterator<? extends E>) collection.iterator();
        while (it.hasNext()) {
            if (contains(it.next())) {
                contain = false;
            }
        }
        
        return contain;
    }

    /**
     * Adds the objects in the specified collection to the end of this
     * {@code WeakList}. The objects are added in the order in which they are
     * returned from the collection's iterator.
     * 
     * @param collection
     *            the collection of objects.
     * @return {@code true} if this {@code WeakList} is modified, {@code false}
     *         otherwise (i.e. if the passed collection was empty).
     */
    public boolean addAll(Collection<? extends E> collection) {
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            add(it.next());
        }
        return !collection.isEmpty();
    }

    /**
     * Inserts the objects in the specified collection at the specified location
     * in this {@code WeakList}. The objects are added in the order they are
     * returned from the collection's iterator.
     * 
     * @param index
     *            the index at which to insert.
     * @param collection
     *            the collection of objects to be inserted.
     * @return true if this {@code WeakList} has been modified through the
     *         insertion, false otherwise (i.e. if the passed collection was
     *         empty).
     */
    public boolean addAll(int index, Collection<? extends E> collection) {
        Iterator<? extends E> it = collection.iterator();
        while (it.hasNext()) {
            add(index++, it.next());
        }
        return !collection.isEmpty();
    }
    
    /**
     * Removes all occurrences in this {@code WeakList} of each object in the
     * specified collection.
     * 
     * @param collection
     *            the collection of objects to remove.
     * @return {@code true} if this {@code WeakList} is modified, {@code false}
     *         otherwise.
     */
    public boolean removeAll(Collection<?> collection) {
        removeDeadRef();
        boolean res = true;
        @SuppressWarnings("unchecked")
        Iterator<? extends E> it = (Iterator<? extends E>) collection.iterator();
        while (it.hasNext()) {
            if (!remove(it.next())) {
                res = false;
            }
        }
        return res;
    }

    /**
     * Removes all objects from this {@code WeakList} that are not contained in
     * the specified collection.
     * 
     * @param collection
     *            the collection of objects to retain.
     * @return {@code true} if this {@code WeakList} is modified, {@code false}
     *         otherwise.
     */
    public boolean retainAll(Collection<?> collection) {
        mList.clear();
        boolean res = true;
        @SuppressWarnings("unchecked")
        Iterator<? extends E> it = (Iterator<? extends E>) collection.iterator();
        while (it.hasNext()) {
            if (!add(it.next())) {
                res = false;
            }
        }
        return res;
    }
    
    /**
     * <p>
     * Searches this {@code WeakList} for the specified object and returns the index
     * of the first occurrence.
     * </p>
     * <p class="note">
     * {@code WeakList} will remove none referenced elements, so the result of
     * {@link #indexOf()} may change though you never execute adding or removing
     * operations.
     * </p>
     * 
     * @param obj
     *            the object to search for.
     * @return the index of the last occurrence of the object, or -1 if the
     *         object was not found.
     */
    public int indexOf(Object obj) {
        removeDeadRef();
        int index = -1;
        
        if (mList.isEmpty() || obj == null) {
            return index;
        }
        
        int size = mList.size();
        for (int i = 0; i < size; i ++) {
            WeakReference<E> reference = mList.get(i);
            E ref = reference.get();
            if (ref == obj) {
                index = i;
                break;
            }
        }
        
        return index;
    }

    /**
     * Searches this {@code WeakList} for the specified object and returns the index
     * of the last occurrence. </p>
     * <p class="note">
     * {@code WeakList} will remove none referenced elements, so the result of
     * {@link #lastIndexOf(Object)} may change though you never execute adding
     * or removing operations.
     * </p>
     * 
     * @param obj
     *            the object to search for.
     * @return the index of the last occurrence of the object, or -1 if the
     *         object was not found.
     */
    public int lastIndexOf(Object obj) {
        removeDeadRef();
        int index = -1;
        
        if (mList.isEmpty() || obj == null) {
            return index;
        }
        
        int size = mList.size();
        for (int i = size - 1; i >= 0; i ++) {
            WeakReference<E> reference = mList.get(i);
            E ref = reference.get();
            if (ref == obj) {
                index = i;
                break;
            }
        }
        
        return index;
    }

    
    @SuppressWarnings("unchecked")
    private void removeDeadRef() {
        Reference<Object> deadRef = null;
        while((deadRef = (Reference<Object>) mDeadRefQueue.poll()) != null) {
            mList.remove(deadRef);
        }
    }

}
