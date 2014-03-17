package mobilebasic;

public final class BasicError extends RuntimeException {

    public int errorNumber;
    public static final int INTERNAL_ERROR = -1;
    public static final int STACK_EMPTY = 1;
    public static final int STACK_OVERFLOW = 2;
    public static final int LINE_NOT_FOUND = 3;
    public static final int NEXT_BEFORE_FOR = 4;
    public static final int ARRAY_BOUNDS = 5;
    public static final int VALUE_ERROR = 6;
    public static final int INTEGER_EXPECTED = 7;
    public static final int FLOAT_EXPECTED = 8;
    public static final int STRING_EXPECTED = 9;
    public static final int INTEGER_ARRAY_EXPECTED = 10;
    public static final int FLOAT_ARRAY_EXPECTED = 11;
    public static final int STRING_ARRAY_EXPECTED = 12;
    public static final int LVALUE_EXPECTED = 13;
    public static final int RVALUE_EXPECTED = 14;
    public static final int DATA_LINE_ERROR = 15;
    public static final int OUT_OF_DATA = 16;
    public static final int INCORRECT_NUMBER_OF_ARGUMENTS = 17;
    public static final int PARENTHESIS_NESTING_ERROR = 18;
    public static final int EXPRESSION_INCOMPLETE = 19;
    public static final int HASH_EXPECTED = 20;
    public static final int COMMA_EXPECTED = 21;
    public static final int SYNTAX_ERROR = 22;
    public static final int OUT_OF_MEMORY = 23;
    public static final int INVALID_GEL = 0x100;
    public static final int INVALID_SPRITE = 0x101;
    public static final int INVALID_CHANNEL = 0x1000;
    public static final int INVALID_IO_MODE = 0x1001;
    public static final int CHANNEL_ALREADY_IN_USE = 0x1002;
    public static final int CHANNEL_NOT_OPEN = 0x1003;
    public static final int FILE_NOT_FOUND = 0x1004;
    public static final int IO_ERROR = 0x1005;
    public static final int INCOMPATIBLE_FILE_FORMAT = 0x1006;

    public BasicError() {
    }

    public BasicError(String errorMessage) {
        super(errorMessage);
    }

    public BasicError(int errorNumber, String errorMessage) {
        super(errorMessage);
        this.errorNumber = errorNumber;
    }
}
