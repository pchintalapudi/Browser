/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.elements;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.collections.FXCollections;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jsoup.nodes.Element;

/**
 *
 * @author prem
 */
public class InputElementMapper {

    public static javafx.scene.Node map(Element element) {
        javafx.scene.Node node;
        switch (element.tagName()) {
            default:
                node = new Group();
                break;
            case "select":
                List<String> options = retrieveOptions(element);
                ChoiceBox<String> choices = new ChoiceBox<>(FXCollections.observableArrayList(options));
                choices.getSelectionModel().selectFirst();
                node = choices;
                break;
            case "datalist":
                options = retrieveOptions(element);
                ComboBox<String> combo = new ComboBox<>(FXCollections.observableArrayList(options));
                node = combo;
                break;
            case "button":
                node = new Button(element.text());
                break;
            case "input":
                node = new TextField();
                break;
            case "textarea":
                node = new TextArea();
                break;
        }
        node.setUserData(element);
        return node;
    }

    public static boolean isInputMapped(String tagName) {
        switch (tagName) {
            case "input":
            case "button":
            case "textarea":
            case "select":
            case "datalist":
                return true;
            default:
                return false;
        }
    }

    private static List<String> retrieveOptions(Element selectOrDatalist) {
        return selectOrDatalist.childNodes().stream().filter(Element.class::isInstance).map(Element.class::cast)
                .flatMap(e -> e.tagName().equals("optgroup") ? recursiveBreakdown(e)
                : e.tagName().equals("option") ? Stream.of(e) : Stream.empty())
                .map(Element::text)
                .collect(Collectors.toList());
    }

    private static Stream<Element> recursiveBreakdown(Element optGroup) {
        return optGroup.childNodes().stream().filter(Element.class::isInstance).map(Element.class::cast)
                .flatMap(e -> e.tagName().equals("optgroup") ? recursiveBreakdown(e)
                : e.tagName().equals("option") ? Stream.of(e) : Stream.empty());
    }
}
