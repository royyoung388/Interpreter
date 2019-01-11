package lexer;

import java.util.regex.Pattern;

class Utils {
    private static String[] keys = {"auto", "break", "case", "char", "const", "continue", "default", "do",
            "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register",
            "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union",
            "unsigned", "void", "volatile", "while", "read", "write"};
    private static String[] types = {"double", "enum", "float", "int", "long", "short", "struct", "union"};

    //运算符集合
    //用于判断运算符后面接运算符的非法情况
    private static char[] operators = {'+', '-', '*', '/', '=', '>', '<', '!', '%'};

    static boolean isAlpha(char ch) {
        return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
    }

    static boolean isNum(char ch) {
        return ch >= '0' && ch <= '9';
    }

    static boolean isEnd(char ch) {
        return isEOF(ch) || isEnter(ch);
    }

    static boolean isEnter(char ch) {
        return ch == '\r' || ch == '\n';
    }

    static boolean isEOF(char ch) {
        return ch == (char) -1;
    }

    static boolean isKey(String word) {
        for (String key : keys)
            if (key.equals(word))
                return true;
        return false;
    }

    /**
     * 判断是否为运算符
     *
     * @return
     */
    static boolean isOperator(char ch) {
        for (char c : operators) {
            if (ch == c) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断一个数是否符合浮点数的形式
     *
     * @return
     */
    static boolean isReal(String num) {
        //小数点在中间
        if (Pattern.matches("[0-9]+\\.[0-9]+", num))
            return true;
        //小数点在前面
        if (Pattern.matches("\\.[0-9]+", num))
            return true;
        //小数点在后面
        if (Pattern.matches("[0-9]+\\.", num))
            return true;
        return false;
    }

    /**
     * 是否为整数
     *
     * @param num
     * @return
     */
    static boolean isInt(String num) {
        if (Pattern.matches("[0-9]+", num))
            return true;
        return false;
    }

    /**
     * 判断是不是一个数据类型的关键字
     *
     * @param type
     * @return
     */
    static boolean isType(String type) {
        for (String t : types) {
            if (type.equals(t))
                return true;
        }
        return false;
    }
}