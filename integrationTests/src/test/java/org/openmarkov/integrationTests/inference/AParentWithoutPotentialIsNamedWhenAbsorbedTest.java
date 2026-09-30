/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.core.AddNodeEdit;
import org.openmarkov.core.exception.ThereIsNoPotentialInNodeException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Evaluating a network where a product has a parent without potential fails naming that parent.
 *
 * @author Manuel Arias
 */
public class AParentWithoutPotentialIsNamedWhenAbsorbedTest {

    /** In ID-mediastinet.pgmx, "Net_QALE" is the product of three utilities. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void whenTheNetworkIsEvaluated() throws Exception {
        URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith("/id/ID-mediastinet.pgmx"))
                          .findFirst().orElseThrow();
        ProbNet net = new PGMXReader().read(url).probNet();
        Variable netQALE = net.getVariable("Net_QALE");
        Variable extra = new Variable("Extra");
        extra.setDecisionCriterion(netQALE.getDecisionCriterion());
        new AddNodeEdit(net, extra, NodeType.UTILITY, null).executeEdit();
        new AddLinkEdit(net, extra, netQALE, true).executeEdit();

        UnrecoverableException thrown =
                assertThrows(UnrecoverableException.class, () -> new VEEvaluation(net).getUtility());

        ThereIsNoPotentialInNodeException cause =
                assertInstanceOf(ThereIsNoPotentialInNodeException.class, thrown.getCause());
        assertEquals("Extra", cause.nodeName);
    }
}
