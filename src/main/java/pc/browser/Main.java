/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import com.steadystate.css.parser.CSSOMParser;
import com.steadystate.css.parser.SACParserCSS3;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.w3c.css.sac.InputSource;
import pc.browser.debug.SceneGraphAnalyzer;
import pc.browser.render.elements.Mapper;
import pc.browser.resources.Resources;

/**
 *
 * @author prem
 */
public class Main {

    @FXML
    private AnchorPane root;
    @FXML
    private HBox tabBar;
    @FXML
    private TextField omnibar;
    @FXML
    private ScrollPane content;
    @FXML
    private Label min1, min2;
    @FXML
    private Circle lighting;

    private final ObservableList<TabController> tabs = FXCollections.observableArrayList();
    private final ObjectProperty<TabController> focusedTab = new SimpleObjectProperty<>();
    private final ExecutorService async = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    @FXML
    private void initialize() {
        Bindings.bindContent(tabBar.getChildren(), tabs);
        focusedTab.addListener((o, b, s) -> {
            if (b != null) {
                b.highlightSelected(false);
            }
            if (s != null) {
                s.highlightSelected(true);
            }
            switchToTab(s);
        });
        newTab();
        KeyCodeCombination newTab = new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN);
        KeyCodeCombination closeTab = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
        KeyCodeCombination newWindow = new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN);
        EventHandler<KeyEvent> keyListener = k -> {
            if (newTab.match(k)) {
                newTab();
            } else if (closeTab.match(k)) {
                closeTab(focusedTab.get());
            } else if (newWindow.match(k)) {
                newWindow();
            }
        };
        root.sceneProperty().addListener((o, b, s) -> {
            if (b != null) {
                b.removeEventHandler(KeyEvent.KEY_PRESSED, keyListener);
            }
            if (s != null) {
                s.addEventHandler(KeyEvent.KEY_PRESSED, keyListener);
            }
        });
    }

    @FXML
    private void closeWindow() {
//        tabs.clear();
        fade(() -> root.getScene().getWindow().hide(), true);
        animate.play();
    }

    private static final double difference = 2.5;

    @FXML
    private void sizing() {
        ((Stage) root.getScene().getWindow()).setMaximized(!((Stage) root.getScene().getWindow()).isMaximized());
        if (((Stage) root.getScene().getWindow()).isMaximized()) {
            min1.setTranslateX(-difference);
            min1.setTranslateY(difference / 2);
            min2.setTranslateX(difference);
            min2.setTranslateY(-difference / 2);
        } else {
            min1.setTranslateX(0);
            min1.setTranslateY(0);
            min2.setTranslateX(0);
            min2.setTranslateY(0);
        }
    }

    private final Timeline animate = new Timeline();

    private double initX = -1, initY = -1, initW = -1, initH = -1;

    private final ChangeListener<Boolean> iconifiedListener = (o, b, s) -> {
        if (b) {
            stageY().setValue(initY);
            fade(() -> {
            }, false);
            animate.play();
        }
    };

    @FXML
    private void minimize() {
        fade(() -> ((Stage) root.getScene().getWindow()).setIconified(true), true);
        if (initX > -1) {
//            stageX().setValue(initX);
            stageY().setValue(initY);
//            stageWidth().setValue(initW);
//            stageHeight().setValue(initH);
        }
        initX = stageX().getValue();
        initY = stageY().getValue();
        initW = stageWidth().getValue();
        initH = stageHeight().getValue();
        animate.getKeyFrames().add(new KeyFrame(Duration.millis(300), e -> stageY().setValue(initY),
                //                    new KeyValue(stageX(), initX + initW * 0.1),
                new KeyValue(stageY(), initY + initH * 0.1)));
//                    new KeyValue(stageWidth(), initW * 0.9),
//                    new KeyValue(stageHeight(), initH * 0.9)));
        animate.play();
        ((Stage) root.getScene().getWindow()).iconifiedProperty().removeListener(iconifiedListener);
        ((Stage) root.getScene().getWindow()).iconifiedProperty().addListener(iconifiedListener);
    }

    private void fade(Runnable onFinish, boolean out) {
        animate.stop();
        animate.getKeyFrames().setAll(new KeyFrame(Duration.millis(300), e -> onFinish.run(), new KeyValue(stageOpacity(), out ? 0d : 1d)));
    }

    private WritableValue<Double> stageOpacity, stageX, stageY, stageWidth, stageHeight;

    private WritableValue<Double> stageOpacity() {
        return stageOpacity == null ? stageOpacity = new WritableValue<Double>() {
            @Override
            public Double getValue() {
                return root.getScene().getWindow().getOpacity();
            }

            @Override
            public void setValue(Double value) {
                root.getScene().getWindow().setOpacity(value);
            }
        } : stageOpacity;
    }

    private WritableValue<Double> stageX() {
        return stageX == null ? stageX = new WritableValue<Double>() {
            @Override
            public Double getValue() {
                return root.getScene().getWindow().getX();
            }

            @Override
            public void setValue(Double value) {
                root.getScene().getWindow().setX(value);
            }
        } : stageX;
    }

    private WritableValue<Double> stageY() {
        return stageY == null ? new WritableValue<Double>() {
            @Override
            public Double getValue() {
                return root.getScene().getWindow().getY();
            }

            @Override
            public void setValue(Double value) {
                root.getScene().getWindow().setY(value);
            }
        } : stageY;
    }

    private WritableValue<Double> stageWidth() {
        return stageWidth == null ? new WritableValue<Double>() {
            @Override
            public Double getValue() {
                return root.getScene().getWindow().getWidth();
            }

            @Override
            public void setValue(Double value) {
                root.getScene().getWindow().setWidth(value);
            }
        } : stageWidth;
    }

    private WritableValue<Double> stageHeight() {
        return stageHeight == null ? new WritableValue<Double>() {
            @Override
            public Double getValue() {
                return root.getScene().getWindow().getHeight();
            }

            @Override
            public void setValue(Double value) {
                root.getScene().getWindow().setHeight(value);
            }
        } : stageHeight;
    }

    @FXML
    private void back() {
    }

    @FXML
    private void forward() {
    }

    @FXML
    private void reload() {
        content.getContent().setOpacity(0.5);
        requestFocus();
        load(focusedTab.get().getCurrent(), null);
    }

    private void load(URL url, String original) {
        TabController current = focusedTab.get();
        current.getLoadingProperty().set(true);
        current.setEnteredText(null);
        current.setCurrent(url);
        omnibar.setText(url.toExternalForm());
        async.submit(() -> {
            try {
                Document document = Jsoup.connect(url.toExternalForm()).get();
                System.out.println(document.head().getElementsByTag("title"));
                Platform.runLater(() -> current.setTitle(document.head().getElementsByTag("title").text()));
                try {
                    Parent p = (Parent) new Mapper(new CSSOMParser(new SACParserCSS3())
                            .parseStyleSheet(new InputSource(new BufferedReader(
                                    new InputStreamReader(Resources.getCSS("blink-user-agent.css")
                                            .openStream()))), null, null)).map(document);
                    Platform.runLater(() -> {
                        SceneGraphAnalyzer.show(p);
                        current.sceneGraphProperty().set(p);
                        if (current == focusedTab.get()) {
                            content.setContent(p);
                        }
                    });
                } catch (IOException ex) {
                }
            } catch (IOException ex) {
                safety(original);
            }
        });
    }

    @FXML
    private void load() {
        URL url;
        String text = omnibar.getText();
        if (text.isEmpty()) {
            return;
        }
        try {
            String text0 = text.replace("https://", "http://");
            if (!text0.contains("www.")) {
                text0 = "www." + text0;
            }
            if (!text0.contains("http://")) {
                text0 = "http://" + text0;
            }
            url = new URL(text0);
        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            try {
                url = new URL("https://www.google.com/search?q=" + URLEncoder.encode(omnibar.getText(), "utf-8") + "&sourceid=browser&ie=UTF-8");
            } catch (MalformedURLException | UnsupportedEncodingException ex1) {
                throw new RuntimeException(ex1);
            }
        }
        TabController current = focusedTab.get();
        current.getLog().commit(current.getCurrent(), current.getTitle());
        requestFocus();
        load(url, text);
    }

    private void safety(String original) {
        try {
            URL url = new URL("https://www.google.com/search?q=" + URLEncoder.encode(original, "utf-8") + "&sourceid=browser&ie=UTF-8");
            load(url, original);
        } catch (MalformedURLException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    @FXML
    private void newTab() {
        TabController next = new TabController();
        next.onClose(() -> closeTab(next));
        next.onClick(() -> focusedTab.set(next));
        tabs.add(next);
        focusedTab.set(next);
    }

    private void closeTab(TabController tab) {
        if (tabs.size() == 1) {
            closeWindow();
        } else {
            if (focusedTab.get() == tab) {
                int tabIndex = tabs.indexOf(tab);
                focusedTab.set(tabIndex == 0 ? tabs.get(1) : tabs.get(tabIndex - 1));
            }
            tabs.remove(tab);
        }
    }

    private void switchToTab(TabController tab) {
        content.setContent(tab.sceneGraphProperty().get());
    }

    @FXML
    private void requestFocus() {
        root.requestFocus();
    }

    @FXML
    private void newWindow() {
        try {
            List<String> commands = new ArrayList<>();
            commands.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
            commands.addAll(ManagementFactory.getRuntimeMXBean().getInputArguments());
            commands.add("-cp");
            commands.add(ManagementFactory.getRuntimeMXBean().getClassPath());
            commands.add(Browser.class.getName());
            System.out.println(commands.stream().collect(Collectors.joining(" ")));
            Process p = new ProcessBuilder().inheritIO().command(commands).start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    ============================================================================
    
    Stage manipulation
    
    ============================================================================
     */
    private double sx, sy;

    @FXML
    private void onStagePress(MouseEvent m) {
        sx = stageX().getValue() - m.getScreenX();
        sy = stageY().getValue() - m.getScreenY();
    }

    @FXML
    private void onStageDrag(MouseEvent m) {
        stageX().setValue(m.getScreenX() + sx);
        stageY().setValue(m.getScreenY() + sy);
    }

    private double ix, iy, iw, ih, px, py;

    @FXML
    private void resizeLeft(MouseEvent m) {
        m.consume();
        ((Stage) root.getScene().getWindow()).setMaximized(false);
        stageX().setValue(ix + m.getScreenX() - px);
        stageWidth().setValue(iw + px - m.getScreenX());
    }

    @FXML
    private void resizeRight(MouseEvent m) {
        m.consume();
        stageWidth().setValue(iw + m.getScreenX() - px);
    }

    @FXML
    private void resizeTop(MouseEvent m) {
        m.consume();
        stageY().setValue(iy + m.getScreenY() - py);
        stageHeight().setValue(ih + py - m.getScreenY());
    }

    @FXML
    private void resizeBottom(MouseEvent m) {
        m.consume();
        stageHeight().setValue(ih + m.getScreenY() - py);
    }

    @FXML
    private void resizeLT(MouseEvent m) {
        resizeLeft(m);
        resizeTop(m);
    }

    @FXML
    private void resizeRT(MouseEvent m) {
        resizeRight(m);
        resizeTop(m);
    }

    @FXML
    private void resizeLB(MouseEvent m) {
        resizeLeft(m);
        resizeBottom(m);
    }

    @FXML
    private void resizeRB(MouseEvent m) {
        resizeRight(m);
        resizeBottom(m);
    }

    @FXML
    private void resizePress(MouseEvent m) {
        m.consume();
        trySetResizeValues();
        px = m.getScreenX();
        py = m.getScreenY();
    }

    private void trySetResizeValues() {
        if (!((Stage) root.getScene().getWindow()).isMaximized()) {
            ix = stageX().getValue();
            iy = stageY().getValue();
            iw = stageWidth().getValue();
            ih = stageHeight().getValue();
        }
    }

    @FXML
    private void trackLighting(MouseEvent m) {
        m.consume();
        lighting.setCenterX(m.getX());
        lighting.setCenterY(m.getY());
    }
}
