/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.events;

import javafx.event.Event;
import javafx.event.EventTarget;
import javafx.event.EventType;

/**
 *
 * @author prem
 */
public class LoadEvent extends Event {

    public static final EventType<LoadEvent> LOAD_EVENT = new EventType<>("load");

    public LoadEvent() {
        super(LOAD_EVENT);
    }

    public LoadEvent(Object source, EventTarget target) {
        super(source, target, LOAD_EVENT);
    }

}
