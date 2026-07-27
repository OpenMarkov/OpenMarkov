/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.potential;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A potential carries a map of properties that the format readers fill in. It was not copied by the
 * copy constructor of {@link Potential}, so six methods scattered over five subclasses put it back
 * by hand afterwards, always by sharing the map; every other way of copying a potential lost it.
 *
 * @author Manuel Arias
 */
public class PotentialPropertiesSurviveCopyingTest {

    private static final Variable X = new Variable("X", new State[]{new State("a"), new State("b")});
    private static final Variable Y = new Variable("Y", new State[]{new State("a"), new State("b")});

    private static TablePotential potentialWithAProperty() {
        TablePotential potential = new TablePotential(List.of(X, Y), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[]{0.1, 0.9, 0.4, 0.6});
        potential.properties.put("nameOfRelation", "the one the file gave it");
        return potential;
    }

    @Test
    public void copyKeepsTheProperties() {
        Potential copy = potentialWithAProperty().copy();

        assertEquals("the one the file gave it", copy.properties.get("nameOfRelation"));
    }

    @Test
    public void deepCopyKeepsTheProperties() {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        net.addNode(new Variable(X), NodeType.CHANCE);
        net.addNode(new Variable(Y), NodeType.CHANCE);

        Potential copy = potentialWithAProperty().deepCopy(net);

        assertEquals("the one the file gave it", copy.properties.get("nameOfRelation"));
    }

    @Test
    public void theCopyGetsItsOwnMapOfProperties() {
        TablePotential original = potentialWithAProperty();

        Potential copy = original.copy();
        copy.properties.put("added", "by the copy");

        assertFalse(original.properties.containsKey("added"),
                "writing a property on the copy must not write it on the original");
    }

    @Test
    public void reorderKeepsTheProperties() {
        // This one already worked, because reorder put the properties back by hand. It stays as a
        // check that removing those lines did not take the properties away with them.
        TablePotential reordered = potentialWithAProperty()
                .reorder(X, new State[]{X.getStates()[1], X.getStates()[0]});

        assertEquals("the one the file gave it", reordered.properties.get("nameOfRelation"));
    }
}
