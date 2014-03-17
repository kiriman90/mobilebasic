package mobilebasic;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

/*
 * mod by dzanis and kiriman 02.2013
 */
public final class BASIC {

//#if MB191
//#     private static final int MAGIC = 0x4D420191;	// MB 0x0191
//#else
    private static final int MAGIC = 0x4D420001;	// MB 0x0001
//#endif
    private static Random random = new Random();
    private static Runtime runtime = Runtime.getRuntime();
    private static byte[] code = new byte[256];
    private static int codeLen = 0;
    private static byte[] sourceProg = null;
    private static int sourceSize = 0;
    private static int sourceLen = 0;
    private static boolean degFlag = false;
    private static boolean stepFlag = false; // Used during runtime of BASIC program if the STEP operand is present in a FOR loop
    private static int nvars;
    private static String[] varName = new String[256];
    private static byte[] varType = new byte[256];
    private static Object[] varObject = new Object[256];
    private static Enumeration dirEnum;
    public static Main main;
    private static String line;
    private static int lineLen;
    private static int lineOffset;
    private static String nextToken;
    private static final String tokenTable[] = {
        "\u1100\001\200STOP", "\u1100\001\200POP", "\u1100\001\200RETURN", "\u1100\001\200END", "\u1100\001\200NEW", "\u1100\001\200RUN", "\u1100\001\200DIR", "\u1100\001\200DEG", "\u1100\001\200RAD", "\u1100\001\200BYE",
        "\u1140\001\000GOTO", "\u1140\001\000GOSUB", "\u1140\001\000SLEEP", "@\001\uA700PRINT", "\u1100\001\000REM", "\u1140\001\u0200DIM", "\u1140\001\000IF", "\u11C2\001\000THEN", "\u1100\001\000CLS", "\u1140\001\000PLOT",
        "\u1140\001\000DRAWLINE", "\u1140\001\000FILLRECT", "\u1140\001\000DRAWRECT", "\u1140\001\000FILLROUNDRECT", "\u1140\001\000DRAWROUNDRECT", "\u1140\001\000FILLARC", "\u1140\001\000DRAWARC", "\u1140\001\000DRAWSTRING", "\u1140\001\000SETCOLOR", "\u1140\001\000BLIT",
        "\uF240\001\000FOR", "\u43C2\001\000TO", "\u43C2\001\000STEP", "\u1140\001\000NEXT", "\u1140\001\000INPUT", "\u1140\001\200LIST", "\u1140\001\000ENTER", "\u1140\001\000LOAD", "\u1140\001\000SAVE", "\u1140\001\000DELETE",
        "\u1140\001\uA700EDIT", "\u1140\001\000TRAP", "\u1140\001\000OPEN", "\u1140\001\000CLOSE", "\u1140\001\000NOTE", "\u1140\001\000POINT", "\u1140\001\000PUT", "\u1140\001\000GET", "\u1100\001\000DATA", "\u1140\001\000RESTORE",
        "\u1140\001\000READ", "\u8801@\247=", "\u8801@\247<>", "\u8801@\247<", "\u8801@\247<=", "\u8801@\247>", "\u8801@\247>=", "\uF201 \247(", "\u2E01\020\u0158)", "\u4301\b\247,",
        "\u9901@\247+", "\u9901@\247-", "\uCC01\200'-", "\uAA01@\247*", "\uAA01@\247/", "\uBB01@\247^", "\u88C1@\247BITAND", "\u88C1@\247BITOR", "\u88C1@\247BITXOR", "\u77C1\200'NOT",
        "\u66C1@\247AND", "\u55C1@\247OR", "\uF201\004 SCREENWIDTH", "\uF201\004 SCREENHEIGHT", "\uF201\004 ISCOLOR", "\uF201\004 NUMCOLORS", "\uF201\004 STRINGWIDTH", "\uF201\004 STRINGHEIGHT", "\uF201\004 LEFT$", "\uF201\004 MID$",
        "\uF201\004 RIGHT$", "\uF201\004 CHR$", "\uF201\004 STR$", "\uF201\004 LEN", "\uF201\004 ASC", "\uF201\004 VAL", "\uF201\004 UP", "\uF201\004 DOWN", "\uF201\004 LEFT", "\uF201\004 RIGHT",
        "\uF201\004 FIRE", "\uF201\004 GAMEA", "\uF201\004 GAMEB", "\uF201\004 GAMEC", "\uF201\004 GAMED", "\uF201\004 DAYS", "\uF201\004 MILLISECONDS", "\uF201\004 YEAR", "\uF201\004 MONTH", "\uF201\004 DAY",
        "\uF201\004 HOUR", "\uF201\004 MINUTE", "\uF201\004 SECOND", "\uF201\004 MILLISECOND", "\uF201\004 RND", "\uF201\004 ERR", "\uF201\004 FRE", "\uF201\004 MOD", "\uF201\004 EDITFORM", "\uF201\004 GAUGEFORM",
        "\uF201\004 CHOICEFORM", "\uF201\004 DATEFORM", "\uF201\004 MESSAGEFORM", "\uF201\004 LOG", "\uF201\004 EXP", "\uF201\004 SQR", "\uF201\004 SIN", "\uF201\004 COS", "\uF201\004 TAN", "\uF201\004 ASIN",
        "\uF201\004 ACOS", "\uF201\004 ATAN", "\uF201\004 ABS", "\u4302\001\000=", "\u4382\001\000#", "\u1140\001\000PRINT", "\u1140\001\000INPUT", "\002\001\000:", "\u1140\001\000GELGRAB", "\u1140\001\000DRAWGEL",
        "\u1140\001\000SPRITEGEL", "\u1140\001\000SPRITEMOVE", "\uF201\004 SPRITEHIT", "\uF201\004 READDIR$", "\uF201\004 PROPERTY$", "\u1140\001\000GELLOAD", "\uF201\004 GELWIDTH", "\uF201\004 GELHEIGHT",
        // ******** by Mumey***********
        "\u1140\001\000PLAYWAV",
        "\u1140\001\000PLAYTONE",
        "\uF201\004 INKEY",
        "\uF201\004 SELECT",
        "\u1140\001\000ALERT",
        "\u1140\001\000SETFONT",
        "\u1140\001\000MENUADD",
        "\uF201\004 MENUITEM",
        "\u1140\001\000MENUREMOVE",
        "\u1140\001\000CALL",
        "\u1100\001\200ENDSUB",
        //***********************************

        //********    added 02.2013    ***********
        "\u1100\001\000REPAINT",
        "\uF201\004 SENDSMS",
        "\uF201\004 RAND",
        "\u1140\001\000ALPHAGEL",
        "\u1140\001\000COLORALPHAGEL",
        "\u1140\001\000PLATFORMREQUEST",
        "\u1140\001\000DELGEL",
        "\u1140\001\000DELSPRITE",
        "\uF201\004 MKDIR",};
    private static final String[] var_38c = new String[]{"", "", "RET", "", "", "", "", "", "", "", "GT", "GS", "SL", "PR", "", "", "", "TH", "", "", "DL", "FR", "DR", "FRR", "DRR", "FA", "DA", "DS", "SC", "", "", "", "", "", "IN", "", "", "", "", "DEL", "", "TR", "OP", "CL", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "SCW", "SCH", "", "", "STW", "STH", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "EF", "GF", "CF", "DF", "MSF", "", "", "", "", "", "", "", "", "", "", "", "", "PR", "IN", "", "GG", "DG", "SG", "SM", "SH", "", "", "GL", "GW", "GH",
        // ******** added by Mumey***********
        "PW", "PT", "IK", "SEL", "AL", "SF", "MA", "MI", "MR", "", "ES",
        //********    added 02.2013    ***********
        "RE", "SS", "", "AG", "CAG", "PFR", "", "", "MKDIR",};
    private static int[] operandStackValue = new int[256];
    private static Object[] operandStackObject = new Object[256];
    private static byte[] operandStackType = new byte[256];
    private static byte[] operandStackClass = new byte[256];
    private static int valueSP = -1;
    private static int[] operatorStack = new int[256];
    private static int[] operatorPrior = new int[256];
    private static int operatorSP = -1;
    private static Object[] controlStack = new Object[256];
    private static int controlSP = -1;
    private static int commaCount = 0;
    private static int argCount = 0;
    private static Object poppedObject;
    private static byte poppedType;
    private static byte poppedClass;
    private static boolean stopProgramFlag;
    private static byte[] exeProg;
    private static int exeLen;
    private static int exePC;
    private static int exeNextLinePC;
    private static int trapPC;        // Execution point if error occurs
    private static int lastError;
    private static int dataPC;        // Pointer for Read Data
    private static int dataOffset;    // Offset into current data statement
    private static int dataLen;       // Length of data in current statement
    private static int var_93c;
    private static final int CONTROL_RETURN = 0;
    private static final int CONTROL_FORLOOP = 1;
    private static final byte CLASS_CONSTANT = 0;
    private static final byte CLASS_VARIABLE = 1;
    private static final byte TYPE_INTEGER = 0;
    private static final byte TYPE_FLOAT = 1;
    private static final byte TYPE_STRING = 2;
    private static final byte tokSTOP = 0;
    private static final byte tokPOP = 1;
    private static final byte tokRETURN = 2;
    private static final byte tokEND = 3;
    private static final byte tokNEW = 4;
    private static final byte tokRUN = 5;
    private static final byte tokDIR = 6;
    private static final byte tokDEG = 7;
    private static final byte tokRAD = 8;
    private static final byte tokBYE = 9;
    private static final byte tokGOTO = 10;
    private static final byte tokGOSUB = 11;
    private static final byte tokSLEEP = 12;
    private static final byte tokPRINT = 13;
    private static final byte tokREM = 14;
    private static final byte tokDIM = 15;
    private static final byte tokIF = 16;
    private static final byte tokTHEN = 17;
    private static final byte tokCLS = 18;
    private static final byte tokPLOT = 19;
    private static final byte tokDRAWLINE = 20;
    private static final byte tokFILLRECT = 21;
    private static final byte tokDRAWRECT = 22;
    private static final byte tokFILLROUNDRECT = 23;
    private static final byte tokDRAWROUNDRECT = 24;
    private static final byte tokFILLARC = 25;
    private static final byte tokDRAWARC = 26;
    private static final byte tokDRAWSTRING = 27;
    private static final byte tokSETCOLOR = 28;
    private static final byte tokBLIT = 29;
    private static final byte tokFOR = 30;
    private static final byte tokTO = 31;
    private static final byte tokSTEP = 32;
    private static final byte tokNEXT = 33;
    private static final byte tokINPUT = 34;
    private static final byte tokLIST = 35;
    private static final byte tokENTER = 36;
    private static final byte tokLOAD = 37;
    private static final byte tokSAVE = 38;
    private static final byte tokDELETE = 39;
    private static final byte tokEDIT = 40;
    private static final byte tokTRAP = 41;
    private static final byte tokOPEN = 42;
    private static final byte tokCLOSE = 43;
    private static final byte tokNOTE = 44;
    private static final byte tokPOINT = 45;
    private static final byte tokPUT = 46;
    private static final byte tokGET = 47;
    private static final byte tokDATA = 48;
    private static final byte tokRESTORE = 49;
    private static final byte tokREAD = 50;
    private static final byte tokEQ = 51;
    private static final byte tokNE = 52;
    private static final byte tokLT = 53;
    private static final byte tokLE = 54;
    private static final byte tokGT = 55;
    private static final byte tokGE = 56;
    private static final byte tokLBRACKET = 57;
    private static final byte tokRBRACKET = 58;
    private static final byte tokCOMMA = 59;
    private static final byte tokADD = 60;
    private static final byte tokSUB = 61;
    private static final byte tokUMINUS = 62;
    private static final byte tokMULT = 63;
    private static final byte tokDIV = 64;
    private static final byte tokPOWER = 65;
    private static final byte tokBITAND = 66;
    private static final byte tokBITOR = 67;
    private static final byte tokBITXOR = 68;
    private static final byte tokLOGNOT = 69;
    private static final byte tokLOGAND = 70;
    private static final byte tokLOGOR = 71;
    private static final byte tokSCREENWIDTH = 72;
    private static final byte tokSCREENHEIGHT = 73;
    private static final byte tokISCOLOR = 74;
    private static final byte tokNUMCOLORS = 75;
    private static final byte tokSTRINGWIDTH = 76;
    private static final byte tokSTRINGHEIGHT = 77;
    private static final byte tokLEFT$ = 78;
    private static final byte tokMID$ = 79;
    private static final byte tokRIGHT$ = 80;
    private static final byte tokCHR$ = 81;
    private static final byte tokSTR$ = 82;
    private static final byte tokLEN = 83;
    private static final byte tokASC = 84;
    private static final byte tokVAL = 85;
    private static final byte tokUP = 86;
    private static final byte tokDOWN = 87;
    private static final byte tokLEFT = 88;
    private static final byte tokRIGHT = 89;
    private static final byte tokFIRE = 90;
    private static final byte tokGAMEA = 91;
    private static final byte tokGAMEB = 92;
    private static final byte tokGAMEC = 93;
    private static final byte tokGAMED = 94;
    private static final byte tokDAYS = 95;
    private static final byte tokMILLISECONDS = 96;
    private static final byte tokYEAR = 97;
    private static final byte tokMONTH = 98;
    private static final byte tokDAY = 99;
    private static final byte tokHOUR = 100;
    private static final byte tokMINUTE = 101;
    private static final byte tokSECOND = 102;
    private static final byte tokMILLISECOND = 103;
    private static final byte tokRND = 104;
    private static final byte tokERR = 105;
    private static final byte tokFRE = 106;
    private static final byte tokMOD = 107;
    private static final byte tokEDITFORM = 108;
    private static final byte tokGAUGEFORM = 109;
    private static final byte tokCHOICEFORM = 110;
    private static final byte tokDATEFORM = 111;
    private static final byte tokMESSAGEFORM = 112;
    private static final byte tokLOG = 113;
    private static final byte tokEXP = 114;
    private static final byte tokSQR = 115;
    private static final byte tokSIN = 116;
    private static final byte tokCOS = 117;
    private static final byte tokTAN = 118;
    private static final byte tokASIN = 119;
    private static final byte tokACOS = 120;
    private static final byte tokATAN = 121;
    private static final byte tokABS = 122;
    private static final byte tokFOREQ = 123;
    private static final byte tokHASH = 124;
    private static final byte tokPRINTHASH = 125;
    private static final byte tokINPUTHASH = 126;
    private static final int tokCOLON = 127;
    private static final int tokGELGRAB = 128;
    private static final int tokDRAWGEL = 129;
    private static final int tokSPRITEGEL = 130;
    private static final int tokSPRITEMOVE = 131;
    private static final int tokSPRITEHIT = 132;
    private static final int tokREADDIR$ = 133;
    private static final int tokPROPERTY$ = 134;
    private static final int tokGELLOAD = 135;
    private static final int tokGELWIDTH = 136;
    private static final int tokGELHEIGHT = 137;
    //********    by Mumey    ***********
    private static final int tokPLAYWAV = 138;
    private static final int tokPLAYTONE = 139;
    private static final int tokINKEY = 140;
    private static final int tokSELECT = 141;
    private static final int tokALERT = 142;
    private static final int tokSETFONT = 143;
    private static final int tokMENUADD = 144;
    private static final int tokMENUITEM = 145;
    private static final int tokMENUREMOVE = 146;
    private static final int tokCALL = 147;
    private static final int tokENDSUB = 148;
    //*****************************************
    //********    added 02.2013    ***********
    private static final int tokREPAINT = 149;
    private static final int tokSENDSMS = 150;
    private static final int tokRAND = 151;
    private static final int tokAG = 152;
    private static final int tokCAG = 153;
    private static final int tokPLATFORMREQUEST = 154;
    private static final int tokDELGEL = 155;
    private static final int tokDELSPRITE = 156;
    private static final int tokMKDIR = 157;
    /*
     * Special Tokens
     */
    private static final int tokASSIGN = 246;
    private static final int tokMAKEREF = 247;
    private static final int tokBYTE = 248;	// -128 -> 127
    private static final int tokUBYTE = 249;	// 0 -> 255
    private static final int tokUWORD = 250;
    private static final int tokINTEGER = 251;
    private static final int tokVARIABLE = 252;
    private static final int tokSTRING = 253;
    private static final int tokFLOAT = 254;
    private static final int tokEOS = 255;

    public BASIC(Main var1, int upperLimit) {
        main = var1;
        int currentSize = upperLimit;

        while (sourceProg == null) {
            try {
                sourceProg = new byte[currentSize];
                sourceSize = currentSize;
            } catch (OutOfMemoryError e) {
                currentSize -= 256;
                if (currentSize <= 0) {
                    throw e;
                }
            }
        }

    }

    private static void PutToken(String token) {
        nextToken = token;
    }

    private static String GetToken() {
        String token = null;

        if (nextToken != null) {
            //lineOffset += nextToken.length();
            token = nextToken;
            nextToken = null;
        } else {
            while (lineOffset < lineLen) {
                if (line.charAt(lineOffset) != ' ') {
                    break;
                }
                lineOffset++;
            }

            if (lineOffset < lineLen) {
                StringBuffer sb = new StringBuffer();

                boolean forceUpperCase = false;
                boolean tokenValid = false;

                char ch = line.charAt(lineOffset++);
                if (ch == '\"') // Must be a String Constant
                {
                    boolean EscFlag = false;

                    sb.append(ch);

                    while (lineOffset < lineLen) {
                        ch = line.charAt(lineOffset++);

                        if (!EscFlag) {
                            if (ch == '\"') {
                                sb.append(ch);
                                tokenValid = true;
                                break;
                            } else if (ch == '\\') {
                                EscFlag = true;
                                continue;
                            }
                        }

                        sb.append(ch);
                        EscFlag = false;
                    }
                } else if ((ch >= '0') && (ch <= '9')) // Must be a Numeric Constant
                {
                    tokenValid = true;			// Just in case we reach EOL

                    int state = 0;

                    sb.append(ch);

                    whileLoop:
                    while (lineOffset < lineLen) {
                        ch = line.charAt(lineOffset);

                        switch (state) {
                            case -1:
                                break whileLoop;

                            case 0:				// Before Decimal Point
                                if ((ch >= '0') && (ch <= '9')) {
                                    sb.append(ch);
                                    lineOffset++;
                                } else if (ch == '.') // Decimal point?
                                {
                                    sb.append(ch);
                                    lineOffset++;
                                    state = 1;
                                } else if ((ch == 'E') || // E or e?
                                        (ch == 'e')) {
                                    tokenValid = false;
                                    sb.append('E');
                                    lineOffset++;
                                    state = 2;
                                } else {
                                    state = -1;			// 123
                                }
                                break;
                            case 1:				// After Decimal Point
                                if ((ch >= '0') && (ch <= '9')) {
                                    sb.append(ch);
                                    lineOffset++;
                                } else if ((ch == 'E') || // E or e?
                                        (ch == 'e')) {
                                    tokenValid = false;
                                    sb.append('E');
                                    lineOffset++;
                                    state = 2;
                                } else {
                                    state = -1;			// 123.[nnn]
                                }
                                break;
                            case 2:
                                if ((ch == '+') || (ch == '-')) {
                                    sb.append(ch);
                                    lineOffset++;
                                    state = 3;
                                } else {
                                    tokenValid = false;		// 123[.[nnn]]E (missing +|-)
                                    state = -1;
                                }
                                break;
                            case 3:
                                if ((ch >= '0') && (ch <= '9')) {
                                    tokenValid = true;
                                    sb.append(ch);
                                    lineOffset++;
                                    state = 4;
                                } else {
                                    tokenValid = false;		// 123[.[nnn]]E+ (missing val)
                                    state = -1;
                                }
                                break;
                            case 4:
                                if ((ch >= '0') && (ch <= '9')) {
                                    sb.append(ch);
                                    lineOffset++;
                                } else {
                                    state = -1;			// OK
                                }
                                break;
                        }
                    }
                } else if (ch == '_' || ((ch >= 'a') && (ch <= 'z')) || // Keyword | Identifier
                        ((ch >= 'A') && (ch <= 'Z'))) {
                    sb.append(ch);
                    forceUpperCase = true;
                    tokenValid = true;

                    while (lineOffset < lineLen) {
                        ch = line.charAt(lineOffset);
                        if (ch == '_' || ((ch >= 'a') && (ch <= 'z'))
                                || ((ch >= 'A') && (ch <= 'Z'))
                                || ((ch >= '0') && (ch <= '9'))) {
                            sb.append(ch);
                            lineOffset++;
                        } else if ((ch == '$') || (ch == '%')) {
                            sb.append(ch);
                            lineOffset++;
                            break;
                        } else {
                            break;
                        }
                    }
                } else // Symbol
                {
                    /*
                     * + - * / & | ^ = ( ) ,
                     * <> < <=
                     * >= >
                     */

                    //if ("+-*/&|^=(),#:".indexOf(ch) != -1)
                    if ("+-*/^=(),#:".indexOf(ch) != -1) {
                        sb.append(ch);
                        tokenValid = true;
                    } else if (ch == '<') {
                        sb.append(ch);
                        if (lineOffset < lineLen) {
                            ch = line.charAt(lineOffset);
                            if ((ch == '=') || (ch == '>')) {
                                sb.append(ch);
                                lineOffset++;
                            }
                        }
                        tokenValid = true;
                    } else if (ch == '>') {
                        sb.append(ch);
                        if (lineOffset < lineLen) {
                            ch = line.charAt(lineOffset);
                            if (ch == '=') {
                                sb.append(ch);
                                lineOffset++;
                            }
                        }
                        tokenValid = true;
                    }
                }

                if (tokenValid) {
                    token = sb.toString();
                    if ((token != null) && forceUpperCase) {
                        token = token.toUpperCase();
                    }
                }
            }
        }

        return token;
    }

    public static String ReadLine(DataInput dataInput) {
        String s = null;

        if (dataInput != null) {
            StringBuffer sb = new StringBuffer();
            int ch = -1;

            try {
                while ((ch = dataInput.readByte()) != -1) {
                    if (ch == '\n') {
                        break;
                    } else if (ch != '\r') {
                        sb.append((char) ch);
                    }
                }
            } catch (EOFException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*
             * Get string to return if length() > 0. We must also hanlde
             * the case where a '\n' was on a line by itself - in this
             * case length() would be zero yet we are not at the end of
             * file. Set return string to ""
             */

            if (sb.length() > 0) {
                s = sb.toString();
            } else {
                if (ch == '\n') {
                    s = "";
                }
            }
        }

        //System.out.println("ReadLine: " + s);

        return s;
    }

    public static boolean Enter(DataInput dataInput) {
        boolean errorFlag = false;

        if (dataInput != null) {
            String s;

            while ((s = ReadLine(dataInput)) != null) {
                //System.out.println("s=" + s);
                boolean okFlag = parseLine(s, false);
                if (!okFlag) {
                    errorFlag = true;
                    break;
                }
            }
        }

        return errorFlag;
    }

    public static void LoadFrom(DataInput dataInput) {
        try {
            if (dataInput.readInt() != MAGIC) {
                throw new BasicError(BasicError.INCOMPATIBLE_FILE_FORMAT, "Incompatible file format");
            } else {
                nvars = dataInput.readShort();

                for (int i = 0; i < nvars; ++i) {
                    varName[i] = dataInput.readUTF();
                    varType[i] = dataInput.readByte();
                    switch (varType[i]) {
//#if MB191
//#                         case TYPE_INTEGER:
//#                             varObject[i] = new int[1];
//#                             break;
//#                         case TYPE_FLOAT:
//#                             varObject[i] = new float[1];
//#                             break;
//#else
                        case TYPE_INTEGER:
                        case TYPE_FLOAT:
                            varObject[i] = new int[1];
                            break;
//#endif

                        case TYPE_STRING:
                            varObject[i] = new String[1];
                            break;
                        default:
                            throw new BasicError(BasicError.INTERNAL_ERROR, "");
                    }
                }
//#if MB191
//# 
//#                 CONST_FLOAT_INDEX = dataInput.readShort();
//#                 for (int i = 0; i < CONST_FLOAT_INDEX; ++i) {
//#                     CONSТ_FLOAT[i] = dataInput.readFloat();
//#                 }
//# 
//#endif



                sourceLen = dataInput.readUnsignedShort();
                if (sourceLen <= sourceSize) {
                    dataInput.readFully(sourceProg, 0, sourceLen);
                } else {
                    throw new BasicError(BasicError.OUT_OF_MEMORY, "Out of memory");
                }
            }
        } catch (IOException e) {
            throw new BasicError(BasicError.IO_ERROR, "");
        }
    }

    public static boolean SaveTo(DataOutput dataOutput) {
        boolean errorFlag;
        try {
            dataOutput.writeInt(MAGIC);
            dataOutput.writeShort(nvars);

            for (int var2 = 0; var2 < nvars; ++var2) {
                dataOutput.writeUTF(varName[var2]);
                dataOutput.writeByte(varType[var2]);
            }
//#if MB191
//# 
//#             dataOutput.writeShort(CONST_FLOAT_INDEX);
//#             for (int i = 0; i < CONST_FLOAT_INDEX; ++i) {
//#                 dataOutput.writeFloat(CONSТ_FLOAT[i]);
//#             }
//# 
//#endif
            dataOutput.writeShort(sourceLen);
            dataOutput.write(sourceProg, 0, sourceLen);
            errorFlag = false;
        } catch (Throwable t) {
            errorFlag = true;
        }

        return errorFlag;
    }

    private static int FindLine(int lno, byte[] searchProg, int searchLen) {
        //System.out.println("FindLine: Searching for line " + lno);

        int linePC = 0;

        while (linePC < searchLen) {
            int lineNum;
            int lineLen;

            lineNum = ((searchProg[linePC] & 0xff) << 8)
                    + (searchProg[linePC + 1] & 0xff);
            lineLen = (searchProg[linePC + 2] & 0xff);

            if (lno == lineNum) {
                //System.out.println("FindLine: Found line " + lno);
                //lastLine[lno] = linePC;
                return linePC;
            } else if (lineNum > lno) {
                return -1;
            }

            linePC += lineLen;
        }

        //System.out.println("FindLine: Line " + lno + " not found");

        return -1;
    }
    /*
     * InsertLine insert the specified line into the program.
     */

    private static void InsertLine(int lno, int len, byte[] buf) {
        RemoveLine(lno);

        int insertPos = 0;

        while (insertPos < sourceLen) {
            int lineNum;
            int lineLen;

            lineNum = ((sourceProg[insertPos] & 0xff) << 8)
                    + (sourceProg[insertPos + 1] & 0xff);
            lineLen = (sourceProg[insertPos + 2] & 0xff);

            if (lineNum > lno) {
                break;
            }

            insertPos += lineLen;
        }

        if ((sourceLen + len) <= sourceSize) {
            if (insertPos < sourceLen) {
                int src = sourceLen;
                sourceLen += len;
                int dest = sourceLen;

                while (src > insertPos) {
                    sourceProg[--dest] = sourceProg[--src];
                }
            } else {
                sourceLen += len;
            }

            buf[0] = (byte) ((lno >> 8) & 0xff);
            buf[1] = (byte) (lno & 0xff);
            buf[2] = (byte) (len);

            for (int i = 0; i < len; i++) {
                sourceProg[insertPos++] = buf[i];
            }
        } else {
            throw new BasicError(BasicError.OUT_OF_MEMORY, "Out of memory");
        }
    }

    /*
     * RemoveLine removes the specified line from the program
     */
    private static void RemoveLine(int lno) {
        int linePC = FindLine(lno, sourceProg, sourceLen);
        if (linePC != -1) {
            int dest = linePC;
            int len = (sourceProg[linePC + 2] & 0xff);
            int src = linePC + len;

            while (src < sourceLen) {
                sourceProg[dest++] = sourceProg[src++];
            }

            sourceLen = dest;
        }
    }

    public static void New() {
        sourceLen = 0;

        nvars = 0;
        for (int i = 0; i < varName.length; i++) {
            varName[i] = null;
            //varType[i] = 0;
            varObject[i] = null;
        }
//#if MB191
//#         CONST_FLOAT_INDEX = 0;
//#endif
    }

    private static int lookupToken(String var0, int var1) {
        int var2 = -1;
        int var3 = var0.length();

        for (int var4 = 0; var4 < tokenTable.length; ++var4) {
            if ((tokenTable[var4].charAt(0) & 15) == var1 || var1 == -1) {
                int var6 = tokenTable[var4].length() - 3;
                int var7 = var_38c[var4].length();
                if (var3 == var6 && var0.regionMatches(true, 0, tokenTable[var4], 3, var3) || var3 == var7 && var0.regionMatches(true, 0, var_38c[var4], 0, var3)) {
                    var2 = var4;
                    break;
                }
            }
        }

        return var2;
    }

    private static int lookupVariable(String token) {
        int variableID = -1;

        for (int i = 0; i < nvars; ++i) {
            if (token.equals(varName[i])) {
                variableID = i;
                break;
            }
        }

        if (variableID == -1 && nvars < 256) {
            varName[nvars] = token;
            if (token.endsWith("$")) {
                varType[nvars] = TYPE_STRING;
                varObject[nvars] = new String[1];
            } else if (token.endsWith("%")) {
                varType[nvars] = TYPE_INTEGER;
                varObject[nvars] = new int[1];
            } else {
                varType[nvars] = TYPE_FLOAT;
//#if MB191
//#                 varObject[nvars] = new float[1];
//#else
        varObject[nvars] = new int[1];
//#endif
            }

            variableID = nvars++;
        }

        return variableID;
    }

//#if MB191
//#     /*
//#      * метод перед созданием константы
//#      * проверяет есть ли такая уже в константах
//#      * если нет то добавляет
//#      */
//#     private static void lookupConstant(byte type, float fval, int ival, String sval) {
//#         int constantID = -1;
//# 
//#         switch (type) {
//#             case TYPE_STRING:
//#                 break;
//#             case TYPE_INTEGER:
//#                 break;
//#             case TYPE_FLOAT: {
//#                 for (int i = 0; i < CONST_FLOAT_INDEX; ++i) {
//#                     if (fval == CONSТ_FLOAT[i]) {
//#                         constantID = i;
//#                         break;
//#                     }
//#                 }
//# 
//#                 break;
//#             }
//# 
//# 
//# 
//#         }
//# 
//#         if (constantID == -1) {// если такой нет
//#             switch (type) {
//#                 case TYPE_STRING:
//#                     break;
//#                 case TYPE_INTEGER:
//#                     break;
//#                 case TYPE_FLOAT:
//#                     code[codeLen++] = (byte) CONST_FLOAT_INDEX;// индек float в константном пуле
//#                     CONSТ_FLOAT[CONST_FLOAT_INDEX++] = fval;//float сохраняю в константный пул
//#                     //System.out.println("ADD NEW CONST fval = " + fval);
//#                     break;
//# 
//# 
//#             }
//# 
//#         } else //если такая константа уже есть,просто записываю её индекс
//#         {
//#             switch (type) {
//#                 case TYPE_STRING:
//#                     break;
//#                 case TYPE_INTEGER:
//#                     break;
//#                 case TYPE_FLOAT:
//#                     code[codeLen++] = (byte) constantID;// индек float в константном пуле
//#                     //System.out.println("ADD INDEX constantID = " + constantID);
//#                     //System.out.println("nvars = "+nvars);
//#                     break;
//# 
//# 
//#             }
//# 
//#         }
//# 
//#     }
//#else

//#endif
//#if MB191
//#     public static int CONST_FLOAT_INDEX = 0;
//#     private static float CONSТ_FLOAT[] = new float[256];
//#     private static float[] operandStackValueFloat = new float[256];
//# 
//#     private static void PushOperand(float f, Object obj, byte t, byte c) {
//#         ++valueSP;
//#         operandStackValueFloat[valueSP] = f;
//#         operandStackValue[valueSP] = 0;
//#         operandStackObject[valueSP] = obj;
//#         operandStackType[valueSP] = t;
//#         operandStackClass[valueSP] = c;
//#     }
//# 
//#     private static void PushOperand(int v, Object obj, byte t, byte c) {
//#         ++valueSP;
//#         operandStackValue[valueSP] = v;
//#         operandStackObject[valueSP] = obj;
//#         operandStackType[valueSP] = t;
//#         operandStackClass[valueSP] = c;
//#     }
//#else
    private static void PushOperand(int v, Object obj, byte t, byte c) {
        ++valueSP;
        operandStackValue[valueSP] = v;
        operandStackObject[valueSP] = obj;
        operandStackType[valueSP] = t;
        operandStackClass[valueSP] = c;
    }
//#endif

    private static void PushInt(int ival) {
        PushOperand(ival, null, TYPE_INTEGER, CLASS_CONSTANT);
    }

//#if MB191
//#     private static void PushFloat(float fval) {
//#         PushOperand(fval, null, TYPE_FLOAT, CLASS_CONSTANT);
//#     }
//#else
    private static void PushFloat(int fval) {
        PushOperand(fval, null, TYPE_FLOAT, CLASS_CONSTANT);
    }
//#endif

    private static void PushString(String sval) {
        PushOperand(0, sval, TYPE_STRING, CLASS_CONSTANT);
    }

//#if MB191
//#     private static int PopInt() {
//#         float fval = operandStackValueFloat[valueSP];
//#         int ival = operandStackValue[valueSP];
//#         poppedObject = operandStackObject[valueSP];
//#         poppedType = operandStackType[valueSP];
//#         poppedClass = operandStackClass[valueSP];
//# 
//#         valueSP--;
//# 
//#         if (poppedType == TYPE_STRING) {
//#             throw new BasicError(BasicError.VALUE_ERROR, "String value unexpected");
//#         }
//# 
//# 
//#         if (poppedClass == CLASS_VARIABLE) {
//#             int var = ival & 0x000000ff;
//#             int index = (ival >> 8) & 0x00ffffff;
//# 
//#             if (poppedType == TYPE_FLOAT) {
//#                 float[] t = (float[]) varObject[var];
//#                 fval = t[index];
//#             } else {
//#                 int[] t = (int[]) varObject[var];
//#                 ival = t[index];
//#             }
//# 
//#         }
//# 
//#         if (poppedType == TYPE_FLOAT) {
//#             ival = (int) fval;
//#         } else if (poppedType != TYPE_INTEGER) {
//#             throw new BasicError(BasicError.VALUE_ERROR, "Integer required");
//#         }
//# 
//#         return ival;
//#     }
//#else
    private static int PopInt() {
        int ival = operandStackValue[valueSP];
        poppedObject = operandStackObject[valueSP];
        poppedType = operandStackType[valueSP];
        poppedClass = operandStackClass[valueSP];
        --valueSP;
        if (poppedType == TYPE_STRING) {
            throw new BasicError(BasicError.VALUE_ERROR, "String value unexpected");
        } else {
            if (poppedClass == CLASS_VARIABLE) {
                int var = ival & 255;
                int index = ival >> 8 & 16777215;
                ival = ((int[]) ((int[]) varObject[var]))[index];
            }

            if (poppedType == TYPE_FLOAT) {
                ival = Float.ftoi(ival);
            } else if (poppedType != TYPE_INTEGER) {
                throw new BasicError(BasicError.VALUE_ERROR, "Integer required");
            }

            return ival;
        }
    }
//#endif

//#if MB191
//#     private static float PopFloat() {
//#         float fval = operandStackValueFloat[valueSP];
//#         int ival = operandStackValue[valueSP];
//#         poppedObject = operandStackObject[valueSP];
//#         poppedType = operandStackType[valueSP];
//#         poppedClass = operandStackClass[valueSP];
//# 
//#         valueSP--;
//# 
//#         if (poppedType == TYPE_STRING) {
//#             throw new BasicError(BasicError.VALUE_ERROR, "String value unexpected");
//#         }
//# 
//#         if (poppedClass == CLASS_VARIABLE) {
//# 
//#             int var = ival & 0x000000ff;
//#             int index = (ival >> 8) & 0x00ffffff;
//# 
//#             if (poppedType == TYPE_INTEGER) {
//# 
//#                 int[] t = (int[]) varObject[var];
//#                 ival = t[index];
//#             } else {
//#                 float[] t = (float[]) varObject[var];
//#                 fval = t[index];
//#             }
//#         }
//# 
//# 
//#         if (poppedType == TYPE_INTEGER) {
//#             fval = ival;
//#         } else if (poppedType != TYPE_FLOAT) {
//#             throw new BasicError(BasicError.VALUE_ERROR, "Float required");
//#         }
//# 
//#         return fval;
//#     }
//#else
private static int PopFloat() {
        int ival = operandStackValue[valueSP];
        poppedObject = operandStackObject[valueSP];
        poppedType = operandStackType[valueSP];
        poppedClass = operandStackClass[valueSP];
        --valueSP;
        if (poppedType == TYPE_STRING) {
            throw new BasicError(BasicError.VALUE_ERROR, "String value unexpected");
        } else {
            if (poppedClass == CLASS_VARIABLE) {
                int var = ival & 255;
                int index = ival >> 8 & 16777215;
                ival = ((int[]) ((int[]) varObject[var]))[index];
            }

            if (poppedType == TYPE_INTEGER) {
                ival = Float.itof(ival);
            } else if (poppedType != TYPE_FLOAT) {
                throw new BasicError(BasicError.VALUE_ERROR, "Float required");
            }

            return ival;
        }
    }
//#endif

    private static String PopString() {
        int ival = operandStackValue[valueSP];
        poppedObject = operandStackObject[valueSP];
        poppedType = operandStackType[valueSP];
        poppedClass = operandStackClass[valueSP];
        --valueSP;
        if (poppedType != TYPE_STRING) {
            throw new BasicError(BasicError.VALUE_ERROR, "String required");
        } else {
            if (poppedClass == CLASS_VARIABLE) {
                int var = ival & 255;
                int index = ival >> 8 & 16777215;
                poppedObject = ((String[]) ((String[]) varObject[var]))[index];
            }

            return (String) poppedObject;
        }
    }

    private static int PopType(int offsetFromTop) {
        return operandStackType[valueSP - offsetFromTop];
    }

    /*
     * v 0..255
     * type INTEGER(0), FLOAT(1), STRING(2)
     * index
     */
    private static int PopRef() {
        if (valueSP == -1) {
            throw new BasicError(-1, "Operand stack underflow");
        } else {
            int ref = operandStackValue[valueSP];
            byte t = operandStackType[valueSP];
            byte c = operandStackClass[valueSP];
            --valueSP;
            if (c != CLASS_VARIABLE) {
                throw new BasicError(BasicError.INTERNAL_ERROR, "Variable expected on operand stack");
            } else if (t != varType[ref & 255]) {
                throw new BasicError(BasicError.INTERNAL_ERROR, "Type mismatch between stack type and physical type");
            } else {
                return ref;
            }
        }
    }
    /*
     * An RValue is an Expression
     */
    private static final int EXPR_END = 0x100;
    private static final int EXPR_UNARY = 0x80;
    private static final int EXPR_BINARY = 0x40;
    private static final int EXPR_LBRACKET = 0x20;
    private static final int EXPR_RBRACKET = 0x10;
    private static final int EXPR_COMMA = 0x08;
    private static final int EXPR_FUNCTION = 0x04;
    private static final int EXPR_LVALUE = 0x02;
    private static final int EXPR_CONSTANT = 0x01;

    private static void AddOperator(int oper, int onStack, int offStack) {
        if (onStack != -1) {
            for (boolean leftBracketPopped = false; operatorSP >= 0 && operatorPrior[operatorSP] >= onStack; Execute(operatorStack[operatorSP--])) {
                if (operatorStack[operatorSP] == 57) {
                    if (leftBracketPopped) {
                        break;
                    }

                    leftBracketPopped = true;
                } else if (leftBracketPopped && operatorPrior[operatorSP] != 2) {
                    break;
                }
            }
        }

        if (oper != 255 && oper != 127) {
            ++operatorSP;
            operatorStack[operatorSP] = oper;
            operatorPrior[operatorSP] = offStack;
        }

    }

    /*
     * An LValue is an IDENTIFIER followed by an optional index
     */
    static boolean parseLValue() {
        boolean validFlag = false;
        boolean arrayFlag = false;

        String token = GetToken();
        if (token != null) {
            char ch = token.charAt(0);

            if ((ch >= 'A') && (ch <= 'Z')) {
                int toknum = lookupToken(token, 1);
                if (toknum == -1) {
                    code[codeLen++] = (byte) tokVARIABLE;
                    code[codeLen++] = (byte) lookupVariable(token);

                    token = GetToken();
                    if ((token != null) && (token.compareTo("(") == 0)) {
                        code[codeLen++] = (byte) tokMAKEREF;


                        code[codeLen++] = (byte) tokLBRACKET;
                        parseRValue(true);


                        token = GetToken();


                        if ((token != null) && (token.compareTo(")") == 0)) {
                            code[codeLen++] = (byte) tokRBRACKET;
                            arrayFlag = true;
                            validFlag = true;
                        }
                    } else {
                        PutToken(token);
                        validFlag = true;
                    }
                }
            }
        }

        if (!validFlag) {
            throw new BasicError(BasicError.LVALUE_EXPECTED, "LVALUE Expected");
        }

        return arrayFlag;
    }

    private static void parseRValue(boolean commaValid) {
        //int currentTokenType = 0x0000;
        char validTokenTypes = EXPR_UNARY | EXPR_LBRACKET | EXPR_FUNCTION | EXPR_LVALUE | EXPR_CONSTANT;
        int bracketLevel = 0;
        boolean[] commaValidStack = new boolean[16];

        String token;
        while ((token = GetToken()) != null) {
            char ch;
            int len;
            if ((ch = token.charAt(0)) == '\"') {
                if ((EXPR_CONSTANT & validTokenTypes) == 0) {
                    PutToken(token);
                    break;
                }

                code[codeLen++] = (byte) tokSTRING;
                len = token.length() - 2;
                code[codeLen++] = (byte) len;

                for (int i = 0; i < len; ++i) {
                    char var11;
                    if ((var11 = token.charAt(1 + i)) >= 1040 && var11 <= 1103) {
                        var11 = (char) (var11 - 848); // еще трабла с win1251
                    }

                    code[codeLen++] = (byte) var11;
                }

                validTokenTypes = EXPR_BINARY | EXPR_RBRACKET | EXPR_COMMA | EXPR_END;
            } else if (ch >= '0' && ch <= '9') {
                if ((EXPR_CONSTANT & validTokenTypes) == 0) {
                    PutToken(token);
                    break;
                }

                try {
                    int tokval = Integer.parseInt(token);
                    if ((tokval >= -128) && (tokval < 128)) {
                        code[codeLen++] = (byte) tokBYTE;
                        code[codeLen++] = (byte) tokval;
                    } else if (tokval >= 0 && tokval < 256) {
                        code[codeLen++] = (byte) tokUBYTE;
                        code[codeLen++] = (byte) tokval;
                    } else if (tokval >= 0 && tokval < 65536) {
                        code[codeLen++] = (byte) tokUWORD;
                        code[codeLen++] = (byte) (tokval >> 8 & 255);
                        code[codeLen++] = (byte) (tokval & 255);
                    } else {
                        code[codeLen++] = (byte) tokINTEGER;
                        code[codeLen++] = (byte) (tokval >> 24 & 255);
                        code[codeLen++] = (byte) (tokval >> 16 & 255);
                        code[codeLen++] = (byte) (tokval >> 8 & 255);
                        code[codeLen++] = (byte) (tokval & 255);
                    }
                } catch (NumberFormatException var10) {
                    try {
//#if MB191
//#                         float fval = java.lang.Float.parseFloat(token);
//#                         code[codeLen++] = (byte) tokFLOAT;
//#                         lookupConstant(TYPE_FLOAT, fval, 0, "");
//# 
//#else
                       int var8 = Float.fromString(token);
                        code[codeLen++] = (byte)tokFLOAT;
                        code[codeLen++] = (byte) (var8 >> 24 & 255);
                        code[codeLen++] = (byte) (var8 >> 16 & 255);
                        code[codeLen++] = (byte) (var8 >> 8 & 255);
                        code[codeLen++] = (byte) (var8 & 255);
//#endif

                    } catch (Exception var9) {
                        throw new BasicError(BasicError.VALUE_ERROR, "Bad Constant2: " + token);
                    }
                }

                validTokenTypes = 344;
            } else {
                if ((len = lookupToken(token, 1)) != -1) {
                    label123:
                    {
                        if ((ch = token.charAt(0)) == '(') {
                            commaValidStack[bracketLevel++] = commaValid;
                            commaValid = true;
                        } else if (ch == ')') {
                            if (bracketLevel == 0) {
                                break label123;
                            }

                            --bracketLevel;
                            commaValid = commaValidStack[bracketLevel];
                        } else {
                            if (!commaValid && ch == 44) {
                                break label123;
                            }

                            if (ch == 45 && (validTokenTypes & 128) != 0) {
                                ++len;
                            }
                        }

                        if ((tokenTable[len].charAt(1) & validTokenTypes) != 0) {
                            code[codeLen++] = (byte) len;
                            validTokenTypes = tokenTable[len].charAt(2);
                            continue;
                        }
                    }
                } else if (ch >= 65 && ch <= 90) {
                    PutToken(token);
                    if ((2 & validTokenTypes) == 0) {
                        break;
                    }

                    parseLValue();
                    validTokenTypes = 344;
                    continue;
                }

                PutToken(token);
                break;
            }
        }

        if (bracketLevel != 0) {
            throw new BasicError(BasicError.PARENTHESIS_NESTING_ERROR, "Parenthesis Nesting Error");
        } else if ((256 & validTokenTypes) == 0) {
            throw new BasicError(BasicError.EXPRESSION_INCOMPLETE, "End of Expression not Expected");
        }
    }

    private static void parseCommaList(String commaList) {
        for (int i = 0; i < commaList.length(); ++i) {
            switch (commaList.charAt(i)) {
                case 35:
                    String token;
                    if ((token = GetToken()) == null) {
                        throw new BasicError(BasicError.HASH_EXPECTED, "# expected");
                    }

                    if (token.compareTo("#") != 0) {
                        throw new BasicError(BasicError.HASH_EXPECTED, "# expected");
                    }

                    code[codeLen++] = 124;
                    break;
                case 44:
                    String var3;
                    if ((var3 = GetToken()) == null) {
                        throw new BasicError(BasicError.COMMA_EXPECTED, "Comma expected");
                    }

                    if (var3.compareTo(",") != 0) {
                        throw new BasicError(BasicError.COMMA_EXPECTED, "Comma expected");
                    }

                    code[codeLen++] = 59;
                    break;
                case 76:
                    parseLValue();
                    break;
                case 82:
                    parseRValue(false); // Don't parse commas at outer bracket level
                    break;
                default:
                    throw new BasicError(BasicError.INTERNAL_ERROR, "Bad item in comma list");
            }
        }

    }

    private static void parseStatement() {
        boolean statementRequired = false; // A statement is only required following an IF or COLON

        String token;
        while ((token = GetToken()) != null) {
            statementRequired = false;
            int keyword;
            if ((keyword = lookupToken(token, 0)) != -1) {
                if (keyword == tokPRINT) {
                    if ((token = GetToken()) != null) {
                        if (token.compareTo("#") == 0) {
                            keyword = tokPRINTHASH;
                        }

                        PutToken(token);
                    }
                } else if (keyword == tokINPUT && (token = GetToken()) != null) {
                    if (token.compareTo("#") == 0) {
                        keyword = tokINPUTHASH;
                    }

                    PutToken(token);
                }

                code[codeLen++] = (byte) keyword;
                switch (keyword) {
                    case tokDIM:
                    case tokNEXT:
                    case tokREAD:
                        parseLValue();
                        break;
                    case tokOPEN:
                        parseCommaList("#R,R,R");
                        break;
                    case tokSENDSMS:
                    case tokRAND:
                    case tokAG:
                    case tokPLOT:
                    case tokSPRITEGEL:
                    case tokGELLOAD:
                    case tokPLAYTONE:
                        parseCommaList("R,R");
                        break;
                    case tokCAG:
                    case tokGELGRAB:
                    case tokALERT:
                        parseCommaList("R,R,R,R,R");
                        break;
                    case tokCLOSE:
                        parseCommaList("#R");
                        break;
                    case tokPOINT:
                    case tokPRINTHASH:
                    case tokPUT:
                        parseCommaList("#R,R");
                        break;
                    case tokNOTE:
                    case tokGET:
                    case tokINPUTHASH:
                        parseCommaList("#R,L");
                        break;
                    case tokDRAWSTRING:
                    case tokSETCOLOR:
                    case tokDRAWGEL:
                    case tokSPRITEMOVE:
                    case tokMENUADD:
                        parseCommaList("R,R,R");
                        break;
                    case tokDRAWLINE:
                    case tokFILLRECT:
                    case tokDRAWRECT:
                        parseCommaList("R,R,R,R");
                        break;
                    case tokFILLROUNDRECT:
                    case tokDRAWROUNDRECT:
                    case tokFILLARC:
                    case tokDRAWARC:
                    case tokBLIT:
                        parseCommaList("R,R,R,R,R,R");
                        break;
                    case tokREM:
                    case tokDATA:
                        while (lineOffset < lineLen && line.charAt(lineOffset) == 32) {
                            ++lineOffset;
                        }

                        if ((token = line.substring(lineOffset)) != null) {
                            token = token.trim();
                        }

                        lineOffset = lineLen;
                        if (token == null || token.length() == 0) {
                            if (keyword != 14) {
                                throw new BasicError(BasicError.SYNTAX_ERROR, "No data on line!");
                            }

                            token = "";
                        }

                        int var3 = token.length();
                        code[codeLen++] = (byte) (var3 & 255);

                        for (int var4 = 0; var4 < var3; ++var4) {
                            char var5;
                            if ((var5 = token.charAt(var4)) >= 1040 && var5 <= 1103) {
                                var5 = (char) (var5 - 848);
                            }

                            code[codeLen++] = (byte) var5;
                        }
                        break;
                    case tokIF:
                        parseRValue(false);
                        if ((token = GetToken()) != null && (token.compareTo("THEN") == 0 || token.compareTo("TH") == 0)) {
                            code[codeLen++] = 17;
                            statementRequired = true;
                            continue;
                        }

                        throw new BasicError(BasicError.SYNTAX_ERROR, "THEN expected");
                    case tokPRINT:
                    case tokGOTO:
                    case tokGOSUB:
                    case tokSLEEP:
                    case tokTRAP:
                    case tokRESTORE:
                    case tokENTER:
                    case tokLOAD:
                    case tokSAVE:
                    case tokDELETE:
                    case tokEDIT:
                    case tokPLAYWAV:
                    case tokSETFONT:
                    case tokMENUREMOVE:
                    case tokCALL:
                    case tokPLATFORMREQUEST:
                    case tokDELGEL:
                    case tokDELSPRITE:
                        parseRValue(false);
                        break;
                    case tokLIST:
                        if ((token = GetToken()) != null) {
                            PutToken(token);
                            parseRValue(false);
                            if ((token = GetToken()) != null) {
                                if (token.compareTo(",") != 0) {
                                    throw new BasicError(BasicError.COMMA_EXPECTED, "Comma expected");
                                }

                                code[codeLen++] = 59;
                                parseRValue(false);
                            }
                        }
                        break;
                    case tokINPUT:
                        parseRValue(false);
                        if ((token = GetToken()) == null || token.compareTo(",") != 0) {
                            throw new BasicError(BasicError.COMMA_EXPECTED, "Comma expected");
                        }

                        code[codeLen++] = 59;
                        parseLValue();
                        break;
                    case tokFOR:
                        parseLValue();
                        if ((token = GetToken()) == null || token.compareTo("=") != 0) {
                            throw new BasicError(BasicError.SYNTAX_ERROR, "Assignment expected");
                        }

                        code[codeLen++] = 123;
                        parseRValue(false);
                        if ((token = GetToken()) == null || token.compareTo("TO") != 0) {
                            throw new BasicError(BasicError.SYNTAX_ERROR, "TO expected");
                        }

                        code[codeLen++] = 31;
                        parseRValue(false);
                        if ((token = GetToken()) != null) {
                            if (token.compareTo("STEP") == 0) {
                                code[codeLen++] = 32;
                                parseRValue(false);
                            } else {
                                PutToken(token);
                            }
                        }
                        break;
                }
            } else {
                PutToken(token);
                parseLValue();
                if (GetToken().compareTo("=") != 0) {
                    throw new BasicError(BasicError.SYNTAX_ERROR, "Assignment expected");
                }

                code[codeLen++] = -10;
                parseRValue(false);
            }

            if ((token = GetToken()) != null) {
                if (token.compareTo(":") != 0) {
                    throw new BasicError(BasicError.SYNTAX_ERROR, "Colon expected");
                }

                statementRequired = true;
                code[codeLen++] = 127;
            }
        }

        if (statementRequired) {
            throw new BasicError(BasicError.SYNTAX_ERROR, "Statement expected");
        }
    }

    public static boolean parseLine(String var0, boolean var1) {
        line = var0;
        lineLen = var0.length();
        lineOffset = 0;
        nextToken = null;

        boolean var2;
        try {
            String var3 = GetToken();

            int var4;
            try {
                var4 = Integer.parseInt(var3);
            } catch (NumberFormatException var6) {
                PutToken(var3);
                var4 = -1;
            }

            codeLen = 3;
            parseStatement();
            String var5;
            if ((var5 = GetToken()) != null) {
                throw new BasicError(BasicError.SYNTAX_ERROR, "Trailing Junk: " + var5);
            }

            if (codeLen > 3) {
                code[codeLen++] = -1;
                code[2] = (byte) (codeLen & 255);
                if (var4 != -1) {
                    InsertLine(var4, codeLen, code);
                } else {
                    RunProgram(code, codeLen);
                }
            } else if (var4 != -1) {
                RemoveLine(var4);
            }

            var2 = true;
        } catch (BasicError var7) {
            main.Error("Error " + var7.errorNumber + ": " + var7.getMessage() + " near " + line.substring(lineOffset - 1));
            if (var7.errorNumber != 23 && var1) {
                var2 = parseLine(main.GetLine("Correct>", line), var1);
            } else {
                var2 = false;
            }
        } catch (RuntimeException var8) {
            main.Error("Error: " + var8.getMessage() + " near " + line.substring(lineOffset - 1));
            var2 = false;
        }

        return var2;
    }

    private static void List(DataOutput dataOutput, int lno1, int lno2, boolean editFlag) {
        int progPC;
        for (int i = 0; i < sourceLen; i = progPC) {
            int var6 = i;
            int var7 = ((sourceProg[i++] & 255) << 8) + (sourceProg[i++] & 255);
            int var8 = sourceProg[i++] & 255;
            progPC = var6 + var8;
            if (var7 >= lno1 && var7 <= lno2) {
                StringBuffer var9;
                (var9 = new StringBuffer()).append(Integer.toString(var7));
                boolean var10 = true;

                int var12;
                while (i < progPC) {
                    int var11;
                    if ((var11 = sourceProg[i++] & 255) < tokenTable.length && (tokenTable[var11].charAt(0) & 128) == 128) {
                        var10 = true;
                    }

                    if (var10) {
                        var9.append(' ');
                        var10 = false;
                    }

                    if (var11 < tokenTable.length && (tokenTable[var11].charAt(0) & 64) == 64) {
                        var10 = true;
                    }

                    if (var11 == 249) {
                        var12 = sourceProg[i++] & 255;
                        var9.append(var12);
                    } else if (var11 == 248) {
                        byte var17 = sourceProg[i++];
                        var9.append(var17);
                    } else if (var11 == 250) {
                        var12 = ((sourceProg[i++] & 255) << 8) + (sourceProg[i++] & 255);
                        var9.append(var12);
                    } else if (var11 == 251) {
                        var12 = ((sourceProg[i++] & 255) << 24) + ((sourceProg[i++] & 255) << 16) + ((sourceProg[i++] & 255) << 8) + (sourceProg[i++] & 255);
                        var9.append(var12);
                    } else if (var11 == 254) {
//#if MB191
//#                         float fval = CONSТ_FLOAT[sourceProg[i++] & 0xff];// float получаю из константного пула
//#                         var9.append(java.lang.Float.toString(fval));
//#else
                        var12 = ((sourceProg[i++] & 255) << 24) + ((sourceProg[i++] & 255) << 16) + ((sourceProg[i++] & 255) << 8) + (sourceProg[i++] & 255);
                        var9.append(Float.toString(var12));
//#endif

                    } else if (var11 == 252) {
                        var12 = sourceProg[i++] & 255;
                        var9.append(varName[var12]);
                    } else {
                        int var13;
                        char var14;
                        if (var11 != 253) {
                            if (var11 != 14 && var11 != 48) {
                                if (var11 == 246) {
                                    var9.append("=");
                                } else if (var11 != 255 && var11 < tokenTable.length) {
                                    var9.append(tokenTable[var11].substring(3));
                                }
                            } else {
                                var9.append(tokenTable[var11].substring(3));
                                var9.append(' ');
                                var12 = sourceProg[i++] & 255;

                                for (var13 = 0; var13 < var12; ++var13) {
                                    if ((var14 = (char) (sourceProg[i++] & 255)) >= 192 && var14 <= 255) {
                                        var14 = (char) (var14 + 848);
                                    }

                                    var9.append(var14);
                                }
                            }
                        } else {
                            var9.append("\"");
                            var12 = sourceProg[i++] & 255;

                            for (var13 = 0; var13 < var12; ++var13) {
                                if ((var14 = (char) (sourceProg[i++] & 255)) >= 192 && var14 <= 255) {
                                    var14 = (char) (var14 + 848);
                                }

                                var9.append(var14);
                            }

                            var9.append("\"");
                        }
                    }
                }

                if (!editFlag) {
                    var9.append('\n');
                }

                String var16 = var9.toString();
                if (editFlag) {
                    parseLine(main.GetLine("Edit>", var16), true);
                    return;
                }

                if (dataOutput != null) {
                    try {
                        int var13;
                        for (var12 = 0; var12 < var16.length(); ++var12) {
                            if (((var13 = var16.charAt(var12)) >= 'А') && (var13 <= 1103)) {
                                var13 = (char) (var13 - 848);
                            }
                            dataOutput.writeByte((byte) var13);
                        }
                    } catch (IOException var15) {
                        throw new BasicError(BasicError.IO_ERROR, "I/O Error");
                    }
                } else {
                    main.PrintString(var16);
                }

                if (i != progPC) {
                    throw new BasicError(BasicError.INTERNAL_ERROR, "List: Internal Error Line " + var7);
                }
            }
        }

    }

    public static void StopProgram() {
        stopProgramFlag = true;
    }

    private static void RunProgram(byte[] code, int codeLen) {
        int lineNum = 0;
        degFlag = false;
        exeProg = code;
        exeLen = codeLen;
        exePC = 0;
        trapPC = -1;
        lastError = 0;
        dataPC = 0;
        dataOffset = -1;
        dataLen = -1;
        valueSP = -1;       // Clear operand stack
        operatorSP = -1;    // Clear operator stack
        controlSP = -1;     // Clear control stack
        dirEnum = null;
        stopProgramFlag = false;


        while (exePC < exeLen) {
            try {
                lineNum = ((exeProg[exePC++] & 255) << 8) + (exeProg[exePC++] & 255);
                int lineLen = exeProg[exePC++] & 255;
                exeNextLinePC = exePC + lineLen - 3;

                while (exePC < exeNextLinePC) {
                    if (stopProgramFlag) {
                        main.CloseAllFiles();
                        main.Message("Stopped at line " + lineNum);
                        return;
                    }

                    int opcode;
                    switch (opcode = exeProg[exePC++] & 255) {
                        case tokREM:
                        case tokDATA:
                            exePC = exeNextLinePC;
                            break;
                        case tokASSIGN:
                            AddOperator(opcode, 1, 1);
                            break;
                        case tokMAKEREF:
                            AddOperator(opcode, 15, 2);
                            break;
                        case tokBYTE:
                            PushOperand(exeProg[exePC++], (Object) null, (byte) 0, (byte) 0);
                            break;
                        case tokUBYTE:
                            PushOperand(exeProg[exePC++] & 255, (Object) null, (byte) 0, (byte) 0);
                            break;
                        case tokUWORD:
                            PushOperand(((exeProg[exePC++] & 255) << 8) + (exeProg[exePC++] & 255), (Object) null, (byte) 0, (byte) 0);
                            break;
                        case tokINTEGER:
                            PushOperand(((exeProg[exePC++] & 255) << 24) + ((exeProg[exePC++] & 255) << 16) + ((exeProg[exePC++] & 255) << 8) + (exeProg[exePC++] & 255), (Object) null, (byte) 0, (byte) 0);
                            break;
                        case tokVARIABLE:
                            int var5;
                            PushOperand(var5 = exeProg[exePC++] & 255, (Object) null, varType[var5], (byte) 1);
                            break;
                        case tokSTRING:
                            StringBuffer var6 = new StringBuffer();
                            int var7 = exeProg[exePC++] & 255;

                            for (int var8 = 0; var8 < var7; ++var8) {
                                char var9;
                                if ((var9 = (char) (exeProg[exePC++] & 255)) >= 192 && var9 <= 255) {
                                    var9 = (char) (var9 + 848);
                                }

                                var6.append(var9);
                            }

                            PushOperand(0, var6.toString(), (byte) 2, (byte) 0);
                            break;
                        case tokFLOAT:
//#if MB191
//#                             float fval = CONSТ_FLOAT[exeProg[exePC++] & 0xff];
//#                             PushOperand(fval, null, TYPE_FLOAT, CLASS_CONSTANT);
//#else
                            PushOperand(((exeProg[exePC++] & 255) << 24) + ((exeProg[exePC++] & 255) << 16) + ((exeProg[exePC++] & 255) << 8) + (exeProg[exePC++] & 255), (Object) null, (byte) 1, (byte) 0);
//#endif

                            break;
                        case tokEOS:
                            AddOperator(opcode, 0, 0);
                            commaCount = 0;
                            break;
                        default:
                            char var14;
                            int var15 = ((var14 = tokenTable[opcode].charAt(0)) & '\uf000') >> 12;
                            int var10 = (var14 & 3840) >> 8;
                            AddOperator(opcode, var15, var10);
                    }
                }

                if (exePC != exeNextLinePC) {
                    throw new BasicError(BasicError.INTERNAL_ERROR, "Internal Error: exePC <> exeNextLinePC");
                }

//#if MB191
//# 
//#else
               Thread.yield();
//#endif

            } catch (BasicError var11) {
                if (trapPC == -1) {
                    main.Error("Error: " + var11.getMessage() + " at line " + lineNum);
                    break;
                }

                valueSP = -1;
                operatorSP = -1;
                lastError = var11.errorNumber;
                exePC = trapPC;
                trapPC = -1;
            } catch (Exception var12) {
                Class var4 = var12.getClass();
                main.Error("Error: " + var4.getName() + " at line " + lineNum);
                break;
            }
        }

        main.CloseAllFiles();
        if (stopProgramFlag) {
            main.Message("Stopped at line " + lineNum);
        }

    }

    public static void sub_750(String var0) {
        line = var0;
        lineLen = var0.length();
        lineOffset = 0;
        nextToken = null;

        String var1;
        label42:
        while ((var1 = GetToken()) != null) {
            if (var1.equals("")) {
                return;
            }

            if (!var1.equals(":")) {
                int var2 = -1;
                int var3 = 0;

                while (true) {
                    if (var3 < tokenTable.length) {
                        if (!var1.equals(tokenTable[var3].substring(3))) {
                            ++var3;
                            continue;
                        }

                        var2 = var3;
                    }

                    if (var2 != -1) {
                        switch (var2) {
                            case tokINPUT:
                            case tokEDITFORM:
                            case tokGAUGEFORM:
                            case tokCHOICEFORM:
                            case tokDATEFORM:
                            case tokMESSAGEFORM:
                            case tokSELECT:
                            case tokALERT:
                                continue label42;
                            case tokGELLOAD:
                            case tokGELGRAB:
                            case tokDRAWGEL:
                            case tokSPRITEGEL:
                            case tokSPRITEMOVE:
                            case tokSPRITEHIT:
                            case tokGELWIDTH:
                            case tokGELHEIGHT:
                                continue label42;
                            case tokPLAYWAV:
                            case tokPLAYTONE:
                                continue label42;
                            case tokCALL:
                            case tokDELETE:
                            case tokOPEN:
                            case tokREADDIR$:
                        }
                    }
                    break;
                }
            }
        }

        /*
        // Float11.class функции
        
        tokPOWER 
        tokLOG
        tokEXP
        tokASIN
        tokACOS
        tokATAN 
         */


    }
    private static final boolean DEVELOPMENT_MODE = false;
    private static final boolean DEBUG = false;
    private static final long MS_PER_DAY = (24 * 60 * 60 * 1000);

    private static void Execute(int oper) {

        if (DEBUG) {
            System.out.print("Executing( " + oper + ") : ");
            if (oper < tokenTable.length) {
                System.out.println(tokenTable[oper].substring(3));
            } else {
                System.out.println(oper);
            }
            //DumpOperandStack();
        }

        switchStatement:
        switch (oper) {
            case tokAG:
                int i = PopInt();
                String name = PopString();
                main.AlphaGel(name, i);
                break;
            case tokCAG:
                int b = PopInt();
                int g = PopInt();
                int r = PopInt();
                int a = PopInt();
                String name1 = PopString();
                main.ColorAlphaGel(name1, a, r, g, b);
                break;
            case tokREPAINT:
                main.canvas.repaint();
                main.canvas.serviceRepaints();
                break;
            case tokSENDSMS:
                String text1 = PopString();
                String number = PopString();
                PushInt(main.sendSms(number, text1));
                break;
            case tokRAND:
                int max = PopInt();
                int min = PopInt();
                PushInt(min + ((random.nextInt() >>> 1) % (max - min)));
                break;
            case tokPLATFORMREQUEST: // PFR "http://site.ru"
                try {
                    main.platformRequest(PopString());
                } catch (ConnectionNotFoundException ex) {
                    ex.printStackTrace();
                }
                break;
            case tokDELGEL:
                main.DelGel(PopString());
                break;
            case tokDELSPRITE:
                main.DelSprite(PopString());
                break;
            case tokSTOP:                            // Value stack: null
                if (DEBUG) {
                    System.out.println("STOP");
                }
                stopProgramFlag = true;
                break;
            case tokMKDIR:
                try {
                    FileConnection folder = (FileConnection) Connector.open(PopString());
                    if (!folder.exists()) {
                        folder.mkdir();
                        folder.close();
                        PushInt(1);
                        return;
                    }
                } catch (IOException ex) {
                }
                PushInt(0);
                break;

            case tokEND:                             // Value stack: null
                if (DEBUG) {
                    System.out.println("END");
                }
                exePC = exeLen;
                exeNextLinePC = exeLen;
                break;

            case tokNEW:                             // Value stack: null
                New();
                exePC = exeLen;
                exeNextLinePC = exeLen;
                break;

            case tokRUN:
                exeProg = sourceProg;
                exeLen = sourceLen;
                exePC = 0;
                exeNextLinePC = 0;
                break;

            case tokDIR: {
                Enumeration directoryEnumeration = main.Directory("*");

                if (directoryEnumeration != null) {
                    while (directoryEnumeration.hasMoreElements()) {
                        main.PrintString((String) directoryEnumeration.nextElement() + "\n");
                    }
                }
                /*
                String[] filename = support.Directory("*");

                if (filename != null)
                for (int i=0;i<filename.length;i++)
                if (filename[i].charAt(0) != '.')
                support.PrintString(filename[i] + "\n");
                 */
                break;
            }

            case tokREADDIR$: {

                String filter = PopString();

                if (filter.length() > 0) {
                    dirEnum = main.Directory(filter);
                }

                if ((dirEnum != null) && (dirEnum.hasMoreElements())) {
                    PushString((String) dirEnum.nextElement());
                } else {
                    PushString("");
                }

                break;
            }

            case tokPROPERTY$: {

                String property = PopString();

                String value = System.getProperty(property);
                if (value != null) {
                    PushString(value);
                } else {
                    PushString("");
                }

                break;
            }

            case tokDEG:
                degFlag = true;
                break;

            case tokRAD:
                degFlag = false;
                break;

            case tokBYE:
                main.Bye();
                exePC = exeLen;
                exeNextLinePC = exeLen;
                break;

            case tokRETURN:
            case tokPOP: {
                whileLoop:
                while (controlSP >= 0) {
                    int controlItem = ((Integer) controlStack[controlSP--]).intValue();
                    if (controlItem == CONTROL_RETURN) {
                        if (oper == tokRETURN) {
                            exeNextLinePC = ((Integer) controlStack[controlSP--]).intValue();
                            exePC = ((Integer) controlStack[controlSP--]).intValue();
                            exeLen = ((Integer) controlStack[controlSP--]).intValue();
                            exeProg = (byte[]) controlStack[controlSP--];
                        } else {
                            controlSP--;                          // exeNextLinePC
                            controlSP--;                          // exePC
                            controlSP--;                          // exeLen
                            controlSP--;                          // exeProg
                        }
                        break switchStatement;
                    } else if (controlItem == CONTROL_FORLOOP) {
                        controlSP--;                              // stepValue
                        controlSP--;                              // toValue
                        controlSP--;                              // controlVariable
                        controlSP--;                          // exeNextLinePC
                        controlSP--;                          // exePC
                        controlSP--;                          // exeLen
                        controlSP--;                          // exeProg

                        if (oper == tokPOP) {
                            break switchStatement;
                        }
                    } else {
                        throw new BasicError(BasicError.INTERNAL_ERROR, "Bad item on control stack");
                    }
                }
                throw new BasicError(BasicError.STACK_EMPTY, "Empty stack");

            }

            case tokGOSUB: {
                controlStack[++controlSP] = exeProg;
                controlStack[++controlSP] = new Integer(exeLen);
                controlStack[++controlSP] = new Integer(exePC);
                controlStack[++controlSP] = new Integer(exeNextLinePC);
                controlStack[++controlSP] = new Integer(CONTROL_RETURN);

            }

            case tokGOTO: {
                int ival = PopInt();
                exeProg = sourceProg;
                exeLen = sourceLen;
                exePC = FindLine(ival, exeProg, exeLen);
                if (exePC == -1) {
                    throw new BasicError(BasicError.LINE_NOT_FOUND, "Line " + ival + " not found");
                }
                exeNextLinePC = exePC;
                break;
            }
            /*
            case tokJUMP :
            {
            int exePC = PopInt();
            exeNextLinePC = exePC;
            break;
            }
             */
            case tokSLEEP: {
                int ival = PopInt();

                if (ival > 0) {
                    try {
                        Thread.sleep(ival);
                    } catch (InterruptedException e) {
                    }
                } else {
                    Thread.yield();
                }
            }
            break;

            case tokPRINT: {
                //if (DEBUG)
                //{
                //  System.out.print("PRINT> ");
                //  DumpOperandStack();
                //}

                StringBuffer sb = new StringBuffer();

                int popType = PopType(0);

                if (popType == TYPE_INTEGER) {
                    int ival = PopInt();
                    sb.insert(0, ival);
                } else if (popType == TYPE_FLOAT) {
//#if MB191
//#                     float fval = PopFloat();
//#                     sb.insert(0, java.lang.Float.toString(fval));
//#else
              int fval = PopFloat();
            sb.insert(0, Float.toString(fval));
//#endif

                } else if (popType == TYPE_STRING) {
                    String sval = PopString();
                    sb.insert(0, sval);
                }

                sb.append("\n");

                main.PrintString(sb.toString());

                if (DEVELOPMENT_MODE) {
                    System.out.println(sb.toString());
                }
                break;
            }

            case tokREM:
                break;

            case tokDIM: // Value stack: Variable Dimension
            {
                //if (DEBUG)
                //{
                //    System.out.print("DIM> ");
                //    DumpOperandStack();
                //}

                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 8) & 0x00ffffff;

                if (DEBUG) {
                    System.out.println("var = " + var);
                    System.out.println("index = " + index);
                }

                switch (varType[var]) {
                    case TYPE_INTEGER:
                        varObject[var] = new int[index];
                        break;
                    case TYPE_FLOAT:
//#if MB191
//#                         varObject[var] = new float[index];
//#else
                    varObject[var] = new int[index];
//#endif
                        break;
                    case TYPE_STRING:
                        varObject[var] = new String[index];
                        break;
                    default:
                        throw new BasicError(BasicError.INTERNAL_ERROR, "DIM: Bad Variable Type");
                }
                break;
            }

            case tokIF: {
                int result = PopInt();
                if (result == 0) {
                    exePC = exeNextLinePC;
                }
                break;
            }

            case tokTHEN:
                break;

            case tokCOLON:
                break;

            case tokCLS:
                main.CLS();
                break;

            case tokPLOT: {
                int n2 = PopInt();
                int n1 = PopInt();
                main.DrawLine(n1, n2, n1, n2);
                break;
            }

            case tokDRAWLINE: {
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.DrawLine(n1, n2, n3, n4);
                break;
            }

            case tokFILLRECT: {
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                //System.out.println("n1=" + n1 + ", n2=" + n2 + ", n3=" + n3 + ", n4=" + n4);
                main.FillRect(n1, n2, n3, n4);
                break;
            }

            case tokDRAWRECT: {
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.DrawRect(n1, n2, n3, n4);
                break;
            }


            case tokFILLROUNDRECT: {
                int n6 = PopInt();
                int n5 = PopInt();
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.FillRoundRect(n1, n2, n3, n4, n5, n6);
                break;
            }

            case tokDRAWROUNDRECT: {
                int n6 = PopInt();
                int n5 = PopInt();
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.DrawRoundRect(n1, n2, n3, n4, n5, n6);
                break;
            }

            case tokFILLARC: {
                int n6 = PopInt();
                int n5 = PopInt();
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.FillArc(n1, n2, n3, n4, n5, n6);
                break;
            }

            case tokDRAWARC: {
                int n6 = PopInt();
                int n5 = PopInt();
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.DrawArc(n1, n2, n3, n4, n5, n6);
                break;
            }

            case tokDRAWSTRING: {
                int n3 = PopInt();
                int n2 = PopInt();
                String n1 = PopString();
                main.DrawString(n1, n2, n3);
                break;
            }

            case tokSETCOLOR: {
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.SetColor(n1, n2, n3);
                break;
            }

            case tokGELLOAD: {
                String resourceName = PopString();
                String gelName = PopString();
                main.GelLoad(gelName, resourceName);
                break;
            }

            case tokGELGRAB: {
                int h = PopInt();
                int w = PopInt();
                int y = PopInt();
                int x = PopInt();
                String gelName = PopString();
                main.GelGrab(gelName, x, y, w, h);
                break;
            }

            case tokDRAWGEL: {
                int y = PopInt();
                int x = PopInt();
                String gelName = PopString();
                main.DrawGel(gelName, x, y);
                break;
            }

            case tokSPRITEGEL: {
                String gelName = PopString();
                String spriteName = PopString();
                main.SpriteGEL(spriteName, gelName);
                break;
            }

            case tokSPRITEMOVE: {
                int y = PopInt();
                int x = PopInt();
                String spriteName = PopString();
                main.SpriteMove(spriteName, x, y);
                break;
            }

            case tokSPRITEHIT: {
                //if (DEBUG)
                //{
                //    System.out.print("SPRITEHIT$> ");
                //    DumpOperandStack();
                //}
                String spriteName2 = PopString();
                String spriteName1 = PopString();
                PushInt(main.SpriteHit(spriteName1, spriteName2));
                break;
            }

            case tokBLIT: {
                int n6 = PopInt();
                int n5 = PopInt();
                int n4 = PopInt();
                int n3 = PopInt();
                int n2 = PopInt();
                int n1 = PopInt();
                main.Blit(n1, n2, n3, n4, n5, n6);
                break;
            }

            case tokFOR: {

                //if (DEBUG)
                //{
                //    System.out.print("FOR> ");
                //    DumpOperandStack();
                //}

                int stepValue;

                if (stepFlag) {
                    stepFlag = false;     // Reset to false first, just in case PopInt() raises an exception
                    stepValue = PopInt(); // ... STEP (stepValue)
                } else {
                    stepValue = 1;
                }

                int toValue = PopInt();// ...TO (toValue) ...
                int initValue = PopInt();// FOR X% = (initValue) ...

                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 24) & 0x00ffffff; // Atari BASIC doesn't allow a subscript (is that general?)

                if (varType[var] != TYPE_INTEGER) {
                    throw new BasicError(BasicError.INTEGER_EXPECTED, "Loop variables must be integer");
                }

                if (index != 0) {
                    throw new BasicError(BasicError.INTERNAL_ERROR, "Loop variables cannot be arrays");
                }

                int[] tmp = (int[]) varObject[var];
                tmp[0] = initValue;

                //long heap_use = 0L;
                //long heap_total = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                controlStack[++controlSP] = exeProg;
                controlStack[++controlSP] = new Integer(exeLen);
                controlStack[++controlSP] = new Integer(exePC);
                controlStack[++controlSP] = new Integer(exeNextLinePC);

                controlStack[++controlSP] = new Integer(var);                     // Control Variable
                controlStack[++controlSP] = new Integer(toValue);                 // Limit
                controlStack[++controlSP] = new Integer(stepValue);               // Step

                controlStack[++controlSP] = new Integer(CONTROL_FORLOOP);

                //heap_use = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory() - heap_total;
                // System.out.println("heap_use " + heap_use );
                break;
            }

            case tokSTEP:
                stepFlag = true;
            case tokFOREQ:
            case tokTO:
                //if (DEBUG)
                //{
                //    System.out.print("FOREQ/TO/STEP> ");
                //    DumpOperandStack();
                //}
                break;

            case tokNEXT: {
                //if (DEBUG)
                //{
                //    System.out.print("NEXT> ");
                //    DumpOperandStack();
                //}

                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 24) & 0x00ffffff; // Atari BASIC doesn't allow a subscript (is that General?)

                if (varType[var] != TYPE_INTEGER) {
                    throw new BasicError(BasicError.INTEGER_EXPECTED, "Loop variables must be integer");
                }

                if (index != 0) {
                    throw new BasicError(BasicError.INTERNAL_ERROR, "Loop variables cannot be arrays");
                }

                whileLoop:
                while (controlSP >= 0) {
                    int controlItem = ((Integer) controlStack[controlSP]).intValue();
                    if (controlItem == CONTROL_FORLOOP) {
                        int localSP = controlSP;

                        localSP--;
                        int stepValue = ((Integer) controlStack[localSP--]).intValue();
                        int toValue = ((Integer) controlStack[localSP--]).intValue();
                        int ctrlVar = ((Integer) controlStack[localSP--]).intValue();

                        if (ctrlVar == var) {
                            int[] tmp = (int[]) varObject[ctrlVar];
                            tmp[0] = tmp[0] + stepValue;

                            if (((stepValue > 0) && (tmp[0] <= toValue))
                                    || ((stepValue < 0) && (tmp[0] >= toValue))) {

                                exeNextLinePC = ((Integer) controlStack[localSP--]).intValue();
                                exePC = ((Integer) controlStack[localSP--]).intValue();
                                exeLen = ((Integer) controlStack[localSP--]).intValue();
                                exeProg = (byte[]) controlStack[localSP--];
                            } else { //System.out.println("NEXT end");
                                controlSP = localSP;
                                controlSP--;
                                controlSP--;
                                controlSP--;
                                controlSP--;
                            }

                            break switchStatement;
                        } else {//System.out.println("ctrlVar != var");
                            controlSP = localSP;
                            controlSP--;
                            controlSP--;
                            controlSP--;
                            controlSP--;
                        }
                    }
                }
                throw new BasicError(BasicError.NEXT_BEFORE_FOR, "NEXT before FOR");
            }

            case tokINPUT: {
                //if (DEBUG)
                //{
                //    System.out.print("INPUT> ");
                //    DumpOperandStack();
                //}

                int ref = PopRef();
                int var = (ref & 0x000000ff);
                int index = (ref >> 8) & 0x00ffffff;
                String s = main.GetLine(PopString(), "");

                /*
                 * This switch is basically the same as the one in the READ statement, it will probably
                 * also be used in file GET and INPUT statements and is an ideal candiate for making into
                 * a function.
                 */

                switch (varType[var]) {
                    case TYPE_INTEGER: {
                        int ival;

                        s = s.trim();

                        try {
                            ival = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
//#if MB191
//#                             ival = (int) java.lang.Float.parseFloat(s);
//#else
                          ival = Float.fromString(s);
                          ival = Float.ftoi(ival);
//#endif

                        }

                        int[] obj = (int[]) varObject[var];
                        obj[index] = ival;
                        break;
                    }
                    case TYPE_FLOAT: {
                        s = s.trim();
//#if MB191
//#                         float ival = java.lang.Float.parseFloat(s);
//#                         float[] obj = (float[]) varObject[var];
//#                         obj[index] = ival;
//#else
                     int ival = Float.fromString(s);
                      int[] obj = (int[])varObject[var];
                      obj[index] = ival;
//#endif

                        break;
                    }
                    case TYPE_STRING: {
                        String[] obj = (String[]) varObject[var];
                        obj[index] = s;
                        break;
                    }
                    default:
                        throw new BasicError(BasicError.INTERNAL_ERROR, "Invalid Type");
                }
                break;
            }

            case tokLOAD: {
                New();
                String filename = PopString();
                main.OpenFile(0, filename, true);
                DataInput dataInput = main.GetDataInputChannel(0);
                try {
                    if (dataInput != null) {
                        LoadFrom(dataInput);
                    }
                } finally {
                    main.CloseFile(0);
                }
                exePC = exeLen;
                exeNextLinePC = exeLen;
                break;
            }

            case tokENTER: {
                String filename = PopString();

                main.OpenFile(0, filename, true);
                DataInput dataInput = main.GetDataInputChannel(0);
                if (dataInput != null) {
                    Enter(dataInput);
                }
                main.CloseFile(0);
                exePC = exeLen;
                exeNextLinePC = exeLen;
                break;
            }

            case tokLIST: {
                //if (DEBUG)
                //{
                //    System.out.print("LIST> ");
                //    DumpOperandStack();
                //}

                String filename = null;
                int firstLine = 0;
                int lastLine = 65535;

                if (valueSP == 0) // List filename | List lno
                {
                    if (PopType(0) == TYPE_STRING) {
                        filename = PopString();
                    } else {
                        firstLine = PopInt();
                        lastLine = firstLine;
                    }
                } else if (valueSP == 1) // List lno1, lno2
                {
                    lastLine = PopInt();
                    firstLine = PopInt();
                } else if (valueSP != -1) {
                    throw new BasicError(BasicError.INTERNAL_ERROR, "List: Invalid number of arguments");
                }

                if (filename != null) {
                    DataOutput out = null;
                    main.Delete(filename);
                    main.OpenFile(0, filename, false);
                    DataOutput dataOutput = main.GetDataOutputChannel(0);
                    List(dataOutput, firstLine, lastLine, false);
                    main.CloseFile(0);
                } else {
                    List(null, firstLine, lastLine, false);
                }

                break;
            }

            case tokSAVE: {
                String filename = PopString();
                main.Delete(filename);
                main.OpenFile(0, filename, false);
                DataOutput dataOutput = main.GetDataOutputChannel(0);
                if (dataOutput != null) {
                    SaveTo(dataOutput);
                }
                main.CloseFile(0);
                break;
            }

            case tokDELETE:
                main.Delete(PopString());
                break;

            case tokEDIT: {
                int lno = PopInt();
                List(null, lno, lno, true);
                break;
            }

            case tokTRAP: {
                int lno = PopInt();
                if (lno != -1) {
                    trapPC = FindLine(lno, exeProg, exeLen);
                    if (trapPC == -1) {
                        throw new BasicError(BasicError.LINE_NOT_FOUND, "Line " + lno + " not found");
                    }
                } else {
                    trapPC = -1;
                }
                break;
            }

            case tokHASH:
                break;

            case tokOPEN: {
                String mode = PopString();
                String filename = PopString();
                int channel = PopInt();

                if (mode.regionMatches(true, 0, "OUTPUT", 0, 6)) {
                    main.OpenFile(channel, filename, false);
                } else if (mode.regionMatches(true, 0, "INPUT", 0, 5)) {
                    main.OpenFile(channel, filename, true);
                } else {
                    throw new BasicError(BasicError.INVALID_IO_MODE, "Invalid Open Mode");
                }
                break;
            }

            case tokCLOSE: {
                int channel = PopInt();
                main.CloseFile(channel);
                break;
            }

            case tokNOTE: {
                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 8) & 0x00ffffff;
                int channel = PopInt();

                switch (varType[var]) {
                    case TYPE_INTEGER: {
                        int[] t = (int[]) varObject[var];
                        t[index] = main.Note(channel);
                        if (DEBUG) {
                            System.out.println("*** NOTE = " + t[index] + " var=" + var + " index=" + index + "******************");
                        }
                        break;
                    }

                    default:
                        throw new BasicError(BasicError.INTEGER_EXPECTED, "Integer Expected");
                }
                break;
            }

            case tokPOINT: {
                int pointVal = PopInt();
                int channel = PopInt();
                main.Point(channel, pointVal);
                break;
            }

            case tokPUT: {
                int byteVal = PopInt();
                int channel = PopInt();
                main.PutByte(channel, byteVal);
                break;
            }

            case tokGET: {
                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 8) & 0x00ffffff;
                int channel = PopInt();

                switch (varType[var]) {
                    case TYPE_INTEGER: {
                        int[] t = (int[]) varObject[var];
                        t[index] = main.GetByte(channel);
                        break;
                    }

                    default:
                        throw new BasicError(BasicError.INTEGER_EXPECTED, "Integer Expected");
                }
                break;
            }

            case tokPRINTHASH: {
                int type = PopType(0);
                switch (type) {
                    case TYPE_INTEGER: {
                        int ival = PopInt();
                        int channel = PopInt();
                        main.PutInt(channel, ival);
                        break;
                    }

                    case TYPE_FLOAT: {

//#if MB191
//#                         float fval = PopFloat();
//#                         int channel = PopInt();
//#                         main.PutFloat(channel, fval);
//#else
                  int fval = PopFloat();
                  int channel = PopInt();
                  main.PutInt(channel, fval);
//#endif

                        break;
                    }

                    case TYPE_STRING: {
                        String sval = PopString();
                        int channel = PopInt();
                        main.PutString(channel, sval);
                        break;
                    }

                    default:
                        throw new BasicError(BasicError.INTERNAL_ERROR, "Invalid Type");
                }
                break;
            }

            case tokINPUTHASH: {
                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 8) & 0x00ffffff;
                int channel = PopInt();
                switch (varType[var]) {
                    case TYPE_INTEGER: {
                        int[] t = (int[]) varObject[var];
                        t[index] = main.GetInt(channel);
                        break;
                    }

                    case TYPE_FLOAT: {
//#if MB191
//#                         float[] t = (float[]) varObject[var];
//#                         t[index] = main.GetFloat(channel);
//#else
                 int[] t = (int[])varObject[var];
                  t[index] = main.GetInt(channel);
//#endif
                        break;
                    }

                    case TYPE_STRING: {
                        String[] t = (String[]) varObject[var];
                        t[index] = main.GetString(channel);
                        break;
                    }

                    default:
                        throw new BasicError(BasicError.INTERNAL_ERROR, "Invalid Type");
                }
                break;
            }

            case tokDATA:
                break;

            case tokRESTORE: {
                int lno = PopInt();
                if (lno == -1) {
                    dataPC = 0;
                } else {
                    dataPC = FindLine(lno, exeProg, exeLen);
                }

                if (dataPC == -1) {
                    throw new BasicError(BasicError.LINE_NOT_FOUND, "Line " + lno + " not found");
                }

                dataOffset = -1;
                dataLen = -1;
                break;
            }

            case tokREAD: {
                /*
                 * prog[dataPC+0] = hi
                 * prog[dataPC+1] = lo
                 * prog[dataPC+2] = lineLen
                 * prog[dataPC+3] = opDATA
                 */

                //if (DEBUG)
                //{
                //    System.out.print("READ> ");
                //    DumpOperandStack();
                //}

                if (dataOffset >= dataLen) {
                    if (dataLen != -1) {
                        dataPC += (dataLen + 1);  // Don't forget to skip the EOS byte
                    }
                    while (exeProg[dataPC + 3] != tokDATA) {
                        dataPC += (int) (exeProg[dataPC + 2] & 0xff);
                        if (dataPC >= exeLen) {
                            throw new BasicError(BasicError.OUT_OF_DATA, "Out of Data");
                        }
                    }

                    dataOffset = 5;
                    dataLen = dataOffset + (int) (exeProg[dataPC + 4] & 0xff);
                    //dataLen -= 1; // Subtract one for the EOS which terminates every line
                }

                StringBuffer sb = new StringBuffer();

                if (DEBUG) {
                    System.out.println("READ> dataOffset=" + dataOffset + ", dataLen=" + dataLen);
                }

                while (dataOffset < dataLen) {
                    char ch = (char) (exeProg[dataPC + dataOffset++] & 0xff);
                    if (ch == ',') {
                        break;
                    }

                    sb.append(ch);
                }

                if (DEBUG) {
                    System.out.println("READ> \"" + sb.toString() + "\"");
                }

                int ref = PopRef();
                int index = (ref >> 8) & 0x003fffff;
                int var = ref & 0x000000ff;
                int type = varType[var];

                String s = sb.toString();
                switch (type) {
                    case TYPE_INTEGER: {
                        int ival;

                        s = s.trim();

                        try {
                            ival = Integer.parseInt(s);
                        } catch (NumberFormatException e) {
//#if MB191
//#                             ival = (int) java.lang.Float.parseFloat(s);
//#else
                      ival = Float.fromString(s);
                      ival = Float.ftoi(ival);
//#endif

                        }

                        int[] obj = (int[]) varObject[var];
                        obj[index] = ival;
                        break;
                    }
                    case TYPE_FLOAT: {
                        s = s.trim();
//#if MB191
//#                         float fval = java.lang.Float.parseFloat(s);
//#                         float[] obj = (float[]) varObject[var];
//#                         obj[index] = fval;
//#else
                  int ival = Float.fromString(s);
                  int[] obj = (int[])varObject[var];
                  obj[index] = ival;
//#endif

                        break;
                    }
                    case TYPE_STRING: {
                        String[] obj = (String[]) varObject[var];
                        obj[index] = s;
                        break;
                    }
                    default:
                        throw new BasicError(BasicError.INTERNAL_ERROR, "Invalid Type");
                }
                break;
            }


            case tokLBRACKET:
                if (DEBUG) {
                    System.out.println("LBRACKET: Setting argCount to " + (commaCount + 1));
                }
                argCount = commaCount + 1;
                commaCount = 0;
                break;

            case tokRBRACKET:
                if (DEBUG) {
                    System.out.println("RBRACKET: Resetting commaCount");
                }
                commaCount = 0;
                break;

            case tokEOS:
                if (DEBUG) {
                    System.out.println("EOS: Resetting commaCount");
                }
                commaCount = 0;
                break;

            case tokCOMMA:
                commaCount++;
                break;

            case tokEQ: // =
            {
                //if (DEBUG)
                //{
                //    System.out.print("EQ> ");
                //    DumpOperandStack();
                //}

                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_STRING) || (type2 == TYPE_STRING)) {
                    String right = PopString();
                    String left = PopString();
                    PushInt(left.compareTo(right) == 0 ? 1 : 0);
                } else if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushInt((left == right) ? 1 : 0);
//#else
             int right = PopFloat();
            int left = PopFloat();
            PushInt(Float.Compare(left,right) == 0 ? 1 : 0);
//#endif

                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left == right ? 1 : 0);
                }
                break;
            }

            case tokNE:// <>
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_STRING) || (type2 == TYPE_STRING)) {
                    String right = PopString();
                    String left = PopString();
                    PushInt(left.compareTo(right) != 0 ? 1 : 0);
                } else if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushInt((left != right) ? 1 : 0);
//#else
             int right = PopFloat();
            int left = PopFloat();
            PushInt(Float.Compare(left,right) != 0 ? 1 : 0);
//#endif

                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left != right ? 1 : 0);
                }
                break;
            }

            case tokLT:// <
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_STRING) || (type2 == TYPE_STRING)) {
                    String right = PopString();
                    String left = PopString();
                    PushInt(left.compareTo(right) < 0 ? 1 : 0);
                } else if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushInt((left < right) ? 1 : 0);
//#else
            int right = PopFloat();
            int left = PopFloat();
            PushInt(Float.Compare(left,right) < 0 ? 1 : 0);
//#endif

                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left < right ? 1 : 0);
                }
                break;
            }

            case tokLE:// <=
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_STRING) || (type2 == TYPE_STRING)) {
                    String right = PopString();
                    String left = PopString();
                    PushInt(left.compareTo(right) <= 0 ? 1 : 0);
                } else if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushInt((left <= right) ? 1 : 0);
//#else
           int right = PopFloat();
           int left = PopFloat();
           PushInt(Float.Compare(left,right) <= 0 ? 1 : 0);
//#endif

                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left <= right ? 1 : 0);
                }
                break;
            }

            case tokGT:// >
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_STRING) || (type2 == TYPE_STRING)) {
                    String right = PopString();
                    String left = PopString();
                    PushInt(left.compareTo(right) > 0 ? 1 : 0);
                } else if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushInt((left > right) ? 1 : 0);
//#else
            int right = PopFloat();
            int left = PopFloat();
            PushInt(Float.Compare(left,right) > 0 ? 1 : 0);
//#endif

                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left > right ? 1 : 0);
                }
                break;
            }

            case tokGE:// >=
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_STRING) || (type2 == TYPE_STRING)) {
                    String right = PopString();
                    String left = PopString();
                    PushInt(left.compareTo(right) >= 0 ? 1 : 0);
                } else if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushInt((left >= right) ? 1 : 0);
//#else
            int right = PopFloat();
            int left = PopFloat();
            PushInt(Float.Compare(left,right) >= 0 ? 1 : 0);
//#endif

                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left >= right ? 1 : 0);
                }
                break;
            }

            case tokADD:// +
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_STRING) || (type2 == TYPE_STRING)) {
                    String right = PopString();
                    String left = PopString();
                    PushString(left + right);
                } else if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushFloat(left + right);
//#else
           int right = PopFloat();
            int left = PopFloat();
            PushFloat(Float.Add(left,right));
//#endif

                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left + right);
                }
                break;
            }

            case tokSUB:// -
            {
                //if (DEBUG)
                //{
                //    System.out.print("SUB> ");
                //    DumpOperandStack();
                //}

                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {
                    //#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushFloat(left - right);
//#else
           int right = PopFloat();
            int left = PopFloat();
            PushFloat(Float.Subtract(left,right));
//#endif
                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left - right);
                }
                break;
            }

            case tokMULT:// *
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {

//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushFloat(left * right);
//#else
           int right = PopFloat();
            int left = PopFloat();
            PushFloat(Float.Multiply(left,right));
//#endif
                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left * right);
                }
                break;
            }

            case tokDIV://  /
            {
                int type1 = PopType(0);
                int type2 = PopType(1);

                if ((type1 == TYPE_FLOAT) || (type2 == TYPE_FLOAT)) {

//#if MB191
//#                     float right = PopFloat();
//#                     float left = PopFloat();
//#                     PushFloat(left / right);
//#else
           int right = PopFloat();
            int left = PopFloat();
            PushFloat(Float.Divide(left,right));
//#endif 
                } else {
                    int right = PopInt();
                    int left = PopInt();
                    PushInt(left / right);
                }
                break;
            }

            case tokPOWER: {
//#if MB191
//#                 float right = PopFloat();
//#                 float left = PopFloat();
//#                 PushFloat((float) Float11.pow((float) left, (float) right));
//#else
          int right = PopFloat();
          int left = PopFloat();
          PushFloat(Float.pow(left, right));
//#endif

                break;
            }

            case tokUMINUS: {
                int type1 = PopType(0);

                if (type1 == TYPE_INTEGER) {
                    PushInt(-PopInt());
                } else {
//#if MB191
//#                     PushFloat(-PopFloat());
//#else
       PushFloat(Float.Negate(PopFloat()));
//#endif

                }
                break;
            }

            case tokBITAND: {
                int right = PopInt();
                int left = PopInt();
                PushInt(left & right);
                break;
            }

            case tokBITOR: {
                int right = PopInt();
                int left = PopInt();
                PushInt(left | right);
                break;
            }

            case tokBITXOR: {
                int right = PopInt();
                int left = PopInt();
                PushInt(left ^ right);
                break;
            }

            case tokLOGNOT: {
                PushInt(PopInt() == 0 ? 1 : 0);
                break;
            }

            case tokLOGAND: {
                int right = PopInt();
                int left = PopInt();
                PushInt((left != 0) && (right != 0) ? 1 : 0);
                break;
            }

            case tokLOGOR: {
                int right = PopInt();
                int left = PopInt();
                PushInt((left != 0) || (right != 0) ? 1 : 0);
                break;
            }

            case tokSCREENWIDTH:
                //if (DEBUG)
                //{
                //    System.out.print("SCREENWIDTH> ");
                //    DumpOperandStack();
                //}

                PopInt(); // Dummy argument
                PushInt(main.ScreenWidth());
                break;

            case tokSCREENHEIGHT:
                //if (DEBUG)
                //{
                //    System.out.print("SCREENHEIGHT> ");
                //    DumpOperandStack();
                //}

                PopInt(); // Dummy argument
                PushInt(main.ScreenHeight());
                break;

            case tokGELWIDTH:
                PushInt(main.GelWidth(PopString()));
                break;

            case tokGELHEIGHT:
                PushInt(main.GelHeight(PopString()));
                break;

            case tokISCOLOR:
                PopInt(); // Dummy argument
                PushInt(main.isColor());
                break;

            case tokNUMCOLORS:
                PopInt(); // Dummy argument
                PushInt(main.NumColors());
                break;

            case tokSTRINGWIDTH:
                PushInt(main.StringWidth(PopString()));
                break;

            case tokSTRINGHEIGHT:
                PushInt(main.StringHeight(PopString()));
                break;

            case tokLEFT$: {
                //if (DEBUG)
                //{
                //    System.out.print("LEFT$> ");
                //    DumpOperandStack();
                //}
                int nbytes = PopInt();
                String str = PopString();
                PushString(str.substring(0, nbytes));
                break;
            }

            case tokMID$: {
                //if (DEBUG)
                //{
                //    System.out.print("MID$> ");
                //    DumpOperandStack();
                //}
                int nbytes = PopInt();
                int start = PopInt() - 1;     // Basic Strings Index from 1 but Java strings are from 0
                String str = PopString();
                PushString(str.substring(start, start + nbytes));
                break;
            }

            case tokRIGHT$: {
                //if (DEBUG)
                //{
                //    System.out.print("RIGHT$> ");
                //    DumpOperandStack();
                //}
                int nbytes = PopInt();
                String str = PopString();
                PushString(str.substring(str.length() - nbytes));
                break;
            }

            case tokCHR$: {
                //if (DEBUG)
                //{
                //    System.out.print("CHR$> ");
                //    DumpOperandStack();
                //}
                char[] chArray = new char[1];
                int ival = PopInt();
                chArray[0] = (char) ival;
                PushString(new String(chArray));
                break;
            }

            case tokSTR$: {
                if (PopType(0) == TYPE_FLOAT) {
//#if MB191
//#                     PushString(java.lang.Float.toString(PopFloat()));
//#else
            PushString(Float.toString(PopFloat()));
//#endif
                } else {
                    PushString(Integer.toString(PopInt()));
                }
                break;
            }

            case tokLEN:
                PushInt(PopString().length());
                break;

            case tokASC:
                PushInt(PopString().charAt(0));
                break;

            case tokVAL: {
                String str = PopString();

                str = str.trim();

                try {
                    PushInt(Integer.parseInt(str));
                } catch (NumberFormatException e) {
//#if MB191
//#                     PushFloat(java.lang.Float.parseFloat(str));
//#else
             PushFloat(Float.fromString(str));
//#endif
                }
                break;
            }

            case tokUP:
                PopInt(); // Dummy argument
                PushInt(main.Up());
                break;

            case tokDOWN:
                PopInt(); // Dummy argument
                PushInt(main.Down());
                break;

            case tokLEFT:
                PopInt(); // Dummy argument
                PushInt(main.Left());
                break;

            case tokRIGHT:
                PopInt(); // Dummy argument
                PushInt(main.Right());
                break;

            case tokFIRE:
                PopInt(); // Dummy argument
                PushInt(main.Fire());
                break;

            case tokGAMEA:
                PopInt(); // Dummy argument
                PushInt(main.GameA());
                break;

            case tokGAMEB:
                PopInt(); // Dummy argument
                PushInt(main.GameB());
                break;

            case tokGAMEC:
                PopInt(); // Dummy argument
                PushInt(main.GameC());
                break;

            case tokGAMED:
                PopInt(); // Dummy argument
                PushInt(main.GameD());
                break;

            case tokDAYS:
                PopInt(); // Dummy argument
                PushInt((int) (System.currentTimeMillis() / MS_PER_DAY));
                break;

            case tokMILLISECONDS:
                PopInt(); // Dummy argument
                PushInt((int) (System.currentTimeMillis() % MS_PER_DAY));
                break;

            case tokYEAR: {
                int n2 = PopInt();
                int n1 = PopInt();
                PushInt(main.Year(new Date((long) n1 * MS_PER_DAY + (long) n2)));
                break;
            }

            case tokMONTH: {
                int n2 = PopInt();
                int n1 = PopInt();
                PushInt(main.Month(new Date((long) n1 * MS_PER_DAY + (long) n2)));
                break;
            }

            case tokDAY: {
                int n2 = PopInt();
                int n1 = PopInt();
                PushInt(main.Day(new Date((long) n1 * MS_PER_DAY + (long) n2)));
                break;
            }

            case tokHOUR: {
                int n2 = PopInt();
                int n1 = PopInt();
                PushInt(main.Hour(new Date((long) n1 * MS_PER_DAY + (long) n2)));
                break;
            }

            case tokMINUTE: {
                int n2 = PopInt();
                int n1 = PopInt();
                PushInt(main.Minute(new Date((long) n1 * MS_PER_DAY + (long) n2)));
                break;
            }

            case tokSECOND: {
                int n2 = PopInt();
                int n1 = PopInt();
                PushInt(main.Second(new Date((long) n1 * MS_PER_DAY + (long) n2)));
                break;
            }

            case tokMILLISECOND: {
                int n2 = PopInt();
                int n1 = PopInt();
                PushInt(main.Millisecond(new Date((long) n1 * MS_PER_DAY + (long) n2)));
                break;
            }

            case tokRND:
                PopInt(); // Dummy argument
                PushInt(random.nextInt());
                break;

            case tokERR:
                PopInt(); // Dummy argument
                PushInt(lastError);
                break;

            /*
             * FRE(0) - sourceSize-sourceLen
             * FRE(1) - sourceLen
             * FRE(2) - sourceSize
             * FRE(3) - runtime.freeMemory()
             * FRE(4) - runtime.totalMemory()
             */
            case tokFRE: {
                int ival = PopInt(); // Dummy argument
                if (ival == 0) {
                    PushInt(sourceSize - sourceLen);
                } else if (ival == 1) {
                    PushInt(sourceLen);
                } else if (ival == 2) {
                    PushInt(sourceSize);
                } else if (ival == 3) {
                    PushInt((int) runtime.freeMemory());
                } else {
                    PushInt((int) runtime.totalMemory());
                }
                break;
            }

            case tokMOD: {
                int right = PopInt();
                int left = PopInt();
                PushInt(left % right);
                break;
            }

            case tokEDITFORM: {
                //if (DEBUG)
                //{
                //    System.out.print("EDITFORM> ");
                //    DumpOperandStack();
                //}
                int mode = PopInt();
                int maxLen = PopInt();
                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 8) & 0x00ffffff;
                String[] stringArray = (String[]) varObject[var];
                String defaultText = stringArray[index];
                String label = PopString();
                String cancelText = PopString();
                String proceedText = PopString();
                String formTitle = PopString();
                int res = -1;

                String text = main.EditForm(formTitle, proceedText, cancelText, label, defaultText, maxLen, mode);
                if (text != null) {
                    stringArray[index] = text;
                    res = text.length();
                }

                PushInt(res);
                break;
            }

            case tokGAUGEFORM: {
                int mode = PopInt();
                int initialValue = PopInt();
                int maxValue = PopInt();
                String label = PopString();
                String cancelText = PopString();
                String proceedText = PopString();
                String formTitle = PopString();
                PushInt(main.GaugeForm(formTitle, proceedText, cancelText, label, maxValue, initialValue, mode));
                break;
            }

            case tokCHOICEFORM: {
                int mode = PopInt();
                int ref = PopRef();
                String[] stringArray = (String[]) varObject[ref & 0x000000ff];
                String label = PopString();
                String cancelText = PopString();
                String proceedText = PopString();
                String formTitle = PopString();

                PushInt(main.ChoiceForm(formTitle, proceedText, cancelText, label, stringArray, mode));
                break;
            }

            case tokDATEFORM: {
                int mode = PopInt();
                int ref = PopRef();
                int var = ref & 0x000000ff;
                int index = (ref >> 8) & 0x00ffffff;
                int[] intArray = (int[]) varObject[var & 0x000000ff];
                String label = PopString();
                String cancelText = PopString();
                String proceedText = PopString();
                String formTitle = PopString();

                if (intArray.length >= 2) {
                    Date date = null;
                    int res = -1;

                    if ((intArray[0] != -1) || (intArray[1] != -1)) {
                        date = new Date((long) intArray[0] * MS_PER_DAY + (long) intArray[1]);
                    }

                    date = main.DateForm(formTitle, proceedText, cancelText, label, date, mode);
                    if (date != null) {
                        long ms = date.getTime();

                        intArray[0] = (int) (ms / MS_PER_DAY);
                        intArray[1] = (int) (ms % MS_PER_DAY);
                        res = 1;
                    }

                    PushInt(res);
                } else {
                    throw new BasicError(BasicError.INTEGER_ARRAY_EXPECTED, "Integer Array Expected");
                    // throw new BasicError(BasicError.ARRAY_BOUNDS, iName[id6] + "(" + 1 + ") out of bounds");
                }
                break;
            }

            case tokMESSAGEFORM: {
                String msg = PopString();
                String label = PopString();
                String cancelText = PopString();
                String proceedText = PopString();
                String formTitle = PopString();
                PushInt(main.MessageForm(formTitle, proceedText, cancelText, label, msg));
                break;
            }

            case tokLOG: {
//#if MB191
//#                 PushFloat((float) Float11.log(PopFloat()));
//#else
          PushFloat(Float.log(PopFloat()));
//#endif

                break;
            }

            case tokEXP: {
//#if MB191
//#                 PushFloat((float) Float11.exp(PopFloat()));
//#else
          PushFloat(Float.exp(PopFloat()));
//#endif
                break;
            }

            case tokSQR: {
//#if MB191
//#                 PushFloat((float) Math.sqrt(PopFloat()));
//#else
          PushFloat(Float.sqrt(PopFloat()));
//#endif         
                break;
            }

            case tokSIN: {
//#if MB191
//#                 float fval = PopFloat();
//#                 PushFloat(degFlag ? (float) Math.sin(Math.toDegrees(fval)) : (float) Math.sin(fval));
//#else
          int fval = PopFloat();
          PushFloat(degFlag ? Float.sind(fval) : Float.sin(fval));
//#endif
                break;
            }

            case tokCOS: {
//#if MB191
//#                 float fval = PopFloat();
//#                 PushFloat(degFlag ? (float) Math.cos(Math.toDegrees(fval)) : (float) Math.cos(fval));
//#else
         int fval = PopFloat();
         PushFloat(degFlag ? Float.cosd(fval) : Float.cos(fval));
//#endif
                break;
            }

            case tokTAN: {
//#if MB191
//#                 float fval = PopFloat();
//#                 PushFloat(degFlag ? (float) Math.tan(Math.toDegrees(fval)) : (float) Math.tan(fval));
//#else
         int fval = PopFloat();
         PushFloat(degFlag ? Float.tand(fval) : Float.tan(fval));
//#endif
                break;
            }

            case tokASIN: {
//#if MB191
//#                 float fval = PopFloat();
//#                 PushFloat(degFlag ? (float) Float11.asin(Math.toDegrees(fval)) : (float) Float11.asin(fval));
//#else
          int fval = PopFloat();
          PushFloat(degFlag ? Float.asind(fval) : Float.asin(fval));
//#endif
                break;
            }

            case tokACOS: {
//#if MB191
//#                 float fval = PopFloat();
//#                 PushFloat(degFlag ? (float) Float11.acos(Math.toDegrees(fval)) : (float) Float11.acos(fval));
//#else
         int fval = PopFloat();
          PushFloat(degFlag ? Float.acosd(fval) : Float.acos(fval));
//#endif
                break;
            }

            case tokATAN: {
//#if MB191
//#                 float fval = PopFloat();
//#                 PushFloat(degFlag ? (float) Float11.atan(Math.toDegrees(fval)) : (float) Float11.atan(fval));
//#else
          int fval = PopFloat();
          PushFloat(degFlag ? Float.atand(fval) : Float.atan(fval));
//#endif
                break;
            }

            case tokABS: {

                if (PopType(0) == TYPE_FLOAT) {
//#if MB191
//#                     float fval = PopFloat();
//#                     PushFloat(Math.abs(fval));
//#else
           int fval = PopFloat();
              if (Float.Compare(fval, Float.ZERO) == -1)
                  fval = Float.Negate(fval);
              PushFloat(fval);
//#endif
                } else {
                    PushInt(Math.abs(PopInt()));
                }
                break;
            }

            case tokMAKEREF: {
                if (DEBUG) {
                    // System.out.print("MAKEREF> ");
                    // DumpOperandStack();
                }

                int index = PopInt();
                int var = PopRef();
                int ref = (index << 8) | var;
                PushOperand(ref, null, varType[var], CLASS_VARIABLE);
                break;
            }

            case tokASSIGN: // Value Stack: Variable [index] Value
            {   // присвоение переменной или массиву н.пр. X% = 10 или  mas%(0) = 1
                //if (DEBUG)
                //{
                //    System.out.println("ASSIGN> ");
                //    DumpOperandStack();
                //}

                int destType = PopType(1);

                //if ((varType[ivar] == TYPE_INTEGER) ||
                //    (varType[ivar] == TYPE_FLOAT))
                if (destType == TYPE_INTEGER) {
                    int ival = PopInt();
                    int ref = PopRef();
                    int var = ref & 0x000000ff;
                    int index = (ref >> 8) & 0x00ffffff;

                    if (DEBUG) {
                        System.out.println("Assigning " + ival + " to var " + varName[var] + "[" + index + "]");
                    }

                    int[] t = (int[]) varObject[var];
                    t[index] = ival;
                } else if (destType == TYPE_FLOAT) {

//#if MB191
//#                     float fval = PopFloat();
//#                     int ref = PopRef();
//#                     int var = ref & 0x000000ff;
//#                     int index = (ref >> 8) & 0x00ffffff;
//# 
//#                     if (DEBUG) {
//#                         System.out.println("Assigning " + java.lang.Float.toString(fval) + " to var " + varName[var] + "[" + index + "]");
//#                     }
//# 
//#                     float[] t = (float[]) varObject[var];
//#                     t[index] = fval;
//#else
           int fval = PopFloat();
            int ref = PopRef();
            int var = ref & 0x000000ff;
            int index = (ref >> 8) & 0x00ffffff;

            if (DEBUG)
              System.out.println("Assigning " + Float.toString(fval) + " to var " + varName[var] + "[" + index + "]");

            int[] t = (int[])varObject[var];
            t[index] = fval;
//#endif
                } else if (destType == TYPE_STRING) {
                    String sval = PopString();
                    int ref = PopRef();
                    int var = ref & 0x000000ff;
                    int index = (ref >> 8) & 0x00ffffff;

                    if (DEBUG) {
                        System.out.println("Assigning \"" + sval + "\" to var " + varName[var] + "[" + index + "]");
                    }

                    String[] t = (String[]) varObject[var];
                    t[index] = sval;
                } else {
                    throw new BasicError(BasicError.INTERNAL_ERROR, "Bad Variable Type");
                }

                break;
            }

            case tokPLAYWAV:
                (new AudioPlayback()).play(PopString());
                return;
            case tokPLAYTONE:
                int duration = PopInt();
                int note = PopInt();
                AudioPlayback.playTone(note, duration);
                return;
            case tokINKEY:
                if (argCount != 1) {
                    throw new BasicError(BasicError.INCORRECT_NUMBER_OF_ARGUMENTS, "Incorrect number of arguments");
                }

                PopInt();
                PushInt(main.INKEY());
                return;
            case tokSELECT:
                String[] var169 = new String[argCount - 1];

                for (int var212 = argCount - 2; var212 >= 0; --var212) {
                    var169[var212] = PopString();
                }

                String var170 = PopString();
                PushInt(main.SELECT(var170, var169));
                return;
            case tokALERT:
                int var22 = PopInt();
                int var140 = PopInt();
                var170 = PopString();
                String var171 = PopString();
                String var172 = PopString();
                main.alert(var172, var171, var170, var140, var22);
                return;
            case tokSETFONT:
                int var173 = PopInt();
                main.canvas.SetFontSize(var173);
                return;
            case tokMENUADD:
                var22 = PopInt();
                var140 = PopInt();
                var170 = PopString();
                main.menuAdd(var170, var140, var22);
                return;
            case tokMENUITEM:
                if (argCount != 1) {
                    throw new BasicError(BasicError.INCORRECT_NUMBER_OF_ARGUMENTS, "Incorrect number of arguments");
                }

                PopInt();
                PushString(main.menuItem());
                return;
            case tokMENUREMOVE:
                main.menuRemove(PopString());
                return;
            case tokCALL:
                controlStack[++controlSP] = exeProg;
                controlStack[++controlSP] = new Integer(exeLen);
                controlStack[++controlSP] = new Integer(exePC);
                controlStack[++controlSP] = new Integer(exeNextLinePC);
                controlStack[++controlSP] = new Integer(0);
                var_93c = controlSP;
                main.Delete("main");
                main.OpenFile(0, "main", false);
                DataOutput var90;
                if ((var90 = main.GetDataOutputChannel(0)) != null) {
                    SaveTo(var90);
                }

                main.CloseFile(0);
                Execute(37);
                Execute(5);
                return;
            case tokENDSUB:
                New();
                main.OpenFile(0, "main", true);
                DataInput var82;
                if ((var82 = main.GetDataInputChannel(0)) != null) {
                    LoadFrom(var82);
                }

                try {
                    main.CloseFile(0);
                } catch (Exception var174) {
                }

                if (((Integer) controlStack[var_93c--]).intValue() == 0) {
                    exeNextLinePC = ((Integer) controlStack[var_93c--]).intValue();
                    exePC = ((Integer) controlStack[var_93c--]).intValue();
                    exeLen = ((Integer) controlStack[var_93c--]).intValue();
                    exeProg = (byte[]) ((byte[]) controlStack[var_93c--]);
                }

                main.Delete("main");
                return;

            default:
                throw new BasicError(BasicError.INTERNAL_ERROR, "Bad operation: " + oper);
        }

    }
//    
//   private static void ExecuteO(int oper) {
//        int var5;
//        int var9;
//        int var12;
//        int var14;
//        int var18;
//        int var22;
//        int var26;
//        int var32;
//        int var38;
//        int var55;
//        int var60;
//        int var80;
//        DataInput var82;
//        String var89;
//        DataOutput var90;
//        int var103;
//        int var110;
//        int var108;
//        int var109;
//        int var119;
//        int var117;
//        int var113;
//        int var127;
//        int var126;
//        int var125;
//        int var124;
//        int var123;
//        int var122;
//        int var121;
//        int var120;
//        int var139;
//        int var140;
//        int var152;
//        int var153;
//        int var154;
//        String var157;
//        String var158;
//        String var170;
//        int var161;
//        int var167;
//        String var165;
//        int var186;
//        String var201;
//        String var202;
//        int var199;
//        switch (oper) {
//            case 0:
//                stopProgramFlag = true;
//                return;
//            case 1:
//            case 2:
//                do {
//                    if (controlSP < 0) {
//                        throw new BasicError(1, "Empty stack");
//                    }
//
//                    if ((var5 = ((Integer) controlStack[controlSP--]).intValue()) == 0) {
//                        if (oper == 2) {
//                            exeNextLinePC = ((Integer) controlStack[controlSP--]).intValue();
//                            exePC = ((Integer) controlStack[controlSP--]).intValue();
//                            exeLen = ((Integer) controlStack[controlSP--]).intValue();
//                            exeProg = (byte[]) ((byte[]) controlStack[controlSP--]);
//                            return;
//                        }
//
//                        --controlSP;
//                        --controlSP;
//                        --controlSP;
//                        --controlSP;
//                        return;
//                    }
//
//                    if (var5 != 1) {
//                        throw new BasicError(-1, "Bad item on control stack");
//                    }
//
//                    --controlSP;
//                    --controlSP;
//                    --controlSP;
//                    --controlSP;
//                    --controlSP;
//                    --controlSP;
//                    --controlSP;
//                } while (oper != 1);
//
//                return;
//            case 3:
//                exePC = exeLen;
//                exeNextLinePC = exeLen;
//                return;
//            case 4:
//                New();
//                exePC = exeLen;
//                exeNextLinePC = exeLen;
//                return;
//            case 5:
//                exeProg = sourceProg;
//                exeLen = sourceLen;
//                exePC = 0;
//                exeNextLinePC = 0;
//                return;
//            case 6:
//                Enumeration var1;
//                if ((var1 = main.Directory("*")) != null) {
//                    while (var1.hasMoreElements()) {
//                        main.PrintString((String) var1.nextElement() + "\n");
//                    }
//                }
//                break;
//            case 7:
//                degFlag = true;
//                return;
//            case 8:
//                degFlag = false;
//                return;
//            case 9:
//                main.Bye();
//                exePC = exeLen;
//                exeNextLinePC = exeLen;
//                return;
//            case 11:
//                controlStack[++controlSP] = exeProg;
//                controlStack[++controlSP] = new Integer(exeLen);
//                controlStack[++controlSP] = new Integer(exePC);
//                controlStack[++controlSP] = new Integer(exeNextLinePC);
//                controlStack[++controlSP] = new Integer(0);
//            case 10:
//                var5 = PopInt();
//                exeProg = sourceProg;
//                exeLen = sourceLen;
//                exePC = FindLine(var5, exeProg, exeLen);
//                if (exePC == -1) {
//                    throw new BasicError(3, "Line " + var5 + " not found");
//                }
//
//                exeNextLinePC = exePC;
//                return;
//            case 12:
//                int var6;
//                if ((var6 = PopInt()) > 0) {
//                    try {
//                        Thread.sleep((long) var6);
//                        return;
//                    } catch (InterruptedException var179) {
//                        return;
//                    }
//                }
//
//                Thread.yield();
//                return;
//            case 13:
//                StringBuffer var7 = new StringBuffer();
//                int var8;
//                if ((var8 = PopType(0)) == 0) {
//                    var9 = PopInt();
//                    var7.insert(0, var9);
//                } else if (var8 == 1) {
//                    var9 = PopFloat();
//                    var7.insert(0, Float.toString(var9));
//                } else if (var8 == 2) {
//                    String var180 = PopString();
//                    var7.insert(0, var180);
//                }
//
//                var7.append("\n");
//                main.PrintString(var7.toString());
//                return;
//            case 14:
//                return;
//            case 15:
//                int var10 = (var9 = PopRef()) & 255;
//                int var11 = var9 >> 8 & 16777215;
//                switch (varType[var10]) {
//                    case 0:
//                        varObject[var10] = new int[var11];
//                        return;
//                    case 1:
//                        varObject[var10] = new int[var11];
//                        return;
//                    case 2:
//                        varObject[var10] = new String[var11];
//                        return;
//                    default:
//                        throw new BasicError(-1, "DIM: Bad Variable Type");
//                }
//            case 16:
//                if (PopInt() == 0) {
//                    exePC = exeNextLinePC;
//                    return;
//                }
//                break;
//            case 17:
//                return;
//            case 18:
//                main.CLS();
//                return;
//            case 19:
//                var12 = PopInt();
//                int var13 = PopInt();
//                main.DrawLine(var13, var12, var13, var12);
//                return;
//            case 20:
//                var14 = PopInt();
//                int var15 = PopInt();
//                int var16 = PopInt();
//                int var17 = PopInt();
//                main.DrawLine(var17, var16, var15, var14);
//                return;
//            case 21:
//                var18 = PopInt();
//                int var19 = PopInt();
//                int var20 = PopInt();
//                int var21 = PopInt();
//                main.FillRect(var21, var20, var19, var18);
//                return;
//            case 22:
//                var22 = PopInt();
//                int var23 = PopInt();
//                int var24 = PopInt();
//                int var25 = PopInt();
//                main.DrawRect(var25, var24, var23, var22);
//                return;
//            case 23:
//                var26 = PopInt();
//                int var27 = PopInt();
//                int var28 = PopInt();
//                int var29 = PopInt();
//                int var30 = PopInt();
//                int var31 = PopInt();
//                main.FillRoundRect(var31, var30, var29, var28, var27, var26);
//                return;
//            case 24:
//                var32 = PopInt();
//                int var33 = PopInt();
//                int var34 = PopInt();
//                int var35 = PopInt();
//                int var36 = PopInt();
//                int var37 = PopInt();
//                main.DrawRoundRect(var37, var36, var35, var34, var33, var32);
//                return;
//            case 25:
//                var38 = PopInt();
//                int var39 = PopInt();
//                int var40 = PopInt();
//                int var41 = PopInt();
//                int var42 = PopInt();
//                int var43 = PopInt();
//                main.FillArc(var43, var42, var41, var40, var39, var38);
//                return;
//            case 26:
//                var38 = PopInt();
//                int var44 = PopInt();
//                int var45 = PopInt();
//                int var46 = PopInt();
//                int var47 = PopInt();
//                int var48 = PopInt();
//                main.DrawArc(var48, var47, var46, var45, var44, var38);
//                return;
//            case 27:
//                var38 = PopInt();
//                int var49 = PopInt();
//                String var50 = PopString();
//                main.DrawString(var50, var49, var38);
//                return;
//            case 28:
//                var38 = PopInt();
//                int var51 = PopInt();
//                int var52 = PopInt();
//                main.SetColor(var52, var51, var38);
//                return;
//            case 29:
//                var60 = PopInt();
//                int var182 = PopInt();
//                int var181 = PopInt();
//                int var69 = PopInt();
//                int var70 = PopInt();
//                int var71 = PopInt();
//                main.Blit(var71, var70, var69, var181, var182, var60);
//                return;
//            case 30:
//                if (stepFlag) {
//                    stepFlag = false;
//                    var60 = PopInt();
//                } else {
//                    var60 = 1;
//                }
//
//                int var72 = PopInt();
//                int var73 = PopInt();
//                int var74;
//                int var75 = (var74 = PopRef()) & 255;
//                int var76 = var74 >> 24 & 16777215;
//                if (varType[var75] != 0) {
//                    throw new BasicError(7, "Loop variables must be integer");
//                }
//
//                if (var76 != 0) {
//                    throw new BasicError(-1, "Loop variables cannot be arrays");
//                }
//
//                ((int[]) ((int[]) varObject[var75]))[0] = var73;
//                controlStack[++controlSP] = exeProg;
//                controlStack[++controlSP] = new Integer(exeLen);
//                controlStack[++controlSP] = new Integer(exePC);
//                controlStack[++controlSP] = new Integer(exeNextLinePC);
//                controlStack[++controlSP] = new Integer(var75);
//                controlStack[++controlSP] = new Integer(var72);
//                controlStack[++controlSP] = new Integer(var60);
//                controlStack[++controlSP] = new Integer(1);
//                return;
//            case 32:
//                stepFlag = true;
//            case 31:
//            case 123:
//                return;
//            case 33:
//                int var77 = (var60 = PopRef()) & 255;
//                int var78 = var60 >> 24 & 16777215;
//                if (varType[var77] != 0) {
//                    throw new BasicError(7, "Loop variables must be integer");
//                }
//
//                if (var78 != 0) {
//                    throw new BasicError(-1, "Loop variables cannot be arrays");
//                }
//
//                while (controlSP >= 0) {
//                    if (((Integer) controlStack[controlSP]).intValue() == 1) {
//                        var80 = controlSP;
//                        --var80;
//                        int var187 = ((Integer) controlStack[var80--]).intValue();
//                        var186 = ((Integer) controlStack[var80--]).intValue();
//                        int var185;
//                        if ((var185 = ((Integer) controlStack[var80--]).intValue()) == var77) {
//                            int[] var184;
//                            (var184 = (int[]) ((int[]) varObject[var185]))[0] = var184[0] + var187;
//                            if ((var187 <= 0 || var184[0] > var186) && (var187 >= 0 || var184[0] < var186)) {
//                                controlSP = var80;
//                                --controlSP;
//                                --controlSP;
//                                --controlSP;
//                                --controlSP;
//                                return;
//                            }
//
//                            exeNextLinePC = ((Integer) controlStack[var80--]).intValue();
//                            exePC = ((Integer) controlStack[var80--]).intValue();
//                            exeLen = ((Integer) controlStack[var80--]).intValue();
//                            exeProg = (byte[]) ((byte[]) controlStack[var80]);
//                            return;
//                        }
//
//                        controlSP = var80;
//                        --controlSP;
//                        --controlSP;
//                        --controlSP;
//                        --controlSP;
//                    }
//                }
//
//                throw new BasicError(4, "NEXT before FOR");
//            case 34:
//                int var79 = (var60 = PopRef()) & 255;
//                var80 = var60 >> 8 & 16777215;
//                String var81 = main.GetLine(PopString(), "");
//                switch (varType[var79]) {
//                    case 0:
//                        var81 = var81.trim();
//
//                        try {
//                            var186 = Integer.parseInt(var81);
//                        } catch (NumberFormatException var178) {
//                            var186 = Float.ftoi(Float.fromString(var81));
//                        }
//
//                        ((int[]) ((int[]) varObject[var79]))[var80] = var186;
//                        return;
//                    case 1:
//                        int var183 = Float.fromString(var81.trim());
//                        ((int[]) ((int[]) varObject[var79]))[var80] = var183;
//                        return;
//                    case 2:
//                        ((String[]) ((String[]) varObject[var79]))[var80] = var81;
//                        return;
//                    default:
//                        throw new BasicError(-1, "Invalid Type");
//                }
//            case 35:
//                String var86 = null;
//                int var87 = 0;
//                int var88 = '\uffff';
//                if (valueSP == 0) {
//                    if (PopType(0) == 2) {
//                        var86 = PopString();
//                    } else {
//                        var88 = var87 = PopInt();
//                    }
//                } else if (valueSP == 1) {
//                    var88 = PopInt();
//                    var87 = PopInt();
//                } else if (valueSP != -1) {
//                    throw new BasicError(-1, "List: Invalid number of arguments");
//                }
//
//                if (var86 != null) {
//                    var89 = null;
//                    main.Delete(var86);
//                    main.OpenFile(0, var86, false);
//                    List(main.GetDataOutputChannel(0), 0, var88, false);
//                    main.CloseFile(0);
//                    return;
//                }
//
//                List((DataOutput) null, var87, var88, false);
//                return;
//            case 36:
//                String var84 = PopString();
//                main.OpenFile(0, var84, true);
//                DataInput var85;
//                if ((var85 = main.GetDataInputChannel(0)) != null) {
//                    Enter(var85);
//                }
//
//                main.CloseFile(0);
//                exePC = exeLen;
//                exeNextLinePC = exeLen;
//                return;
//            case 37:
//                New();
//                String var83 = PopString();
//                main.OpenFile(0, var83, true);
//                if ((var82 = main.GetDataInputChannel(0)) != null) {
//                    LoadFrom(var82);
//                }
//
//                try {
//                    main.CloseFile(0);
//                } catch (Exception var177) {
//                    ;
//                }
//
//                exePC = exeLen;
//                exeNextLinePC = exeLen;
//                return;
//            case 38:
//                var89 = PopString();
//                main.Delete(var89);
//                main.OpenFile(0, var89, false);
//                if ((var90 = main.GetDataOutputChannel(0)) != null) {
//                    SaveTo(var90);
//                }
//
//                main.CloseFile(0);
//                return;
//            case 39:
//                main.Delete(PopString());
//                return;
//            case 40:
//                int var91 = PopInt();
//                List((DataOutput) null, var91, var91, true);
//                return;
//            case 41:
//                int var92;
//                if ((var92 = PopInt()) == -1) {
//                    trapPC = -1;
//                    return;
//                }
//
//                trapPC = FindLine(var92, exeProg, exeLen);
//                if (trapPC == -1) {
//                    throw new BasicError(3, "Line " + var92 + " not found");
//                }
//                break;
//            case 42:
//                String var93 = PopString();
//                String var94 = PopString();
//                int var95 = PopInt();
//                if (var93.regionMatches(true, 0, "OUTPUT", 0, 6)) {
//                    main.OpenFile(var95, var94, false);
//                    return;
//                }
//
//                if (var93.regionMatches(true, 0, "INPUT", 0, 5)) {
//                    main.OpenFile(var95, var94, true);
//                    return;
//                }
//
//                throw new BasicError(4097, "Invalid Open Mode");
//            case 43:
//                int var96 = PopInt();
//                main.CloseFile(var96);
//                return;
//            case 44:
//                int var97;
//                int var98 = (var97 = PopRef()) & 255;
//                int var99 = var97 >> 8 & 16777215;
//                int var100 = PopInt();
//                switch (varType[var98]) {
//                    case 0:
//                        ((int[]) ((int[]) varObject[var98]))[var99] = main.Note(var100);
//                        return;
//                    default:
//                        throw new BasicError(7, "Integer Expected");
//                }
//            case 45:
//                int var101 = PopInt();
//                int var102 = PopInt();
//                main.Point(var102, var101);
//                return;
//            case 46:
//                var103 = PopInt();
//                int var104 = PopInt();
//                main.PutByte(var104, var103);
//                return;
//            case 47:
//                int var105 = (var103 = PopRef()) & 255;
//                int var106 = var103 >> 8 & 16777215;
//                int var107 = PopInt();
//                switch (varType[var105]) {
//                    case 0:
//                        ((int[]) ((int[]) varObject[var105]))[var106] = main.GetByte(var107);
//                        return;
//                    default:
//                        throw new BasicError(7, "Integer Expected");
//                }
//            case 48:
//                return;
//            case 49:
//                if ((var103 = PopInt()) == -1) {
//                    dataPC = 0;
//                } else {
//                    dataPC = FindLine(var103, exeProg, exeLen);
//                }
//
//                if (dataPC == -1) {
//                    throw new BasicError(3, "Line " + var103 + " not found");
//                }
//
//                dataOffset = -1;
//                dataLen = -1;
//                return;
//            case 50:
//                if (dataOffset >= dataLen) {
//                    if (dataLen != -1) {
//                        dataPC += dataLen + 1;
//                    }
//
//                    while (exeProg[dataPC + 3] != 48) {
//                        dataPC += exeProg[dataPC + 2] & 255;
//                        if (dataPC >= exeLen) {
//                            throw new BasicError(16, "Out of Data");
//                        }
//                    }
//
//                    dataOffset = 5;
//                    dataLen = dataOffset + (exeProg[dataPC + 4] & 255);
//                }
//
//                StringBuffer var190;
//                char var189;
//                for (var190 = new StringBuffer(); dataOffset < dataLen && (var189 = (char) (exeProg[dataPC + dataOffset++] & 255)) != 44; var190.append(com.vmx.StringEncoder.decodeCharCP1251((byte) var189))) {
//                    /*
//                    if (var189 >= 192 && var189 <= 255) {
//                    var189 = (char) (var189 + 848);
//                    }
//                     */
//                }
//
//                int var188;
//                var113 = (var188 = PopRef()) >> 8 & 4194303;
//                int var114 = var188 & 255;
//                byte var115 = varType[var114];
//                String var116 = var190.toString();
//                switch (var115) {
//                    case 0:
//                        var116 = var116.trim();
//
//                        try {
//                            var117 = Integer.parseInt(var116);
//                        } catch (NumberFormatException var176) {
//                            var117 = Float.ftoi(Float.fromString(var116));
//                        }
//
//                        ((int[]) ((int[]) varObject[var114]))[var113] = var117;
//                        return;
//                    case 1:
//                        var119 = Float.fromString(var116.trim());
//                        ((int[]) ((int[]) varObject[var114]))[var113] = var119;
//                        return;
//                    case 2:
//                        ((String[]) ((String[]) varObject[var114]))[var113] = var116;
//                        return;
//                    default:
//                        throw new BasicError(-1, "Invalid Type");
//                }
//            case 51:
//                var117 = PopType(0);
//                int var118 = PopType(1);
//                if (var117 != 2 && var118 != 2) {
//                    if (var117 != 1 && var118 != 1) {
//                        var119 = PopInt();
//                        PushInt(PopInt() != var119 ? 0 : 1);
//                        return;
//                    }
//
//                    var119 = PopFloat();
//                    PushInt(Float.Compare(PopFloat(), var119) != 0 ? 0 : 1);
//                    return;
//                }
//
//                String var196 = PopString();
//                PushInt(PopString().compareTo(var196) != 0 ? 0 : 1);
//                return;
//            case 52:
//                var117 = PopType(0);
//                var119 = PopType(1);
//                if (var117 != 2 && var119 != 2) {
//                    if (var117 != 1 && var119 != 1) {
//                        var120 = PopInt();
//                        PushInt(PopInt() == var120 ? 0 : 1);
//                        return;
//                    }
//
//                    var120 = PopFloat();
//                    PushInt(Float.Compare(PopFloat(), var120) == 0 ? 0 : 1);
//                    return;
//                }
//
//                String var198 = PopString();
//                PushInt(PopString().compareTo(var198) == 0 ? 0 : 1);
//                return;
//            case 53:
//                var117 = PopType(0);
//                var120 = PopType(1);
//                if (var117 != 2 && var120 != 2) {
//                    if (var117 != 1 && var120 != 1) {
//                        var121 = PopInt();
//                        PushInt(PopInt() >= var121 ? 0 : 1);
//                        return;
//                    }
//
//                    var121 = PopFloat();
//                    PushInt(Float.Compare(PopFloat(), var121) >= 0 ? 0 : 1);
//                    return;
//                }
//
//                String var197 = PopString();
//                PushInt(PopString().compareTo(var197) >= 0 ? 0 : 1);
//                return;
//            case 54:
//                var117 = PopType(0);
//                var121 = PopType(1);
//                if (var117 != 2 && var121 != 2) {
//                    if (var117 != 1 && var121 != 1) {
//                        var122 = PopInt();
//                        PushInt(PopInt() > var122 ? 0 : 1);
//                        return;
//                    }
//
//                    var122 = PopFloat();
//                    PushInt(Float.Compare(PopFloat(), var122) > 0 ? 0 : 1);
//                    return;
//                }
//
//                String var192 = PopString();
//                PushInt(PopString().compareTo(var192) > 0 ? 0 : 1);
//                return;
//            case 55:
//                var117 = PopType(0);
//                var122 = PopType(1);
//                if (var117 != 2 && var122 != 2) {
//                    if (var117 != 1 && var122 != 1) {
//                        var123 = PopInt();
//                        PushInt(PopInt() <= var123 ? 0 : 1);
//                        return;
//                    }
//
//                    var123 = PopFloat();
//                    PushInt(Float.Compare(PopFloat(), var123) <= 0 ? 0 : 1);
//                    return;
//                }
//
//                String var193 = PopString();
//                PushInt(PopString().compareTo(var193) <= 0 ? 0 : 1);
//                return;
//            case 56:
//                var117 = PopType(0);
//                var123 = PopType(1);
//                if (var117 != 2 && var123 != 2) {
//                    if (var117 != 1 && var123 != 1) {
//                        var124 = PopInt();
//                        PushInt(PopInt() < var124 ? 0 : 1);
//                        return;
//                    }
//
//                    var124 = PopFloat();
//                    PushInt(Float.Compare(PopFloat(), var124) < 0 ? 0 : 1);
//                    return;
//                }
//
//                String var191 = PopString();
//                PushInt(PopString().compareTo(var191) < 0 ? 0 : 1);
//                return;
//            case 57:
//                argCount = commaCount + 1;
//                commaCount = 0;
//                return;
//            case 58:
//                commaCount = 0;
//                return;
//            case 59:
//                ++commaCount;
//                return;
//            case 60:
//                var117 = PopType(0);
//                var124 = PopType(1);
//                if (var117 != 2 && var124 != 2) {
//                    if (var117 != 1 && var124 != 1) {
//                        var125 = PopInt();
//                        PushInt(PopInt() + var125);
//                        return;
//                    }
//
//                    var125 = PopFloat();
//                    PushFloat(Float.Add(PopFloat(), var125));
//                    return;
//                }
//
//                String var195 = PopString();
//                String var194 = PopString();
//                PushString(var194 + var195);
//                return;
//            case 61:
//                var117 = PopType(0);
//                var125 = PopType(1);
//                if (var117 != 1 && var125 != 1) {
//                    var126 = PopInt();
//                    PushInt(PopInt() - var126);
//                    return;
//                }
//
//                var126 = PopFloat();
//                PushFloat(Float.Subtract(PopFloat(), var126));
//                return;
//            case 62:
//                if (PopType(0) == 0) {
//                    PushInt(-PopInt());
//                    return;
//                }
//
//                PushFloat(Float.Negate(PopFloat()));
//                return;
//            case 63:
//                var117 = PopType(0);
//                var126 = PopType(1);
//                if (var117 != 1 && var126 != 1) {
//                    var127 = PopInt();
//                    PushInt(PopInt() * var127);
//                    return;
//                }
//
//                var127 = PopFloat();
//                PushFloat(Float.Multiply(PopFloat(), var127));
//                return;
//            case 64:
//                var117 = PopType(0);
//                var127 = PopType(1);
//                int var128;
//                if (var117 != 1 && var127 != 1) {
//                    var128 = PopInt();
//                    PushInt(PopInt() / var128);
//                    return;
//                }
//
//                var128 = PopFloat();
//                PushFloat(Float.Divide(PopFloat(), var128));
//                return;
//            case 65:
//                var117 = PopFloat();
//                PushFloat(Float.pow(PopFloat(), var117));
//                return;
//            case 66:
//                int var129 = PopInt();
//                PushInt(PopInt() & var129);
//                return;
//            case 67:
//                int var131 = PopInt();
//                PushInt(PopInt() | var131);
//                return;
//            case 68:
//                int var133 = PopInt();
//                PushInt(PopInt() ^ var133);
//                return;
//            case 69:
//                PushInt(PopInt() != 0 ? 0 : 1);
//                return;
//            case 70:
//                int var135 = PopInt();
//                PushInt(PopInt() != 0 && var135 != 0 ? 1 : 0);
//                return;
//            case 71:
//                var9 = PopInt();
//                PushInt(PopInt() == 0 && var9 == 0 ? 0 : 1);
//                return;
//            case 72:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.ScreenWidth());
//                return;
//            case 73:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.ScreenHeight());
//                return;
//            case 74:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.isColor());
//                return;
//            case 75:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.NumColors());
//                return;
//            case 76:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushInt(main.StringWidth(PopString()));
//                return;
//            case 77:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushInt(main.StringHeight(PopString()));
//                return;
//            case 78:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var12 = PopInt();
//                PushString(PopString().substring(0, var12));
//                return;
//            case 79:
//                if (argCount != 3) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var199 = PopInt();
//                var139 = PopInt() - 1;
//                PushString(PopString().substring(var139, var139 + var199));
//                return;
//            case 80:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var199 = PopInt();
//                String var213 = PopString();
//                PushString(var213.substring(var213.length() - var199));
//                return;
//            case 81:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                char[] var200 = new char[1];
//                var139 = PopInt();
//                var200[0] = (char) var139;
//                PushString(new String(var200));
//                return;
//            case 82:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                if (PopType(0) == 1) {
//                    PushString(Float.toString(PopFloat()));
//                    return;
//                }
//
//                PushString(Integer.toString(PopInt()));
//                return;
//            case 83:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushInt(PopString().length());
//                return;
//            case 84:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushInt(PopString().charAt(0));
//                return;
//            case 85:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                String var138 = PopString().trim();
//
//                try {
//                    PushInt(Integer.parseInt(var138));
//                    return;
//                } catch (NumberFormatException var175) {
//                    PushFloat(Float.fromString(var138));
//                    return;
//                }
//            case 86:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.Up());
//                return;
//            case 87:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.Down());
//                return;
//            case 88:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.Left());
//                return;
//            case 89:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.Right());
//                return;
//            case 90:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.Fire());
//                return;
//            case 91:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.GameA());
//                return;
//            case 92:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.GameB());
//                return;
//            case 93:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.GameC());
//                return;
//            case 94:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.GameD());
//                return;
//            case 95:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt((int) (System.currentTimeMillis() / 86400000L));
//                return;
//            case 96:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt((int) (System.currentTimeMillis() % 86400000L));
//                return;
//            case 97:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var139 = PopInt();
//                var140 = PopInt();
//                PushInt(main.Year(new Date((long) var140 * 86400000L + (long) var139)));
//                return;
//            case 98:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var14 = PopInt();
//                var139 = PopInt();
//                PushInt(main.Month(new Date((long) var139 * 86400000L + (long) var14)));
//                return;
//            case 99:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var139 = PopInt();
//                var140 = PopInt();
//                PushInt(main.Day(new Date((long) var140 * 86400000L + (long) var139)));
//                return;
//            case 100:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var139 = PopInt();
//                var140 = PopInt();
//                PushInt(main.Hour(new Date((long) var140 * 86400000L + (long) var139)));
//                return;
//            case 101:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var139 = PopInt();
//                var140 = PopInt();
//                PushInt(main.Minute(new Date((long) var140 * 86400000L + (long) var139)));
//                return;
//            case 102:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var18 = PopInt();
//                var139 = PopInt();
//                PushInt(main.Second(new Date((long) var139 * 86400000L + (long) var18)));
//                return;
//            case 103:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var139 = PopInt();
//                var140 = PopInt();
//                PushInt(main.Millisecond(new Date((long) var140 * 86400000L + (long) var139)));
//                return;
//            case 104:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(random.nextInt());
//                return;
//            case 105:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(lastError);
//                return;
//            case 106:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                if ((var139 = PopInt()) == 0) {
//                    PushInt(sourceSize - sourceLen);
//                    return;
//                }
//
//                if (var139 == 1) {
//                    PushInt(sourceLen);
//                    return;
//                }
//
//                if (var139 == 2) {
//                    PushInt(sourceSize);
//                    return;
//                }
//
//                if (var139 == 3) {
//                    PushInt((int) runtime.freeMemory());
//                    return;
//                }
//
//                PushInt((int) runtime.totalMemory());
//                return;
//            case 107:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var38 = PopInt();
//                PushInt(PopInt() % var38);
//                return;
//            case 108:
//                if (argCount != 7) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var22 = PopInt();
//                var140 = PopInt();
//                int var141;
//                int var142 = (var141 = PopRef()) & 255;
//                int var143 = var141 >> 8 & 16777215;
//                String[] var144;
//                String var145 = (var144 = (String[]) ((String[]) varObject[var142]))[var143];
//                String var146 = PopString();
//                String var147 = PopString();
//                String var148 = PopString();
//                String var149 = PopString();
//                int var150 = -1;
//                String var151;
//                if ((var151 = main.EditForm(var149, var148, var147, var146, var145, var140, var22)) != null) {
//                    var144[var143] = var151;
//                    var150 = var151.length();
//                }
//
//                PushInt(var150);
//                return;
//            case 109:
//                if (argCount != 7) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var152 = PopInt();
//                var153 = PopInt();
//                var154 = PopInt();
//                var202 = PopString();
//                var201 = PopString();
//                var157 = PopString();
//                var158 = PopString();
//                PushInt(main.GaugeForm(var158, var157, var201, var202, var154, var153, var152));
//                return;
//            case 110:
//                if (argCount != 6) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var152 = PopInt();
//                var153 = PopRef();
//                String[] var203 = (String[]) ((String[]) varObject[var153 & 255]);
//                var202 = PopString();
//                var201 = PopString();
//                var157 = PopString();
//                var158 = PopString();
//                PushInt(main.ChoiceForm(var158, var157, var201, var202, var203, var152));
//                return;
//            case 111:
//                if (argCount != 6) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var152 = PopInt();
//                var154 = PopRef() & 255;
//                boolean var155 = false;
//                int[] var156 = (int[]) ((int[]) varObject[var154 & 255]);
//                var157 = PopString();
//                var158 = PopString();
//                String var159 = PopString();
//                String var160 = PopString();
//                if (var156.length < 2) {
//                    throw new BasicError(10, "Integer Array Expected");
//                }
//
//                Date var206 = null;
//                byte var209 = -1;
//                if (var156[0] != -1 || var156[1] != -1) {
//                    var206 = new Date((long) var156[0] * 86400000L + (long) var156[1]);
//                }
//
//                if ((var206 = main.DateForm(var160, var159, var158, var157, var206, var152)) != null) {
//                    long var211 = var206.getTime();
//                    var156[0] = (int) (var211 / 86400000L);
//                    var156[1] = (int) (var211 % 86400000L);
//                    var209 = 1;
//                }
//
//                PushInt(var209);
//                return;
//            case 112:
//                if (argCount != 5) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                String var207 = PopString();
//                String var210 = PopString();
//                String var163 = PopString();
//                String var208 = PopString();
//                var165 = PopString();
//                PushInt(main.MessageForm(var165, var208, var163, var210, var207));
//                return;
//            case 113:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushFloat(Float.log(PopFloat()));
//                return;
//            case 114:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushFloat(Float.exp(PopFloat()));
//                return;
//            case 115:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushFloat(Float.sqrt(PopFloat()));
//                return;
//            case 116:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var26 = PopFloat();
//                PushFloat(degFlag ? Float.sind(var26) : Float.sin(var26));
//                return;
//            case 117:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var161 = PopFloat();
//                PushFloat(degFlag ? Float.tan(var161) : Float.cos(var161));
//                return;
//            case 118:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var161 = PopFloat();
//                PushFloat(degFlag ? Float.sub_5a6(var161) : Float.tand(var161));
//                return;
//            case 119:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var161 = PopFloat();
//                PushFloat(degFlag ? Float.asind(var161) : Float.asin(var161));
//                return;
//            case 120:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var32 = PopFloat();
//                PushFloat(degFlag ? Float.acosd(var32) : Float.acos(var32));
//                return;
//            case 121:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                var161 = PopFloat();
//                PushFloat(degFlag ? Float.atand(var161) : Float.atan(var161));
//                return;
//            case 122:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                if (PopType(0) == 1) {
//                    if (Float.Compare(var161 = PopFloat(), 0) == -1) {
//                        var161 = Float.Negate(var161);
//                    }
//
//                    PushFloat(var161);
//                    return;
//                }
//
//                PushInt(Math.abs(PopInt()));
//                return;
//            case 124:
//                return;
//            case 125:
//                switch (PopType(0)) {
//                    case 0:
//                        var108 = PopInt();
//                        var109 = PopInt();
//                        main.PutInt(var109, var108);
//                        return;
//                    case 1:
//                        var110 = PopFloat();
//                        int var111 = PopInt();
//                        main.PutInt(var111, var110);
//                        return;
//                    case 2:
//                        String var112 = PopString();
//                        var113 = PopInt();
//                        main.PutString(var113, var112);
//                        return;
//                    default:
//                        throw new BasicError(-1, "Invalid Type");
//                }
//            case 126:
//                var108 = (var103 = PopRef()) & 255;
//                var109 = var103 >> 8 & 16777215;
//                var110 = PopInt();
//                switch (varType[var108]) {
//                    case 0:
//                        ((int[]) ((int[]) varObject[var108]))[var109] = main.GetInt(var110);
//                        return;
//                    case 1:
//                        ((int[]) ((int[]) varObject[var108]))[var109] = main.GetInt(var110);
//                        return;
//                    case 2:
//                        ((String[]) ((String[]) varObject[var108]))[var109] = main.GetString(var110);
//                        return;
//                    default:
//                        throw new BasicError(-1, "Invalid Type");
//                }
//            case 127:
//                return;
//            case 128:
//                var55 = PopInt();
//                int var56 = PopInt();
//                int var57 = PopInt();
//                int var58 = PopInt();
//                String var59 = PopString();
//                main.GelGrab(var59, var58, var57, var56, var55);
//                return;
//            case 129:
//                var60 = PopInt();
//                int var61 = PopInt();
//                String var62 = PopString();
//                main.DrawGel(var62, var61, var60);
//                return;
//            case 130:
//                String var63 = PopString();
//                String var64 = PopString();
//                main.SpriteGEL(var64, var63);
//                return;
//            case 131:
//                var55 = PopInt();
//                int var65 = PopInt();
//                String var66 = PopString();
//                main.SpriteMove(var66, var65, var55);
//                return;
//            case 132:
//                if (argCount != 2) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                String var67 = PopString();
//                String var68 = PopString();
//                PushInt(main.SpriteHit(var68, var67));
//                return;
//            case 133:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                String var2;
//                if ((var2 = PopString()).length() > 0) {
//                    dirEnum = main.Directory(var2);
//                }
//
//                if (dirEnum != null && dirEnum.hasMoreElements()) {
//                    PushString((String) dirEnum.nextElement());
//                    return;
//                }
//
//                PushString("");
//                return;
//            case 134:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                String var4;
//                if ((var4 = System.getProperty(PopString())) != null) {
//                    PushString(var4);
//                    return;
//                }
//
//                PushString("");
//                return;
//            case 135:
//                String var53 = PopString();
//                String var54 = PopString();
//                main.GelLoad(var54, var53);
//                return;
//            case 136:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushInt(main.GelWidth(PopString()));
//                return;
//            case 137:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PushInt(main.GelHeight(PopString()));
//                return;
//            case tokPLAYWAV:
//                (new AudioPlayback()).play(PopString());
//                return;
//            case tokPLAYTONE:
//                new AudioPlayback();
//                Object var204 = null;
//                var167 = PopInt();
//                AudioPlayback.playTone(PopInt(), var167);
//                return;
//            case tokINKEY:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushInt(main.INKEY());
//                return;
//            case tokSELECT:
//                String[] var169 = new String[argCount - 1];
//
//                for (int var212 = argCount - 2; var212 >= 0; --var212) {
//                    var169[var212] = PopString();
//                }
//
//                var170 = PopString();
//                PushInt(main.SELECT(var170, var169));
//                return;
//            case tokALERT:
//                var22 = PopInt();
//                var140 = PopInt();
//                var170 = PopString();
//                String var171 = PopString();
//                String var172 = PopString();
//                main.alert(var172, var171, var170, var140, var22);
//                return;
//            case tokSETFONT:
//                int var173 = PopInt();
//                main.canvasScreen.SetFontSize(var173);
//                return;
//            case tokMENUADD:
//                var22 = PopInt();
//                var140 = PopInt();
//                var170 = PopString();
//                main.menuAdd(var170, var140, var22);
//                return;
//            case tokMENUITEM:
//                if (argCount != 1) {
//                    throw new BasicError(17, "Incorrect number of arguments");
//                }
//
//                PopInt();
//                PushString(main.menuItem());
//                return;
//            case tokMENUREMOVE:
//                main.menuRemove(PopString());
//                return;
//            case tokCALL:
//                controlStack[++controlSP] = exeProg;
//                controlStack[++controlSP] = new Integer(exeLen);
//                controlStack[++controlSP] = new Integer(exePC);
//                controlStack[++controlSP] = new Integer(exeNextLinePC);
//                controlStack[++controlSP] = new Integer(0);
//                var_93c = controlSP;
//                main.Delete("main");
//                main.OpenFile(0, "main", false);
//                if ((var90 = main.GetDataOutputChannel(0)) != null) {
//                    SaveTo(var90);
//                }
//
//                main.CloseFile(0);
//                Execute(37);
//                Execute(5);
//                return;
//            case tokENDSUB:
//                New();
//                main.OpenFile(0, "main", true);
//                if ((var82 = main.GetDataInputChannel(0)) != null) {
//                    LoadFrom(var82);
//                }
//
//                try {
//                    main.CloseFile(0);
//                } catch (Exception var174) {
//                }
//
//                if (((Integer) controlStack[var_93c--]).intValue() == 0) {
//                    exeNextLinePC = ((Integer) controlStack[var_93c--]).intValue();
//                    exePC = ((Integer) controlStack[var_93c--]).intValue();
//                    exeLen = ((Integer) controlStack[var_93c--]).intValue();
//                    exeProg = (byte[]) ((byte[]) controlStack[var_93c--]);
//                }
//
//                main.Delete("main");
//                return;
//            case 149:
//            case 150:
//            case 151:
//            case 152:
//            case 153:
//            case 154:
//            case 155:
//            case 156:
//            case 157:
//            case 158:
//            case 159:
//            case 160:
//            case 161:
//            case 162:
//            case 163:
//            case 164:
//            case 165:
//            case 166:
//            case 167:
//            case 168:
//            case 169:
//            case 170:
//            case 171:
//            case 172:
//            case 173:
//            case 174:
//            case 175:
//            case 176:
//            case 177:
//            case 178:
//            case 179:
//            case 180:
//            case 181:
//            case 182:
//            case 183:
//            case 184:
//            case 185:
//            case 186:
//            case 187:
//            case 188:
//            case 189:
//            case 190:
//            case 191:
//            case 192:
//            case 193:
//            case 194:
//            case 195:
//            case 196:
//            case 197:
//            case 198:
//            case 199:
//            case 200:
//            case 201:
//            case 202:
//            case 203:
//            case 204:
//            case 205:
//            case 206:
//            case 207:
//            case 208:
//            case 209:
//            case 210:
//            case 211:
//            case 212:
//            case 213:
//            case 214:
//            case 215:
//            case 216:
//            case 217:
//            case 218:
//            case 219:
//            case 220:
//            case 221:
//            case 222:
//            case 223:
//            case 224:
//            case 225:
//            case 226:
//            case 227:
//            case 228:
//            case 229:
//            case 230:
//            case 231:
//            case 232:
//            case 233:
//            case 234:
//            case 235:
//            case 236:
//            case 237:
//            case 238:
//            case 239:
//            case 240:
//            case 241:
//            case 242:
//            case 243:
//            case 244:
//            case 245:
//            case 248:
//            case 249:
//            case 250:
//            case 251:
//            case 252:
//            case 253:
//            case 254:
//            default:
//                throw new BasicError(-1, "Bad operation: " + oper);
//            case 246:
//                int var168;
//                int var166;
//                int var164;
//                int var205;
//                if ((var164 = PopType(1)) == 0) {
//                    var205 = PopInt();
//                    var167 = (var166 = PopRef()) & 255;
//                    var168 = var166 >> 8 & 16777215;
//                    ((int[]) ((int[]) varObject[var167]))[var168] = var205;
//                    return;
//                }
//
//                if (var164 == 1) {
//                    var205 = PopFloat();
//                    var167 = (var166 = PopRef()) & 255;
//                    var168 = var166 >> 8 & 16777215;
//                    ((int[]) ((int[]) varObject[var167]))[var168] = var205;
//                    return;
//                }
//
//                if (var164 == 2) {
//                    var165 = PopString();
//                    var167 = (var166 = PopRef()) & 255;
//                    var168 = var166 >> 8 & 16777215;
//                    ((String[]) ((String[]) varObject[var167]))[var168] = var165;
//                    return;
//                }
//
//                throw new BasicError(-1, "Bad Variable Type");
//            case 247:
//                var161 = PopInt();
//                int var162 = PopRef();
//                PushOperand(var161 << 8 | var162, (Object) null, varType[var162], (byte) 1);
//                return;
//            case 255:
//                commaCount = 0;
//                return;
//        }
//
//    }
//    
//      
}
