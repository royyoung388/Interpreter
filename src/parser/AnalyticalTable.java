package parser;

import lexer.Token;

import java.util.*;

/**
 * 预测分析表
 * 执行整个预测过程
 */
public class AnalyticalTable {

    private Map<Production, Set<String>> select;
    //分析栈
    private Stack<String> analyze;
    //输入串
    private List<Token> tokens;

    public AnalyticalTable(String start) {
        AnalyseProduction analyseProduction = new AnalyseProduction(start);
        select = analyseProduction.getSelect();
        analyze = new Stack<>();
        analyze.push(start);
    }

    public void analyze(List<Token> tokens) {
        int index = 0;

        while (index <= tokens.size() && !analyze.isEmpty()) {
            String ana = analyze.pop();
            Token pre = tokens.get(index);


        }
    }
}
