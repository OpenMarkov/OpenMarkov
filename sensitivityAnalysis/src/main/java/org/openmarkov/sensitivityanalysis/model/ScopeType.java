/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.sensitivityanalysis.model;

import org.openmarkov.java.enumUtils.EnumUtils;

/**
 * Enum with the scope types
 *
 * @author jperez-martin
 */
public enum ScopeType {
	// Analysis type options
    GLOBAL,
    DECISION;
    
    @Override public String toString() {
        return "ScopeSelector.Scenario." + EnumUtils.toPascalCase(this);
	}
}
