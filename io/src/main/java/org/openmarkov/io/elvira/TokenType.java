/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.elvira;

import java.io.Serializable;

/**
 * Types of tokens:
 * <ol>
 * <li>IDENT
 * <li>RESERVED
 * <li>INTEGER
 * <li>DOUBLE
 * </ol>
 *
 * @author Manuel Arias
 */
public enum TokenType implements Serializable {
    IDENTIFIER,
    RESERVED,
    INTEGER,
    DOUBLE;
}
