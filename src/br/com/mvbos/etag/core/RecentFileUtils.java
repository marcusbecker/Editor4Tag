/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.core;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author Marcus Becker
 */
public class RecentFileUtils {

    static LinkedList<Recent> list = new LinkedList<>();

    public static String[] getRecentsOpeneds() {

        if (list.isEmpty()) {
            load();
        }

        List<String> temp = new ArrayList<>(list.size());

        for (int i = list.size() - 1; i >= 0; i--) {
            Recent r = list.get(i);

            if (r.opened) {
                temp.add(r.file);
            }
        }

        return temp.toArray(new String[0]);
    }

    public static String[] getRecentsList() {

        if (list.isEmpty()) {
            load();
        }

        List<String> temp = new ArrayList<>(list.size());

        for (int i = list.size() - 1; i >= 0; i--) {
            Recent r = list.get(i);

            if (r.listed) {
                temp.add(r.file);
            }
        }

        return temp.toArray(new String[0]);
    }

    public static void load() {
        RecentFileUtils rr = new RecentFileUtils();

        String[] arr = ConfigUtil.loadList("recent_file_list");

        for (String s : arr) {
            Recent r = rr.new Recent(s);

            if (!list.contains(r)) {
                r.listed = true;
                list.add(r);
            }
        }

        arr = ConfigUtil.loadList("opened_files");

        for (String s : arr) {
            Recent r = rr.new Recent(s);

            if (list.contains(r)) {
                list.get(list.indexOf(r)).opened = true;
            } else {
                r.opened = true;
                list.add(r);
            }
        }
    }

    public static boolean contains(String filePath) {
        Recent r = new RecentFileUtils().new Recent(filePath);
        return list.contains(r);
    }

    public static void add(String filePath) {

        Recent r = new RecentFileUtils().new Recent(filePath);
        if (!list.contains(r)) {
            r.listed = true;
            r.opened = true;
            list.add(r);
        }
        
        persist();
    }

    private static void persist() {
        List<String> temp = new ArrayList<>(list.size());

        for (Recent r : list) {
            if (r.listed) {
                temp.add(r.file);
            }
        }

        ConfigUtil.saveList("recent_file_list", temp.toArray());

        temp.clear();
        for (Recent r : list) {
            if (r.opened) {
                temp.add(r.file);
            }
        }

        ConfigUtil.saveList("opened_files", temp.toArray());
    }

    public static void removeOpened(String absolutePath) {
        Recent r = new RecentFileUtils().new Recent(absolutePath);
        
        if(list.contains(r)){
            list.remove(r);
            persist();
        }
    }

    public class Recent {

        String file;
        boolean opened;
        boolean listed;

        public Recent(String file) {
            this.file = file;
        }

        @Override
        public int hashCode() {
            return 5;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Recent other = (Recent) obj;
            if (!Objects.equals(this.file, other.file)) {
                return false;
            }
            return true;
        }

    }

}
