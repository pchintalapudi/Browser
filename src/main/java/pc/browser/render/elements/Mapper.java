/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.dom.CSSStyleDeclarationImpl;
import com.steadystate.css.dom.CSSStyleSheetImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
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
        CSSStyleDeclaration style;
        if (rules == null) {
            parseStylesheet();
        }
        Element styler = domIdentity instanceof Element ? (Element) domIdentity
                : domIdentity instanceof TextNode ? (Element) domIdentity.parent() : null;
        style = styler == null ? null : rules.stream().filter(r
                -> r.getSelectorText().equals(styler.tagName())
                || r.getSelectorText().contains(" " + styler.tagName())
                || r.getSelectorText().contains(" " + styler.tagName() + ",")
                || r.getSelectorText().contains(" " + styler.tagName() + " "))
                .findFirst().map(CSSStyleRule::getStyle).orElse(null);
        style = style != null ? style : new CSSStyleDeclarationImpl();
        return style;
    }

    public javafx.scene.Node map(Document document) {
        try {
            CSSRuleList documentRules = new CSSOMParser(new SACParserCSS3()).parseStyleSheet(new InputSource(
                    new StringReader(document.getElementsByTag("style").text())), null, null).getCssRules();
            for (int i = 0; i < documentRules.getLength(); i++) {
                if (documentRules.item(i) instanceof CSSStyleRule) {
                    rules.add((CSSStyleRule) documentRules.item(i));
                }
            }
            javafx.scene.Node n = map(document.body()).getKey();
            return n;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Pair<javafx.scene.Node, CSSStyleDeclaration> map(Node node) {
        CSSStyleDeclaration styling = style(node);
        if (node.hasAttr("style")) {
            Arrays.stream(node.attr("style").trim().split(";")).filter(s -> !s.isEmpty()).map(s -> s.split(":"))
                    .peek(props -> System.out.println(Arrays.toString(props)))
                    .forEach(props -> styling.setProperty(props[0].trim(), props[1].trim(), ""));
        }
        if (node instanceof TextNode) {
            Text t = new Text(((TextNode) node).text());
            t.setUserData(node);
            try {
                String color = styling.getPropertyValue("color");
                t.setFill(color.isEmpty() ? Color.BLACK : Color.web(color));
            } catch (IllegalArgumentException ex) {
            }
            return new Pair<>(t, styling);
        } else if (node instanceof Element && getDisplayType(styling) != DisplayType.NONE) {
            if (InputElementMapper.isInputMapped(((Element) node).tagName())) {
                return new Pair<>(InputElementMapper.map((Element) node), styling);
            } else if (node.childNodeSize() > 0) {
                return new Pair<>(DisplayMapper.map(node, styling, this::map), styling);
            } else {
                Group g = new Group();
                g.setUserData(node);
                return new Pair<>(g, styling);
            }
        } else {
            Group g = new Group();
            g.setUserData(node);
            return new Pair<>(g, styling);
        }
    }

    private static DisplayType getDisplayType(CSSStyleDeclaration styling) {
        DisplayType dt = DisplayType.read(styling.getPropertyValue("display").isEmpty()
                ? "block" : styling.getPropertyValue("display"));
        return dt;
    }
}
