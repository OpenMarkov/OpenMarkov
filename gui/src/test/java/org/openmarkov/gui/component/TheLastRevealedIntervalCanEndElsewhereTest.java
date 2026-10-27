/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.component;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.gui.configuration.UserPreference;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Writing the upper limit of the last interval of the revelation conditions of a link ended in an unexpected error:
 * the check of the next interval read a row past the end of the table (error 163 of the pending list).
 *
 * @author Manuel Arias
 */
class TheLastRevealedIntervalCanEndElsewhereTest {

    private static final String[] COLUMNS = { "Id", "Name", "(", "Low", ",", "High", ")" };

    @ParameterizedTest @ValueSource(ints = { 1, 2 })
    void theUpperLimitOfTheLastIntervalChanges(int intervals) {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            ProbNet probNet = new ProbNet(DecisionAnalysisNetworkType.getUniqueInstance());
            Node age = probNet.addNode(new Variable("Age"), NodeType.CHANCE);
            Node test = probNet.addNode(new Variable("Test", "negative", "positive"), NodeType.CHANCE);
            probNet.addLink(age, test, true);
            Link<Node> link = probNet.getLink(age, test, true);
            link.addRevealingInterval(new PartitionedInterval(new double[]{ 0, 10 }, new boolean[]{ false, true }));
            if (intervals == 2) {
                link.addRevealingInterval(new PartitionedInterval(new double[]{ 20, 30 }, new boolean[]{ false, true }));
            }
            RevelationArcDiscretizeTablePanel panel = new RevelationArcDiscretizeTablePanel(COLUMNS, link);
            panel.setPartitionedInterval();

            panel.getTableModel().setValueAt(25.0 * intervals / 2 + 2.5, intervals - 1,
                                             DiscretizeTablePanel.UPPER_BOUND_VALUE_COLUMN_INDEX);

            assertEquals(25.0 * intervals / 2 + 2.5, probNet.getLink(age, test, true).getRevealingIntervals().getLast().getMax());
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
