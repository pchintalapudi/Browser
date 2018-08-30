/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.events;

import javafx.event.Event;
import javafx.event.EventType;
import pc.browser.tabs.TabController;

/**
 *
 * @author prem
 */
public class LoadEvent extends Event {

    public static final EventType<LoadEvent> LOAD_EVENT = new EventType<>("load");

    private final String loadPayload;

    public LoadEvent(String loadPayload) {
        super(LOAD_EVENT);
        this.loadPayload = loadPayload;
    }

    public String getLoadPayload() {
        return loadPayload;
    }
}
