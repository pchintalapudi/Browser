/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.async;

import java.util.Comparator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 *
 * @author prem
 * @param <C>
 */
public class PriorityFuture<C extends Comparable<C>> implements RunnableFuture<Void> {

    private final RunnableFuture<?> src;
    private final C prioritizer;

    public PriorityFuture(RunnableFuture<?> other, C prioritizer) {
        this.src = other;
        this.prioritizer = prioritizer;
    }

    public C getPriority() {
        return this.prioritizer;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return src.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return src.isCancelled();
    }

    @Override
    public boolean isDone() {
        return src.isDone();
    }

    @Override
    public Void get() throws InterruptedException, ExecutionException {
        src.get();
        return null;
    }

    @Override
    public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        src.get();
        return null;
    }

    @Override
    public void run() {
        src.run();
    }
    
    public static Comparator<Runnable> comparator() {
        return (o1, o2) -> o1 == null && o2 == null ? 0 : o1 == null ? -1 : o2 == null ? 1
                : o1 instanceof PriorityFuture && o2 instanceof PriorityFuture
                        ? ((PriorityFuture) o1).prioritizer.compareTo(((PriorityFuture) o2).prioritizer)
                        : o1 instanceof PriorityFuture ? 1 : o2 instanceof PriorityFuture ? -1 : 0;
    }
}
