/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;

import java.io.Serializable;

/**
 * Defines the different actions on the state an PartitionedInterval objects
 *
 * @author mpalacios
 * @version 1.0
 */
public enum StateAction implements Serializable, Localizable {
    ADD,
    REMOVE,
    RENAME,
    UP,
    DOWN,
    MODIFY_DELIMITER_INTERVAL,
    MODIFY_VALUE_INTERVAL;
    
    @Override public @NotNull String path() {
        return "";
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        return this.name();
    }
    
    @Override public String toString() {
        return this.localize();
    }
}
