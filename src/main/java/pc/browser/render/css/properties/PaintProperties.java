/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.css.properties;

import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;

/**
 *
 * @author prem
 */
public class PaintProperties {

    private final Background background;
    private final Border border;
    private final Cursor cursor;

    public PaintProperties(Background background, Border border, Cursor cursor) {
        this.background = background;
        this.border = border;
        this.cursor = cursor;
    }

    /**
     * @return the background
     */
    public Background getBackground() {
        return background;
    }

    /**
     * @return the border
     */
    public Border getBorder() {
        return border;
    }

    /**
     * @return the cursor
     */
    public Cursor getCursor() {
        return cursor;
    }
}
