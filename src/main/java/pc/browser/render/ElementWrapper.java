/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import java.util.Arrays;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import org.jsoup.nodes.Element;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 *
 * @author prem
 */
public class ElementWrapper extends StackPane {

    private final ChildLayoutManager contentBox = new ChildLayoutManager();
    private final StackPane paddingBox = new StackPane(contentBox),
            borderBox = new StackPane(paddingBox), marginBox = new StackPane(borderBox);

    public ElementWrapper() {
        super.getChildren().add(marginBox);
    }

    public ObservableList<Node> getDOMChildren() {
        return contentBox.getChildren();
    }

    public void applyCSS(CSSStyleDeclaration styling) {
        marginBox.setPadding(getMargins(styling));
        paddingBox.setPadding(getPaddings(styling));
    }

    private Insets getMargins(CSSStyleDeclaration styling) {
        double[] margins = new double[4];
        String[] shorthand = Arrays.stream(styling.getPropertyValue("margin").trim().split(" "))
                .filter(s -> !s.isEmpty()).map(String::trim).toArray(String[]::new);
        switch (shorthand.length) {
            case 1:
                Arrays.fill(margins, Styler.toPixels(shorthand[0]));
                break;
            case 2:
                double vert = Styler.toPixels(shorthand[0]),
                 hor = Styler.toPixels(shorthand[1]);
                margins[0] = vert;
                margins[1] = hor;
                margins[2] = vert;
                margins[3] = hor;
                break;
            case 3:
                double top = Styler.toPixels(shorthand[0]),
                 lr = Styler.toPixels(shorthand[1]),
                 bottom = Styler.toPixels(shorthand[2]);
                margins[0] = top;
                margins[1] = lr;
                margins[2] = bottom;
                margins[3] = lr;
                break;
            case 4:
            default:
                margins[0] = Styler.toPixels(shorthand[0]);
                margins[1] = Styler.toPixels(shorthand[1]);
                margins[2] = Styler.toPixels(shorthand[2]);
                margins[3] = Styler.toPixels(shorthand[3]);
                break;
            case 0:
                break;
        }
        if (!styling.getPropertyValue("margin-top").isEmpty()) {
            margins[0] = Styler.toPixels(styling.getPropertyValue("margin-top").trim());
        }
        if (!styling.getPropertyValue("margin-right").isEmpty()) {
            margins[1] = Styler.toPixels(styling.getPropertyValue("margin-right").trim());
        }
        if (!styling.getPropertyValue("margin-bottom").isEmpty()) {
            margins[2] = Styler.toPixels(styling.getPropertyValue("margin-bottom").trim());
        }
        if (!styling.getPropertyValue("margin-left").isEmpty()) {
            margins[3] = Styler.toPixels(styling.getPropertyValue("margin-left").trim());
        }
        return new Insets(margins[0], margins[1], margins[2], margins[3]);
    }

    private Insets getPaddings(CSSStyleDeclaration styling) {
        double[] paddings = new double[4];
        String[] shorthand = Arrays.stream(styling.getPropertyValue("padding").trim().split(" "))
                .filter(s -> !s.isEmpty()).map(String::trim).toArray(String[]::new);
        switch (shorthand.length) {
            case 1:
                Arrays.fill(paddings, Styler.toPixels(shorthand[0]));
                break;
            case 2:
                double vert = Styler.toPixels(shorthand[0]),
                 hor = Styler.toPixels(shorthand[1]);
                paddings[0] = vert;
                paddings[1] = hor;
                paddings[2] = vert;
                paddings[3] = hor;
                break;
            case 3:
                double top = Styler.toPixels(shorthand[0]),
                 lr = Styler.toPixels(shorthand[1]),
                 bottom = Styler.toPixels(shorthand[2]);
                paddings[0] = top;
                paddings[1] = lr;
                paddings[2] = bottom;
                paddings[3] = lr;
                break;
            case 4:
            default:
                paddings[0] = Styler.toPixels(shorthand[0]);
                paddings[1] = Styler.toPixels(shorthand[1]);
                paddings[2] = Styler.toPixels(shorthand[2]);
                paddings[3] = Styler.toPixels(shorthand[3]);
                break;
            case 0:
                break;
        }
        if (!styling.getPropertyValue("padding-top").isEmpty()) {
            paddings[0] = Styler.toPixels(styling.getPropertyValue("padding-top").trim());
        }
        if (!styling.getPropertyValue("padding-right").isEmpty()) {
            paddings[1] = Styler.toPixels(styling.getPropertyValue("padding-right").trim());
        }
        if (!styling.getPropertyValue("padding-bottom").isEmpty()) {
            paddings[2] = Styler.toPixels(styling.getPropertyValue("padding-bottom").trim());
        }
        if (!styling.getPropertyValue("padding-left").isEmpty()) {
            paddings[3] = Styler.toPixels(styling.getPropertyValue("padding-left").trim());
        }
        return new Insets(paddings[0], paddings[1], paddings[2], paddings[3]);
    }

    public void setElement(org.jsoup.nodes.Node node) {
    }
}
