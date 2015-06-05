/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag;

import br.com.mvbos.etag.core.ConfigUtil;
import java.awt.Color;
import java.awt.Font;

/**
 *
 * @author Marcus Becker
 */
class FontUtil {

    public static void save(Font font, Color selectedForegroundColor, Color selectedForegroundColor0) {
        if (font == null) {
            return;
        }

        ConfigUtil.save("font_name", font.getFontName());
        ConfigUtil.save("font_size", String.valueOf(font.getSize()));
        ConfigUtil.save("font_style", String.valueOf(font.getStyle()));
    }

    public static Font load() {
        String fName = ConfigUtil.load("font_name");
        String fSize = ConfigUtil.load("font_size");
        String fStyle = ConfigUtil.load("font_style");

        if (fName != null && fSize != null && fStyle != null) {
            try {
                return new Font(fName, Integer.valueOf(fStyle), Integer.valueOf(fSize));
            } catch (NumberFormatException e) {

            }
        }

        return new Font("Courier New", 0, 12);
    }

}
