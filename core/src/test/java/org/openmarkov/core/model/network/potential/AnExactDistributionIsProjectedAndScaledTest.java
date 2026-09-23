/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.ExactFunction;
import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * An "Exact" distribution with numbers, the way format 1.0 writes an exact relation, is projected
 * to the table of its values and is scaled; any other distribution still cannot be a table.
 *
 * @author Manuel Arias
 */
public class AnExactDistributionIsProjectedAndScaledTest {

    private final Variable cost = new Variable("Cost");
    private final Variable implant = new Variable("Implant", new State[] { new State("no"), new State("yes") });

    private UnivariateDistrPotential exact() {
        UnivariateDistrPotential potential = new UnivariateDistrPotential(List.of(cost, implant), ExactFunction.class,
                PotentialRole.UNSPECIFIED);
        potential.getDistributionTable().setValues(new double[] { 0.0, 4.64 });
        return potential;
    }

    @Tag(TestSpeed.FAST)
    @Test public void itIsProjectedToItsValues() throws Exception {
        TablePotential table = exact().tableProject(new EvidenceCase(), null);

        assertEquals(List.of(implant), table.getVariables());
        assertArrayEquals(new double[] { 0.0, 4.64 }, table.getValues(), 0.0);
    }

    @Tag(TestSpeed.FAST)
    @Test public void itIsScaled() throws Exception {
        UnivariateDistrPotential potential = exact();
        potential.scalePotential(2);

        assertArrayEquals(new double[] { 0.0, 9.28 }, potential.tableProject(new EvidenceCase(), null).getValues(), 1E-12);
    }

    @Tag(TestSpeed.FAST)
    @Test public void anotherDistributionIsNotATable() {
        UnivariateDistrPotential normal = new UnivariateDistrPotential(List.of(cost, implant), NormalFunction.class,
                PotentialRole.UNSPECIFIED);

        assertThrows(NonProjectablePotentialException.PotentialCannotBeConvertedToATable.class,
                () -> normal.tableProject(new EvidenceCase(), null));
    }
}
