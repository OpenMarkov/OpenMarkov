/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.temporalevolution;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JTable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The table of the temporal evolution of a chance variable with two states that the network defines in cycles 1
 * and 2, but not in cycle 0.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTableOfAVariableThatStartsInTheFirstCycleTest {

    private static Variable inCycle(int cycle) {
        Variable variable = new Variable("X [" + cycle + "]", "no", "yes");
        variable.setTimeSlice(cycle);
        return variable;
    }

    @Test void theCycleInWhichItDoesNotExistHasNoValue() {
        Map<Variable, TablePotential> evolution = new LinkedHashMap<>();
        for (int cycle = 1; cycle <= 2; cycle++) {
            Variable variable = inCycle(cycle);
            evolution.put(variable, new TablePotential(List.of(variable), PotentialRole.CONDITIONAL_PROBABILITY,
                                                       new double[]{ 0.25 * cycle, 1 - 0.25 * cycle }));
        }

        JTable table = (JTable) new TemporalEvolutionTablePane(evolution, new ProbNet(), inCycle(1), List.of(), 2,
                                                               false, false).getViewport().getView();

        // the first column has the states; the next ones, cycles 0, 1 and 2
        assertEquals("-", table.getValueAt(0, 1));
        assertEquals(0.25, table.getValueAt(0, 2));
        assertEquals(0.5, table.getValueAt(0, 3));
    }
}
