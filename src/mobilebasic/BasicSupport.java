package mobilebasic;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.Date;
import java.util.Enumeration;

public abstract interface BasicSupport {

    /*
     * GUI Callback Methods
     */
    abstract void Message(String msg);

    abstract void Error(String msg);

    /*
     * BASIC Program's Callback Methods
     */
    abstract void OpenFile(int iocb, String filename, boolean readOnly);

    abstract void CloseFile(int iocb);

    abstract void CloseAllFiles();

    abstract int Note(int iocb);

    abstract void Point(int iocb, int record);

    abstract DataInput GetDataInputChannel(int iocb);

    abstract DataOutput GetDataOutputChannel(int iocb);

    abstract void PutByte(int iocb, int byteValue);

    abstract void PutInt(int iocb, int intValue);

    abstract void PutString(int iocb, String s);

    abstract int GetByte(int iocb);

    abstract int GetInt(int iocb);

    abstract String GetString(int iocb);

    abstract void PrintString(String string);   // canvas.printString(string)

    abstract void CLS();

    abstract void DrawLine(int fromX, int fromY, int toX, int toY);

    abstract void FillRect(int x, int y, int w, int h);

    abstract void DrawRect(int x, int y, int w, int h);

    abstract void FillRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight);

    abstract void DrawRoundRect(int x, int y, int w, int h, int arcWidth, int arcHeight);

    abstract void FillArc(int x, int y, int w, int h, int startAngle, int arcAngle);

    abstract void DrawArc(int x, int y, int w, int h, int startAngle, int arcAngle);

    abstract void SetColor(int r, int g, int b);

    abstract void GelLoad(String gelName, String resourceName);

    abstract void GelGrab(String gelName, int x, int y, int w, int h);

    abstract int GelWidth(String gelName);

    abstract int GelHeight(String gelName);

    abstract void DrawGel(String gelName, int x, int y);

    abstract void SpriteGEL(String spriteName, String gelName);

    abstract void SpriteMove(String spriteName, int x, int y);

    abstract int SpriteHit(String spriteName1, String spriteName2);

    abstract void Blit(int fromX, int fromY, int w, int h, int toX, int toY);

    abstract void DrawString(String s, int x, int y);

    abstract int ScreenWidth();

    abstract int ScreenHeight();

    abstract int isColor();

    abstract int NumColors();

    abstract int StringWidth(String s);

    abstract int StringHeight(String s);

    abstract int Up();

    abstract int Down();

    abstract int Left();

    abstract int Right();

    abstract int Fire();

    abstract int GameA();

    abstract int GameB();

    abstract int GameC();

    abstract int GameD();

    abstract int Year(Date date);

    abstract int Month(Date date);

    abstract int Day(Date date);

    abstract int Hour(Date date);

    abstract int Minute(Date date);

    abstract int Second(Date date);

    abstract int Millisecond(Date date);

    abstract Enumeration Directory(String filter);

    abstract String GetLine(String prompt, String defaultText);

    abstract void Bye();

    abstract void Delete(String filename);

    abstract String EditForm(String formTitle, String proceedText, String cancelText, String label, String defaultText, int maxLen, int mode);

    abstract Date DateForm(String formTitle, String proceedText, String cancelText, String label, Date date, int mode);

    abstract int ChoiceForm(String formTitle, String proceedText, String cancelText, String label, String[] stringArray, int mode);

    abstract int GaugeForm(String formTitle, String proceedText, String cancelText, String label, int maxValue, int initialValue, int mode);

    abstract int MessageForm(String formTitle, String proceedText, String cancelText, String label, String msg);

    abstract int sendSms(String number, String text);

    abstract void menuRemove(String command);

    abstract String menuItem();

    abstract void menuAdd(String command, int type, int priority);

    abstract void alert(String var1, String var2, String var3, int var4, int var5);

    abstract int SELECT(String var1, String[] var2);

    abstract void DelGel(String key);

    abstract void DelSprite(String key);

    abstract void AlphaGel(String name, int i);

    abstract void ColorAlphaGel(String name, int a, int r1, int g1, int b1);
}
