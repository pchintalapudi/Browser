/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import java.util.List;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import pc.browser.render.styles.BorderStyle;

/**
 *
 * @author prem
 */
public class ElementWrapper {

    private final Insets margins, padding;
    private final BorderWidths borderWidths;

    public ElementWrapper(Insets margins, Insets borderWidths, Insets padding) {
        this.margins = margins;
        this.borderWidths = new BorderWidths(borderWidths.getTop(), borderWidths.getRight(), borderWidths.getBottom(), borderWidths.getLeft());
        this.padding = padding;
    }

    public StackPane wrap(Node n, List<Pair<BorderStyle, Color>> borderStyling, Color backgroundColor) {
        Border b;
        switch (borderStyling.size()) {
            case 0:
                b = null;
                break;
            case 1:
                b = new Border(new BorderStroke(borderStyling.get(0).getValue(), BorderStyle.mapToBorderStrokeStyle(borderStyling.get(0).getKey()), null, borderWidths));
                break;
            case 2:
                b = new Border(new BorderStroke(borderStyling.get(0).getValue(),
                        borderStyling.get(1).getValue(), borderStyling.get(0).getValue(),
                        borderStyling.get(1).getValue(),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(0).getKey()),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(1).getKey()),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(0).getKey()),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(1).getKey()),
                        null, borderWidths, null));
                break;
            case 4:
                b = new Border(new BorderStroke(borderStyling.get(0).getValue(),
                        borderStyling.get(1).getValue(), borderStyling.get(2).getValue(),
                        borderStyling.get(3).getValue(),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(0).getKey()),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(1).getKey()),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(2).getKey()),
                        BorderStyle.mapToBorderStrokeStyle(borderStyling.get(3).getKey()),
                        null, borderWidths, null));
                break;
            default:
                return null;
        }
        StackPane container = new StackPane(n);
        container.setBackground(new Background(new BackgroundFill(backgroundColor, null, null)));
        if (b != null) {
            container.setBorder(b);
        }
        container.setPadding(padding);
        StackPane marginContainer = new StackPane(container);
        marginContainer.setPadding(margins);
        return marginContainer;
    }
}
