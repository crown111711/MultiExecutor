package com.multiexecutor.core;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TransmitterQueue<T> implements BlockingQueue<T> {
    private BlockingQueue<T> delegate;
    private Transmitter<T> transformer;

    public interface Transmitter<T> {
        boolean accept(T t);
    }

    public TransmitterQueue(BlockingQueue<T> delegate, Transmitter<T> transformer) {
        this.delegate = delegate;
        this.transformer = transformer;
    }

    @Override
    public boolean add(T t) {
        return
                transformer.accept(t) ||
                delegate.add(t);
    }

    @Override
    public boolean offer(T t) {
        return
                transformer.accept(t) ||
                delegate.offer(t);
    }

    @Override
    public T remove() {
        return delegate.remove();
    }

    @Override
    public T poll() {
        return delegate.poll();
    }

    @Override
    public T element() {
        return delegate.element();
    }

    @Override
    public T peek() {
        return delegate.peek();
    }

    @Override
    public void put(T t) throws InterruptedException {
        if (transformer.accept(t)) {
            return;
        }
        delegate.put(t);
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        return transformer.accept(t) || delegate.offer(t, timeout, unit);
    }

    @Override
    public T take() throws InterruptedException {
        return delegate.take();
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.poll(timeout, unit);
    }

    @Override
    public int remainingCapacity() {
        return delegate.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return delegate.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return delegate.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        List<T> remain = new LinkedList<>();
        for (T t : c) {
            if (!transformer.accept(t)) {
                remain.add(t);
            }
        }
        return delegate.addAll(remain);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return delegate.removeAll(c);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return delegate.retainAll(c);
    }

    @Override
    public void clear() {
        delegate.clear();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return delegate.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return delegate.toArray(a);
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return delegate.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        return delegate.drainTo(c, maxElements);
    }
}
