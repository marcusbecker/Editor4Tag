/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcus Becker
 */
public class FileUtil {

    public static File selected;

    //private static boolean externalChangeNotified;
    //private static long lastModified;
    private static Map<File, Long> map = new HashMap<>(10);

    public synchronized static String read(File file) {
        //selected = file;

        StringBuilder sb = new StringBuilder(500);
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                //sb.append(System.lineSeparator());
                sb.append('\n');
            }

            map.put(file, Files.getLastModifiedTime(file.toPath()).toMillis());

            sc.close();

        } catch (Exception e) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, e);
        }

        return sb.toString();
    }

    public static boolean isValid(File file) {
        return file.isFile();
    }

    public synchronized static boolean save(String text, File file) {
        try {
            FileWriter fw = new FileWriter(file);
            fw.write(text);
            fw.close();

            map.put(file, Files.getLastModifiedTime(file.toPath()).toMillis());

            return true;

        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public static boolean externalChange(File selected) {

        try {
            long lastModified = map.get(selected);
            long fTime = Files.getLastModifiedTime(selected.toPath()).toMillis();

            if (lastModified != fTime) {
                map.put(selected, fTime);
                return true;
            }

        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

}
