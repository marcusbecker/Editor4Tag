/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag;

import br.com.mvbos.etag.core.ConfigUtil;
import br.com.mvbos.etag.core.FileUtil;
import br.com.mvbos.etag.core.MiscUtil;
import br.com.mvbos.etag.core.StyleUtil;
import static br.com.mvbos.etag.core.StyleUtil.addStylesToDocument;
import br.com.mvbos.etag.pojo.Tag;
import br.com.mvbos.etag.core.TagUtil;
import br.com.mvbos.etag.pojo.State;
import br.com.mvbos.etag.ui.EtagTextPane;
import br.com.mvbos.etag.ui.GoLine;
import br.com.mvbos.etag.ui.MyDocumentListener;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
    private MyDocumentListener docListener;

    private final State state;

    private static Tag lastTag;
    private static Timer timer;

    private int searchIndex;

    private GoLine goLine;

    private final Clipboard clipboard;

    public Window() {
        initComponents();
        addTagButtons();
        addLineNumber(text);
        addRecentFileList();
        loadFont(text);

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        addStylesToDocument(text.getStyledDocument());

        docListener = new MyDocumentListener();
        text.getDocument().addDocumentListener(docListener);

        ((EtagTextPane) text).addLineColumnChangeEvent(new ActionListener() {

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

        String recent = ConfigUtil.load("recent_file");
        if (recent != null) {
            loadTextFromFile(new File(recent));
        }

        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int sel = tabbedPane.getSelectedIndex();
                if (FileUtil.selected != null) {

                    if (FileUtil.externalChange(FileUtil.selected)) {
                        int res = JOptionPane.showConfirmDialog(getContentPane(), "Reload file?", "File external change", JOptionPane.YES_NO_OPTION);

                        if (res == JOptionPane.YES_OPTION) {
                            loadTextFromFile(FileUtil.selected);
                        } else {
                            docListener.externalChange();
                        }
                    }

                    String title = tabbedPane.getTitleAt(sel);

                    if (docListener.isChange() && !title.startsWith("*")) {
                        tabbedPane.setTitleAt(sel, "*" + title);
                    } else if (!docListener.isChange() && title.startsWith("*")) {
                        tabbedPane.setTitleAt(sel, title.replaceFirst("\\*", ""));
                    }
                }
            }
        });

        state = MiscUtil.loadState();
        config(state);
        text.requestFocus();

        timer.start();

    }

    private void addLineNumber(JTextPane text) {
        spText.setRowHeaderView(new TextLineNumber(text));
    }

    private void addTagButtons() {
        int shortcutId = 1;

        for (final Tag t : TagUtil.list()) {

            JButton btn = new JButton(t.getText());
            JMenuItem item = new JMenuItem(t.getText());

            Action ac = new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent evt) {
                    String sel = text.getSelectedText();

                    if (sel == null) {
                        int st = text.getCaret().getDot();
                        int stCode = t.getCode().indexOf(Tag.MARK);
                        String code = TagUtil.blankMark(t);

                        text.replaceSelection(code);
                        text.setSelectionStart(st + stCode);
                        text.setSelectionEnd(text.getSelectionStart() + 1);

                    } else {
                        String res = TagUtil.process(t, sel);
                        text.replaceSelection(res);
                    }

                    StyleUtil.update(text);
                    text.requestFocus();
                    lastTag = t;
                }
            };

            btn.addActionListener(ac);
            item.addActionListener(ac);

            int code;
            if (shortcutId == 0) {
                code = -1;
            } else if (shortcutId == 10) {
                shortcutId = 0;
                code = KeyEvent.VK_0;
                btn.setToolTipText("Shortcut in CTRL + 0");
            } else {
                code = KeyStroke.getKeyStroke(String.valueOf(shortcutId)).getKeyCode();
                btn.setToolTipText("Shortcut in CTRL + " + shortcutId);
                shortcutId++;
            }

            if (code != -1) {
                final String link = "shortcut_" + shortcutId;
                text.getInputMap().put(KeyStroke.getKeyStroke(code, InputEvent.CTRL_DOWN_MASK), link);
                text.getActionMap().put(link, ac);
            }

            tagMenu.add(item);
            pnTag.add(btn);
        }
    }

    private void loadTextFromFile(File file) {

        recentFiles.add(file.getAbsolutePath());
        ConfigUtil.saveList("recent_file_list", recentFiles.toArray());

        if (FileUtil.isValid(file)) {
            ((EtagTextPane) text).setNewText(FileUtil.read(file));
            text.setCaretPosition(0);
            StyleUtil.update(text);

            int sel = tabbedPane.getSelectedIndex();
            tabbedPane.setTitleAt(sel, file.getName());
            tabbedPane.setToolTipTextAt(sel, file.getAbsolutePath());

            docListener.setChange(false);
        }
    }

    private void saveFile() throws HeadlessException {
        int sel = tabbedPane.getSelectedIndex();

        if (FileUtil.selected == null) {
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File(tabbedPane.getTitleAt(sel)));
            int returnVal = fc.showSaveDialog(this);
            //fileChooser.setDialogTitle("Specify a file to save");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                if (FileUtil.save(text.getText(), file)) {
                    FileUtil.selected = file;

                    tabbedPane.setTitleAt(sel, file.getName());
                    tabbedPane.setToolTipTextAt(sel, file.getAbsolutePath());

                    docListener.setChange(false);
                }
            }
        } else {
            if (FileUtil.save(text.getText(), FileUtil.selected)) {
                docListener.setChange(false);
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
        pn1 = new javax.swing.JPanel();
        spText = new javax.swing.JScrollPane();
        text = new br.com.mvbos.etag.ui.EtagTextPane();
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

        text.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        text.setOpaque(false);
        text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textKeyReleased(evt);
            }
        });
        spText.setViewportView(text);

        javax.swing.GroupLayout pn1Layout = new javax.swing.GroupLayout(pn1);
        pn1.setLayout(pn1Layout);
        pn1Layout.setHorizontalGroup(
            pn1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spText, javax.swing.GroupLayout.DEFAULT_SIZE, 858, Short.MAX_VALUE)
        );
        pn1Layout.setVerticalGroup(
            pn1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(spText, javax.swing.GroupLayout.DEFAULT_SIZE, 375, Short.MAX_VALUE)
        );

        tabbedPane.addTab("arquivo", pn1);

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
                .addComponent(tfSearch)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblReplace, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tfReplace)
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
                .addComponent(tabbedPane)
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
        String sel = text.getSelectedText();
        if (sel != null) {
            String s = sel;
            s = s.toLowerCase().replaceAll(" ", "_").replaceAll("\\.", "_");
            text.replaceSelection(s);
        }

    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void miOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miOpenActionPerformed
        verifyChangeAndOpenFile(null);
    }//GEN-LAST:event_miOpenActionPerformed

    private void miSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSaveActionPerformed
        saveFile();

    }//GEN-LAST:event_miSaveActionPerformed


    private void miNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miNewActionPerformed

        //TODO remover ao trabalhar com muitos arquivos
        if (docListener.isChange()) {
            int res = JOptionPane.showConfirmDialog(this, "Save changes?", "The text has changed.", JOptionPane.YES_NO_CANCEL_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                saveFile();
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        FileUtil.selected = null;
        docListener.setChange(false);

        //TODO resetar
        text.setText(null);
        int sel = tabbedPane.getSelectedIndex();
        tabbedPane.setTitleAt(sel, "New file.txt");
        tabbedPane.setToolTipTextAt(sel, null);

    }//GEN-LAST:event_miNewActionPerformed

    private void textKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textKeyReleased

        if (evt.getModifiers() == 0) {
            StyleUtil.update(text);
        }

    }//GEN-LAST:event_textKeyReleased

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        close();

    }//GEN-LAST:event_formWindowClosing

    private void miRepeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRepeatActionPerformed

        if (lastTag != null) {
            String sel = text.getSelectedText();

            if (sel != null) {
                String res = TagUtil.process(lastTag, sel);
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
        if (goLine == null) {
            goLine = new GoLine(this, false, text);
        }

        goLine.setLocationRelativeTo(this);
        goLine.setVisible(true);

    }//GEN-LAST:event_miGoLineActionPerformed

    private void miPasteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miPasteActionPerformed

        EtagTextPane t = (EtagTextPane) text;

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
        close();
    }//GEN-LAST:event_miExitActionPerformed

    private void btnReplaceActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnReplaceActionPerformed

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
        saveFile();
    }//GEN-LAST:event_btnSecSaveActionPerformed


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
    private javax.swing.JMenuItem miExit;
    private javax.swing.JMenuItem miFind;
    private javax.swing.JMenuItem miFont;
    private javax.swing.JMenuItem miGoLine;
    private javax.swing.JMenuItem miNew;
    private javax.swing.JMenuItem miOpen;
    private javax.swing.JMenuItem miPaste;
    private javax.swing.JMenuItem miRepeat;
    private javax.swing.JMenuItem miSave;
    private javax.swing.JPanel pn1;
    private javax.swing.JPanel pnSearch;
    private javax.swing.JPanel pnTag;
    private javax.swing.JPopupMenu.Separator sepRecentFiles;
    private javax.swing.JScrollPane spText;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JMenu tagMenu;
    private javax.swing.JTextPane text;
    private javax.swing.JTextField tfReplace;
    private javax.swing.JTextField tfSearch;
    // End of variables declaration//GEN-END:variables

    private void loadFont(JTextPane text) {
        text.setFont(FontUtil.load());
    }

    private void config(State state) {
        pnSearch.setVisible(state.showFind);

        ((EtagTextPane) text).goDot(state.dot);

        if (state.dimension != null) {
            this.setSize(state.dimension);
        }
    }

    private void close() {
        if (docListener.isChange()) {
            int res = JOptionPane.showConfirmDialog(this, "Save changes?", "The text has changed.", JOptionPane.YES_NO_CANCEL_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                saveFile();
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }
        timer.stop();

        state.showFind = pnSearch.isVisible();
        state.dimension = this.getSize();
        state.dot = text.getCaret().getDot();
        MiscUtil.saveState(state);

        dispose();
    }

    public static Set<String> recentFiles = new HashSet<>(20);

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

        String recent = ConfigUtil.load("recent_file_list");
        if (recent != null && !recent.isEmpty()) {
            String[] files = recent.split(ConfigUtil.FILE_LIST_SEP);
            for (String s : files) {
                final File f = new File(s);
                if (!f.exists() || f.isDirectory() || recentFiles.contains(f.getAbsolutePath())) {
                    continue;
                }

                JMenuItem item = new JMenuItem(f.getName());
                recentFiles.add(f.getAbsolutePath());
                menuFile.insert(item, px++);
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent evt) {
                        verifyChangeAndOpenFile(f);
                    }
                });
            }
        }
    }

    private void verifyChangeAndOpenFile(File file) {
        //TODO remover ao trabalhar com muitos arquivos
        if (docListener.isChange()) {
            int res = JOptionPane.showConfirmDialog(this, "Save changes?", "The text has changed.", JOptionPane.YES_NO_CANCEL_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                saveFile();
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        if (file == null) {
            final JFileChooser fc = new JFileChooser(FileUtil.selected);
            int returnVal = fc.showOpenDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                docListener.setChange(false);
                file = fc.getSelectedFile();
                loadTextFromFile(file);
            }
        } else {
            loadTextFromFile(file);
        }
    }
}
