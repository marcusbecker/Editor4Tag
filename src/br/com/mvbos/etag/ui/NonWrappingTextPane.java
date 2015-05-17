/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.ui;

import java.awt.Component;
import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Marcus Becker
 */
public class NonWrappingTextPane extends JTextPane {

    public NonWrappingTextPane() {
        super();
    }

    public NonWrappingTextPane(StyledDocument doc) {
        super(doc);
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        //ComponentUI ui = getUI();

        return parent != null ? (getUI().getPreferredSize(this).width <= parent
                .getSize().width) : true;
    }

}
