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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import pc.browser.resources.Resources;

/**
 * FXML Controller class
 *
 * @author prem
 */
public class TabController extends AnchorPane {

    @FXML
    private HBox tabBody;
    @FXML
    private HBox tabContent;
    @FXML
    private ImageView icon;
    @FXML
    private Label tabTitle;

    public TabController() {
        FXMLLoader loader = Resources.getFXMLLoader("Tab.fxml");
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
        if (onClick != null) {
            this.onClick.run();
        }
    }

    private Runnable onClick;

    public void onClick(Runnable onClick) {
        if (onClick != null) {
            this.onClick = onClick;
        }
    }

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    public void highlightSelected(boolean selected) {
        tabBody.pseudoClassStateChanged(SELECTED, selected);
    }

    public void setTitle(String title) {
        this.tabTitle.setText(title);
    }

    public String getTitle() {
        return tabTitle.getText();
    }

    private URL current;
    private String enteredText;
    private ObjectProperty<Parent> sceneGraphProperty = new SimpleObjectProperty<>(Resources.directLoad("NullPage.fxml"));
    private final BooleanProperty loadingProperty = new SimpleBooleanProperty();
    private final TabLog log = new TabLog();

    /**
     * @return the current
     */
    public URL getCurrent() {
        return current;
    }

    /**
     * @param current the current to set
     */
    public void setCurrent(URL current) {
        this.current = current;
    }

    /**
     * @return the enteredText
     */
    public String getEnteredText() {
        return enteredText;
    }

    /**
     * @param enteredText the enteredText to set
     */
    public void setEnteredText(String enteredText) {
        this.enteredText = enteredText;
    }

    public ObjectProperty<Parent> sceneGraphProperty() {
        return sceneGraphProperty;
    }

    /**
     * @return the loadingProperty
     */
    public BooleanProperty getLoadingProperty() {
        return loadingProperty;
    }

    /**
     * @return the log
     */
    public TabLog getLog() {
        return log;
    }
}
