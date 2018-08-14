/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.debug;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
        treeview.setPrefSize(1600, 900);
        Stage stage = new Stage();
        stage.setScene(new Scene(treeview, 1600, 900));
        stage.show();
    }

    private static TreeItem<String> constructTree(Node n) {
        TreeItem<String> item = new TreeItem<>(toString(n));
        item.setExpanded(true);
        if (n instanceof Parent) {
            ((Parent) n).getChildrenUnmodifiable().stream().map(SceneGraphAnalyzer::constructTree).forEach(item.getChildren()::add);
        }
        return item;
    }

    private static String toString(Node n) {
        if (n instanceof Parent) {
            int childCount = ((Parent) n).getChildrenUnmodifiable().size();
            return n.getClass() + " (" + childCount + (childCount != 1 ? " children)" : " child)");
        }
        return n.toString();
    }
}
