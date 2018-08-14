/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.w3c.dom.css.CSSRule;
import org.w3c.dom.css.CSSStyleRule;
import org.w3c.dom.css.CSSStyleSheet;

/**
 *
 * @author prem
 */
public class Mapper {

    private final CSSStyleSheet stylesheet;
    private ElementWrapper wrapper = new ElementWrapper(new Insets(0), new Insets(0), new Insets(0));

    public Mapper(CSSStyleSheet stylesheet) {
        this.stylesheet = stylesheet;
    }

    public Mapper appendStyleSheet(CSSStyleSheetImpl stylesheet) {
        stylesheet.setParentStyleSheet(this.stylesheet);
        return new Mapper(stylesheet);
    }

    private void parseStylesheet() {
        List<CSSStyleRule> rules = new ArrayList<>();
        for (int i = 0; i < stylesheet.getCssRules().getLength(); i++) {
            CSSRule rule = stylesheet.getCssRules().item(i);
            if (rule instanceof CSSStyleRule) {
                rules.add((CSSStyleRule) rule);
            }
        }
        rules.sort(Mapper::prioritizer);
    }

    private static int prioritizer(CSSStyleRule r1, CSSStyleRule r2) {
        return 0;
    }
    
    public javafx.scene.Node map(Document document) {
        return map(document.body());
    }

    private javafx.scene.Node map(Node node) {
        if (node instanceof TextNode) {
            return new Text(((TextNode) node).text());
        } else {
            VBox content = new VBox();
            if (node instanceof Element && ((Element) node).tagName().equals("a")) {
                content.setStyle("-fx-text-color: #0000ff;\n" + "-fx-cursor: hand;");
                System.out.println(content.getStyle());
            }
            node.childNodes().stream().map(this::map).forEach(content.getChildren()::add);
            return wrapper.wrap(content, Collections.emptyList(), Color.TRANSPARENT);
        }
    }
}
