/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.css.properties;

import javafx.scene.paint.Paint;
import javafx.scene.text.Font;

/**
 *
 * @author prem
 */
public class FontProperties {

    private final Font font;
    private final Paint fontColor;
    private final boolean underline;

    public FontProperties(Font font, Paint fontColor, boolean underline) {
        this.font = font;
        this.fontColor = fontColor;
        this.underline = underline;
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
}
