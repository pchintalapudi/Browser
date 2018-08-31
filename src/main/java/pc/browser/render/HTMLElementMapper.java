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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import pc.browser.render.css.DisplayType;
import pc.browser.render.css.Styler;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.async.RenderTask;
import pc.browser.render.css.StyleUtils;
import pc.browser.render.nonsemantic.ButtonElement;
import pc.browser.render.nonsemantic.ImageElement;
import pc.browser.render.nonsemantic.ProgressElement;
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
                if (nonSemanticElements.contains(element.tagName())) {
                    return mapNonSemantics(element);
                } else {
                    ElementWrapper wrapper = new ElementWrapper();
                    async.accept(() -> {
                        wrapper.setElement(element, this::map, styler);
                        async.accept(wrapper.applyLayoutCSS(style), RenderTask.LAYOUT);
                        async.accept(wrapper.applyPaintCSS(style), RenderTask.PAINT);
                    }, RenderTask.LAYOUT);
                    return wrapper;
                }
            } else {
                return new Group();
            }
        } else {
            return new Group();
        }
    }

    private static boolean isStyleSheetLink(org.jsoup.nodes.Element element) {
        return ((element.hasAttr("as") && element.attr("as").equals("style"))
                || (element.hasAttr("type") && element.attr("type").equals("text/css")
                && element.hasAttr("rel") && element.attr("rel").equals("stylesheet")))
                && element.hasAttr("href");
    }

    private static final Set<String> nonSemanticElements = new HashSet<>(
            Arrays.asList("img", "button", "select", "datalist", "input",
                    "textarea", "progress"));

    private javafx.scene.Node mapNonSemantics(org.jsoup.nodes.Element element) {
        CSSStyleDeclaration styling = styler.style(element);
        switch (element.tagName()) {
            case "img":
                if (element.hasAttr("src") && !element.attr("src").isEmpty() && !element.absUrl("src").isEmpty()) {
                    ImageElement ie = new ImageElement();
                    ie.setElement(element, this::map, styler);
                    async.accept(ie.applyLayoutCSS(styling), RenderTask.LAYOUT);
                    async.accept(ie.applyPaintCSS(styling), RenderTask.PAINT);
                    return ie;
                } else {
                    return new Group();
                }
            case "button":
                ButtonElement button = new ButtonElement();
                button.setElement(element, this::map, styler);
                async.accept(button.applyLayoutCSS(styling), RenderTask.LAYOUT);
                async.accept(button.applyPaintCSS(styling), RenderTask.PAINT);
                return button;
            case "select":
                List<String> options = retrieveOptions(element);
                ChoiceBox<String> choices = new ChoiceBox<>(FXCollections.observableArrayList(options));
                choices.getSelectionModel().selectFirst();
                return choices;
            case "datalist":
                options = retrieveOptions(element);
                ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(options));
                return combo;
            case "input":
                return new TextInputElement(element.hasAttr("value") ? element.attr("value") : "");
            case "textarea":
                return new TextAreaElement(element.text());
            case "progress":
                ProgressElement progress = new ProgressElement();
                progress.setElement(element, this::map, styler);
                async.accept(progress.applyLayoutCSS(styling), RenderTask.LAYOUT);
                async.accept(progress.applyPaintCSS(styling), RenderTask.PAINT);
                return progress;
            default:
                return new Group();
        }
    }

    private static List<String> retrieveOptions(org.jsoup.nodes.Element selectOrDatalist) {
        return selectOrDatalist.childNodes().stream().filter(org.jsoup.nodes.Element.class::isInstance)
                .map(org.jsoup.nodes.Element.class::cast)
                .flatMap(e -> e.tagName().equals("optgroup") ? recursiveBreakdown(e)
                : e.tagName().equals("option") ? Stream.of(e) : Stream.empty())
                .map(org.jsoup.nodes.Element::text)
                .collect(Collectors.toList());
    }

    private static Stream<org.jsoup.nodes.Element> recursiveBreakdown(org.jsoup.nodes.Element optGroup) {
        return optGroup.childNodes().stream().filter(org.jsoup.nodes.Element.class::isInstance)
                .map(org.jsoup.nodes.Element.class::cast)
                .flatMap(e -> e.tagName().equals("optgroup") ? recursiveBreakdown(e)
                : e.tagName().equals("option") ? Stream.of(e) : Stream.empty());
    }
}
