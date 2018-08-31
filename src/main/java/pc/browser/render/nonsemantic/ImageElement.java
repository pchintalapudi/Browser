/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import java.util.function.Function;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.jsoup.nodes.Node;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.cache.ImageCache;
import pc.browser.render.RenderedElement;
import pc.browser.render.css.properties.LayoutProperties;
import pc.browser.render.css.properties.PaintProperties;
import pc.browser.render.css.StyleUtils;
import pc.browser.render.css.Styler;

/**
 *
 * @author prem
 */
public class ImageElement extends StackPane implements RenderedElement {

    private final ImageView imageView = new ImageView();
    private final StackPane paddingBox = new StackPane(imageView);

    public ImageElement() {
        super.getChildren().add(paddingBox);
        super.setAlignment(Pos.TOP_LEFT);
        paddingBox.setAlignment(Pos.TOP_LEFT);
    }

    @Override
    public Runnable applyLayoutCSS(CSSStyleDeclaration styling) {
        return () -> {
            LayoutProperties props = StyleUtils.getLayoutProperties(styling);
            Platform.runLater(() -> {
                setPadding(props.getMargins());
                paddingBox.setPadding(props.getPaddings());
            });
        };
    }

    @Override
    public Runnable applyPaintCSS(CSSStyleDeclaration styling) {
        return () -> {
            PaintProperties props = StyleUtils.getPaintProperties(styling, ((org.jsoup.nodes.Element) getUserData()).baseUri());
            Platform.runLater(() -> {
                paddingBox.setBorder(props.getBorder());
                paddingBox.setBackground(props.getBackground());
                paddingBox.setCursor(props.getCursor());
            });
        };
    }

    @Override
    public void setElement(Node element, Function<Node, javafx.scene.Node> mapper, Styler styler) {
        if (element.hasAttr("src") && !element.attr("src").isEmpty()) {
            Platform.runLater(() -> {
                imageView.setImage(ImageCache.getImageForUrl(element.absUrl("src")));
            });
            setUserData(element);
        }
    }
}
