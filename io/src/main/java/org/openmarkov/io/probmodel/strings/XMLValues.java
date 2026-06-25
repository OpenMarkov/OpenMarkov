/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.io.probmodel.strings;

import org.openmarkov.java.enumUtils.EnumUtils;

import java.io.Serializable;

public enum XMLValues implements Serializable {
    FALSE,
    TRUE,
    TABLE,  // potential role
    DECISION,  // potential role
    UTILITY,  // potential role
    LEFT, // for intervals (belongsTo = left)
    RIGHT, // for intervals (belongsTo = right)
    POLICY;
    
    public String toString() {
        return switch (this) {
            case POLICY -> "Policy";
            default -> EnumUtils.toCamelCase(this);
        };
	}
	
}