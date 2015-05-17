/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.ui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Marcus Becker
 */
public class MyDocumentListener implements DocumentListener {

    private boolean change;

    @Override
    public void insertUpdate(DocumentEvent e) {
        //System.err.println("insertUpdate");
        change = true;
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        //System.err.println("removeUpdate");
        change = true;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        //System.err.println("changedUpdate");
    }

    public boolean isChange() {
        return change;
    }

    public void setChange(boolean change) {
        this.change = change;
    }

    public void externalChange() {
        change = true;
    }

}
