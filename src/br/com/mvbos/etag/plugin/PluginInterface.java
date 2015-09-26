/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.plugin;

import java.io.File;

/**
 *
 * @author Marcus Becker
 */
public interface PluginInterface {

    public void init();

    public void fileOpened(File file);

    public void fileClosed(File file);

}
