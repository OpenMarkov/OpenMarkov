/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

// $ANTLR : "arff.g" -> "ArffLexer.java"$

package org.openmarkov.io.database.weka;

public interface ArffLexerTokenTypes {
    int EOF = 1;
    int NULL_TREE_LOOKAHEAD = 3;
    int QUOTATION = 4;
    int COMMA = 5;
    int LEFTB = 6;
    int RIGHTB = 7;
    int WHITE = 8;
    int REMARK = 9;
    int SEPARATOR = 10;
    int IDENT = 11;
    int STRING = 12;
    int RELATION = 13;
    int ATTRIBUTE = 14;
    int DATA = 15;
}
