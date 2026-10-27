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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecisionTreeEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecompositionIntoSymmetricDANsEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A finding whose probability depends on a decision, such as «Wedding» in DAN-king, which copies the decision
 * «Marriage». Eliminating the decision, the evaluation took the probability of the finding under the first option for
 * that of all of them (error 290 of the pending list): the decision tree gave utility 0 when the first option rules
 * the finding out, and the optimal strategy changed with findings that are possible under every option. Now the
 * options that rule the finding out are left out, the best of the others is chosen and its probability is kept;
 * where the variable of the finding does not exist, the finding does not count (error 224).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class ADecisionAnalysisNetworkWeighsAFindingByTheOptionChosenTest {

    private static ProbNet read(String name) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/dan/" + name + ".pgmx"))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static EvidenceCase knowing(ProbNet probNet, String variableName, String state) throws Exception {
        EvidenceCase known = new EvidenceCase();
        Variable variable = probNet.getVariable(variableName);
        known.addFinding(new Finding(variable, variable.getStateIndex(state)));
        return known;
    }

    private static DANEvaluation tree(String name, String variableName, String state) throws Exception {
        ProbNet probNet = read(name);
        return new DANDecisionTreeEvaluation(probNet, knowing(probNet, variableName, state));
    }

    /** «Marriage = no» rules the wedding out; the tree took that option's probability, 0, and gave utility 0. */
    @Test void aFindingTheFirstOptionRulesOutKeepsTheUtility() throws Exception {
        DANEvaluation wedding = tree("DAN-king", "Wedding", "yes");
        assertEquals(7.73, wedding.getUtility().getValues()[0], 1E-9);
        assertEquals(1, wedding.getProbability().getValues()[0], 1E-9);

        DANEvaluation offspring = tree("DAN-king", "Offspring", "yes");
        assertEquals(8.85, offspring.getUtility().getValues()[0], 1E-9);
        assertEquals(0.6, offspring.getProbability().getValues()[0], 1E-9);
    }

    /** The probability is that of the task chosen, «kill unicorn», not that of the first one, «kill dragon». */
    @Test void theProbabilityIsThatOfTheOptionChosen() throws Exception {
        ProbNet probNet = read("DAN-king");
        DANEvaluation evaluation =
                new DANDecompositionIntoSymmetricDANsEvaluation(probNet, knowing(probNet, "Result 1", "fail"));
        assertEquals(7.45, evaluation.getUtility().getValues()[0], 1E-9);
        assertEquals(0.5, evaluation.getProbability().getValues()[0], 1E-9);
    }

    /**
     * With any finding on a chance variable, the optimal strategy and the decision tree give the same. They did not
     * in DAN-unordered-two-decs; DAN-king is left out because its tree takes seconds for each finding.
     */
    @ParameterizedTest
    @ValueSource(strings = {"DAN-unordered-two-decs", "DAN-dating", "DAN-diabetes", "DAN-decide-test-with-symptom"})
    void theOptimalStrategyIsTheOneOfTheDecisionTree(String name) throws Exception {
        for (Node node : read(name).getNodes()) {
            Variable variable = node.getVariable();
            if (node.getNodeType() != NodeType.CHANCE || variable.getVariableType() != VariableType.FINITE_STATES) {
                continue;
            }
            for (var state : variable.getStates()) {
                ProbNet forTheStrategy = read(name);
                DANEvaluation strategy = new DANDecompositionIntoSymmetricDANsEvaluation(
                        forTheStrategy, knowing(forTheStrategy, variable.getName(), state.getName()));
                DANEvaluation tree = tree(name, variable.getName(), state.getName());

                String finding = name + ", " + variable.getName() + " = " + state.getName();
                assertEquals(tree.getUtility().getValues()[0], strategy.getUtility().getValues()[0], 1E-9, finding);
                assertEquals(tree.getProbability().getValues()[0], strategy.getProbability().getValues()[0], 1E-9,
                             finding);
            }
        }
    }
}
