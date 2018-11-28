package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class AnalyseProduction {

    //文法开始符号
    private String start;

    private List<Production> productions = new ArrayList<>();
    private Set<String> nonTerminal = new HashSet<>(), terminal = new HashSet<>(), nullNonter = new HashSet<>();
    private Map<String, Set<String>> first = new HashMap<>(), follow = new HashMap<>();
    private Map<Production, Set<String>> select = new HashMap<>();

    public AnalyseProduction(String start) {
        this("production.txt", start);
    }

    public AnalyseProduction(String filePath, String start) {
        this.start = start;
        readProduction(filePath);
        calcTerminal();
        calcNullNonter();
        calcFirst();
        calcFollow();
        calcSelect();
    }

    private void readProduction(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath));) {
            String line = br.readLine();

            while (line != null) {
                if (line.length() == 0) {
                    line = br.readLine();
                    continue;
                }

                productions.add(new Production(line));

                //计算终结符号
                nonTerminal.add(line.split(" -> ")[0]);

                line = br.readLine();
            }

            System.out.println("nonTerminal = " + nonTerminal);
            System.out.println();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("read production error = " + e);
            System.out.println();
        }
    }

    /**
     * 计算非终结符号
     */
    private void calcTerminal() {
        for (Production production : productions) {
            for (String r : production.getRights()) {
                if (!nonTerminal.contains(r))
                    terminal.add(r);
            }
        }

        System.out.println("terminal = " + terminal);
        System.out.println();
    }

    /**
     * 计算能推出空串的非终结符
     */
    private void calcNullNonter() {
        while (true) {
            boolean flag = true;

            for (Production production : productions) {
                String left = production.getLeft(), right = production.getRight();
                String[] rights = production.getRights();

                if (nullNonter.contains(left))
                    continue;

                if (right.equals("$")) {
                    nullNonter.add(left);
                    flag = false;
                }

                for (int i = 0; i < rights.length; i++) {
                    //含有非终结符，该条产生式一定不能产生空串
                    if (terminal.contains(rights[i])) {
                        break;
                    }

                    //该非终结符不产生空串
                    if (!nullNonter.contains(rights[i])) {
                        break;
                    }

                    //最后一个非终结符也能产生空串
                    if (i == rights.length - 1) {
                        nullNonter.add(left);
                        flag = false;
                    }
                }
            }

            if (flag)
                break;
        }

        //如果非终结符不在以上两个集合中，则为空

        System.out.println("nullNonter = " + nullNonter);
        System.out.println();
    }


    /**
     * 计算first集
     */
    private void calcFirst() {
        //终结符号first集为自己
        for (String t : terminal) {
            first.put(t, new HashSet<String>() {{
                add(t);
            }});
        }

        //把所有非终结符作为key加入
        for (String n : nonTerminal) {
            first.put(n, new HashSet<>());
        }

        //对于能产生空串的非终结符
        for (String n : nullNonter) {
            first.get(n).add("$");
        }

        //根据产生式计算first集
        while (true) {
            //当first集合不再变化，则退出循环
            boolean flag = true;

            for (Production production : productions) {
                String left = production.getLeft(), right = production.getRight();
                String[] rights = production.getRights();

                Set<String> leftFirst = first.get(left);

                //以终结符打头并且该产生式未被添加
                if (terminal.contains(rights[0]) && !leftFirst.contains(rights[0])) {
                    leftFirst.add(rights[0]);
                    flag = false;
                }

                //非终结符打头，遍历右部
                for (int i = 0; i < rights.length; i++) {
                    //不能产生空串的非终结符或者终结符
                    if (!nullNonter.contains(rights[i])) {
                        //未被添加
                        if (!leftFirst.containsAll(first.get(rights[i]))) {
                            leftFirst.addAll(first.get(rights[i]));
                            flag = false;
                        }
                        break;
                    } else {
                        //能产生空串的非终结符号

                        //去除非终结符的first中的空串
                        Set<String> temp = new HashSet<>(first.get(rights[i]));
                        temp.remove("$");

                        //未被添加
                        if (!leftFirst.containsAll(temp)) {
                            leftFirst.addAll(temp);
                            flag = false;
                        }

                        //最后一个非终结符也存在空串，并且未被添加，加入空串
                        if (i == rights.length - 1 && !leftFirst.contains("$")) {
                            leftFirst.add("$");
                            flag = false;
                        }
                    }
                }
            }

            if (flag)
                break;
        }

        //加入所有产生式右部
        for (Production production : productions) {
            if (!first.containsKey(production.getRight()))
                first.put(production.getRight(), new HashSet<>());
        }

        //计算所有产生式右部的first集
        for (Production production : productions) {
            String right = production.getRight();
            String[] rights = production.getRights();

            Set<String> rightFirst = first.get(right);

            //第一个符号不能产生空串
            if (!nullNonter.contains(rights[0]))
                rightFirst.addAll(first.get(rights[0]));
            else {
                //第一个符号能产生空串
                for (int i = 0; i < rights.length; i++) {
                    //去除非终结符的first中的空串
                    Set<String> temp = new HashSet<String>(first.get(rights[i]));
                    temp.remove("$");

                    //能产生空串的非终结符
                    if (nullNonter.contains(rights[i])) {
                        rightFirst.addAll(temp);

                        if (i == rights.length - 1)
                            rightFirst.add("$");
                    } else {
                        //不能产生空串的非终结符或者终结符
                        rightFirst.addAll(temp);
                        break;
                    }
                }
            }
        }

        System.out.println("first = " + first);
        System.out.println();
    }

    /**
     * 计算follow集
     */
    private void calcFollow() {
        //将所有非终结符号加入follow集
        for (String n : nonTerminal) {
            follow.put(n, new HashSet<>());
        }

        //将结束符号加入开始符号之后
        follow.get(start).add("#");

        while (true) {
            boolean flag = true;

            for (Production production : productions) {
                String left = production.getLeft();
                String[] rights = production.getRights();

                Set<String> leftFollow = follow.get(left);

                for (int i = 0; i < rights.length; i++) {
                    Set<String> iRightFollow = follow.get(rights[i]);

                    if (terminal.contains(rights[i]))
                        continue;

                    //当前是最后一个符号
                    if (i == rights.length - 1 && !iRightFollow.containsAll(leftFollow)) {
                        iRightFollow.addAll(leftFollow);
                    }

                    for (int j = i + 1; j < rights.length; j++) {
                        //去掉空串
                        Set<String> temp = first.get(rights[j]);
                        temp.remove("$");

                        if (!iRightFollow.containsAll(temp)) {
                            iRightFollow.addAll(temp);
                            flag = false;
                        }

                        //如果不能产生空串
                        if (!nullNonter.contains(rights[j])) {
                            break;
                        } else if (j == rights.length - 1 && !iRightFollow.containsAll(leftFollow)) {
                            //最后一个符号也能产生空串
                            iRightFollow.addAll(leftFollow);
                            flag = false;
                        }
                    }
                }
            }

            if (flag)
                break;
        }

        System.out.println("follow = " + follow);
        System.out.println();
    }

    /**
     * 计算select集
     */
    private void calcSelect() {
        for (Production production : productions) {
            //有空串
            if (first.get(production.getRight()).contains("$")) {
                Set<String> result = new HashSet<>(first.get(production.getRight()));
                result.remove("$");
                result.addAll(follow.get(production.getLeft()));
                select.put(production, result);
            } else {
                //没有空串
                select.put(production, first.get(production.getRights()[0]));
            }
        }

        System.out.println("select:");

        for (Map.Entry<Production, Set<String>> entry : select.entrySet()) {
            System.out.print(entry.getKey().getProduction() + " = ");
            System.out.println(entry.getValue());
        }
        System.out.println();
    }

    /**
     * 判断是否为LL1文法
     */
    private void testLL1() {
        Map<String, List<Production>> map = new HashMap<>();

        //先将非终结符加入
        for (String n : nonTerminal) {
            map.put(n, new ArrayList<>());
        }

        //加入产生式
        for (Production production : productions) {
            map.get(production.getLeft()).add(production);
        }

        //对每一个非终结符，计算其select的交集
        for (String n : nonTerminal) {
            List<Production> list = map.get(n);
            int size = list.size();

            //两两之间求交集
            for (int i = 0; i < size - 1; i++) {
                for (int j = i + 1; j < size; j++) {
                    Set<String> result = select.get(list.get(i));
                    result.retainAll(select.get(list.get(j)));

                    if (result.size() > 0) {
                        System.out.println(list.get(i).getProduction() + " /\\ " + list.get(j).getProduction() + " = " + result);
                    }
                }
            }
        }
    }

    public Map<Production, Set<String>> getSelect() {
        return select;
    }

    public Set<String> getNonTerminal() {
        return nonTerminal;
    }

    public Set<String> getTerminal() {
        return terminal;
    }

    public static void main(String[] args) {
//        AnalyseProduction analyseProduction = new AnalyseProduction("testProduction.txt", "S");
        AnalyseProduction analyseProduction = new AnalyseProduction("program");
        analyseProduction.testLL1();
    }
}
