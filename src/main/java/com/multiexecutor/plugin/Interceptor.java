package com.multiexecutor.plugin;

import java.lang.reflect.InvocationHandler;

/**
 * @author tanjia
 * @since 2019/9/19
 * 对象级别拦截器
 */
public interface Interceptor extends InvocationHandler {
    Object plugin(Object target);
}
