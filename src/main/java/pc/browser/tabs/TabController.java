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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import pc.browser.Async;
import pc.browser.async.RenderTask;
import pc.browser.cache.ImageCache;
import pc.browser.events.URLChangeEvent;
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

    private final RenderPool pool = new RenderPool();

    private final List<History> history = new ArrayList<>();
    private int index = -1;

    private String loadURL(URL url) {
        int id;
        id = loadId.incrementAndGet();
        try {
            Platform.runLater(() -> setTabState(TabState.CONNECTING));
            Document doc = Jsoup.connect(url.toExternalForm()).get();
            Platform.runLater(() -> {
                fireEvent(new URLChangeEvent(url.toExternalForm()));
                loadTaskCount.set(0);
                tabTitle.setText(doc.head().getElementsByTag("title").get(0).text());
                setTabState(TabState.RENDERING);
            });
            Node n = new HTMLElementMapper((r, tt) -> renderAsync(r, id, tt, 0)).map(doc);
            Platform.runLater(() -> {
                sceneGraphProperty.set(n);
            });
            return doc.head().getElementsByTag("title").get(0).text();
        } catch (UnsupportedMimeTypeException ex) {
            if ("image/jpeg".equals(ex.getMimeType())) {
                Platform.runLater(() -> sceneGraphProperty.set(new ImageView(ImageCache.getImageForUrl(ex.getUrl()))));
            }
        } catch (UnknownHostException ex) {
        } catch (IOException ex) {
            Logger.getLogger(TabController.class.getName()).log(Level.SEVERE, null, ex);
            Platform.runLater(() -> setTabState(TabState.IDLE));
        }
        return null;
    }

    private void asyncLoad(URL url, boolean tracked) {
        Async.asyncStandard(() -> {
            if (tracked) {
                loadTrackedURL(url);
            } else {
                loadURL(url);
            }
        });
    }

    private void loadTrackedURL(URL url) {
        String title = loadURL(url);
        if (title != null) {
            history.subList(++index, history.size()).clear();
            history.add(new History(title, url));
        }
    }

    private void renderAsync(Runnable task, int loadId, RenderTask taskType, int priority) {
        pool.asyncRender(() -> {
            if (this.loadId.get() == loadId) {
                try {
                    incrementAsyncCount();
                    task.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                } finally {
                    if (this.loadId.get() == loadId) {
                        decrementAsyncCount();
                    };
                }
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
            loadTrackedURL(url);
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void load(String trial) {
        try {
            if (!trial.contains(":") && trial.contains(".")) {
                trial = "http://" + trial;
            }
            asyncLoad(new URL(trial), true);
        } catch (MalformedURLException ex) {
            search(trial);
        }
    }

    public void history(int index, boolean past) {
        try {
            if (index > -1 && index < (past ? index : (history.size() - index - 1))) {
                if (past) {
                    this.index = index;
                    Platform.runLater(() -> tabTitle.setText(history.get(index).getTitle()));
                    asyncLoad(history.get(index).getUrl(), false);
                } else {
                    this.index += index + 1;
                    Platform.runLater(() -> tabTitle.setText(history.get(this.index).getTitle()));
                    asyncLoad(history.get(this.index).getUrl(), false);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public List<History> past() {
        return history.subList(0, index);
    }

    public List<History> future() {
        return history.subList(index + 1, history.size());
    }

    private final ObjectProperty<Node> sceneGraphProperty = new SimpleObjectProperty<>();

    public final Node getSceneGraph() {
        return sceneGraphProperty.get();
    }

    public ObjectProperty<Node> sceneGraphProperty() {
        return sceneGraphProperty;
    }
}
