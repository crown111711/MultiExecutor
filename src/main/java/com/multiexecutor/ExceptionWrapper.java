package com.multiexecutor;


import java.util.function.Function;

/**
 * @author tanjia
 * @since 2019/7/24
 */
public final class ExceptionWrapper {

    public static <T, R> Function<T, R> checkRun(Function<T, R> function) {
        return i -> {
            try {
                return function.apply(i);
            } catch (Exception e) {
                Exceptions.sneakThrow(e);
            }
            return null;
        };
    }

    public static Runnable runnable(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                Exceptions.sneakThrow(e);
            }
        };
    }

    public static <E extends Exception> Runnable uncheckRunnable(RunnableThrowing<E> runnableThrowing) {
        return () -> {
            try {
                runnableThrowing.run();
            } catch (Exception e) {
                Exceptions.sneakThrow(e);
            }
        };
    }

    public static <T, R, E extends Exception> Function<T, R> unCheckRun(FunctionThrowing<T, R, E> function) {
        return i -> {
            try {
                return function.accept(i);
            } catch (Exception e) {
                Exceptions.sneakThrow(e);
            }
            return null;
        };
    }
}