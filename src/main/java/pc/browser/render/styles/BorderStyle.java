/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

import javafx.scene.layout.BorderStrokeStyle;

/**
 *
 * @author prem
 */
public enum BorderStyle {
    DOTTED, DASHED, SOLID, DOUBLE, GROOVE, RIDGE, INSET, OUTSET, NONE, HIDDEN;

    public static BorderStrokeStyle mapToBorderStrokeStyle(BorderStyle bs) {
        switch (bs) {
            case DOTTED:
                return BorderStrokeStyle.DOTTED;
            default:
            case SOLID:
                return BorderStrokeStyle.SOLID;
            case DASHED:
                return BorderStrokeStyle.DASHED;
            case NONE:
                return BorderStrokeStyle.NONE;
        }
    }
}
