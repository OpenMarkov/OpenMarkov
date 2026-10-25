/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.exception;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * In an influence diagram with a link from a chance node to a utility, a link from the utility to the chance node
 * breaks two constraints, NoUtilityParent and NoCycle; the window that refuses it is titled after both.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TwoConstraintsBrokenAtOnceAreTitledRightTest {

    @Test void theLinkFromAUtilityToItsParentIsRefusedWithTheTitleOfBoth() {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        Variable disease = probNet.addNode(new Variable("Disease", "absent", "present"), NodeType.CHANCE).getVariable();
        Variable cost = probNet.addNode(new Variable("Cost"), NodeType.UTILITY).getVariable();
        probNet.addLink(disease, cost, true);

        DoEditException refusal = assertThrows(DoEditException.class,
                                               () -> new AddLinkEdit(probNet, cost, disease, true).executeEdit());

        assertEquals("Multiple constraints violated", refusal.getExceptionTitle());
    }
}
