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
public class RecentFilePlugin implements PluginInterface {

    @Override
    public void init() {
        Window.addOnMenu(Window.Menu.FILE, Window.SubMenu.RECENT_FILES, RecentFileUtils.getRecentsList());
    }

    @Override
    public void fileOpened(File file) {
        RecentFileUtils.add(file.getAbsolutePath());
    }

    @Override
    public void fileClosed(File file) {
        RecentFileUtils.removeOpened(file.getAbsolutePath());
    }
}
