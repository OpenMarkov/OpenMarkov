/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential.canonical;

import org.jetbrains.annotations.NotNull;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;
import org.openmarkov.java.enumUtils.EnumUtils;

import java.io.Serializable;

/**
 * Enumerates the specific ICI model types: OR, CAUSAL_MAX, GENERAL_MAX (all in the
 * OR/MAX family), AND, CAUSAL_MIN, GENERAL_MIN (all in the AND/MIN family), and TUNING.
 * Each type maps to an {@link ICIFamily} via {@link #getFamily()}.
 */
public enum ICIModelType implements Serializable, Localizable {
    OR,
    CAUSAL_MAX,
    GENERAL_MAX,
    AND,
    CAUSAL_MIN,
    GENERAL_MIN,
    TUNING;
    
    public ICIFamily getFamily() {
        return switch (this) {
            case OR, CAUSAL_MAX, GENERAL_MAX -> ICIFamily.OR;
            case AND, CAUSAL_MIN, GENERAL_MIN -> ICIFamily.AND;
            case TUNING -> ICIFamily.TUNING;
        };
    }
    
    @Override public @NotNull String path() {
        return "";
    }
    
    @Override public @NotNull String localize(LocalizationFormatter formatter) {
        return this.toString();
    }
    
    @Override public String toString() {
        return EnumUtils.toCamelCase(this);
    }
}
