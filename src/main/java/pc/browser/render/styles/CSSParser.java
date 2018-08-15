/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pc.browser.render.styles;

import java.io.IOException;
import java.io.Reader;

/**
 *
 * @author prem
 */
public class CSSParser {

    public void parse(Reader cssReader) throws IOException {
        StringBuilder sb = new StringBuilder();
        int read;
        while ((read = cssReader.read()) > -1) {
            char c = (char) read;
            sb.append(c);
        }
        String file = sb.toString();
        file = file.replaceAll("/\\*.*\\*/", "");
        file = file.replaceAll("\\s+", " ");
    }
}
