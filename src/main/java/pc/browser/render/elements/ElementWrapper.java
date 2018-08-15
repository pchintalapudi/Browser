/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.dom.CSSStyleDeclarationImpl;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 *
 * @author prem
 */
public class ElementWrapper extends StackPane {

    private static class InnerWrapper extends StackPane {

        public InnerWrapper() {
        }

        public InnerWrapper(Node... children) {
            super(children);
        }

    }

    private final InnerWrapper inner;
    private final ObjectProperty<CSSStyleDeclaration> stylingProperty = new SimpleObjectProperty<>(new CSSStyleDeclarationImpl());

    public ElementWrapper(Node n) {
        super();
        setUserData(n.getUserData());
        n.setUserData(null);
        super.getChildren().add(inner = new InnerWrapper(n));
        super.setAlignment(Pos.TOP_LEFT);
        inner.setAlignment(Pos.TOP_LEFT);
        inner.backgroundProperty().bind(Bindings.createObjectBinding(()
                -> stylingProperty.get().getPropertyValue("background-color").isEmpty()
                ? Background.EMPTY : new Background(new BackgroundFill(
                        Color.web(stylingProperty.get().getPropertyValue("background-color")), null, null)),
                stylingProperty));
        paddingProperty().bind(Bindings.createObjectBinding(this::getMargins, stylingProperty));
        inner.paddingProperty().bind(Bindings.createObjectBinding(this::getPaddings, stylingProperty));
    }

    private Insets getMargins() {
        CSSStyleDeclaration styling = stylingProperty.get();
        double[] margins = new double[4];
        String[] shorthand = Arrays.stream(styling.getPropertyValue("margin").trim().split(" "))
                .filter(s -> !s.isEmpty()).map(String::trim).toArray(String[]::new);
        switch (shorthand.length) {
            case 1:
                Arrays.fill(margins, toPixels(shorthand[0]));
                break;
            case 2:
                double vert = toPixels(shorthand[0]),
                 hor = toPixels(shorthand[1]);
                margins[0] = vert;
                margins[1] = hor;
                margins[2] = vert;
                margins[3] = hor;
                break;
            case 3:
                double top = toPixels(shorthand[0]),
                 lr = toPixels(shorthand[1]),
                 bottom = toPixels(shorthand[2]);
                margins[0] = top;
                margins[1] = lr;
                margins[2] = bottom;
                margins[3] = lr;
                break;
            case 4:
            default:
                margins[0] = toPixels(shorthand[0]);
                margins[1] = toPixels(shorthand[1]);
                margins[2] = toPixels(shorthand[2]);
                margins[3] = toPixels(shorthand[3]);
                break;
            case 0:
                break;
        }
        if (!styling.getPropertyValue("margin-top").isEmpty()) {
            margins[0] = toPixels(styling.getPropertyValue("margin-top").trim());
        }
        if (!styling.getPropertyValue("margin-right").isEmpty()) {
            margins[1] = toPixels(styling.getPropertyValue("margin-right").trim());
        }
        if (!styling.getPropertyValue("margin-bottom").isEmpty()) {
            margins[2] = toPixels(styling.getPropertyValue("margin-bottom").trim());
        }
        if (!styling.getPropertyValue("margin-left").isEmpty()) {
            margins[3] = toPixels(styling.getPropertyValue("margin-left").trim());
        }
        return new Insets(margins[0], margins[1], margins[2], margins[3]);
    }

    private Insets getPaddings() {
        CSSStyleDeclaration styling = stylingProperty.get();
        double[] paddings = new double[4];
        String[] shorthand = Arrays.stream(styling.getPropertyValue("padding").trim().split(" "))
                .filter(s -> !s.isEmpty()).map(String::trim).toArray(String[]::new);
        switch (shorthand.length) {
            case 1:
                Arrays.fill(paddings, toPixels(shorthand[0]));
                break;
            case 2:
                double vert = toPixels(shorthand[0]),
                 hor = toPixels(shorthand[1]);
                paddings[0] = vert;
                paddings[1] = hor;
                paddings[2] = vert;
                paddings[3] = hor;
                break;
            case 3:
                double top = toPixels(shorthand[0]),
                 lr = toPixels(shorthand[1]),
                 bottom = toPixels(shorthand[2]);
                paddings[0] = top;
                paddings[1] = lr;
                paddings[2] = bottom;
                paddings[3] = lr;
                break;
            case 4:
            default:
                paddings[0] = toPixels(shorthand[0]);
                paddings[1] = toPixels(shorthand[1]);
                paddings[2] = toPixels(shorthand[2]);
                paddings[3] = toPixels(shorthand[3]);
                break;
            case 0:
                break;
        }
        if (!styling.getPropertyValue("padding-top").isEmpty()) {
            paddings[0] = toPixels(styling.getPropertyValue("padding-top").trim());
        }
        if (!styling.getPropertyValue("padding-right").isEmpty()) {
            paddings[1] = toPixels(styling.getPropertyValue("padding-right").trim());
        }
        if (!styling.getPropertyValue("padding-bottom").isEmpty()) {
            paddings[2] = toPixels(styling.getPropertyValue("padding-bottom").trim());
        }
        if (!styling.getPropertyValue("padding-left").isEmpty()) {
            paddings[3] = toPixels(styling.getPropertyValue("padding-left").trim());
        }
        return new Insets(paddings[0], paddings[1], paddings[2], paddings[3]);
    }

    private static final Pattern lengthPattern = Pattern.compile("(\\d+(?:\\.(?:\\d+)?)?)(cm|mm|in|px|pt|pc|em|ex|ch|rem|vw|vh|vmin|vmax|\\%)?");

    private static double toPixels(String value) {
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
                            return val * 16;
                    }
                }
            } catch (Exception ex) {
                System.out.println(matcher);
                return 0;
            }
        } else {
            return 0;
        }
        return 0;
    }

    public void setStyling(CSSStyleDeclaration styling) {
        stylingProperty.set(styling);
    }
}
