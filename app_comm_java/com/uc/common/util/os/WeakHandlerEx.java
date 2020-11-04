package com.uc.common.util.os;

import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * 防止Handler对外应用引发的对外持有引用，导致内存泄漏的风险，我们可以选用本类以static形式内部类或独立类文件定义自己的Handler
 * 泛型参数是外部类需要调用的方法
 * 只需实现handle方法，handle方法中确保外部类实例不是空
 */
abstract public class WeakHandlerEx<T> extends HandlerEx {

    private WeakReference<T> mWeakReference;

    public WeakHandlerEx(String name, T outClass) {
        super(name);
        mWeakReference = new WeakReference<T>(outClass);
    }

    public WeakHandlerEx(String name, Callback callback, T outClass) {
        super(name, callback);
        mWeakReference = new WeakReference<T>(outClass);
    }

    public WeakHandlerEx(String name, Looper looper, T outClass) {
        super(name, looper);
        mWeakReference = new WeakReference<T>(outClass);
    }

    public WeakHandlerEx(String name, Looper looper, Callback callback, T outClass) {
        super(name, looper, callback);
        mWeakReference = new WeakReference<T>(outClass);
    }

    /**
     * 只有弱引用的持有还存在的时候才去执行handle
     */
    @Override
    final public void handleMessage(Message message) {
        T genericClass = mWeakReference.get();
        if (genericClass != null) {
            handle(message, genericClass);
        }
    }

    abstract public void handle(Message message, T outClass);


    @Override
    public String toString() {
        return "WeakHandlerEx (" + getName() + ") {}";
    }
}
