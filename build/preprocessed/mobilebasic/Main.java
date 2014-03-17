package mobilebasic;

import com.view.SaveScreen;
import com.view.Settings;
import com.view.LoadScreen;
import com.view.HelpScreen;
import com.view.EditorScreen;
import com.view.BuildScreen;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.DateField;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Ticker;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

public class Main extends MIDlet implements BasicSupport, Runnable, CommandListener {

    private List exitList;
    private boolean AutorunFlag;
    private Form userForm;
    private int userExitStatus;
    private String waitObject;
    private RandomAccessFile[] randomAccessFile;
    private HttpConnection[] httpConn;
    private FileConnection[] fc;
    private SocketConnection[] socketConn;
    private ByteArrayOutputStream[] baos;
    private DataInput[] dataInput;
    private DataOutput[] dataOutput;
    private boolean var_c61 = false;
    private boolean var_c90 = false;
    private InputStream[] inputStream;
    private static final int MAXFILES = 10;
    private List var_13d9;
    private Hashtable commandHashtable = new Hashtable();
    private TextBox inputTB;
    private Command runCMD, saveCMD, menuCMD, editCMD,
            var_1463, userProceedCommand, userCancelCommand, stopCMD;
    /********************/
    public static Main mdl;
    public static Graphics gc;
    public Thread thread = null;
    public BasicCanvas canvas;
    public Command editorCMD, cancelCMD;
    public EditorScreen editor;
    public boolean var_11e5 = false;
    public Display display;
    public BASIC m_ac;
    public Calendar calendar;
    public boolean listFlag = false;
    public int var_1064 = 0;
    public int offsetLine = 0;
    public int lineInScr = 0;
    public List menuList;
    public String nameProgram = "NewFile";
    public int numbLine = 10;

    private void menuScreen() {
        menuList = new List("MobileBASIC 1.9.1", List.IMPLICIT);
        menuList.append("Создать", null);
        menuList.append("Открыть", null);
        menuList.append("Собрать", null);
        menuList.append("Настройки", null);
        menuList.append("Справка", null);
        menuList.append("О программе", null);
        menuList.append("Выход", null);
        menuList.setCommandListener(this);
    }

    public void bldAlert(String var1, String var2, AlertType var3) {
        menuScreen();
        Alert alert;
        (alert = new Alert(var1, var2, (Image) null, var3)).setTimeout(-2);
        display.setCurrent(alert, menuList);
        System.gc();
        BASIC.parseLine("LOAD \"temp\"\n", false);
        BASIC.parseLine("DELETE \"temp\"\n", false);
        menuList.addCommand(editorCMD);
        editor = new EditorScreen(this);
        listInCanvas();
    }

    public void listInCanvas() {
        if (offsetLine < 0) {
            offsetLine = 0;
        }
        if (offsetLine > var_1064) {
            offsetLine = var_1064;
        }
        var_1064 = 0;
        editor.setString("");
        listFlag = true;
        CLS();
        BASIC.parseLine("LIST\n", false);
    }

    public void startApp() {
        canvas.start();
    }

    public void pauseApp() {
        if (display.getCurrent() == canvas) {
            canvas.sub_2b5();
            thread = null;
        }
    }

    public void destroyApp(boolean var1) {
        try {
            Settings.writeConfig(); // Сохраняем настройки
        } catch (IOException ex) {
        }
        if (display.getCurrent() == canvas) {
            canvas.sub_2b5();
            thread = null;
        }
    }

    private void InitBASIC() {
        m_ac = new BASIC(this,
                //#if MB191
//#                 65535);
//#else
        16384);
//#endif
    }

    public Main() {
        mdl = this;
        Settings.readConfig(); // Читаем настройки
        Runtime.getRuntime();
        calendar = Calendar.getInstance();
        userForm = null;
        userExitStatus = 0;
        userCancelCommand = null;
        userProceedCommand = null;
        waitObject = "X";
        display = Display.getDisplay(this);
        canvas = new BasicCanvas(this);
        canvas.SetFontSize(Settings.fontSize);
        menuCMD = new Command("Меню", 2, 2);
        editCMD = new Command("Редактировать", 4, 1);
        runCMD = new Command("Пуск", 4, 2);
        saveCMD = new Command("Сохранить", 4, 3);
        cancelCMD = new Command("Назад", 2, 2);
        stopCMD = new Command("Стоп", 8, 3);
        editorCMD = new Command("Редактор", 2, 2);
        canvas.addCommand(menuCMD);
        canvas.addCommand(editCMD);
        canvas.addCommand(runCMD);
        canvas.addCommand(saveCMD);
        canvas.setCommandListener(this);
        gc = canvas.graphicsGc;
        menuScreen();
        randomAccessFile = new RandomAccessFile[10];
        httpConn = new HttpConnection[10];
        inputStream = new InputStream[10];
        fc = new FileConnection[10];
        socketConn = new SocketConnection[10];
        baos = new ByteArrayOutputStream[10];
        dataInput = new DataInput[10];
        dataOutput = new DataOutput[10];

        for (int iocb = 0; iocb < 10; ++iocb) {
            inputStream[iocb] = null;
            randomAccessFile[iocb] = null;
            httpConn[iocb] = null;
            baos[iocb] = null;
            dataInput[iocb] = null;
            dataOutput[iocb] = null;
        }

        InputStream is = null;
        AutorunFlag = false;
        if ((is = getClass().getResourceAsStream("/Autorun.bas")) != null) {
            try {
                InitBASIC();
                BASIC.LoadFrom(new DataInputStream(is));
                AutorunFlag = true;
            } catch (Throwable var3) {
            }
        } else {
            if ((is = getClass().getResourceAsStream("/Autorun.lis")) != null) {
                InitBASIC();
                if (!BASIC.Enter(new DataInputStream(is))) {
                    AutorunFlag = true;
                }
            }
        }

        InitBASIC();
        if (AutorunFlag) {
            display.setCurrent(canvas);
        } else {
            display.setCurrent(menuList);
        }
    }

    public void commandAction(Command command, Displayable disp) {
        if (inputTB != null && disp == inputTB) {
            synchronized (waitObject) {
                waitObject.notify();
            }
        } else if (userForm != null && disp == userForm) {
            userExitStatus = command == userProceedCommand ? 1 : -1;
            synchronized (waitObject) {
                waitObject.notify();
            }
        } else if (var_13d9 != null && disp == var_13d9) {
            if (command == List.SELECT_COMMAND) {
                userExitStatus = var_13d9.getSelectedIndex();
            } else {
                userExitStatus = -1;
            }

            synchronized (waitObject) {
                waitObject.notify();
            }
        } else { // Дальнейшие условия рантайму не требуются, смело удалять
            if (disp == canvas) {
                if (command == stopCMD) {
                    BASIC.StopProgram();
                    thread = null;
                    var_c90 = true;
                } else if (command == runCMD) {
                    menuList = null;
                    editor = null;
                    listFlag = false;
                    BASIC.parseLine("CLS\n", false);
                    System.gc();
                    thread = new Thread(this);
                    thread.start();
                } else if (command == editCMD) {
                    listFlag = false;
                    display.setCurrent(editor);
                    if (Settings.autoNumb) {
                        editor.insert(Integer.toString(numbLine) + " ", editor.size());
                    }

                } else if (command == saveCMD) {
                    SaveScreen save = new SaveScreen();
                    save.showSaveList();
                } else if (command == menuCMD && menuList != null) {
                    display.setCurrent(menuList);
                } else {
                    var_1463 = command;
                }
            } else if (disp == menuList) {
                if (command == List.SELECT_COMMAND) {
                    switch (menuList.getSelectedIndex()) {
                        case 0: // Создать
                            CLS();
                            BASIC.New();
                            System.gc();
                            nameProgram = "NewFile";
                            offsetLine = 0;
                            editor = new EditorScreen(this);
                            listFlag = false;
                            display.setCurrent(editor);
                            if (Settings.autoNumb) {
                                editor.insert(Integer.toString(Settings.var_1430) + " ", 0);
                            }

                            menuList.addCommand(editorCMD);
                            break;
                        case 1: // Открыть
                            LoadScreen loadscr = new LoadScreen();
                            loadscr.showOpenList();
                            break;
                        case 2: // Собрать
                            display.setCurrent(new BuildScreen());
                            break;
                        case 3: // Настройки
                            display.setCurrent(new Settings());
                            break;
                        case 4: // Справка
                            display.setCurrent(new HelpScreen());
                            break;
                        case 5: // О программе
                            Alert about = new Alert("О программе", "Авторское право (c) 2003, David Firth\nРедакция (c) 2008, Mumey\nhttp://www.mobilebasic.com/\nhttp://www.firthsoftware.co.uk/ \nРедакция (c) 2013 kiriman & dzanis", (Image) null, AlertType.INFO);
                            about.setTimeout(-2);
                            display.setCurrent(about, menuList);
                            return;
                        case 6: // Выход
                            exitList = new List("Выйти из BASIC\'a?", List.IMPLICIT);
                            exitList.append("Нет", (Image) null);
                            exitList.append("Да", (Image) null);
                            exitList.addCommand(cancelCMD);
                            exitList.setCommandListener(this);
                            exitList.setSelectedIndex(0, true);
                            display.setCurrent(exitList);
                    }
                } else if (command == editorCMD) {
                    display.setCurrent(canvas);
                }
            } else { // Выйти?
                if (command == List.SELECT_COMMAND) {
                    if (exitList.getSelectedIndex() != 1) {
                        if (var_c61) {
                            var_c61 = false;
                            return;
                        }

                        display.setCurrent(menuList);
                        return;
                    }
                    destroyApp(true);
                    notifyDestroyed();
                }

                if (command == cancelCMD) {
                    display.setCurrent(menuList);
                    return;
                }
            }

        }
    }

    private int UserForm(String title, String proceedText, String cancelText, Item item) {
        userForm = new Form(title);
        userForm.append(item);
        if (proceedText != null) {
            userProceedCommand = new Command(proceedText, Command.OK, 1);
            userForm.addCommand(userProceedCommand);
        }

        if (cancelText != null) {
            userCancelCommand = new Command(cancelText, Command.BACK, 1);
            userForm.addCommand(userCancelCommand);
        }

        userForm.setCommandListener(this);
        display.setCurrent(userForm);
        synchronized (waitObject) {
            try {
                waitObject.wait();
            } catch (Exception ex) {
            }
        }

        canvas.Focus(); // Tell canvas its getting the focus
        display.setCurrent(canvas);
        return userExitStatus;
    }

    public void run() {
        //System.out.println("Basic Thread: starting");
        if (AutorunFlag) {
            BASIC.parseLine("RUN", false);
            Bye();
            BASIC.New();
        } else {
            canvas.removeCommand(menuCMD);
            canvas.removeCommand(editCMD);
            canvas.removeCommand(runCMD);
            canvas.removeCommand(saveCMD);
            canvas.addCommand(stopCMD);
            var_c90 = false;
            BASIC.parseLine("RUN", false);
        }

        canvas.removeCommand(stopCMD);

        try {
            Thread.sleep(3000L);
        } catch (InterruptedException var2) {
        }

        Alert alert = new Alert("MobileBASIC", "Программа\nЗавершена", null, AlertType.INFO);
        alert.setTimeout(-2);
        display.setCurrent(alert, canvas);
        canvas.Init();
        thread = null;
        System.gc();
        canvas.SetFontSize(Settings.fontSize);
        canvas.addCommand(menuCMD);
        canvas.addCommand(editCMD);
        canvas.addCommand(runCMD);
        canvas.addCommand(saveCMD);
        menuScreen();
        menuList.addCommand(editorCMD);
        editor = new EditorScreen(this);
        listInCanvas();
    }

    /*
     * BASIC Support Routines
     */
    public void Message(String msg) {
        PrintString(msg + "\n");
    }

    public void Error(String err) {
        PrintString(err + "\n");
    }

    public void OpenFile(int iocb, String filename, boolean readOnlyFlag) {
        if (iocb >= 0 && iocb < MAXFILES) {
            if (randomAccessFile[iocb] == null && httpConn[iocb] == null && fc[iocb] == null && socketConn[iocb] == null && inputStream[iocb] == null) {
                Class clazz;
                if (filename.startsWith("file:")) {
                    try {
                        fc[iocb] = (FileConnection) Connector.open(filename);
                        if (readOnlyFlag) {
                            if (!fc[iocb].exists()) {
                                throw new BasicError(4130, "No file: " + filename);
                            }
                        } else {
                            if (fc[iocb].exists()) {
                                fc[iocb].delete();
                                fc[iocb].close();
                                fc[iocb] = (FileConnection) Connector.open(filename);
                            }

                            fc[iocb].create();
                        }

                        dataInput[iocb] = fc[iocb].openDataInputStream();
                        dataOutput[iocb] = fc[iocb].openDataOutputStream();
                    } catch (Exception ex) {
                        clazz = ex.getClass();
                        throw new BasicError(4130, clazz.getName());
                    }
                } else if (filename.startsWith("socket:")) {
                    try {
                        socketConn[iocb] = (SocketConnection) Connector.open(filename);
                        dataOutput[iocb] = socketConn[iocb].openDataOutputStream();
                        dataInput[iocb] = socketConn[iocb].openDataInputStream();
                    } catch (Exception ex) {
                        clazz = ex.getClass();
                        throw new BasicError(4130, clazz.getName());
                    }
                } else if (filename.startsWith("http:")) {
                    try {
                        httpConn[iocb] = (HttpConnection) Connector.open(filename, 3);
                        if (readOnlyFlag) {
                            httpConn[iocb].setRequestMethod("GET");
                        } else {
                            httpConn[iocb].setRequestMethod("POST");
                            httpConn[iocb].setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        }

                        //httpConn[iocb].setRequestProperty("User-Agent", "Profile/MIDP-2.0 Configuration/CLCD-1.1 (Mobile BASIC MIDlet 1.9.1 by kiriman & dzanis)");
                        //httpConn[iocb].setRequestProperty("Content-Language", "en-US");
                        //httpConn[iocb].setRequestProperty("Accept", "text/plain");
                        //httpConn[iocb].setRequestProperty("Content-Type", "text/plain");
                        // httpConn[iocb].setRequestProperty("Connection", "close");
                        baos[iocb] = new ByteArrayOutputStream();
                        dataOutput[iocb] = new DataOutputStream(baos[iocb]);
                        dataInput[iocb] = null;
                    } catch (Exception ex) {
                        clazz = ex.getClass();
                        throw new BasicError(4131, clazz.getName());
                    }
                } else if (filename.startsWith("/")) {
                    try {
                        inputStream[iocb] = getClass().getResourceAsStream(filename);
                        dataInput[iocb] = new DataInputStream(inputStream[iocb]);
                        dataOutput[iocb] = null;
                    } catch (Exception ex) {
                        throw new BasicError(4133, ex.getClass().getName());
                    }
                } else {
                    try {
                        randomAccessFile[iocb] = new RandomAccessFile(filename, readOnlyFlag);
                        dataInput[iocb] = randomAccessFile[iocb];
                        dataOutput[iocb] = randomAccessFile[iocb];
                    } catch (Exception ex) {
                        randomAccessFile[iocb] = null;
                        dataInput[iocb] = null;
                        dataOutput[iocb] = null;
                        throw new BasicError(4132, ex.getClass().getName());
                    }
                }
            } else {
                throw new BasicError(4098, "Channel " + iocb + " already in use");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    private void sub_lolclose(int iocb) {
        if (iocb >= 0 && iocb < MAXFILES && httpConn[iocb] != null && baos[iocb] != null) {
            try {
                httpConn[iocb].setRequestProperty("Content-Length", String.valueOf(baos[iocb].size()));
                OutputStream os = httpConn[iocb].openOutputStream();
                byte[] b = baos[iocb].toByteArray();
                os.write(b);
                os.close();
                InputStream is = httpConn[iocb].openInputStream();
                dataInput[iocb] = new DataInputStream(is);
                dataOutput[iocb] = null;
            } catch (IOException ex) {
                Class clazz = ex.getClass();
                throw new BasicError(4133, clazz.getName());
            }

            baos[iocb] = null;
        }

    }

    public void CloseFile(int iocb) {
        if (iocb >= 0 && iocb < MAXFILES) {
            if (httpConn[iocb] != null) {
                sub_lolclose(iocb);
            }

            try {
                if (randomAccessFile[iocb] != null) {
                    randomAccessFile[iocb].close();
                }

                if (fc[iocb] != null) {
                    ((DataOutputStream) dataOutput[iocb]).close();
                    ((DataInputStream) dataInput[iocb]).close();
                    fc[iocb].close();
                }

                if (socketConn[iocb] != null) {
                    ((DataOutputStream) dataOutput[iocb]).close();
                    ((DataInputStream) dataInput[iocb]).close();
                    socketConn[iocb].close();
                }

                if (inputStream[iocb] != null) {
                    ((DataInputStream) dataInput[iocb]).close();
                }
            } catch (Exception ex) {
                inputStream[iocb] = null;
                httpConn[iocb] = null;
                fc[iocb] = null;
                socketConn[iocb] = null;
                randomAccessFile[iocb] = null;
                dataInput[iocb] = null;
                dataOutput[iocb] = null;
                throw new BasicError(4134, ex.getMessage());
            }

            inputStream[iocb] = null;
            httpConn[iocb] = null;
            fc[iocb] = null;
            socketConn[iocb] = null;
            randomAccessFile[iocb] = null;
            dataInput[iocb] = null;
            dataOutput[iocb] = null;
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public void CloseAllFiles() {
        for (int index = 0; index < MAXFILES; ++index) {
            CloseFile(index);
        }
    }

    public int Note(int iocb) {
        long offset = 0L;
        if (iocb >= 0 && iocb < MAXFILES) {
            if (httpConn[iocb] == null && fc[iocb] == null && socketConn[iocb] == null && inputStream[iocb] == null) {
                offset = (long) randomAccessFile[iocb].getFilePointer();
                return (int) offset;
            } else {
                throw new BasicError(4101, "Not Random Access File");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public void Point(int iocb, int offset) {
        if (iocb >= 0 && iocb < MAXFILES) {
            if (httpConn[iocb] == null && fc[iocb] == null && socketConn[iocb] == null && inputStream[iocb] == null) {
                randomAccessFile[iocb].seek(offset);
            } else {
                throw new BasicError(4101, "Not Random Access File");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public DataInput GetDataInputChannel(int iocb) {
        if (iocb >= 0 && iocb < MAXFILES) {
            if (httpConn[iocb] != null && baos[iocb] != null) {
                sub_lolclose(iocb);
            }

            return dataInput[iocb];
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public DataOutput GetDataOutputChannel(int iocb) {
        if (iocb >= 0 && iocb < MAXFILES) {
            return dataOutput[iocb];
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public void PutByte(int iocb, int byteValue) {
        if (iocb >= 0 && iocb < MAXFILES) {
            try {
                dataOutput[iocb].writeByte(byteValue);
            } catch (IOException ex) {
                Class clazz = ex.getClass();
                throw new BasicError(4101, clazz.getName());
            } catch (NullPointerException ex) {
                throw new BasicError(4101, "Channel not writable");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public void PutInt(int iocb, int intValue) {
        if (iocb >= 0 && iocb < MAXFILES) {
            try {
                dataOutput[iocb].writeInt(intValue);
            } catch (IOException ex) {
                Class clazz = ex.getClass();
                throw new BasicError(4101, clazz.getName());
            } catch (NullPointerException ex) {
                throw new BasicError(4101, "Channel not writable");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public void PutString(int iocb, String s) {
        if (iocb >= 0 && iocb < MAXFILES) {
            try {
                dataOutput[iocb].writeUTF(s);
            } catch (IOException ex) {
                Class clazz = ex.getClass();
                throw new BasicError(4101, clazz.getName());
            } catch (NullPointerException ex) {
                throw new BasicError(4101, "Channel not writable");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public int GetByte(int iocb) {
        if (iocb >= 0 && iocb < MAXFILES) {
            if (httpConn[iocb] != null && baos[iocb] != null) {
                sub_lolclose(iocb);
            }

            try {
                byte b = dataInput[iocb].readByte();
                return b;
            } catch (IOException ex) {
                Class clazz = ex.getClass();
                throw new BasicError(4101, clazz.getName());
            } catch (NullPointerException ex) {
                throw new BasicError(4101, "Channel not readable");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public int GetInt(int iocb) {
        if (iocb >= 0 && iocb < MAXFILES) {
            if (httpConn[iocb] != null && baos[iocb] != null) {
                sub_lolclose(iocb);
            }

            try {
                int i = dataInput[iocb].readInt();
                return i;
            } catch (IOException ex) {
                Class clazz = ex.getClass();
                throw new BasicError(4101, clazz.getName());
            } catch (NullPointerException ex) {
                throw new BasicError(4101, "Channel not readable");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

//#if MB191
//#     public void PutFloat(int iocb, float value) {
//#         if (iocb >= 0 && iocb < MAXFILES) {
//#             try {
//#                 this.dataOutput[iocb].writeFloat(value);
//#             } catch (IOException ex) {
//#                 Class clazz = ex.getClass();
//#                 throw new BasicError(4101, clazz.getName());
//#             } catch (NullPointerException ex) {
//#                 throw new BasicError(4101, "Channel not writable");
//#             }
//#         } else {
//#             throw new BasicError(4096, "Invalid channel");
//#         }
//#     }
//# 
//#     public float GetFloat(int iocb) {
//#         if (iocb >= 0 && iocb < MAXFILES) {
//#             if (httpConn[iocb] != null && baos[iocb] != null) {
//#                 sub_lolclose(iocb);
//#             }
//# 
//#             try {
//#                 float f = dataInput[iocb].readFloat();
//#                 return f;
//#             } catch (IOException ex) {
//#                 Class clazz = ex.getClass();
//#                 throw new BasicError(4101, clazz.getName());
//#             } catch (NullPointerException ex) {
//#                 throw new BasicError(4101, "Channel not readable");
//#             }
//#         } else {
//#             throw new BasicError(4096, "Invalid channel");
//#         }
//#     }
//# 
//#endif
    public String GetString(int iocb) {
        if (iocb >= 0 && iocb < MAXFILES) {
            if (httpConn[iocb] != null && baos[iocb] != null) {
                sub_lolclose(iocb);
            }

            try {
                String s = dataInput[iocb].readUTF();
                return s;
            } catch (IOException ex) {
                Class clazz = ex.getClass();
                throw new BasicError(4101, clazz.getName());
            } catch (NullPointerException ex) {
                throw new BasicError(4101, "Channel not readable");
            }
        } else {
            throw new BasicError(4096, "Invalid channel");
        }
    }

    public void PrintString(String s) {
        if (var_11e5) {
            BASIC.sub_750(s);
        } else if (listFlag) {
            if (var_1064 == offsetLine) {
                if (canvas.printString(s, 0, -1, true, true)) {
                    ++lineInScr;
                    editor.insert(s, editor.size());
                } else {
                    var_1064 += lineInScr;
                    lineInScr = 0;
                }
            } else {
                ++var_1064;
            }
        } else {
            Alert alert;
            if (!s.startsWith("Error") && !s.startsWith("Error:")) {
                if (var_c90) {
                    (alert = new Alert("Стоп", s, (Image) null, AlertType.INFO)).setTimeout(3000);
                    display.setCurrent(alert);
                } else {
                    canvas.printString(s, 0, -1, true, Settings.m_aZ);
                }
            } else {
                (alert = new Alert("Ошибка", s, (Image) null, AlertType.ERROR)).setTimeout(-2);
                display.setCurrent(alert);
            }
        }
    }

    public void CLS() {
        gc.setColor(0xffffff);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setColor(0);
        canvas.xposText = 0;
        canvas.yposText = 0;
    }

    public void DrawLine(int fromX, int fromY, int toX, int toY) {
        gc.drawLine(fromX, fromY, toX, toY);
    }

    public void FillRect(int x, int y, int w, int h) {
        gc.fillRect(x, y, w, h);
    }

    public void DrawRect(int x, int y, int w, int h) {
        gc.drawRect(x, y, w, h);
    }

    public void FillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight) {
        gc.fillRoundRect(x, y, w, h, arcWidth, arcHeight);
    }

    public void DrawRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight) {
        gc.drawRoundRect(x, y, w, h, arcWidth, arcHeight);
    }

    public void FillArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        gc.fillArc(x, y, w, h, startAngle, arcAngle);
    }

    public void DrawArc(int x, int y, int w, int h, int startAngle, int arcAngle) {
        gc.drawArc(x, y, w, h, startAngle, arcAngle);
    }

    public void SetColor(int r, int g, int b) {
        gc.setColor(r, g, b);
    }

    public void Blit(int fromX, int fromY, int w, int h, int toX, int toY) {
        canvas.Blit(fromX, fromY, w, h, toX, toY);
    }

    public void GelLoad(String gelName, String resourceName) {
        canvas.GelLoad(gelName, resourceName);
    }

    public void GelGrab(String gelName, int x, int y, int w, int h) {
        canvas.GelGrab(gelName, x, y, w, h);
    }

    public int GelWidth(String gelName) {
        return canvas.GelWidth(gelName);
    }

    public int GelHeight(String gelName) {
        return canvas.GelHeight(gelName);
    }

    public void ColorAlphaGel(String name, int a, int r1, int g1, int b1) {
        canvas.ColorAlphaGel(name, a, r1, g1, b1);
    }

    public void AlphaGel(String name, int i) {
        canvas.AlphaGel(name, i);
    }

    public void DrawGel(String gelName, int x, int y) {
        canvas.DrawGel(gelName, x, y);
    }

    public void DelGel(String key) {
        canvas.DelGel(key);
    }

    public void DelSprite(String key) {
        canvas.DelSprite(key);
    }

    public void SpriteGEL(String spriteName, String gelName) {
        canvas.SpriteGEL(spriteName, gelName);
    }

    public void SpriteMove(String spriteName, int x, int y) {
        canvas.SpriteMove(spriteName, x, y);
    }

    public int SpriteHit(String spriteName1, String spriteName2) {
        return canvas.SpriteHit(spriteName1, spriteName2);
    }

    public void DrawString(String s, int x, int y) {
        gc.drawString(s, x, y, Graphics.TOP | Graphics.LEFT);
    }

    public int ScreenWidth() {
        return canvas.getWidth();
    }

    public int ScreenHeight() {
        return canvas.getHeight();
    }

    public int isColor() {
        return display.isColor() ? 1 : 0;
    }

    public int NumColors() {
        return display.numColors();
    }

    public int StringWidth(String s) {
        return canvas.font.stringWidth(s);
    }

    public int StringHeight(String s) {
        return canvas.font.getHeight();
    }

    public int Up() {
        return (canvas.gameActionBits & BasicCanvas.GAME_UP);
    }

    public int Down() {
        return (canvas.gameActionBits & BasicCanvas.GAME_DOWN);
    }

    public int Left() {
        return (canvas.gameActionBits & BasicCanvas.GAME_LEFT);
    }

    public int Right() {
        return (canvas.gameActionBits & BasicCanvas.GAME_RIGHT);
    }

    public int Fire() {
        return (canvas.gameActionBits & BasicCanvas.GAME_FIRE);
    }

    public int GameA() {
        return (canvas.gameActionBits & BasicCanvas.GAME_A);
    }

    public int GameB() {
        return (canvas.gameActionBits & BasicCanvas.GAME_B);
    }

    public int GameC() {
        return (canvas.gameActionBits & BasicCanvas.GAME_C);
    }

    public int GameD() {
        return (canvas.gameActionBits & BasicCanvas.GAME_D);
    }

    public int INKEY() {
        return canvas.keyPressed;
    }

    public int Year(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    public int Month(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.MONTH) + 1;
    }

    public int Day(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    public int Hour(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.HOUR_OF_DAY);
    }

    public int Minute(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.MINUTE);
    }

    public int Second(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.SECOND);
    }

    public int Millisecond(Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.MILLISECOND);
    }

    public Enumeration Directory(String filter) {
        String[] filenames = null;
        Vector v = new Vector();
        if (filter.startsWith("file://")) {
            int index = filter.lastIndexOf(47);
            String path = filter.substring(0, index + 1);
            filter = filter.substring(index + 1);
            try {
                FileConnection fc = (FileConnection) Connector.open(path, Connector.READ);
                if (path.length() == 0) {
                    Enumeration e = FileSystemRegistry.listRoots();
                    while (e.hasMoreElements()) {
                        v.addElement(e.nextElement());
                    }
                } else {
                    Enumeration e = null;
                    try {
                        e = fc.list();
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    while (e.hasMoreElements()) {
                        v.addElement(e.nextElement());
                    }
                    fc.close();
                    int size = v.size();
                    filenames = new String[size];
                    for (int i = 0; i < size; ++i) {
                        filenames[i] = (String) v.elementAt(i);
                    }
                }
            } catch (IOException ex) {
            }
        } else {
            filenames = RecordStore.listRecordStores();
        }
        v = null;
        Enumeration elements = null;
        if (filenames != null) {
            int len = filenames.length;
            for (int i = 0; i < len; ++i) {
                String filename;
                if ((filename = filenames[i]).charAt(0) != '.') {
                    boolean match = true;
                    char filterCh = 0;
                    int fOffset = 0;

                    for (int j = 0; j < filename.length(); ++j) {
                        if (filterCh == 0) {
                            if (fOffset >= filter.length()) {
                                match = false;
                                break;
                            }
                            filterCh = filter.charAt(fOffset++);
                        }
                        if (filterCh == '*') {
                            /*
                             * Check next filter character (if present)
                             * for wildcard termination character
                             */
                            if (fOffset < filter.length()) {
                                char nextFilterCh = filter.charAt(fOffset);
                                if (filename.charAt(j) == nextFilterCh) {
                                    filterCh = 0;
                                    ++fOffset;
                                }
                            }
                        } else {
                            if (filename.charAt(j) != filterCh) {
                                match = false;
                                break;
                            }
                            filterCh = 0;
                        }
                    }
                    if (fOffset != filter.length()) {
                        match = false;
                    }
                    if (match) {
                        if (v == null) {
                            v = new Vector();
                        }
                        v.addElement(filename);
                    }
                }
            }
        }
        if (v != null) {
            elements = v.elements();
        }
        return elements;
    }

    public String GetLine(String prompt, String defaultText) {
        Displayable originalDisplayable;
        if ((originalDisplayable = display.getCurrent()) != inputTB) {
            display.setCurrent(inputTB);
        }
        inputTB = new TextBox(prompt, "", 255, 0);
        Ticker ticker = new Ticker(prompt);
        inputTB.setTicker(ticker);
        userProceedCommand = new Command("Ok", 4, 1);
        inputTB.addCommand(userProceedCommand);
        inputTB.setCommandListener(this);
        display.setCurrent(inputTB);
        String s = waitObject;
        synchronized (waitObject) {
            try {
                this.waitObject.wait();
            } catch (Exception ex) {
            }
        }
        canvas.Focus();
        s = inputTB.getString();
        inputTB = null;
        if (originalDisplayable != inputTB) {
            display.setCurrent(originalDisplayable);
        }
        return s;
    }

    public void Bye() {
        destroyApp(false);
        notifyDestroyed();
    }

    public void Delete(String path) {
        if (path.startsWith("file://")) {
            try {
                FileConnection fm = (FileConnection) Connector.open(path);
                fm.delete();
                fm.close();
            } catch (IOException ex) {
            }
        }
        try {
            RecordStore.deleteRecordStore(path);
        } catch (RecordStoreNotFoundException ex) {
        } catch (RecordStoreException ex) {
        }
    }

    public String EditForm(String formTitle, String proceedText, String cancelText, String label, String defaultText, int maxLen, int mode) {
        String text = null;
        switch (mode) {
            case 0:
                mode = TextField.ANY;
                break;
            case 1:
                mode = TextField.PASSWORD;
                break;
            case 2:
                mode = TextField.NUMERIC;
                break;
            case 3:
                mode = TextField.EMAILADDR;
                break;
            case 4:
                mode = TextField.PHONENUMBER;
                break;
            case 5:
                mode = TextField.URL;
                break;
            default:
                throw new BasicError(BasicError.VALUE_ERROR, "type must be 0..5");
        }
        if (maxLen > 0) {
            try {
                TextField textField = new TextField(label, defaultText, maxLen, mode);
                if (UserForm(formTitle, proceedText, cancelText, textField) == 1) {
                    text = textField.getString();
                }
                return text;
            } catch (IllegalArgumentException ex) {
                throw new BasicError(BasicError.VALUE_ERROR, "Invalid default text");
            }
        } else {
            throw new BasicError(BasicError.VALUE_ERROR, "Maximum length must be > 0");
        }
    }

    public Date DateForm(String formTitle, String proceedText, String cancelText, String label, Date date, int mode) {
        if (mode == 1) {
            mode = DateField.DATE;
        } else if (mode == 2) {
            mode = DateField.TIME;
        } else {
            mode = DateField.DATE_TIME;
        }
        DateField dateField = new DateField(label, mode);
        if (date != null) {
            dateField.setDate(date);
        }
        if (UserForm(formTitle, proceedText, cancelText, dateField) == 1) {
            date = dateField.getDate();
        } else {
            date = null;
        }
        return date;
    }

    public int ChoiceForm(String formTitle, String proceedText, String cancelText, String label, String[] stringArray, int mode) {
        boolean var7 = false;
        byte var11;
        if (mode == 0) {
            var11 = 1;
        } else {
            var11 = 2;
            if (stringArray.length > 32) {
                throw new BasicError(6, "Maximum of 32 items in a multiple choice");
            }
        }

        ChoiceGroup choiceGroup = new ChoiceGroup(label, var11, stringArray, null);
        int res;
        if (this.UserForm(formTitle, proceedText, cancelText, choiceGroup) == 1) {
            if (var11 == 2) {
                boolean[] var9 = new boolean[32];
                choiceGroup.getSelectedFlags(var9);
                res = 0;

                for (int var10 = 31; var10 >= 0; --var10) {
                    res <<= 1;
                    if (var9[var10]) {
                        res |= 1;
                    }
                }
            } else {
                res = choiceGroup.getSelectedIndex();
            }
        } else {
            res = -1;
        }

        return res;
    }

    public int GaugeForm(String var1, String var2, String var3, String var4, int var5, int var6, int var7) {
        boolean var8 = false;
        if (var5 > 0) {
            Gauge var9 = new Gauge(var4, var7 == 1, var5, var6);
            int var10;
            if (this.UserForm(var1, var2, var3, var9) == 1) {
                var10 = var9.getValue();
            } else {
                var10 = -1;
            }

            return var10;
        } else {
            throw new BasicError(6, "Maximum value must be >0");
        }
    }

    public int MessageForm(String var1, String var2, String var3, String var4, String var5) {
        StringItem var6 = new StringItem(var4, var5);
        return this.UserForm(var1, var2, var3, var6);
    }

    public int SELECT(String var1, String[] var2) {
        boolean var3 = false;
        this.var_13d9 = new List(var1, 3, var2, (Image[]) null);
        this.var_13d9.setCommandListener(this);
        this.display.setCurrent(this.var_13d9);
        String var4 = this.waitObject;
        synchronized (this.waitObject) {
            try {
                this.waitObject.wait();
            } catch (Exception ex) {
            }
        }

        this.canvas.Focus();
        this.display.setCurrent(this.canvas);
        int var9 = this.userExitStatus;
        return this.userExitStatus;
    }

    public void alert(String var1, String var2, String var3, int var4, int var5) {
        Image var7 = (Image) this.canvas.gelHashtable.get(var3);
        AlertType var6;
        switch (var4) {
            case 0:
                var6 = AlertType.CONFIRMATION;
                break;
            case 1:
                var6 = AlertType.INFO;
                break;
            case 2:
                var6 = AlertType.WARNING;
                break;
            case 3:
                var6 = AlertType.ERROR;
                break;
            case 4:
                var6 = AlertType.ALARM;
                break;
            default:
                throw new BasicError(6, "type must be 0..4");
        }

        Alert var8 = new Alert(var1, var2, var7, var6);
        if (var5 <= 0) {
            var5 = -2;
        }

        var8.setTimeout(var5);
        this.display.setCurrent(var8);
    }

    public void menuAdd(String command, int type, int priority) {
        if ((Command) commandHashtable.get(command) == null) {
            try {
                Command var5 = new Command(command, type, priority);
                canvas.addCommand(var5);
                canvas.setCommandListener(this);
                commandHashtable.put(command, var5);
            } catch (Exception var6) {
                throw new BasicError(6, "Invalid Command");
            }
        }
    }

    public String menuItem() {
        String var1 = "";
        if (var_1463 != null) {
            var1 = var_1463.getLabel();
            var_1463 = null;
        }

        return var1;
    }

    public void menuRemove(String command) {
        Command var2;
        if ((var2 = (Command) commandHashtable.get(command)) != null) {
            canvas.removeCommand(var2);
            commandHashtable.remove(command);
        }

    }

    public int sendSms(String number, String text) {
        try {
            String adrr = "sms://" + number;
            MessageConnection mc = (MessageConnection) Connector.open(adrr);
            TextMessage tm = (TextMessage) mc.newMessage("text");
            tm.setAddress(adrr);
            tm.setPayloadText(text);
            mc.send(tm);
            mc.close();
        } catch (Throwable ex) {
            return 0;
        }
        return 1;
    }
}
