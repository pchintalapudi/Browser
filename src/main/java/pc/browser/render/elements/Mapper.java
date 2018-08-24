/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.render.styles.Styler;

/**
 *
 * @author prem
 */
public class Mapper {

    private final Styler styler = new Styler();

    public javafx.scene.Node map(Document document) {
        try {
            document.getElementsByTag("link").stream().filter(Mapper::isStyleSheetLink)
                    .map(e -> e.absUrl("href")).map(InputSource::new).forEach(is -> {
                try {
                    styler.appendStyleSheet(new CSSOMParser(new SACParserCSS3()).parseStyleSheet(is, null, null));
                } catch (IOException ex) {
                }
            });
            styler.appendStyleSheet(new CSSOMParser(new SACParserCSS3()).parseStyleSheet(new InputSource(
                    new StringReader(document.getElementsByTag("style").text())), null, null));
            return map(document.body());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private javafx.scene.Node map(Node node) {
        CSSStyleDeclaration styling = styler.style(node);
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
            Text t = new Text(((TextNode) node).text().replace("&nbsp;", "\u00A0"));
            t.setUserData(node);
            String color = styling.getPropertyValue("color");
            try {
                t.setFill(color.isEmpty() ? Color.BLACK : Color.web(color));
            } catch (IllegalArgumentException ex) {
                System.out.println("Failed color: " + color);
            }
            Pair<Font, Boolean> p = Styler.getFont(styling);
            t.setFont(p.getKey());
            t.setUnderline(p.getValue());
            return t;
        } else if (node instanceof Element && Styler.getDisplayType(styling) != DisplayType.NONE) {
            ElementWrapper wrapper;
            javafx.scene.Node n;
            if (SpecialMapper.isSpecialMapped((Element) node)) {
                n = SpecialMapper.map((Element) node);
            } else if (InputElementMapper.isInputMapped(((Element) node).tagName())) {
                n = InputElementMapper.map((Element) node);
            } else if (node.childNodeSize() > 0) {
                n = DisplayMapper.map(node, styler, this::map);
            } else {
                n = new Group();
                n.setUserData(node);
            }
            wrapper = new ElementWrapper(n);
            wrapper.setStyling(styling);
            return wrapper;
        } else {
            Group g = new Group();
            g.setUserData(node);
            return g;
        }
    }

    private static boolean isStyleSheetLink(Element element) {
        return ((element.hasAttr("as") && element.attr("as").equals("style"))
                || (element.hasAttr("type") && element.attr("type").equals("text/css")
                && element.hasAttr("rel") && element.attr("rel").equals("stylesheet")))
                && element.hasAttr("href");
    }
}
