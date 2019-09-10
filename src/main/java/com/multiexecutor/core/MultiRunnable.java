package com.multiexecutor.core;

import lombok.Data;

import java.util.concurrent.atomic.AtomicReference;

import static com.multiexecutor.core.ContextTransmitter.*;

/**
 * @author tanjia
 * @email 378097217@qq.com
 * @date 2019/9/10 0:44
 */
@Data
public class MultiRunnable implements Runnable, Enhanced {
    private Runnable delegate;
    private boolean rejected = false;
    private final AtomicReference<Object> captureRef;

    public static MultiRunnable get(Runnable delegate) {
        if (delegate instanceof MultiRunnable) {
            return (MultiRunnable) delegate;
        }
        return new MultiRunnable(delegate);
    }

    MultiRunnable(Runnable delegate) {
        this.delegate = delegate;
        this.captureRef = new AtomicReference<>(capture());
    }

    @Override
    public void run() {
        Object capture = this.captureRef.get();
        Object backup = relay(capture);
        try {
            delegate.run();
        } finally {
            restore(backup);
        }
    }
}
