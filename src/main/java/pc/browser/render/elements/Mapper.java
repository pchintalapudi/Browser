/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

/**
 *
 * @author prem
 */
public class Mapper {

    private final CSSStyleSheet stylesheet;
    private List<CSSStyleRule> rules;
    private ElementWrapper wrapper = new ElementWrapper(new Insets(0), new Insets(0), new Insets(0));

    public Mapper(CSSStyleSheet stylesheet) {
        this.stylesheet = stylesheet;
    }

    public Mapper appendStyleSheet(CSSStyleSheetImpl stylesheet) {
        stylesheet.setParentStyleSheet(this.stylesheet);
        return new Mapper(stylesheet);
    }

    private void parseStylesheet() {
        rules = new ArrayList<>();
        for (int i = 0; i < stylesheet.getCssRules().getLength(); i++) {
            CSSRule rule = stylesheet.getCssRules().item(i);
            if (rule instanceof CSSStyleRule) {
                rules.add((CSSStyleRule) rule);
            }
        }
        rules.sort(Mapper::prioritizer);
    }

    private static final Pattern multispaceCompressor = Pattern.compile(" +");

    private static int prioritizer(CSSStyleRule r1, CSSStyleRule r2) {
        String selector1 = multispaceCompressor.matcher(r1.getSelectorText()).replaceAll(" ");
        String selector2 = multispaceCompressor.matcher(r2.getSelectorText()).replaceAll(" ");
        return 0;
    }

    private CSSStyleDeclaration style(Node domIdentity) {
        if (rules == null) {
            parseStylesheet();
        }
        Element styler = domIdentity instanceof Element ? (Element) domIdentity
                : domIdentity instanceof TextNode ? (Element) domIdentity.parent() : null;
        return styler == null ? null : rules.stream().filter(r
                -> r.getSelectorText().contains(" " + styler.tagName())
                || r.getSelectorText().contains(" " + styler.tagName() + ",")
                || r.getSelectorText().contains(" " + styler.tagName() + " "))
                .findFirst().map(CSSStyleRule::getStyle).orElse(null);
    }

    public javafx.scene.Node map(Document document) {
        javafx.scene.Node n = map(document.body()).getKey();
        return n;
    }

    private Pair<javafx.scene.Node, CSSStyleDeclaration> map(Node node) {
        CSSStyleDeclaration styling = style(node);
        DisplayType displayType;
        if (node instanceof TextNode) {
            return new Pair<>(new Text(((TextNode) node).text()), styling);
        } else if (node instanceof Element && (displayType = getDisplayType(styling)) != DisplayType.NONE) {
            if (((Element) node).tagName().equals("input")) {
                styling.setProperty("display", "inline-block", "");
                return new Pair<>(new TextField(), styling);
            } else if (node.childNodeSize() > 0) {
                switch (displayType) {
                    default:
                        VBox content = new VBox();
                        List<Pair<javafx.scene.Node, CSSStyleDeclaration>> mapped = node.childNodes().stream().map(this::map).collect(Collectors.toList());
                        HBox current = new HBox(mapped.get(0).getKey());
                        for (int i = 1; i < mapped.size(); i++) {
                            if (getDisplayType(styling) != DisplayType.NONE) {
                                if (DisplayType.isInline(getDisplayType(mapped.get(i).getValue()))) {
                                    current.getChildren().add(mapped.get(i).getKey());
                                } else {
                                    if (current.getChildren().size() > 1) {
                                        System.out.println(current.getChildren());
                                    }
                                    content.getChildren().add(current);
                                    current = new HBox(mapped.get(i).getKey());
                                }
                            }
                        }
                        content.getChildren().add(current);
                        return new Pair<>(wrapper.wrap(content, Collections.emptyList(), Color.TRANSPARENT), styling);
                }
            } else {
                return new Pair<>(wrapper.wrap(new Group(), Collections.emptyList(), Color.TRANSPARENT), styling);
            }
        } else {
            return new Pair<>(new Group(), styling);
        }
    }

    private static DisplayType getDisplayType(CSSStyleDeclaration styling) {
        try {
            DisplayType dt = DisplayType.read(styling == null ? "block"
                    : styling.getPropertyValue("display") == null ? "block"
                    : styling.getPropertyValue("display"));
            return dt;
        } catch (IllegalArgumentException ex) {
            return DisplayType.BLOCK;
        }
    }
}
