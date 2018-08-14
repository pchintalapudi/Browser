/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.IOException;
import java.util.Deque;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.jsoup.nodes.Node;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.css.CSSStyleSheet;

/**
 *
 * @author prem
 */
public class Mapper {

    private final ObjectProperty<CSSStyleSheet> stylingProperty = new SimpleObjectProperty<>();

    public Mapper(InputSource stylesheetSource) {
        try {
            stylingProperty.set(new CSSOMParser(new SACParserCSS3()).parseStyleSheet(stylesheetSource, null, null));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void addStyleSheet(InputSource stylesheetSource) {
        CSSOMParser parser = new CSSOMParser(new SACParserCSS3());
        parser.setParentStyleSheet((CSSStyleSheetImpl) stylingProperty.get());
        try {
            stylingProperty.set(parser.parseStyleSheet(stylesheetSource, null, null));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void map(Node domNode) {
    }

    private void applyStyles(Deque<Node> cssSelectorPath) {

    }
}
