package com.multiexecutor.core;

import com.multiexecutor.spi.Executable;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @author tanjia
 * @email 378097217@qq.com
 * @date 2019/9/10 0:35
 * 线程池节点
 */
@Data
@NoArgsConstructor
public class PoolNode implements Executable {
    PoolNode parent;
    List<PoolNode> children;
    ThreadPoolExecutor value;
    String poolName;

    public boolean isLeaf() {
        return children == null || children.isEmpty();
    }

    public List<ThreadPoolExecutor> childrenPools() {
        return children.stream().map(PoolNode::getValue).collect(Collectors.toList());
    }

    /**
     * 切面拦截 -> 保证线程上下文传递
     *
     * @param runnable
     */
    @Override
    public void execute(Runnable runnable) {
        Runnable wrapperRunnable = MultiRunnable.get(runnable);
        this.value.execute(wrapperRunnable);
    }

    public ExecutorService getExecutor() {
        PoolTagHandler poolTagHandler = new PoolTagHandler();
        ExecutorService proxy = (ExecutorService) Proxy.newProxyInstance(null, new Class[]{ExecutorService.class}, poolTagHandler);
        return proxy;
    }

    public class PoolTagHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof Runnable) {
                        args[i] = MultiRunnable.get((Runnable) args[i]);
                    }
                }
            }
            method.invoke(PoolNode.this.value, args);
            return null;
        }
    }
}
