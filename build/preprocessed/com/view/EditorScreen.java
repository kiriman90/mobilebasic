package com.view;

import mobilebasic.Main;
import mobilebasic.BASIC;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.TextBox;

/**
 *
 * @author kiriman
 */
public class EditorScreen extends TextBox implements CommandListener {

    private Command inputCMD, listCMD;
    private int indexOf;
    private int size;
    public static int step = 10;
    private Main main;
    public boolean autoNumb;

    public EditorScreen(Main main) {
        super(main.nameProgram, "", main.canvas.widthInChars * main.canvas.heightInChars, 0);
        this.main = main;
        this.autoNumb = Settings.autoNumb;
        inputCMD = new Command("Ввод", 4, 1);
        listCMD = new Command("Список", 2, 2);
        addCommand(inputCMD);
        addCommand(listCMD);
        setCommandListener(this);
    }

    private void input() {
        int i = 0;
        indexOf = 0;
        size = 0;
        boolean isValid;
        String lineStr;
        //#if MB191
//#         BASIC.CONST_FLOAT_INDEX = 0;
        //#endif
        do {
            indexOf = getString().indexOf(10, i);
            if (indexOf == -1) {
                size = size();
            } else {
                size = indexOf;
            }
            if ((lineStr = getString().substring(i, size)).length() > 254) {
                main.Error("Ошибка: Превышена длина в строке " + lineStr.substring(0, lineStr.indexOf(32, 0)));
                return;
            }
            if (!(isValid = BASIC.parseLine(lineStr + "\n", false))) {
                return;
            }
            if (indexOf == -1) {
                insert("\n", size);
            }
            i = size + 1;
        } while (size + 1 < size());
        if (autoNumb && isValid && !lineStr.trim().equals(Integer.toString(main.numbLine))) {
            main.numbLine += step;
            insert(Integer.toString(main.numbLine) + " ", size + 1);
        }
        AlertType.CONFIRMATION.playSound(main.display);
    }

    public void commandAction(Command command, Displayable disp) {
        if (command == inputCMD) {
            input();
        } else if (command == listCMD) {
            main.display.setCurrent(main.canvas);
            main.listInCanvas();
        }
    }
}
