package semantic;

import lexer.Category;

public class Utils {
    public static String calcType(String type1, String type2) {
        switch (type2) {
            case "int":
                type2 = Category.INT;
                break;
            case "float":
            case "double":
                type2 = Category.REAL;
        }
        if (type1.isEmpty() || type2.isEmpty()) {
            return type1.isEmpty() ? type2 : type1;
        }
        if ((type1.equals(Category.REAL) || type1.equals(Category.INT)) != (type2.equals(Category.REAL) || type2.equals(Category.INT))) {
            return "";
        }
        if (type1.equals(Category.REAL) || type2.equals(Category.REAL)) {
            return Category.REAL;
        }
        return Category.INT;
    }

    public static boolean typeConvert(String dis, String src) {
        switch (dis) {
            case "int":
                dis = Category.INT;
                break;
            case "float":
            case "double":
                dis = Category.REAL;
        }
        if (dis.equals(Category.REAL) && (src.equals(Category.INT) || src.equals(Category.REAL)))
            return true;
        if (dis.equals(src))
            return true;
        return false;
    }

    public static void main(String[] args) {
        System.out.print(Utils.calcType("", "INT"));
    }
}
