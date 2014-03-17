package mobilebasic;

public class Float {
    
  public static final int ZERO = 0x00000000;      // fromString("0.0") 
    // DEGRAD = (2. * PI) / 360.0 = 0.017453292519943295769236907684886
  static final int DEGRAD = 0x0d50d8fe; // Float.MakeFP(1745329, -2, false)

    static public int MakeFP(long val, int exp, boolean adjustExp) {
        while (Math.abs(val) > 9999999) {
            val = val / 10;
            if (adjustExp) {
                exp += 1;
            }
        }

        /*
         * If digits is zero then it is not possible to multiply it
         * by 10 any number of times such that the answer will be
         * greater than 1000000. It will always be zero!
         * The result must also be zero since 0 * x = 0
         */

        if (Math.abs(val) > 0) {
            while (Math.abs(val) < 1000000) {
                val = val * 10;
                if (adjustExp) {
                    exp -= 1;
                }
            }
        } else {
            exp = 0;
            val = 0;
        }

        /*
         * Create result Float
         */

        int result;

        if (exp < 0) {
            exp = exp + 128;
        }

        result = ((int) val << 7) | exp;

        return result;
    }

    static public int itof(int ival) {
        return MakeFP(ival, 6, true);
    }

    static public int ftoi(int fp) {
        long val = fp >> 7;
        int exp = fp & 0x7f;

        if (exp > 0x3f) {
            exp = exp - 128;
        }

        //System.out.println("ftoi(" + toString(fp) + ")");
        //System.out.println("  exp=" + exp);
        //System.out.println("  val=" + val);

        // if (exp >= 0)
        //{
        int shift = 6 - exp;

        while (shift > 0) {
            val = val / 10;
            shift--;
        }

        while (shift < 0) {
            val = val * 10;
            shift++;
        }
        //}

        //System.out.println("  val=" + val);

        return (int) val;
    }

    static public String toString(int fp) {
        StringBuffer sb = new StringBuffer();

        int val = fp >> 7;
        int exp = fp & 0x7f;
        if (exp > 0x3f) {
            exp = exp - 128;
        }

        //sb.append("sign=" + sign + ", exp=" + exp + ", val=" + val + " ");

        if (val < 0) {
            sb.append('-');
            val = -val;
        }

        int divisor = 1000000;

        if ((exp > 6) || (exp < -6)) {
            boolean pointFlag = false;

            while (divisor > 0) {
                int digit = val / divisor;
                val = val - digit * divisor;

                sb.append((char) (0x30 + digit));

                divisor = divisor / 10;

                if (!pointFlag) {
                    sb.append('.');
                    pointFlag = true;
                }
            }

            sb.append("E");
            if (exp >= 0) {
                sb.append('+');
            }
            sb.append(exp);
        } else if (exp >= 0) {
            while (divisor > 0) {
                int digit = val / divisor;
                val = val - digit * divisor;

                sb.append((char) (0x30 + digit));

                divisor = divisor / 10;

                if ((exp == 0) && (divisor > 0)) {
                    sb.append(".");
                }

                exp = exp - 1;
                if ((exp < -1) && (val == 0)) {
                    break;
                }
            }
        } else //  exp = -1 or less
        {
            sb.append("0.");

            while (divisor > 0) {
                exp = exp + 1;

                if (exp < 0) {
                    sb.append('0');
                } else {
                    int digit = val / divisor;
                    val = val - digit * divisor;

                    sb.append((char) (0x30 + digit));

                    divisor = divisor / 10;
                }

                if (val == 0) {
                    break;
                }
            }
        }

        return sb.toString();
    }

    static public int fromString(String ascii) {
        int sign = 1;
        int exp = -1;
        int val = 0;
        int expVal = 0;
        int expSign = 1;
        int fp;

        final int ERROR = -1;
        final int SIGN_OR_DIGIT = 0;
        final int DIGIT1 = 1;
        final int DIGIT2 = 2;
        final int EXPSIGN = 3;
        final int GETEXP = 4;

        int state = SIGN_OR_DIGIT;

        int len = ascii.length();

        int offset = 0;
        while (offset < len) {
            char ch = ascii.charAt(offset);

            switch (state) {
                case ERROR:
                    throw new BasicError(8, "Bad Float: " + ascii);

                case SIGN_OR_DIGIT:
                    if (ch == '-') {
                        offset++;
                        sign = -1;
                        state = DIGIT1;
                    } else if ((ch >= '0') && (ch <= '9')) {
                        state = DIGIT1;
                    } else {
                        state = ERROR;
                    }
                    break;

                case DIGIT1:
                    if ((ch >= '0') && (ch <= '9')) {
                        val = val * 10 + (int) (ch - '0');
                        if (val != 0) {
                            exp++;
                        }
                        offset++;
                    } else if (ch == '.') {
                        state = DIGIT2;
                        offset++;
                    } else if ((ch == 'E') || (ch == 'e')) {
                        state = EXPSIGN;
                        offset++;
                    } else {
                        state = ERROR;
                    }
                    break;

                case DIGIT2:
                    if ((ch >= '0') && (ch <= '9')) {
                        val = val * 10 + (int) (ch - '0');
                        if (val == 0) {
                            exp--;
                        }
                        offset++;
                    } else if ((ch == 'E') || (ch == 'e')) {
                        state = EXPSIGN;
                        offset++;
                    } else {
                        state = ERROR;
                    }
                    break;

                case EXPSIGN:
                    if (ch == '+') {
                        expSign = 1;
                        offset++;
                        state = GETEXP;
                    } else if (ch == '-') {
                        expSign = -1;
                        offset++;
                        state = GETEXP;
                    } else {
                        state = ERROR;
                    }
                    break;

                case GETEXP:
                    if ((ch >= '0') && (ch <= '9')) {
                        expVal = expVal * 10 + (int) (ch - '0');
                        offset++;
                    } else {
                        state = ERROR;
                    }
                    break;

                default:
                    state = ERROR;
                    break;
            }
        }

        fp = MakeFP(val * sign, exp + expVal * expSign, false);

        return fp;
    }

    static public int Add(int fp1, int fp2) {
        long val1 = fp1 >> 7;
        long val2 = fp2 >> 7;
        int exp1 = fp1 & 0x7f;
        int exp2 = fp2 & 0x7f;

        if (exp1 > 0x3f) {
            exp1 = exp1 - 128;
        }

        if (exp2 > 0x3f) {
            exp2 = exp2 - 128;
        }

        /*
         * Align values so that they can be added
         */
        /*
        if (exp1 > exp2)
        {
        int expDiff = exp1 - exp2; // 1
        for (int i=0;i<expDiff;i++)
        {
        val1 = val1 * 10;
        exp1--;
        }
        }
        else if (exp1 < exp2)
        {
        int expDiff = exp2 - exp1;
        for (int i=0;i<expDiff;i++)
        {
        val2 = val2 * 10;
        exp2--;
        }
        }
         */
        while (exp1 > exp2) {
            val2 = val2 / 10;
            exp2++;
        }

        while (exp1 < exp2) {
            val1 = val1 / 10;
            exp1++;
        }

        val1 = val1 + val2;

        return MakeFP(val1, exp1, true);
    }

    static public int Subtract(int fp1, int fp2) {
//System.out.print(toString(fp1) + "-" + toString(fp2) + "=");
        long val1 = fp1 >> 7;
        long val2 = fp2 >> 7;
        int exp1 = fp1 & 0x7f;
        int exp2 = fp2 & 0x7f;

        if (exp1 > 0x3f) {
            exp1 = exp1 - 128;
        }

        if (exp2 > 0x3f) {
            exp2 = exp2 - 128;
        }

        /*
         * Align values so that they can be subtracted
         */
        /*
        if (exp1 > exp2)
        {
        int expDiff = exp1 - exp2; // 1
        for (int i=0;i<expDiff;i++)
        {
        val1 = val1 * 10;
        exp1--;
        }
        }
        else if (exp1 < exp2)
        {
        int expDiff = exp2 - exp1;
        for (int i=0;i<expDiff;i++)
        {
        val2 = val2 * 10;
        exp2--;
        }
        }
         */

        while (exp1 > exp2) {
            val2 = val2 / 10;
            exp2++;
        }

        while (exp1 < exp2) {
            val1 = val1 / 10;
            exp1++;
        }

        val1 = val1 - val2;

        return MakeFP(val1, exp1, true);
    }

    static public int Multiply(int fp1, int fp2) {
        long val1 = fp1 >> 7;
        long val2 = fp2 >> 7;
        int exp1 = fp1 & 0x7f;
        int exp2 = fp2 & 0x7f;

        if (exp1 > 0x3f) {
            exp1 = exp1 - 128;
        }

        if (exp2 > 0x3f) {
            exp2 = exp2 - 128;
        }

        val1 = (val1 * val2) / 1000000;
        exp1 += exp2;

        return MakeFP(val1, exp1, true);
    }

    static public int Divide(int fp1, int fp2) {
        long val1 = fp1 >> 7;
        long val2 = fp2 >> 7;
        int exp1 = fp1 & 0x7f;
        int exp2 = fp2 & 0x7f;

        if (exp1 > 0x3f) {
            exp1 = exp1 - 128;
        }

        if (exp2 > 0x3f) {
            exp2 = exp2 - 128;
        }

        val1 = (val1 * 1000000) / val2;
        exp1 -= exp2;

        return MakeFP(val1, exp1, true);
    }

    /*
     *
     * fp1 < fp2 = -1
     * fp1 = fp2 =  0
     * fp1 > fp2 =  1
     */
    static public int Compare(int fp1, int fp2) {
        int result;

        if (fp1 == fp2) {
            result = 0;
        } else {
            int diff = Subtract(fp1, fp2);
//System.out.println(toString(fp1) + " - " + toString(fp2) + " = " + toString(diff));
            if (diff < 0) {
                result = -1;
            } else {
                result = 1;
            }
        }

        return result;
    }

    static public int Negate(int fp) {
        long val = fp >> 7;
        int exp = fp & 0x7f;

        if (exp > 0x3f) {
            exp = exp - 128;
        }

        val = -val;

        return MakeFP(val, exp, true);
    }

    /*
     * Log(x+1) = x - (x^2 / 2) + (x^3 / 3) - (x^4 / 4) + ...
     *
     * where 1 > X >= 0
     */
    public static int log(int var0) {
        boolean var1 = false;
        if (Compare(var0, 0) <= 0) {
            throw new BasicError(6, "log(" + toString(var0) + ") : Invalid input");
        } else {
            int var2;
            for (var2 = 0; Compare(var0, 128000000) < 1; var2 = Subtract(var2, 128000000)) {
                var0 = Multiply(var0, 347940096);
            }

            while (Compare(var0, 128000000) >= 1) {
                var0 = Divide(var0, 347940096);
                var2 = Add(var2, 128000000);
            }

            int var10000 = var0 = Subtract(var0, 128000000);
            int var3 = Negate(Multiply(var10000, var10000));
            int var4 = 256000000;
            int var7 = var0;
            int var5 = var0 + 1;
            int var6 = 0;

            while (var7 != var5) {
                var5 = var7;
                var7 = Add(var7, Divide(var3, var4));
                var3 = Negate(Multiply(var3, var0));
                var4 = Add(var4, 128000000);
                if (var6++ > 1000) {
                    break;
                }
            }

            var7 = Add(var2, var7);
            return var7;
        }
    }

    /*
     * Exp(x) = 1 + x + (x^2 / 2!) + (x^3 / 3!) + (x^4 / 4!) + ...
     * This function doesn't converge for small -ve values of
     * < about -6. This seems to be due to integer rounding errors.
     *
     * This can be solved by remembering that: e^(-x) = 1/(e^x)
     */
    public static int exp(int x) {
        int result;
        if (Compare(x, 0) < 0) {
            result = exp2(Negate(x));
            result = Divide(128000000, result);
        } else {
            result = exp2(x);
        }

        return result;
    }

    private static int exp2(int var0) {
        int var1 = 128000000;
        int var2 = var0;
        int var3 = 128000000;
        int var4 = 128000000;
        int var5 = var1 + 1;
        int var6 = 0;

        while (var1 != var5) {
            var5 = var1;
            int var7 = Divide(var2, var3);
            var1 = Add(var1, var7);
            var2 = Multiply(var2, var0);
            var4 = Add(var4, 128000000);
            var3 = Multiply(var3, var4);
            if (var6++ > 1000) {
                break;
            }
        }

        if (Compare(var1, 0) < 0) {
            var1 = 0;
        }

        return var1;
    }

    public static int sqrt(int x) {
        int result;
        if ((result = Compare(x, 0)) > 0) {
            result = exp(Divide(log(x), 256000000));
        } else {
            if (result != 0) {
                throw new BasicError(6, "sqrt(" + toString(x) + ") : Invalid input");
            }

            result = 0;
        }

        return result;
    }

    /*
     * Power(x,y) = Exp(Log(x) * y)
     */
    public static int pow(int x, int y) {
        int result;
        if ((result = Compare(x, 0)) > 0) {
            result = exp(Multiply(log(x), y));
        } else if (result == 0) {
            result = 0;
        } else {
            int var3 = itof(ftoi(y));
            if (Compare(Subtract(y, var3), 0) != 0) {
                throw new BasicError(6, "pow(" + toString(x) + "," + toString(y) + ") : Invalid input");
            }

            result = exp(Multiply(log(Negate(x)), y));
            if ((var3 & 1) == 1) {
                result = Negate(result);
            }
        }

        return result;
    }

    /*
     * Sin(x) = x - (x^3 / 3!) + (x^5 / 5!) - (x^7 / 7!) + ...
     *
     * Also
     *   Sin(x) = t/sqrt(1+t^2) where t = tan(x)
     */
    public static int sin(int x) {
        int var1 = itof(ftoi(Divide(x, 804247680)));
        int var2 = Multiply(804247680, var1);
        x = Subtract(x, var2);
        int result = 0;
        int var4 = x;
        int var5 = 128000000;
        int var6 = 128000000;
        int var7 = 1;
        int var8 = 0;

        while (result != var7) {
            var7 = result;
            result = Add(result, Divide(var4, var5));
            var4 = Negate(Multiply(var4, Multiply(x, x)));
            var6 = Add(var6, 128000000);
            var5 = Multiply(var5, var6);
            var6 = Add(var6, 128000000);
            var5 = Multiply(var5, var6);
            if (var8++ > 1000) {
                break;
            }
        }

        return result;
    }

    public static int sind(int x) {
        return sin(Multiply(x, 223402238));
    }

    /*
     * Cos(x) = 1 - (x^2 / 2!) + (x^4 / 4!) - (x^6 / 6!) + ...
     *
     * Also
     *   Cos(x) = 1 / sqrt(1+t^2) where t = tan(x)
     *   Cos(x) = Sin(X + Pi/2)
     */
    public static int cos(int var0) {
        return sin(Add(var0, 201061888));
    }
    
    
    static public int cosd(int x)
  {
      return cos(Multiply(x, DEGRAD));
  }

    /*
     * Tan(x) = Sin(x) / Cos(x)
     */
    public static int tan(int x) {
        return cos(Multiply(x, 223402238));
    }

    public static int tand(int x) {
        int var1;
        if (Compare(var1 = cos(x), 0) != 0) {
            int var2 = Divide(sin(x), var1);
            return var2;
        } else {
            throw new BasicError(6, "tan(" + toString(x) + ") : Invalid input");
        }
    }

    public static int sub_5a6(int var0) {
        return tand(Multiply(var0, 223402238));
    }

    public static int asin(int x) {
        if (Compare(x, -128000000) >= 0 && Compare(x, 128000000) <= 0) {
            int var1 = Multiply(x, x);
            if (Compare(var1 = sqrt(Subtract(128000000, var1)), 0) != 0) {
                var1 = atan(Divide(x, var1));
            } else if (Compare(x, 0) < 0) {
                var1 = Negate(201061888);
            } else {
                var1 = 201061888;
            }

            return var1;
        } else {
            throw new BasicError(6, "asin(" + toString(x) + ") : Invalid input");
        }
    }

    public static int asind(int var0) {
        return Multiply(asin(var0), 733385985);
    }

    /*
     * ACos(x) = ATan(sqrt(1-x*x)/x)
     *
     * Also
     *   ACos(x) = ACot(x/sqrt(1-x*x))
     *   ACos(x) = Asin(sqrt(1-x*x))
     *   ACos(x) = 0.5 * PI * asin(-x)
     *   ACos(x) = 0.5 * PI - asin(x)
     */
    public static int acos(int var0) {
        return Subtract(Divide(402123904, 256000000), asin(var0));
    }

    public static int acosd(int var0) {
        return Multiply(acos(var0), 733385985);
    }

    /*
     * ATan(x) = x - (x^3 / 3) + (x^5 / 5) - (x^7 / 7) + ...
     */
    public static int atan(int x) {
        int var1 = Negate(128000000);
        int var2 = MakeFP(1570796L, 0, false);
        boolean var3 = false;
        int var4 = Compare(x, var1);
        int var5 = Compare(x, 128000000);
        int result;
        if (var4 == 0) {
            result = Negate(1005309823);
        } else if (var5 == 0) {
            result = 1005309823;
        } else if (var4 < 0) {
            result = atan2(Divide(128000000, x));
            result = Subtract(Negate(var2), result);
        } else if (var5 > 0) {
            result = atan2(Divide(128000000, x));
            result = Subtract(var2, result);
        } else {
            result = atan2(x);
        }

        return result;
    }

    public static int atand(int x) {
        return Multiply(atan(x), 733385985);
    }

    /*
     * atan2 is just a support routine for atan. It is not a atan2(x,y) function!
     */
    private static int atan2(int x) {
        int result = 0;
        int var2 = x;
        int var3 = 128000000;
        int var4 = 1;
        int var5 = 0;

        while (result != var4) {
            var4 = result;
            result = Add(result, Divide(var2, var3));
            var2 = Negate(Multiply(var2, Multiply(x, x)));
            var3 = Add(var3, 256000000);
            if (var5++ > 1000) {
                break;
            }
        }

        return result;
    }
}
