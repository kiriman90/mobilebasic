package com.view;

import mobilebasic.Main;
import java.io.DataInputStream;
import java.io.IOException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;

public class HelpScreen extends List implements CommandListener {

    private Main main;
    private Form helpForm;
    private DataInputStream dos;

    public HelpScreen() {
        super("Справка", List.IMPLICIT);
        append("MobileBASIC 1.9.1", null);
        append("Основные команды", null);
        append("Функции Даты и Времени", null);
        append("Функции Интерфейса", null);
        append("Игровые Функции", null);
        append("Графические команды", null);
        append("Команды Ввода-вывода", null);
        append("Математ. функции", null);
        append("Спрайтовые команды", null);
        append("Строковые функции", null);
        append("Работа со звуком", null);
        append("Дополн. функции", null);
        append("Номера Ошибок", null);
        main = Main.mdl;
        addCommand(main.cancelCMD);
        setCommandListener(this);
    }

    public void commandAction(Command command, Displayable disp) {
        if (disp == this) {
            if (command == List.SELECT_COMMAND) {
                int index = getSelectedIndex();
                helpForm = new Form(getString(index));
                helpForm.append(get(index));
                helpForm.addCommand(main.cancelCMD);
                helpForm.setCommandListener(this);
                main.display.setCurrent(helpForm);
                return;
            } else {
                dos = null;
                deleteAll();
                removeCommand(main.cancelCMD);
                main.display.setCurrent(main.menuList);
            }
        } else {
            helpForm = null;
            main.display.setCurrent(this);
        }
    }

    private String get(int index) {
        try {
            if (dos == null) {
                dos = new DataInputStream(HelpScreen.class.getResourceAsStream("/res/help.bin"));
            } else {
                dos.reset();
            }
            for (int i = 0; i < index; i++) {
                dos.skip(dos.readShort());
            }
            return dos.readUTF();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
