/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Marcus Becker
 */
public class Tag implements Comparable<Tag>, Serializable {

    public static String MARK = "@";

    public static Iterable<Tag> list() {
        List<Tag> arr = new ArrayList<>(10);
        int id = 1;
        arr.add(new Tag(id++, "[chap]", "[chapter @]"));
        arr.add(new Tag(id++, "[sect]", "[section @]"));
        arr.add(new Tag(id++, "code", "%%@%%"));
        arr.add(new Tag(id++, "i", "::@::"));
        arr.add(new Tag(id++, "n", "**@**"));
        arr.add(new Tag(id++, "[lbl]", "[label @]"));
        arr.add(new Tag(id++, "[img]", "[img resources/cap02/@ w=100% \"@\"]"));
        arr.add(new Tag(id++, "[code java]", "[code java]\r\n@\r\n[/code]"));

        arr.add(new Tag(id++, "[list 1]", "[list number]\r\n* @;\r\n[/list]", true));
        arr.add(new Tag(id++, "[list A]", "[list letter]\r\n* @;\r\n[/list]", true));
        arr.add(new Tag(id++, "[list •]", "[list]\r\n* @;\r\n[/list]", true));
        
        arr.add(new Tag(id++, "[box]", "[box Dica:]\r\n@\r\n[/box]"));

        //arr.add(new Tag(1, "[code]", "[code]@[code]"));
        return arr;
    }

    public static String process(Tag t, String text) {
        String res;

        if (t.breakLine) {
            String[] code = t.getCode().split("\n");
            String[] lines = text.split("\n");
            String temp = "";
            String tCode = code[0] + "\n" + MARK + code[2];
            
            for (String s : lines) {
                temp += code[1].replaceAll(MARK, s) + "\n";
            }

            res = tCode.replaceAll(MARK, temp);

        } else {
            res = t.getCode().replaceAll(MARK, text);
        }

        return res;
    }

    public static String blankMark(Tag t) {
        return t.getCode().replaceAll(MARK, " ");
    }

    private int id;
    private String code;
    private String text;
    private boolean breakLine;

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

}
