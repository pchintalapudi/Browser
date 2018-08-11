/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

/**
 *
 * @author prem
 */
public class Styling {

    public static final Styling EMPTY = StylingBuilder.create().createStyling();

    private final DisplayType displayType;

    private final String fontFamily;
    private final double fontSize;
    private final FontWeight fontWeight;

    private final Color backgroundColor;
    private final Color textColor;
    private final double[] padding;
    private final double[] margin;

    private final Color[] borderColors;
    private final BorderStyle[] borderTypes;
    private final double[] borderWidths;

    public Styling(DisplayType displayType, String fontFamily, double fontSize,
            FontWeight fontWeight, Color backgroundColor, Color textColor,
            double[] padding, double[] margin, Color[] borderColors,
            BorderStyle[] borderTypes, double[] borderWidths) {
        this.displayType = displayType;
        this.fontFamily = fontFamily;
        this.fontSize = fontSize;
        this.fontWeight = fontWeight;
        this.backgroundColor = backgroundColor;
        this.textColor = textColor;
        this.padding = padding;
        this.margin = margin;
        this.borderColors = borderColors;
        this.borderTypes = borderTypes;
        this.borderWidths = borderWidths;
    }

    /**
     * @return the displayType
     */
    public DisplayType getDisplayType() {
        return displayType;
    }

    /**
     * @return the fontFamily
     */
    public String getFontFamily() {
        return fontFamily;
    }

    /**
     * @return the fontSize
     */
    public double getFontSize() {
        return fontSize;
    }

    /**
     * @return the fontWeight
     */
    public FontWeight getFontWeight() {
        return fontWeight;
    }

    /**
     * @return the backgroundColor
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getTextColor() {
        return textColor;
    }

    /**
     * @return the padding
     */
    public double[] getPadding() {
        return padding;
    }

    /**
     * @return the margin
     */
    public double[] getMargin() {
        return margin;
    }

    /**
     * @return the borderColors
     */
    public Color[] getBorderColors() {
        return borderColors;
    }

    /**
     * @return the borderTypes
     */
    public BorderStyle[] getBorderTypes() {
        return borderTypes;
    }

    /**
     * @return the borderWidths
     */
    public double[] getBorderWidths() {
        return borderWidths;
    }
}
