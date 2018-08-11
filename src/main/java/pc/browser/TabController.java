/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

/**
 * FXML Controller class
 *
 * @author prem
 */
public class TabController extends AnchorPane {

    @FXML
    private AnchorPane AnchorPane;
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
        tabTitle.textProperty().bind(data.titleProperty);
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
        this.onSelect.run();
    }

    private Runnable onSelect;

    public void onSelect(Runnable onSelect) {
        if (onSelect != null) {
            this.onSelect = onSelect;
        }
    }

    private final TabData data = new TabData();

    public TabData getData() {
        return data;
    }

    public static class TabData {

        private final ReadOnlyObjectWrapper<VisitedData> current = new ReadOnlyObjectWrapper(new VisitedData(null));
        private final StringProperty titleProperty = new SimpleStringProperty("New Tab");

        private final ObservableList<VisitedData> history = FXCollections.observableArrayList();
        private final ObservableList<VisitedData> future = FXCollections.observableArrayList();

        {
            current.addListener((o, b, s) -> {
                if (s != null) {

                } else {
                    titleProperty.set("New Tab");
                }
            });
        }

        public ReadOnlyObjectProperty<VisitedData> currentDataProperty() {
            return current.getReadOnlyProperty();
        }

        public void navigated(VisitedData vd) {
            history.add(0, current.get());
            future.clear();
            current.set(vd);
        }

        public void shift(VisitedData target) {
            int index = history.indexOf(target);
            if (index > -1) {
                history.subList(index + 1, history.size()).forEach(vd -> future.add(0, vd));
                history.subList(index, history.size()).clear();
                current.set(target);
            } else if ((index = future.indexOf(target)) > -1) {
                future.subList(0, index).forEach(vd -> history.add(0, vd));
                future.subList(0, index + 1).clear();
                current.set(target);
            }
        }

        public static class VisitedData {

            private final URL url;
            private String title = "";
            private final List<BiConsumer<String, String>> titleChangeListeners = new ArrayList<>();

            public VisitedData(URL url) {
                this.url = url;
            }

            public URL getUrl() {
                return url;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                String prev = this.title;
                this.title = title;
                titleChangeListeners.forEach(tcl -> tcl.accept(prev, title));
            }
        }
    }
}
