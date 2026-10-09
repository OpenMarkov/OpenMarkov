/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.ComplementFunction;
import org.openmarkov.core.model.network.modelUncertainty.DirichletFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.exception.FamilyDistributionRuleBrokenException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The third rule of the dialog of the uncertainty: with a Dirichlet in a column, at least another cell must be a
 * Dirichlet and the rest must be the exact value zero.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AColumnOfDirichletDistributionsIsAcceptedTest {

    private static UncertainValue dirichlet(double alpha) {
        return new UncertainValue(new DirichletFunction(alpha));
    }

    /** A column of MID-Chancellor-corrected, node «State [1]». */
    @Test void threeDirichletsAndAnExactZeroAreAccepted() throws Exception {
        assertTrue(UncertainValuesDialog.verifyGlobalConstraintUncertainty(
                List.of(dirichlet(15), dirichlet(512), dirichlet(731), new UncertainValue(0.0))));
    }

    @Test void aDirichletAloneIsRejected() {
        assertThrows(FamilyDistributionRuleBrokenException.Rule3Broken.class,
                     () -> UncertainValuesDialog.verifyGlobalConstraintUncertainty(
                             List.of(dirichlet(15), new UncertainValue(0.0), new UncertainValue(0.0))));
    }

    @Test void twoDirichletsWithAnotherDistributionAreRejected() {
        assertThrows(FamilyDistributionRuleBrokenException.Rule3Broken.class,
                     () -> UncertainValuesDialog.verifyGlobalConstraintUncertainty(
                             List.of(dirichlet(15), dirichlet(512), new UncertainValue(new ComplementFunction(1)))));
    }

    @Test void aBetaWithAComplementIsStillAccepted() throws Exception {
        assertTrue(UncertainValuesDialog.verifyGlobalConstraintUncertainty(
                List.of(new UncertainValue(new BetaFunction(14, 86)), new UncertainValue(new ComplementFunction(1)))));
    }
}
