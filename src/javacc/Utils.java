package javacc;

import lexer.Category;
import semantic.Type;

public class Utils {
    public static String calcCate(String cate1, String cate2) {
        if (cate1.isEmpty() || cate2.isEmpty()) {
            return cate1.isEmpty() ? cate2 : cate1;
        }
        if ((cate1.equals(Category.REAL) || cate1.equals(Category.INT)) != (cate2.equals(Category.REAL) || cate2.equals(Category.INT))) {
            return "";
        }
        if (cate1.equals(Category.REAL) || cate2.equals(Category.REAL)) {
            return Category.REAL;
        }
        return Category.INT;
    }

    public static boolean cateConvert(String dis, String src) {
        switch (dis) {
            case Type.INT:
                dis = Category.INT;
                break;
            case Type.FLOAT:
            case Type.DOUBLE:
                dis = Category.REAL;
        }
        if (dis.equals(Category.REAL) && (src.equals(Category.INT) || src.equals(Category.REAL)))
            return true;
        if (dis.equals(src))
            return true;
        return false;
    }

    public static String type2Cate(String type) {
        switch (type) {
            case Type.INT:
                return Category.INT;
            case Type.FLOAT:
            case Type.DOUBLE:
                return Category.REAL;
            case Type.CHAR:
                return Category.CHAR;
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.print(Utils.calcCate("", "int"));
    }
}
