package semantic;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, Symbol> symbolMap;

    public SymbolTable() {
        symbolMap = new HashMap<>();
    }

    public void insert(Symbol symbol) {
        symbolMap.put(symbol.getName(), symbol);
    }

    public Symbol query(String symbol) {
        return symbolMap.get(symbol);
    }

    public void delete(String symbol) {
        symbolMap.remove(symbol);
    }
}
