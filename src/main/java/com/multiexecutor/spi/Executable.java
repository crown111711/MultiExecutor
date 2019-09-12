package com.multiexecutor.spi;

/**
 * @author tanjia
 * @since 2019/9/12
 */
public interface Executable {
    void execute(Runnable runnable);
}
