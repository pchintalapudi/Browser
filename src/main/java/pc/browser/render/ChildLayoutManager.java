/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import pc.browser.render.css.DisplayType;
import pc.browser.render.css.Styler;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import pc.browser.render.css.StyleUtils;

/**
 *
 * @author prem
 */
public class ChildLayoutManager extends TextFlow {

    public ChildLayoutManager() {
    }

    public void manage(org.jsoup.nodes.Element element) {
        List<DisplayType> displayTypes = element.childNodes().stream().map(styler::style)
                .map(StyleUtils::getDisplayType).collect(Collectors.toList());
        DisplayType parent = StyleUtils.getDisplayType(styler.style(element));
        switch (parent) {
            case FLEX:
            case INLINE_FLEX:
                layoutFlex(displayTypes, element.childNodes());
                break;
            case TABLE:
            case INLINE_TABLE:
                layoutTable(displayTypes, element.childNodes());
                break;
            default:
                layoutStandard(displayTypes, element.childNodes());
                break;
        }
    }

    private Styler styler;
    private Function<org.jsoup.nodes.Node, javafx.scene.Node> mapper;

    private void layoutStandard(List<DisplayType> displayTypes, List<org.jsoup.nodes.Node> nodes) {
        if (displayTypes.size() > 0) {
            TextFlow father = new TextFlow();
            if (displayTypes.get(0) != DisplayType.NONE) {
                father.getChildren().add(mapper.apply(nodes.get(0)));
            }
            for (int i = 1; i < displayTypes.size(); i++) {
                DisplayType dt = displayTypes.get(i);
                if (dt != DisplayType.NONE) {
                    if (!DisplayType.isInline(dt) && !(nodes.get(i) instanceof org.jsoup.nodes.TextNode)) {
                        Text t = new Text("\n");
                        father.getChildren().add(t);
                    }
                    father.getChildren().add(mapper.apply(nodes.get(i)));
                }
            }
            Platform.runLater(() -> getChildren().setAll(father));
        }
    }

    private void layoutFlex(List<DisplayType> displayTypes, List<org.jsoup.nodes.Node> nodes) {
        HBox grandfather = new HBox();
        for (int i = 0; i < displayTypes.size(); i++) {
            DisplayType dt = displayTypes.get(i);
            if (dt != DisplayType.NONE) {
                grandfather.getChildren().add(mapper.apply(nodes.get(i)));
            }
        }
        VBox.setVgrow(grandfather, Priority.ALWAYS);
        Platform.runLater(() -> getChildren().setAll(grandfather));
    }

    private void layoutTable(List<DisplayType> displayTypes, List<org.jsoup.nodes.Node> nodes) {
        org.jsoup.nodes.Element head = null, body = null, foot = null;
        for (int i = displayTypes.size() - 1; i > -1; i--) {
            switch (displayTypes.get(i)) {
                case TABLE_HEADER_GROUP:
                    if (nodes.get(i) instanceof org.jsoup.nodes.Element) {
                        head = (org.jsoup.nodes.Element) nodes.get(i);
                    }
                    break;
                case TABLE_ROW_GROUP:
                    if (nodes.get(i) instanceof org.jsoup.nodes.Element) {
                        body = (org.jsoup.nodes.Element) nodes.get(i);
                    }
                    break;
                case TABLE_FOOTER_GROUP:
                    if (nodes.get(i) instanceof org.jsoup.nodes.Element) {
                        body = (org.jsoup.nodes.Element) nodes.get(i);
                    }
                    break;
            }
        }
        GridPane layout = new GridPane();
        if (head != null) {
            for (int i = 0; i < head.children().size(); i++) {
                org.jsoup.nodes.Element e = head.child(i);
                if (StyleUtils.getDisplayType(styler.style(e)) == DisplayType.TABLE_ROW) {
                    for (int j = 0; j < e.childNodeSize(); j++) {
                        layout.add(mapper.apply(e.childNode(j)), i, j);
                    }
                }
            }
        }
        if (body != null) {
            for (int i = 0; i < body.children().size(); i++) {
                org.jsoup.nodes.Element e = body.child(i);
                if (StyleUtils.getDisplayType(styler.style(e)) == DisplayType.TABLE_ROW) {
                    for (int j = 0; j < e.childNodeSize(); j++) {
                        layout.add(mapper.apply(e.childNode(j)), i, j);
                    }
                }
            }
        }
        if (foot != null) {
            for (int i = 0; i < foot.children().size(); i++) {
                org.jsoup.nodes.Element e = foot.child(i);
                if (StyleUtils.getDisplayType(styler.style(e)) == DisplayType.TABLE_ROW) {
                    for (int j = 0; j < e.childNodeSize(); j++) {
                        layout.add(mapper.apply(e.childNode(j)), i, j);
                    }
                }
            }
        }
        Platform.runLater(() -> getChildren().setAll(layout));
    }

    private FlowPane getFlowPane(Orientation orientation) {
        FlowPane fp = new FlowPane(orientation);
        switch (orientation) {
            case HORIZONTAL:
                fp.prefWrapLengthProperty().bind(fp.widthProperty());
                break;
            case VERTICAL:
                fp.prefWrapLengthProperty().bind(fp.heightProperty());
                break;
        }
        return fp;
    }

    public void setStyler(Styler styler) {
        this.styler = styler;
    }

    public void setMapper(Function<org.jsoup.nodes.Node, javafx.scene.Node> mapper) {
        this.mapper = mapper;
    }
}
