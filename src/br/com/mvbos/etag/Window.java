/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag;

import br.com.mvbos.etag.core.FileUtil;
import br.com.mvbos.etag.core.MiscUtil;
import br.com.mvbos.etag.core.RecentFileUtils;
import br.com.mvbos.etag.core.StyleUtil;
import br.com.mvbos.etag.pojo.Tag;
import br.com.mvbos.etag.core.TagUtil;
import br.com.mvbos.etag.pojo.State;
import br.com.mvbos.etag.ui.EtagTextPane;
import br.com.mvbos.etag.ui.GoLine;
import br.com.mvbos.etag.ui.TextLineNumber;
import br.com.mvbos.etag.ui.selector.IMyFontSelector;
import br.com.mvbos.etag.ui.selector.MyFontSelector;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Highlighter;

/**
 *
 * @author Marcus Becker
 */
public class Window extends javax.swing.JFrame {

    //TODO replace all
    //TODO tranferir para EtagTextPane
    //TODO Limpar arquivos recentes
    //private MyDocumentListener docListener;
    private final State state;

    private static Tag lastTag;
    private static Timer timer;

    private int searchIndex;

    private GoLine goLine;

    private final Clipboard clipboard;

    private void addLineColumnChangeEvent(EtagTextPane text) {
        text.addLineColumnChangeEvent(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                EtagTextPane t = (EtagTextPane) evt.getSource();

                if (evt.getID() == 0) {
                    lblInfo.setText("Line: " + t.getRowNum() + " | Column: " + t.getColNum());
                } else {
                    //TODO corrigir
                    lblInfo.setText("");
                }
            }
        });
    }

    private final Map<String, Short> filePx = new HashMap<>(10);
    private final List<EtagTextPane> editors = new ArrayList<>(10);

    private EtagTextPane addEditor() {
        JPanel pn = new JPanel();
        EtagTextPane editor = new EtagTextPane();
        JScrollPane spText = new JScrollPane();
        spText.setViewportView(editor);
        editors.add(editor);
        tabbedPane.addTab("arquivo", pn);

        final GroupLayout pnLayout = new GroupLayout(pn);
        pn.setLayout(pnLayout);
        pnLayout.setHorizontalGroup(
                pnLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(spText, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );
        pnLayout.setVerticalGroup(
                pnLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(spText, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
        );

        spText.setRowHeaderView(new TextLineNumber(editor));

        loadFont(editor);
        addLineColumnChangeEvent((EtagTextPane) editor);
        StyleUtil.addStylesToDocument(editor.getStyledDocument());

        return editor;
    }

    public Window() {
        initComponents();

        //addEditor();
        addTagButtons();
        addRecentFileList();

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        String[] recents = RecentFileUtils.getRecentsOpeneds();

        for (String s : recents) {
            loadTextFromFile(new File(s));
        }

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                if (editors.isEmpty()) {
                    return;
                }

                int sel = tabbedPane.getSelectedIndex();
                EtagTextPane editor = editors.get(sel);

                if (FileUtil.selected != null) {

                    if (FileUtil.externalChange(FileUtil.selected)) {
                        int res = JOptionPane.showConfirmDialog(getContentPane(), "Reload file?", "File external change", JOptionPane.YES_NO_OPTION);

                        if (res == JOptionPane.YES_OPTION) {
                            reloadTextFromFile(sel, editor, FileUtil.selected);
                        } else {
                            editor.getDocumentListener().externalChange();
                        }
                    }

                    String title = tabbedPane.getTitleAt(sel);

                    if (editor.getDocumentListener().isChange() && !title.startsWith("*")) {
                        tabbedPane.setTitleAt(sel, "*" + title);
                    } else if (!editor.getDocumentListener().isChange() && title.startsWith("*")) {
                        tabbedPane.setTitleAt(sel, title.replaceFirst("\\*", ""));
                    }
                }
            }
        });

        state = MiscUtil.loadState();
        config(state);

        timer.start();
    }

    private EtagTextPane getSelected() {
        int sel = tabbedPane.getSelectedIndex();
        if (sel != -1) {
            return editors.get(sel);
        }

        return null;
    }

    private void addTagButtons() {
        int shortcutId = 1;

        for (final Tag t : TagUtil.list()) {

            JButton btn = new JButton(t.getText());
            JMenuItem item = new JMenuItem(t.getText());

            Action ac = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    if (getSelected() == null) {
                        return;
                    }

                    String sel = getSelected().getSelectedText();

                    if (sel == null) {
                        int st = getSelected().getCaret().getDot();
                        int stCode = t.getCode().indexOf(Tag.MARK);
                        String code = TagUtil.blankMark(t);

                        getSelected().replaceSelection(code);
                        getSelected().setSelectionStart(st + stCode);
                        getSelected().setSelectionEnd(getSelected().getSelectionStart() + 1);

                    } else {
                        String res = TagUtil.process(t, sel);
                        getSelected().replaceSelection(res);
                    }

                    StyleUtil.update(getSelected());
                    //getSelected().requestFocus();
                    lastTag = t;
                }
            };

            btn.addActionListener(ac);
            item.addActionListener(ac);

            if (shortcutId == 0) {
            } else if (shortcutId == 10) {
                shortcutId = 0;
                btn.setToolTipText("Shortcut in CTRL + 0");
            } else {
                btn.setToolTipText("Shortcut in CTRL + " + shortcutId);
                shortcutId++;
            }

            if (t.getShortCut() != null && !t.getShortCut().isEmpty()) {
                btn.setToolTipText(btn.getToolTipText() + " or " + t.getShortCut());
            }

            tagMenu.add(item);
            pnTag.add(btn);
        }
    }

    private void addKeyListern(EtagTextPane editor) {
        int shortcutId = 1;

        for (final Tag t : TagUtil.list()) {

            Action ac = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String sel = getSelected().getSelectedText();

                    if (sel == null) {
                        int st = getSelected().getCaret().getDot();
                        int stCode = t.getCode().indexOf(Tag.MARK);
                        String code = TagUtil.blankMark(t);

                        getSelected().replaceSelection(code);
                        getSelected().setSelectionStart(st + stCode);
                        getSelected().setSelectionEnd(getSelected().getSelectionStart() + 1);

                    } else {
                        String res = TagUtil.process(t, sel);
                        getSelected().replaceSelection(res);
                    }

                    StyleUtil.update(getSelected());
                    //getSelected().requestFocus();
                    lastTag = t;
                }
            };

            int code;
            if (shortcutId == 0) {
                code = -1;
            } else if (shortcutId == 10) {
                shortcutId = 0;
                code = KeyEvent.VK_0;
            } else {
                code = KeyStroke.getKeyStroke(String.valueOf(shortcutId)).getKeyCode();
                shortcutId++;
            }

            if (code != -1) {
                final String link = "shortcut_" + shortcutId;
                editor.getInputMap().put(KeyStroke.getKeyStroke(code, InputEvent.CTRL_DOWN_MASK), link);
                editor.getActionMap().put(link, ac);
            }

            if (t.getShortCut() != null && !t.getShortCut().isEmpty()) {
                editor.getInputMap().put(KeyStroke.getKeyStroke(t.getShortCut()), t.getShortCut());
                editor.getActionMap().put(t.getShortCut(), ac);
            }
        }

    }

    private void reloadTextFromFile(int sel, EtagTextPane editor, File selected) {
        int cp = editor.getCaretPosition();

        editor.setNewText(FileUtil.read(selected));
        editor.setCaretPosition(cp);
        StyleUtil.update(editor);
    }

    private void loadTextFromFile(File file) {

        if (!FileUtil.isValid(file)) {
            return;
        }

        if (filePx.containsKey(file.getAbsolutePath())) {
            short sel = filePx.get(file.getAbsolutePath());
            tabbedPane.setSelectedIndex(sel);

        } else {

            RecentFileUtils.add(file.getAbsolutePath());

            EtagTextPane text = addEditor();

            text.setFile(file);
            text.setNewText(FileUtil.read(file));
            text.setCaretPosition(0);
            StyleUtil.update(text);

            addKeyListern(text);

            short sel = (short) (tabbedPane.getTabCount() - 1);
            tabbedPane.setTitleAt(sel, file.getName());
            tabbedPane.setToolTipTextAt(sel, file.getAbsolutePath());
            tabbedPane.setSelectedIndex(sel);

            text.getDocumentListener().setChange(false);

            filePx.put(file.getAbsolutePath(), sel);

            FileUtil.selected = file;
        }
    }

    private void saveFile(int sel) throws HeadlessException {
        EtagTextPane text = editors.get(sel);

        if (text.getFile() == null) {
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(tabbedPane.getTitleAt(sel)));
            int returnVal = fc.showSaveDialog(this);
            //fileChooser.setDialogTitle("Specify a file to save");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (FileUtil.save(text.getText(), file)) {

                    tabbedPane.setTitleAt(sel, file.getName());
                    tabbedPane.setToolTipTextAt(sel, file.getAbsolutePath());

                    FileUtil.selected = file;

                    text.setFile(file);
                    text.getDocumentListener().setChange(false);
                }
            }
        } else {
            if (FileUtil.save(text.getText(), text.getFile())) {
                text.getDocumentListener().setChange(false);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnTag = new javax.swing.JPanel();
        btnAdd = new javax.swing.JButton();
        tabbedPane = new javax.swing.JTabbedPane();
        pnSearch = new javax.swing.JPanel();
        btnCloseSearch = new javax.swing.JButton();
        tfSearch = new javax.swing.JTextField();
        lblReplace = new javax.swing.JLabel();
        tfReplace = new javax.swing.JTextField();
        btnReplace = new javax.swing.JButton();
        btnReplaceAll = new javax.swing.JButton();
        lblInfo = new javax.swing.JLabel();
        btnSecSave = new javax.swing.JButton();
        fileMenu = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        miNew = new javax.swing.JMenuItem();
        miOpen = new javax.swing.JMenuItem();
        miSave = new javax.swing.JMenuItem();
        miClose = new javax.swing.JMenuItem();
        sepRecentFiles = new javax.swing.JPopupMenu.Separator();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        miExit = new javax.swing.JMenuItem();
        editMenu = new javax.swing.JMenu();
        miPaste = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        miFind = new javax.swing.JMenuItem();
        miGoLine = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JPopupMenu.Separator();
        miFont = new javax.swing.JMenuItem();
        miRepeat = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();
        tagMenu = new javax.swing.JMenu();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Editor4Tag");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pnTag.setBackground(new java.awt.Color(204, 204, 204));
        pnTag.setLayout(new java.awt.GridBagLayout());

        btnAdd.setText("+");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });
        pnTag.add(btnAdd, new java.awt.GridBagConstraints());

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        btnCloseSearch.setText("Close find");
        btnCloseSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCloseSearchActionPerformed(evt);
            }
        });

        tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                tfSearchKeyReleased(evt);
            }
        });

        lblReplace.setText("Replace:");

        btnReplace.setText("Replace");
        btnReplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnReplaceActionPerformed(evt);
            }
        });

        btnReplaceAll.setText("Replace All");

        javax.swing.GroupLayout pnSearchLayout = new javax.swing.GroupLayout(pnSearch);
        pnSearch.setLayout(pnSearchLayout);
        pnSearchLayout.setHorizontalGroup(
            pnSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnCloseSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfReplace, javax.swing.GroupLayout.DEFAULT_SIZE, 207, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnReplace)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnReplaceAll)
                .addContainerGap())
        );
        pnSearchLayout.setVerticalGroup(
            pnSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnSearchLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnSearchLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnCloseSearch)
                    .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblReplace)
                    .addComponent(tfReplace, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnReplace)
                    .addComponent(btnReplaceAll))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblInfo.setText("-");

        btnSecSave.setText("S");
        btnSecSave.setToolTipText("Save");
        btnSecSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSecSaveActionPerformed(evt);
            }
        });

        menuFile.setText("File");

        miNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        miNew.setText("New");
        miNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miNewActionPerformed(evt);
            }
        });
        menuFile.add(miNew);

        miOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        miOpen.setText("Open...");
        miOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miOpenActionPerformed(evt);
            }
        });
        menuFile.add(miOpen);

        miSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        miSave.setText("Save");
        miSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miSaveActionPerformed(evt);
            }
        });
        menuFile.add(miSave);

        miClose.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        miClose.setText("Close");
        miClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miCloseActionPerformed(evt);
            }
        });
        menuFile.add(miClose);
        menuFile.add(sepRecentFiles);
        menuFile.add(jSeparator4);

        miExit.setText("Exit");
        miExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miExitActionPerformed(evt);
            }
        });
        menuFile.add(miExit);

        fileMenu.add(menuFile);

        editMenu.setText("Edit");

        miPaste.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_V, java.awt.event.InputEvent.CTRL_MASK));
        miPaste.setText("Paste");
        miPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miPasteActionPerformed(evt);
            }
        });
        editMenu.add(miPaste);
        editMenu.add(jSeparator2);

        miFind.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_MASK));
        miFind.setText("Find");
        miFind.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miFindActionPerformed(evt);
            }
        });
        editMenu.add(miFind);

        miGoLine.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        miGoLine.setText("Go...");
        miGoLine.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miGoLineActionPerformed(evt);
            }
        });
        editMenu.add(miGoLine);
        editMenu.add(jSeparator3);

        miFont.setText("Font...");
        miFont.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miFontActionPerformed(evt);
            }
        });
        editMenu.add(miFont);

        miRepeat.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        miRepeat.setText("Repeat tag");
        miRepeat.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                miRepeatActionPerformed(evt);
            }
        });
        editMenu.add(miRepeat);

        jMenuItem1.setText("to URL");
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        editMenu.add(jMenuItem1);

        fileMenu.add(editMenu);

        tagMenu.setText("Tags");
        fileMenu.add(tagMenu);

        setJMenuBar(fileMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabbedPane)
            .addComponent(pnSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addComponent(lblInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnSecSave)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnTag, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 359, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblInfo)
                    .addComponent(btnSecSave)))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed

        int sel = tabbedPane.getSelectedIndex();
        EtagTextPane text = editors.get(sel);

        String selection = text.getSelectedText();
        if (selection != null) {
            String s = selection;
            s = s.toLowerCase().replaceAll(" ", "_").replaceAll("\\.", "_");
            text.replaceSelection(s);
        }

    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void miOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miOpenActionPerformed
        verifyChangeAndOpenFile(null);
    }//GEN-LAST:event_miOpenActionPerformed

    private void miSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSaveActionPerformed
        int sel = tabbedPane.getSelectedIndex();
        if (sel != -1) {
            saveFile(sel);
        }
    }//GEN-LAST:event_miSaveActionPerformed


    private void miNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miNewActionPerformed

        EtagTextPane text = addEditor();

        //FileUtil.selected = null;
        //docListener.setChange(false);
        //TODO resetar
        //text.setText(null);
        int sel = tabbedPane.getSelectedIndex();
        tabbedPane.setTitleAt(sel, "New file.txt");
        tabbedPane.setToolTipTextAt(sel, null);

    }//GEN-LAST:event_miNewActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        closeApplication();
    }//GEN-LAST:event_formWindowClosing

    private void miRepeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRepeatActionPerformed

        int sel = tabbedPane.getSelectedIndex();
        EtagTextPane text = editors.get(sel);

        if (lastTag != null) {
            String selection = text.getSelectedText();

            if (selection != null) {
                String res = TagUtil.process(lastTag, selection);
                text.replaceSelection(res);
                StyleUtil.update(text);
                //text.requestFocus();
            }

        }

    }//GEN-LAST:event_miRepeatActionPerformed

    public void removeHighlights(JTextPane jtext) {
        Highlighter hilite = jtext.getHighlighter();
        Highlighter.Highlight[] hilites = hilite.getHighlights();
        for (Highlighter.Highlight h : hilites) {
            if (h.getPainter() instanceof DefaultHighlightPainter) {
                hilite.removeHighlight(h);
            }
        }
    }


    private void tfSearchKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSearchKeyReleased

        EtagTextPane text = getSelected();

        if (text == null) {
            return;
        }

        String s = tfSearch.getText();
        String t = text.getText();

        if (s != null && !s.isEmpty()) {
            if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
                searchIndex++;
            }

            int idx = t.indexOf(s, searchIndex);
            if (idx > -1) {
                text.select(idx, idx + s.length());
                searchIndex = idx;
            } else {
                searchIndex = 0;
            }
        }

    }//GEN-LAST:event_tfSearchKeyReleased

    private void miFindActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miFindActionPerformed

        EtagTextPane text = getSelected();
        if (text == null) {
            return;
        }

        pnSearch.setVisible(true);
        tfSearch.requestFocus();
        if (text.getSelectedText() != null && !text.getSelectedText().trim().isEmpty()) {
            tfSearch.setText(text.getSelectedText());
        }

    }//GEN-LAST:event_miFindActionPerformed

    private void btnCloseSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseSearchActionPerformed
        pnSearch.setVisible(false);
        tfSearch.setText(null);
    }//GEN-LAST:event_btnCloseSearchActionPerformed

    private void miGoLineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miGoLineActionPerformed

        int sel = tabbedPane.getSelectedIndex();
        EtagTextPane text = editors.get(sel);

        if (goLine == null) {
            goLine = new GoLine(this, false, text);
        }

        goLine.setLocationRelativeTo(this);
        goLine.setVisible(true);

    }//GEN-LAST:event_miGoLineActionPerformed

    private void miPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miPasteActionPerformed

        int sel = tabbedPane.getSelectedIndex();
        EtagTextPane t = editors.get(sel);

        try {
            if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
                t.paste(clipboard.getData(DataFlavor.stringFlavor).toString(), DataFlavor.stringFlavor);

            } else if (clipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor)) {
                List<File> files = (List<File>) clipboard.getData(DataFlavor.javaFileListFlavor);

                t.paste(files, DataFlavor.javaFileListFlavor);

            } else if (clipboard.isDataFlavorAvailable(java.awt.datatransfer.DataFlavor.imageFlavor)) {
                //TODO paste image from printscreen
            }

        } catch (UnsupportedFlavorException | IOException ufe) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ufe);
        }
    }//GEN-LAST:event_miPasteActionPerformed

    private void miFontActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miFontActionPerformed
        int sel = tabbedPane.getSelectedIndex();
        final EtagTextPane text = editors.get(sel);

        MyFontSelector m = new MyFontSelector(this, true, text.getFont());
        m.setLocationRelativeTo(this);
        m.setColors(text.getForeground(), text.getBackground());

        m.setEvent(new IMyFontSelector() {

            @Override
            public void saveAction(Object obj) {
                MyFontSelector mfs = (MyFontSelector) obj;
                if (mfs.getSelectedFont() != null) {
                    text.setFont(mfs.getSelectedFont());
                    FontUtil.save(mfs.getSelectedFont(), mfs.getSelectedForegroundColor(), mfs.getSelectedForegroundColor());
                }

                if (mfs.getSelectedForegroundColor() != null) {
                    text.setForeground(mfs.getSelectedForegroundColor());
                }

                if (mfs.getSelectedBackgroundColor() != null) {
                    text.setBackground(mfs.getSelectedBackgroundColor());
                }
            }

            @Override
            public void cancelAction(Object obj) {
            }

            @Override
            public void change(Object obj) {
            }

        });

        m.setColors(text.getForeground(), text.getBackground());
        m.setVisible(true);

    }//GEN-LAST:event_miFontActionPerformed

    private void miExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miExitActionPerformed
        closeApplication();
    }//GEN-LAST:event_miExitActionPerformed

    private void btnReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReplaceActionPerformed

        EtagTextPane text = getSelected();

        if (text == null) {
            return;
        }

        text.replaceSelection(tfReplace.getText());
        searchIndex++;

        String s = tfSearch.getText();
        String t = text.getText();

        if (s != null && !s.isEmpty()) {

            int idx = t.indexOf(s, searchIndex);
            if (idx > -1) {
                text.select(idx, idx + s.length());
                searchIndex = idx;
            } else {
                searchIndex = 0;
            }
        }


    }//GEN-LAST:event_btnReplaceActionPerformed

    private void btnSecSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSecSaveActionPerformed
        int sel = tabbedPane.getSelectedIndex();
        if (sel != -1) {
            saveFile(sel);
        }
    }//GEN-LAST:event_btnSecSaveActionPerformed

    private void miCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miCloseActionPerformed
        closeTab();
    }//GEN-LAST:event_miCloseActionPerformed

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
        int sel = ((JTabbedPane) evt.getSource()).getSelectedIndex();
        if (sel != -1) {
            FileUtil.selected = editors.get(sel).getFile();
        }

    }//GEN-LAST:event_tabbedPaneStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCloseSearch;
    private javax.swing.JButton btnReplace;
    private javax.swing.JButton btnReplaceAll;
    private javax.swing.JButton btnSecSave;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuBar fileMenu;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator3;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JLabel lblInfo;
    private javax.swing.JLabel lblReplace;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem miClose;
    private javax.swing.JMenuItem miExit;
    private javax.swing.JMenuItem miFind;
    private javax.swing.JMenuItem miFont;
    private javax.swing.JMenuItem miGoLine;
    private javax.swing.JMenuItem miNew;
    private javax.swing.JMenuItem miOpen;
    private javax.swing.JMenuItem miPaste;
    private javax.swing.JMenuItem miRepeat;
    private javax.swing.JMenuItem miSave;
    private javax.swing.JPanel pnSearch;
    private javax.swing.JPanel pnTag;
    private javax.swing.JPopupMenu.Separator sepRecentFiles;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JMenu tagMenu;
    private javax.swing.JTextField tfReplace;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables

    private void loadFont(JTextPane text) {
        text.setFont(FontUtil.load());
    }

    private void config(State state) {
        pnSearch.setVisible(state.showFind);

        //TODO usar lista
        //int sel = tabbedPane.getSelectedIndex();
        //EtagTextPane text = editors.get(sel);
        //text.goDot(state.dot);
        if (!editors.isEmpty()) {
            editors.get(0).requestFocus();
        }

        if (state.dimension != null) {
            this.setSize(state.dimension);
        }
    }

    private boolean confirmAndSave(int sel) {
        int res = JOptionPane.showConfirmDialog(this, "Save changes?", "The text has changed.", JOptionPane.YES_NO_CANCEL_OPTION);
        if (res == JOptionPane.YES_OPTION) {
            saveFile(sel);
            return true;
        }

        return false;
    }

    private void closeTab() {
        int sel = tabbedPane.getSelectedIndex();

        if (sel != -1) {

            EtagTextPane text = editors.get(sel);

            if (text.getDocumentListener().isChange()) {
                confirmAndSave(sel);
            }

            text.clear();
            editors.remove(sel);
            tabbedPane.remove(sel);

            RecentFileUtils.removeOpened(text.getFile().getAbsolutePath());
        }
    }

    private void closeApplication() {

        for (int i = 0; i < editors.size(); i++) {
            EtagTextPane text = editors.get(i);
            text.requestFocus();

            if (text.getDocumentListener().isChange()) {
                confirmAndSave(i);
            }
        }

        timer.stop();

        state.showFind = pnSearch.isVisible();
        state.dimension = this.getSize();
        //state.dot = text.getCaret().getDot();
        MiscUtil.saveState(state);

        dispose();
    }

    private void addRecentFileList() {
        int px = 0;
        menuFile.getTreeLock();
        for (int i = 0; i < menuFile.getComponents().length; i++) {

            Component c = menuFile.getComponents()[i];
            System.out.println("c.getName() " + c.getName());
            if (c.getName().equals("sepRecentFiles")) {
                px = i;
                break;
            }
        }

        if (px == 0) {
            px = menuFile.getItemCount() - 2;
        }

        String[] files = RecentFileUtils.getRecentsList();

        for (String s : files) {
            final File f = new File(s);
            if (!f.exists() || f.isDirectory() /*|| recentFiles.contains(f.getAbsolutePath())*/) {
                continue;
            }

            JMenuItem item = new JMenuItem(f.getName());
            //recentFiles.add(f.getAbsolutePath());
            menuFile.insert(item, px++);
            item.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    verifyChangeAndOpenFile(f);
                }
            });
        }

    }

    private void verifyChangeAndOpenFile(File file) {

        if (file == null) {
            final JFileChooser fc = new JFileChooser(FileUtil.selected);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                file = fc.getSelectedFile();
                loadTextFromFile(file);
            }
        } else {
            loadTextFromFile(file);
        }
    }
}
