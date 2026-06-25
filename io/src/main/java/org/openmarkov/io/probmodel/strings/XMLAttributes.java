/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.strings;

import org.openmarkov.java.enumUtils.EnumUtils;

/** Names of attributes */
public enum XMLAttributes {
    BELONGS_TO,
    DIRECTED,
    DISTRIBUTION,
    FORMAT_VERSION,
    FUNCTION,
    LABEL,
    NAME,
    NUMERIC_VALUE,
    ORDER,
	//For Univariate
    PARAMETRIZATION,
    REF,
    ROLE,
    SHOW_WHEN_OPENING_NETWORK,
    TIME_SLICE,
    TYPE,
    VALUE,
    VAR1,
    VAR2,
    X,
    Y,
	//TODO OOBN start
    IS_INPUT,
	//TODO OOBN end
    
    UNIT;
    
    public String toString() {
        return EnumUtils.toCamelCase(this);
	}
	
}
