/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.async;

import java.util.concurrent.Callable;

/**
 *
 * @author prem
 * @param <C>
 */
public class PriorityRunnable<C extends Comparable<C>> implements Callable<Void> {

    private final Runnable action;
    private final C comparable;

    public PriorityRunnable(Runnable action, C comparable) {
        this.action = action;
        this.comparable = comparable;
    }

    @Override
    public Void call() throws Exception {
        action.run();
        return null;
    }
    
    public C getComparable() {
        return comparable;
    }
}
