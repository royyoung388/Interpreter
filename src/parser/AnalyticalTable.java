package parser;

import lexer.Lexer;
import lexer.Token;

import java.io.IOException;
import java.util.*;

/**
 * 预测分析表
 * 执行整个预测过程
 */
public class AnalyticalTable {

    private Map<Production, Set<String>> select;
    //预测分析表
    private Map<String, Map<String, Production>> predictTable = new HashMap<>();
    //分析栈
    private Stack<String> analyze;
    //输入串
    private List<Token> tokens;

    public AnalyticalTable(String start) {
        this("production.txt", start);
    }

    public AnalyticalTable(String filePath, String start) {
        analyze = new Stack<>();
        analyze.push(start);


        initTable(filePath, start);
    }

    //创建预测分析表
    private void initTable(String filePath, String start) {
        AnalyseProduction analyseProduction = new AnalyseProduction(filePath, start);

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

        while (pre != null || !analyze.isEmpty()) {
            System.out.println(analyze);
            String ana = analyze.pop();

            if (ana.equals(pre)) {
                System.out.println(pre + " 匹配");
                index++;
                pre = index >= tokens.size() ? null : tokens.get(index).getCategory();
                continue;
            }

            Production production = predictTable.get(ana).get(pre);

            if (production == null) {
                //todo 启动应急恢复
                System.out.println("应急恢复");
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
    }

    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
//        ArrayList<Token> tokens = lexer.parse("test.c");
        ArrayList<Token> tokens = lexer.parse("testAnalyse.c");

//        AnalyticalTable at = new AnalyticalTable("tesstProduction.txt", "E");
        AnalyticalTable at = new AnalyticalTable("program");
        at.analyze(tokens);
    }
}
