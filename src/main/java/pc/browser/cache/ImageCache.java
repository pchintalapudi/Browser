/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.cache;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.image.Image;

/**
 *
 * @author prem
 */
public class ImageCache {

    private static final Map<String, SoftReference<Image>> imageCache = new HashMap<>();

    public static Image getImageForUrl(String url) {
        Image i = null;
        try {
            SoftReference<Image> ref = imageCache.computeIfAbsent(url, s -> new SoftReference<>(new Image(s, true)));
            i = ref.get();
        } catch (IllegalArgumentException ex) {
            System.out.println(url);
        }
        if (i == null) {
            i = new Image(url, true);
            imageCache.put(url, new SoftReference<>(i));
        }
        return i;
    }
}
