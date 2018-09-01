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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import pc.browser.render.css.StyleUtils;

/**
 *
 * @author prem
 */
public class ChildLayoutManager extends StackPane {

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
            default:
                layoutStandard(displayTypes, element.childNodes());
                break;
        }
    }

    private Styler styler;
    private Function<org.jsoup.nodes.Node, javafx.scene.Node> mapper;

    private void layoutStandard(List<DisplayType> displayTypes, List<org.jsoup.nodes.Node> nodes) {
        if (displayTypes.size() > 0) {
            VBox grandfather = new VBox();
            HBox father = new HBox();
            VBox.setVgrow(father, Priority.ALWAYS);
            father.getChildren().add(mapper.apply(nodes.get(0)));
            for (int i = 1; i < displayTypes.size(); i++) {
                DisplayType dt = displayTypes.get(i);
                if (dt != DisplayType.NONE) {
                    if (DisplayType.isInline(dt) || nodes.get(i) instanceof org.jsoup.nodes.TextNode) {
                        father.getChildren().add(mapper.apply(nodes.get(i)));
                    } else {
                        grandfather.getChildren().add(father);
                        father = new HBox();
                        VBox.setVgrow(father, Priority.ALWAYS);
                        father.getChildren().add(mapper.apply(nodes.get(i)));
                    }
                }
            }
            grandfather.getChildren().add(father);
            Platform.runLater(() -> getChildren().setAll(grandfather));
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
