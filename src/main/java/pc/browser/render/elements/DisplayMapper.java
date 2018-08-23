/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Pair;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.w3c.dom.css.CSSStyleDeclaration;

/**
 *
 * @author prem
 */
public class DisplayMapper {

    public static javafx.scene.Node map(Node n, CSSStyleDeclaration styling,
            Function<Node, Pair<javafx.scene.Node, CSSStyleDeclaration>> childMapper) {
        switch (InternalDisplayType.toInternal(getDisplayType(styling))) {
            case FLEX_HOR:
                HBox hContainer = new HBox();
                n.childNodes().stream().map(childMapper).filter(p -> getDisplayType(p.getValue()) != DisplayType.NONE)
                        .map(Pair::getKey).forEach(hContainer.getChildren()::add);
                hContainer.setUserData(n);
                return hContainer;
            case STANDARD:
            default:
                VBox vContainer = new VBox();
                List<Pair<javafx.scene.Node, CSSStyleDeclaration>> mappedList
                        = n.childNodes().stream().map(childMapper).collect(Collectors.toList());
                if (mappedList.size() > 0) {
                    HBox prev = new HBox(mappedList.get(0).getKey());
                    for (int i = 1; i < mappedList.size(); i++) {
                        if (n.childNode(i) instanceof TextNode || DisplayType.isInline(getDisplayType(mappedList.get(i).getValue()))) {
                            prev.getChildren().add(mappedList.get(i).getKey());
                        } else if (getDisplayType(mappedList.get(i).getValue()) != DisplayType.NONE) {
                            if (prev.getChildren().size() > 1) {
                                vContainer.getChildren().add(prev);
                            } else {
                                vContainer.getChildren().add(prev.getChildren().remove(0));
                            }
                            prev = new HBox(mappedList.get(i).getKey());
                        }
                    }
                    if (prev.getChildren().size() > 1) {
                        vContainer.getChildren().add(prev);
                    } else {
                        vContainer.getChildren().add(prev.getChildren().remove(0));
                    }
                }
                if (vContainer.getChildren().size() > 1) {
                    vContainer.setUserData(n);
                    return vContainer;
                } else {
                    javafx.scene.Node node = vContainer.getChildren().remove(0);
                    node.setUserData(n);
                    return node;
                }
            case FLEX_VER:
                vContainer = new VBox();
                n.childNodes().stream().map(childMapper).filter(p -> getDisplayType(p.getValue()) != DisplayType.NONE)
                        .map(Pair::getKey).forEach(vContainer.getChildren()::add);
                vContainer.setUserData(n);
                return vContainer;
            case TABLE:
                GridPane table = new GridPane();
                for (int i = 0; i < n.childNodeSize(); i++) {
                    Pair<javafx.scene.Node, CSSStyleDeclaration> mapped = childMapper.apply(n.childNode(i));
                    if (getDisplayType(mapped.getValue()) != DisplayType.NONE) {
                        if (mapped.getKey() instanceof Parent) {
                            List<javafx.scene.Node> grandchildren = fetchRealChildren(mapped.getKey());
                            for (int j = 0; j < grandchildren.size(); j++) {
                                table.add(grandchildren.get(j), j, i);
                            }
                        } else {
                            table.add(mapped.getKey(), 0, i);
                        }
                    }
                }
                table.setUserData(n);
                return table;
        }
    }
    
//    private static javafx.scene.Node mapTable(Node n, CSSStyleDeclaration styling) {
//        GridPane layout = new GridPane();
//        for (int i = 0; i < n.childNodeSize(); i++) {
//            
//        }
//    }

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

    private static DisplayType getDisplayType(CSSStyleDeclaration styling) {
        try {
            DisplayType dt = DisplayType.read(styling.getPropertyValue("display").isEmpty() ? "inline-block"
                    : styling.getPropertyValue("display"));
            return dt;
        } catch (IllegalArgumentException ex) {
            return DisplayType.BLOCK;
        }
    }

    private static List<javafx.scene.Node> fetchRealChildren(javafx.scene.Node n) {
        while (n instanceof Parent && ((Parent) n).getChildrenUnmodifiable().size() == 1
                && ((Parent) n).getChildrenUnmodifiable().get(0) instanceof StackPane) {
            n = ((Parent) n).getChildrenUnmodifiable().get(0);
        }
        return n instanceof Parent ? ((Parent) n).getChildrenUnmodifiable() : Arrays.asList(n);
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
