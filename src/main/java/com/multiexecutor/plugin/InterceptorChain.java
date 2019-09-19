package com.multiexecutor.plugin;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tanjia
 * @since 2019/9/19
 */
public class InterceptorChain {
    public static InterceptorChain INSTANCE = new InterceptorChain();
    private final List<Interceptor> interceptors = new ArrayList<>();

    public void addInterceptors(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public Object wrapper(Object target) {
        for (Interceptor interceptor : interceptors) {
            target = interceptor.plugin(target);
        }
        return target;
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }
}
