/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.ui;

import br.com.mvbos.etag.core.StyleUtil;
import br.com.mvbos.etag.pojo.Tag;
import br.com.mvbos.etag.core.TagUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
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

    class eTagTransferHandler extends TransferHandler {

        private final EtagTextPane text;

        public eTagTransferHandler(EtagTextPane text) {
            this.text = text;
        }

        @Override
        public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
            return true;
        }

        @Override
        public boolean importData(JComponent comp, Transferable t) {

            try {
                if (t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                    text.paste(t.getTransferData(DataFlavor.stringFlavor).toString(), DataFlavor.stringFlavor);

                } else if (t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                    List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
                    text.paste(files, DataFlavor.javaFileListFlavor);

                } else if (t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
                    //TODO paste image from printscreen
                }

                return true;

            } catch (UnsupportedFlavorException | IOException ufe) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ufe);
            }

            return false;
        }
    }

    public boolean paste(Object o, DataFlavor flavor) {
        try {
            if (flavor == DataFlavor.stringFlavor) {
                getDocument().insertString(getCaretPosition(), o.toString(), null);

            } else if (flavor == DataFlavor.javaFileListFlavor) {
                List<File> files = (List<File>) o;
                StringBuilder sb = new StringBuilder();

                for (File f : files) {
                    if (f.isDirectory()) {
                        continue;
                    }

                    String ext = f.getName().substring(f.getName().lastIndexOf("."));
                    ext = ext.toLowerCase();

                    for (Tag tag : TagUtil.cache.getTags()) {
                        if (tag.acceptPaste(ext)) {
                            sb.append(TagUtil.process(tag, f.getName()));
                        }
                    }
                }

                if (sb.length() > 0) {
                    getDocument().insertString(getCaret().getDot(), sb.toString(), null);
                    StyleUtil.update(this);
                }

            } else if (flavor == DataFlavor.imageFlavor) {
                //TODO paste image from printscreen
            }

            return true;

        } catch (BadLocationException ufe) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ufe);
        }

        return false;
    }

    public EtagTextPane() {
        super();
        addUndoRedo();
        addTransferHandler();
    }

    public EtagTextPane(StyledDocument doc) {
        super(doc);
        addUndoRedo();
        addTransferHandler();
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

    private void addTransferHandler() {
        //System.out.println(this.getTransferHandler().getClass());
        setTransferHandler(new eTagTransferHandler(this));
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
