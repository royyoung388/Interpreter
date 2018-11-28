import lexer.Lexer;
import lexer.Token;

import java.io.IOException;
import java.util.ArrayList;

public class Main {
    public static void main(String[] args) throws IOException {
        Lexer lexer = new Lexer();
        ArrayList<Token> tokens = lexer.parse("test.c");
    }
}
