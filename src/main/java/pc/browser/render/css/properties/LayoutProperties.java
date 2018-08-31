/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.css.properties;

import javafx.geometry.Insets;

/**
 *
 * @author prem
 */
public class LayoutProperties {

    private final Insets margins, paddings;

    public LayoutProperties(Insets margins, Insets paddings) {
        this.margins = margins;
        this.paddings = paddings;
    }

    /**
     * @return the margins
     */
    public Insets getMargins() {
        return margins;
    }

    /**
     * @return the paddings
     */
    public Insets getPaddings() {
        return paddings;
    }
}
