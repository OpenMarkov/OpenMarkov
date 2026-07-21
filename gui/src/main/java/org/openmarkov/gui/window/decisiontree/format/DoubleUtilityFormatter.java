/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import java.text.DecimalFormat;

/**
 * Formatter for plain expected-utility decision trees: prints
 * {@code " U=<value>"} with the given decimal format. {@code NaN} utilities
 * are rendered as {@code 0}.
 *
 * <p>Extracted from {@code EvaluationDecisionTreeNode#formatUtility} as part of
 * Phase 3 of the decisiontree refactor (separating model from presentation).
 */
public final class DoubleUtilityFormatter implements DecisionTreeUtilityFormatter<Double> {

    public static final DoubleUtilityFormatter INSTANCE = new DoubleUtilityFormatter();

    private DoubleUtilityFormatter() {
    }

    @Override
    public String format(Double utility, DecimalFormat df, boolean addSlashPrefix) {
        double value = utility == null || Double.isNaN(utility) ? 0.0 : utility;
        StringBuilder out = new StringBuilder();
        if (addSlashPrefix) {
            out.append(" / ");
        }
        out.append(" U=").append(df.format(value));
        return out.toString();
    }
}
