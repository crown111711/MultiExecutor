package com.virjar.j2executor;

import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by virjar on 2018/2/25.<br>
 * 主入口
 * 
 * @author virjar
 * @since 1.0
 */
public class J2Executor {
    private ThreadPoolExecutor parentThreadPoolExecutor;
    private BlockingQueue<Runnable> parentBlockingQueue;

    /**
     * 二级线程池增强器,需要传入一级线程池对象。 请注意,一般情况下,一级线程池的拒绝策略不会生效。
     * <ul>
     * <li>1. 如果该任务是二级线程池溢出提交过来的,那么如果该线程池拒绝,拒绝策略将会路由到原生的二级线程池。</li>
     * <li>2. 如果该任务本身就是提交到一级线程池,那么仍然以一级线程池的拒绝策略为主</li>
     * </ul>
     *
     * 一级线程池,永远不会达到maxThreadSize状态,当二级线程池溢出提交任务的时候,发现一级线程池繁忙,仍然会将该任务交给一级线程池自己处理
     * 
     * @param parentThreadPoolExecutor 对应的一级线程池,
     */
    public J2Executor(ThreadPoolExecutor parentThreadPoolExecutor) {
        this.parentThreadPoolExecutor = parentThreadPoolExecutor;
        parentBlockingQueue = parentThreadPoolExecutor.getQueue();

        // 替代 reject handler,虽然这个代码几乎不会被执行
        RejectedExecutionHandler originRejectExecutionHandler = parentThreadPoolExecutor.getRejectedExecutionHandler();
        if (!(originRejectExecutionHandler instanceof RejectMarkExecutionHandler)) {
            parentThreadPoolExecutor
                    .setRejectedExecutionHandler(new RejectMarkExecutionHandler(originRejectExecutionHandler));
        }
    }

    public J2Executor registrySubThreadPoolExecutors(Collection<ThreadPoolExecutor> threadPoolExecutors) {
        for (ThreadPoolExecutor threadPoolExecutor : threadPoolExecutors) {
            registrySubThreadPoolExecutor(threadPoolExecutor);
        }
        return this;
    }

    public J2Executor registrySubThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor) {
        // 将该线程池的任务队列替换掉,
        BlockingQueue<Runnable> blockingQueue = threadPoolExecutor.getQueue();
        if (!(blockingQueue instanceof ConsumeImmediatelyBlockingQueue)) {
            // 对线程池任务队列功能增强
            ReflectUtil.setFieldValue(threadPoolExecutor, "workQueue", new ConsumeImmediatelyBlockingQueue<>(
                    blockingQueue, new ConsumeImmediatelyBlockingQueue.ImmediatelyConsumer<Runnable>() {
                        @Override
                        public boolean consume(Runnable runnable) {
                            if (parentBlockingQueue.size() > 0 || parentThreadPoolExecutor
                                    .getActiveCount() >= parentThreadPoolExecutor.getCorePoolSize()) {
                                return false;
                            }
                            RejectedMonitorRunnable rejectedMonitorRunnable = new RejectedMonitorRunnable(runnable);
                            parentThreadPoolExecutor.execute(rejectedMonitorRunnable);
                            return !rejectedMonitorRunnable.rejected;
                        }
                    }));
        }
        return this;
    }

    private class RejectMarkExecutionHandler implements RejectedExecutionHandler {
        private RejectedExecutionHandler originRejectExecutionHandler;

        RejectMarkExecutionHandler(RejectedExecutionHandler originRejectExecutionHandler) {
            this.originRejectExecutionHandler = originRejectExecutionHandler;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            if (r instanceof RejectedMonitorRunnable) {
                ((RejectedMonitorRunnable) r).rejected = true;
                return;
            }
            originRejectExecutionHandler.rejectedExecution(r, executor);
        }
    }

    private class RejectedMonitorRunnable implements Runnable {
        private Runnable delegate;
        private boolean rejected = false;

        RejectedMonitorRunnable(Runnable delegate) {
            this.delegate = delegate;
        }

        @Override
        public void run() {
            // System.out.println("父线程池在执行任务");
            delegate.run();
        }
    }

}
