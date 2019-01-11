package javacc;

import semantic.Type;

import java.util.Arrays;

/**
 * 符号表的表项
 */
public class Symbol {
    private String symbol;
    // 数据类型: int double ...
    private String type;
    // 数组长度
    private int length = -1;
    // 元素的整形数值
    private int intValue;
    // 元素的浮点型数值
    private double realValue;
    // 元素的字符值
    private char charvalue;
    // 整数数组
    private int[] intArray;
    // 小数数组
    private double[] realArray;
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
            case Type.INT:
                intValue = (int) Double.parseDouble(value);
                break;
            case Type.DOUBLE:
            case Type.FLOAT:
                realValue = Double.parseDouble(value);
                break;
            case Type.CHAR:
                charvalue = value.charAt(0);
                break;
        }
    }

    public void selfAdd() {
        switch (type) {
            case Type.INT:
                intValue++;
                break;
            case Type.DOUBLE:
            case Type.FLOAT:
                realValue++;
                break;
        }
    }

    public void selfMin() {
        switch (type) {
            case Type.INT:
                intValue--;
                break;
            case Type.DOUBLE:
            case Type.FLOAT:
                realValue--;
                break;
        }
    }

    public void setArrayValue(int index, String value) throws NumberFormatException {
        switch (type) {
            case Type.INT:
                intArray[index] = Integer.parseInt(value);
                break;
            case Type.DOUBLE:
            case Type.FLOAT:
                realArray[index] = Float.parseFloat(value);
                break;
            case Type.CHAR:
                realArray[index] = value.charAt(0);
                break;
        }
    }

    public int getIntValue() {
        return intValue;
    }

    public double getRealValue() {
        return realValue;
    }

    public double getValue() {
        switch (type) {
            case Type.INT:
                return intValue;
            case Type.DOUBLE:
            case Type.FLOAT:
                return realValue;
        }
        return 0;
    }

    public String getValueString() {
        switch (type) {
            case Type.INT:
                return String.valueOf(intValue);
            case Type.DOUBLE:
            case Type.FLOAT:
                return String.valueOf(realValue);
        }
        return "";
    }

    public String getArray() {
        switch (type) {
            case Type.INT:
                return Arrays.toString(intArray);
            case Type.DOUBLE:
            case Type.FLOAT:
                return Arrays.toString(realArray);
            case Type.CHAR:
                return Arrays.toString(charArray);
        }
        return null;
    }

    public String getArray(int index) {
        switch (type) {
            case Type.INT:
                return "" + intArray[index];
            case Type.DOUBLE:
            case Type.FLOAT:
                return "" + realArray[index];
            case Type.CHAR:
                return "" + charArray[index];
        }
        return null;
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

    public double[] getRealArray() {
        return realArray;
    }

    public void setRealArray(double[] realArray) {
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
        switch (type) {
            case Type.INT:
                intArray = new int[length];
            case Type.DOUBLE:
            case Type.FLOAT:
                realArray = new double[length];
            case Type.CHAR:
                charArray = new char[length];
        }
    }
}