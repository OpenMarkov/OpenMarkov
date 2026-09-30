/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A node refuses a null potential and keeps the potentials it had.
 *
 * @author Manuel Arias
 */
class ANodeRefusesANullPotentialTest {

    private Node node;
    private Potential itsPotential;

    @BeforeEach
    void aNodeWithOnePotential() {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        node = probNet.addNode(new Variable("A", "a0", "a1"), NodeType.CHANCE);
        itsPotential = PotentialUtils.generateDefaultPotential(node);
        node.setPotential(itsPotential);
    }

    @Tag(TestSpeed.FAST)
    @Test void whenItIsSet() {
        assertThrows(InvalidArgumentException.class, () -> node.setPotential(null));
        assertEquals(List.of(itsPotential), node.getPotentials());
    }

    @Tag(TestSpeed.FAST)
    @Test void whenItIsAdded() {
        assertThrows(InvalidArgumentException.class, () -> node.addPotential(null));
        assertEquals(List.of(itsPotential), node.getPotentials());
    }

    @Tag(TestSpeed.FAST)
    @Test void whenItComesInsideAList() {
        Potential another = PotentialUtils.generateDefaultPotential(node);
        assertThrows(InvalidArgumentException.class, () -> node.setPotentials(Arrays.asList(another, null)));
        assertEquals(List.of(itsPotential), node.getPotentials());
    }
}
