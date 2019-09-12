package com.multiexecutor.core.test;

import com.multiexecutor.ProxyUtil;
import com.multiexecutor.core.PoolNode;
import io.vavr.API;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author tanjia
 * @since 2019/9/12
 */
public class FunctionTest {


    public static void main(String[] args) {

        PoolNode poolNode = new PoolNode();
        ThreadPoolExecutor pool1 = new ThreadPoolExecutor(2, 3, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<Runnable>());
        poolNode.setValue(pool1);

        ExecutorService executor = poolNode.getExecutor();

        API.println(executor.toString());

        ClassLoader classLoader = executor.getClass().getClassLoader();
        API.println(classLoader == null ? true : false);
        executor.execute(() -> API.println("done"));
        executor.shutdown();

        ProxyUtil.generateClassFile(poolNode.getClass(), "PoolNodeProxy");
    }
}
