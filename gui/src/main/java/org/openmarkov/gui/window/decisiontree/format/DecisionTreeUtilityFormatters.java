/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import org.openmarkov.core.model.network.CEP;

import java.text.DecimalFormat;

/**
 * Single entry point for formatting the utility carried by a decision-tree
 * node when its concrete type is not statically known (e.g. inside generic
 * GUI panels).
 *
 * <p>Dispatches by runtime type to {@link DoubleUtilityFormatter} or
 * {@link CEPUtilityFormatter}; future analysis types only need to register a
 * new formatter here.
 */
public final class DecisionTreeUtilityFormatters {

    private DecisionTreeUtilityFormatters() {
    }

    /**
     * Formats the given utility value, choosing the right formatter by
     * runtime type. Returns an empty string when the utility is {@code null}.
     *
     * @throws IllegalArgumentException if the utility type is not supported.
     */
    public static String format(Object utility, DecimalFormat df, boolean addSlashPrefix) {
        if (utility == null) {
            return "";
        }
        if (utility instanceof Double d) {
            return DoubleUtilityFormatter.INSTANCE.format(d, df, addSlashPrefix);
        }
        if (utility instanceof CEP cep) {
            return CEPUtilityFormatter.INSTANCE.format(cep, df, addSlashPrefix);
        }
        throw new IllegalArgumentException(
                "No DecisionTreeUtilityFormatter registered for type: " + utility.getClass());
    }
}
