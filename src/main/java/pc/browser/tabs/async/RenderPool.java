/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.tabs.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import pc.browser.async.PriorityFuture;
import pc.browser.async.PriorityRunnable;
import pc.browser.async.PriorityThreadExecutorPool;
import pc.browser.async.RenderCompare;
import pc.browser.async.RenderTask;

/**
 *
 * @author prem
 */
public class RenderPool {

    private final PriorityThreadExecutorPool renderPool
            = new PriorityThreadExecutorPool(4, true, PriorityFuture.comparator());

    public Future<?> asyncRender(Runnable renderTask, RenderTask taskType, int level) {
        RenderCompare compare = new RenderCompare(taskType, level);
        return renderPool.submit(new PriorityRunnable<>(() -> {
            Lock l = activeLock.readLock();
            try {
                l.lockInterruptibly();
                renderTask.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                l.unlock();
            }
        }, compare));
    }

    private final ReadWriteLock activeLock = new ReentrantReadWriteLock(true);
    private Lock sleepLock;
    private final ExecutorService alarmThread = Executors.newSingleThreadExecutor();

    public void lock() {
        alarmThread.submit(() -> {
            if (sleepLock != null) {
                sleepLock.unlock();
            }
            try {
                sleepLock = activeLock.writeLock();
                sleepLock.lockInterruptibly();
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        });
    }

    public void unlock() {
        alarmThread.submit(() -> {
            if (sleepLock != null) {
                sleepLock.unlock();
            }
        });
    }

    public void close() {
        renderPool.shutdownNow();
        alarmThread.shutdownNow();
    }
}
