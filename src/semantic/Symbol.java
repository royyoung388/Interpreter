package semantic;

import lexer.Category;
import lexer.Token;

/**
 * 符号表的表项
 */
public class Symbol {
    private String symbol;
    private String type;
    //数组长度
    private int length;
    // 元素的整形数值
    private int intValue;
    // 元素的浮点型数值
    private float realValue;
    // 元素的字符串值
    private String stringValue;
    // 元素的字符值
    private char charvalue;
    // 整数数组
    private int[] intArray;
    // 小数数组
    private float[] realArray;
    // 字符串数组
    private char[] charArray;

    public Symbol(String type, String symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    public String getName() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(String value) throws NumberFormatException {
        switch (type) {
            case Category.INT:
                intValue = Integer.parseInt(value);
                break;
            case Category.REAL:
                realValue = Float.parseFloat(value);
                break;
            case Category.STRING:
                stringValue = value;
                break;
            case Category.CHAR:
                charvalue = value.charAt(0);
                break;
        }
    }

    public void setArrayValue(int index, String value) throws NumberFormatException {
        switch (type) {
            case Category.INT:
                intArray[index] = Integer.parseInt(value);
                break;
            case Category.REAL:
                realArray[index] = Float.parseFloat(value);
                break;
            case Category.CHAR:
                realArray[index] = value.charAt(0);
                break;
        }
    }

    public int getIntValue() {
        return intValue;
    }

    public float getRealValue() {
        return realValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public char getCharvalue() {
        return charvalue;
    }

    public int getLength() {
        return length;
    }

    public int[] getIntArray() {
        return intArray;
    }

    public void setIntArray(int[] intArray) {
        this.intArray = intArray;
    }

    public float[] getRealArray() {
        return realArray;
    }

    public void setRealArray(float[] realArray) {
        this.realArray = realArray;
    }

    public char[] getCharArray() {
        return charArray;
    }

    public void setCharArray(char[] charArray) {
        this.charArray = charArray;
    }

    public void setLength(int length) {
        this.length = length;
    }
}