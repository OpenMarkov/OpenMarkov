/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.util;

/**
 * This interface defines (in alphabetical order) a set of tokens used
 * additionalProperties.
 */
public interface PropertyNames {
    
    enum netPropertyNames {
        DEFAULT_STATES, COMMENT, NAME, TYPE, VARIABLES_CONSTRAINT, OTHER_PROPERTIES, DEFAULT_PARTITIONEDINTERVAL
    }
    
    enum nodePropertyNames {
        NAME, STATES, COMMENT, PRECISION, RELEVANCE, TYPE, PARTITIONEDINTERVAL, PURPOSE, MODEL_TYPE, RELATION_TYPE, CANONICAL_PARAMETERS, COMPOUSE_VALUES, AS_VALUES, ALL_PARAMETERS
    }
    
    enum stateActions {
        ADD, REMOVE, RENAME, UP, DOWN
    }
    
}
