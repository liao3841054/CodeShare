/**
*****************************************************************************
* Copyright (C) 2005-2015 UCWEB Corporation. All rights reserved
* File        : BackgroundHandler
*
* Description :
*
* Creation    : 2014
* Author      : caisq@ucweb.com
*****************************************************************************
**/

package com.uc.common.util.concurrent;

import android.os.Build;
import android.os.Debug;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.MessageQueue;
import android.os.MessageQueue.IdleHandler;
import android.os.Process;

import com.uc.common.util.device.CpuUtil;
import com.uc.common.util.log.Logger;
import com.uc.common.util.os.HandlerEx;
import com.uc.common.util.reflect.ReflectionHelper;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadManager {
    //SAFE_STATIC_VAR
    private static HandlerThread sBackgroundThread;
    //SAFE_STATIC_VAR
    private static HandlerEx sBackgroundHandler;
    //SAFE_STATIC_VAR
    private static HandlerThread sWorkThread;
    //SAFE_STATIC_VAR
    private static HandlerEx sWorkHandler;
    //SAFE_STATIC_VAR
    private static HandlerThread sNormalThread;
    //SAFE_STATIC_VAR
    private static HandlerEx sNormalHandler;
    // 最小线程数量，兼容旧逻辑最小线程数以防止死锁
    private static final int THREAD_POOL_SIZE = Math.max(CpuUtil.getCpuCoreCount() + 2, 5);
    //SAFE_STATIC_VAR
    private static ExecutorService mThreadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    //SAFE_STATIC_VAR
    private static HandlerEx mMainThreadHandler;
    //SAFE_STATIC_VAR
    private static HandlerEx sMonitorHandler;
    
    private static HashMap<Object, RunnableMap> mRunnableCache = new HashMap<Object, RunnableMap>();
    
    private static final long RUNNABLE_TIME_OUT_TIME =  30 * 1000;
        
    //后台线程优先级的线程
    public static final int THREAD_BACKGROUND = 0;
    //介于后台和默认优先级之间的线程
    public static final int THREAD_WORK = 1;
    //主线程
    public static final int THREAD_UI = 2;
    //和主线程同优先级的线程
    public static final int THREAD_NORMAL = 3;
    // just for internal purpose
    private static final int THREAD_IDLE_INTERNAL = 1024;

    private static boolean sDebugMode = false;
    public static void debugMode() {
        sDebugMode = true;
        enableMonitor();
    }

    private static void enableMonitor() {
        if (sMonitorHandler == null) {
            HandlerThread thread = new HandlerThread("MonitorThread", android.os.Process.THREAD_PRIORITY_BACKGROUND + android.os.Process.THREAD_PRIORITY_MORE_FAVORABLE);
            thread.start();
            sMonitorHandler = new HandlerEx("MonitorThread", thread.getLooper());
        }
    }

    private ThreadManager() {}

    /**
     * 将Runnable放入线程池执行
     * 默认为后台优先级执行，如果需要调优先级请使用execute(runnable, callback, priority)调用
     * @param runnable
     */
    public static void execute(final Runnable runnable) {
        execute(runnable, null, android.os.Process.THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * 将Runnable放入线程池执行
     * 默认为后台优先级执行，如果需要调优先级请使用execute(runnable, callback, priority)调用
     * @param runnable
     * @param callback 回调到execute函数所运行的线程中
     */
    public static void execute(final Runnable runnable, final Runnable callback) {
        execute(runnable, callback, android.os.Process.THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * 将Runnable放入线程池执行
     * @param runnable
     * @param callback 回调到execute函数所运行的线程中
     * @param priority android.os.Process中指定的线程优先级
     */
    public static void execute(final Runnable runnable, final Runnable callback, final int priority) {
        try {
            if (!mThreadPool.isShutdown()) {
                HandlerEx handler = null;
                if (callback != null) {
                    handler = new HandlerEx("threadpool", Looper.myLooper());
                }

                final HandlerEx finalHandler = handler;
                mThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        android.os.Process.setThreadPriority(priority);
                        try {
                            runnable.run();
                            if (finalHandler != null && callback != null) {
                                finalHandler.post(callback);
                            }
                        } catch (final Throwable t) {
                            // ignore
                            if (sDebugMode) {
                                if (mMainThreadHandler == null) {
                                    createMainThread();
                                }

                                mMainThreadHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        throw new RuntimeException(Logger.getStackTraceString(t), t);
                                    }
                                });
                            }
                        } finally {
                            if (priority != Process.THREAD_PRIORITY_BACKGROUND) {
                                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                            }
                        }
                    }
                });
            }
        } catch (final Exception e) {
            if (sDebugMode) {
                if (mMainThreadHandler == null) {
                    createMainThread();
                }
                mMainThreadHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        throw new RuntimeException(Logger.getStackTraceString(e), e);
                    }
                });
            }
            // ignore
        }
    }

    /**
     *
     * @param threadType  ThreadManager.THREAD_BACKGROUND or ThreadManager.THREAD_WORK or ThreadManager.THREAD_UI or ThreadManager.THREAD_NORMAL
     * @param callbackToMainThread if true, preCallback and postCallback will run in UI thread; if false, run in calling thread
     * @param preCallback run before execute task runnable, control by callbackToMainThread argument, can be null
     * @param postCallback run after execute task runnable, control by callbackToMainThread argument, can be null
     */
    public static void post(final int threadType, final Runnable preCallback, final Runnable task, final Runnable postCallback, final boolean callbackToMainThread, long delayMillis) {
        if(task == null) {
            return;
        }

        if (mMainThreadHandler == null) {
            createMainThread();
        }

        Handler handler = null;
        switch (threadType) {
            case THREAD_BACKGROUND:
                if (sBackgroundThread == null) {
                    createBackgroundThread();
                }
                handler = sBackgroundHandler;
                break;
            case THREAD_WORK:
                if (sWorkThread == null) {
                    createWorkerThread();
                }
                handler = sWorkHandler;
                break;
            case THREAD_UI:
                handler = mMainThreadHandler;
                break;
            case THREAD_NORMAL:
                if (sNormalThread == null) {
                    createNormalThread();
                }

                handler = sNormalHandler;
                break;
            default:
                handler = mMainThreadHandler;
                break;
        }

        if (handler == null)
        	return;
        final Handler finalHandler = handler;

        Looper myLooper = null;
        if (callbackToMainThread == false) {
            myLooper = Looper.myLooper();
            if (myLooper == null) {
                myLooper = mMainThreadHandler.getLooper();
            }
        }
        final Looper looper = myLooper;

        final Runnable postRunnable = new Runnable() {
            @Override
            public void run() {

                Runnable monitorRunnable = null;
                if (sMonitorHandler != null) {
                    monitorRunnable = new Runnable() {
                        @Override
                        public void run() {
                            mMainThreadHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (Debug.isDebuggerConnected()) {
                                        return;
                                    }
                                    RuntimeException re = new RuntimeException("这里使用了ThreadManager.post函数运行了一个超过30s的任务，" +
                                            "请查看这个任务是否是非常耗时的任务，或者存在死循环，或者存在死锁，或者存在一直卡住线程的情况，" +
                                            "如果存在上述情况请解决或者使用ThreadManager.execute函数放入线程池执行该任务。", new Throwable(task.toString()));
                                    throw re;
                                }
                            });
                        }
                    };
                }

                if (sMonitorHandler != null) {
                    sMonitorHandler.postDelayed(monitorRunnable, RUNNABLE_TIME_OUT_TIME);
                }

                synchronized (mRunnableCache) {
                    mRunnableCache.remove(task);
                }

                try {
                    task.run();
                } catch (final Throwable t) {
                    // ignore
                    if (sDebugMode) {
                        mMainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                throw new RuntimeException(Logger.getStackTraceString(t), t);
                            }
                        });
                    }
                }

                if (sMonitorHandler != null) {
                    sMonitorHandler.removeCallbacks(monitorRunnable);
                }

                if(postCallback != null) {
                    if (callbackToMainThread || (looper == mMainThreadHandler.getLooper())) {
                        mMainThreadHandler.post(postCallback);
                    } else {
                       new Handler(looper).post(postCallback);
                    }
                }
            }
        };

        Runnable realRunnable = new Runnable() {
            @Override
            public void run() {
                if(preCallback != null) {
                    if (callbackToMainThread || (looper == mMainThreadHandler.getLooper())) {
                        mMainThreadHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                preCallback.run();
                                finalHandler.post(postRunnable);
                            }
                        });
                    } else {
                       new Handler(looper).post(new Runnable() {
                           @Override
                           public void run() {
                               preCallback.run();
                               finalHandler.post(postRunnable);
                           }
                       });
                    }
                } else {
                    postRunnable.run();
                }
            }
        };

        synchronized (mRunnableCache) {
            mRunnableCache.put(task, new RunnableMap(realRunnable, threadType));
        }

       	finalHandler.postDelayed(realRunnable, delayMillis);
    }

    /**
     *
     * @param threadType  ThreadManager.THREAD_BACKGROUND or ThreadManager.THREAD_WORK or ThreadManager.THREAD_UI or ThreadManager.THREAD_NORMAL
     * @param callbackToMainThread if true, preCallback and postCallback will run in UI thread; if false, run in calling thread
     * @param preCallback run before execute task runnable, control by callbackToMainThread argument, can be null
     * @param postCallback run after execute task runnable, control by callbackToMainThread argument, can be null
     */
    public static void post(int threadType, final Runnable preCallback, final Runnable task, final Runnable postCallback, boolean callbackToMainThread) {
        post(threadType, preCallback, task, postCallback, callbackToMainThread, 0);
    }

    /**
     *
     * @param threadType  ThreadManager.THREAD_BACKGROUND or ThreadManager.THREAD_WORK or ThreadManager.THREAD_UI or ThreadManager.THREAD_NORMAL
     * @param postCallbackRunnable run after execute task runnable, control by callbackToMainThread argument, can be null
     */
    public static void post(int threadType, final Runnable task, final Runnable postCallbackRunnable) {
        post(threadType, null, task, postCallbackRunnable, false, 0);
    }

    /**
     *
     * @param threadType  ThreadManager.THREAD_BACKGROUND or ThreadManager.THREAD_WORK or ThreadManager.THREAD_UI or ThreadManager.THREAD_NORMAL
     * @param preCallback run before execute task runnable, control by callbackToMainThread argument, can be null
     * @param postCallback run after execute task runnable, control by callbackToMainThread argument, can be null
     */
    public static void post(int threadType, final Runnable preCallback, final Runnable task, final Runnable postCallback) {
        post(threadType, preCallback, task, postCallback, false, 0);
    }

    /**
     * @note 谨慎使用:post到消息队列，顺序执行事件,如需要实时性强的操作请注意，可考虑使用本类的execute()或其他
     * @param threadType  ThreadManager.THREAD_BACKGROUND or ThreadManager.THREAD_WORK or ThreadManager.THREAD_UI or ThreadManager.THREAD_NORMAL
     * @param task
     */
    public static void post(int threadType, Runnable task) {
        post(threadType, null, task, null, false, 0);
    }

    /**
     * @note 谨慎使用:post到消息队列，顺序执行事件,如需要实时性强的操作请注意，可考虑使用本类的execute()或其他
     * @param threadType  ThreadManager.THREAD_BACKGROUND or ThreadManager.THREAD_WORK or ThreadManager.THREAD_UI or ThreadManager.THREAD_NORMAL
     * @param task
     */
    public static void postDelayed(int threadType, Runnable task, long delayMillis) {
        post(threadType, null, task, null, false, delayMillis);
    }

    /**
     * 可以直接移除所有使用ThreadManager post出去的task，不用指定是线程类型threadType.
     */
    public static void removeRunnable(final Runnable task) {
        if(task == null) {
            return;
        }

        RunnableMap map = (RunnableMap)mRunnableCache.get(task);
        if (map == null) {
            return;
        }

        Runnable realRunnable = map.getRunnable();
        if (realRunnable != null) {
            switch (map.getType()) {
                case THREAD_BACKGROUND:
                    if (sBackgroundHandler != null) {
                        sBackgroundHandler.removeCallbacks(realRunnable);
                    }
                    break;
                case THREAD_WORK:
                    if (sWorkHandler != null) {
                        sWorkHandler.removeCallbacks(realRunnable);
                    }
                    break;
                case THREAD_UI:
                    if (mMainThreadHandler != null) {
                        mMainThreadHandler.removeCallbacks(realRunnable);
                    }
                    break;
                case THREAD_NORMAL:
                    if (sNormalHandler != null) {
                        sNormalHandler.removeCallbacks(realRunnable);
                    }
                    break;
                case THREAD_IDLE_INTERNAL:
                    realRunnable.run(); // 特殊处理，使idle runnable支持取消
                    break;
                default:
                    break;
            }

            synchronized (mRunnableCache) {
                mRunnableCache.remove(task);
            }
        }
    }

    private static synchronized void createBackgroundThread() {
        if (sBackgroundThread == null) {
            sBackgroundThread = new HandlerThread("BackgroundHandler", android.os.Process.THREAD_PRIORITY_BACKGROUND);
            sBackgroundThread.start();
            sBackgroundHandler = new HandlerEx("BackgroundHandler", sBackgroundThread.getLooper());
        }
    }

    private static synchronized void createWorkerThread() {
        if (sWorkThread == null) {
            sWorkThread = new HandlerThread("WorkHandler", (android.os.Process.THREAD_PRIORITY_DEFAULT + android.os.Process.THREAD_PRIORITY_BACKGROUND) / 2);
            sWorkThread.start();
            sWorkHandler = new HandlerEx("WorkHandler", sWorkThread.getLooper());
        }
    }

    private static synchronized void createNormalThread() {
        if (sNormalThread == null) {
            sNormalThread = new HandlerThread("sNormalHandler", android.os.Process.THREAD_PRIORITY_DEFAULT);
            sNormalThread.start();
            sNormalHandler = new HandlerEx("sNormalHandler", sNormalThread.getLooper());
        }
    }
    
    private static synchronized void createMainThread() {
        if (mMainThreadHandler == null) {
            mMainThreadHandler = new HandlerEx("BackgroundHandler.MainThreadHandler + 38", Looper.getMainLooper());
        }
    }
    
    public static void doSomthingBeforDestroy() {
        if (sBackgroundThread != null) {
            sBackgroundThread.setPriority(Thread.MAX_PRIORITY);
        }
        if (sWorkThread != null) {
            sWorkThread.setPriority(Thread.MAX_PRIORITY);
        }
    }
    
    public static synchronized void destroy() {
        if (sBackgroundThread != null) {
            sBackgroundThread.quit();
            try {
                sBackgroundThread.interrupt();
            } catch (Throwable t) {}
            sBackgroundThread = null;
        }
        
        if (sWorkThread != null) {
            sWorkThread.quit();
            try {
                sWorkThread.interrupt();
            } catch (Throwable t) {}
            sWorkThread = null;
        }
           
        if (sNormalThread != null) {
            sNormalThread.quit();
            try {
                sNormalThread.interrupt();
            } catch (Throwable t) {}
            sNormalThread = null;
        }
        
        if (mThreadPool != null) {
            try {
                mThreadPool.shutdown();
            } catch (Throwable t) {}
            mThreadPool = null;
        }
    }
    
    public static Looper getBackgroundLooper() {
        createBackgroundThread();
        return sBackgroundThread.getLooper();
    }

    public static Looper getWorkLooper() {
        createWorkerThread();
        return sWorkThread.getLooper();
    }
    
    private static class RunnableMap {
        private Runnable mRunnable;
        private Integer mType;
        
        public RunnableMap(Runnable runnable, Integer type) {
            mRunnable = runnable;
            mType = type;
        }
        
        public Runnable getRunnable() {
            return mRunnable;
        }
        
        public int getType() {
            return mType;
        }
    }
    
    private static class CustomIdelHandler implements IdleHandler {
        private static final MessageQueue mMainThreadQueue = (MessageQueue)ReflectionHelper.getFieldValue(Looper.getMainLooper(), "mQueue");
        private static final Handler mHandler = new HandlerEx("IdleHandler", Looper.getMainLooper());
        private static final long mRunnableDelayTime = 10 * 1000;
        private Runnable mRunnable;
        
        public CustomIdelHandler(Runnable runnable) {
            mRunnable = runnable;
        }

        private final Runnable mRemoveRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMainThreadQueue != null) {
                    mMainThreadQueue.removeIdleHandler(CustomIdelHandler.this);
                }
                mHandler.removeCallbacks(mPostRunnable);
            }
        };

        private final Runnable mPostRunnable = new Runnable() {
            @Override
            public void run() {
                if (mMainThreadQueue != null) {
                    mMainThreadQueue.removeIdleHandler(CustomIdelHandler.this);
                }
                synchronized (mRunnableCache) {
                    mRunnableCache.remove(mRunnable);
                }
                mRunnable.run();
            }
        };
        
        @Override
        public boolean queueIdle() {
            mHandler.removeCallbacks(mPostRunnable);
            synchronized (mRunnableCache) {
                mRunnableCache.remove(mRunnable);
            }
            mRunnable.run();
            return false;
        }
        
        public void post() {
            if (mMainThreadQueue != null) {
                synchronized (mRunnableCache) {
                    mRunnableCache.put(mRunnable, new RunnableMap(mRemoveRunnable, THREAD_IDLE_INTERNAL));
                }
                mHandler.postDelayed(mPostRunnable, mRunnableDelayTime);
                mMainThreadQueue.addIdleHandler(this);
            } else {
                throw new Error("CustomIdelHandler main thread queue is null!");
            }
        }
    };
    
    /**
     * 向主线程发送一个闲时处理的Runnable,这个Runnable会在主线程空闲的时候进行处理，也就是主线程没有消息要处理的时候进行处理
     * @param runnable
     */
    public static void postIdleRunnable(final Runnable runnable) {
        new CustomIdelHandler(runnable).post();
    }
    
    public static boolean isMainThread(){
        return Looper.myLooper() == Looper.getMainLooper();
    }
    
    public static abstract class RunnableEx implements Runnable {
        private Object mArg;
        
        public void setArg(Object arg) {
            mArg = arg;
        }
        
        public Object getArg() {
            return mArg;
        }
    }

    public static boolean fakeMainLooper(boolean reset) {
        if(isMainThread()){
            return true;
        }
        ThreadLocal<Looper> threadLocal = (ThreadLocal<Looper>)ReflectionHelper.getStaticFieldValue(Looper.class, "sThreadLocal");
        if (threadLocal == null) {
            return false;
        }
        Looper looper = null;
        if (!reset) {
            looper = Looper.getMainLooper();
        }
        ReflectionHelper.invokeMethod(threadLocal, "set", new Class[]{Object.class}, new Object[]{looper});
        return true;
    }

    public static long fakeThreadId(long fakeId){
        Thread t = Thread.currentThread();
        long id = t.getId();
        if (fakeId != id) {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
                ReflectionHelper.setFieldValue(t, "id", fakeId);
            } else {
                ReflectionHelper.setFieldValue(t, "tid", fakeId);
            }
        }
        return id;
    }

    public static boolean prepareLooperWithMainThreadQueue(boolean reset) {
        if(isMainThread()){
            return true;
        }
        ThreadLocal<Looper> threadLocal = (ThreadLocal<Looper>)ReflectionHelper.getStaticFieldValue(Looper.class, "sThreadLocal");
        if (threadLocal == null) {
            return false;
        }
        Looper looper = null;
        if (!reset) {
            Looper.prepare();
            looper = Looper.myLooper();
            Object queue = ReflectionHelper.invokeMethod(Looper.getMainLooper(), "getQueue", new Class[0], new Object[0]);
            if(!(queue instanceof MessageQueue)){
                return false;
            }
            ReflectionHelper.setFieldValue(looper, "mQueue", queue);
        }
        ReflectionHelper.invokeMethod(threadLocal, "set", new Class[]{Object.class}, new Object[]{looper});
        return true;
    }

    public static void runOnUiThread(Runnable r) {
        if (isMainThread()) {
            r.run();
        } else {
            post(THREAD_UI, r);
        }
    }
}
