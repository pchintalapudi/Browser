/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import pc.browser.render.css.Styler;
import java.util.function.Function;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.events.LoadEvent;
import pc.browser.render.css.properties.LayoutProperties;
import pc.browser.render.css.properties.PaintProperties;
import pc.browser.render.css.StyleUtils;

/**
 *
 * @author prem
 */
public class ElementWrapper extends StackPane implements RenderedElement {

    private final ChildLayoutManager contentBox = new ChildLayoutManager();
    private final StackPane paddingBox = new StackPane(contentBox),
            borderBox = new StackPane(paddingBox), marginBox = new StackPane(borderBox);

    public ElementWrapper() {
        super.getChildren().add(marginBox);
        setAlignment(Pos.TOP_LEFT);
        marginBox.setAlignment(Pos.TOP_LEFT);
        borderBox.setAlignment(Pos.TOP_LEFT);
        contentBox.setAlignment(Pos.TOP_LEFT);
    }

    public ObservableList<Node> getDOMChildren() {
        return contentBox.getChildren();
    }

    @Override
    public Runnable applyLayoutCSS(CSSStyleDeclaration styling) {
        return () -> {
            LayoutProperties props = StyleUtils.getLayoutProperties(styling);
            Platform.runLater(() -> {
                marginBox.setPadding(props.getMargins());
                paddingBox.setPadding(props.getPaddings());
            });
        };
    }

    @Override
    public Runnable applyPaintCSS(CSSStyleDeclaration styling) {
        return () -> {
            PaintProperties props = StyleUtils.getPaintProperties(styling, ((org.jsoup.nodes.Element) getUserData()).baseUri());
            Platform.runLater(() -> {
                paddingBox.setBackground(props.getBackground());
                borderBox.setBorder(props.getBorder());
                borderBox.setCursor(props.getCursor());
                borderBox.setOpacity(props.getOpacity());
            });
        };
    }

    @Override
    public void setElement(org.jsoup.nodes.Node element, Function<org.jsoup.nodes.Node, Node> mapper, Styler styler) {
        if (element instanceof org.jsoup.nodes.Element) {
            if (element.hasAttr("href") && !element.absUrl("href").isEmpty()) {
                borderBox.setOnMouseClicked(m -> {
                    m.consume();
                    if (m.getButton() == MouseButton.PRIMARY) {
                        fireEvent(new LoadEvent(element.absUrl("href")));
                    }
                });
            } else {
                borderBox.setOnMouseClicked(null);
            }
            contentBox.setStyler(styler);
            contentBox.setMapper(mapper);
            setUserData(element);
            Platform.runLater(() -> {
                getProperties().put("", StyleUtils.getDisplayType(styler.style(element)));
            });
            contentBox.manage((org.jsoup.nodes.Element) element);
        }
    }
}
