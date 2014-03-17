package mobilebasic;

import com.view.Settings;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

public class BasicCanvas extends Canvas implements Runnable {

    public static final int GAME_UP = 0x0001;
    public static final int GAME_DOWN = 0x0002;
    public static final int GAME_LEFT = 0x0004;
    public static final int GAME_RIGHT = 0x0008;
    public static final int GAME_FIRE = 0x0010;
    public static final int GAME_A = 0x0020;
    public static final int GAME_B = 0x0040;
    public static final int GAME_C = 0x0080;
    public static final int GAME_D = 0x0100;
    public int gameActionBits;
    private int widthInPixels;
    private int heightInPixels;
    public Font font;
    private int lineHeight;
    private int charWidth;
    public int widthInChars;
    public int heightInChars;
    private int nlines;
    private Image graphicsImage;
    private Image offScreenImage;
    public Graphics graphicsGc;
    private Graphics offScreenGc;
    /*
     * Sprite / Gobs (Graphic Objects) / Gels (Graphic Elements)
     *
     * Updating the sprite is very costly since there is no
     * direct access to the image data. Consequently all
     * images should be drawn first. If we have a image
     * per sprite then there could be a lot of duplication
     * particularily if there are many similar sprites.
     */
    public java.util.Hashtable gelHashtable;
    private com.utils.Hashtable spriteHashtable;
    private Thread thread;
    public int yposText;
    public int xposText;
    private static char[][] lowerCaseKeys = new char[][]{{' ', '\n', '\u0001', '\u0002', '\b', '\t'}, {'%', '$', ',', '\"', '.', ':', '?', '\'', '(', ')', '!', '@', ';', '{', '}', '[', ']'}, {'a', 'b', 'c', '\u0430', '\u0431', '\u0432', '\u0433'}, {'d', 'e', 'f', '\u0434', '\u0435', '\u0451', '\u0436', '\u0437'}, {'g', 'h', 'i', '\u0438', '\u0439', '\u043a', '\u043b'}, {'j', 'k', 'l', '\u043c', '\u043d', '\u043e', '\u043f'}, {'m', 'n', 'o', '\u0440', '\u0441', '\u0442', '\u0443'}, {'p', 'q', 'r', 's', '\u0444', '\u0445', '\u0446', '\u0447'}, {'t', 'u', 'v', '\u0448', '\u0449', '\u044a', '\u044b', '\u044c'}, {'w', 'x', 'y', 'z', '\u044d', '\u044e', '\u044f'}, {'/', '+', '-', '%', '=', '<', '>', '&', '|', '^'}, {'\u0003', '\u0004'}};
    private static char[][] upperCaseKeys = new char[][]{{' ', '\n', '\u0001', '\u0002', '\b', '\t'}, {'%', '$', ',', '\"', '.', ':', '?', '\'', '(', ')', '!', '@', ';', '{', '}', '[', ']'}, {'A', 'B', 'C', '\u0410', '\u0411', '\u0412', '\u0413'}, {'D', 'E', 'F', '\u0414', '\u0415', '\u0401', '\u0416', '\u0417'}, {'G', 'H', 'I', '\u0418', '\u0419', '\u041a', '\u041b'}, {'J', 'K', 'L', '\u041c', '\u041d', '\u041e', '\u041f'}, {'M', 'N', 'O', '\u0420', '\u0421', '\u0422', '\u0423'}, {'P', 'Q', 'R', 'S', '\u0424', '\u0425', '\u0426', '\u0427'}, {'T', 'U', 'V', '\u0428', '\u0429', '\u042a', '\u042b', '\u042c'}, {'W', 'X', 'Y', 'Z', '\u042d', '\u042e', '\u042f'}, {'/', '+', '-', '=', '<', '>', '&', '|', '^'}, {'\u0003', '\u0004'}};
    private static char[][] keys = lowerCaseKeys;
    private StringBuffer inputLine;
    private int var_4c4;
    private int var_51d;
    private int var_54f;
    private boolean var_567;
    public int keyPressed;
    private int lastKeyIndex;
    private int keyClicks;
    private long keyTime;
    private long var_6d4;
    private boolean var_728;
    private char[] chArray;
    private Main main;

    public BasicCanvas(Main var1) {
        this.main = var1;
        gameActionBits = 0;
        gelHashtable = new java.util.Hashtable();
        spriteHashtable = new com.utils.Hashtable();
        yposText = 0;
        xposText = 0;
        inputLine = new StringBuffer(128);
        var_4c4 = 0;
        var_51d = 0;
        var_54f = 0;
        var_567 = false;
        keyPressed = 0;
        lastKeyIndex = -1;
        keyClicks = 0;
        keyTime = 0L;
        var_6d4 = 0L;
        chArray = new char[1];
        setFullScreenMode(Settings.m_fullScreenZ);
        //setFullScreenMode(main.getAppProperty("FullScreenMode").equals("true") ? true : false);
        widthInPixels = getWidth();
        heightInPixels = getHeight();
        graphicsImage = Image.createImage(widthInPixels, heightInPixels);
        graphicsGc = this.graphicsImage.getGraphics();
        offScreenImage = Image.createImage(widthInPixels, heightInPixels);
        offScreenGc = offScreenImage.getGraphics();
    }

    public final void SetFontSize(int fontSize) {
        /*
         * FACE_ {MONOSPACE|PROPORTIONAL|SYSTEM}
         * SIZE_ {SMALL|MEDIUM|LARGE}
         * STYLE_ {BOLD|ITALIC|PLAIN|UNDERLINE}
         */

        //System.out.println("fontSize = " + fontSize);
        switch (fontSize) {
            case 1:
                font = Font.getFont(Font.FACE_MONOSPACE,
                        Font.STYLE_PLAIN,
                        Font.SIZE_SMALL);
                break;
            case 2:
            default:
                font = Font.getFont(Font.FACE_MONOSPACE,
                        Font.STYLE_PLAIN,
                        Font.SIZE_MEDIUM);
                break;
            case 3:
                font = Font.getFont(Font.FACE_MONOSPACE,
                        Font.STYLE_PLAIN,
                        Font.SIZE_LARGE);
        }

        charWidth = font.charWidth('W');
        widthInChars = widthInPixels / charWidth;
        if (fontSize == 0 && widthInChars < 12) {
            //System.out.println("Auto: Changing to SMALL Font");
            font = Font.getFont(Font.FACE_MONOSPACE,
                    Font.STYLE_PLAIN,
                    Font.SIZE_SMALL);
            charWidth = font.charWidth('W');
            widthInChars = widthInPixels / charWidth;
        }

        lineHeight = font.getHeight();
        heightInPixels = getHeight();
        heightInChars = heightInPixels / lineHeight;
        graphicsGc.setFont(font);
        offScreenGc.setFont(font);
    }

    public final void Init() {
        gelHashtable.clear();
        spriteHashtable.clear();
    }

    public void DelGel(String key) {
        gelHashtable.remove(key);
    }

    public void DelSprite(String key) {
        spriteHashtable.remove(key);
    }

    /*
     * Called when the Canvas is getting the Focus again.
     * If pressing FIRE causes the BASIC program to display a form then the canvas
     * never gets key release events and the gameActionBits will still indicate
     * that the Fire button is pressed. This routine allows us to reset the
     * status before it is re-focussed.
     */
    public final void Focus() {
        gameActionBits = 0;
    }

    public boolean printString(String string, int firstChar, int cursorChar, boolean wrapEnabled, boolean pauseEnabled) {
        heightInPixels = getHeight();
        heightInChars = heightInPixels / lineHeight;
        int strlen = string.length();

        for (int i = firstChar; i < strlen; ++i) {
            char ch;
            if ((ch = string.charAt(i)) == '\n' || xposText + charWidth > widthInPixels) {
                if (!wrapEnabled) {
                    return true;
                }

                xposText = 0;
                yposText += lineHeight;
                if (yposText + lineHeight > heightInPixels) {
                    if (main.listFlag) {
                        return false;
                    }

                    if (pauseEnabled) {
                        if (nlines >= heightInChars) {

                            while (keyPressed == 0) { // Wait for key press

                                Thread.yield();
                            }

                            while (keyPressed != 0) { // Wait for key release
                                Thread.yield();
                            }


                            nlines = 0;
                        }
                    } else {
                        nlines = 0;
                    }

                    Blit(0, lineHeight, widthInPixels, heightInPixels - lineHeight, 0, 0);
                    yposText -= lineHeight;
                    ++nlines;
                }
            }

            /*
             * Clear line if at start of line
             */
            if (xposText == 0) {
                graphicsGc.setColor(0xffffff);
                graphicsGc.fillRect(0, yposText, widthInPixels, lineHeight);
                graphicsGc.setColor(0);
            }

            /*
             * Draw Character if not newline
             */
            if (ch != '\n') {
                if (i == cursorChar && !main.listFlag) {
                    graphicsGc.setColor(0);
                    graphicsGc.fillRect(xposText, yposText, charWidth, lineHeight);
                    graphicsGc.setColor(0xffffff);
                } else {
                    graphicsGc.setColor(0);
                }

                graphicsGc.drawChar(ch, xposText + charWidth / 2, yposText, 17);
                xposText += charWidth;
            }
        }

        return true;
    }

    private void sub_1fc(int var1) {
        if (var1 == -1) {
            var_51d = 0;
            var_54f = this.var_4c4;
            if (xposText > 0) {
                printString("\n", 0, -1, true, false);
                xposText = 0;
            }
        } else if (var1 == 10) {
            String var2 = inputLine.toString();
            inputLine.delete(0, inputLine.length());
            printString(var2 + "\n", 0, -1, true, false);
            var2.substring(var_4c4);
            var_51d = var_4c4;
            var_54f = var_4c4;
        } else if (var1 == 1) {
            if (var_54f > var_4c4) {
                --var_54f;
            }
        } else if (var1 == 2) {
            ++var_54f;
            if (var_54f > inputLine.length()) {
                var_54f = inputLine.length();
            }
        } else if (var1 == 8) {
            if (var_54f > var_4c4) {
                inputLine.deleteCharAt(var_54f - 1);
                --var_54f;
            }
        } else if (var1 == 9) {
            if (var_54f < inputLine.length()) {
                inputLine.deleteCharAt(var_54f);
                if (var_54f > inputLine.length()) {
                    var_54f = inputLine.length();
                }
            }
        } else {
            inputLine.insert(var_54f++, (char) var1);
        }

        if (var_54f < var_51d) {
            var_51d = var_54f;
        } else if (var_54f >= var_51d + widthInChars) {
            var_51d = var_54f - widthInChars + 1;
        }

        printString(inputLine.toString() + " ", var_51d, var_54f, false, false);
        xposText = 0;
    }

    private void sub_21b(int var1) {
        synchronized (this) {
            boolean var3 = false;
            byte var7;
            switch (var1) {
                case 35:
                    var7 = 11;
                    break;
                case 36:
                case 37:
                case 38:
                case 39:
                case 40:
                case 41:
                case 43:
                case 44:
                case 45:
                case 46:
                case 47:
                default:
                    var7 = -1;
                    break;
                case 42:
                    var7 = 10;
                    break;
                case 48:
                    var7 = 0;
                    break;
                case 49:
                    var7 = 1;
                    break;
                case 50:
                    var7 = 2;
                    break;
                case 51:
                    var7 = 3;
                    break;
                case 52:
                    var7 = 4;
                    break;
                case 53:
                    var7 = 5;
                    break;
                case 54:
                    var7 = 6;
                    break;
                case 55:
                    var7 = 7;
                    break;
                case 56:
                    var7 = 8;
                    break;
                case 57:
                    var7 = 9;
            }

            if (var7 != -1) {
                label46:
                {
                    keyTime = System.currentTimeMillis();
                    if (var7 == lastKeyIndex) {
                        ++keyClicks;
                        if (keyClicks != keys[var7].length) {
                            break label46;
                        }
                    } else {
                        if (lastKeyIndex == -1) {
                            break label46;
                        }

                        char var6;
                        if ((var6 = keys[lastKeyIndex][keyClicks]) == 3) {
                            keys = lowerCaseKeys;
                        } else if (var6 == 4) {
                            keys = upperCaseKeys;
                        } else {
                            sub_1fc(var6);
                        }
                    }

                    keyClicks = 0;
                }
            }

            lastKeyIndex = var7;
        }
    }

    private void sub_22e(int var1) {
        boolean var2 = System.currentTimeMillis() - this.var_6d4 > 500L;
        synchronized (this) {
            if (this.lastKeyIndex != -1) {
                char var4 = keys[this.lastKeyIndex][this.keyClicks];
                this.sub_1fc(var4);
                this.lastKeyIndex = -1;
                this.keyClicks = 0;
            }

            this.keyTime = 0L;
            switch (this.getGameAction(var1)) {
                case 1:
                case 2:
                    this.sub_1fc(var2 ? 8 : 1);
                case 3:
                case 4:
                case 7:
                default:
                    break;
                case 5:
                case 6:
                    this.sub_1fc(var2 ? 9 : 2);
                    break;
                case 8:
                case 9:
                case 10:
                case 11:
                case 12:
                    this.sub_1fc(10);
            }

        }
    }

    public void start() {
        var_728 = false;
        thread = new Thread(this);
        thread.start();
        System.out.println("start()\n");
    }

    public void sub_2b5() {
        this.var_728 = true;

        try {
            System.out.println("join()\n");
            this.thread.join();
        } catch (InterruptedException ex) {
        }

        this.thread = null;
    }
	
    int coun = 0;

    public void run() {
        while (!this.var_728) {
            try {
                if (this.keyTime > 0L) {
                    synchronized (this) {
                        long var1;
                        if ((var1 = System.currentTimeMillis()) > this.keyTime + 500L && this.keyPressed > 0) {
                            this.keyTime = 0L;
                            this.keyClicks = 0;
                            this.lastKeyIndex = -1;
                            this.sub_1fc(this.keyPressed);
                        } else if (var1 > this.keyTime + 1000L) {
                            char var4 = keys[this.lastKeyIndex][this.keyClicks];
                            this.keyTime = 0L;
                            this.keyClicks = 0;
                            this.lastKeyIndex = -1;
                            if (var4 == 3) {
                                keys = lowerCaseKeys;
                            } else if (var4 == 4) {
                                keys = upperCaseKeys;
                            } else {
                                this.sub_1fc(var4);
                            }
                        }
                    }

                }

//#if MB191
//#                 if (this.main.thread == null) {
//#                     this.repaint();
//#                     Thread.sleep(100L);
//#                 } else {
//#                     Thread.sleep(1L);
//#                 }
//#else

               this.repaint();
                Thread.sleep(100L);

//#endif




            } catch (InterruptedException vex) {
            } catch (ArrayIndexOutOfBoundsException ex) {
            } catch (Exception ex) {
            }
        }

    }

    public void keyPressed(int var1) {
        if (this.main.listFlag) {
            switch (this.getGameAction(var1)) {
                case 1:
                    --this.main.offsetLine;
                    break;
                case 2:
                    this.main.offsetLine -= this.heightInChars;
                case 3:
                case 4:
                default:
                    break;
                case 5:
                    this.main.offsetLine += this.heightInChars;
                    break;
                case 6:
                    ++this.main.offsetLine;
            }

            main.listInCanvas();
        } else {
            this.keyPressed = var1;
            if (var1 == -8) {
                this.sub_1fc(8);
            }

            switch (this.getGameAction(var1)) {
                case 1:
                    this.gameActionBits = 1;
                    break;
                case 2:
                    this.gameActionBits = 4;
                case 3:
                case 4:
                case 7:
                default:
                    break;
                case 5:
                    this.gameActionBits = 8;
                    break;
                case 6:
                    this.gameActionBits = 2;
                    break;
                case 8:
                    this.gameActionBits = 16;
                    break;
                case 9:
                    this.gameActionBits = 32;
                    break;
                case 10:
                    this.gameActionBits = 64;
                    break;
                case 11:
                    this.gameActionBits = 128;
                    break;
                case 12:
                    this.gameActionBits = 256;
            }

            if (this.var_567) {
                if (var1 > 0) {
                    this.sub_21b(var1);
                    return;
                }

                this.var_6d4 = System.currentTimeMillis();
            }

        }
    }

    public void keyReleased(int var1) {
        if (this.var_567 && var1 < 0) {
            this.sub_22e(var1);
        }

        this.keyPressed = 0;
        this.gameActionBits = 0;
    }

    public void Blit(int var1, int var2, int var3, int var4, int var5, int var6) {
        int var7 = this.graphicsGc.getClipX();
        int var8 = this.graphicsGc.getClipY();
        int var9 = this.graphicsGc.getClipWidth();
        int var10 = this.graphicsGc.getClipHeight();
        this.offScreenGc.drawImage(this.graphicsImage, -var1, -var2, 20);
        this.graphicsGc.setClip(var5, var6, var3, var4);
        this.graphicsGc.drawImage(this.offScreenImage, var5, var6, 20);
        this.graphicsGc.setClip(var7, var8, var9, var10);
    }

    public final void GelLoad(String var1, String var2) {
        Image var3 = null;

        try {
            if (var2.startsWith("file:")) {
                DataInputStream var4 = null;
                byte[] var7 = new byte[(var4 = ((FileConnection) Connector.open(var2)).openDataInputStream()).available() + 1];
                int var8 = 0;

                int var6;
                while ((var6 = var4.read(var7, var8, var7.length - var8)) != -1) {
                    if ((var8 += var6) == var7.length) {
                        byte[] var9 = new byte[var7.length + 4096];
                        System.arraycopy(var7, 0, var9, 0, var8);
                        var7 = var9;
                    }
                }

                var3 = Image.createImage(var7, 0, var8);
                var4.close();
            } else {
                var3 = Image.createImage("/" + var2);
            }

            this.gelHashtable.put(var1, var3);
        } catch (IOException ex) {
        }
    }

    public void AlphaGel(String name, int i) {
        Image img = (Image) gelHashtable.get(name);
        if (img == null) {
            return;
        }
        int w = img.getWidth();
        int h = img.getHeight();
        int length = w * h;
        int[] rgbData = new int[length];
        img.getRGB(rgbData, 0, w, 0, 0, w, h);
        int pixel, r, g, b;
        for (int k = 0; k < length; k++) {
            pixel = rgbData[k];
            r = (pixel >> 16) & 0xff;
            g = (pixel >> 8) & 0xff;
            b = pixel & 0xff;
            rgbData[k] = (i << 24) | ((r << 16) | (g << 8) | b);
        }
        gelHashtable.put(name, Image.createRGBImage(rgbData, w, h, true));
    }

    public void ColorAlphaGel(String name, int a, int r1, int g1, int b1) {
        Image img = (Image) gelHashtable.get(name);
        if (img == null) {
            return;
        }
        int color = (0xff << 24) | (r1 << 16) | (g1 << 8) | b1;
        int w = img.getWidth();
        int h = img.getHeight();
        int length = w * h;
        int[] rgbData = new int[length];
        img.getRGB(rgbData, 0, w, 0, 0, w, h);
        int pixel, r, g, b;
        for (int k = 0; k < length; k++) {
            pixel = rgbData[k];
            if (pixel == color) {
                r = (pixel >> 16) & 0xff;
                g = (pixel >> 8) & 0xff;
                b = pixel & 0xff;
                rgbData[k] = (a << 24) | ((r << 16) | (g << 8) | b);
            }
        }
        gelHashtable.put(name, Image.createRGBImage(rgbData, w, h, true));
    }

    public void GelGrab(String name, int x, int y, int w, int h) {
        Image image = Image.createImage(w, h);
        Graphics gr = image.getGraphics();
        gr.drawImage(graphicsImage, -x, -y, 20);
        gelHashtable.put(name, image);
    }

    public int GelWidth(String name) {
        Image var2;
        int var3;
        if ((var2 = (Image) this.gelHashtable.get(name)) != null) {
            var3 = var2.getWidth();
        } else {
            var3 = 0;
        }

        return var3;
    }

    public int GelHeight(String var1) {
        Image var2;
        int var3;
        if ((var2 = (Image) this.gelHashtable.get(var1)) != null) {
            var3 = var2.getHeight();
        } else {
            var3 = 0;
        }

        return var3;
    }

    public void DrawGel(String nameGel, int x, int y) {
        Image img = (Image) gelHashtable.get(nameGel);
        if (img != null) {
            graphicsGc.drawImage(img, x, y, 20);
        }
    }

    public void SpriteGEL(String nameSpr, String nameGel) {
        Image img;
        if ((img = (Image) gelHashtable.get(nameGel)) != null) {
            spriteHashtable.put(nameSpr, new Sprite(img));
        } else {
            throw new BasicError(256, "Invalid GEL");
        }
    }

    public void SpriteMove(String var1, int var2, int var3) {
        Sprite var4;
        if ((var4 = (Sprite) spriteHashtable.get(var1)) != null) {
            var4.setPosition(var2, var3);
        } else {
            throw new BasicError(257, "Invalid Sprite");
        }
    }

    public int SpriteHit(String name1, String name2) {
        Sprite spr1 = ((Sprite) spriteHashtable.get(name1));
        Sprite spr2 = ((Sprite) spriteHashtable.get(name2));
        if (spr1 != null && spr2 != null) {
            byte result = 0;
            if (spr1.collidesWith(spr2, true)) {
                result = 1;
            }
            return result;
        } else {
            throw new BasicError(257, "Invalid Sprite");
        }
    }

    public void paint(Graphics g) {
        offScreenGc.drawImage(graphicsImage, 0, 0, 20);
        Enumeration spriteEnumeration = spriteHashtable.elements();

        while (spriteEnumeration.hasMoreElements()) {
            ((Sprite) spriteEnumeration.nextElement()).paint(offScreenGc);
        }

        g.drawImage(offScreenImage, 0, 0, Graphics.TOP | Graphics.LEFT);
    }
}
