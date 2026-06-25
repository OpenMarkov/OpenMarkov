/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

/**
 * A token represents an identifier or a reserved word or a number.
 *
 * @author Manuel Arias
 */
public class ElviraToken {

	// Attributes
	private String identifierString;

	private ReservedWord reservedWord;

	private int integerValue;

	private double doubleValue;

	private boolean booleanValue;

	private String stringValue1;

	private String stringValue2;

	private String[] stringListValue;

	private double[] doublesTableValue;

	private boolean isReservedWord;

	private boolean isIdentifier;

	private boolean isInteger;

	private boolean isDouble;

	private boolean isBoolean;

	private final TokenType tokenType;

	// Constructors

	/**
	 * Integer value constructor.
	 *
     * @param tokenType    {@code TokenType}
     * @param integerValue {@code int}
	 */
	public ElviraToken(TokenType tokenType, int integerValue) {
		this.tokenType = tokenType;
		this.integerValue = integerValue;
		this.isInteger = true;
	}

	/**
	 * Double value constructor.
	 *
     * @param tokenType   {@code TokenType}
     * @param doubleValue {@code double}
	 */
	public ElviraToken(TokenType tokenType, double doubleValue) {
		this.tokenType = tokenType;
		this.doubleValue = doubleValue;
		this.isDouble = true;
	}

	/**
	 * Identifier constructor.
	 *
     * @param tokenType        {@code TokenType}
     * @param identifierString {@code String}
	 * tokenType == IDENTIFIER during the whole method execution
	 */
	public ElviraToken(TokenType tokenType, String identifierString) {
		this.tokenType = tokenType;
		this.identifierString = identifierString;
		this.isIdentifier = true;
	}

	/**
	 * Reserved word constructor.
	 *
     * @param tokenType    {@code TokenType}
     * @param reservedWord {@code ReservedWord}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord) {
		this.tokenType = tokenType;
		this.reservedWord = reservedWord;
		this.isReservedWord = true;
	}

	/**
	 * Reserved word constructor.
	 *
     * @param tokenType    {@code TokenType}
     * @param reservedWord {@code ReservedWord}
     * @param integerValue {@code int}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, int integerValue) {
		this(tokenType, reservedWord);
		this.integerValue = integerValue;
		this.isInteger = true;
	}

	/**
	 * Reserved word constructor.
	 *
     * @param tokenType    {@code TokenType}
     * @param reservedWord {@code ReservedWord}
     * @param doubleValue  {@code double}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, double doubleValue) {
		this(tokenType, reservedWord);
		this.doubleValue = doubleValue;
		this.isDouble = true;
	}

	/**
	 * Boolean value constructor.
	 *
     * @param tokenType    {@code TokenType}
     * @param booleanValue {@code boolean}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, boolean booleanValue) {
		this(tokenType, reservedWord);
		this.booleanValue = booleanValue;
		this.isBoolean = true;
	}

	/**
	 * Reserved word constructor.
	 *
     * @param tokenType    {@code TokenType}
     * @param reservedWord {@code ReservedWord}
     * @param stringValue1 {@code String}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, String stringValue1) {
		this(tokenType, reservedWord);
		this.stringValue1 = stringValue1;
	}

	/**
	 * Reserved word constructor.
	 *
     * @param tokenType    {@code TokenType}
     * @param reservedWord {@code ReservedWord}
     * @param stringValue1 {@code String}
     * @param stringValue2 {@code String}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, String stringValue1, String stringValue2) {
		this(tokenType, reservedWord);
		this.stringValue1 = stringValue1;
		this.stringValue2 = stringValue2;
	}

	/**
	 * Reserved word constructor for words with a list of strings.
	 *
     * @param tokenType       {@code TokenType}
     * @param reservedWord    {@code ReservedWord}
     * @param stringListValue {@code String[]}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, String[] stringListValue) {
		this(tokenType, reservedWord);
		this.stringListValue = stringListValue;
	}

	/**
	 * Reserved word constructor for tables of doubles.
	 *
     * @param tokenType    {@code TokenType}
     * @param reservedWord {@code ReservedWord}
	 */
	public ElviraToken(TokenType tokenType, ReservedWord reservedWord, double[] table) {
		this(tokenType, reservedWord);
		this.doublesTableValue = table;
	}

	// Methods

	/**
     * @return Reserved word of this token. {@code ReservedWord}
	 */
	public ReservedWord getReservedWord() {
		return reservedWord;
	}

	/**
     * @return isInteger. {@code boolean}
	 */
	public boolean isInteger() {
		return isInteger;
	}

	/**
     * @return isDouble. {@code boolean}
	 */
	public boolean isDouble() {
		return isDouble;
	}

	/**
     * @return isReservedWord. {@code boolean}
	 */
	public boolean isReservedWord() {
		return isReservedWord;
	}

	/**
     * @return isIdentifier. {@code boolean}
	 */
	public boolean isIdentifier() {
		return isIdentifier;
	}

	/**
     * @return isBoolean. {@code boolean}
	 */
	public boolean isBoolean() {
		return isBoolean;
	}

	/**
     * @return integerValue. {@code int}
	 */
	public int getIntegerValue() {
		return integerValue;
	}

	/**
     * @return doubleValue. {@code double}
	 */
	public double getDoubleValue() {
		return doubleValue;
	}

	/**
     * @return stringValue. {@code String}
	 */
	public String getStringValue1() {
		return stringValue1;
	}

	/**
     * @return stringValue. {@code String}
	 */
	public String getStringValue2() {
		return stringValue2;
	}

	/**
     * @return identifierString. {@code String}
	 */
	public String getIdentifierString() {
		return identifierString;
	}

	/**
     * @return booleanValue. {@code boolean}
	 */
	public boolean getBooleanValue() {
		return booleanValue;
	}

	/**
     * @return stringListValue. {@code String[]}
	 */
	public String[] getStringListValue() {
		return stringListValue;
	}

	/**
     * @return doublesTableValue. {@code double[]}
	 */
	public double[] getDoublesTableValue() {
		return doublesTableValue;
	}

	/**
     * @return tokenType. {@code Enumerate TokenType}
	 */
	public TokenType getTokenType() {
		return tokenType;
	}

	/**
	 * Checks whether the given token is equivalent to the current token.
	 * Note: this method currently always returns false (stub implementation).
	 *
     * @param token the {@code ElviraToken} to compare
	 * @return {@code true} if the tokens are equivalent, {@code false} otherwise
	 */
    public static boolean sameToken(ElviraToken token) {
		/*
        boolean equalStringList;
		equalStringList = (
				(token.stringListValue == stringListValue) && (stringListValue == null)
		);
        */
        return false;
	}

	public String toString() {
        String string = "";
		string = string + "Token type: " + tokenType + "\n";
		if (isReservedWord) {
			string = string + "Reserved word: " + reservedWord;
			if (stringValue1 != null) {
				string = string + "(" + stringValue1;
				if (stringValue2 != null) {
					string = string + "," + stringValue2 + ")";
				} else {
					string = string + ")\n";
				}
			} else {
				string = string + "\n";
			}
		} else if (isIdentifier) {
			string = string + "Identifier: " + identifierString;
		}
		if (isInteger) {
			string = string + "Value = " + integerValue;
		}
		if (isDouble) {
			string = string + "Value = " + doubleValue;
		}
		if (isBoolean) {
			string = string + "Value = " + booleanValue;
		}
		if (stringListValue != null) {
			string = string + "Value = ";
			for (String stringValue : stringListValue) {
				string = string + " " + stringValue;
			}
		}
		if (doublesTableValue != null) {
			string = string + "Value = ";
			for (int i = 0; i < doublesTableValue.length; i++) {
				string = string + doublesTableValue[i];
			}
		}
		return string;
	}

}
