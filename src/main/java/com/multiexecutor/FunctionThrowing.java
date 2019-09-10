package com.multiexecutor;

/**
 * @author tanjia
 * @since 2019/7/24
 */
@FunctionalInterface
public interface FunctionThrowing<T, R, E extends Exception> {
    R accept(T t) throws E;
}

