/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.inspect;

import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import org.fxmisc.easybind.EasyBind;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import pc.browser.render.ElementWrapper;

/**
 *
 * @author prem
 */
public class SceneGraph {

    @FXML
    private Node root;

    @FXML
    private TreeView<Node> treeView;

    @FXML
    private void initialize() {
        EasyBind.when(root.sceneProperty().isNotNull()).bind(treeView.rootProperty(),
                EasyBind.select(EasyBind.select(root.sceneProperty()).selectObject(Scene::rootProperty)
                        .map(p -> p.lookup("#content")).map(ScrollPane.class::cast))
                        .selectObject(ScrollPane::contentProperty).map(this::traverseTree));
        treeView.rootProperty().addListener((o, b, s) -> System.out.println(treeView.toString()));
        treeView.setCellFactory(tv -> {
            TreeCell<Node> cell = new TreeCell<>();
            cell.textProperty().bind(Bindings.createStringBinding(() -> cell.getItem() == null ? "" : toString(cell.getItem()), cell.itemProperty()));
//            BooleanProperty entered = new SimpleBooleanProperty();
//            cell.setOnMouseEntered(m -> {
//                if (cell.getItem() != null && !cell.isSelected()) {
//                    cell.getItem().setStyle("background-color:#0088ff22");
//                }
//                entered.set(true);
//            });
//            cell.setOnMouseExited(m -> {
//                if (cell.getItem() != null && !cell.isSelected()) {
//                    cell.getItem().setStyle("");
//                }
//                entered.set(false);
//            });
//            cell.selectedProperty().addListener((o, b, s) -> {
//                if (cell.getItem() != null) {
//                    if (s) {
//                        cell.getItem().setStyle("background-color:#0088ff44");
//                    } else {
//                        cell.getItem().setStyle(entered.get() ? "background-color:#0088ff22" : "");
//                    }
//                }
//            });
            return cell;
        });
    }

    private TreeItem<Node> traverseTree(Node n) {
        TreeItem<Node> ti = new TreeItem<>(n);
        ti.setExpanded(true);
        if (n instanceof Parent) {
            EasyBind.listBind(ti.getChildren(), EasyBind.map(strip(((Parent) n).getChildrenUnmodifiable()), this::traverseTree));
        }
        return ti;
    }

    private ObservableList<Node> strip(ObservableList<Node> toStrip) {
        return EasyBind.map(toStrip, this::getNonFiller);
    }

    private Node getNonFiller(Node n) {
        while (!(n instanceof ElementWrapper) && n instanceof StackPane) {
            if (((StackPane) n).getChildren().isEmpty()) {
                break;
            }
            n = ((StackPane) n).getChildren().get(0);
        }
        return n;
    }

    private String toString(Node n) {
        if (n.getUserData() instanceof TextNode) {
            return n.getClass().getSimpleName() + " Text: " + ((TextNode) n.getUserData()).text();
        } else if (n.getUserData() instanceof Element) {
            return n.getClass().getSimpleName() + " DOM Element: " + ((Element) n.getUserData()).tagName();
        } else {
            return n.getClass().getSimpleName();
        }
    }

    public TreeView<Node> getTreeView() {
        return treeView;
    }

    @FXML
    private void close() {
        root.setVisible(false);
    }
}
