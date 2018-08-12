/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import java.util.Arrays;
import javafx.scene.Group;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import pc.browser.render.styles.BorderStyle;
import pc.browser.render.styles.Styling;

/**
 *
 * @author prem
 */
public abstract class ElementView extends Group {

    private static StackPane wrap(StackPane s, Styling styling) {
        BorderPane paddingBox = new BorderPane();
        paddingBox.setBackground(new Background(new BackgroundFill(styling.getBackgroundColor(), null, null)));
        paddingBox.setCenter(s);
        Rectangle[] padding = bind(s, styling.getPadding());
        Arrays.stream(padding).forEach(r -> r.setFill(null));
        paddingBox.setTop(padding[0]);
        paddingBox.setBottom(padding[1]);
        paddingBox.setLeft(padding[2]);
        paddingBox.setRight(padding[3]);
        BorderPane borderBox = new BorderPane();
        borderBox.setBackground(Background.EMPTY);
        border(borderBox, paddingBox, styling);
        BorderPane marginBox = new BorderPane();
        marginBox.setBackground(Background.EMPTY);
        marginBox.setCenter(borderBox);
        Rectangle[] margin = bind(borderBox, styling.getMargin());
        Arrays.stream(margin).forEach(r -> r.setFill(null));
        marginBox.setTop(margin[0]);
        marginBox.setBottom(margin[1]);
        marginBox.setLeft(margin[2]);
        marginBox.setRight(margin[3]);
        return new StackPane(marginBox);
    }

    private static Rectangle[] bind(Pane p, double[] widths) {
        Rectangle top = new Rectangle();
        top.widthProperty().bind(p.widthProperty());
        top.setHeight(widths[0]);
        Rectangle bottom = new Rectangle();
        bottom.widthProperty().bind(p.widthProperty());
        bottom.setHeight(widths[1]);
        Rectangle left = new Rectangle();
        left.heightProperty().bind(p.heightProperty());
        left.widthProperty().set(widths[2]);
        Rectangle right = new Rectangle();
        right.heightProperty().bind(p.heightProperty());
        right.widthProperty().set(widths[3]);
        return new Rectangle[]{top, bottom, left, right};
    }

    private static void border(BorderPane bordering, Pane center, Styling styling) {
        bordering.setCenter(center);
        if (styling.getBorderTypes()[0] != BorderStyle.NONE) {
            Pane top = new Pane();
            top.setBackground(new Background(new BackgroundFill(styling.getBorderColors()[0], null, null)));
            if (styling.getBorderTypes()[2] != BorderStyle.NONE) {
                Path tl = new Path();
                LineTo toTL = new LineTo(styling.getBorderWidths()[2], styling.getBorderWidths()[0]);
                LineTo toSide = new LineTo(-styling.getBorderWidths()[2], 0);
                tl.getElements().addAll(toTL, toSide, new ClosePath());
                tl.setFill(styling.getBorderColors()[2]);
                top.getChildren().add(tl);
            }
            if (styling.getBorderTypes()[3] != BorderStyle.NONE) {
                Path tr = new Path();
                MoveTo r = new MoveTo(0, 0);
                r.xProperty().bind(bordering.widthProperty());
                LineTo toTR = new LineTo(-styling.getBorderWidths()[3], styling.getBorderWidths()[0]);
                LineTo toSide = new LineTo(styling.getBorderWidths()[3], 0);
                tr.getElements().addAll(r, toTR, toSide, new ClosePath());
                tr.setFill(styling.getBorderColors()[2]);
                top.getChildren().add(tr);
            }
            bordering.setTop(top);
        }
        if (styling.getBorderTypes()[1] != BorderStyle.NONE) {
            Pane bottom = new Pane();
            bottom.setBackground(new Background(new BackgroundFill(styling.getBorderColors()[1], null, null)));
            if (styling.getBorderTypes()[2] != BorderStyle.NONE) {
                Path bl = new Path();
                MoveTo b = new MoveTo(0, styling.getBorderWidths()[1]);
                LineTo toBL = new LineTo(styling.getBorderWidths()[2], -styling.getBorderWidths()[1]);
                LineTo toSide = new LineTo(-styling.getBorderWidths()[2], 0);
                bl.getElements().addAll(b, toBL, toSide, new ClosePath());
                bl.setFill(styling.getBorderColors()[3]);
                bottom.getChildren().add(bl);
            }
            if (styling.getBorderTypes()[3] != BorderStyle.NONE) {
                Path br = new Path();
                MoveTo r = new MoveTo(0, styling.getBorderWidths()[1]);
                r.xProperty().bind(bordering.widthProperty());
                LineTo toBR = new LineTo(-styling.getBorderWidths()[3], -styling.getBorderWidths()[1]);
                LineTo toSide = new LineTo(styling.getBorderWidths()[3], 0);
                br.getElements().addAll(r, toBR, toSide, new ClosePath());
                br.setFill(styling.getBorderColors()[3]);
                bottom.getChildren().add(br);
            }
            bordering.setBottom(bottom);
        }
        if (styling.getBorderTypes()[2] != BorderStyle.NONE) {
            Pane left = new Pane();
            left.setBackground(new Background(new BackgroundFill(styling.getBorderColors()[2], null, null)));
            bordering.setLeft(left);
        }
        if (styling.getBorderTypes()[3] != BorderStyle.NONE) {
            Pane right = new Pane();
            right.setBackground(new Background(new BackgroundFill(styling.getBorderColors()[2], null, null)));
            bordering.setRight(right);
        }
    }
}
