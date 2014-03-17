package com.view;

import javax.microedition.lcdui.List;

/**
 *
 * @author kiriman
 */
public class AuxList {

    public List list;

    public AuxList(String title) {
        list = new List(title, List.IMPLICIT);
        list.append("Файловая Система", null);
        list.append("RMS", null);
    }
}
