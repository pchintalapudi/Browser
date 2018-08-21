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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
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
    }

    private CSSStyleDeclaration style(Node domIdentity) {
        CSSStyleDeclaration style;
        if (rules == null) {
            parseStylesheet();
        }
        Element styler = domIdentity instanceof Element ? (Element) domIdentity
                : domIdentity instanceof TextNode ? (Element) domIdentity.parent() : null;
        style = styler == null ? null : rules.stream().filter(r
                -> Arrays.stream(r.getSelectorText().trim().split(",\\s*"))
                        .filter(s -> !s.contains(":")).anyMatch(styler::is))
                .map(CSSStyleRule::getStyle)
                .reduce(new CSSStyleDeclarationImpl(),
                        (d1, d2) -> {
                            for (int i = 0; i < d2.getLength(); i++) {
                                d1.setProperty(d2.item(i), d2.getPropertyValue(d2.item(i)), d2.getPropertyPriority(d2.item(i)));
                            }
                            return d1;
                        });
        style = style != null ? style : new CSSStyleDeclarationImpl();
        return style;
    }

    public javafx.scene.Node map(Document document) {
        try {
            document.getElementsByTag("link").stream().filter(Mapper::isStyleSheetLink)
                    .map(e -> e.absUrl("href")).map(InputSource::new).map(is -> {
                try {
                    return new CSSOMParser(new SACParserCSS3()).parseStyleSheet(is, null, null).getCssRules();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }).forEach(rl -> {
                try {
                    for (int i = 0; i < rl.getLength(); i++) {
                        if (rl.item(i) instanceof CSSStyleRule) {
                            rules.add((CSSStyleRule) rl.item(i));
                        }
                    }
                } catch (NullPointerException expected) {
                    //If the css file is poorly written, errors will show up here.
                }
            });
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
            CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
            Arrays.stream(node.attr("style").trim().split(";")).filter(s -> !s.isEmpty()).map(s -> {
                try {
                    return parser.parseStyleDeclaration(new InputSource(new StringReader(s)));
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }).forEach(sd -> {
                for (int i = 0; i < sd.getLength(); i++) {
                    styling.setProperty(sd.item(i), sd.getPropertyValue(sd.item(i)), sd.getPropertyPriority(sd.item(i)));
                }
            });
        }
        if (node instanceof TextNode) {
            Text t = new Text(((TextNode) node).text());
            t.setUserData(node);
            try {
                String color = styling.getPropertyValue("color");
                t.setFill(color.isEmpty() ? Color.BLACK : Color.web(color));
            } catch (IllegalArgumentException ex) {
            }
            Pair<Font, Boolean> p = getFont(styling);
            t.setFont(p.getKey());
            t.setUnderline(p.getValue());
            return new Pair<>(t, styling);
        } else if (node instanceof Element && getDisplayType(styling) != DisplayType.NONE) {
            ElementWrapper wrapper;
            if (InputElementMapper.isInputMapped(((Element) node).tagName())) {
                wrapper = new ElementWrapper(InputElementMapper.map((Element) node));
                wrapper.setStyling(styling);
                return new Pair<>(wrapper, styling);
            } else if (node.childNodeSize() > 0) {
                wrapper = new ElementWrapper(DisplayMapper.map(node, styling, this::map));
                wrapper.setStyling(styling);
                return new Pair<>(wrapper, styling);
            } else {
                Group g = new Group();
                g.setUserData(node);
                wrapper = new ElementWrapper(g);
                wrapper.setStyling(styling);
                return new Pair<>(wrapper, styling);
            }
        } else {
            Group g = new Group();
            g.setUserData(node);
            return new Pair<>(g, styling);
        }
    }

    private static DisplayType getDisplayType(CSSStyleDeclaration styling) {
        DisplayType dt = DisplayType.read(styling.getPropertyValue("display").isEmpty()
                ? "inline-block" : styling.getPropertyValue("display"));
        return dt;
    }

    private static boolean isStyleSheetLink(Element element) {
        return ((element.hasAttr("as") && element.attr("as").equals("style"))
                || (element.hasAttr("type") && element.attr("type").equals("text/css")
                && element.hasAttr("rel") && element.attr("rel").equals("stylesheet")))
                && element.hasAttr("href");
    }

    private static Pair<Font, Boolean> getFont(CSSStyleDeclaration styling) {
        String fontFamily = styling.getPropertyValue("font-family").isEmpty() ? "Arial" : styling.getPropertyValue("font-family");
        double fontSize;
        fontSize = toPixels(styling.getPropertyValue("font-size"));
        FontWeight fontWeight;
        String value = styling.getPropertyValue("font-weight").toLowerCase();
        switch (value) {
            case "bold":
                fontWeight = FontWeight.BOLD;
                break;
            default:
                try {
                    fontWeight = FontWeight.findByWeight(Integer.parseInt(value));
                    break;
                } catch (NumberFormatException ex) {
                    //Defaults to normal
                }
            //Intentional fall-through
            case "initial":
            case "inherit":
            case "normal":
                fontWeight = FontWeight.NORMAL;
                break;
            case "bolder":
                fontWeight = FontWeight.EXTRA_BOLD;
                break;
            case "lighter":
                fontWeight = FontWeight.LIGHT;
                break;
        }
        FontPosture posture = styling.getPropertyValue("font-styling").equals("italics")
                || styling.getPropertyValue("font-styling").equals("oblique")
                ? FontPosture.ITALIC : FontPosture.REGULAR;
        boolean notUnderline = styling.getPropertyValue("text-decoration-line").isEmpty()
                || !styling.getPropertyValue("text-decoration-line").contains("underline");
        return new Pair<>(Font.font(fontFamily, fontWeight, posture, fontSize), !notUnderline);
    }

    private static final Pattern lengthPattern = Pattern.compile("(\\d+(?:\\.(?:\\d+)?)?)(cm|mm|in|px|pt|pc|em|ex|ch|rem|vw|vh|vmin|vmax|\\%)?");

    private static double toPixels(String value) {
        Matcher matcher = lengthPattern.matcher(value);
        if (matcher.find()) {
            try {
                double val = Double.parseDouble(matcher.group(1));
                if (val != 0) {
                    if (matcher.start(2) == -1) {
                        return 0;
                    }
                    switch (matcher.group(2)) {
                        case "mm":
                            val /= 10;
                        case "cm":
                            val /= 2.54;
                        case "in":
                            val *= 96;
                        case "px":
                        default:
                            return val;
                        case "pt":
                            val /= 6;
                        case "pc":
                        case "em":
                            return val * 16;
                    }
                } else {
                    return 0;
                }
            } catch (Exception ex) {
                return 16;
            }
        } else {
            return 16;
        }
    }
}
