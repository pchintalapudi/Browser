/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.css;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Cursor;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
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

    private static final Pattern lengthPattern = Pattern.compile("(\\d+(?:\\.(?:\\d+)?)?)(cm|mm|in|px|pt|pc|" + "em|ex|ch|rem|vw|vh|vmin|vmax|\\%)?");

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
            } catch (Exception ex) {
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

    private Background getCSSBackground(CSSStyleDeclaration styling, String imgUrl) {
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

}
