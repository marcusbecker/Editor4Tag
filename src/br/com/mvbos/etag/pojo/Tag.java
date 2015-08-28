/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.pojo;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Marcus Becker
 */
@XmlRootElement(name = "tag")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tag implements Comparable<Tag>, Serializable {

    public static String MARK = "@";

    private int id;

    @XmlElement
    private String code;
    @XmlElement
    private String text;
    @XmlElement
    private boolean breakLine;

    @XmlElement
    private String acceptPaste;

    @XmlElement
    private String shortCut;

    public Tag() {
    }

    public Tag(int id, String text, String code) {
        this(id, text, code, false);
    }

    public Tag(int id, String text, String code, boolean breakLine) {
        this.id = id;
        this.code = code;
        this.text = text;
        this.breakLine = breakLine;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isBreakLine() {
        return breakLine;
    }

    public void setBreakLine(boolean breakLine) {
        this.breakLine = breakLine;
    }

    public String getAcceptPaste() {
        return acceptPaste;
    }

    public void setAcceptPaste(String acceptPaste) {
        this.acceptPaste = acceptPaste;
    }

    public String getShortCut() {
        return shortCut;
    }

    public void setShortCut(String shortCut) {
        this.shortCut = shortCut;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Tag other = (Tag) obj;
        if (this.id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(Tag t) {
        return Integer.compare(this.getId(), t.getId());
    }

    @Override
    public String toString() {
        return "Tag{" + "id=" + id + ", code=" + code + ", text=" + text + ", breakLine=" + breakLine + '}';
    }

    public boolean acceptPaste(String ext) {
        if (getAcceptPaste() == null || getAcceptPaste().isEmpty()) {
            return false;
        }

        return getAcceptPaste().toLowerCase().contains(ext);
    }

}
