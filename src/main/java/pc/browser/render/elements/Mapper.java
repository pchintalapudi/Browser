/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import com.steadystate.css.dom.CSSStyleSheetImpl;
import org.jsoup.nodes.Document;
import org.w3c.dom.css.CSSStyleSheet;

/**
 *
 * @author prem
 */
public class Mapper {

    private final CSSStyleSheet stylesheet;

    public Mapper(CSSStyleSheet stylesheet) {
        this.stylesheet = stylesheet;
    }

    public Mapper appendStyleSheet(CSSStyleSheetImpl stylesheet) {
        stylesheet.setParentStyleSheet(this.stylesheet);
        return new Mapper(stylesheet);
    }
}
