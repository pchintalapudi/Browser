/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import pc.browser.render.css.DisplayType;
import pc.browser.render.css.Styler;
import java.util.function.BiConsumer;
import javafx.scene.Group;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.async.RenderTask;
import pc.browser.render.css.StyleUtils;
import pc.browser.render.nonsemantic.ImageElement;
import pc.browser.render.nonsemantic.TextAreaElement;
import pc.browser.render.nonsemantic.TextInputElement;
import pc.browser.render.nonsemantic.TextNode;

/**
 *
 * @author prem
 */
public class HTMLElementMapper {

    private Styler styler = new Styler();
    private final BiConsumer<Runnable, RenderTask> async;

    public HTMLElementMapper(BiConsumer<Runnable, RenderTask> async) {
        this.async = async;
    }

    public javafx.scene.Node map(org.jsoup.nodes.Document document) {
        this.styler = new Styler();
        document.getElementsByTag("link").stream().filter(HTMLElementMapper::isStyleSheetLink)
                .map(e -> e.absUrl("href")).map(InputSource::new).forEach(is -> {
            try {
                styler.appendStyleSheet(new CSSOMParser(new SACParserCSS3()).parseStyleSheet(is, null, null));
            } catch (IOException ex) {
            }
        });
        try {
            styler.appendStyleSheet(new CSSOMParser(new SACParserCSS3()).parseStyleSheet(new InputSource(
                    new StringReader(document.getElementsByTag("style").text())), null, null));
        } catch (IOException ex) {
        }
        return map(document.body());
    }

    private javafx.scene.Node map(org.jsoup.nodes.Node node) {
        CSSStyleDeclaration style = styler.style(node);
        if (StyleUtils.getDisplayType(style) != DisplayType.NONE) {
            if (node instanceof org.jsoup.nodes.TextNode) {
                TextNode tn = new TextNode(((org.jsoup.nodes.TextNode) node).text());
                tn.setUserData(node);
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

    private static boolean isStyleSheetLink(org.jsoup.nodes.Element element) {
        return ((element.hasAttr("as") && element.attr("as").equals("style"))
                || (element.hasAttr("type") && element.attr("type").equals("text/css")
                && element.hasAttr("rel") && element.attr("rel").equals("stylesheet")))
                && element.hasAttr("href");
    }
}
