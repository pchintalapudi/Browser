/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.inspect;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.fxmisc.easybind.EasyBind;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;

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
        root.visibleProperty().addListener((o, b, s) -> {
            if (s) {
                refreshTree();
            }
        });
        treeView.setCellFactory(tv -> {
            TreeCell<Node> cell = new TreeCell<>();
            cell.textProperty().bind(Bindings.createStringBinding(() -> cell.getItem() == null ? "" : toString(cell.getItem()), cell.itemProperty()));
            cell.setOnMouseEntered(m -> {
                tv.getSelectionModel().select(cell.getIndex());
            });
            cell.setOnMouseExited(m -> {
                tv.getSelectionModel().clearSelection(cell.getIndex());
            });
            cell.selectedProperty().addListener((o, b, s) -> {
                if (s) {
                    cell.getItem().getStyleClass().add("highlight");
                } else {
                    cell.getItem().getStyleClass().remove("highlight");
                }
            });
            return cell;
        });
    }

    private void refreshTree() {
        if (root.sceneProperty().get() != null) {
            treeView.setRoot(traverseTree(root.sceneProperty().get().getRoot().lookup("#content")));
        }
    }

    private TreeItem<Node> traverseTree(Node n) {
        TreeItem<Node> ti = new TreeItem<>(n);
        ti.setExpanded(true);
        if (n instanceof Parent) {
            EasyBind.listBind(ti.getChildren(), EasyBind.map(((Parent) n).getChildrenUnmodifiable(), this::traverseTree));
        }
        return ti;
    }

    private String toString(Node n) {
        if (n.getUserData() instanceof TextNode) {
            return n.getClass().getSimpleName() + " Text: " + ((TextNode) n.getUserData()).text();
        } else if (n.getUserData() instanceof Element) {
            return n.getClass().getSimpleName() + " DOM Element: " + ((Element) n.getUserData()).tagName() + " DisplayType: " + n.getProperties().get("");
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
