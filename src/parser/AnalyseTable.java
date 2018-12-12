package parser;

import lexer.Lexer;
import lexer.Token;

import java.io.IOException;
import java.util.*;

/**
 * 预测分析表
 * 执行整个预测过程
 */
public class AnalyseTable {

    private Map<Production, Set<String>> select;
    private AnalyseProduction analyseProduction;
    //语法树头结点,和当前结点
    private TreeNode header, curr;
    //预测分析表
    private Map<String, Map<String, Production>> predictTable = new HashMap<>();
    //分析栈
    private Stack<String> analyze;
    //当前语法没有错误
    private boolean correct = true;

    public AnalyseTable(String start) {
        this("production.txt", start);
    }

    public AnalyseTable(String filePath, String start) {
        analyze = new Stack<>();
        analyze.push(start);
        //语法树根节点
        header = new TreeNode(null, "");
        header.next = new TreeNode[]{new TreeNode(header, start)};
        curr = header.next[0];

        initTable(filePath, start);
    }

    //创建预测分析表
    private void initTable(String filePath, String start) {
        analyseProduction = new AnalyseProduction(filePath, start);

        //创建非终结符表头
        for (String non : analyseProduction.getNonTerminal()) {
            predictTable.put(non, new HashMap<>());
        }

        Map<Production, Set<String>> select = analyseProduction.getSelect();

        //填充表
        for (Map.Entry<Production, Set<String>> entry : select.entrySet()) {
            for (String ter : entry.getValue()) {
                predictTable.get(entry.getKey().getLeft()).put(ter, entry.getKey());
            }
        }
    }

    /**
     * 开始分析
     *
     * @param tokens
     */
    public void analyze(List<Token> tokens) {
        int index = 0;
        String pre = tokens.get(index).getCategory();
        ArrayList<String> errors = new ArrayList<>();

        while (!analyze.isEmpty()) {
            System.out.println(pre + " " + analyze);
            String top = analyze.pop();

            if (top.equals(pre)) {
                System.out.printf("%s %s 匹配\n", pre, tokens.get(index).getSymbol());
                //语法树叶子结点
                curr.next = new TreeNode[]{new TreeNode(curr, tokens.get(index))};
                curr = curr.pre.findNext(curr);

                index++;
                pre = index >= tokens.size() ? "#" : tokens.get(index).getCategory();
                continue;
            }

            //如果栈顶是非终结符，那么直接报错
            if (analyseProduction.getTerminal().contains(top)) {
                Token token = tokens.get(index);
                String error = String.format("语法错误(%d,%d): 此处应该是 %s ， 但实际是 %s", token.getRow(), token.getColumn(), top, pre);
                errors.add(error);
                System.out.println(error);
                correct = false;

                //尝试跳过这个错误，分析后面的语法
                continue;
            }

            Production production = predictTable.get(top).get(pre);

            //应急恢复，栈顶是非终结符号，开始尝试恢复
            if (production == null) {
                //先报错
                Token token = tokens.get(index);
                String error = String.format("语法错误(%d,%d): 不符合语法规则", token.getRow(), token.getColumn());
                errors.add(error);
                System.out.println(error);
                correct = false;

                while (!analyze.isEmpty()) {
                    if (pre.equals("#")) {
                        System.out.println("应急恢复结束");
                        return;
                    }

                    System.out.println("跳过符号 ： " + pre);
                    index++;
                    pre = index >= tokens.size() ? "#" : tokens.get(index).getCategory();

                    //是当前非终结符的follow集合，则非终结符出栈
                    if (analyseProduction.getFollow().get(top).contains(pre)) {
                        System.out.println("应急恢复：非终结符出栈 " + top);
                        break;
                    }

                    //是当前非终结符的first集合，则非终结符恢复分析
                    if (analyseProduction.getFirst().get(top).contains(pre)) {
                        System.out.println("应急恢复：非终结符恢复分析");
                        analyze.push(top);
                        break;
                    }
                }
                continue;
            } else {
                System.out.println(production.getProduction());
            }

            //将产生式右部反序入到分析栈和语法树中
            String[] rights = production.getRights();
            curr.next = new TreeNode[rights.length];

            //空产生式不入栈
            if (rights[0].equals("$")) {
                curr.next = new TreeNode[]{new TreeNode(curr, "$")};
                curr = curr.pre.findNext(curr);
            } else {
                for (int i = rights.length - 1; i >= 0; i--) {
                    analyze.push(rights[i]);
                    //语法树
                    curr.next[i] = new TreeNode(curr, rights[i]);
                }
                //移动当前语法树结点
                curr = curr.next[0];
            }
        }

        //集中报错
        if (errors.size() > 0) {
            System.out.println("\n发现以下错误：\n");
            correct = false;

            for (String error : errors) {
                System.out.println(error);
            }
            return;
        }

        System.out.println("接受");
    }

    private void printSyntaxTree() {
        Queue<TreeNode> queue = new LinkedList<>();
        queue.offer(header);

        while (!queue.isEmpty()) {
            TreeNode temp = queue.peek();

            for (int i = 0; i < temp.next.length; i++) {
                if (temp.next[i].cate != null) {
                    System.out.print(temp.next[i].cate + "  ");

                    if (!temp.next[i].cate.equals("$")) {
                        queue.offer(temp.next[i]);
                    }

                } else {
                    System.out.print(temp.next[i].token.getCategory() + "  ");
                }
            }
            System.out.println();

            queue.poll();
        }
    }

    //返回语法树根节点
    public TreeNode getHeader() {
        return header;
    }

    //当前语法分析是否正确
    public boolean isCorrect() {
        return correct;
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
//        ArrayList<Token> tokens = lexer.parse("test.c");
        ArrayList<Token> tokens = lexer.parse("testAnalyse.c");

//        AnalyseTable at = new AnalyseTable("testProduction.txt", "E");
        AnalyseTable at = new AnalyseTable("program");
        at.analyze(tokens);

        if (at.isCorrect()) {
            //at.printSyntaxTree();
        }
    }
}
