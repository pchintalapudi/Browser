/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableFloatArray;
import javafx.collections.ObservableList;
import javafx.scene.paint.Paint;

/**
 *
 * @author prem
 */
public class Styling {

    private ObservableFloatArray paddingProperty, borderWidthProperty, marginProperty;
    private ObservableList<Paint> borderColorProperty;
    private ObservableList<BorderStyle> borderStyles;
    private ObjectProperty<Paint> backgroundColorProperty;
    private ObjectProperty<DisplayType> displayTypeProperty;
}
