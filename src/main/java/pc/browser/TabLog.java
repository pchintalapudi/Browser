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

/**
 *
 * @author prem
 */
public class TabLog {

    private final ObservableList<Pair<URL, String>> history = FXCollections.observableArrayList();
    private final ObservableList<Pair<URL, String>> future = FXCollections.observableArrayList();
    private final ObservableList<String> historyTitles = new ListBinding<String>() {
        @Override
        protected ObservableList<String> computeValue() {
            return FXCollections.observableArrayList(history.stream().map(Pair::getValue).collect(Collectors.toList()));
        }
    };
    private final ObservableList<String> futureTitles = new ListBinding<String>() {
        @Override
        protected ObservableList<String> computeValue() {
            return FXCollections.observableArrayList(future.stream().map(Pair::getValue).collect(Collectors.toList()));
        }
    };

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
        return historyTitles;
    }

    public ObservableList<String> futureTitles() {
        return futureTitles;
    }
}
