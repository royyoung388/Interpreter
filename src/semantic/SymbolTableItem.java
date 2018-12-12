package semantic;

/**
 * 符号表的表项
 */
public class SymbolTableItem {
    String name;
    Type type;
}

/**
 * 所有符号的类型
 */
class Type {
    public static final String CHAR = "char";
    public static final String UNSIGNEDCHAR = "unsigned char";
    public static final String SIGNEDCHAR = "singned char";
    public static final String SHORT = "short";
    public static final String UNSIGNEDSHORT = "unsigned short";
    public static final String LONG = "long";
    public static final String UNSIGNEDLONG = "unsigned long";
    public static final String FLOAT = "float";
    public static final String DOUBLE = "double";
    public static final String LONGDOUBLE = "long double";
    public static final String VOID = "void";
}