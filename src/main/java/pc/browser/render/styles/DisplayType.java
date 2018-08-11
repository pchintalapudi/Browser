/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

/**
 *
 * @author prem
 */
public enum DisplayType {
    BLOCK, INLINE_BLOCK, INLINE, NONE;

    public static boolean isBlock(DisplayType type) {
        return type == BLOCK;
    }

    public static boolean isInline(DisplayType type) {
        return type == INLINE || type == INLINE_BLOCK;
    }
}
