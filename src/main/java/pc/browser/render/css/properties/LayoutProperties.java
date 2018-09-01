/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.css.properties;

import javafx.geometry.Insets;
import javafx.geometry.Point3D;

/**
 *
 * @author prem
 */
public class LayoutProperties {

    private final Insets margins, paddings;
    private final Point3D widthProps, heightProps;
    private final boolean contentBox;

    public LayoutProperties(Insets margins, Insets paddings, Point3D widthProps, Point3D heightProps, boolean contentBox) {
        this.margins = margins;
        this.paddings = paddings;
        this.widthProps = widthProps;
        this.heightProps = heightProps;
        this.contentBox = contentBox;
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

    public double getMinWidth() {
        return widthProps.getX();
    }

    public double getWidth() {
        return widthProps.getY();
    }

    public double getMaxWidth() {
        return widthProps.getZ();
    }

    public double getMinHeight() {
        return heightProps.getX();
    }

    public double getHeight() {
        return heightProps.getY();
    }

    public double getMaxHeight() {
        return heightProps.getZ();
    }

    public boolean isContentBox() {
        return contentBox;
    }
}
