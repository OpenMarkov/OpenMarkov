/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * The decisions are ordered by peeling the network from the outside: whatever has no
 * children comes off, over and over. A cycle leaves every remaining node with a child, so
 * nothing comes off and the walk used to go round for ever. Networks of discrete-event
 * simulation are the ones that allow a cycle: every other type forbids it.
 *
 * <p>The timeout is what tells a hang from a failure. Without it a return of the fault
 * would stop the whole suite instead of failing this test.
 *
 * @author Manuel Arias
 */
class ANetworkWithACycleDoesNotHangTheDecisionsTest {

    @Tag(TestSpeed.FAST)
    @Test void aCycleIsAnsweredAndNotWalkedForEver() {
        ProbNet net = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable first = new Variable("A", "no", "yes");
        Variable second = new Variable("B", "no", "yes");
        Variable decision = new Variable("D", "no", "yes");
        net.addNode(first, NodeType.CHANCE);
        net.addNode(second, NodeType.CHANCE);
        net.addNode(decision, NodeType.DECISION);
        // The decision points into the cycle, so it never becomes a leaf.
        net.addLink(decision, first, true);
        net.addLink(first, second, true);
        net.addLink(second, first, true);

        assertTimeoutPreemptively(Duration.ofSeconds(10), () ->
                assertThrows(InvalidArgumentException.class,
                             () -> BasicOperations.getSequenceOfDecisions(net)));
    }
}
