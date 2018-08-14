/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import java.util.regex.Pattern;

/**
 *
 * @author prem
 */
public enum DisplayType {
    INLINE, BLOCK, CONTENTS, FLEX, GRID, INLINE_BLOCK, INLINE_FLEX, INLINE_GRID,
    INLINE_TABLE, LIST_ITEM, RUN_IN, TABLE, TABLE_CAPTION, TABLE_COLUMN_GROUP, TABLE_HEADER_GROUP,
    TABLE_FOOTER_GROUP, TABLE_ROW_GROUP, TABLE_CELL, TABLE_ROW, NONE, INITIAL, INHERIT;

    private static final Pattern cssPattern = Pattern.compile("-");

    public static DisplayType read(String cssValue) {
        return DisplayType.valueOf(cssPattern.matcher(cssValue).replaceAll("_").toUpperCase());
    }

    public static boolean isInline(DisplayType dt) {
        switch (dt) {
            case INLINE:
            case INLINE_BLOCK:
            case INLINE_FLEX:
            case INLINE_GRID:
            case INLINE_TABLE:
                return true;
            default:
                return false;
        }
    }
}
