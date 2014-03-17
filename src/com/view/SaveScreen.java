package com.view;

import mobilebasic.Main;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import mobilebasic.BASIC;

/**
 *
 * @author kiriman
 */
public class SaveScreen extends AuxList implements CommandListener {

    private Main main;
    private Form saveForm;
    private TextField createInTF, nameFileTF;
    private ChoiceGroup typeFileCG;
    private int index;

    public SaveScreen() {
        super("Сохранить в:");
        this.main = Main.mdl;
        list.addCommand(main.cancelCMD);
        list.setCommandListener(this);
    }

    public void showSaveList() {
        main.display.setCurrent(list);
    }

    public void commandAction(Command command, Displayable disp) {
        if (disp == list) {
            if (command == List.SELECT_COMMAND) {
                saveForm = new Form("Сохранить Как...");
                if ((index = list.getSelectedIndex()) == 0) {
                    saveForm.append(createInTF = new TextField("Сохранить в:", "/" + Settings.path, 120, 0));
                }
                nameFileTF = new TextField("Имя Файла:", "", 12, 4);
                saveForm.append(nameFileTF);
                typeFileCG = new ChoiceGroup("Тип Файла:", 1);
                typeFileCG.append("BASIC Формат (*.bas)", null);
                typeFileCG.append("Текстовый Формат (*.lis)", null);
                saveForm.append(typeFileCG);
                saveForm.addCommand(main.cancelCMD);
                saveForm.addCommand(new Command("Сохранить", 4, 3));
                saveForm.setCommandListener(this);
                main.display.setCurrent(saveForm);
            } else if (command == main.cancelCMD) {
                main.display.setCurrent(main.menuList);
            }
        } else if (command == main.cancelCMD) {
            main.display.setCurrent(list);
        } else {
            main.nameProgram = nameFileTF.getString();
            String path = "";
            if (index == 0) {
                path = "file://" + (path = createInTF.getString());
            }
            path += main.nameProgram;
            if (typeFileCG.getSelectedIndex() == 0) {
                BASIC.parseLine("SAVE \"" + path + ".bas\"\n", false);
            } else {
                BASIC.parseLine("LIST \"" + path + ".lis\"\n", false);
            }
            saveForm = null;
            nameFileTF = null;
            typeFileCG = null;
            main.editor.setTitle(main.nameProgram);
            main.display.setCurrent(main.canvas);
        }
    }
}
