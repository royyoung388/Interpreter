package parser;

public class Production {
    private String production, left, right;
    private String[] rights;

    public Production(String production) {
        this.production = production;
        String[] list = production.split(" -> ");
        left = list[0];
        right = list[1];
        rights = right.split(" ");
    }

    public String getLeft() {
        return left;
    }

    public String getRight() {
        return right;
    }

    public String[] getRights() {
        return rights;
    }

    public String getProduction() {
        return production;
    }
}
