package lexical;

import java.util.regex.Pattern;

class Utils {
    private static String[] keys = {"auto", "break", "case", "char", "const", "continue", "default", "do",
            "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register",
            "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union",
            "unsigned", "void", "volatile", "while"};

    //运算符集合
    //用于判断运算符后面接运算符的非法情况
    private static char[] operators = {'+', '-', '*', '/', '=', '>', '<', '!', '%'};

    enum Category {
        //关键字，   标识符，         常量，        运算符，       分隔符
        KEYWORD(1), IDENTIFIER(2), CONSTANT(3), OPERATOR(4), DELIMITERS(5);

        private final int value;

        //构造器默认也只能是private, 从而保证构造函数只能在内部使用
        Category(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

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
     * 判断一个数是否符合整数或者浮点数的形式
     *
     * @return
     */
    static boolean isCorrectNum(String num) {
        //没有小数点
        if (Pattern.matches("[0-9]+", num))
            return true;
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

    static void printTuple(Category category, String word, int row, int col) {
        System.out.printf("%d,%d: category: %d , symbol: %s\n", row, col, category.getValue(), word);
    }

    static void printError(int row, int col, String msg) {
        System.out.printf("%d,%d : ERROR: %s\n", row, col, msg);
    }
}
