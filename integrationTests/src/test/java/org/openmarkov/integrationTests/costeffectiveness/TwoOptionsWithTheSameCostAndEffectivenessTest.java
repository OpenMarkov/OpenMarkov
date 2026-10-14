/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.costeffectiveness;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.StrategyTree;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.costEffectiveness.CEDecisionResults;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The options «no» and «yes» of a decision cost nothing and have no effect, so their results are equal. The frontier
 * must name the option the analysis chose (error 84).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TwoOptionsWithTheSameCostAndEffectivenessTest {

    private static CEP nothing() throws Exception {
        return new CEP(new StrategyTree[]{ null }, new double[]{ 0 }, new double[]{ 0 }, new double[0], 0,
                       Double.POSITIVE_INFINITY);
    }

    @Test void theFrontierNamesTheOptionChosen() throws Exception {
        Variable decision = new Variable("D", "no", "yes");
        CEP[] results = { nothing(), nothing() };

        List<Integer> frontier = CEDecisionResults.frontierOptions(decision, results, List.of(0, 1), 1000);

        assertEquals(List.of(1), frontier);
    }
}
