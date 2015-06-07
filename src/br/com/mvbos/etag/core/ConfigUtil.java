 /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marcus Becker
 */
public class ConfigUtil {

    private final static Properties props = new Properties();
    private static final String PROPERTIES_FILE = "config.properties";

    static {

        try {
            File f = new File(PROPERTIES_FILE);

            if (!f.exists()) {
                f.createNewFile();
            } else {
                props.load(new FileInputStream(f));
            }

        } catch (FileNotFoundException ex) {
            Logger.getLogger(ConfigUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ConfigUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void save(String key, String value) {
        try {
            props.setProperty(key, value);
            props.store(new FileOutputStream(PROPERTIES_FILE), null);
        } catch (IOException ex) {
            Logger.getLogger(ConfigUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String load(String key) {
        return props.getProperty(key);
    }

    public static void saveState(Object obj) {
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("state.ser"));
            out.writeObject(obj);
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ConfigUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Object loadState() {
        Object o = null;

        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream("state.ser"));
            o = in.readObject();
            in.close();
        } catch (FileNotFoundException nfe) {

        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ConfigUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return o;
    }

}
