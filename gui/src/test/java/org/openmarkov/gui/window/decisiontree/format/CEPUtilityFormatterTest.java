/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.window.decisiontree.format;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.CostEffectivenessException;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.potential.StrategyTree;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CEPUtilityFormatterTest {

    private static final DecimalFormat DF =
            new DecimalFormat("0.0000", new DecimalFormatSymbols(Locale.US));

    @Test
    void emptyCEPRendersAsSingleSpace() {
        // The formatter does not need StrategyTree-aware data when the CEP
        // has no strategy trees: hasStrategyTrees() == false should short-circuit.
        CEP zero = CEP.getZeroPartition();
        String out = CEPUtilityFormatter.INSTANCE.format(zero, DF, false);
        assertEquals(" ", out);
    }

    @Test
    void nullCEPRendersAsSingleSpace() {
        String out = CEPUtilityFormatter.INSTANCE.format(null, DF, false);
        assertEquals(" ", out);
    }

    @Test
    void singleIntervalRendersCostAndEffectiveness() throws Exception {
        CEP cep = singleIntervalCEP(10.0, 2.0);
        String out = CEPUtilityFormatter.INSTANCE.format(cep, DF, false);
        assertEquals(" Cost=10.0000 / Effectiveness=2.0000", out);
    }

    @Test
    void singleIntervalWithSlashPrefixPrependsSlash() throws Exception {
        CEP cep = singleIntervalCEP(10.0, 2.0);
        String out = CEPUtilityFormatter.INSTANCE.format(cep, DF, true);
        assertEquals(" / Cost=10.0000 / Effectiveness=2.0000", out);
    }

    @Test
    void multipleIntervalsRenderEachWithThresholds() throws Exception {
        // Two intervals: λ ∈ [0, 50): C=10/E=2  |  λ ∈ [50, 100): C=20/E=5
        CEP cep = new CEP(
                new StrategyTree[]{null, null},
                new double[]{10.0, 20.0},
                new double[]{2.0, 5.0},
                new double[]{50.0},
                0.0,
                100.0);
        String out = CEPUtilityFormatter.INSTANCE.format(cep, DF, false);
        assertEquals(" λ∈[0.0000,50.0000): C=10.0000/E=2.0000 |"
                        + " λ∈[50.0000,100.0000): C=20.0000/E=5.0000",
                out);
    }

    @Test
    void infinityMaxThresholdRendersAsInfinitySymbol() throws Exception {
        CEP cep = new CEP(
                new StrategyTree[]{null, null},
                new double[]{10.0, 20.0},
                new double[]{2.0, 5.0},
                new double[]{50.0},
                0.0,
                Double.POSITIVE_INFINITY);
        String out = CEPUtilityFormatter.INSTANCE.format(cep, DF, false);
        assertEquals(" λ∈[0.0000,50.0000): C=10.0000/E=2.0000 |"
                        + " λ∈[50.0000,∞): C=20.0000/E=5.0000",
                out);
    }

    private static CEP singleIntervalCEP(double cost, double effectiveness)
            throws CostEffectivenessException.WrongNumberOfThresholds,
                   CostEffectivenessException.WrongNumberOfCostsEffectivitiesAndInterventions {
        return new CEP(
                new StrategyTree[]{null},
                new double[]{cost},
                new double[]{effectiveness},
                null,
                0.0,
                100.0);
    }
}
