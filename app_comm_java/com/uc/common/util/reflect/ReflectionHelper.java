/**
 * ****************************************************************************
 * Copyright (C) 2005-2012 UCWEB Corporation. All rights reserved
 * File        : 2012-11-29
 * <p>
 * Description : ReflectionHelper.java
 * <p>
 * Creation    : 2012-11-29
 * Author      : liangcm@ucweb.com
 * History     : Creation, 2012-11-29, liangcm, Create the file
 * ****************************************************************************
 */

package com.uc.common.util.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.uc.common.util.log.Logger;


public final class ReflectionHelper {

    private static final String TAG = "ReflectionHelper";
    // FIXME: -1也不是很好的做啊，不过不想加异常怀疑代码会增大，需要的时候改抛异常吧。
    public final static int INVALID_VALUE = -1;

    /**
     * 通过构造器取得实例
     * @param className 类的全限定名
     * @param intArgsClass 构造函数的参数类型
     * @param intArgs 构造函数的参数值
     *
     * @return Object
     */
    public static Object getObjectByConstructor(String className, Class[] intArgsClass, Object[] intArgs) {

        Object returnObj = null;
        try {
            Class classType = Class.forName(className);
            Constructor constructor = classType.getDeclaredConstructor(intArgsClass); //找到指定的构造方法
            constructor.setAccessible(true);//设置安全检查，访问私有构造函数必须
            returnObj = constructor.newInstance(intArgs);
        } catch (InstantiationException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (InvocationTargetException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (NoSuchMethodException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (ClassNotFoundException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }
        return returnObj;
    }

    public static Object getStaticFieldValue(Class cls, String fieldName) {
        Field field;
        Object fieldValue = null;
        try {
            field = cls.getDeclaredField(fieldName);
            field.setAccessible(true);//设置安全检查，访问私有成员变量必须
            fieldValue = field.get(null);
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (NoSuchFieldException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }
        return fieldValue;
    }

    /**
     * 访问类成员变量(int类型)
     * @param object 访问对象
     * @param filedName 指定成员变量名
     * @return int 取得的成员变量的值, 找不到就返回 INVALID_VALUE
     * */
    public static int getIntFieldValue(Object object, String filedName) {
        Class classType = object.getClass();
        Field field = null;
        int fieldValue = INVALID_VALUE;
        try {
            field = classType.getDeclaredField(filedName);
            field.setAccessible(true);//设置安全检查，访问私有成员变量必须
            fieldValue = field.getInt(object);
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (NoSuchFieldException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }
        return fieldValue;
    }

    /**
     * 从类 访问类成员变量(int类型)
     * @param c 访问对象
     * @param filedName 指定成员变量名
     * @return int 取得的成员变量的值
     * */
    public static int getStaticIntFieldValue(Class c, String filedName) {
        Field fild = null;
        int fildValue = 0;
        try {
            fild = c.getDeclaredField(filedName);
            fild.setAccessible(true);//设置安全检查，访问私有成员变量必须
            fildValue = fild.getInt(c);
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (NoSuchFieldException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }
        return fildValue;
    }

    public static void setFieldValue(Object obj, String field, Object value) {
        try {
            Field f;
            try {
                f = obj.getClass().getDeclaredField(field);
            } catch (Exception e) {
                f = obj.getClass().getField(field);
            }
            f.setAccessible(true);
            f.set(obj, value);
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (NoSuchFieldException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }
    }

    public static void setSuperFieldValue(Object obj, String field, Object value) {
        try {
            Field f = null;
            Class<?> curClass = obj.getClass().getSuperclass();
            for (; curClass != null; ) {
                try {
                    f = curClass.getDeclaredField(field);
                    if (f != null)
                        break;
                } catch (Exception e) {
                    curClass = curClass.getSuperclass();
                }
            }

            if (f != null) {
                f.setAccessible(true);
                f.set(obj, value);
            }
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }
    }

    public static Object getSuperFieldValue(Object obj, String field) {
        try {
            Field f = null;
            Class<?> curClass = obj.getClass().getSuperclass();
            for (; curClass != null; ) {
                try {
                    f = curClass.getDeclaredField(field);
                    if (f != null)
                        break;
                } catch (Exception e) {
                    curClass = curClass.getSuperclass();
                }
            }
            if (f != null) {
                f.setAccessible(true);
                return f.get(obj);
            }
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }

        return null;
    }


    public static Object getFieldValue(Object obj, String field) {
        try {
            Field f;

            try {
                f = obj.getClass().getDeclaredField(field);
            } catch (Exception e) {
                f = obj.getClass().getField(field);
            }

            f.setAccessible(true);
            return f.get(obj);
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (NoSuchFieldException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }

        return null;
    }

    public static Object invokeMethod(Object obj, String method) {
        try {
            Method m;
            try {
                m = obj.getClass().getDeclaredMethod(method);
            } catch (Exception e) {
                m = obj.getClass().getMethod(method);
            }

            m.setAccessible(true);
            return m.invoke(obj);
        } catch (NoSuchMethodException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (InvocationTargetException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }

        return null;
    }

    /**
     * invoke object's method with a single param   
     * @param o: Object to be use
     * @param methodName: method to be invoke
     * @param argsClass: parameter's type
     * @param args: arguments
     * by lixl
     */
    public static Object invokeMethod(Object o, String methodName, Class[] argsClass, Object[] args) {
        Object returnValue = null;
        try {
            Class<?> c = o.getClass();
            Method method;
            method = c.getMethod(methodName, argsClass);
            method.setAccessible(true);
            returnValue = method.invoke(o, args);
        } catch (NoSuchMethodException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (InvocationTargetException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }

        return returnValue;
    }

    /**
     * 反射调用静态方法
     * @param cls
     * @param methodName
     * @param argsClass
     * @param args
     * @return
     */
    public static Object invokeStaticMethod(Class<?> cls, String methodName, Class[] argsClass, Object[] args) {
        Object returnValue = null;
        try {
            Method method = cls.getMethod(methodName, argsClass);
            returnValue = method.invoke(null, args);
        } catch (NoSuchMethodException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (IllegalAccessException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        } catch (InvocationTargetException e) {
            Logger.e(TAG, Logger.getStackTraceString(e));
        }

        return returnValue;
    }
}
