/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential.canonical;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The parameters of a canonical model are kept one row per parent, and the row is found by the
 * position of the parent among the variables. The conditioned variable is in that list too, and
 * its position gives no row, so handing it over gave a raw index error. A file whose model names
 * the child twice is enough to get there.
 *
 * <p>Two models of different kinds are not the same model either, however equal their numbers.
 *
 * @author Manuel Arias
 */
public class TheParametersOfACanonicalModelAreAskedForByParentTest {

    private static final Variable CHILD = new Variable("C", 2);
    private static final Variable PARENT = new Variable("P", 2);

    private static MaxPotential model(ICIModelType kind) {
        MaxPotential potential = new MaxPotential(kind, List.of(CHILD, PARENT));
        potential.setNoisyParameters(PARENT, new double[]{1.0, 0.0, 0.2, 0.8});
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void theChildIsRefusedWhereAParentIsExpected() {
        MaxPotential potential = model(ICIModelType.GENERAL_MAX);

        UnrecoverableException refused = assertThrows(UnrecoverableException.class,
                () -> potential.getNoisyParameters(CHILD));

        assertTrue(refused.getMessage().contains("C"), "the message names the variable: " + refused.getMessage());
    }

    @Tag(TestSpeed.FAST)
    @Test public void theChildIsRefusedWhenSettingParametersToo() {
        MaxPotential potential = model(ICIModelType.GENERAL_MAX);

        assertThrows(UnrecoverableException.class,
                () -> potential.setNoisyParameters(CHILD, new double[]{1.0, 0.0, 0.2, 0.8}));
    }

    @Tag(TestSpeed.FAST)
    @Test public void aVariableOfAnotherModelIsRefusedAsWell() {
        MaxPotential potential = model(ICIModelType.GENERAL_MAX);

        assertThrows(UnrecoverableException.class,
                () -> potential.getNoisyParameters(new Variable("Z", 2)));
    }

    @Tag(TestSpeed.FAST)
    @Test public void twoKindsOfModelWithTheSameNumbersAreNotEqual() {
        assertNotEquals(model(ICIModelType.GENERAL_MAX), model(ICIModelType.OR),
                "a general MAX and an OR are different models");
    }

    @Tag(TestSpeed.FAST)
    @Test public void twoModelsOfTheSameKindWithTheSameNumbersAreStillEqual() {
        assertEquals(model(ICIModelType.GENERAL_MAX), model(ICIModelType.GENERAL_MAX));
    }
}
