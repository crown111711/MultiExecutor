package com.multiexecutor;

/**
 * @author tanjia
 * @since 2019/7/23
 */
public class Exceptions {

    public static void sneakThrow(Throwable throwable) {
        sneakThrow0(throwable);
    }

    public static <T extends Throwable> void sneakThrow0(Throwable throwable) throws T {
        throw (T) throwable;
    }
}
