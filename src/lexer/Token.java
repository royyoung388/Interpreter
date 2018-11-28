package lexer;


public class Token {
    private String category;
    private String symbol;
    private int row, column;

    public Token(Lexer.Category category, String symbol, int row, int column) {
        this(category.getValue(), symbol, row, column);
    }


    public Token(String category, String symbol, int row, int column) {
        this.category = category;
        this.symbol = symbol;
        this.row = row;
        this.column = column;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategory(Lexer.Category category) {
        this.category = category.getValue();
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public void print() {
        System.out.printf("%d,%d: category: %s , symbol: %s\n", row, column, category, symbol);
    }

    public void testPrint() {
        System.out.printf("%d,%d: category: %s , symbol: %s\n", row, column, category, symbol);
    }
}
