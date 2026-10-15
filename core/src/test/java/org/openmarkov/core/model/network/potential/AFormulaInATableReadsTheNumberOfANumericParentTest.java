/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

/**
 * The probability that a person smokes is 1 - {Age}/100 for "no" and {Age}/100 for "yes". The formulas of the table
 * have to be worked out with the number observed for the age: the values of the findings were written as the names
 * of their states, which a numeric finding does not have, and the table died with an index out of bounds (error 269
 * of the pending list).
 *
 * @author Manuel Arias
 */
public class AFormulaInATableReadsTheNumberOfANumericParentTest {

    @Test
    public void aSmokerOf40() throws Exception {
        Variable smoker = new Variable("Smoker", new State[]{new State("no"), new State("yes")});
        Variable age = new Variable("Age");
        List<Variable> variables = new ArrayList<>(List.of(smoker, age));
        AugmentedProbTablePotential table = new AugmentedProbTablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        VariableExpression[] cells = table.getAugmentedProbTable().getFunctionValues();
        cells[0] = new VariableExpression(variables, "1-{Age}/100");
        cells[1] = new VariableExpression(variables, "{Age}/100");
        EvidenceCase aPersonOf40 = new EvidenceCase();
        aPersonOf40.addFinding(new Finding(age, 40.0));

        // The cells are worked out in single precision (AugmentedProbTableInference), hence the tolerance
        assertArrayEquals(new double[]{0.6, 0.4}, table.tableProject(aPersonOf40, null, new ArrayList<>()).getValues(), 1e-6);
    }
}
