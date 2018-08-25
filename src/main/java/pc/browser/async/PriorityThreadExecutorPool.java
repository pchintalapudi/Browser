/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.async;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author prem
 */
public class PriorityThreadExecutorPool extends ThreadPoolExecutor {

    public PriorityThreadExecutorPool(int corePoolSize, boolean daemon, Comparator<Runnable> comparator) {
        super(corePoolSize, corePoolSize, 0L, TimeUnit.MILLISECONDS, new PriorityBlockingQueue<>(11, comparator), daemon ? r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        } : Thread::new);
    }

    @Override
    protected <T> RunnableFuture<T> newTaskFor(Callable<T> callable) {
        RunnableFuture<T> newTaskFor = super.newTaskFor(callable);
        return new PriorityFuture(newTaskFor, ((PriorityRunnable<?>) callable).getComparable());
    }
}
