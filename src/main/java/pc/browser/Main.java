/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import javafx.css.PseudoClass;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author prem
 */
public class Main {

    @FXML
    private AnchorPane root;
    @FXML
    private HBox tabBar;

    private final ObservableList<TabController> tabs = FXCollections.observableArrayList();
    private final Map<TabController, Parent> views = new HashMap<>();
    private final ObjectProperty<TabController> focusedTab = new SimpleObjectProperty<>();
    private final ExecutorService async = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        return t;
    });

    private static final PseudoClass SELECTED = PseudoClass.getPseudoClass("selected");

    @FXML
    private void initialize() {
        Bindings.bindContent(tabBar.getChildren(), tabs);
        focusedTab.addListener((o, b, s) -> {
            if (b != null) {
                b.pseudoClassStateChanged(SELECTED, false);
            }
            if (s != null) {
                s.pseudoClassStateChanged(SELECTED, true);
            }
        });
        newTab();
        KeyCodeCombination newTab = new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN);
        KeyCodeCombination closeTab = new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN);
        EventHandler<KeyEvent> keyListener = k -> {
            if (newTab.match(k)) {
                newTab();
            } else if (closeTab.match(k)) {
                closeTab(focusedTab.get());
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
        animate.getKeyFrames().remove(1);
        animate.play();
    }

    @FXML
    private void sizing() {
        ((Stage) root.getScene().getWindow()).setMaximized(!((Stage) root.getScene().getWindow()).isMaximized());
    }

    private final Timeline animate = new Timeline();

    private final ChangeListener<Boolean> iconifiedListener = (o, b, s) -> {
        if (b) {
            fade(() -> {
            }, false);
            animate.play();
        }
    };

    @FXML
    private void minimize() {
        fade(() -> ((Stage) root.getScene().getWindow()).setIconified(true), true);
        animate.play();
        ((Stage) root.getScene().getWindow()).iconifiedProperty().removeListener(iconifiedListener);
        ((Stage) root.getScene().getWindow()).iconifiedProperty().addListener(iconifiedListener);
    }

    private double initX = -1, initY = -1, initW = -1, initH = -1;

    private void fade(Runnable onFinish, boolean out) {
        animate.stop();
        if (initX > -1) {
//            stageX().setValue(initX);
            stageY().setValue(initY);
//            stageWidth().setValue(initW);
            stageHeight().setValue(initH);
        }
        animate.getKeyFrames().setAll(new KeyFrame(Duration.millis(300), e -> onFinish.run(), new KeyValue(stageOpacity(), out ? 0d : 1d)));
        if (out) {
            initX = stageX().getValue();
            initY = stageY().getValue();
            initW = stageWidth().getValue();
            initH = stageHeight().getValue();
            animate.getKeyFrames().add(new KeyFrame(Duration.millis(300), e -> {
                stageX().setValue(initX);
                stageY().setValue(initY);
            },
//                    new KeyValue(stageX(), initX + initW * 0.1),
                    new KeyValue(stageY(), initY + initH * 0.1)));
//                    new KeyValue(stageWidth(), initW * 0.9),
//                    new KeyValue(stageHeight(), initH * 0.9)));
        }
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
    }

    @FXML
    private void load() {
    }

    @FXML
    private void newTab() {
        TabController next = new TabController();
        next.onClose(() -> closeTab(next));
        next.onSelect(() -> focusedTab.set(next));
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
            views.remove(tab);
        }
    }

//    private void newWindow() {
//        ProcessBuilder pb = new ProcessBuilder();
//        try {
//            pb.inheritIO().command("java Browser").start();
//        } catch (IOException ex) {
//            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
