/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.render.css.Styler;
import pc.browser.render.css.StyleUtils;

/**
 *
 * @author prem
 */
public class TextNode extends Text {

    public TextNode() {
    }

    public TextNode(String text) {
        super(text);
    }

    public TextNode(double x, double y, String text) {
        super(x, y, text);
    }

    public Runnable applyCSS(CSSStyleDeclaration styling) {
        return () -> {
            String color = styling.getPropertyValue("color");
            try {
                Platform.runLater(() -> setFill(color.isEmpty() ? Color.BLACK : Color.web(color)));
            } catch (IllegalArgumentException ex) {
                System.out.println("Failed color: " + color);
            }
            Pair<Font, Boolean> p = StyleUtils.getFont(styling);
            Platform.runLater(() -> {
                setFont(p.getKey());
                setUnderline(p.getValue());
            });
        };
    }
}
