/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Both test networks are a chain of three nodes — A -> B -> C in one, A -- B -- C in the other — so
 * adding the third link, between A and C, closes a loop whichever way round it is written.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NoLoopsTest {

    private ProbNet directedNet;
    private ProbNet undirectedNet;
    private NoLoops noLoops;

    @BeforeEach public void setUp() {
        directedNet = ConstraintsTests.getTestProbNetDirected();
        undirectedNet = ConstraintsTests.getTestProbNetUndirected();
        noLoops = new NoLoops();
        directedNet.addConstraint(noLoops);
        undirectedNet.addConstraint(noLoops);
    }

    @Tag(TestSpeed.SLOW)
    @Test public void aChainOfNodesHasNoLoops() {
        assertTrue(noLoops.isMetBy(directedNet));
        assertTrue(noLoops.isMetBy(undirectedNet));
    }

    @Tag(TestSpeed.SLOW)
    @Test public void closingTheChainMakesALoop() {
        directedNet.addLink(directedNet.getVariable("A"), directedNet.getVariable("C"), true);

        assertFalse(noLoops.isMetBy(directedNet));
    }

    /**
     * The same, on the undirected network. This is the assertion the previous version of this test
     * meant to make: it built this scenario and then asserted on {@code directedNet}, three times, so
     * the undirected half was never checked at all.
     */
    @Tag(TestSpeed.SLOW)
    @Test public void closingTheChainMakesALoopWithUndirectedLinksToo() {
        undirectedNet.addLink(undirectedNet.getVariable("A"), undirectedNet.getVariable("C"), false);

        assertFalse(noLoops.isMetBy(undirectedNet));
    }

    /** A loop does not need every link to point the same way: orientation is not what is being looked at. */
    @Tag(TestSpeed.SLOW)
    @Test public void aLoopIsALoopWhicheverWayItsLinksPoint() {
        undirectedNet.addLink(undirectedNet.getVariable("C"), undirectedNet.getVariable("A"), true);

        assertFalse(noLoops.isMetBy(undirectedNet));
    }

    /**
     * The point of the whole exercise: checking a constraint must not change the network. This used to
     * remove each link and add it back the other way round, so every arc came out reversed — and since
     * the check runs during ordinary validation, validating a network corrupted it in silence.
     */
    @Tag(TestSpeed.SLOW)
    @Test public void checkingTheConstraintLeavesTheNetworkAsItWas() {
        List<String> before = NoLoopsTest.linksOf(directedNet);

        noLoops.isMetBy(directedNet);

        assertEquals(before, NoLoopsTest.linksOf(directedNet));
        assertEquals(List.of("A -> B", "B -> C"), before);   // and they were pointing the right way
    }

    /** The same on a network that does have a loop: reporting it must not alter it either. */
    @Tag(TestSpeed.SLOW)
    @Test public void checkingANetworkWithALoopLeavesItAsItWas() {
        directedNet.addLink(directedNet.getVariable("A"), directedNet.getVariable("C"), true);
        List<String> before = NoLoopsTest.linksOf(directedNet);

        assertFalse(noLoops.isMetBy(directedNet));

        assertEquals(before, NoLoopsTest.linksOf(directedNet));
    }

    /**
     * The shortest loop there is: two nodes joined both ways. It went unnoticed because the search
     * underneath was asked to ignore the link being judged and ignored the pair of nodes instead,
     * so the way back through the second link was hidden along with it. A diamond was reported
     * correctly all the same, which is why this went unseen for so long: the two links that close
     * it join different pairs of nodes.
     */
    @Tag(TestSpeed.SLOW)
    @Test public void twoNodesJoinedBothWaysAreALoop() {
        directedNet.addLink(directedNet.getVariable("B"), directedNet.getVariable("A"), true);

        assertFalse(noLoops.isMetBy(directedNet));
    }

    /**
     * A node linked to itself is a closed path, so it counts as a loop — as it did before. Whether a
     * network may have one at all is the business of another constraint, {@code NoSelfLoop}.
     */
    @Tag(TestSpeed.SLOW)
    @Test public void aSelfLoopCountsAsALoop() {
        Variable varA = undirectedNet.getVariable("A");
        undirectedNet.addLink(varA, varA, false);

        assertFalse(noLoops.isMetBy(undirectedNet));
    }

    /** The links of a network, written down so that a change of orientation shows up. */
    private static List<String> linksOf(ProbNet probNet) {
        return probNet.getLinks()
                      .stream()
                      .map(NoLoopsTest::asText)
                      .sorted()
                      .toList();
    }

    private static String asText(Link<Node> link) {
        String arrow = link.isDirected() ? " -> " : " -- ";
        return link.getFrom().getName() + arrow + link.getTo().getName();
    }

}
