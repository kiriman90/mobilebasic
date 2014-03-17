package com.view;

import mobilebasic.Main;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;
import javax.microedition.rms.RecordStore;

/**
 * @author kiriman
 */
public class FileManager implements CommandListener {

    public static final int RMS = 0;
    public static final int FS = 1;
    private PathListener pl;
    private List fmList, delList;
    private boolean isMark;
    private Command deleteCMD, markCMD, addResCMD, markAllCMD;
    private String pathSeparator, currentDirectoryName, protocol, back, title;
    private Enumeration fileListEnumeration;
    private int system, mode;
    private Main main;
    private Image folderIMG, fileIMG;

    public FileManager(String title, int mode) {
        main = Main.mdl;
        this.title = title;
        setMode(mode);
        system = FS;
        deleteCMD = new Command("Удалить", 3, 10);
        markCMD = new Command("Отметить неск.", 4, 2);
        markAllCMD = new Command("Отметить все", 4, 2);
        addResCMD = new Command("Добавить", 4, 2);
        protocol = "file:///";
        currentDirectoryName = pathSeparator = "/";
        back = "..";
        try {
            folderIMG = Image.createImage("/res/Folder.png");
            fileIMG = Image.createImage("/res/File.png");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /** 
     * Установить тип файловой системы - RMS/FS
     */
    public void setSystem(int system) {
        this.system = system;
    }

    /** 
     * Установить текущую директорию
     */
    public void setCurrentDir(String path) {
        currentDirectoryName = path;
    }

    /**
     * Добавить команду "Отметить неск."
     */
    public void addMarkCMD() {
        isMark = true;
    }

    /**
     * Удалить команду "Отметить неск."
     */
    public void removeMarkCMD() {
        isMark = false;
    }

    /**
     * Отобразить список файлов/папок текущей директории
     */
    public void showCurrentDir() {
        fmList = new List(title, mode);
        fmList.addCommand(deleteCMD);
        fmList.addCommand(main.cancelCMD);
        if (system == FS) {
            switch (mode) {
                case List.IMPLICIT:
                    if (isMark) {
                        fmList.addCommand(markCMD);
                        fmList.removeCommand(markAllCMD);
                        fmList.removeCommand(addResCMD);
                    }
                    break;
                case List.MULTIPLE:
                    fmList.removeCommand(markCMD);
                    fmList.addCommand(markAllCMD);
                    fmList.addCommand(addResCMD);
                    break;
            }
            readListFiles();
            addToFilesList();
        } else {
            String[] lst = RecordStore.listRecordStores();
            int len = lst.length;
            for (int i = 0; i < len; i++) {
                fmList.append(lst[i], fileIMG);
            }
        }
        fmList.setCommandListener(this);
        main.display.setCurrent(fmList);
    }

    /**
     * Установить прослушку команд/путей.
     * В случае MobileBASIC.. Прослушку одной команды cancelCMD и путей :)
     */
    public void setPathListener(PathListener pl) {
        this.pl = pl;
    }

    private void setMode(int mode) {
        this.mode = mode;
    }

    private void readListFiles() {
        try {
            if (pathSeparator.equals(currentDirectoryName) || currentDirectoryName.length() == 0) {
                fileListEnumeration = FileSystemRegistry.listRoots();
            } else {
                String path = protocol + currentDirectoryName;
                FileConnection dirFileConnection = (FileConnection) Connector.open(path, Connector.READ);
                fileListEnumeration = dirFileConnection.list();
                if (mode != List.MULTIPLE) {
                    fmList.append(back, null);
                }
            }
        } catch (IOException ex) {
            // Вдруг путь кривой
            setCurrentDir(Settings.path = "");
            fmList.setTitle(pathSeparator);
            readListFiles();
        }
    }

    private void addToFilesList() {
        Vector vf = new Vector();
        do {
            if (!fileListEnumeration.hasMoreElements()) {
                break;
            }
            String path = (String) fileListEnumeration.nextElement();
            if (path.endsWith(pathSeparator)) {
                if (mode != List.MULTIPLE) {
                    fmList.append(path, folderIMG); // Сначала добавляем папки в список
                }
            } else {
                vf.addElement(path);
            }
        } while (true);
        int size = vf.size();
        for (int i = 0; i < size; i++) {
            fmList.append((String) vf.elementAt(i), fileIMG); // А теперь файлы
        }
    }

    private String cursorLabel() {
        int index = fmList.getSelectedIndex();
        return index != -1 ? fmList.getString(index) : null;
    }

    private void traverseDirectory(String path) {
        if (currentDirectoryName.equals(pathSeparator)) {
            currentDirectoryName = path;
        } else if (path.equals(back)) {
            int i = currentDirectoryName.lastIndexOf('/', currentDirectoryName.length() - 2);
            if (i != -1) {
                currentDirectoryName = currentDirectoryName.substring(0, i + 1);
            } else {
                currentDirectoryName = pathSeparator;
            }
        } else {
            currentDirectoryName += path;
        }
        title = currentDirectoryName;
        Settings.path = currentDirectoryName; // Запоминаем директорию
        showCurrentDir();
    }

    private boolean isFile(String path) {
        return !path.endsWith(pathSeparator) && !path.endsWith(back);
    }

    private boolean[] getSelectedFlags(int size) {
        boolean[] selected = new boolean[size];
        fmList.getSelectedFlags(selected);
        return selected;
    }

    public void commandAction(Command command, Displayable disp) {
        String label = cursorLabel();
        String path = protocol + currentDirectoryName + label;
        int size = fmList.size();
        if (disp == fmList) {
            if (command == List.SELECT_COMMAND) {
                if (system == FS) {
                    if (isFile(path)) {
                        pl.pathAction(path, false);
                    } else {
                        traverseDirectory(label);
                    }
                } else if (label != null) {
                    pl.pathAction(label, false);
                }
            } else if (command == deleteCMD) {
                delList = new List(deleteCMD.getLabel(), List.IMPLICIT);
                delList.append("Да", null);
                delList.append("Нет", null);
                delList.setCommandListener(this);
                main.display.setCurrent(delList);
            } else if (command == markCMD) {
                setMode(List.MULTIPLE);
                showCurrentDir();
            } else if (command == addResCMD) {
                boolean[] selected = getSelectedFlags(size);
                Vector v = new Vector(size);
                for (int i = 0; i < size; i++) {
                    if (selected[i]) {
                        v.addElement(protocol + currentDirectoryName + fmList.getString(i));
                    }
                }
                pl.pathAction(v, false);
            } else if (command == markAllCMD) {
                boolean[] selected = getSelectedFlags(size);
                for (int i = 0; i < size; i++) {
                    selected[i] = true;
                }
                fmList.setSelectedFlags(selected);
            } else {
                pl.pathAction(null, true);
            }
        } else {
            if (delList.getSelectedIndex() == 0) {
                if (system == RMS) {
                    path = label;
                }
                main.Delete(path);
                showCurrentDir();
            } else {
                main.display.setCurrent(fmList);
            }
        }
    }
}
