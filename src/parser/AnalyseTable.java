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

    //预测分析表
    private Map<String, Map<String, Production>> predictTable = new HashMap<>();
    //分析栈
    private Stack<String> analyze;
    //输入串
    private List<Token> tokens;

    public AnalyseTable(String start) {
        this("production.txt", start);
    }

    public AnalyseTable(String filePath, String start) {
        analyze = new Stack<>();
        analyze.push(start);


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
//
//            if (pre.equals("#")) {
//                System.out.println("语法错误");
//                break;
//            }

            if (top.equals(pre)) {
                System.out.printf("%s %s 匹配\n", pre, tokens.get(index).getSymbol());
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

            String[] rights = production.getRights();
            for (int i = rights.length - 1; i >= 0; i--) {
                if (!rights[i].equals("$"))
                    analyze.push(rights[i]);
            }
        }

        //集中报错
        if (errors.size() > 0) {
            System.out.println("\n发现以下错误：\n");

            for (String error : errors) {
                System.out.println(error);
            }
            return;
        }

        System.out.println("接受");
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
//        ArrayList<Token> tokens = lexer.parse("test.c");
        ArrayList<Token> tokens = lexer.parse("testAnalyse.c");

//        AnalyseTable at = new AnalyseTable("testProduction.txt", "E");
        AnalyseTable at = new AnalyseTable("program");
        at.analyze(tokens);
    }
}
