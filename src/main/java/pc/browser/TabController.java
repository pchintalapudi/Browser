/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;

/**
 * FXML Controller class
 *
 * @author prem
 */
public class TabController extends AnchorPane {

    @FXML
    private HBox tabContent;
    @FXML
    private ImageView icon;
    @FXML
    private Label tabTitle;

    public TabController() {
        FXMLLoader loader = new FXMLLoader(TabController.class.getResource("/fxml/Tab.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException ex) {
            Logger.getLogger(TabController.class.getName()).log(Level.SEVERE, null, ex);
        }
        icon.imageProperty().addListener((o, b, s) -> {
            if (s == null) {
                tabContent.getChildren().remove(icon);
            } else {
                tabContent.getChildren().add(icon);
            }
        });
        tabContent.getChildren().remove(icon);
    }

    @FXML
    private void close() {
        if (onClose != null) {
            onClose.run();
        }
    }

    private Runnable onClose;

    public void onClose(Runnable onClose) {
        this.onClose = onClose;
    }

    @FXML
    private void select() {
        if (onSelect != null) {
            this.onSelect.run();
        }
    }

    private Runnable onSelect;

    public void onSelect(Runnable onSelect) {
        if (onSelect != null) {
            this.onSelect = onSelect;
        }
    }

    public void setTitle(String title) {
        this.tabTitle.setText(title);
    }
    
    private URL current;
    private String enteredText;
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty();
}
