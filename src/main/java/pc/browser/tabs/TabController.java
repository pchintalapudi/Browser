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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
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
import pc.browser.async.RenderTask;
import pc.browser.render.HTMLElementMapper;
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
        icon.managedProperty().bind(icon.visibleProperty());
        icon.visibleProperty().bind(icon.imageProperty().isNotNull().and(progressIndicator.visibleProperty().not()));
        progressIndicator.managedProperty().bind(progressIndicator.visibleProperty());
        progressIndicator.visibleProperty().bind(tabStateProperty.isNotEqualTo(TabState.IDLE));
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

    private final ObjectProperty<TabState> tabStateProperty = new SimpleObjectProperty<>(TabState.IDLE);

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
    private final ReadWriteLock idLock = new ReentrantReadWriteLock();

    private final RenderPool pool = new RenderPool();

    private void loadURL(URL url, Runnable onUnknownHost) {
        Async.asyncStandard(() -> {
            Lock l = idLock.writeLock();
            int id;
            try {
                l.lockInterruptibly();
                id = loadId.incrementAndGet();
            } catch (InterruptedException ex) {
                return;
            } finally {
                l.unlock();
            }
            try {
                Platform.runLater(() -> setTabState(TabState.CONNECTING));
                Document doc = Jsoup.connect(url.toExternalForm()).get();
                Platform.runLater(() -> {
                    loadTaskCount.set(0);
                    tabTitle.setText(doc.head().getElementsByTag("title").get(0).text());
                    setTabState(TabState.RENDERING);
                });
                Node n = new HTMLElementMapper((r, tt) -> renderAsync(r, id, tt, 0)).map(doc);
                Platform.runLater(() -> sceneGraphProperty.set(n));
            } catch (UnknownHostException ex) {
                onUnknownHost.run();
            } catch (IOException ex) {
                Logger.getLogger(TabController.class.getName()).log(Level.SEVERE, null, ex);
                Platform.runLater(() -> setTabState(TabState.IDLE));
            }
        });
    }

    private void renderAsync(Runnable task, int loadId, RenderTask taskType, int priority) {
        pool.asyncRender(() -> {
            Lock l0 = idLock.readLock();
            try {
                l0.lockInterruptibly();
                if (this.loadId.get() == loadId) {
                    try {
                        incrementAsyncCount();
                        task.run();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        Lock l = idLock.readLock();
                        try {
                            l.lockInterruptibly();
                            if (this.loadId.get() == loadId) {
                                decrementAsyncCount();
                            }
                        } catch (InterruptedException ex) {
                        } finally {
                            l.unlock();
                        }
                    }
                }
            } catch (InterruptedException ex) {
            } catch (Throwable ex) {
                ex.printStackTrace();
            } finally {
                l0.unlock();
            }
        }, taskType, priority);
    }

    private void incrementAsyncCount() {
        loadTaskCount.incrementAndGet();
    }

    private void decrementAsyncCount() {
        if (loadTaskCount.decrementAndGet() == 0) {
            Platform.runLater(() -> setTabState(TabState.IDLE));
        }
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
