/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.tabs;

import java.net.URL;

/**
 *
 * @author prem
 */
public class History {

    private String title;
    private final URL url;

    public History(String title, URL url) {
        this.title = title;
        this.url = url;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the url
     */
    public URL getUrl() {
        return url;
    }
}
