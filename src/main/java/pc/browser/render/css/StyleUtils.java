/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.css;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.cache.ImageCache;

/**
 *
 * @author prem
 */
public class StyleUtils {

    private static final Pattern lengthPattern = Pattern.compile("(\\d+(?:\\.(?:\\d+)?)?)(cm|mm|in|px|pt|pc|"
            + "em|ex|ch|rem|vw|vh|vmin|vmax|\\%)?");

    public static Pair<Font, Boolean> getFont(CSSStyleDeclaration styling) {
        String fontFamily = styling.getPropertyValue("font-family").isEmpty() ? "Arial" : styling.getPropertyValue("font-family");
        double fontSize;
        fontSize = toPixels(styling.getPropertyValue("font-size"));
        FontWeight fontWeight;
        String value = styling.getPropertyValue("font-weight").toLowerCase();
        switch (value) {
            case "bold":
                fontWeight = FontWeight.BOLD;
                break;
            default:
                try {
                    fontWeight = FontWeight.findByWeight(Integer.parseInt(value));
                    break;
                } catch (NumberFormatException ex) {
                    //Defaults to normal
                }
            //Intentional fall-through
            case "initial":
            case "inherit":
            case "normal":
                fontWeight = FontWeight.NORMAL;
                break;
            case "bolder":
                fontWeight = FontWeight.EXTRA_BOLD;
                break;
            case "lighter":
                fontWeight = FontWeight.LIGHT;
                break;
        }
        FontPosture posture = styling.getPropertyValue("font-styling").equals("italics") || styling.getPropertyValue("font-styling").equals("oblique") ? FontPosture.ITALIC : FontPosture.REGULAR;
        boolean notUnderline = styling.getPropertyValue("text-decoration-line").isEmpty() || !styling.getPropertyValue("text-decoration-line").contains("underline");
        return new Pair<>(Font.font(fontFamily, fontWeight, posture, fontSize), !notUnderline);
    }

    public static double toPixels(String value) {
        Matcher matcher = lengthPattern.matcher(value);
        if (matcher.find()) {
            try {
                double val = Double.parseDouble(matcher.group(1));
                if (val != 0) {
                    if (matcher.start(2) == -1) {
                        return 0;
                    }
                    switch (matcher.group(2)) {
                        case "mm":
                            val /= 10;
                        case "cm":
                            val /= 2.54;
                        case "in":
                            val *= 96;
                        case "px":
                        default:
                            return val;
                        case "pt":
                            val /= 6;
                        case "pc":
                        case "em":
                            return val * 16;
                    }
                } else {
                    return 0;
                }
            } catch (NumberFormatException ex) {
                return 16;
            }
        } else {
            return 16;
        }
    }

    static <E extends Enum<E>> String toString(E val) {
        return val.name().toLowerCase().replace("_", "-");
    }

    public static DisplayType getDisplayType(CSSStyleDeclaration styling) {
        DisplayType dt = DisplayType.read(styling.getPropertyValue("display").isEmpty() ? "inline-block" : styling.getPropertyValue("display"));
        return dt;
    }

    static <E extends Enum<E>> E toEnum(String str, Class<E> enumClass) {
        return Enum.valueOf(enumClass, str.toUpperCase().replace("-", "_").trim());
    }

    public static Background getCSSBackground(CSSStyleDeclaration styling, String imgUrl) {
        String bcolor = styling.getPropertyValue("background-color");
        if (bcolor.isEmpty()) {
            bcolor = styling.getPropertyValue("background-image");
            String rep = styling.getPropertyValue("background-repeat");
            String pos = styling.getPropertyValue("background-position");
            try {
                if (bcolor.isEmpty()) {
                    return Background.EMPTY;
                } else {
                    return new Background(new BackgroundImage(ImageCache.getImageForUrl(imgUrl),
                            BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, null, BackgroundSize.DEFAULT));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                return Background.EMPTY;
            }
        } else {
            try {
                return new Background(new BackgroundFill(Color.web(bcolor), null, null));
            } catch (IllegalArgumentException ex) {
                System.out.println("styling-block:");
                System.out.println(styling.getCssText());
                return Background.EMPTY;
            }
        }
    }

    public static Cursor getCursor(CSSStyleDeclaration styling) {
        String cssValue = styling.getPropertyValue("cursor");
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
    
    public static LayoutProperties getLayoutProperties(CSSStyleDeclaration styling) {
        return new LayoutProperties(getMargins(styling), getPaddings(styling));
    }
    
    public static PaintProperties getPaintProperties(CSSStyleDeclaration styling, String baseUri) {
        return new PaintProperties(getCSSBackground(styling, baseUri), Border.EMPTY, getCursor(styling));
    }

    private static Insets getPaddings(CSSStyleDeclaration styling) {
        double[] paddings = new double[4];
        String[] shorthand = Arrays.stream(styling.getPropertyValue("padding").trim().split(" ")).filter((String s) -> !s.isEmpty()).map(String::trim).toArray(String[]::new);
        switch (shorthand.length) {
            case 1:
                Arrays.fill(paddings, StyleUtils.toPixels(shorthand[0]));
                break;
            case 2:
                double vert = StyleUtils.toPixels(shorthand[0]);
                double hor = StyleUtils.toPixels(shorthand[1]);
                paddings[0] = vert;
                paddings[1] = hor;
                paddings[2] = vert;
                paddings[3] = hor;
                break;
            case 3:
                double top = StyleUtils.toPixels(shorthand[0]);
                double lr = StyleUtils.toPixels(shorthand[1]);
                double bottom = StyleUtils.toPixels(shorthand[2]);
                paddings[0] = top;
                paddings[1] = lr;
                paddings[2] = bottom;
                paddings[3] = lr;
                break;
            case 4:
            default:
                paddings[0] = StyleUtils.toPixels(shorthand[0]);
                paddings[1] = StyleUtils.toPixels(shorthand[1]);
                paddings[2] = StyleUtils.toPixels(shorthand[2]);
                paddings[3] = StyleUtils.toPixels(shorthand[3]);
                break;
            case 0:
                break;
        }
        if (!styling.getPropertyValue("padding-top").isEmpty()) {
            paddings[0] = StyleUtils.toPixels(styling.getPropertyValue("padding-top").trim());
        }
        if (!styling.getPropertyValue("padding-right").isEmpty()) {
            paddings[1] = StyleUtils.toPixels(styling.getPropertyValue("padding-right").trim());
        }
        if (!styling.getPropertyValue("padding-bottom").isEmpty()) {
            paddings[2] = StyleUtils.toPixels(styling.getPropertyValue("padding-bottom").trim());
        }
        if (!styling.getPropertyValue("padding-left").isEmpty()) {
            paddings[3] = StyleUtils.toPixels(styling.getPropertyValue("padding-left").trim());
        }
        return new Insets(paddings[0], paddings[1], paddings[2], paddings[3]);
    }

    private static Insets getMargins(CSSStyleDeclaration styling) {
        double[] margins = new double[4];
        String[] shorthand = Arrays.stream(styling.getPropertyValue("margin").trim().split(" ")).filter((String s) -> !s.isEmpty()).map(String::trim).toArray(String[]::new);
        switch (shorthand.length) {
            case 1:
                Arrays.fill(margins, StyleUtils.toPixels(shorthand[0]));
                break;
            case 2:
                double vert = StyleUtils.toPixels(shorthand[0]);
                double hor = StyleUtils.toPixels(shorthand[1]);
                margins[0] = vert;
                margins[1] = hor;
                margins[2] = vert;
                margins[3] = hor;
                break;
            case 3:
                double top = StyleUtils.toPixels(shorthand[0]);
                double lr = StyleUtils.toPixels(shorthand[1]);
                double bottom = StyleUtils.toPixels(shorthand[2]);
                margins[0] = top;
                margins[1] = lr;
                margins[2] = bottom;
                margins[3] = lr;
                break;
            case 4:
            default:
                margins[0] = StyleUtils.toPixels(shorthand[0]);
                margins[1] = StyleUtils.toPixels(shorthand[1]);
                margins[2] = StyleUtils.toPixels(shorthand[2]);
                margins[3] = StyleUtils.toPixels(shorthand[3]);
                break;
            case 0:
                break;
        }
        if (!styling.getPropertyValue("margin-top").isEmpty()) {
            margins[0] = StyleUtils.toPixels(styling.getPropertyValue("margin-top").trim());
        }
        if (!styling.getPropertyValue("margin-right").isEmpty()) {
            margins[1] = StyleUtils.toPixels(styling.getPropertyValue("margin-right").trim());
        }
        if (!styling.getPropertyValue("margin-bottom").isEmpty()) {
            margins[2] = StyleUtils.toPixels(styling.getPropertyValue("margin-bottom").trim());
        }
        if (!styling.getPropertyValue("margin-left").isEmpty()) {
            margins[3] = StyleUtils.toPixels(styling.getPropertyValue("margin-left").trim());
        }
        return new Insets(margins[0], margins[1], margins[2], margins[3]);
    }
}
