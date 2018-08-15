/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.debug;

import java.util.Map;
import java.util.WeakHashMap;
import javafx.beans.binding.Bindings;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;

/**
 *
 * @author prem
 */
public class SceneGraphAnalyzer {

    public static void show(Node n) {
        TreeView<String> treeview = new TreeView<>(constructTree(n));
        treeview.setCellFactory(tv -> {
            TreeCell<String> tc = new TreeCell<>();
            tc.setOnMouseEntered(m -> tv.getSelectionModel().select(tc.getTreeItem()));
            tc.textProperty().bind(Bindings.createStringBinding(() -> tc.itemProperty().get() == null ? "" : tc.itemProperty().get(), tc.itemProperty()));
            tc.setOnMouseExited(m -> {
                if (tv.getSelectionModel().getSelectedItem() == tc.getTreeItem()) {
                    tv.getSelectionModel().clearSelection();
                }
            });
            return tc;
        });
        treeview.getSelectionModel().selectedItemProperty().addListener((o, b, s) -> {
            if (b != null) {
                highlightMap.get(b).getStyleClass().remove("highlight");
            }
            if (s != null) {
                highlightMap.get(s).getStyleClass().add("highlight");
            }
        });
        treeview.setPrefSize(1600, 900);
        Stage stage = new Stage();
        stage.setScene(new Scene(treeview, 1600, 900));
        stage.show();
    }

    private static final Map<TreeItem<String>, Node> highlightMap = new WeakHashMap<>();

    private static TreeItem<String> constructTree(Node n) {
        TreeItem<String> item = new TreeItem<>(toString(n));
        highlightMap.put(item, n);
        item.setExpanded(true);
        if (n instanceof Parent) {
            ((Parent) n).getChildrenUnmodifiable().stream().map(SceneGraphAnalyzer::constructTree).forEach(item.getChildren()::add);
        }
        return item;
    }

    private static String toString(Node n) {
        String nodeString;
        if (n instanceof Parent) {
            int childCount = ((Parent) n).getChildrenUnmodifiable().size();
            nodeString = n.getClass().getSimpleName() + " (" + childCount + (childCount != 1 ? " children)" : " child)");
        } else {
            nodeString = n.toString();
        }
        if (n.getUserData() != null) {
            nodeString += " DOM Element: " + ((org.jsoup.nodes.Node) n.getUserData()).nodeName();
        }
        return nodeString;
    }
}
