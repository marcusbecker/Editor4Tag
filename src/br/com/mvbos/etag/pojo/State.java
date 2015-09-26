/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.pojo;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

/**
 *
 * @author Marcus Becker
 */
public class State implements Serializable {

    private static final long serialVersionUID = -1;

    public boolean showFind;
    public Dimension dimension;
    public int dot;
    public int extendedState;
    public int editorIndex;
    public Point location;

}
