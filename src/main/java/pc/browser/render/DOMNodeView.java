/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import pc.browser.render.styles.DisplayType;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import pc.browser.render.styles.BorderStyle;
import pc.browser.render.styles.Styling;
import pc.browser.render.styles.StylingBuilder;

/**
 *
 * @author prem
 */
public final class DOMNodeView {

    public static Parent map(Node n) {
        return mapInternal(n, Styling.EMPTY);
    }

    private static Parent mapInternal(Node n, Styling carried) {
        if (n instanceof TextNode) {
            Text t = new Text(((TextNode) n).text());
            t.setFill(carried.getTextColor());
            t.setFont(Font.font(carried.getFontFamily(), carried.getFontWeight(), carried.getFontSize()));
            return new Group(t);
        }
        Styling nodeStyling = style((Element) n, carried);
        if (nodeStyling.getDisplayType() == DisplayType.NONE) {
            return new Group();
        }
        if (n.childNodeSize() > 0) {
            VBox content = new VBox();
            content.getChildren().addAll(flow(n.childNodes()).stream().map(l
                    -> new FlowPane(l.stream().map(ln -> mapInternal(ln, nodeStyling))
                            .toArray(Parent[]::new))).collect(Collectors.toList()));
            return wrap(new StackPane(content), nodeStyling);
        }
        return new Group();
    }

    private static Deque<List<Node>> flow(List<Node> nodes) {
        Deque<List<Node>> flowed = new ArrayDeque<>();
        List<Node> initial = new ArrayList<>();
        initial.add(nodes.get(0));
        flowed.add(initial);
        nodes.subList(1, nodes.size()).forEach(n -> {
            if (n instanceof TextNode) {
                List<Node> newList = new ArrayList<>();
                newList.add(n);
                flowed.add(newList);
            } else if (n instanceof Element) {
                Styling styling = style((Element) n, Styling.EMPTY);
                if (DisplayType.isInline(styling.getDisplayType())) {
                    flowed.peekLast().add(n);
                } else {
                    List<Node> newList = new ArrayList<>();
                    newList.add(n);
                    flowed.add(newList);
                }
            }
        });
        return flowed;
    }

    private static StackPane wrap(StackPane s, Styling styling) {
        BorderPane paddingBox = new BorderPane();
        paddingBox.setBackground(new Background(new BackgroundFill(styling.getBackgroundColor(), null, null)));
//        paddingBox.setBackground(new Background(new BackgroundFill(Color.BLUE, null, null)));
        paddingBox.setCenter(s);
        Rectangle[] padding = bind(s, styling.getPadding());
        Arrays.stream(padding).forEach(r -> r.setFill(null));
        paddingBox.setTop(padding[0]);
        paddingBox.setBottom(padding[1]);
        paddingBox.setLeft(padding[2]);
        paddingBox.setRight(padding[3]);
        BorderPane borderBox = new BorderPane();
        borderBox.setBackground(Background.EMPTY);
//        borderBox.setBackground(new Background(new BackgroundFill(Color.LIME, null, null)));
        border(borderBox, paddingBox, styling);
        BorderPane marginBox = new BorderPane();
        marginBox.setBackground(Background.EMPTY);
//        marginBox.setBackground(new Background(new BackgroundFill(Color.RED, null, null)));
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

    private static Styling style(Element e, Styling carried) {
        switch (e.tagName()) {
            case "head":
            case "link":
            case "meta":
            case "script":
            case "style":
            case "title":
                return StylingBuilder.create().setDisplayType(DisplayType.NONE).createStyling();
            case "html":
            case "body":
            case "address":
            case "article":
            case "aside":
            case "div":
            case "footer":
            case "header":
            case "hgroup":
            case "layer":
            case "main":
            case "nav":
            case "section":
            case "figcaption":
            case "blockquote":
            case "figure":
            case "center":
            case "hr":
            case "ul":
            case "menu":
            case "dir":
            case "ol":
            case "dd":
            case "dl":
            case "form":
            case "legend":
            case "p":
                return StylingBuilder.create(carried).setDisplayType(DisplayType.INLINE_BLOCK).createStyling();
            case "a":
                return StylingBuilder.create(carried).setDisplayType(DisplayType.BLOCK).setTextColor(Color.BLUE).createStyling();
            case "h1":
                return StylingBuilder.create(carried).setFontWeight(FontWeight.BLACK).setFontSize(32).createStyling();
            case "h2":
                return StylingBuilder.create(carried).setFontWeight(FontWeight.BOLD).setFontSize(24).createStyling();
            case "h3":
                return StylingBuilder.create(carried).setFontWeight(FontWeight.BOLD).setFontSize(18.67).createStyling();
            case "h4":
                return StylingBuilder.create(carried).setFontWeight(FontWeight.BOLD).createStyling();
            case "h5":
                return StylingBuilder.create(carried).setFontWeight(FontWeight.BOLD).createStyling();
            case "h6":
                return StylingBuilder.create(carried).setFontWeight(FontWeight.BOLD).createStyling();
            case "b":
                return StylingBuilder.create(carried).setFontWeight(FontWeight.BOLD).createStyling();
            default:
                return StylingBuilder.create(carried).setDisplayType(DisplayType.INLINE_BLOCK).createStyling();
        }
    }
}
