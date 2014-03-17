package com.view;

import mobilebasic.Main;
import mobilebasic.BASIC;
import java.util.Vector;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 *
 * @author kiriman
 */
public class BuildScreen extends Form implements CommandListener, PathListener {

    private TextField nameMidletTB, versionMidletTB, vendorMidletTB, iconMidletTB, createInTB;
    private Command buildCMD, addResCMD;
    private Vector fileVector;
    public static String manifest;
    //#if MB191
//#     private ChoiceGroup fullScreenBuildCG;
    //#endif
    private Main main;

    public BuildScreen() {
        super("Создать Мидлет");
        main = Main.mdl;
        fileVector = new Vector();
        createInTB = new TextField("Создать в:", "/" + Settings.path, 120, 0);
        append(createInTB);
        nameMidletTB = new TextField("Имя Мидлета", main.nameProgram, 12, 0);
        versionMidletTB = new TextField("Версия Мидлета", "1.0.0", 5, 0);
        vendorMidletTB = new TextField("Поставщик Мидлета", "Vendor", 20, 0);
        iconMidletTB = new TextField("Значок Мидлета", "", 12, 0);
        append(nameMidletTB);
        append(versionMidletTB);
        append(vendorMidletTB);
        append(iconMidletTB);
        //#if MB191
//#         fullScreenBuildCG = new ChoiceGroup("Полный экран", 2);
//#         fullScreenBuildCG.append("Вкл./Выкл.", null);
//#         fullScreenBuildCG.setSelectedIndex(0, true);
//#         append(fullScreenBuildCG);
        //#endif
        append(new StringItem("Ресурсы:\n", ""));
        buildCMD = new Command("Собрать", 4, 1);
        addResCMD = new Command("Добавить Ресурс", 4, 2);
        addCommand(buildCMD);
        addCommand(addResCMD);
        addCommand(main.cancelCMD);
        setCommandListener(this);
    }
    //#if MB191
//# 
//#     private boolean isFullScreen() {
//#         boolean[] result = new boolean[1];
//#         if (fullScreenBuildCG != null) {
//#             fullScreenBuildCG.getSelectedFlags(result);
//#         } else {
//#             result[0] = false;
//#         }
//#         return result[0];
//#     }
    //#endif

    public void commandAction(Command command, Displayable disp) {
        if (command == addResCMD) {
            FileManager fm = new FileManager(addResCMD.getLabel(), List.IMPLICIT);
            fm.addMarkCMD();
            fm.setSystem(FileManager.FS);
            fm.setPathListener(this);
            fm.setCurrentDir(Settings.path);
            fm.showCurrentDir();
        } else if (command == buildCMD) {
            // MANIFEST.MF
            StringBuffer sb = new StringBuffer();
            sb.append("Manifest-Version: 1.0\nMIDlet-1: ");
            sb.append(nameMidletTB.getString() + ",");
            String pathIcon;
            if (!(pathIcon = iconMidletTB.getString()).equals("")) {
                sb.append("/" + pathIcon);
            }
            sb.append(",Main\nMIDlet-Vendor: " + vendorMidletTB.getString() + "\nMIDlet-Version: ");
            sb.append(versionMidletTB.getString() + "\nMIDlet-Name: ");
            sb.append(nameMidletTB.getString() + "\nMicroEdition-Configuration: CLDC-1.1\nMicroEdition-Profile: MIDP-2.0\n");
            //#if MB191
//#             sb.append("FullScreenMode: " + isFullScreen() + "\n");
            //#endif
            manifest = sb.toString();
            main.nameProgram = nameMidletTB.getString();
            BASIC.parseLine("SAVE \"temp\"\n", false);
            main.editor = null;
            main.menuList = null;
            main.var_11e5 = true;
            BASIC.parseLine("LIST\n", false);
            main.var_11e5 = false;
            // Собираем мидлет
            new BuildApp(createInTB.getString(), fileVector);
        } else if (command == main.cancelCMD) {
            main.display.setCurrent(main.menuList);
            return;
        }
    }

    public void pathAction(Object object, boolean isCancel) {
        String fileName;
        if (object instanceof String) {
            fileName = (String) object;
            fileVector.addElement(fileName);
            appendFileNameToForm(fileName);
        } else if (object instanceof Vector) {
            Vector v = (Vector) object;
            int size = v.size();
            for (int i = 0; i < size; i++) {
                fileName = (String) v.elementAt(i);
                fileVector.addElement(fileName);
                appendFileNameToForm(fileName);
            }
        }
        main.display.setCurrent(this);
    }

    private void appendFileNameToForm(String fileName) {
        append(fileName.substring(fileName.lastIndexOf('/') + 1, fileName.length()) + "\n");
    }
}
