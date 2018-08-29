/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.tabs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import pc.browser.Async;
import pc.browser.resources.Resources;
import pc.browser.tabs.async.RenderPool;

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
    @FXML
    private ProgressIndicator progressIndicator;

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
                tabContent.getChildren().add(0, icon);
            }
        });
        tabContent.getChildren().remove(icon);
        tabContent.getChildren().remove(progressIndicator);
        tabStateProperty.addListener((o, b, s) -> {
            switch (s) {
                case IDLE:
                    tabContent.getChildren().remove(progressIndicator);
                    if (icon.getImage() != null) {
                        tabContent.getChildren().add(0, icon);
                    }
                    break;
                case CONNECTING:
                    if (b == TabState.IDLE) {
                        tabContent.getChildren().remove(icon);
                        tabContent.getChildren().add(0, progressIndicator);
                    }
                    break;
                case RENDERING:
                    if (b == TabState.IDLE) {
                        tabContent.getChildren().remove(icon);
                        tabContent.getChildren().add(0, progressIndicator);
                    }
            }
        });
        selectedProperty.addListener((o, b, s) -> {
            pseudoClassStateChanged(SELECTED, s);
            if (s) {
                pool.unlock();
            } else {
                pool.lock();
            }
        });
    }

    public void close() {
        if (onClose != null) {
            onClose.run();
        }
        pool.close();
    }

    private Runnable onClose;

    public void onClose(Runnable onClose) {
        this.onClose = onClose;
    }

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");
    private final BooleanProperty selectedProperty = new SimpleBooleanProperty();

    public final boolean isSelected() {
        return selectedProperty.get();
    }

    public final void setSelected(boolean value) {
        selectedProperty.set(value);
    }

    public BooleanProperty selectedProperty() {
        return selectedProperty;
    }

    private final ObjectProperty<TabState> tabStateProperty = new SimpleObjectProperty<>();

    public final TabState getTabState() {
        return tabStateProperty.get();
    }

    public final void setTabState(TabState value) {
        tabStateProperty.set(value);
    }

    public ObjectProperty<TabState> tabStateProperty() {
        return tabStateProperty;
    }

    private final AtomicInteger loadId = new AtomicInteger();
    private final AtomicInteger loadTaskCount = new AtomicInteger();

    private final RenderPool pool = new RenderPool();

    private void loadURL(URL url, Runnable onUnknownHost) {
        Async.asyncStandard(() -> {
            try {
                loadId.incrementAndGet();
                loadTaskCount.set(0);
                Platform.runLater(() -> setTabState(TabState.CONNECTING));
                Document doc = Jsoup.connect(url.toExternalForm()).get();
                Platform.runLater(() -> {
                    tabTitle.setText(doc.head().getElementsByTag("title").get(0).text());
                    setTabState(TabState.RENDERING);
                });
            } catch (UnknownHostException ex) {
                onUnknownHost.run();
            } catch (IOException ex) {
                Logger.getLogger(TabController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> setTabState(TabState.IDLE));
            }
        });
    }

    private void search(String original) {
        try {
            URL url = new URL("https://www.google.com/search?q=" + URLEncoder.encode(original, "utf-8") + "&sourceid=browser&ie=UTF-8");
            loadURL(url, () -> search(original));
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void load(String trial) {
        try {
            loadURL(new URL(trial), () -> search(trial));
        } catch (MalformedURLException ex) {
            search(trial);
        }
    }

    private final ObjectProperty<Node> sceneGraphProperty = new SimpleObjectProperty<>();

    public final Node getSceneGraph() {
        return sceneGraphProperty.get();
    }

    public ObjectProperty<Node> sceneGraphProperty() {
        return sceneGraphProperty;
    }
}
