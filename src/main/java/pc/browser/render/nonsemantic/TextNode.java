/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import javafx.application.Platform;
import javafx.scene.text.Text;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.render.css.StyleUtils;
import pc.browser.render.css.properties.FontProperties;

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
            FontProperties props = StyleUtils.getFontProperties(styling);
            Platform.runLater(() -> {
                setFont(props.getFont());
                setUnderline(props.isUnderline());
                setFill(props.getFontColor());
            });
        };
    }
}
