package lexical;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import static lexical.Utils.*;

public class Lexical {
    BufferedReader br = null;
    //行号和列号
    int rowNum = 1, colNum = 1;
    char ch;

    public static void main(String[] args) throws IOException {
        Lexical lexical = new Lexical();
        lexical.parse("test.c");
    }

    public void parse(String filePath) throws IOException {
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
            printTuple(Category.KEYWORD, word, rowNum, colNum);
        } else if (word.equals("true") || word.equals("false")) {
            //是布尔变量
            printTuple(Category.CONSTANT, word, rowNum, colNum);
        } else {
            //是标识符
            printTuple(Category.IDENTIFIER, word, rowNum, colNum);
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

        if (isCorrectNum(word))
            printTuple(Category.CONSTANT, word, rowNum, colNum);
        else
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

        if (isCorrectNum(word))
            printTuple(Category.CONSTANT, word, rowNum, colNum);
        else
            printError(rowNum, colNum, "非法数字: " + sb.toString());

        colNum += i;
    }

    /**
     * 解析非数字和字母开头
     *
     * @throws IOException
     */
    private void handleOther() throws IOException {
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
                printTuple(Category.DELIMITERS, "" + ch, rowNum, colNum);
                ch = (char) br.read();
                colNum++;
                break;
            case ':':
            case ',':
            case ';':
//                System.out.println("单界符");
                printTuple(Category.DELIMITERS, "" + ch, rowNum, colNum);
                ch = (char) br.read();
                colNum++;
                break;
            case '.':
                ch = (char) br.read();
                if (isNum(ch)) {
                    handleDouble();
                } else {
//                    System.out.println("单界符");
                    printTuple(Category.DELIMITERS, "" + ch, rowNum, colNum);
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
        ch = (char) br.read();
        i++;

        while (ch != '\'') {
            if (isEnd(ch)) {
                //todo 错误处理
                printError(rowNum, colNum, "字符没有结束");
            } else {
                sb.append(ch);
            }

            ch = (char) br.read();
            i++;
        }
        colNum += i;

        printTuple(Category.CONSTANT, sb.toString(), rowNum, colNum);
    }

    /**
     * 处理字符串
     */
    private void handleString() throws IOException {
        int i = 0;
        StringBuilder sb = new StringBuilder();
        ch = (char) br.read();
        i++;

        while (ch != '\"') {
            if (isEnd(ch)) {
                //todo 错误处理
                printError(rowNum, colNum, "字符串没有结束");
            } else {
                sb.append(ch);
            }

            ch = (char) br.read();
            i++;
        }
        colNum += i;

        printTuple(Category.CONSTANT, sb.toString(), rowNum, colNum);
    }

    /**
     * 处理加号
     */
    private void handlePlus() throws IOException {
        StringBuilder sb = new StringBuilder();
        int i = 1;

        sb.append(ch);

        ch = (char) br.read();

        //++
        if (ch == '+') {
            sb.append(ch);
            ch = (char) br.read();
            i++;
        } else if (ch == '=') {
            //+=
            sb.append(ch);
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
        else
            printTuple(Category.OPERATOR, sb.toString(), rowNum, colNum);

        colNum += i;
    }

    /**
     * 处理减号
     */
    private void handleMinus() throws IOException {
        StringBuilder sb = new StringBuilder();
        int i = 1;

        sb.append(ch);

        ch = (char) br.read();

        //--
        if (ch == '-') {
            sb.append(ch);
            ch = (char) br.read();
            i++;
        } else if (ch == '=') {
            //+=
            sb.append(ch);
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
        else
            printTuple(Category.OPERATOR, sb.toString(), rowNum, colNum);

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
     */
    private void handleOperator() throws IOException {
        StringBuilder sb = new StringBuilder();
        int i = 1;

        sb.append(ch);

        ch = (char) br.read();

        if (ch == '=') {
            //+=
            sb.append(ch);
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
        else
            printTuple(Category.OPERATOR, sb.toString(), rowNum, colNum);

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
        ch = (char) br.read();

        if (ch == '=') {
            printTuple(Category.OPERATOR, "/=", rowNum, colNum);
            ch = (char) br.read();
            colNum += 2;
        } else if (ch == '*') {
            //处理多行注释
            int i = 1;


            do {
                ch = (char) br.read();

                if (ch == '*') {
                    ch = (char) br.read();
                    i++;
                    if (ch == '/') {
                        printTuple(Category.DELIMITERS, "多行注释", rowNum, colNum);
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
            printTuple(Category.DELIMITERS, "单行注释", rowNum, colNum);
            br.readLine();
            ch = (char) br.read();
            rowNum++;
            colNum = 1;
        }
    }

    /**
     * 处理大于
     */
    private void handleGreater() throws IOException {
        ch = (char) br.read();

        switch (ch) {
            case '=':
            case '>':
                String s = ">" + ch;
                printTuple(Category.OPERATOR, s, rowNum, colNum);
                ch = (char) br.read();
                colNum += 2;
                break;
            default:
                printTuple(Category.OPERATOR, ">", rowNum, colNum);
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
        ch = (char) br.read();

        switch (ch) {
            case '=':
            case '<':
                String s = "<" + ch;
                printTuple(Category.OPERATOR, s, rowNum, colNum);
                ch = (char) br.read();
                colNum += 2;
                break;
            default:
                printTuple(Category.OPERATOR, "<", rowNum, colNum);
                colNum++;
                break;
        }
    }
}
