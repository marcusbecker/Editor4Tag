/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.ui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JTextPane;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Marcus Becker
 */
public class EtagTextPane extends JTextPane {

    public static final DefaultHighlighter.DefaultHighlightPainter hPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(57, 105, 138));
    public static final DefaultHighlighter.DefaultHighlightPainter findPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(65, 25, 85));
    public final LinePainter painter = new LinePainter(this);
    
    public EtagTextPane() {
        super();
    }

    public EtagTextPane(StyledDocument doc) {
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
