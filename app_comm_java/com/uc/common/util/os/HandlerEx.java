package com.uc.common.util.os;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class HandlerEx extends Handler {
    public interface IHandlerExNotifier {
        void onSendMessageAtTime(boolean ret, Message msg, long uptimeMillis);

        void onDispatchMessage(Message msg);
    }

    private static IHandlerExNotifier sHandlerExNotifier;

    private String mName;

    public static void setHandlerExNotifier(IHandlerExNotifier handlerExNotifier) {
        sHandlerExNotifier = handlerExNotifier;
    }

    public HandlerEx(String name) {
        setName(name);
    }

    public HandlerEx(String name, Callback callback) {
        super(callback);
        setName(name);
    }

    public HandlerEx(String name, Looper looper) {
        super(looper);
        setName(name);
    }

    public HandlerEx(String name, Looper looper, Callback callback) {
        super(looper, callback);
        setName(name);
    }

    public void setName(String name) {
        this.mName = name;
    }

    public String getName() {
        return mName;
    }

    @Override
    public String toString() {
        return "HandlerEx (" + mName + ") {}";
    }

    @Override
    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        boolean sent = super.sendMessageAtTime(msg, uptimeMillis);

        IHandlerExNotifier handlerExNotifier = sHandlerExNotifier;
        if (handlerExNotifier != null) {
            handlerExNotifier.onSendMessageAtTime(sent, msg, uptimeMillis);
        }
        return sent;
    }

    @Override
    public void dispatchMessage(Message msg) {
        IHandlerExNotifier handlerExNotifier = sHandlerExNotifier;
        if (handlerExNotifier != null) {
            handlerExNotifier.onDispatchMessage(msg);
        }

        super.dispatchMessage(msg);
    }
}
