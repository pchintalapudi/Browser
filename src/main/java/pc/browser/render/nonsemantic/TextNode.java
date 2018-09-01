/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import java.util.function.Function;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import org.jsoup.nodes.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.render.RenderedElement;
import pc.browser.render.css.StyleUtils;
import pc.browser.render.css.Styler;
import pc.browser.render.css.properties.FontProperties;

/**
 *
 * @author prem
 */
public class TextNode extends StackPane implements RenderedElement {

    private final Text textNode = new Text();

    public TextNode() {
        super.getChildren().add(textNode);
        super.setAlignment(Pos.TOP_LEFT);
    }

    @Override
    public Runnable applyLayoutCSS(CSSStyleDeclaration styling) {
        return () -> {
        };
    }

    @Override
    public Runnable applyPaintCSS(CSSStyleDeclaration styling) {
        return () -> {
            FontProperties props = StyleUtils.getFontProperties(styling);
            Platform.runLater(() -> {
                setAlignment(props.getVAlign());
                textNode.setTextAlignment(props.getAlign());
                textNode.setFill(props.getFontColor());
                textNode.setFont(props.getFont());
                textNode.setUnderline(props.isUnderline());
            });
        };
    }

    @Override
    public void setElement(Node element, Function<Node, javafx.scene.Node> mapper, Styler styler) {
        if (element instanceof org.jsoup.nodes.TextNode) {
            org.jsoup.nodes.TextNode text = (org.jsoup.nodes.TextNode) element;
            textNode.setText(text.text());
        }
    }
}
