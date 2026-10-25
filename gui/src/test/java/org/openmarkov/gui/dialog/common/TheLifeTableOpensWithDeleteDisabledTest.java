/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The life table of an event opens with «Delete» disabled, since no row is selected, and enables it when a row is
 * selected and the table has more than one stretch.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheLifeTableOpensWithDeleteDisabledTest {

    private static PiecewiseExponentialTablePanel tableWith(int stretches) throws Exception {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable death = new Variable("Death", "no", "yes");
        death.setVariableType(VariableType.EVENT);
        Node node = probNet.addNode(death, NodeType.EVENT);
        PiecewiseExponentialPotential potential =
                new PiecewiseExponentialPotential(List.of(death), PotentialRole.CONDITIONAL_PROBABILITY);
        TreeMap<Double, Double> table = new TreeMap<>();
        for (int stretch = 0; stretch < stretches; stretch++) {
            table.put(5.0 * stretch, 0.1);
        }
        potential.setPiecewiseTable(table);
        node.setPotentials(List.of(potential));
        AtomicReference<PiecewiseExponentialTablePanel> built = new AtomicReference<>();
        new PiecewiseExponentialPanel(node) {
            @Override protected PiecewiseExponentialTablePanel createTablePanel(Object[][] data) {
                built.set(super.createTablePanel(data));
                return built.get();
            }
        };
        return built.get();
    }

    @ParameterizedTest @ValueSource(ints = {1, 2, 3}) void deleteIsDisabledWithNoRowSelected(int stretches)
            throws Exception {
        assertFalse(tableWith(stretches).removeValueButton.isEnabled());
    }

    @ParameterizedTest @ValueSource(ints = {1, 2, 3}) void selectingARowEnablesDeleteIfAnotherRemains(int stretches)
            throws Exception {
        PiecewiseExponentialTablePanel table = tableWith(stretches);
        table.getValuesTable().onScrollableTable(scrollable -> scrollable.setRowSelectionInterval(0, 0));
        assertEquals(stretches > 1, table.removeValueButton.isEnabled());
    }
}
