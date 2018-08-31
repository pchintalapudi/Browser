/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import java.util.function.Function;
import javafx.application.Platform;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.StackPane;
import org.jsoup.nodes.Node;
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
public class ProgressElement extends StackPane implements RenderedElement {

    private final ProgressBar progressBar = new ProgressBar();

    public ProgressElement() {
        super.getChildren().add(progressBar);
    }

    @Override
    public Runnable applyLayoutCSS(CSSStyleDeclaration styling) {
        return () -> {
            LayoutProperties props = StyleUtils.getLayoutProperties(styling);
            Platform.runLater(() -> {
                setPadding(props.getMargins());
                progressBar.setPadding(props.getPaddings());
            });
        };
    }

    @Override
    public Runnable applyPaintCSS(CSSStyleDeclaration styling) {
        return () -> {
            PaintProperties props = StyleUtils.getPaintProperties(styling, ((org.jsoup.nodes.Element) getUserData()).baseUri());
            Platform.runLater(() -> {
                progressBar.setCursor(props.getCursor());
                progressBar.setBorder(props.getBorder());
                progressBar.setBackground(props.getBackground());
            });
        };
    }

    @Override
    public void setElement(Node element, Function<Node, javafx.scene.Node> mapper, Styler styler) {
        double progress;
        try {
            progress = Double.parseDouble(element.attr("value"));
        } catch (NumberFormatException ex) {
            progress = 0;
        }
        double prog = progress;
        Platform.runLater(() -> {
            progressBar.setProgress(prog);
        });
        setUserData(element);
    }

}
