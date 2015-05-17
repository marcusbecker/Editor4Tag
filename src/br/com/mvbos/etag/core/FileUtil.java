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
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcus Becker
 */
public class FileUtil {

    public static File selected;

    private static boolean externalChangeNotified;

    private static long lastModified;

    public synchronized static String read(File file) {
        selected = file;

        StringBuilder sb = new StringBuilder(500);
        try {
            Scanner sc = new Scanner(file);
            while (sc.hasNextLine()) {
                sb.append(sc.nextLine());
                //sb.append(System.lineSeparator());
                sb.append('\n');
            }

            externalChangeNotified = false;
            lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
            ConfigUtil.save("recent_file", file.getAbsolutePath());

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

            externalChangeNotified = false;
            lastModified = Files.getLastModifiedTime(file.toPath()).toMillis();
            return true;

        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    public static boolean externalChange(File selected) {

        if (externalChangeNotified) {
            return false;
        }

        try {
            long fTime = Files.getLastModifiedTime(selected.toPath()).toMillis();

            if (lastModified != fTime) {
                externalChangeNotified = true;
                return true;
            }

        } catch (IOException ex) {
            Logger.getLogger(FileUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

}
