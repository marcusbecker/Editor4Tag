/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag;

import br.com.mvbos.etag.core.ConfigUtil;
import br.com.mvbos.etag.core.FileUtil;
import br.com.mvbos.etag.core.StyleUtil;
import static br.com.mvbos.etag.core.StyleUtil.addStylesToDocument;
import br.com.mvbos.etag.core.Tag;
import br.com.mvbos.etag.ui.LinePainter;
import br.com.mvbos.etag.ui.MyDocumentListener;
import br.com.mvbos.etag.ui.NonWrappingTextPane;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Marcus Becker
 */
public class Window extends javax.swing.JFrame {

    /**
     * Creates new form Window
     */
    private StyledDocument doc;
    private MyDocumentListener docListener;

    private static Tag lastTag;
    private static Timer timer;

    public Window() {
        initComponents();
        addTagButtons();

        doc = text.getStyledDocument();
        addStylesToDocument(doc);

        docListener = new MyDocumentListener();
        text.getDocument().addDocumentListener(docListener);
        
        LinePainter painter = new LinePainter(text);

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

        timer.start();
    }

    private void addTagButtons() {
        for (final Tag t : Tag.list()) {
            JButton btn = new JButton(t.getText());

            btn.addActionListener(new java.awt.event.ActionListener() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String sel = text.getSelectedText();

                    if (sel == null) {
                        int st = text.getCaret().getDot();
                        int stCode = t.getCode().indexOf(Tag.MARK);
                        String code = Tag.blankMark(t);

                        text.replaceSelection(code);
                        text.setSelectionStart(st + stCode);
                        text.setSelectionEnd(text.getSelectionStart() + 1);

                        //System.out.println();
                    } else {
                        String res = Tag.process(t, sel);
                        text.replaceSelection(res);
                    }

                    StyleUtil.update(doc, text);
                    text.requestFocus();
                    lastTag = t;
                }
            });
            pnTag.add(btn);
        }
    }

    private void loadTextFromFile(File file) {
        if (FileUtil.isValid(file)) {
            text.setText(FileUtil.read(file));
            StyleUtil.update(doc, text);

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
        jScrollPane2 = new javax.swing.JScrollPane();
        text = new NonWrappingTextPane();
        fileMenu = new javax.swing.JMenuBar();
        menuFile = new javax.swing.JMenu();
        miNew = new javax.swing.JMenuItem();
        miOpen = new javax.swing.JMenuItem();
        miSave = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        editMenu = new javax.swing.JMenu();
        miRepeat = new javax.swing.JMenuItem();
        jMenuItem1 = new javax.swing.JMenuItem();

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
        text.setText("Neste capítulo iremos criar a base para os jogos que serão desenvolvidos nos capítulos seguintes e a cada capítulo e jogo criado, aumentaremos nossa biblioteca de código. Começaremos criando um novo projeto no Eclipse, depois veremos código após código, como criar nosso protótipo de jogo, passando pelas etapas de desenho, animação e interação.");
        text.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                textKeyReleased(evt);
            }
        });
        jScrollPane2.setViewportView(text);

        javax.swing.GroupLayout pn1Layout = new javax.swing.GroupLayout(pn1);
        pn1.setLayout(pn1Layout);
        pn1Layout.setHorizontalGroup(
            pn1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 690, Short.MAX_VALUE)
        );
        pn1Layout.setVerticalGroup(
            pn1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 243, Short.MAX_VALUE)
        );

        tabbedPane.addTab("arquivo", pn1);

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
        miOpen.setText("Open");
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
        menuFile.add(jSeparator1);

        fileMenu.add(menuFile);

        editMenu.setText("Edit");

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

        setJMenuBar(fileMenu);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnTag, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(pnTag, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane))
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
        final JFileChooser fc = new JFileChooser();
        int returnVal = fc.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            loadTextFromFile(file);
        }

    }//GEN-LAST:event_miOpenActionPerformed


    private void miSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miSaveActionPerformed
        saveFile();

    }//GEN-LAST:event_miSaveActionPerformed


    private void miNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miNewActionPerformed

        FileUtil.selected = null;

        text.setText(null);
        int sel = tabbedPane.getSelectedIndex();
        tabbedPane.setTitleAt(sel, "New file.txt");
        tabbedPane.setToolTipTextAt(sel, null);

    }//GEN-LAST:event_miNewActionPerformed

    private void textKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textKeyReleased
        StyleUtil.update(doc, text);

    }//GEN-LAST:event_textKeyReleased

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        if (docListener.isChange()) {
            int res = JOptionPane.showConfirmDialog(this, "Save changes?", "The text has changed.", JOptionPane.YES_NO_CANCEL_OPTION);
            if (res == JOptionPane.YES_OPTION) {
                saveFile();
            } else if (res == JOptionPane.CANCEL_OPTION) {
                return;
            }
        }

        timer.stop();
        dispose();

    }//GEN-LAST:event_formWindowClosing

    private void miRepeatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_miRepeatActionPerformed

        if (lastTag != null) {
            String sel = text.getSelectedText();

            if (sel != null) {
                String res = Tag.process(lastTag, sel);
                text.replaceSelection(res);
                StyleUtil.update(doc, text);
                //text.requestFocus();
            }

        }

    }//GEN-LAST:event_miRepeatActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAdd;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuBar fileMenu;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JMenu menuFile;
    private javax.swing.JMenuItem miNew;
    private javax.swing.JMenuItem miOpen;
    private javax.swing.JMenuItem miRepeat;
    private javax.swing.JMenuItem miSave;
    private javax.swing.JPanel pn1;
    private javax.swing.JPanel pnTag;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JTextPane text;
    // End of variables declaration//GEN-END:variables
}
