package com.multiexecutor;

/**
 * @author tanjia
 * @since 2019/9/10
 */
@FunctionalInterface
public interface RunnableThrowing<E extends Exception> {
    void run() throws E;
}
