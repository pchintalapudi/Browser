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
import javafx.scene.Node;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;
import javafx.scene.text.TextFlow;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.events.LoadEvent;
import pc.browser.render.css.properties.LayoutProperties;
import pc.browser.render.css.properties.PaintProperties;
import pc.browser.render.css.StyleUtils;

/**
 *
 * @author prem
 */
public class ElementWrapper extends TextFlow implements RenderedElement {

    private final ChildLayoutManager contentBox = new ChildLayoutManager();
    private final TextFlow paddingBox = new TextFlow(contentBox),
            borderBox = new TextFlow(paddingBox), marginBox = new TextFlow(borderBox);

    public ElementWrapper() {
        super.getChildren().add(marginBox);
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
                Region r = props.isContentBox() ? contentBox : borderBox;
                r.setMinSize(props.getMinWidth(), props.getMinHeight());
                r.setPrefSize(props.getWidth(), props.getHeight());
                r.setMaxSize(props.getMaxWidth(), props.getMaxHeight());
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
                borderBox.setOnDragDetected(m -> {
                    ClipboardContent cc = new ClipboardContent();
                    cc.putUrl(element.absUrl("href"));
                    borderBox.startDragAndDrop(TransferMode.ANY).setContent(cc);
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
