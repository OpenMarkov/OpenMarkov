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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecisionTreeEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A finding on a chance variable that restricts others, such as «Accept» in DAN-dating. What is known beforehand is
 * the evidence that «Show optimal strategy» gives to the evaluation.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ADecisionAnalysisNetworkCountsWhatIsKnownOfAVariableThatRestrictsOthersTest {

    private static ProbNet read(String name) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/dan/" + name + ".pgmx"))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static void know(EvidenceCase known, ProbNet probNet, String variableName, String state)
            throws Exception {
        Variable variable = probNet.getVariable(variableName);
        known.addFinding(new Finding(variable, variable.getStateIndex(state)));
    }

    private static double utility(DANDecompositionIntoSymmetricDANsEvaluation evaluation) {
        return evaluation.getUtility().getValues()[0];
    }

    /** Knowing that she was asked, each answer weighted by its probability adds up to the utility of asking. */
    @Test void theAnswersThatAreKnownAddUpToTheUtilityOfAsking() throws Exception {
        ProbNet probNet = read("DAN-dating");
        EvidenceCase asked = new EvidenceCase();
        know(asked, probNet, "Ask?", "yes");
        double asking = utility(new DANDecompositionIntoSymmetricDANsEvaluation(probNet, asked));

        double weighted = 0;
        for (String answer : new String[] {"no", "yes"}) {
            probNet = read("DAN-dating");
            EvidenceCase known = new EvidenceCase();
            know(known, probNet, "Ask?", "yes");
            know(known, probNet, "Accept", answer);
            DANDecompositionIntoSymmetricDANsEvaluation evaluation =
                    new DANDecompositionIntoSymmetricDANsEvaluation(probNet, known);
            weighted += evaluation.getProbability().getValues()[0] * utility(evaluation);
        }
        assertEquals(asking, weighted, 1E-9);
    }

    /** With the same finding, the optimal strategy and the decision tree give the same utility. */
    @ParameterizedTest
    @ValueSource(strings = {"DAN-dating", "DAN-reactor", "DAN-decide-test-with-restrictive-symptom"})
    void theUtilityIsTheOneOfTheDecisionTree(String name) throws Exception {
        Set<String> restricting = new TreeSet<>();
        for (Link<Node> link : read(name).getLinks()) {
            if (link.hasRestrictions() && link.getFrom().getNodeType() == NodeType.CHANCE) {
                restricting.add(link.getFrom().getName());
            }
        }
        for (String variableName : restricting) {
            for (var state : read(name).getVariable(variableName).getStates()) {
                ProbNet forTheStrategy = read(name);
                EvidenceCase known = new EvidenceCase();
                know(known, forTheStrategy, variableName, state.getName());
                ProbNet forTheTree = read(name);
                EvidenceCase sameFinding = new EvidenceCase();
                know(sameFinding, forTheTree, variableName, state.getName());

                assertEquals(new DANDecisionTreeEvaluation(forTheTree, sameFinding).getUtility().getValues()[0],
                             utility(new DANDecompositionIntoSymmetricDANsEvaluation(forTheStrategy, known)), 1E-9,
                             name + ", " + variableName + " = " + state.getName());
            }
        }
    }
}
