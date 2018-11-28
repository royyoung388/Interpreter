package lexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import static lexer.Utils.*;

public class Lexer {
    private BufferedReader br = null;
    //行号和列号
    private int rowNum = 1, colNum = 1;
    private char ch;
    //所有的token
    private ArrayList<Token> tokens;

    public enum Category {
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
        DIGIT("DIGIT"), LETTER("LETTER"), ID("ID"), UN_AR_OP("UN_AR_OP"), SELF_OP("SELF_OP"),
        UN_LOG_OP("UN_LOG_OP"), BIN_AR_OP_1("BIN_AR_OP_1"), BIN_AR_OP_2("BIN_AR_OP_2"), LOGIC_OP("LOGIC_OP"),
        RELATION_OP("RELATION_OP"), BIT_OP("BIT_OP"), ASS_OP("ASS_OP"), INT("INT"), REAL("REAL"),
        CHAR("CHAR"), STRING("STRING"), BOOL("BOOL"), TYPE("TYPE"), DELIMITERS("DELIMITERS");

        private final String value;

        //构造器默认也只能是private, 从而保证构造函数只能在内部使用
        Category(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public Lexer() {
        tokens = new ArrayList<>();
    }

    public ArrayList<Token> parse(String filePath) throws IOException {
        br = new BufferedReader(new FileReader(filePath));

        ch = (char) br.read();

        while (!isEOF(ch)) {
            if (isAlpha(ch) || ch == '_') {
                handleAlpha();
            } else if (isNum(ch)) {
                handleNum();
            } else {
                handleOther();
            }
        }

        return tokens;
    }

    /**
     * 处理字母开头或者下划线开头
     *
     * @throws IOException
     */
    private void handleAlpha() throws IOException {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        sb.append(ch);

        ch = (char) br.read();
        i++;

        while (ch == '_' || isAlpha(ch) || isNum(ch)) {
            sb.append(ch);
            ch = (char) br.read();
            i++;
        }

        String word = sb.toString();
        //是关键字
        if (isKey(word)) {
//            printTuple(Category.KEYWORD, word, rowNum, colNum);
            Token token = new Token(word, word, rowNum, colNum);
            if (isType(word))
                token.setCategory(Category.TYPE);
            token.print();
            tokens.add(token);
        } else if (word.equals("true") || word.equals("false")) {
            //是布尔变量
//            printTuple(Category.CONSTANT, word, rowNum, colNum);
            Token token = new Token(Category.BOOL, word, rowNum, colNum);
            token.print();
            tokens.add(token);
        } else {
            //是标识符
//            printTuple(Category.IDENTIFIER, word, rowNum, colNum);
            Token token = new Token(Category.ID, word, rowNum, colNum);
            token.print();
            tokens.add(token);
        }

        colNum += i;
    }

    /**
     * 处理以数字开头的整数或者浮点数
     *
     * @throws IOException
     */
    private void handleNum() throws IOException {
        int i = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(ch);

        ch = (char) br.read();
        i++;

        while (isNum(ch) || ch == '.') {
            sb.append(ch);
            ch = (char) br.read();
            i++;
        }

        //判断数字开头的标识符
        while (isAlpha(ch) || ch == '_') {
            sb.append(ch);
            ch = (char) br.read();
            i++;
        }

        String word = sb.toString();

        if (isInt(word)) {
//            printTuple(Category.CONSTANT, word, rowNum, colNum);
            Token token = new Token(Category.INT, word, rowNum, colNum);
            token.print();
            tokens.add(token);
        } else if (isReal(word)) {
            Token token = new Token(Category.REAL, word, rowNum, colNum);
            token.print();
            tokens.add(token);
        } else
            printError(rowNum, colNum, "非法数字: " + sb.toString());
        colNum += i;
    }

    /**
     * 处理以小数点开头的浮点数
     */
    private void handleDouble() throws IOException {
        int i = 1;

        StringBuilder sb = new StringBuilder();
        sb.append('.');
        sb.append(ch);

        ch = (char) br.read();
        i++;

        while (isNum(ch) || ch == '.') {
            sb.append(ch);
            ch = (char) br.read();
            i++;
        }

        String word = sb.toString();

        if (isReal(word)) {
//            printTuple(Category.REAL, word, rowNum, colNum);
            Token token = new Token(Category.REAL, word, rowNum, colNum);
            token.print();
            tokens.add(token);
        } else
            printError(rowNum, colNum, "非法数字: " + sb.toString());

        colNum += i;
    }

    /**
     * 解析非数字和字母开头
     *
     * @throws IOException
     */
    private void handleOther() throws IOException {
        Token token;

        switch (ch) {
            case ' ':
                ch = (char) br.read();
                colNum++;
                break;
            case '\t':
                ch = (char) br.read();
                colNum += 5;
                break;
            case '\r':
                ch = (char) br.read();
                if (ch == '\n') {
                    ch = (char) br.read();
                }
                colNum = 1;
                rowNum++;
                break;
            case '\n':
                ch = (char) br.read();
                colNum = 1;
                rowNum++;
                break;
            case '[':
            case ']':
            case '(':
            case ')':
            case '{':
            case '}':
//                System.out.println("双界符");
//                printTuple(Category.DELIMITERS, "" + ch, rowNum, colNum);
                token = new Token(ch + "", ch + "", rowNum, colNum);
                token.print();
                tokens.add(token);
                ch = (char) br.read();
                colNum++;
                break;
            case ':':
            case ',':
            case ';':
//                System.out.println("单界符");
//                printTuple(Category.DELIMITERS, "" + ch, rowNum, colNum);
                token = new Token(ch + "", ch + "", rowNum, colNum);
                token.print();
                tokens.add(token);
                ch = (char) br.read();
                colNum++;
                break;
            case '.':
                ch = (char) br.read();
                if (isNum(ch)) {
                    handleDouble();
                } else {
//                    System.out.println("单界符");
//                    printTuple(Category.DELIMITERS, "" + ch, rowNum, colNum);
                    token = new Token(ch + "", ch + "", rowNum, colNum);
                    token.print();
                    tokens.add(token);
                    ch = (char) br.read();
                    colNum++;
                }
                break;
            case '\'':
                handleChar();
                break;
            case '\"':
                handleString();
                break;
            case '+':
                handlePlus();
                break;
            case '-':
                handleMinus();
                break;
            case '*':
            case '%':
                handleOperator();
                break;
            case '=':
            case '!':
                handleOperator();
                break;
            case '/':
                handleDiv();
                break;
//            case '!':
//            case '=':
//                String s = "" + ch;
//
//                ch = (char) br.read();
//                if (ch == '=') {
//                    s = s + ch;
//                    printTuple(Category.OPERATOR, s, rowNum, colNum);
//                    ch = (char) br.read();
//                    colNum += 2;
//                } else {
//                    printTuple(Category.OPERATOR, s, rowNum, colNum);
//                    colNum++;
//                }
//
//                break;
            case '>':
                handleGreater();
                break;
            case '<':
                handleLess();
                break;
//            case '%':
//                ch = (char) br.read();
//
//                if (ch == '=') {
//                    printTuple(Category.OPERATOR, "%=", rowNum, colNum);
//                    ch = (char) br.read();
//                    colNum += 2;
//                } else {
//                    printTuple(Category.OPERATOR, "%", rowNum, colNum);
//                    colNum++;
//                }
//                break;
            default:
                //todo 错误处理
                printError(rowNum, colNum, "无法识别的符号");
                ch = (char) br.read();
                colNum++;
        }
    }

    /**
     * 处理字符
     */
    private void handleChar() throws IOException {
        int i = 0;

        StringBuilder sb = new StringBuilder();
        sb.append('\'');

        ch = (char) br.read();
        i++;

        while (ch != '\'') {
            if (isEnd(ch)) {
                //todo 错误处理
                printError(rowNum, colNum, "字符没有结束");
                break;
            } else {
                sb.append(ch);
            }

            ch = (char) br.read();
            i++;
        }

        if (ch == '\'') {
            sb.append('\'');
            ch = (char) br.read();
            i++;
//            printTuple(Category.CONSTANT, sb.toString(), rowNum, colNum);
            Token token = new Token(Category.CHAR, sb.toString(), rowNum, colNum);
            token.print();
            tokens.add(token);
        }

        colNum += i;
    }

    /**
     * 处理字符串
     */
    private void handleString() throws IOException {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        sb.append('\"');

        ch = (char) br.read();
        i++;

        while (ch != '\"') {
            if (isEnd(ch)) {
                //todo 错误处理
                printError(rowNum, colNum, "字符串没有结束");
                break;
            } else {
                sb.append(ch);
            }

            ch = (char) br.read();
            i++;
        }

        if (ch == '\"') {
            sb.append('\"');
            ch = (char) br.read();
            i++;
//            printTuple(Category.CONSTANT, sb.toString(), rowNum, colNum);
            Token token = new Token(Category.STRING, sb.toString(), rowNum, colNum);
            token.print();
            tokens.add(token);
        }

        colNum += i;
    }

    /**
     * 处理加号
     */
    private void handlePlus() throws IOException {
        //默认 +
        Token token = new Token(Category.BIN_AR_OP_2, ch + "", rowNum, colNum);
        ;
        StringBuilder sb = new StringBuilder();
        int i = 1;

        sb.append(ch);

        ch = (char) br.read();

        //++
        if (ch == '+') {
            sb.append(ch);
            token = new Token(Category.SELF_OP, sb.toString(), rowNum, colNum);
            ch = (char) br.read();
            i++;
        } else if (ch == '=') {
            //+=
            sb.append(ch);
            token = new Token(Category.ASS_OP, sb.toString(), rowNum, colNum);
            ch = (char) br.read();
            i++;
        }

        boolean illegal = false;
        //如果后面还有运算符，则为非法
        while (isOperator(ch)) {
            illegal = true;
            sb.append(ch);
            ch = (char) br.read();
            i++;
        }

        //如果后面还有运算符，则为非法
        if (illegal)
            printError(rowNum, colNum, "非法运算符: " + sb.toString());
        else {
//            printTuple(Category.OPERATOR, sb.toString(), rowNum, colNum);
            token.print();
            tokens.add(token);
        }

        colNum += i;
    }

    /**
     * 处理减号
     */
    private void handleMinus() throws IOException {
        //默认 -
        Token token = new Token(Category.BIN_AR_OP_2, ch + "", rowNum, colNum);

        StringBuilder sb = new StringBuilder();
        int i = 1;

        sb.append(ch);

        ch = (char) br.read();

        // 负数
        if (isNum(ch)) {
            while (isNum(ch) || ch == '.') {
                sb.append(ch);
                ch = (char) br.read();
                i++;
            }

            try {
                int integer = Integer.parseInt(sb.toString());
//                printTuple(Category.CONSTANT, sb.toString(), rowNum, colNum);
                token = new Token(Category.INT, sb.toString(), rowNum, colNum);
                token.print();
                tokens.add(token);
            } catch (Exception e) {
                try {
                    double d = Double.parseDouble(sb.toString());
//                    printTuple(Category.CONSTANT, sb.toString(), rowNum, colNum);
                    token = new Token(Category.REAL, sb.toString(), rowNum, colNum);
                    token.print();
                    tokens.add(token);
                } catch (Exception exc) {
                    printError(rowNum, colNum, "数字出现未知错误");
                }
            } finally {
                colNum += i;
                return;
            }
        }

        // --
        if (ch == '-') {
            sb.append(ch);
            token = new Token(Category.SELF_OP, sb.toString(), rowNum, colNum);
            ch = (char) br.read();
            i++;
        } else if (ch == '=') {
            //+=
            sb.append(ch);
            token = new Token(Category.ASS_OP, sb.toString(), rowNum, colNum);
            ch = (char) br.read();
            i++;
        }

        boolean illegal = false;
        //如果后面还有运算符，则为非法
        while (isOperator(ch)) {
            illegal = true;
            sb.append(ch);
            ch = (char) br.read();
            i++;
        }

        //如果后面还有运算符，则为非法
        if (illegal)
            printError(rowNum, colNum, "非法运算符");
        else {
//            printTuple(Category.OPERATOR, sb.toString(), rowNum, colNum);
            token.print();
            tokens.add(token);
        }

        colNum += i;

//        ch = (char) br.read();
//
//        if (ch == '-') {
//            printTuple(Category.OPERATOR, "--", rowNum, colNum);
//            ch = (char) br.read();
//            colNum += 2;
//        } else if (ch == '=') {
//            printTuple(Category.OPERATOR, "-=", rowNum, colNum);
//            ch = (char) br.read();
//            colNum += 2;
//        } else {
//            printTuple(Category.OPERATOR, "-", rowNum, colNum);
//            colNum++;
//        }
    }

    /**
     * 处理 * % = ！
     *
     * @throws IOException
     */
    private void handleOperator() throws IOException {
        Token token = null;
        StringBuilder sb = new StringBuilder();
        sb.append(ch);

        int i = 1;

        switch (ch) {
            case '*':
            case '%':
                token = new Token(Category.BIN_AR_OP_1, sb.toString(), rowNum, colNum);
                break;
            case '!':
                token = new Token(Category.UN_LOG_OP, sb.toString(), rowNum, colNum);
                break;
            case '=':
                token = new Token(Category.ASS_OP, sb.toString(), rowNum, colNum);
                break;
        }

        ch = (char) br.read();

        if (ch == '=') {
            //+=
            sb.append(ch);
            ch = (char) br.read();
            i++;

            switch (sb.toString()) {
                case "*=":
                case "%=":
                case "==":
                    token = new Token(Category.ASS_OP, sb.toString(), rowNum, colNum);
                    break;
                case "!=":
                    token = new Token(Category.RELATION_OP, sb.toString(), rowNum, colNum);

                    break;
            }
        }

        boolean illegal = false;
        //如果后面还有运算符，则为非法
        while (isOperator(ch)) {
            illegal = true;
            sb.append(ch);
            ch = (char) br.read();
            i++;
        }

        //如果后面还有运算符，则为非法
        if (illegal)
            printError(rowNum, colNum, "非法运算符: " + sb.toString());
        else {
//            printTuple(Category.OPERATOR, sb.toString(), rowNum, colNum);
            token.print();
            tokens.add(token);
        }

        colNum += i;

//        ch = (char) br.read();
//
//        if (ch == '=') {
//            printTuple(Category.OPERATOR, "*=", rowNum, colNum);
//            ch = (char) br.read();
//            colNum += 2;
//        } else {
//            printTuple(Category.OPERATOR, "*", rowNum, colNum);
//            colNum++;
//            handleOther();
//        }
    }

    /**
     * 处理除法
     *
     * @throws IOException
     */
    private void handleDiv() throws IOException {
        //默认 /
        Token token = new Token(Category.BIN_AR_OP_1, ch + "", rowNum, colNum);

        ch = (char) br.read();

        if (ch == '=') {
//            printTuple(Category.OPERATOR, "/=", rowNum, colNum);
            token = new Token(Category.ASS_OP, "/=", rowNum, colNum);
            token.print();
            tokens.add(token);

            ch = (char) br.read();
            colNum += 2;
        } else if (ch == '*') {
            //处理多行注释
            token = new Token(Category.DELIMITERS, "/*", rowNum, colNum);
            token.print();

            int i = 1;

            do {
                ch = (char) br.read();

                if (ch == '*') {
                    ch = (char) br.read();
                    i++;
                    if (ch == '/') {
//                        printTuple(Category.DELIMITERS, "多行注释", rowNum, colNum);
                        token = new Token(Category.DELIMITERS, "*/", rowNum, colNum);
                        token.print();

                        ch = (char) br.read();
                        colNum += i + 1;
                        return;
                    }
                }

                if (isEnter(ch)) {
                    ch = (char) br.read();
                    rowNum++;
                    colNum = 1;
                    i = 0;
                }
            } while (!isEOF(ch));

            //ch == -1
            //todo 错误处理
            printError(rowNum, colNum, "多行注释没有结束");
        } else if (ch == '/') {
            //单行注释
//            printTuple(Category.DELIMITERS, "单行注释", rowNum, colNum);
            token = new Token(Category.DELIMITERS, "//", rowNum, colNum);
            token.print();

            br.readLine();
            ch = (char) br.read();
            rowNum++;
            colNum = 1;
        } else {
            //除法
//            printTuple(Category.OPERATOR, "/", rowNum, colNum);
            token.print();
            tokens.add(token);

            colNum++;
        }
    }

    /**
     * 处理大于 >
     */
    private void handleGreater() throws IOException {
        //默认 >
        Token token = new Token(Category.RELATION_OP, ch + "", rowNum, colNum);

        ch = (char) br.read();

        switch (ch) {
            case '>':
                token.setCategory(Category.BIT_OP);
                //注意这里没有break
            case '=':
                String s = ">" + ch;

//                printTuple(Category.OPERATOR, s, rowNum, colNum);
                token.setSymbol(s);
                token.print();
                tokens.add(token);

                ch = (char) br.read();
                colNum += 2;
                break;
            default:
//                printTuple(Category.OPERATOR, ">", rowNum, colNum);
                token.print();
                tokens.add(token);

                colNum++;
                break;
        }
    }

    /**
     * 处理小于
     *
     * @throws IOException
     */
    private void handleLess() throws IOException {
        //默认 <
        Token token = new Token(Category.RELATION_OP, ch + "", rowNum, colNum);

        ch = (char) br.read();

        switch (ch) {
            case '<':
                token.setCategory(Category.BIT_OP);
                //注意这里没有break
            case '=':
                String s = "<" + ch;

//                printTuple(Category.OPERATOR, s, rowNum, colNum);
                token.setSymbol(s);
                token.print();
                tokens.add(token);

                ch = (char) br.read();
                colNum += 2;
                break;
            default:
//                printTuple(Category.OPERATOR, "<", rowNum, colNum);
                token.print();
                tokens.add(token);

                colNum++;
                break;
        }
    }

    private void printError(int row, int col, String msg) {
        System.out.printf("%d,%d : ERROR: %s\n", row, col, msg);
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        ArrayList<Token> tokens = lexer.parse("test.c");

        System.out.println();

        for (Token token : tokens) {
            token.testPrint();
        }
    }
}
