package org.openmarkov.io.amua.model;

/**
 * Represents the dimension information associated with a decision tree
 * in Amua expected format.
 *
 * @author Hugo Manuel
 * @version 1.0
 */

public class AmuaDimensionInfo {
    // name of the dimension
    private String name;

    // symbols associated with de dimension
    private String symbols;

    // number of decimal places used in the analysis
    private int decimals;

    public AmuaDimensionInfo(String name, String symbols, int decimals) {
        this.name = name;
        this.symbols = symbols;
        this.decimals = decimals;
    }

    public String getName() {
        return name;
    }

    public String getSymbols() {
        return symbols;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbols(String symbols) {
        this.symbols = symbols;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }
}