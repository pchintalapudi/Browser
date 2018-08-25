/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pc.browser.async.PriorityFuture;
import pc.browser.async.PriorityRunnable;
import pc.browser.async.PriorityThreadExecutorPool;
import pc.browser.async.RenderCompare;
import pc.browser.async.RenderTask;

/**
 *
 * @author prem
 */
public class Async {

    private static final PriorityThreadExecutorPool renderPool
            = new PriorityThreadExecutorPool(4, true, PriorityFuture.comparator());

    public static void asyncRender(Runnable renderTask, RenderTask taskType, int level) {
        RenderCompare compare = new RenderCompare(taskType, level);
        renderPool.submit(new PriorityRunnable<>(renderTask, compare));
    }
    
    private static final ExecutorService standard = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });
    
    public static void asyncStandard(Runnable task) {
        standard.submit(task);
    }
}
