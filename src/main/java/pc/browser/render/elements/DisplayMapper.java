/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.w3c.dom.css.CSSStyleDeclaration;
import pc.browser.render.styles.Styler;

/**
 *
 * @author prem
 */
public class DisplayMapper {

    public static javafx.scene.Node map(Node n, Styler styler,
            Function<Node, javafx.scene.Node> childMapper) {
        CSSStyleDeclaration styling = styler.style(n);
        List<CSSStyleDeclaration> childStyles = n.childNodes().stream().map(styler::style).collect(Collectors.toList());
        switch (InternalDisplayType.toInternal(Styler.getDisplayType(styling))) {
            case FLEX_HOR:
                HBox hContainer = new HBox();
                for (int i = 0; i < childStyles.size(); i++) {
                    if (Styler.getDisplayType(childStyles.get(i)) != DisplayType.NONE) {
                        hContainer.getChildren().add(childMapper.apply(n.childNode(i)));
                    }
                }
                hContainer.setUserData(n);
                return hContainer;
            case FLEX_VER:
                VBox vContainer = new VBox();
                for (int i = 0; i < childStyles.size(); i++) {
                    if (Styler.getDisplayType(childStyles.get(i)) != DisplayType.NONE) {
                        vContainer.getChildren().add(childMapper.apply(n.childNode(i)));
                    }
                }
                vContainer.setUserData(n);
                return vContainer;
            case TABLE:
                return mapTable(n, styling, childStyles, childMapper);
            default:
            case STANDARD:
                VBox standard = new VBox();
                if (childStyles.size() > 0) {
                    HBox prev = new HBox(childMapper.apply(n.childNode(0)));
                    for (int i = 1; i < childStyles.size(); i++) {
                        if (n.childNode(i) instanceof TextNode || DisplayType.isInline(Styler.getDisplayType(childStyles.get(i)))) {
                            prev.getChildren().add(childMapper.apply(n.childNode(i)));
                        } else if (Styler.getDisplayType(childStyles.get(i)) != DisplayType.NONE) {
                            if (prev.getChildren().size() > 1) {
                                standard.getChildren().add(prev);
                            } else {
                                standard.getChildren().add(prev.getChildren().remove(0));
                            }
                            prev = new HBox(childMapper.apply(n.childNode(i)));
                        }
                    }
                    if (prev.getChildren().size() > 1) {
                        standard.getChildren().add(prev);
                    } else {
                        standard.getChildren().add(prev.getChildren().remove(0));
                    }
                }
                if (standard.getChildren().size() > 1) {
                    standard.setUserData(n);
                    return standard;
                } else {
                    javafx.scene.Node node = standard.getChildren().remove(0);
                    node.setUserData(n);
                    return node;
                }
            
        }
    }

    private static class TableData extends HBox {
    }

    private static javafx.scene.Node mapTable(Node n, CSSStyleDeclaration styling,
            List<CSSStyleDeclaration> childStyles, Function<Node, javafx.scene.Node> childMapper) {
        VBox global = new VBox();
        GridPane layout = new GridPane();
        global.getChildren().add(layout);
        for (int i = 0; i < childStyles.size(); i++) {
            switch (Styler.getDisplayType(childStyles.get(i))) {
                case TABLE_COLUMN_GROUP:
                case TABLE_COLUMN:
                case NONE:
                    continue;
                case TABLE_CAPTION:
                    global.getChildren().add(childMapper.apply(n));
                    break;
                    
            }
        }
        return global;
    }

    private static StackPane getVAlignWrapper(CSSStyleDeclaration styling) {
        StackPane s = new StackPane();
        VerticalAlign va = VerticalAlign.getVA(styling.getPropertyValue("vertical-align"));
        switch (va) {
            case BOTTOM:
                s.setAlignment(Pos.BOTTOM_LEFT);
                break;
            case MIDDLE:
                s.setAlignment(Pos.CENTER_LEFT);
                break;
            case TOP:
                s.setAlignment(Pos.TOP_LEFT);
                break;
            case SUP:
                s.setPadding(new Insets(5, 0, 0, 0));
                s.setAlignment(Pos.TOP_LEFT);
                break;
            case SUB:
                s.setPadding(new Insets(0, 0, 5, 0));
                s.setAlignment(Pos.CENTER);
        }
        return s;
    }

    private static enum InternalDisplayType {
        TABLE, FLEX_HOR, FLEX_HOR_WRAP, FLEX_VER, FLEX_VER_WRAP, STANDARD;

        public static InternalDisplayType toInternal(DisplayType dt) {
            switch (dt) {
                case TABLE:
                case INLINE_TABLE:
                    return TABLE;
                case FLEX:
                case INLINE_FLEX:
                    return FLEX_HOR;
                default:
                    return STANDARD;
            }
        }
    }

    private static enum VerticalAlign {
        TOP, MIDDLE, BOTTOM, SUP, SUB, UNSUPPORTED;

        public static VerticalAlign getVA(String value) {
            switch (value.toLowerCase()) {
                case "top":
                    return TOP;
                case "middle":
                    return MIDDLE;
                case "bottom":
                    return BOTTOM;
                case "sub":
                    return SUB;
                case "sup":
                    return SUP;
                default:
                    return UNSUPPORTED;
            }
        }
    }

//    private static List<Text> splitText(Text t) {
//        t.getText().split("\\s");
//    }
}
