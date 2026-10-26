/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.constraint;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.DecisionAnalysisNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In the networks that variable elimination evaluates with utilities, a utility S whose parent is the utility U cannot
 * also take the chance variable C as a parent: the link is refused while editing, not when evaluating (error 39).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AUtilityCannotMixUtilityAndChanceParentsTest {

    static List<NetworkType> types() {
        return List.of(InfluenceDiagramType.getUniqueInstance(), MIDType.getUniqueInstance(),
                       DecisionAnalysisNetworkType.getUniqueInstance());
    }

    @ParameterizedTest @MethodSource("types")
    void theLinkFromAChanceVariableIsRefused(NetworkType type) {
        ProbNet net = new ProbNet(type);
        Variable c = new Variable("C", "absent", "present");
        Variable u = new Variable("U");
        Variable s = new Variable("S");
        net.addNode(c, NodeType.CHANCE);
        net.addNode(u, NodeType.UTILITY);
        net.addNode(s, NodeType.UTILITY);
        net.addLink(c, u, true);
        net.addLink(u, s, true);

        assertThrows(DoEditException.class, () -> new AddLinkEdit(net, c, s, true).executeEdit());
        assertFalse(net.isParent(net.getNode(c), net.getNode(s)));
        assertTrue(net.getConstraints().stream().anyMatch(NoMixedParents.class::isInstance));
    }
}
