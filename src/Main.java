import lexer.Lexer;
import lexer.Token;
import parser.AnalyseTable;
import semantic.Semantic;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
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
