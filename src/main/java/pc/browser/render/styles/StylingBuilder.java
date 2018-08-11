/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

import java.util.Arrays;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

public class StylingBuilder {

    private DisplayType displayType = DisplayType.BLOCK;
    private String fontFamily = "Arial";
    private double fontSize = 16;
    private FontWeight fontWeight = FontWeight.NORMAL;
    private Color backgroundColor = Color.WHITE;
    private Color textColor = Color.BLACK;
    private double[] padding = new double[4];
    private double[] margin = new double[4];
    private Color[] borderColors = new Color[4];
    private BorderStyle[] borderTypes = new BorderStyle[4];
    private double[] borderWidths = new double[4];

    public StylingBuilder() {
        Arrays.fill(borderColors, Color.BLACK);
        Arrays.fill(borderTypes, BorderStyle.NONE);
        Arrays.fill(borderWidths, 1);
    }

    public StylingBuilder setDisplayType(DisplayType displayType) {
        this.displayType = displayType;
        return this;
    }

    public StylingBuilder setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
        return this;
    }

    public StylingBuilder setFontSize(double fontSize) {
        this.fontSize = fontSize;
        return this;
    }

    public StylingBuilder setFontWeight(FontWeight fontWeight) {
        this.fontWeight = fontWeight;
        return this;
    }

    public StylingBuilder setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        return this;
    }

    public StylingBuilder setTextColor(Color textColor) {
        this.textColor = textColor;
        return this;
    }

    public StylingBuilder setPadding(double padding) {
        return setPadding(expand(padding));
    }

    public StylingBuilder setPadding(double vertical, double horizontal) {
        return setPadding(expand(vertical, horizontal));
    }

    public StylingBuilder setPadding(double top, double bottom, double left, double right) {
        return setPadding(expand(top, bottom, left, right));
    }

    private StylingBuilder setPadding(double[] padding) {
        this.padding = padding;
        return this;
    }

    public StylingBuilder setMargin(double margin) {
        return setMargin(expand(margin));
    }

    public StylingBuilder setMargin(double vertical, double horizontal) {
        return setMargin(expand(vertical, horizontal));
    }

    public StylingBuilder setMargin(double top, double bottom, double left, double right) {
        return setMargin(expand(top, bottom, left, right));
    }

    private StylingBuilder setMargin(double[] margins) {
        this.margin = margins;
        return this;
    }

    public StylingBuilder setBorderColors(Color borderColor) {
        return setBorderColors(expand(borderColor));
    }

    public StylingBuilder setBorderColors(Color vertical, Color horizontal) {
        return setBorderColors(expand(vertical, horizontal));
    }

    public StylingBuilder setBorderColors(Color top, Color bottom, Color left, Color right) {
        return setBorderColors(expand(top, bottom, left, right));
    }

    private StylingBuilder setBorderColors(Color[] borderColors) {
        this.borderColors = borderColors;
        return this;
    }

    public StylingBuilder setBorderStyles(BorderStyle borderStyle) {
        return setBorderStyles(expand(borderStyle));
    }

    public StylingBuilder setBorderStyles(BorderStyle vertical, BorderStyle horizontal) {
        return setBorderStyles(expand(vertical, horizontal));
    }

    public StylingBuilder setBorderStyles(BorderStyle top, BorderStyle bottom, BorderStyle left, BorderStyle right) {
        return setBorderStyles(expand(top, bottom, left, right));
    }

    private StylingBuilder setBorderStyles(BorderStyle[] borderStyles) {
        this.borderTypes = borderStyles;
        return this;
    }

    public StylingBuilder setBorderWidth(double borderWidth) {
        return setBorderWidth(expand(borderWidth));
    }

    public StylingBuilder setBorderWidth(double vertical, double horizontal) {
        return setBorderWidth(expand(vertical, horizontal));
    }

    public StylingBuilder setBorderWidth(double top, double bottom, double left, double right) {
        return setBorderWidth(expand(top, bottom, left, right));
    }

    private StylingBuilder setBorderWidth(double[] borderWidths) {
        this.borderWidths = borderWidths;
        return this;
    }

    private static double[] expand(double val) {
        double[] vals = new double[4];
        Arrays.fill(vals, val);
        return vals;
    }

    private static double[] expand(double val1, double val2) {
        return new double[]{val1, val1, val2, val2};
    }

    private static double[] expand(double val1, double val2, double val3, double val4) {
        return new double[]{val1, val2, val3, val4};
    }

    private static <T> T[] expand(T val) {
        T[] vals = (T[]) new Object[4];
        Arrays.fill(vals, val);
        return vals;
    }

    private static <T> T[] expand(T val1, T val2) {
        return (T[]) new Object[]{val1, val1, val2, val2};
    }

    private static <T> T[] expand(T val1, T val2, T val3, T val4) {
        return (T[]) new Object[]{val1, val2, val3, val4};
    }

    public Styling createStyling() {
        return new Styling(displayType, fontFamily, fontSize, fontWeight, backgroundColor,
                textColor, padding, margin, borderColors, borderTypes, borderWidths);
    }

    public static StylingBuilder create() {
        return new StylingBuilder();
    }

    public static StylingBuilder create(Styling copy) {
        return new StylingBuilder().setBackgroundColor(copy.getBackgroundColor())
                .setBorderColors(copy.getBorderColors()).setBorderStyles(copy.getBorderTypes())
                .setBorderWidth(copy.getBorderWidths()).setDisplayType(copy.getDisplayType())
                .setFontFamily(copy.getFontFamily()).setFontSize(copy.getFontSize())
                .setFontWeight(copy.getFontWeight()).setMargin(copy.getMargin())
                .setPadding(copy.getPadding());
    }
}
