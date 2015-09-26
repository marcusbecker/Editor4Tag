/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.core;

import br.com.mvbos.etag.pojo.TagList;
import br.com.mvbos.etag.pojo.Tag;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Marcus Becker
 */
public class TagUtil {

    public static TagList cache;
    private static final File tagFile = new File("tag.xml");

    public static String process(Tag t, String text) {
        String res;
        if (t.isBreakLine()) {
            String[] code = t.getCode().split("\n");
            String[] lines = text.split("\n");
            String temp = "";
            String tCode = code[0] + "\n" + Tag.MARK + code[2];
            for (String s : lines) {
                temp += code[1].replaceAll(Tag.MARK, s) + "\n";
            }
            res = tCode.replaceAll(Tag.MARK, temp);
        } else {
            res = t.getCode().replaceAll(Tag.MARK, text);
        }
        return res;
    }

    public static List<Tag> list() {
        TagList lst = loadTagFromXML(tagFile);
        if (lst == null) {
            lst = new TagList();
        }

        cache = lst;
        return lst.getTags();
    }

    public static void saveTagToXML(File f) {
        try {
            if (f == null) {
                f = tagFile;
            }

            TagList tags = new TagList();
            tags.getTags().addAll(list());
            JAXBContext context = JAXBContext.newInstance(TagList.class);
            Marshaller mar = context.createMarshaller();
            mar.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            mar.marshal(tags, f);

        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }

    public static List<Tag> _list() {
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
        arr.add(new Tag(id++, "[list \u2022]", "[list]\r\n* @;\r\n[/list]", true));
        arr.add(new Tag(id++, "[box]", "[box Dica:]\r\n@\r\n[/box]"));
        return arr;
    }

    public static String blankMark(Tag t) {
        return t.getCode().replaceAll(Tag.MARK, " ");
    }

    public static TagList loadTagFromXML(File f) {
        if (f == null) {
            f = tagFile;
        }

        if (!f.exists()) {
            return new TagList();
        }

        try {
            JAXBContext context = JAXBContext.newInstance(TagList.class);
            Unmarshaller un = context.createUnmarshaller();
            TagList tags = (TagList) un.unmarshal(f);

            /*for (int i = 0; i < tags.getTags().size(); i++) {
             Tag t = tags.getTags().get(i);
             t.setId(i + 1);
             }*/
            return tags;
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return null;
    }

}
