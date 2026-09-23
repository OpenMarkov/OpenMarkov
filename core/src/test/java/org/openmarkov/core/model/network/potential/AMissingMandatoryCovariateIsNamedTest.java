/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A regression without Constant, or a Weibull hazard without Gamma, cannot be computed: it says
 * which covariate is missing instead of failing with index -1.
 *
 * @author Manuel Arias
 */
public class AMissingMandatoryCovariateIsNamedTest {

    private final Variable numeric = new Variable("Y");
    private final Variable event = new Variable("E", new State[] { new State("no"), new State("yes") });
    private final VariableExpression[] onlyConstant = { VariableExpression.Common.CONSTANT };
    private final VariableExpression[] onlyGamma = { VariableExpression.Common.GAMMA };

    private static void assertMissing(String covariate, Potential potential) {
        NonProjectablePotentialException.MissingMandatoryCovariate e = assertThrows(
                NonProjectablePotentialException.MissingMandatoryCovariate.class,
                () -> potential.tableProject(new EvidenceCase(), null));
        assertEquals(covariate, e.covariate);
    }

    @Tag(TestSpeed.FAST)
    @Test public void aWeibullHazardWithoutGamma() {
        assertMissing("Gamma", new WeibullHazardPotential(List.of(event), PotentialRole.CONDITIONAL_PROBABILITY,
                onlyConstant, new double[] { 0 }));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aLinearCombinationWithoutConstant() {
        assertMissing("Constant", new LinearCombinationPotential(List.of(numeric), PotentialRole.UNSPECIFIED,
                onlyGamma, new double[] { 0 }));
    }

    @Tag(TestSpeed.FAST)
    @Test public void anExponentialWithoutConstant() {
        assertMissing("Constant", new ExponentialPotential(List.of(numeric), PotentialRole.UNSPECIFIED,
                onlyGamma, new double[] { 0 }));
    }

    @Tag(TestSpeed.FAST)
    @Test public void anExponentialHazardNeedsOnlyConstant() throws Exception {
        ExponentialHazardPotential hazard = new ExponentialHazardPotential(List.of(event),
                PotentialRole.CONDITIONAL_PROBABILITY, onlyConstant, new double[] { 0 }, null);
        assertEquals(2, hazard.tableProject(new EvidenceCase(), null).getValues().length);
    }

    @Tag(TestSpeed.FAST)
    @Test public void theAccessorsSayWhatIsMissing() {
        LinearCombinationPotential linear = new LinearCombinationPotential(List.of(numeric), PotentialRole.UNSPECIFIED,
                onlyGamma, new double[] { 0 });
        InvalidArgumentException e = assertThrows(InvalidArgumentException.class, linear::getConstant);
        assertTrue(e.getMessage().contains("Constant"), e.getMessage());
    }
}
