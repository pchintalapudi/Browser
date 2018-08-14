/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import javafx.scene.Group;
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
            Text t = new Text(((TextNode) node).text());
            if (styling != null) {
                String color = styling.getPropertyValue("color");
                t.setFill(color == null || color.isEmpty() ? Color.BLACK : Color.web(color));
            }
            return new Pair<>(t, styling);
        } else if (node instanceof Element && (displayType = getDisplayType(styling)) != DisplayType.NONE) {
            if (InputElementMapper.isInputMapped(((Element) node).tagName())) {
                return new Pair<>(InputElementMapper.map((Element) node), styling);
            } else if (node.childNodeSize() > 0) {
                return new Pair<>(DisplayMapper.map(node, styling, this::map), styling);
            } else {
                return new Pair<>(new Group(), styling);
            }
        } else {
            return new Pair<>(new Group(), styling);
        }
    }

    private static DisplayType getDisplayType(CSSStyleDeclaration styling) {
        System.out.println(styling);
        DisplayType dt = DisplayType.read(styling == null ? "block"
                : styling.getPropertyValue("display").isEmpty() ? "block"
                : styling.getPropertyValue("display"));
        System.out.println(dt + "\n");
        return dt;
    }
}
