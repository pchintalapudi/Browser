/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

import com.steadystate.css.dom.CSSStyleDeclarationImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.util.Pair;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSImportRule;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSRuleList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;
import pc.browser.render.elements.DisplayType;
import pc.browser.resources.Resources;

/**
 *
 * @author prem
 */
public final class Styler {

    private final List<CSSStyleRule> rules = new ArrayList<>();
    private final Map<Node, CSSStyleDeclaration> cache = new WeakHashMap<>();

    public Styler() {
        try {
            appendStyleSheet(new CSSOMParser(new SACParserCSS3())
                    .parseStyleSheet(new InputSource(new BufferedReader(
                            new InputStreamReader(Resources.getCSS("blink-user-agent.css")
                                    .openStream()))), null, null));
        } catch (IOException ex) {
            Logger.getLogger(Styler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void appendStyleSheet(CSSStyleSheet stylesheet) {
        appendRules(stylesheet.getCssRules());
    }

    private void handleRule(CSSRule rule) {
        if (rule instanceof CSSStyleRule) {
            rules.add((CSSStyleRule) rule);
        } else if (rule instanceof CSSImportRule) {
            try {
                appendStyleSheet(new CSSOMParser(new SACParserCSS3()).parseStyleSheet(new InputSource(((CSSImportRule) rule).getHref()), null, ((CSSImportRule) rule).getHref()));
            } catch (IOException ex) {
                Logger.getLogger(Styler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void appendRules(List<CSSRule> rules) {
        rules.forEach(this::handleRule);
    }

    public void appendRules(CSSRuleList ruleList) {
        for (int i = 0; i < ruleList.getLength(); i++) {
            handleRule(ruleList.item(i));
        }
    }

    private static final Set<CSSInheritedProperties> AUTO_INHERIT = EnumSet.allOf(CSSInheritedProperties.class);

    public CSSStyleDeclaration style(Node domNode) {
        return cache.computeIfAbsent(domNode, n -> {
            Element styler = n instanceof Element ? (Element) n : null;
            CSSStyleDeclaration selfStyle = styler == null ? new CSSStyleDeclarationImpl() : rules.stream().filter(r
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
            if (n.parent() != null) {
                CSSStyleDeclaration parent = style(n.parent());
                CSSStyleDeclaration realStyle = new CSSStyleDeclarationImpl();
                AUTO_INHERIT.forEach(p -> realStyle.setProperty(p.toCSSProperty(),
                        parent.getPropertyValue(p.toCSSProperty()),
                        parent.getPropertyPriority(p.toCSSProperty())));
                for (int i = 0; i < selfStyle.getLength(); i++) {
                    if (selfStyle.getPropertyValue(selfStyle.item(i)).trim().equalsIgnoreCase("inherit")) {
                        realStyle.setProperty(selfStyle.item(i),
                                parent.getPropertyValue(selfStyle.item(i)),
                                parent.getPropertyPriority(selfStyle.item(i)));
                    } else {
                        realStyle.setProperty(selfStyle.item(i),
                                selfStyle.getPropertyValue(selfStyle.item(i)),
                                selfStyle.getPropertyPriority(selfStyle.item(i)));
                    }
                }
                return realStyle;
            }
            return selfStyle;
        });
    }

    public CSSStyleDeclaration parentStyle(Node domNode) {
        return cache.getOrDefault(domNode, new CSSStyleDeclarationImpl());
    }

    public static DisplayType getDisplayType(CSSStyleDeclaration styling) {
        DisplayType dt = DisplayType.read(styling.getPropertyValue("display").isEmpty()
                ? "inline-block" : styling.getPropertyValue("display"));
        return dt;
    }

    public static Pair<Font, Boolean> getFont(CSSStyleDeclaration styling) {
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

    private static final Pattern lengthPattern
            = Pattern.compile("(\\d+(?:\\.(?:\\d+)?)?)(cm|mm|in|px|pt|pc|"
                    + "em|ex|ch|rem|vw|vh|vmin|vmax|\\%)?");

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
