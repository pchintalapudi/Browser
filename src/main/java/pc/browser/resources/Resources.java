/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.resources;

import java.io.IOException;
import javafx.fxml.FXMLLoader;

/**
 *
 * @author prem
 */
public class Resources {

    public static FXMLLoader getFXMLLoader(String localName) {
        return new FXMLLoader(Resources.class.getResource("/fxml/" + localName));
    }

    public static <T> T load(FXMLLoader loader) {
        try {
            return loader.load();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public static <T> T directLoad(String localName) {
        return load(getFXMLLoader(localName));
    }
}
