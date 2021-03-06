/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.plugin;

import br.com.mvbos.etag.Window;
import br.com.mvbos.etag.core.RecentFileUtils;
import java.io.File;

/**
 *
 * @author Marcus Becker
 */
public class ReOpenPlugin implements PluginInterface {

    @Override
    public void init() {
        String[] recents = RecentFileUtils.getRecentsOpeneds();

        for (int i = recents.length - 1; i >= 0; i--) {
            Window.loadTextFromFile(new File(recents[i]));
        }
    }

    @Override
    public void fileOpened(File file) {
    }

    @Override
    public void fileClosed(File file) {
    }

}
