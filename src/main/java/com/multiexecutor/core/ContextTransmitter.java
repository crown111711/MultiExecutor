package com.multiexecutor.core;


/**
 * @author tanjia
 * @email 378097217@qq.com
 * @date 2019/9/10 0:55
 */
public class ContextTransmitter {
    public static Snapshot capture() {
        Thread thread = Thread.currentThread();
        Object threadFields = ReflectUtil.getFieldValue(thread, "threadLocals");
        Object inheritFields = ReflectUtil.getFieldValue(thread, "inheritableThreadLocals");
        return new Snapshot(threadFields, inheritFields);
    }

    public static Snapshot relay(Object snapshot) {
        Thread thread = Thread.currentThread();
        Snapshot org = (Snapshot) snapshot;
        Object threadFields = ReflectUtil.getFieldValue(thread, "threadLocals");
        Object inheritFields = ReflectUtil.getFieldValue(thread, "inheritableThreadLocals");
        ReflectUtil.setFieldValue(thread, "threadLocals", org.getThreadLocalMap());
        ReflectUtil.setFieldValue(thread, "inheritableThreadLocals", org.getInheritThreadLocalMap());
        return new Snapshot(threadFields, inheritFields);
    }

    public static void restore(Object backup) {
        Thread thread = Thread.currentThread();
        Snapshot org = (Snapshot) backup;
        ReflectUtil.setFieldValue(thread, "threadLocals", org.getThreadLocalMap());
        ReflectUtil.setFieldValue(thread, "inheritableThreadLocals", org.getInheritThreadLocalMap());
    }
}
