package com.virjar.j2executor.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.virjar.j2executor.J2Executor;

/**
 * Created by virjar on 2018/2/25.<br>
 * 使用范例
 */
public class J2ExecutorTest {
    public static void main(String[] args) {
        // 业务线程池
        // 1号线程池,使用LinkedBlockingDeque,不会拒绝任务,但是当任务堵塞的时候,将会导致超时
        ThreadPoolExecutor pool1 = new ThreadPoolExecutor(2, 3, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(), new PrintLogRejectHandler("1号线程池"));

        // 2号线程池,使用ArrayBlockingQueue,任务会被拒绝,将会打印日志
        ThreadPoolExecutor pool2 = new ThreadPoolExecutor(2, 3, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(10), new PrintLogRejectHandler("2号线程池"));

        // 1、2号线程池,她们的任务都会被提交到一级线程池进行执行(在各自的线程池繁忙的时候),用来抵御流量突发问题

        // 3号线程池,使用LinkedBlockingDeque,任务提交速度小于消费速度,线程池处于正常状况
        ThreadPoolExecutor pool3 = new ThreadPoolExecutor(2, 3, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>(), new PrintLogRejectHandler("3号线程池"));

        // 4号线程池,ArrayBlockingQueue,任务提交速度小于消费速度,线程池处于正常状况
        ThreadPoolExecutor pool4 = new ThreadPoolExecutor(2, 3, 0L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(10), new PrintLogRejectHandler("4号线程池"));

        final List<ThreadPoolExecutor> pools = new ArrayList<>();
        pools.add(pool1);
        pools.add(pool2);
        pools.add(pool3);
        pools.add(pool4);
        // 使用两级线程池关联各个业务线程池
        new J2Executor(new ThreadPoolExecutor(20, 30, 0L, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<Runnable>(10)))
                .registrySubThreadPoolExecutors(pools);

        // 启动四个任务,同时往各自线程池丢任务
        for (int i = 0; i < pools.size(); i++) {
            new WorkThread(pools.get(i), (i + 1)).start();
        }

    }

    private static class WorkThread extends Thread {
        private ThreadPoolExecutor poolExecutor;
        private int index;

        public WorkThread(ThreadPoolExecutor poolExecutor, int index) {
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
                        // 每个任务执行2s
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        System.out.println("第" + index + "组任务在执行,完成时间: "
                                + (System.currentTimeMillis() - startTimestamp) / 1000 + "s");
                    }
                });

                // 第1、2组任务,直接打满,其他任务,每隔3s投递一个任务
                long sleepTime;
                if (index == 1 || index == 2) {
                    sleepTime = 50;
                } else {
                    sleepTime = 1500;
                }

                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            poolExecutor.shutdown();
        }
    }

    private static class PrintLogRejectHandler implements RejectedExecutionHandler {
        private String tag;

        public PrintLogRejectHandler(String tag) {
            this.tag = tag;
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println(tag + "任务被拒绝");
        }
    }
}
