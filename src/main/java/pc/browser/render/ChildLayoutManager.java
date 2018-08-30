/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javafx.geometry.Orientation;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 *
 * @author prem
 */
public class ChildLayoutManager extends StackPane {

    public ChildLayoutManager() {
    }

    public void manage(org.jsoup.nodes.Element element) {
        try {
            List<DisplayType> displayTypes = element.childNodes().stream().map(styler::style)
                    .map(Styler::getDisplayType).collect(Collectors.toList());
            DisplayType parent = Styler.getDisplayType(styler.style(element));
            switch (parent) {
                default:
                    layoutStandard(displayTypes, element.childNodes());
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private Styler styler;
    private Function<org.jsoup.nodes.Node, javafx.scene.Node> mapper;

    private void layoutStandard(List<DisplayType> displayTypes, List<org.jsoup.nodes.Node> nodes) {
        if (displayTypes.size() > 0) {
            VBox grandfather = new VBox();
            FlowPane father = getFlowPane(Orientation.HORIZONTAL);
            father.getChildren().add(mapper.apply(nodes.get(0)));
            for (int i = 1; i < displayTypes.size(); i++) {
                DisplayType dt = displayTypes.get(i);
                if (dt != DisplayType.NONE) {
                    if (DisplayType.isInline(dt)) {
                        father.getChildren().add(mapper.apply(nodes.get(i)));
                    } else {
                        grandfather.getChildren().add(father);
                        father = getFlowPane(Orientation.HORIZONTAL);
                        father.getChildren().add(mapper.apply(nodes.get(i)));
                    }
                }
            }
            grandfather.getChildren().add(father);
            getChildren().setAll(grandfather);
        }
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
