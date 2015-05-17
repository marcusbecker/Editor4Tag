/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.core;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Insets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JTextPane;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Marcus Becker
 */
public class StyleUtil {

    public static void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                getStyle(StyleContext.DEFAULT_STYLE);

        Style regular = doc.addStyle("regular", def);
        //StyleConstants.setFontFamily(def, "SansSerif");

        Style s = doc.addStyle("keyword", def);
        StyleConstants.setBold(s, true);
        StyleConstants.setForeground(s, new Color(135, 206, 250));

        s = doc.addStyle("italic", regular);
        StyleConstants.setItalic(s, true);

        s = doc.addStyle("bold", regular);
        StyleConstants.setBold(s, true);

        s = doc.addStyle("small", regular);
        StyleConstants.setFontSize(s, 10);

        s = doc.addStyle("large", regular);
        StyleConstants.setFontSize(s, 16);

        JButton button = new JButton();
        button.setText("BEEP");

        button.setCursor(Cursor.getDefaultCursor());
        button.setMargin(new Insets(0, 0, 0, 0));
        //button.setActionCommand(buttonString);
        //button.addActionListener(this);
        StyleConstants.setComponent(s, button);
    }

    public static void update(StyledDocument doc, JTextPane text) {
        if (doc == null) {
            return;
        }

        try {
            int len = text.getText().length();
            doc.setCharacterAttributes(0, len, doc.getStyle("regular"), true);

            for (String k : KeyWordsUtil.keyWords) {
                int st;
                int idx = text.getText().indexOf(k);

                while (idx > -1) {

                    doc.setCharacterAttributes(idx, k.length(), doc.getStyle("keyword"), true);
                    doc.setCharacterAttributes(idx + k.length(), 0, doc.getStyle("regular"), true);

                    st = idx + k.length();
                    idx = text.getText().indexOf(k, st);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(StyleUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
