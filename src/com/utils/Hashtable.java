package com.utils;

import java.util.Enumeration;
import java.util.Vector;

/**
 * @author kiriman
 *
 * Таблица для хранения спрайтов. Не использую нативную, т.к. важен порядок записи.
 */
public class Hashtable extends Vector {

    private Vector key;

    public Hashtable() {
        key = new Vector();
    }

    public void put(Object key, Object value) {
        int id = this.key.indexOf(key);
        if (id > -1) {
            this.key.setElementAt(key, id);
            setElementAt(value, id);
            return;
        }
        this.key.addElement(key);
        addElement(value);
    }

    public Object get(Object key) {
        int id = this.key.indexOf(key);
        if (id > -1) {
            return elementAt(id);
        }
        return null;
    }

    public void remove(Object key) {
        int id = this.key.indexOf(key);
        if (id > -1) {
            this.key.removeElementAt(id);
            removeElementAt(id);
        }
    }

    public void clear() {
        key.removeAllElements();
        removeAllElements();
    }

    public Enumeration keys() {
        return key.elements();
    }
}
