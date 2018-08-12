/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser;

import java.net.URL;
import java.util.stream.Collectors;
import javafx.beans.binding.ListBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.util.Pair;
import org.fxmisc.easybind.EasyBind;

/**
 *
 * @author prem
 */
public class TabLog {

    private final ObservableList<Pair<URL, String>> history = FXCollections.observableArrayList();
    private final ObservableList<Pair<URL, String>> future = FXCollections.observableArrayList();

    public void commit(URL url, String title) {
        future.clear();
        history.add(new Pair<>(url, title));
    }

    public Pair<URL, String> shift(int index, boolean back, Pair<URL, String> current) {
        if (back) {
            future.add(0, current);
            history.subList(0, index).forEach(element -> future.add(0, element));
            Pair<URL, String> toReturn = history.get(index);
            history.subList(0, index + 1).clear();
            return toReturn;
        } else {
            history.add(0, current);
            future.subList(0, index).forEach(element -> history.add(0, element));
            Pair<URL, String> toReturn = future.get(index);
            future.subList(0, index + 1).clear();
            return toReturn;
        }
    }

    public ObservableList<String> historyTitles() {
        return EasyBind.map(history, Pair::getValue);
    }

    public ObservableList<String> futureTitles() {
        return EasyBind.map(future, Pair::getValue);
    }
}
