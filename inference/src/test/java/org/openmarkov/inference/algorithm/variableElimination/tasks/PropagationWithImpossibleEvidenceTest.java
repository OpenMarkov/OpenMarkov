/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination.tasks;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A Bayesian network in which «Either» is present exactly when «Tuberculosis» or «Cancer» is, and the findings
 * are «Tuberculosis = present» and «Either = absent».
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class PropagationWithImpossibleEvidenceTest {

    @Test void itIsReportedAsFindingsThatAreImpossible() throws Exception {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable tuberculosis = new Variable("Tuberculosis", "absent", "present");
        Variable cancer = new Variable("Cancer", "absent", "present");
        Variable either = new Variable("Either", "absent", "present");
        Node tuberculosisNode = probNet.addNode(tuberculosis, NodeType.CHANCE);
        Node cancerNode = probNet.addNode(cancer, NodeType.CHANCE);
        Node eitherNode = probNet.addNode(either, NodeType.CHANCE);
        probNet.addLink(tuberculosisNode, eitherNode, true);
        probNet.addLink(cancerNode, eitherNode, true);
        tuberculosisNode.setPotential(new TablePotential(List.of(tuberculosis),
                                                         PotentialRole.CONDITIONAL_PROBABILITY,
                                                         new double[]{ 0.99, 0.01 }));
        cancerNode.setPotential(new TablePotential(List.of(cancer), PotentialRole.CONDITIONAL_PROBABILITY,
                                                   new double[]{ 0.9, 0.1 }));
        eitherNode.setPotential(new TablePotential(List.of(either, tuberculosis, cancer),
                                                   PotentialRole.CONDITIONAL_PROBABILITY,
                                                   new double[]{ 1, 0, 0, 1, 0, 1, 0, 1 }));
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(tuberculosis, tuberculosis.getStateIndex("present")));
        evidence.addFinding(new Finding(either, either.getStateIndex("absent")));
        VEPropagation propagation = new VEPropagation(probNet);
        propagation.setVariablesOfInterest(probNet.getVariables());
        propagation.setPostResolutionEvidence(evidence);

        IncompatibleEvidenceException.EvidenceIsImpossible exception =
                assertThrows(IncompatibleEvidenceException.EvidenceIsImpossible.class, propagation::getPosteriorValues);

        assertEquals("These findings are impossible in this network: together they have probability zero.",
                     exception.getExceptionMessage());
    }
}
