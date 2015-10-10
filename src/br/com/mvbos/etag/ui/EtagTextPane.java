/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.ui;

import br.com.mvbos.etag.Window;
import br.com.mvbos.etag.core.EditorState;
import br.com.mvbos.etag.core.FileUtil;
import br.com.mvbos.etag.core.StyleUtil;
import br.com.mvbos.etag.pojo.Tag;
import br.com.mvbos.etag.core.TagUtil;
import java.awt.Color;
import java.awt.Component;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.StyledDocument;
import javax.swing.text.Utilities;
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
    public LinePainter painter;

    public static final String UNDO_ACTION = "UNDO_ACTION";
    public static final String REDO_ACTION = "REDO_ACTION";

    private final UndoManager undoMgr = new UndoManager();

    private final List<ActionListener> actionListenerList = new ArrayList<>(2);

    private MyDocumentListener docListener;

    public void clear() {
    }

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
        public void exportToClipboard(JComponent comp, Clipboard clip, int action) throws IllegalStateException {
            if (action == TransferHandler.COPY || action == TransferHandler.MOVE) {
                clip.setContents(new StringSelection(text.getSelectedText()), null);
                if (action == TransferHandler.MOVE) {
                    text.replaceSelection("");
                }
            }
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

    public static boolean replaceQuote = true;

    public boolean paste(Object o, DataFlavor flavor) {
        try {
            if (flavor == DataFlavor.stringFlavor) {
                String s = o.toString();

                if (replaceQuote) {
                    s = s.replaceAll("“", "\"").replaceAll("”", "\"");
                }

                replaceSelection(s);

                //TODO replaceSelection add \r char and cause bugs on style selection
                //setText(getText().replaceAll("\r", ""));

                //getDocument().insertString(getCaretPosition(), o.toString(), null);
            } else if (flavor == DataFlavor.javaFileListFlavor) {
                List<File> files = (List<File>) o;
                StringBuilder sb = new StringBuilder();

                for (File f : files) {
                    if (f.isDirectory()) {
                        continue;
                    }

                    String ext = f.getName().substring(f.getName().lastIndexOf("."));
                    ext = ext.toLowerCase();

                    String fName = f.getName();

                    try {
                        Path textPath = FileUtil.selected.toPath();
                        Path resPath = textPath.relativize(f.toPath());

                        String p = resPath.toString();
                        if (p.toCharArray()[0] == '.') {
                            p = p.substring(3);
                        }

                        p = p.replaceAll("\\\\", "/");

                        fName = p;

                    } catch (Exception e) {
                        System.err.println("Erro ao obter path " + e.getMessage());
                    }

                    for (Tag tag : TagUtil.cache.getTags()) {
                        if (tag.acceptPaste(ext)) {
                            sb.append(TagUtil.process(tag, fName));
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

    private File file;

    public EtagTextPane() {
        super();
        init();
    }

    public EtagTextPane(StyledDocument doc) {
        super(doc);
        init();
    }

    private int rowNum;
    private int colNum;

    private final Hig hig = new Hig(this);

    private void init() {
        addUndoRedo();
        addTransferHandler();

        this.addCaretListener(hig);

        this.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                textKeyPressed(evt);
            }

            @Override
            public void keyReleased(KeyEvent evt) {
                textKeyReleased(evt);
            }
        });

        //painter = new LinePainter(this);
        addDocumentListener();
    }

    private void textKeyPressed(KeyEvent evt) {
        EditorState.keyRelease = false;
    }

    private void textKeyReleased(KeyEvent evt) {

        if (evt.getModifiers() == 0) {
            StyleUtil.update(this);
            EditorState.keyRelease = true;
        }
    }

    class Hig implements CaretListener {

        private JTextPane t;

        private Hig(EtagTextPane t) {
            this.t = t;
        }

        public void updateRownColumn(int id) {

            try {

                int caretPos = getCaretPosition();
                colNum = 0;
                rowNum = (caretPos == 0) ? 1 : 0;

                int offset = Utilities.getRowStart(t, caretPos);
                colNum = caretPos - offset + 1;

                for (offset = caretPos; offset > 0;) {
                    offset = Utilities.getRowStart(t, offset) - 1;
                    rowNum++;
                }

            } catch (BadLocationException ex) {
                Logger.getLogger(Window.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (!actionListenerList.isEmpty()) {
                ActionEvent aEvent = new ActionEvent(t, id, t.toString());
                for (ActionListener a : actionListenerList) {
                    a.actionPerformed(aEvent);
                }
            }
        }

        @Override
        public void caretUpdate(CaretEvent evt) {
            if (!EditorState.keyRelease) {
                return;
            }

            updateRownColumn(0);

            Highlighter hilite = getHighlighter();
            hilite.removeAllHighlights();

            if (evt.getDot() == evt.getMark()) {
                return;
            }

            int start = getSelectionStart();
            String sel = getSelectedText();

            if (sel == null || sel.trim().isEmpty()) {
                return;
            }

            try {

                int pos = 0;
                String t = getText();

                while ((pos = t.indexOf(sel, pos)) > -1) {
                    hilite.addHighlight(pos, pos + sel.length(), start == pos ? EtagTextPane.findPainter : EtagTextPane.hPainter);
                    pos += sel.length();
                }

            } catch (BadLocationException e) {
            }
        }

    }

    public void goDot(int dot) {
        getCaret().setDot(dot);
        hig.updateRownColumn(1);
    }

    private void addDocumentListener() {
        docListener = new MyDocumentListener();
        this.getDocument().addDocumentListener(docListener);
    }

    public MyDocumentListener getDocumentListener() {
        return docListener;
    }

    public void addLineColumnChangeEvent(ActionListener a) {
        actionListenerList.add(a);
    }

    public int getRowNum() {
        return rowNum;
    }

    public int getColNum() {
        return colNum;
    }

    public void setNewText(String t) {
        super.setText(t);
        undoMgr.discardAllEdits();
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isTemporary() {
        return getFile() == null;
    }

    @Override
    public boolean getScrollableTracksViewportWidth() {
        Component parent = getParent();
        //ComponentUI ui = getUI();

        return parent != null ? (getUI().getPreferredSize(this).width <= parent
                .getSize().width) : true;
    }

    private void addTransferHandler() {
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
