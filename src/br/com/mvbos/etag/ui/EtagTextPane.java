/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.StyledDocument;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 *
 * @author Marcus Becker
 */
public class EtagTextPane extends JTextPane {

    public static final DefaultHighlighter.DefaultHighlightPainter hPainter = new DefaultHighlighter.DefaultHighlightPainter(new Color(57, 105, 138));
    public static final DefaultHighlighter.DefaultHighlightPainter findPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.LIGHT_GRAY); //new DefaultHighlighter.DefaultHighlightPainter(new Color(65, 25, 85));
    public final LinePainter painter = new LinePainter(this);

    public static final String UNDO_ACTION = "UNDO_ACTION";
    public static final String REDO_ACTION = "REDO_ACTION";

    private final UndoManager undoMgr = new UndoManager();

    public EtagTextPane() {
        super();
        addUndoRedo();
    }

    public EtagTextPane(StyledDocument doc) {
        super(doc);
        addUndoRedo();
    }

    public void setNewText(String t) {
        super.setText(t);
        undoMgr.discardAllEdits();
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        //ComponentUI ui = getUI();

        return parent != null ? (getUI().getPreferredSize(this).width <= parent
                .getSize().width) : true;
    }

    private void addUndoRedo() {
        // Add listener for undoable events
        this.getDocument().addUndoableEditListener(new UndoableEditListener() {
            @Override
            public void undoableEditHappened(UndoableEditEvent evt) {
                if (evt.getEdit() instanceof AbstractDocument.DefaultDocumentEvent) {
                    AbstractDocument.DefaultDocumentEvent ad = (AbstractDocument.DefaultDocumentEvent) evt.getEdit();
                    if (ad.getType() == DocumentEvent.EventType.CHANGE) {
                        return;
                    }

                    undoMgr.addEdit(evt.getEdit());

                }
            }
        }
        );

        // Add undo/redo actions
        this.getActionMap().put(UNDO_ACTION, new AbstractAction(UNDO_ACTION) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoMgr.canUndo()) {
                        undoMgr.undo();
                    }
                } catch (CannotUndoException e) {

                }
            }
        });
        this.getActionMap().put(REDO_ACTION, new AbstractAction(REDO_ACTION) {
            @Override
            public void actionPerformed(ActionEvent evt) {
                try {
                    if (undoMgr.canRedo()) {
                        undoMgr.redo();
                    }
                } catch (CannotRedoException e) {

                }
            }
        });

        // Create keyboard accelerators for undo/redo actions (Ctrl+Z/Ctrl+Y)
        this.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK), UNDO_ACTION);
        this.getInputMap().put(
                KeyStroke.getKeyStroke(KeyEvent.VK_Y, InputEvent.CTRL_DOWN_MASK), REDO_ACTION);
    }
}
