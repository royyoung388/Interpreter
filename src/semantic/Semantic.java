package semantic;

import lexer.Category;
import lexer.Token;
import parser.TreeNode;

import java.util.ArrayList;
import java.util.Stack;

/**
 * 语义分析
 * 进行类型判定
 */
public class Semantic {
    //语法树头结点和当前结点
    private TreeNode curr;
    //符号表栈
    private SymbolTable symbolTable = new SymbolTable();
    //报错信息
    private ArrayList<String> error = new ArrayList<>();

    public Semantic(TreeNode header) {
        //初始为第一个语法结点，即文法开始符号结点
        this.curr = header;
    }

    /**
     * 开始解析
     */
    public void semantic() {
        curr = curr.next[0];
        //当分析到了头结点，则停止
        while (curr.pre != null) {
            switch (curr.cate) {
                case "read()":
                    break;
                case "write()":
                    break;
                case "declare_stm":
                    declare(curr);
                    break;
                case "assign_stm":
                    assign(curr);
                    break;
                case "while_stm":
                    //while_stm -> while ( logic_exp ) { statements }
                    logicExpression(curr.next[2], curr.next[1].token.getRow(), curr.next[1].token.getColumn());
                    curr = curr.next[5];
                    break;
                case "do_stm":
                    //do_stm -> do { statements } while ( logic_exp ) ;
                    logicExpression(curr.next[6], curr.next[5].token.getRow(), curr.next[5].token.getColumn());
                    curr = curr.next[2];
                    break;
                case "for_stm":
                    //for_stm -> for ( declare_stm ; logic_exp ; assign_stm ) { statements }
                    declare(curr.next[2]);
                    logicExpression(curr.next[4], curr.next[3].token.getRow(), curr.next[3].token.getColumn());
                    curr = curr.next[9];
                    break;
                case "if_stm":
                    //if_stm -> if ( logic_exp ) { statements }
                    logicExpression(curr.next[2], curr.next[1].token.getRow(),  curr.next[1].token.getColumn());
                    curr = curr.next[5];
                    break;
                case "switch_stm":
                    break;
            }

            if (curr.next != null && curr.next.length > 0)
                curr = curr.next[0];
            else
                curr = curr.findNext();
        }

        for (String s : error) {
            System.out.println(s);
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
    private void declare(TreeNode curr) {
        //declare_stm -> TYPE declare_ass
        String cate = curr.next[0].token.getCategory();
        String type = curr.next[0].token.getSymbol();
        Stack<TreeNode> stack = new Stack<>();

        curr = curr.next[1];
        stack.push(curr);

        while (!stack.isEmpty()) {
            TreeNode top = stack.pop();

            if (!top.cate.equals("declare_ass")) {
                continue;
            }

            Token id = top.next[0].token;
            if (symbolTable.query(id.getSymbol()) != null) {
                error.add(String.format("%d,%d: 重复定义的标识符", id.getRow(), id.getColumn()));
            } else {
                //declare_ass -> ID declare_val declare_item
                symbolTable.insert(new Symbol(type, id.getSymbol()));
            }

            //declare_val -> $
            top = top.next[1];
            if (top.next[0].token.getCategory().equals("$")) {
                top.findRight();
                break;
            }

            if (top.next[0].token.getSymbol().equals("[")) {
                //declare_val -> [ INT ] ASS_OP declare_arr
                symbolTable.query(id.getSymbol()).setLength(Integer.parseInt(top.next[1].token.getSymbol()));

                TreeNode temp = top.next[4];
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
                        if (!value.getCategory().equals(cate)) {
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
                String expType = mathExpression(top.next[1]);

                if (!Utils.typeConvert(type, expType)) {
                    error.add(String.format("%d,%d: 类型不一致", top.next[0].token.getRow(), top.next[0].token.getColumn()));
                } else {
                    symbolTable.query(id.getSymbol()).setValue("0");
                }
            }

            //curr -> declare_val
            //declare_ass -> ID declare_val declare_item
            top = top.findRight();

            if (!top.next[0].cate.equals("$")) {
                stack.push(top.next[1]);
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
    private String mathExpression(TreeNode curr) {
        String type = "";
        Stack<TreeNode> stack = new Stack<>();
        stack.push(curr);
        while (!stack.isEmpty()) {
            TreeNode top = stack.pop();

            switch (top.cate) {
                case "value":
                    String topType = top.next[0].token.getCategory();
                    type = Utils.calcType(type, topType);
                    if (type.isEmpty()) {
                        error.add(String.format("%d,%d: 类型不一致", top.next[0].token.getRow(), top.next[0].token.getColumn()));
                    }
                    break;
                case "ID":
                    Token id = top.token;
                    Symbol symbol = symbolTable.query(top.token.getSymbol());
                    if (symbol == null) {
                        error.add(String.format("%d,%d: 没有声明的标识符", top.token.getRow(), top.token.getColumn()));
                    } else {
                        type = Utils.calcType(type, symbol.getType());
                        if (type.isEmpty()) {
                            error.add(String.format("%d,%d: 类型不一致", id.getRow(), id.getColumn()));
                        }
                    }
                    break;
                default:
                    if (top.next != null && !top.next[0].cate.equals("$")) {
                        for (int i = top.next.length - 1; i >= 0; i--) {
                            stack.push(top.next[i]);
                        }
                    }
                    break;
            }
        }
        return type;
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
        if (curr.next[0].cate.equals("SELF_OP"))
            return;

        Symbol symbol = symbolTable.query(curr.next[0].token.getSymbol());
        if (symbol.getType() == null) {
            error.add(String.format("%d,%d: 没有声明的标识符", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
            return;
        }

        if (curr.next[1].cate.equals("assign_item")) {
            if (curr.next[1].next[0].token.getCategory().equals("ASS_OP")) {
                String expType = mathExpression(curr.next[1]);
                if (Utils.calcType(symbol.getType(), expType).isEmpty()) {
                    error.add(String.format("%d,%d: 类型不一致", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
                }
            } else if (curr.next[1].next[0].token.getCategory().equals("[")) {
                int index = Integer.parseInt(curr.next[1].next[1].token.getSymbol());
                if (index >= symbol.getLength()) {
                    error.add(String.format("%d,%d: 数组越界", curr.next[0].token.getRow(), curr.next[0].token.getColumn()));
                }
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
    private void logicExpression(TreeNode curr, int row, int column) {
        String type = "";
        Stack<TreeNode> stack = new Stack<>();
        stack.push(curr);
        while (!stack.isEmpty()) {
            TreeNode top = stack.pop();

            switch (top.cate) {
                case "arithmetic_exp":
                    String expType = mathExpression(top);
                    if (expType.isEmpty() || !expType.equals(Category.INT)) {
                        error.add(String.format("%d,%d: 类型不一致", row, column));
                    }
                    break;
                default:
                    if (top.next != null && !top.next[0].cate.equals("$")) {
                        for (int i = top.next.length - 1; i >= 0; i--) {
                            stack.push(top.next[i]);
                        }
                    }
                    break;
            }
        }
    }
}
