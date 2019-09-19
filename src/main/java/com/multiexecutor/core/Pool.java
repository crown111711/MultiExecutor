package com.multiexecutor.core;

import com.multiexecutor.spi.Executable;
import io.vavr.control.Option;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author tanjia
 * @since 2019/9/12
 */
public class Pool {
    PoolNode root;
    ConcurrentHashMap<String, PoolNode> cachePools;

    public Pool(PoolNode poolNode, AtomicLong counter) {
        cachePools = new ConcurrentHashMap<>(counter.intValue());
        this.root = poolNode;
        init(poolNode);
    }

    public Pool(PoolNode poolNode) {
        cachePools = new ConcurrentHashMap<>();
        this.root = poolNode;
        init(poolNode);
    }

    public void init(PoolNode rootNode) {
        if (rootNode == null) {
            return;
        }
        cachePools.put(rootNode.getPoolName(), rootNode);
        rootNode.children.forEach(this::init);
    }

    /**
     * 指定线程池运行
     *
     * @param runnable
     * @param poolName
     */
    public void execute(Runnable runnable, String poolName) {
        Option.of(cachePools.get(poolName)).getOrElseThrow(() -> new RuntimeException("poolName" + poolName + "not exist"))
                .execute(runnable);
    }

    /**
     * 获取指定线程池
     * 若指定proxy,则会返回线程池的代理,代理中主要为了解决跨线程问题由于代理有性能损耗,故可在进程启动时挂载{@link com.multiexecutor.agent.MultiAgent}
     *
     * @param poolName
     * @param proxy
     */
    public ExecutorService borrowPool(String poolName, /*是否对pool进行代理*/boolean proxy) {
        return Option.of(cachePools.get(poolName)).getOrElseThrow(() -> new RuntimeException("poolName" + poolName + "not exist")).getExecutor(proxy);
    }

    /**
     * 获取指定线程池的executable对象
     *
     * @param poolName
     * @return
     */
    public Executable borrowPoolNode(String poolName) {
        return Option.of(cachePools.get(poolName)).getOrElseThrow(() -> new RuntimeException("poolName" + poolName + "not exist"));
    }

}
