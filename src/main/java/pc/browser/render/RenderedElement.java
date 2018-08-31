/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import java.util.function.Function;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.render.css.Styler;

/**
 *
 * @author prem
 */
public interface RenderedElement {

    Runnable applyLayoutCSS(CSSStyleDeclaration styling);

    Runnable applyPaintCSS(CSSStyleDeclaration styling);

    void setElement(org.jsoup.nodes.Node element, Function<org.jsoup.nodes.Node, javafx.scene.Node> mapper, Styler styler);
}
