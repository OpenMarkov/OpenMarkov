/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.decisiontree.ProbAndCEP;
import org.openmarkov.core.model.decisiontree.ProbAndUtilityAndStrategy;
import org.openmarkov.core.model.network.CEP;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DecisionTreeUtilityFormattersTest {

    private static final DecimalFormat DF =
            new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    @Test
    void dispatchesASingleCriterionEvaluationToDoubleFormatter() {
        String out = DecisionTreeUtilityFormatters.format(new ProbAndUtilityAndStrategy(1.0, 7.5), DF, false);
        assertEquals(" U=7.5000", out);
    }

    @Test
    void dispatchesACostEffectivenessEvaluationToCepFormatter() {
        CEP zero = CEP.getZeroPartition();
        String out = DecisionTreeUtilityFormatters.format(new ProbAndCEP(1.0, zero), DF, false);
        assertEquals(" ", out);
    }

    @Test
    void aNodeNotYetEvaluatedProducesEmptyString() {
        assertEquals("", DecisionTreeUtilityFormatters.format(null, DF, false));
    }

    @Test
    void aCostEffectivenessEvaluationWithoutPartitionProducesEmptyString() {
        assertEquals("", DecisionTreeUtilityFormatters.format(new ProbAndCEP(1.0, null), DF, false));
    }
}
