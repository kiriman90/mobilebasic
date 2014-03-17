package com.view;

import mobilebasic.Main;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 *
 * @author kiriman
 */
public class Settings extends Form implements CommandListener {

    public static int fontSize; // Размер шрифта
    public static String m_bString;
    public static String m_aString;
    public static boolean m_fullScreenZ; // Полный экран
    public static String path; // Последний путь сборки Jar
    public static boolean m_aZ;
    public static boolean autoNumb = true; // Автонумерация строк
    /*************************/
    private ChoiceGroup fontSizeCG;
    private ChoiceGroup pauseTextCG;
    private ChoiceGroup automaticNumberingLinesCG;
    private TextField numberingWithTF;
    private TextField stepNumberingTF;
    private ChoiceGroup fullScreenModeCG;
    public static int var_1430 = 10;
    private Main main;

    public Settings() {
        super("Настройки");
        automaticNumberingLinesCG = new ChoiceGroup("Автонумерация строк", 2);
        automaticNumberingLinesCG.append("Вкл./Выкл.", (Image) null);
        automaticNumberingLinesCG.setSelectedIndex(0, autoNumb);
        append(automaticNumberingLinesCG);
        numberingWithTF = new TextField("Начать нумерацию с:", "10", 5, 2);
        stepNumberingTF = new TextField("Шаг нумерации:", "10", 5, 2);
        append(numberingWithTF);
        append(stepNumberingTF);
        fontSizeCG = new ChoiceGroup("Размер Шрифта", 1);
        fontSizeCG.append("Автоопределение", (Image) null);
        fontSizeCG.append("Наименьший", (Image) null);
        fontSizeCG.append("Средний", (Image) null);
        fontSizeCG.append("Наибольший", (Image) null);
        append(fontSizeCG);
        pauseTextCG = new ChoiceGroup("Пауза Текста", 1);
        pauseTextCG.append("Выключить", (Image) null);
        pauseTextCG.append("Включить", (Image) null);
        append(pauseTextCG);
        fullScreenModeCG = new ChoiceGroup("Полноэкранный Режим", 2);
        fullScreenModeCG.append("Вкл./Выкл.", (Image) null);
        append(fullScreenModeCG);
        fullScreenModeCG.setSelectedIndex(0, m_fullScreenZ);
        addCommand(Main.mdl.cancelCMD);
        setCommandListener(this);
        main = Main.mdl;
    }

    /**
     * Чтение настроек
     */
    public static void readConfig() {
        RecordStore record = null;
        m_bString = "";
        fontSize = 0;
        m_aString = "";
        m_fullScreenZ = false;
        path = new String();
        m_aZ = false;

        try {
            byte[] b = (record = RecordStore.openRecordStore(".CONFIG", false)).getRecord(1);
            ByteArrayInputStream bais = new ByteArrayInputStream(b);
            DataInputStream dis = new DataInputStream(bais);
            m_bString = dis.readUTF();
            fontSize = dis.readInt();
            m_aString = dis.readUTF();
            m_fullScreenZ = dis.readBoolean();
            autoNumb = dis.readBoolean();
            path = dis.readUTF();
            m_aZ = dis.readBoolean();
        } catch (RecordStoreException e) {
            if (record != null) {
                try {
                    record.closeRecordStore();
                    return;
                } catch (Exception ex) {
                }
            }

            return;
        } catch (IOException ex) {
            if (record != null) {
                try {
                    record.closeRecordStore();
                    return;
                } catch (Exception e) {
                }
            }

            return;
        }

        if (record != null) {
            try {
                record.closeRecordStore();
                return;
            } catch (Exception ex) {
            }
        }

    }

    /**
     * Сохранение настроек
     */
    public static void writeConfig() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeUTF(m_bString);
        dos.writeInt(fontSize);
        dos.writeUTF(m_aString);
        dos.writeBoolean(m_fullScreenZ);
        dos.writeBoolean(autoNumb);
        dos.writeUTF(path);
        dos.writeBoolean(m_aZ);
        byte[] b;
        if ((b = baos.toByteArray()) != null) {
            try {
                RecordStore.deleteRecordStore(".CONFIG");
            } catch (RecordStoreException ex) {
                ex.printStackTrace();
            }
            RecordStore record = null;

            try {
                (record = RecordStore.openRecordStore(".CONFIG", true)).addRecord(b, 0, b.length);
            } catch (RecordStoreException var6) {
                throw new IOException();
            }

            if (record != null) {
                try {
                    record.closeRecordStore();
                } catch (Exception var5) {
                    throw new IOException();
                }
            }
        }
    }

    public void commandAction(Command command, Displayable disp) {
        boolean[] z = new boolean[1];
        automaticNumberingLinesCG.getSelectedFlags(z);
        if (autoNumb = z[0]) {
            autoNumb = true;
            var_1430 = main.numbLine = Integer.parseInt(numberingWithTF.getString());
            EditorScreen.step = Integer.parseInt(stepNumberingTF.getString());

            if (main.editor != null) {
                main.editor.insert(Integer.toString(main.numbLine) + " ", main.editor.size());
            }
        }
        fontSize = fontSizeCG.getSelectedIndex();
        main.canvas.SetFontSize(fontSize);
        m_aZ = pauseTextCG.getSelectedIndex() == 1;
        boolean[] var14 = new boolean[1];
        fullScreenModeCG.getSelectedFlags(var14);
        m_fullScreenZ = var14[0];
        automaticNumberingLinesCG = null;
        numberingWithTF = null;
        stepNumberingTF = null;
        fontSizeCG = null;
        pauseTextCG = null;
        main.display.setCurrent(main.menuList);
    }
}
