package javacc;

public class Category {
    /**
     * 未被定义的符号直接以自身表示
     * <p>
     * DIGIT	数字
     * LETTER	字母
     * ID	标识符
     * UN_AR_OP	一元算术运算符 -
     * SELF_OP	自增自减运算符 ++ --
     * UN_LOG_OP	一元逻辑运算符 !
     * BIN_AR_OP_1	第一优先级二元算术运算符 * / %
     * BIN_AR_OP_2	第二优先级二元算术运算符 + -
     * LOGIC_OP	逻辑运算符 && || !
     * RELATION_OP	关系运算符 > < >= <= == !=
     * BIT_OP	位运算 >> <<
     * ASS_OP	赋值运算符 = += -= *= /= %=
     * INT	整数
     * REAL	实数
     * CHAR	字符
     * STRING	字符串
     * BOOL	布尔变量 true false
     * TYPE	数据类型 int float double long short char
     * DELIMITERS	多行注释 单行注释
     */
    public static final String ID = "ID";
    public static final String SELF_OP = "SELF_OP";
    public static final String UN_LOG_OP = "UN_LOG_OP";
    public static final String BIN_AR_OP_1 = "BIN_AR_OP_1";
    public static final String BIN_AR_OP_2 = "BIN_AR_OP_2";
    public static final String LOGIC_OP = "LOGIC_OP";
    public static final String RELATION_OP = "RELATION_OP";
    public static final String BIT_OP = "BIT_OP";
    public static final String ASS_OP = "ASS_OP";
    public static final String INT = "INT";
    public static final String REAL = "REAL";
    public static final String CHAR = "CHAR";
    public static final String STRING = "STRING";
    public static final String BOOL = "BOOL";
    public static final String VOID = "VOID";
    public static final String DELIMITERS = "DELIMITERS";
}
