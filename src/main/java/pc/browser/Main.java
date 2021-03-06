/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import pc.browser.tabs.TabController;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WritableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.fxmisc.easybind.EasyBind;
import org.fxmisc.easybind.monadic.MonadicBinding;
import pc.browser.events.LoadEvent;
import pc.browser.events.URLChangeEvent;
import pc.browser.tabs.History;
import pc.browser.tabs.TabState;

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
    private VBox header;
    @FXML
    private TextField omnibar;
    @FXML
    private ScrollPane content;
    @FXML
    private Label min1, min2;
    @FXML
    private Circle lighting;
    @FXML
    private Rectangle lightingClip;
    @FXML
    private Button backButton, forwardButton, reloadButton;
    @FXML
    private Label reloadButtonText;
    @FXML
    private Node sceneGraph;

    private final ObservableList<TabController> tabs = FXCollections.observableArrayList();
    private final ObjectProperty<TabController> focusedTab = new SimpleObjectProperty<>();

    private final ContextMenu windowMenu = new ContextMenu(), pageMenu = new ContextMenu();

    public Main() {
        MenuItem bScreenshot = new MenuItem("Take Application Screenshot");
        bScreenshot.setOnAction(e -> copy(imageBrowser()));
        windowMenu.getItems().add(bScreenshot);
        MenuItem pScreenshot = new MenuItem("Take Page Screenshot");
        bScreenshot.setOnAction(e -> copy(imageWebImage()));
        pageMenu.getItems().add(pScreenshot);
        MenuItem sScreenshot = new MenuItem("Take Entire Page Screenshot");
        bScreenshot.setOnAction(e -> copy(imageWebScrolledContent()));
        pageMenu.getItems().add(sScreenshot);
        MenuItem inspectElement = new MenuItem("Inspect Element");
        inspectElement.setOnAction(e -> sceneGraph.setVisible(true));
        pageMenu.getItems().add(inspectElement);
    }

    @FXML
    private void initialize() {
        Bindings.bindContent(tabBar.getChildren(), tabs);
        sceneGraph.managedProperty().bind(sceneGraph.visibleProperty());
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
        lightingClip.widthProperty().bind(header.widthProperty());
        lightingClip.heightProperty().bind(header.heightProperty());
        root.addEventFilter(MouseEvent.MOUSE_MOVED, this::trackLighting);
        MonadicBinding<Boolean> idle = EasyBind.select(focusedTab).selectObject(TabController::tabStateProperty).map(TabState.IDLE::equals);
        reloadButtonText.textProperty().bind(idle.map(b -> b ? "↻" : "×"));
        reloadButton.disableProperty().bind(idle.map(Boolean.FALSE::equals));
        content.contentProperty().bind(EasyBind.select(focusedTab).selectObject(TabController::sceneGraphProperty));
        root.addEventHandler(LoadEvent.LOAD_EVENT, e -> {
            root.requestFocus();
            focusedTab.get().load(e.getLoadPayload());
        });
        root.addEventHandler(URLChangeEvent.SET_TO_URL, e -> {
            omnibar.setText(e.getURLString());
            if (omnibar.isFocused()) {
                omnibar.selectAll();
            }
        });
        content.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
    }

    @FXML
    private void closeWindow() {
        for (int i = tabs.size() - 1; i > -1 && tabs.size() > 0; i--) {
            tabs.remove(i).close();
        }
        fade(() -> root.getScene().getWindow().hide(), true);
        animate.play();
    }

    private static final double difference = 2.5;

    private void adjustMaximizeIcon(boolean maximized) {
        if (maximized) {
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

    private double mix, miy, miw, mih;

    @FXML
    private void sizing() {
        maximized = !maximized;
        adjustMaximizeIcon(maximized);
        if (!maximized) {
            stageX().setValue(mix);
            stageY().setValue(miy);
            stageWidth().setValue(miw);
            stageHeight().setValue(mih);
        } else {
            mix = stageX().getValue();
            miy = stageY().getValue();
            miw = stageWidth().getValue();
            mih = stageHeight().getValue();
            Rectangle2D bounds = getCurrentScreen().getVisualBounds();
            stageX().setValue(bounds.getMinX());
            stageY().setValue(bounds.getMinY());
            stageWidth().setValue(bounds.getWidth());
            stageHeight().setValue(bounds.getHeight());
        }
    }

    private boolean maximized;

    private final Timeline animate = new Timeline();

    private double initY = -1, initH = -1;

    private boolean iAdded;
    private final ChangeListener<Boolean> iconifiedListener = (o, b, s) -> {
        if (!s) {
            stageY().setValue(initY);
            fade(() -> {
            }, false);
            animate.play();
        }
    };

    @FXML
    private void minimize() {
        fade(() -> getStage().setIconified(true), true);
        initY = stageY().getValue();
        initH = stageHeight().getValue();
        animate.getKeyFrames().add(new KeyFrame(Duration.millis(300), e -> {
            stageY().setValue(initY);
        },
                new KeyValue(stageY(), initY + initH * 0.1)));
        animate.play();
        if (!iAdded) {
            iAdded = true;
            getStage().iconifiedProperty().addListener(iconifiedListener);
        }
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
                root.getScene().getWindow().setWidth(Math.max(value, 300));
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
                root.getScene().getWindow().setHeight(Math.max(value, 100));
            }
        } : stageHeight;
    }

    private final ContextMenu backMenu = new ContextMenu(), forwardMenu = new ContextMenu();

    @FXML
    private void backPress() {
        focusedTab.get().history(focusedTab.get().past().size() - 1, true);
    }

    @FXML
    private void forwardPress() {
        focusedTab.get().history(focusedTab.get().future().size() - 1, false);
    }

    @FXML
    private void back(ContextMenuEvent cme) {
        List<History> past = focusedTab.get().past();
        Collections.reverse(past);
        backMenu.getItems().clear();
        for (int i = 0; i < past.size(); i++) {
            int i0 = i;
            MenuItem path = new MenuItem(past.get(i).getTitle());
            path.setOnAction(e -> focusedTab.get().history(i0, true));
            backMenu.getItems().add(path);
        }
        backMenu.show(backButton, cme.getScreenX(), cme.getScreenY());
    }

    @FXML
    private void forward(ContextMenuEvent cme) {
        List<History> future = focusedTab.get().future();
        forwardMenu.getItems().clear();
        for (int i = 0; i < future.size(); i++) {
            int i0 = i;
            MenuItem path = new MenuItem(future.get(i).getTitle());
            path.setOnAction(e -> focusedTab.get().history(i0, false));
            forwardMenu.getItems().add(path);
        }
        forwardMenu.show(forwardButton, cme.getScreenX(), cme.getScreenY());
    }

    @FXML
    private void reload() {
        content.getContent().setOpacity(0.5);
        requestFocus();
    }

    @FXML
    private void load() {
        focusedTab.get().load(omnibar.getText());
    }

    @FXML
    private void newTab() {
        TabController next = new TabController();
        next.onClose(() -> closeTab(next));
        next.setOnMouseClicked(m -> focusedTab.set(next));
        tabs.add(next);
        next.selectedProperty().bind(focusedTab.isEqualTo(next));
        focusedTab.set(next);
    }

    private void closeTab(TabController tab) {
        if (tabs.size() == 1) {
            closeWindow();
        } else {
            if (focusedTab.get() == tab) {
                int tabIndex = tabs.indexOf(tab);
                if (tabIndex > -1) {
                    focusedTab.set(tabIndex == 0 ? tabs.get(1) : tabs.get(tabIndex - 1));
                }
            }
            tabs.remove(tab);
        }
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
            new ProcessBuilder().inheritIO().command(commands).start();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static final Clipboard SYSTEM_CLIPBOARD = Clipboard.getSystemClipboard();

    private void copy(Image image) {
        ClipboardContent cc = new ClipboardContent();
        cc.putImage(image);
        SYSTEM_CLIPBOARD.setContent(cc);
    }

    private Image imageBrowser() {
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.TRANSPARENT);
        return root.snapshot(sp, null);
    }

    private Image imageWebImage() {
        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.WHITE);
        return content.snapshot(sp, null);
    }

    private Image imageWebScrolledContent() {

        SnapshotParameters sp = new SnapshotParameters();
        sp.setFill(Color.WHITE);
        return content.getContent().snapshot(sp, null);
    }

    @FXML
    private void pageClick(MouseEvent m) {
        if (m.getButton() == MouseButton.SECONDARY) {
            pageMenu.show(getStage(), m.getScreenX(), m.getScreenY());
        } else {
            pageMenu.hide();
        }
        m.consume();
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
        if (maximized) {
            sizing();
        }
        stageX().setValue(m.getScreenX() + sx);
        stageY().setValue(m.getScreenY() + sy);
    }

    @FXML
    private void onStageClick(MouseEvent m) {
        if (m.getButton() == MouseButton.PRIMARY) {
            windowMenu.hide();
            if (m.isStillSincePress() && m.getClickCount() > 1) {
                sizing();
            }
        } else if (m.getButton() == MouseButton.SECONDARY) {
            windowMenu.show(getStage(), m.getScreenX(), m.getScreenY());
            m.consume();
        }
    }

    private double ix, iy, iw, ih, px, py;
    private boolean set;

    @FXML
    private void resizeLeft(MouseEvent m) {
        if (maximized) {
            sizing();
            trySetResizeValues();
        }
        m.consume();
        if (stageWidth().getValue() > 300) {
            stageX().setValue(ix + m.getScreenX() - px);
            stageWidth().setValue(iw + px - m.getScreenX());
        }
    }

    @FXML
    private void resizeRight(MouseEvent m) {
        if (maximized) {
            sizing();
            trySetResizeValues();
        }
        m.consume();
        stageWidth().setValue(iw + m.getScreenX() - px);
    }

    @FXML
    private void resizeTop(MouseEvent m) {
        if (maximized) {
            sizing();
            trySetResizeValues();
        }
        m.consume();
        if (stageHeight().getValue() > 100) {
            stageY().setValue(iy + m.getScreenY() - py);
            stageHeight().setValue(ih + py - m.getScreenY());
        }
    }

    @FXML
    private void resizeBottom(MouseEvent m) {
        if (maximized) {
            sizing();
            trySetResizeValues();
        }
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
        set = false;
        m.consume();
        trySetResizeValues();
        px = m.getScreenX();
        py = m.getScreenY();
    }

    private void trySetResizeValues() {
        if (!maximized && !set) {
            set = true;
            ix = stageX().getValue();
            iy = stageY().getValue();
            iw = stageWidth().getValue();
            ih = stageHeight().getValue();
        }
    }

    @FXML
    private void trackLighting(MouseEvent m) {
        lighting.setCenterX(m.getX());
        lighting.setCenterY(m.getY());
    }

    @FXML
    private void omnibarDragOver(DragEvent d) {
        if (d.getGestureSource() != omnibar && d.getDragboard().hasUrl()) {
            d.acceptTransferModes(TransferMode.ANY);
        }
    }
    
    @FXML
    private void omnibarDragDrop(DragEvent d) {
        if (d.getDragboard().hasUrl()) {
            omnibar.setText(d.getDragboard().getUrl());
            load();
            d.setDropCompleted(true);
        } else {
            d.setDropCompleted(false);
        }
    }

    private Stage getStage() {
        return (Stage) root.getScene().getWindow();
    }

    private Screen getCurrentScreen() {
        return Screen.getScreensForRectangle(stageX().getValue(), stageY().getValue(),
                stageWidth().getValue(), stageHeight().getValue())
                .stream().filter(s -> s.getVisualBounds().contains(
                stageX().getValue() + stageWidth().getValue() / 2,
                stageY().getValue() + stageHeight().getValue() / 2))
                .findAny().orElse(Screen.getScreensForRectangle(
                        stageX().getValue(), stageY().getValue(),
                        stageWidth().getValue(), stageHeight().getValue()).get(0));
    }
}
