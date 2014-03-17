package com.view;

import mobilebasic.Main;
import com.view.AuxList;
import java.io.DataInput;
import java.io.DataInputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import mobilebasic.BASIC;
import mobilebasic.RandomAccessFile;

/**
 *
 * @author kiriman
 */
public class LoadScreen extends AuxList implements CommandListener, PathListener {

    private Main main;

    public LoadScreen() {
        super("Загрузить из:");
        this.main = Main.mdl;
        list.addCommand(main.cancelCMD);
        list.setCommandListener(this);
    }

    public void showOpenList() {
        main.display.setCurrent(list);
    }

    private boolean isBasFile(String path) {
        return path.endsWith(".bas");
    }

    public void commandAction(Command command, Displayable disp) {
        if (command == main.cancelCMD) {
            main.display.setCurrent(main.menuList);
        } else {
            FileManager fm;
            int index;
            if ((index = list.getSelectedIndex()) == 0) {
                fm = new FileManager(Settings.path, List.IMPLICIT);
                fm.setSystem(FileManager.FS);
                fm.setCurrentDir(Settings.path);
            } else {
                fm = new FileManager(list.getString(index), List.IMPLICIT);
                fm.setSystem(FileManager.RMS);
            }
            fm.setPathListener(this);
            fm.showCurrentDir();
        }
    }

    public void pathAction(Object object, boolean isCancel) {
        if (isCancel) {
            main.display.setCurrent(list);
        } else {
            try {
                String path = (String) object;
                Object dis = null;
                if (path.startsWith("file:")) {
                    FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                    dis = fc.openDataInputStream();
                } else {
                    dis = new RandomAccessFile(path, true);
                }
                loadSource(path, (DataInput) dis);
                if (dis instanceof RandomAccessFile) {
                    ((RandomAccessFile) dis).close();
                } else {
                    ((DataInputStream) dis).close();
                }
                main.nameProgram = path.substring(path.lastIndexOf('/') + 1, path.length());
                main.nameProgram = main.nameProgram.substring(0, main.nameProgram.indexOf('.'));
                main.menuList.addCommand(main.editorCMD);
                main.editor = new EditorScreen(main);
                main.editor.autoNumb = false;
                main.var_1064 = 0;
                main.offsetLine = 0;
                main.listInCanvas();
                main.display.setCurrent(main.canvas);
            } catch (Throwable ex) {
                ex.printStackTrace();
                String err = ex.getMessage();
                Alert alert = new Alert("Ошибка", err == null ? ex.toString() : err, null, AlertType.ERROR);
                alert.setTimeout(-2);
                main.display.setCurrent(alert);
            }
        }
    }

    private void loadSource(String path, DataInput dis) {
        if (isBasFile(path)) {
            BASIC.LoadFrom(dis);
        } else { // .lis
            BASIC.New();
            BASIC.Enter(dis);
        }
    }
}
