/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Variable;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * The risk of revision of a prosthesis is a Weibull hazard on the age, which is its time variable, the sex and the
 * type of prosthesis. Pasting the node with its parents copies the relation and replaces each of its variables by
 * the pasted one; the time variable, which the relation keeps apart, stayed the one of the original, so the pasted
 * relation could not be computed (error 257 of the pending list).
 *
 * @author Manuel Arias
 */
public class APastedWeibullHazardCountsTheTimeWithThePastedVariableTest {

    private static final double[] COEFFICIENTS = {0.3740968, -5.490935, -0.0367022, 0.768536, -1.344474};
    private static final double[] COVARIANCES = {0.0022515, -0.005691, 0.0432191, 0.000000028, -0.000783, 0.00002715,
            0.0000051, -0.007247, 0.000033, 0.01189, 0.000259, -0.000642, -0.000111, 0.000184, 0.14636};

    private static Variable pasted(Variable original, String suffix) {
        Variable copy = new Variable(original);
        copy.setName(original.getName() + suffix);
        return copy;
    }

    /** What pasting does to the relation of a node: a copy with every variable replaced by the pasted one. */
    private static WeibullHazardPotential paste(WeibullHazardPotential original, List<Variable> pastedVariables) {
        WeibullHazardPotential copy = (WeibullHazardPotential) original.copy();
        for (int i = 0; i < pastedVariables.size(); i++) {
            copy.replaceVariable(i, pastedVariables.get(i));
        }
        return copy;
    }

    @Test
    public void thePastedRelationCountsTheTimeWithThePastedAge() throws Exception {
        Variable risk = new Variable("Revision Risk", "no", "yes");
        Variable age = new Variable("Age", true, 0.0, Double.POSITIVE_INFINITY, false, 1.0);
        Variable sex = new Variable("Sex", "Female", "Male");
        Variable prosthesis = new Variable("Prosthesis Type", "Standard", "NP1");
        risk.setTimeSlice(5);
        age.setTimeSlice(5);
        List<Variable> variables = new ArrayList<>(List.of(risk, age, sex, prosthesis));
        WeibullHazardPotential original = new WeibullHazardPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                                     COEFFICIENTS, COVARIANCES);
        original.setTimeVariable(age);
        EvidenceCase sixtyFive = new EvidenceCase();
        sixtyFive.addFinding(new Finding(age, 65.0));
        double[] expected = original.tableProject(sixtyFive, null).getValues();

        // In the same network the pasted nodes get new names; in another one they may keep them
        for (String suffix : List.of("'", "")) {
            List<Variable> pastedVariables = variables.stream().map(variable -> pasted(variable, suffix)).toList();
            WeibullHazardPotential copy = paste(original, pastedVariables);
            Variable pastedAge = pastedVariables.get(1);

            assertSame(pastedAge, copy.getTimeVariable(), "the time variable, pasted as Age" + suffix);
            EvidenceCase pastedSixtyFive = new EvidenceCase();
            pastedSixtyFive.addFinding(new Finding(pastedAge, 65.0));
            TablePotential table = copy.tableProject(pastedSixtyFive, null);
            assertEquals(List.of(pastedVariables.get(0), pastedVariables.get(2), pastedVariables.get(3)), table.getVariables());
            assertArrayEquals(expected, table.getValues(), 1e-12);
        }
        assertSame(age, original.getTimeVariable(), "the original keeps its own");
    }

    /** A time variable with states, named by their values, is part of the table: the pasted one, not the original. */
    @Test
    public void aTimeVariableWithStatesIsThePastedOneInTheTable() throws Exception {
        Variable risk = new Variable("Risk", "no", "yes");
        Variable years = new Variable("Years", "1", "2", "3");
        List<Variable> variables = new ArrayList<>(List.of(risk, years));
        WeibullHazardPotential original = new WeibullHazardPotential(variables, PotentialRole.CONDITIONAL_PROBABILITY,
                                                                     new double[]{-2, 0.1, 0.5},
                                                                     new double[]{0.01, 0, 0.01, 0, 0, 0.01});
        original.setTimeVariable(years);
        double[] expected = original.tableProject(new EvidenceCase(), null).getValues();

        for (String suffix : List.of("'", "")) {
            List<Variable> pastedVariables = variables.stream().map(variable -> pasted(variable, suffix)).toList();
            WeibullHazardPotential copy = paste(original, pastedVariables);

            assertSame(pastedVariables.get(1), copy.getTimeVariable(), "the time variable, pasted as Years" + suffix);
            TablePotential table = copy.tableProject(new EvidenceCase(), null);
            assertEquals(pastedVariables, table.getVariables());
            assertArrayEquals(expected, table.getValues(), 1e-12);
        }
    }
}
