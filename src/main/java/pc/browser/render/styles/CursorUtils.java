/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

import javafx.scene.Cursor;
import javafx.scene.Node;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 *
 * @author prem
 */
public class CursorUtils {

    public static Cursor translate(String cssValue) {
        switch (cssValue.trim().toLowerCase()) {
            case "default":
            default:
                return Cursor.DEFAULT;
            case "pointer":
                return Cursor.HAND;
            case "none":
                return Cursor.DISAPPEAR;
            case "text":
                return Cursor.TEXT;
            case "progress":
            case "wait":
                return Cursor.WAIT;
            case "grab":
                return Cursor.HAND;
            case "grabbing":
                return Cursor.CLOSED_HAND;
            case "move":
                return Cursor.MOVE;
            case "crosshair":
                return Cursor.CROSSHAIR;
            case "n-resize":
                return Cursor.N_RESIZE;
            case "w-resize":
                return Cursor.W_RESIZE;
            case "s-resize":
                return Cursor.S_RESIZE;
            case "e-resize":
                return Cursor.E_RESIZE;
            case "nw-resize":
                return Cursor.NW_RESIZE;
            case "ne-resize":
                return Cursor.NE_RESIZE;
            case "sw-resize":
                return Cursor.SW_RESIZE;
            case "se-resize":
                return Cursor.SE_RESIZE;
        }
    }
}
