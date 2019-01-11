package semantic;

import lexer.Category;
import lexer.Token;
import parser.TreeNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Stack;

/**
 * 用来处理表达式的类
 */
public class Expression {
    //栈外优先级
    private static HashMap<String, Integer> icp = new HashMap<String, Integer>() {{
        put("#", 0);
        put("!", 16);
        put("(", 14);
        put("*", 12);
        put("/", 12);
        put("%", 12);
        put("+", 10);
        put("-", 10);
        put(">=", 8);
        put(">", 8);
        put("<", 8);
        put("<=", 8);
        put("==", 6);
        put("!=", 6);
        put("&&", 4);
        put("||", 2);
        put(")", 1);
    }};
    //栈内优先级
    private static HashMap<String, Integer> isp = new HashMap<String, Integer>() {{
        put("#", 0);
        put("!", 17);
        put("(", 15);
        put("*", 13);
        put("/", 13);
        put("%", 13);
        put("+", 11);
        put("-", 11);
        put(">=", 9);
        put(">", 9);
        put("<", 9);
        put("<=", 9);
        put("==", 7);
        put("!=", 7);
        put("&&", 5);
        put("||", 3);
        put(")", 14);
    }};

    /**
     * 将树解析，转换为一个token列表
     *
     * @return
     */
    public static List<Token> getTokens(TreeNode curr) {
        List<Token> tokens = new ArrayList<>();

        if (curr.token != null) {
            tokens.add(curr.token);
//            System.out.println(curr.token.getSymbol());
//            System.out.println(curr.token.getCategory());
            return tokens;
        }

        if (curr.cate.equals("$")) {
            return tokens;
        }

        for (int i = 0; i < curr.next.length; i++) {
            tokens.addAll(getTokens(curr.next[i]));
        }

        return tokens;
    }

    //中缀变后缀
    public static List<Token> infix2Postfix(List<Token> tokens) {
        List<Token> result = new ArrayList<>(tokens.size());
        Stack<Token> stack = new Stack<>();
        stack.push(new Token("END", "#", 0, 0));

        //中缀转后缀
        for (int i = 0; i <= tokens.size(); i++) {
            if (i == tokens.size()) {
                while (stack.size() > 1) {
                    result.add(stack.pop());
                }
                break;
            }

            switch (tokens.get(i).getCategory()) {
                case Category.ID:
                case Category.INT:
                case Category.REAL:
                case Category.BOOL:
                    result.add(tokens.get(i));
                    break;
                case Category.BIN_AR_OP_1:
                case Category.BIN_AR_OP_2:
                case Category.RELATION_OP:
                case Category.LOGIC_OP:
                case Category.UN_LOG_OP:
                case "(":
                    while (icp.get(tokens.get(i).getSymbol()) <= isp.get(stack.peek().getSymbol())) {
                        result.add(stack.pop());
                    }
                    stack.push(tokens.get(i));
                    break;
                case ")":
                    while (!stack.peek().getCategory().equals("(")) {
                        result.add(stack.pop());
                    }
                    stack.pop();
                    break;
            }
        }

//        for (Token token : result) {
//            System.out.print(token.getSymbol());
//        }

        return result;
    }

    public static double calc(List<Token> tokens, SymbolTable table) {
        Stack<Double> stack = new Stack<>();
        for (Token token : tokens) {
            switch (token.getCategory()) {
                case Category.INT:
                case Category.REAL:
                    stack.push(Double.parseDouble(token.getSymbol()));
                    break;
                case Category.BOOL:
                    stack.push(token.getSymbol().equals("true") ? 1.0 : 0.0);
                    break;
                case Category.ID:
                    stack.push(table.query(token.getSymbol()).getValue());
                    break;
                case Category.UN_LOG_OP:
                    stack.push(stack.pop() > 0 ? 0.0 : 1.0);
                    break;
                default:
                    double b = stack.pop();
                    double a = stack.pop();
                    switch (token.getSymbol()) {
                        case "+":
                            stack.push(a + b);
                            break;
                        case "-":
                            stack.push(a - b);
                            break;
                        case "*":
                            stack.push(a * b);
                            break;
                        case "/":
                            stack.push(a / b);
                            break;
                        case "%":
                            stack.push(a % b);
                            break;
                        case ">":
                            stack.push(a > b ? 1.0 : 0.0);
                            break;
                        case ">=":
                            stack.push(a >= b ? 1.0 : 0.0);
                            break;
                        case "<":
                            stack.push(a < b ? 1.0 : 0.0);
                            break;
                        case "<=":
                            stack.push(a <= b ? 1.0 : 0.0);
                            break;
                        case "==":
                            stack.push(a == b ? 1.0 : 0.0);
                            break;
                        case "!=":
                            stack.push(a != b ? 1.0 : 0.0);
                            break;
                        case "&&":
                            stack.push(a > 0 && b > 0 ? 1.0 : 0.0);
                            break;
                        case "||":
                            stack.push(a > 0 || b > 0 ? 1.0 : 0.0);
                            break;
                    }
                    break;
            }
        }
        return stack.pop();
    }
}
