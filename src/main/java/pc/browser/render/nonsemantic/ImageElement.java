/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.nonsemantic;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author prem
 */
public class ImageElement extends ImageView {

    public ImageElement() {
    }

    public ImageElement(String url) {
        super(url);
    }

    public ImageElement(Image image) {
        super(image);
    }

}
