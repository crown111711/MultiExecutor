package com.multiexecutor.spi;

/**
 * @author tanjia
 * @since 2019/9/12
 * 可执行容器
 */
public interface Executable {

    /**
     * 运行任务
     *
     * @param runnable
     */
    void execute(Runnable runnable);
}
