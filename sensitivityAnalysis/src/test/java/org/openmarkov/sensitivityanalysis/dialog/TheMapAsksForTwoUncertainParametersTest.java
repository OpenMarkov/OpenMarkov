/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.sensitivityanalysis.dialog;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.sensitivityanalysis.exceptions.UncertainParameterException;
import org.openmarkov.sensitivityanalysis.model.ParameterType;
import org.openmarkov.sensitivityanalysis.model.SensitivityAnalysisConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The sensitivity analysis dialog refuses an analysis when the network has fewer named uncertain parameters than it
 * needs: two for the map, which moves two at once, one for the tornado and the plot, none for the others.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheMapAsksForTwoUncertainParametersTest {

    /** The configuration that the dialog sets for an analysis. */
    private static SensitivityAnalysisConfiguration configuration(ParameterType parameterType, boolean biaxial) {
        SensitivityAnalysisConfiguration configuration = new SensitivityAnalysisConfiguration();
        configuration.setParameterType(parameterType);
        configuration.setIsBiaxial(biaxial);
        return configuration;
    }

    @Test void eachAnalysisNeedsTheParametersItMoves() {
        assertEquals(2, configuration(ParameterType.ONE_PARAMETER, true).requiredUncertainParameters(), "map");
        assertEquals(1, configuration(ParameterType.MULTI_PARAMETER, false).requiredUncertainParameters(), "tornado");
        assertEquals(1, configuration(ParameterType.ONE_PARAMETER, false).requiredUncertainParameters(), "plot");
        assertEquals(0, configuration(ParameterType.NO_PARAMETER, false).requiredUncertainParameters(), "CE plane");
    }

    @Test void theRefusalSaysHowManyThereAreAndHowManyAreNeeded() {
        assertEquals("Uncertain parameters in the network: 0. This analysis needs at least 2.",
                     new UncertainParameterException.FewUncertainParameters(2, 0).getExceptionMessage());
    }
}
