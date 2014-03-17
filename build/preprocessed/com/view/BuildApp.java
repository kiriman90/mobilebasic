package com.view;

import mobilebasic.Main;
import mobilebasic.BASIC;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import com.utils.zipme.ZipEntry;
import com.utils.zipme.ZipOutputStream;

public class BuildApp implements Runnable {

    private ZipOutputStream jarFile;
    private boolean var_4d;
    private String nameJar;
    private String ext1;
    private Gauge waitGAUGE = new Gauge("Подождите...", false, 100, 0);
    private Form buildForm = new Form("Сборка");
    public boolean z = false;
    private boolean var_23d = false;
    private boolean var_29c = false;
    private byte[] autorunBas;
    private String nameJad;
    private Vector fileVector;
    private Main main;

    public BuildApp(String path, Vector fileVector) {
        main = Main.mdl;
        buildForm.append(waitGAUGE);
        main.display.setCurrent(buildForm);
        /*
         * Если Nokia, то пишем файлы без расширения.
         * Т.к. стоит ограничение на создание файлов *.jar
         */
        String str1, str2;
        if (System.getProperty("microedition.platform").indexOf("Nokia") != -1) {
            str1 = "_jar";
            str2 = "_jad";
        } else {
            str1 = ".jar";
            str2 = ".jad";
        }
        nameJar = path + main.nameProgram + str1;
        nameJad = path + main.nameProgram + str2;
        ext1 = path;
        this.fileVector = fileVector;
        this.fileVector.addElement("/lib/Autorun.bas");
        this.fileVector.addElement("/lib/META-INF/MANIFEST.MF");
        this.fileVector.addElement("/lib/Main");
        this.fileVector.addElement("/lib/b");
        this.fileVector.addElement("/lib/f");
        // this.fileVector.addElement("/lib/i");
        this.fileVector.addElement("/lib/d");
        this.fileVector.addElement("/lib/a");
        this.fileVector.addElement("/lib/e");
        this.fileVector.addElement("/lib/c");
        this.fileVector.addElement("/lib/g");
        // this.fileVector.addElement("/lib/h");
        // this.fileVector.addElement("/lib/j");
        // this.fileVector.addElement("/lib/k");
        // this.fileVector.addElement("/lib/l");
        //this.fileVector.addElement("/lib/m");
        new Thread(this).start();
    }

    private void addFileToZip(String path) throws IOException {
        InputStream is = null;
        FileConnection fc = null;
        if (z) {
            is = (BuildApp.class.getResourceAsStream(path));
            if ((path = path.substring("/lib/".length())).startsWith("Autorun")) {
                var_29c = true;
            } else {
                var_29c = false;
                if (path.startsWith("META-INF")) {
                    var_23d = true;
                } else {
                    var_23d = false;
                    path = path + ".class";
                }
            }
        } else {
            fc = (FileConnection) Connector.open(path, Connector.READ);
            is = fc.openInputStream();
            path = path.substring(path.lastIndexOf(47) + 1);
        }

        StringBuffer sb = new StringBuffer(path.length());
        int i;
        for (i = 0; i < path.length(); ++i) {
            char ch = path.charAt(i);
            sb.append(com.utils.StringEncoder.decodeCharCP1251((byte) ch));
        }
        jarFile.putNextEntry(new ZipEntry(sb.toString()));
        byte[] var9 = "".getBytes();
        if (var_29c) {
            i = autorunBas.length;
        } else if (var_23d) {
            i = (var9 = BuildScreen.manifest.getBytes()).length;
        } else {
            if (z) {
                i = is.available();
            } else {
                i = (int) fc.fileSize();
            }
        }

        while (i > 0) {
            int length = '\uc350';
            if ('\uc350' > i) {
                length = i;
            }

            byte[] b = new byte[length];
            if (var_29c) {
                System.arraycopy(this.autorunBas, 0, b, 0, length);
            } else if (var_23d) {
                System.arraycopy(var9, 0, b, 0, length);
            } else {
                is.read(b, 0, length);
            }
            jarFile.write(b, 0, length);
            i -= length;
        }

        if (var_29c) {
            autorunBas = null;
        }
        is.close();
    }

    public void run() {
        String protocol = "file://";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos;
            BASIC.SaveTo(dos = new DataOutputStream(baos));
            autorunBas = baos.toByteArray();
            dos.close();
            baos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        BASIC.New();
        System.gc();
        var_4d = false;
        FileConnection file = null;
        try {
            file = (FileConnection) Connector.open(protocol + nameJar, Connector.READ_WRITE);
            if (file.exists()) {
                file.delete();
            }
            file.create();
            jarFile = new ZipOutputStream(file.openDataOutputStream());
            jarFile.setMethod(8);
            jarFile.setLevel(9);
            int size = this.fileVector.size();
            for (int i = 0; i < size; ++i) {
                String element = (String) this.fileVector.elementAt(i);
                if ((element).startsWith("/lib/")) {
                    z = true;
                } else {
                    z = false;
                }
                addFileToZip(element);
                jarFile.flush();
                waitGAUGE.setValue(waitGAUGE.getValue() + 100 / size);
                if (var_4d) {
                    break;
                }
            }
            jarFile.finish();
            jarFile.flush();
            jarFile.close();
        } catch (Throwable ex) {
            ex.printStackTrace();
            String err = ex.getMessage();
            main.bldAlert("Ошибка", "Ошибка упаковки " + err == null ? ex.toString() : err, AlertType.ERROR);
            buildForm = null;
            return;
        }

        try {
            z = false;
            file = (FileConnection) Connector.open(protocol + nameJar, Connector.READ);
            // Узнаем размер для записи в JAD
            String size = Integer.toString((int) file.fileSize());
            file.close();
            FileConnection fc = (FileConnection) Connector.open(protocol + nameJad);
            if (fc.exists()) {
                fc.delete();
            }
            fc.create();
            byte[] manifest = (BuildScreen.manifest + "MIDlet-Jar-Size: " + size + "\nMIDlet-Jar-URL: " + nameJar.substring(ext1.length()) + "\n").getBytes();
            fc.openDataOutputStream().write(manifest);
            fc.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        main.bldAlert("Мидлет Создан", "JAR файл:\n" + nameJar + "\nJAD файл:\n" + nameJad, AlertType.CONFIRMATION);
        buildForm = null;
    }
}
