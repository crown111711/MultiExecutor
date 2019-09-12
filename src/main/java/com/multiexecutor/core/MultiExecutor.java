package com.multiexecutor.core;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tanjia
 */
public class MultiExecutor {

    /***
     * 注册两层线程池
     *
     * @param parent
     * @param threadPoolExecutors
     */
    public static void register(ThreadPoolExecutor parent, Collection<ThreadPoolExecutor> threadPoolExecutors) {
        RejectedExecutionHandler originRejectExecutionHandler = parent.getRejectedExecutionHandler();
        if (!(originRejectExecutionHandler instanceof RejectMarkExecutionHandler)) {
            parent.setRejectedExecutionHandler(new RejectMarkExecutionHandler(originRejectExecutionHandler));
        }
        for (ThreadPoolExecutor threadPoolExecutor : threadPoolExecutors) {
            register(parent, threadPoolExecutor);
        }
    }


    /**
     * 注册线程池树
     */
    public static Pool registerTree(PoolNode root) {
        AtomicLong counter = new AtomicLong(0);
        registerTree0(root, counter);
        Pool pool = new Pool(root);
        return pool;
    }

    /**
     * 递归注册线程池
     *
     * @param root
     */
    public static void registerTree0(PoolNode root, AtomicLong counter) {
        if (root == null) {
            return;
        }
        if (root.isLeaf()) {
            counter.incrementAndGet();
            return;
        }
        register(root.getValue(), root.childrenPools());
        root.getChildren().forEach(e -> registerTree0(e, counter));
    }

    public static void register(ThreadPoolExecutor parent, ThreadPoolExecutor threadPoolExecutor) {
        // 将该线程池的任务队列替换掉,
        BlockingQueue<Runnable> blockingQueue = threadPoolExecutor.getQueue();
        if (!(blockingQueue instanceof TransmitterQueue)) {
            // 对线程池任务队列功能增强
            ReflectUtil.setFieldValue(threadPoolExecutor, "workQueue", new TransmitterQueue<>(
                    blockingQueue, runnable -> {
                if (parent.getQueue().size() > 0 || parent.getActiveCount() >= parent.getCorePoolSize()) {
                    return false;
                }
                MultiRunnable rejectedMonitorRunnable = MultiRunnable.get(runnable);
                parent.execute(rejectedMonitorRunnable);
                return !rejectedMonitorRunnable.isRejected();
            }));
        }
    }

    public static class RejectMarkExecutionHandler implements RejectedExecutionHandler {
        private RejectedExecutionHandler originRejectExecutionHandler;

        RejectMarkExecutionHandler(RejectedExecutionHandler originRejectExecutionHandler) {
            this.originRejectExecutionHandler = originRejectExecutionHandler;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof MultiRunnable) {
                ((MultiRunnable) r).setRejected(true);
                return;
            }
            originRejectExecutionHandler.rejectedExecution(r, executor);
        }
    }


}
