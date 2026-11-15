/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.action.base.linkEdits;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UncertainTablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Drawing a link to a node whose probability table has uncertainty gives a table in which each new cell repeats the
 * number and the distribution of the cell it comes from, with the same comment; undoing gives back the old table.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AddingAParentKeepsTheUncertaintyOfATableTest {

    private ProbNet net;
    private Variable x;
    private Variable p;
    private Node nodeX;

    @BeforeEach void setUp() {
        net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        x = new Variable("X", "no", "yes");
        p = new Variable("P", "absent", "present");
        nodeX = net.addNode(x, NodeType.CHANCE);
        net.addNode(p, NodeType.CHANCE);
        net.getPNESupport().setWithUndo(true);
    }

    private UncertainTablePotential tableWithUncertainty() {
        var table = new UncertainTablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 });
        table.setUncertainValues(new UncertainValue[] {
                new UncertainValue(new BetaFunction(3, 7)), new UncertainValue(new BetaFunction(7, 3)) });
        table.setComment("from the literature");
        return table;
    }

    @Test void theNewTableRepeatsTheDistributionOfEachCellAndKeepsTheComment() throws Exception {
        UncertainTablePotential old = tableWithUncertainty();
        nodeX.setPotential(old);

        new AddLinkEdit(net, p, x, true).executeEdit();

        var table = assertInstanceOf(UncertainTablePotential.class, nodeX.getPotential());
        assertEquals(List.of(x, p), table.getVariables());
        assertArrayEquals(new double[] { 0.3, 0.7, 0.3, 0.7 }, table.getValues());
        assertEquals("from the literature", table.getComment());
        UncertainValue[] uncertain = table.getUncertainValues();
        assertEquals(4, uncertain.length);
        for (int i = 0; i < 4; i++) {
            UncertainValue source = old.getUncertainValues()[i % 2];
            assertArrayEquals(source.getProbDensFunction().getParameters(), uncertain[i].getProbDensFunction().getParameters());
            assertNotSame(source, uncertain[i]);
        }
        assertNotSame(uncertain[0], uncertain[2]);
    }

    @Test void undoingGivesBackTheOldTable() throws Exception {
        UncertainTablePotential old = tableWithUncertainty();
        nodeX.setPotential(old);

        new AddLinkEdit(net, p, x, true).executeEdit();
        net.getPNESupport().undo();

        assertSame(old, nodeX.getPotential());
    }

    @Test void aTableWithoutUncertaintyKeepsItsComment() throws Exception {
        var table = new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[] { 0.3, 0.7 });
        table.setComment("from the literature");
        nodeX.setPotential(table);

        new AddLinkEdit(net, p, x, true).executeEdit();

        assertEquals("from the literature", nodeX.getPotential().getComment());
    }
}
