package semantic;

import lexer.Category;
import lexer.Lexer;
import lexer.Token;
import parser.AnalyseTable;
import parser.TreeNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * 语义分析
 * 进行类型判定
 */
public class Semantic {
    //符号表栈
    private SymbolTable symbolTable = new SymbolTable();
    //报错信息
    private ArrayList<String> error = new ArrayList<>();

    public Semantic() {
    }

    public void run(TreeNode curr) {
        semantic(curr);

        for (String s : error) {
            System.out.println(s);
        }
    }

    /**
     * 开始解析
     */
    private void semantic(TreeNode curr) {
        double value;

        switch (curr.cate) {
            case "read_stm":
                Scanner in = new Scanner(System.in);
                String line = in.nextLine();
                return;
            case "write_stm":
                if (curr.next[2].next.length > 1) {
                    Symbol symbol = symbolTable.query(curr.next[2].next[0].token.getSymbol());
                    Token id = curr.next[2].next[0].token;
                    if (symbol == null) {
                        error.add(String.format("%d,%d: 没有声明的标识符", id.getRow(), id.getColumn()));
                    } else {
                        // 输出变量
                        if (curr.next[2].next[1].next[0].cate.equals("$")) {
                            System.out.println(symbol.getLength() < 0 ? symbol.getValueString() : symbol.getArray());
                        } else {
                            // 输出数组元素
                            if (symbol.getLength() < 0) {
                                error.add(String.format("%d,%d: 标识符不是数组类型", id.getRow(), id.getColumn()));
                            } else {
                                System.out.println(symbol.getArray(Integer.parseInt(curr.next[2].next[2].token.getSymbol())));
                            }
                        }
                    }
                } else {
                    System.out.println(curr.next[2].next[0].next[0].token.getSymbol());
                }
                return;
            case "declare_stm":
                declare(curr, null);
                return;
            case "assign_stm":
                assign(curr);
                return;
            case "while_stm":
                //while_stm -> while ( logic_exp ) { statements }
                value = logicExpression(curr.next[2], curr.next[1].token.getRow(), curr.next[1].token.getColumn());
                if (Double.isNaN(value))
                    break;
                while (value > 0) {
                    semantic(curr.next[5]);
                    value = logicExpression(curr.next[2], curr.next[1].token.getRow(), curr.next[1].token.getColumn());
                }
                return;
            case "do_stm":
                //do_stm -> do { statements } while ( logic_exp ) ;
                value = logicExpression(curr.next[6], curr.next[5].token.getRow(), curr.next[5].token.getColumn());
                if (Double.isNaN(value))
                    break;

                do {
                    semantic(curr.next[2]);
                    value = logicExpression(curr.next[6], curr.next[5].token.getRow(), curr.next[5].token.getColumn());
                } while (value > 0);
                return;
            case "for_stm":
                //for_stm -> for ( declare_stm ; logic_exp ; assign_stm ) { statements }
                value = logicExpression(curr.next[4], curr.next[3].token.getRow(), curr.next[3].token.getColumn());
                if (Double.isNaN(value))
                    break;

                declare(curr.next[2], null);

                while (value > 0) {
                    semantic(curr.next[9]);
                    assign(curr.next[6]);
                    value = logicExpression(curr.next[4], curr.next[3].token.getRow(), curr.next[3].token.getColumn());
                }
                return;
            case "if_stm":
                //if_stm -> if ( logic_exp ) { statements }
                value = logicExpression(curr.next[2], curr.next[1].token.getRow(), curr.next[1].token.getColumn());
                if (Double.isNaN(value))
                    break;

                if (value > 0) {
                    semantic(curr.next[5]);
                }
                return;
            case "switch_stm":
                return;
        }

        if (curr.next != null && curr.next.length > 0)
            for (TreeNode node : curr.next) {
                semantic(node);
            }
    }

    /**
     * 分析声明语句
     * declare_stm -> TYPE declare_ass
     * declare_ass -> ID declare_val declare_item
     * declare_item -> $
     * declare_item -> , declare_val declare_item
     * declare_val -> $
     * declare_val -> ASS_OP arithmetic_exp
     * declare_val -> [ INT ] ASS_OP declare_arr
     * declare_arr -> { value arr_item }
     * arr_item -> $
     * arr_item -> , value arr_item
     */
    private void declare(TreeNode curr, String type) {
        if (curr.cate.equals("declare_stm")) {
            declare(curr.next[1], curr.next[0].token.getSymbol());
            declare(curr.next[3], curr.next[0].token.getSymbol());
            return;
        }

        if (curr.cate.equals("declare_item") && !curr.next[0].cate.equals("$")) {
            declare(curr.next[1], type);
            declare(curr.next[3], type);
            return;
        }

        if (!curr.cate.equals("ID")) {
            return;
        }

        Token id = curr.token;
        if (symbolTable.query(id.getSymbol()) != null) {
            error.add(String.format("%d,%d: 重复定义的标识符", id.getRow(), id.getColumn()));
        } else {
            // declare_ass -> ID declare_val declare_item
            symbolTable.insert(new Symbol(type, id.getSymbol()));
        }

        //declare_val -> $
        TreeNode val = curr.findRight();
        if (val.next[0].cate.equals("$")) {
            return;
        }

        if (val.next[0].token.getSymbol().equals("[")) {
            //declare_val -> [ INT ] ASS_OP declare_arr
            symbolTable.query(id.getSymbol()).setLength(Integer.parseInt(val.next[1].token.getSymbol()));

            TreeNode temp = val.next[4];
            //declare_arr -> { value arr_item }
            //arr_item -> $
            //arr_item -> , value arr_item
            int index = 0;
            while (temp != null) {
                /**
                 * value -> INT
                 * value -> REAL
                 * value -> CHAR
                 * value -> STRING
                 */
                if (temp.cate.equals("value")) {
                    //类型不对，报错
                    Token value = temp.next[0].token;
                    if (!value.getCategory().equals(Utils.type2Cate(type))) {
                        error.add(String.format("%d,%d: 类型不对", value.getRow(), value.getColumn()));
                    } else {
                        symbolTable.query(id.getSymbol()).setArrayValue(index, value.getSymbol());
                    }
                    index++;
                    temp = temp.findRight();
                }

                if (temp.next == null || temp.next[0].cate.equals("$")) {
                    temp = temp.findRight();
                } else {
                    temp = temp.next[0];
                }
            }
        } else {
            //declare_val -> ASS_OP arithmetic_exp
            String[] exp = mathExpression(val.next[1], val.next[0].token.getRow(), val.next[0].token.getColumn());

            if (exp == null)
                return;

            if (!Utils.cateConvert(type, exp[0])) {
                error.add(String.format("%d,%d: 类型不一致", val.next[0].token.getRow(), val.next[0].token.getColumn()));
            } else {
                symbolTable.query(id.getSymbol()).setValue(exp[1]);
            }
        }
    }

    /**
     * arithmetic_exp -> arithmetic_item item_op arithmetic_alt
     * arithmetic_alt -> $
     * arithmetic_alt -> BIN_AR_OP_2 arithmetic_item arithmetic_alt
     * item_op -> BIN_AR_OP_1 arithmetic_item
     * item_op -> $
     * arithmetic_item -> ( arithmetic_exp )
     * arithmetic_item -> ID
     * arithmetic_item -> value
     * <p>
     * 算术表达式
     *
     * @param curr
     * @return
     */
    private String[] mathExpression(TreeNode curr, int row, int column) {
        List<Token> tokens = Expression.getTokens(curr);
        boolean err = false;

        //先进行类型检查
        String cate = "";
        for (Token token : tokens) {
            switch (token.getCategory()) {
                case Category.INT:
                case Category.REAL:
                    cate = Utils.calcCate(cate, token.getCategory());
                    if (cate.isEmpty()) {
                        error.add(String.format("%d,%d: 表达式类型与变量类型不一致", token.getRow(), token.getColumn()));
                        err = true;
                    }
                    break;
                case Category.ID:
                    Symbol symbol = symbolTable.query(token.getSymbol());
                    if (symbol == null) {
                        error.add(String.format("%d,%d: 没有声明的标识符", token.getRow(), token.getColumn()));
                        err = true;
                    } else {
                        cate = Utils.calcCate(cate, Utils.type2Cate(symbol.getType()));
                        if (cate.isEmpty()) {
                            error.add(String.format("%d,%d: 表达式类型与变量类型不一致", token.getRow(), token.getColumn()));
                            err = true;
                        }
                    }
                    break;
            }
        }

        if (err) {
            return null;
        }

        //计算表达式
        //中缀转后缀
        List<Token> postfix = Expression.infix2Postfix(tokens);
        //计算后缀表达式
        double value = Expression.calc(postfix, symbolTable);
//        System.out.println(value);
        if (Double.isInfinite(value)) {
            error.add(String.format("%d,%d: 表达式中含有除数为0的项", row, column));
            return null;
        }

        return new String[]{cate, Double.toString(value)};
    }

    /**
     * 赋值语句
     * assign_stm -> SELF_OP ID
     * assign_stm -> ID assign_item
     * assign_item -> ASS_OP arithmetic_exp
     * assign_item -> SELF_OP
     * assign_item -> [ INT ] ASS_OP arithmetic_exp
     *
     * @param curr
     */
    private void assign(TreeNode curr) {
        if (curr.next[0].cate.equals("SELF_OP")) {
            if (curr.next[0].token.getSymbol().equals("++"))
                symbolTable.query(curr.next[1].token.getSymbol()).selfAdd();
            else
                symbolTable.query(curr.next[1].token.getSymbol()).selfMin();
            return;
        }

        Symbol symbol = symbolTable.query(curr.next[0].token.getSymbol());
        if (symbol == null) {
            error.add(String.format("%d,%d: 没有声明的标识符", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
            return;
        }

        if (curr.next[1].cate.equals("assign_item")) {
            if (curr.next[1].next[0].cate.equals("ASS_OP")) {
                TreeNode node = curr.next[1].next[1].next[0];
                if (node.cate.equals("arithmetic_exp")) {
                    String[] exp = mathExpression(curr.next[1], curr.next[1].next[0].token.getRow(), curr.next[1].next[0].token.getColumn());
                    if (exp == null) {
                        return;
                    }
                    if (Utils.calcCate(Utils.type2Cate(symbol.getType()), exp[0]).isEmpty()) {
                        error.add(String.format("%d,%d: 类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
                    } else {
                        symbolTable.query(curr.next[0].token.getSymbol()).setValue(exp[1]);
                    }
                } else {
                    Scanner in = new Scanner(System.in);
                    String line = in.nextLine();
                    try {
                        switch (symbol.getType()) {
                            case Type.INT:
                                symbol.setValue(Integer.parseInt(line) + "");
                                break;
                            case Type.DOUBLE:
                            case Type.FLOAT:
                                symbol.setValue(Double.parseDouble(line) + "");
                                break;
                            case Type.CHAR:
                                symbol.setValue(line.charAt(0) + "");
                                break;
                        }
                    } catch (Exception e) {
                        error.add(String.format("%d,%d: 输入类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
                    }
                }
            } else if (curr.next[1].next[0].token.getCategory().equals("[")) {
                int index = Integer.parseInt(curr.next[1].next[1].token.getSymbol());
                if (index >= symbol.getLength()) {
                    error.add(String.format("%d,%d: 数组越界", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
                } else {
                    String[] exp = mathExpression(curr.next[1].next[4], curr.next[1].next[3].token.getRow(), curr.next[1].next[3].token.getColumn());

                    if (exp == null) {
                        return;
                    }
                    if (Utils.calcCate(Utils.type2Cate(symbol.getType()), exp[0]).isEmpty()) {
                        error.add(String.format("%d,%d: 类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
                    } else {
                        symbolTable.query(curr.next[0].token.getSymbol()).setValue(exp[1]);
                    }
                    symbolTable.query(curr.next[0].token.getSymbol()).setArrayValue(index, exp[1]);
                }
            } else {
                if (curr.next[0].token.getSymbol().equals("++"))
                    symbolTable.query(curr.next[0].token.getSymbol()).selfAdd();
                else
                    symbolTable.query(curr.next[0].token.getSymbol()).selfMin();
            }
        }
    }

    /**
     * logic_exp -> arithmetic_exp logic_ari
     * logic_exp -> BOOL logic_bool
     * logic_ari -> $
     * logic_ari -> RELATION_OP arithmetic_exp
     * logic_bool -> $
     * logic_bool -> RELATION_OP logic_exp
     */
    private double logicExpression(TreeNode curr, int row, int column) {
        List<Token> tokens = Expression.getTokens(curr);
        boolean err = false;

        //先进行类型检查
        String type = "";
        for (Token token : tokens) {
            //变量只接受BOOL和INT
            switch (token.getCategory()) {
                case Category.ID:
                    Symbol symbol = symbolTable.query(token.getSymbol());
                    if (symbol == null) {
                        error.add(String.format("%d,%d: 没有声明的标识符", token.getRow(), token.getColumn()));
                        err = true;
                    } else {
                        if (!symbol.getType().equals(Type.INT)) {
                            error.add(String.format("%d,%d: 变量类型与表达式不一致", token.getRow(), token.getColumn()));
                            err = true;
                        }
                    }
                    break;
                case Category.CHAR:
                case Category.STRING:
                case Category.REAL:
                    error.add(String.format("%d,%d: 与表达式类型不一致", token.getRow(), token.getColumn()));
                    err = true;
                    break;
            }
        }

        if (err) {
            return Double.NaN;
        }

        //计算表达式
        //中缀转后缀
        List<Token> postfix = Expression.infix2Postfix(tokens);
        //计算后缀表达式
        double value = Expression.calc(postfix, symbolTable);
//        System.out.println(value);

        return value;
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
//        ArrayList<Token> tokens = lexer.parse("test.c");
        ArrayList<Token> tokens = lexer.parse("testAnalyse.c");

//        AnalyseTable at = new AnalyseTable("testProduction.txt", "program");
        AnalyseTable at = new AnalyseTable("Production.txt", "program");
        at.analyze(tokens);

        Semantic semantic = new Semantic();
        semantic.run(at.getHeader());
    }
}
