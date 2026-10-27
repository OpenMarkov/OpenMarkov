/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.gui.configuration.UserPreference;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tab «Parents» of the window of a node offers as new parents only the nodes that the rules of the network admit.
 * It offered every node, such as a descendant that would close a cycle, and choosing one ended in an error (error 162
 * of the pending list). The network: Visit → Tuberculosis → X-ray, and Smoker, apart.
 *
 * @author Manuel Arias
 */
class TheParentsTabOffersOnlyAdmittedParentsTest {

    @Test void theDescendantsAreNotOffered() {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
            Node visit = probNet.addNode(new Variable("Visit", "no", "yes"), NodeType.CHANCE);
            Node tuberculosis = probNet.addNode(new Variable("Tuberculosis", "no", "yes"), NodeType.CHANCE);
            Node xRay = probNet.addNode(new Variable("X-ray", "no", "yes"), NodeType.CHANCE);
            probNet.addNode(new Variable("Smoker", "no", "yes"), NodeType.CHANCE);
            probNet.addLink(visit, tuberculosis, true);
            probNet.addLink(tuberculosis, xRay, true);

            PrefixedDataTablePanel parents = new PrefixedDataTablePanel(visit, new String[]{ "", "Parents" },
                                                                        new Object[0][2], new Object[0][2], "Parents",
                                                                        true);

            assertEquals(List.of("Smoker"), parents.getOfferedNames());
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
