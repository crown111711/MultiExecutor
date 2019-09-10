package com.multiexecutor.core.test;

import com.multiexecutor.core.MultiExecutor;
import com.multiexecutor.core.MultiRunnable;
import com.multiexecutor.spi.ContextStarter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tanjia
 * @email 378097217@qq.com
 * @date 2019/9/10 1:24
 */
public class ThoughThread {

    public static ThreadLocal<String> attach = new ThreadLocal<String>() {
        protected String initialValue() {
            return "hello";
        }
    };

    public static InheritableThreadLocal<String> attach0 = new InheritableThreadLocal<String>() {
        protected String initialValue() {
            return "world";
        }
    };

    public static void main(String[] args) throws Exception {

        ContextStarter.start();

        ThreadPoolExecutor pool1 = new ThreadPoolExecutor(20, 30, 1000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(),
                new MultiExecutorTest.PrintLogRejectHandler("1号线程池"));

        // 2号线程池,使用ArrayBlockingQueue,任务会被拒绝,将会打印日志
        ThreadPoolExecutor pool2 = new ThreadPoolExecutor(20, 30, 1000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(), new MultiExecutorTest.PrintLogRejectHandler("2号线程池"));
        final List<ThreadPoolExecutor> pools = new ArrayList<>();
        pools.add(pool1);
        pools.add(pool2);

        attach.set("tanjia hello");
        attach0.set("cmr hello");

        // 使用两级线程池关联各个业务线程池
        MultiExecutor.register(new ThreadPoolExecutor(1, 30, 1000L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10))
                , pools);

        // 启动四个任务,同时往各自线程池丢任务
        for (int i = 0; i < pools.size(); i++) {
            new Thread(MultiRunnable.get(new WorkRunnable(pools.get(i), i))).start();
        }

    }

    public static class WorkRunnable implements Runnable {
        private ThreadPoolExecutor poolExecutor;
        private int index;

        public WorkRunnable(ThreadPoolExecutor poolExecutor, int index) {
            this.poolExecutor = poolExecutor;
            this.index = index;
        }

        @Override
        public void run() {
            for (int i = 0; i < 100000; i++) {
                final long startTimestamp = System.currentTimeMillis();
                poolExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println(Thread.currentThread().getName() + ":attach:" + attach.get());
                        System.out.println("attach0:" + attach0.get());
                        System.out.println("第" + index + "组任务在执行,完成时间: "
                                + (System.currentTimeMillis() - startTimestamp) / 1000 + "s");
                    }

                });

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            poolExecutor.shutdown();
        }
    }
}
