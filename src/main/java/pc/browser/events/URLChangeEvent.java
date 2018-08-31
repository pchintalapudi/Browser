/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.events;

import javafx.event.Event;
import javafx.event.EventType;

/**
 *
 * @author prem
 */
public class URLChangeEvent extends Event {

    public static final EventType<URLChangeEvent> SET_TO_URL = new EventType<>("set_url");

    private final String urlString;

    public URLChangeEvent(String urlString) {
        super(SET_TO_URL);
        this.urlString = urlString;
    }

    public String getURLString() {
        return urlString;
    }
}
