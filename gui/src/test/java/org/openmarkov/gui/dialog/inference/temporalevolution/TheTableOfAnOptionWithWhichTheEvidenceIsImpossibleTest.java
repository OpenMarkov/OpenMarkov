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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The table of the temporal evolution of a chance variable with two states, for a decision with the options
 * «cure» and «wait», when the probabilities of «cure» are all zero because the evidence cannot happen with it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheTableOfAnOptionWithWhichTheEvidenceIsImpossibleTest {

    @Test void thatOptionHasNoValue() {
        Variable patient = new Variable("Patient [0]", "healthy", "ill");
        patient.setTimeSlice(0);
        Variable therapy = new Variable("Therapy", "cure", "wait");
        TablePotential inCycle0 = new TablePotential(List.of(patient, therapy), PotentialRole.CONDITIONAL_PROBABILITY,
                                                     new double[]{ 0, 0, 0.1, 0.9 });

        JTable table = (JTable) new TemporalEvolutionTablePane(Map.of(patient, inCycle0), new ProbNet(), patient,
                                                               List.of(therapy), 0, false, false)
                .getViewport().getView();

        // the columns are the option, the state and cycle 0
        assertEquals("-", table.getValueAt(0, 2));
        assertEquals("-", table.getValueAt(1, 2));
        assertEquals(0.1, table.getValueAt(2, 2));
        assertEquals(0.9, table.getValueAt(3, 2));
    }
}
