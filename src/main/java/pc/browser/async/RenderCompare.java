/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.async;

/**
 *
 * @author prem
 */
public class RenderCompare implements Comparable<RenderCompare> {

    private final RenderTask task;
    private final int priority;

    public RenderCompare(RenderTask task, int priority) {
        this.task = task;
        this.priority = priority;
    }

    @Override
    public int compareTo(RenderCompare o) {
        int taskCompare = task.compareTo(o.task);
        return taskCompare == 0 ? Integer.compare(priority, o.priority) : taskCompare;
    }
}
