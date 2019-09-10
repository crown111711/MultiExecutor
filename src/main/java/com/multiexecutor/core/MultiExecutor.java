package com.multiexecutor.core;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author tanjia
 */
public class MultiExecutor {

    /***
     * 注册两层线程池
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
