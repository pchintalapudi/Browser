/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import java.util.function.Function;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.render.RenderedElement;
import pc.browser.render.css.properties.LayoutProperties;
import pc.browser.render.css.properties.PaintProperties;
import pc.browser.render.css.StyleUtils;
import pc.browser.render.css.Styler;

/**
 *
 * @author prem
 */
public class ButtonElement extends StackPane implements RenderedElement {

    private final Button actualButton = new Button();

    public ButtonElement() {
        super.getChildren().add(actualButton);
        super.setAlignment(Pos.TOP_LEFT);
    }

    @Override
    public Runnable applyLayoutCSS(CSSStyleDeclaration styling) {
        return () -> {
            LayoutProperties props = StyleUtils.getLayoutProperties(styling);
            Platform.runLater(() -> {
                actualButton.setPadding(props.getPaddings());
                setPadding(props.getMargins());
            });
        };
    }

    @Override
    public Runnable applyPaintCSS(CSSStyleDeclaration styling) {
        return () -> {
            PaintProperties props = StyleUtils.getPaintProperties(styling, ((org.jsoup.nodes.Element) getUserData()).baseUri());
            Platform.runLater(() -> {
                actualButton.setBackground(props.getBackground());
                actualButton.setBorder(props.getBorder());
                actualButton.setCursor(props.getCursor());
            });
        };
    }

    @Override
    public void setElement(org.jsoup.nodes.Node element, Function<org.jsoup.nodes.Node, Node> mapper, Styler styler) {
        setUserData(element);
        actualButton.setText(((org.jsoup.nodes.Element) element).text());
    }

}
