/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import pc.browser.Async;
import pc.browser.async.RenderTask;

/**
 *
 * @author prem
 */
public class SpecialMapper {

    public static javafx.scene.Node map(Element element) {
        javafx.scene.Node node;
        switch (element.tagName()) {
            case "img":
                try {
                    Image i = new Image(element.absUrl("src"), true);
                    node = new ImageView(i);
                } catch (IllegalArgumentException ex) {
                    node = new Group();
                }
                break;
            case "iframe":
                StackPane s = new StackPane();
                s.setAlignment(Pos.TOP_LEFT);
                Async.asyncRender(() -> {
                    try {
                        Node n = new Mapper(1).map(Jsoup.connect(element.absUrl("src")).get());
                        Platform.runLater(() -> s.getChildren().add(n));
                    } catch (IOException ex) {
                        Logger.getLogger(SpecialMapper.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IllegalArgumentException ex) {
                    }
                }, RenderTask.UNPRIVILEGED, 0);
                node = s;
                break;
            case "progress":
                ProgressBar progress = new ProgressBar();
                if (element.hasAttr("value") && element.hasAttr("max")) {
                    try {
                        progress.setProgress(Double.parseDouble(element.attr("value")) / Double.parseDouble(element.attr("max")));
                    } catch (NumberFormatException ex) {
                    }
                }
                node = progress;
                break;
            default:
                node = new Group();
                break;
        }
        node.setUserData(element);
        return node;
    }

    public static boolean isSpecialMapped(Element element) {
        switch (element.tagName()) {
            case "img":
            case "iframe":
                if (!element.hasAttr("src")) {
                    return false;
                }
            case "progress":
                return true;
            default:
                return false;
        }
    }
}
