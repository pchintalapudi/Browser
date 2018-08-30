/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import java.util.function.BiConsumer;
import javafx.scene.Group;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.async.RenderTask;
import pc.browser.render.nonsemantic.ImageElement;
import pc.browser.render.nonsemantic.TextAreaElement;
import pc.browser.render.nonsemantic.TextInputElement;
import pc.browser.render.nonsemantic.TextNode;

/**
 *
 * @author prem
 */
public class HTMLElementMapper {

    private final Styler styler = new Styler();
    private final BiConsumer<Runnable, RenderTask> async;

    public HTMLElementMapper(BiConsumer<Runnable, RenderTask> async) {
        this.async = async;
    }

    public javafx.scene.Node map(org.jsoup.nodes.Node node) {
        CSSStyleDeclaration style = styler.style(node);
        if (Styler.getDisplayType(style) != DisplayType.NONE) {
            if (node instanceof org.jsoup.nodes.TextNode) {
                TextNode tn = new TextNode(((org.jsoup.nodes.TextNode) node).text());
                async.accept(tn.applyCSS(style), RenderTask.PAINT);
                return tn;
            } else if (node instanceof org.jsoup.nodes.Element) {
                org.jsoup.nodes.Element element = (org.jsoup.nodes.Element) node;
                switch (element.tagName()) {
                    case "input":
                        TextInputElement tie = new TextInputElement();
                        return tie;
                    case "textarea":
                        TextAreaElement tae = new TextAreaElement();
                        return tae;
                    case "img":
                        ImageElement ie = new ImageElement();
                        return ie;
                    default:
                        ElementWrapper wrapper = new ElementWrapper();
                        wrapper.setElement(element, this::map, styler);
                        async.accept(wrapper.applyLayoutCSS(style), RenderTask.LAYOUT);
                        async.accept(wrapper.applyPaintCSS(style), RenderTask.PAINT);
                        return wrapper;
                }
            }
        }
        return new Group();
    }
}
