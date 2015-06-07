/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package br.com.mvbos.etag.core;

import br.com.mvbos.etag.pojo.State;

/**
 *
 * @author Marcus Becker
 */
public class MiscUtil {

    public static void saveState(State state) {
        ConfigUtil.saveState(state);
    }

    public static State loadState() {
        State st = (State) ConfigUtil.loadState();
        if (st == null) {
            st = new State();
        }

        return st;
    }

}
