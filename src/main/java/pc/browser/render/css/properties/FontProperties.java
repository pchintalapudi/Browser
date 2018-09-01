/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.css.properties;

import javafx.geometry.Pos;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author prem
 */
public class FontProperties {

    private final Font font;
    private final Paint fontColor;
    private final boolean underline;
    private final TextAlignment align;
    private final Pos vAlign;

    public FontProperties(Font font, Paint fontColor, boolean underline, TextAlignment align, Pos vAlign) {
        this.font = font;
        this.fontColor = fontColor;
        this.underline = underline;
        this.align = align;
        this.vAlign = vAlign;
    }

    /**
     * @return the font
     */
    public Font getFont() {
        return font;
    }

    /**
     * @return the fontColor
     */
    public Paint getFontColor() {
        return fontColor;
    }

    /**
     * @return the underline
     */
    public boolean isUnderline() {
        return underline;
    }

    public TextAlignment getAlign() {
        return align;
    }

    public Pos getVAlign() {
        return vAlign;
    }
}
