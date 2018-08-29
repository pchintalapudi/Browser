/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 *
 * @author prem
 */
public class ButtonElement extends Button {

    public ButtonElement() {
    }

    public ButtonElement(String text) {
        super(text);
    }

    public ButtonElement(String text, Node graphic) {
        super(text, graphic);
    }

}
