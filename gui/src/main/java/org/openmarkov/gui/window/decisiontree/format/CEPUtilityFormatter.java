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
 * Formatter for cost-effectiveness analysis decision trees. For a single
 * interval prints {@code " Cost=X / Effectiveness=Y"}; for multiple intervals
 * prints each one with its willingness-to-pay threshold range.
 *
 * <p>Extracted from {@code CEADecisionTreeNode#formatUtility} as part of
 * Phase 3 of the decisiontree refactor (separating model from presentation).
 */
public final class CEPUtilityFormatter implements DecisionTreeUtilityFormatter<CEP> {

    public static final CEPUtilityFormatter INSTANCE = new CEPUtilityFormatter();

    private CEPUtilityFormatter() {
    }

    @Override
    public String format(CEP cep, DecimalFormat df, boolean addSlashPrefix) {
        if (cep == null || !cep.hasStrategyTrees()) {
            return " ";
        }
        StringBuilder out = new StringBuilder();
        if (addSlashPrefix) {
            out.append(" /");
        }
        int numIntervals = cep.getNumIntervals();
        if (numIntervals == 1) {
            out.append(" Cost=").append(df.format(cep.getCost(0)))
               .append(" / Effectiveness=").append(df.format(cep.getEffectiveness(0)));
        } else {
            double[] thresholds = cep.getThresholds();
            for (int i = 0; i < numIntervals; i++) {
                if (i > 0) {
                    out.append(" |");
                }
                out.append(" λ∈[");
                if (i == 0) {
                    out.append(df.format(cep.getMinThreshold()));
                } else {
                    out.append(df.format(thresholds[i - 1]));
                }
                out.append(",");
                if (i == numIntervals - 1) {
                    if (cep.getMaxThreshold() == Double.POSITIVE_INFINITY) {
                        out.append("∞");
                    } else {
                        out.append(df.format(cep.getMaxThreshold()));
                    }
                } else {
                    out.append(df.format(thresholds[i]));
                }
                out.append("): C=").append(df.format(cep.getCost(i)))
                   .append("/E=").append(df.format(cep.getEffectiveness(i)));
            }
        }
        return out.toString();
    }
}
