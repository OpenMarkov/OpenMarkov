/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference.decisiontree;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.decisiontree.DecisionTreeBranch;
import org.openmarkov.core.model.decisiontree.DecisionTreeNode;
import org.openmarkov.core.model.decisiontree.DecisionTreeVisualNode;
import org.openmarkov.core.model.decisiontree.ProbAndCEP;
import org.openmarkov.core.model.decisiontree.ProbAndUtilityAndStrategy;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.decisiontree.operation.DecisionTreeManagerImpl;
import org.openmarkov.integrationTests.IntegrationTest;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The window of the decision tree builds it with the new algorithm. Expanding a tree by some levels gives
 * the tree built that much deeper, and the subnetwork of each node is named after the findings of its path.
 *
 * @author Manuel Arias
 */
class TheDecisionTreeWindowExpandsWithTheNewAlgorithmTest {

    private static ProbNet load(String network) throws Exception {
        return new PGMXReader().read(
                IntegrationTest.class.getResource("/networks/decision-tree-examples/" + network)).probNet();
    }

    @ParameterizedTest(name = "{0}: profundidad {1} y {2} niveles más")
    @CsvSource({
            "03.4-ID-decide-test.pgmx, 2, 1",
            "03.4-ID-decide-test.pgmx, 0, 3",
            "03.5-DAN-decide-two-tests.pgmx, 1, 2",
            "04.5-ID-CEA-one-test.pgmx, 2, 2",
            "04.7-DAN-CEA-two-tests.pgmx, 1, 1",
    })
    void expandingSomeLevelsGivesTheTreeBuiltThatMuchDeeper(String network, int depth, int levels)
            throws Exception {
        ProbNet net = load(network);
        DecisionTreeManagerImpl manager = new DecisionTreeManagerImpl();

        DecisionTreeVisualNode expanded = manager.buildDecisionTree(net, depth);
        manager.expandLevels(expanded, levels);

        assertThat(text(expanded)).isEqualTo(text(manager.buildDecisionTree(net, depth + levels)));
    }

    @Test
    void expandingEverythingGivesTheWholeTree() throws Exception {
        ProbNet net = load("03.4-ID-decide-test.pgmx");
        DecisionTreeManagerImpl manager = new DecisionTreeManagerImpl();

        DecisionTreeVisualNode expanded = manager.buildDecisionTree(net, 0);
        manager.expandLevels(expanded, Integer.MAX_VALUE);

        assertThat(text(expanded)).isEqualTo(text(manager.buildDecisionTree(net, Integer.MAX_VALUE)));
    }

    /** Below a decision the finding is not repeated in the name, as the old algorithm did. */
    @Test
    void theSubnetworkOfANodeIsNamedAfterTheFindingsOfItsPath() throws Exception {
        DecisionTreeVisualNode root = new DecisionTreeManagerImpl().buildDecisionTree(load("03.4-ID-decide-test.pgmx"), 6);
        DecisionTreeNode therapy = child(child(child((DecisionTreeNode) root.getChildren().getFirst(), "yes"),
                                               "negative"), "no");

        assertThat(therapy.getNetwork().getName()).isEqualTo("03.4-ID-decide-test-Result_of_test_(Y)=negative");
        assertThat(child(therapy, "present").getNetwork().getName())
                .isEqualTo("03.4-ID-decide-test-Result_of_test_(Y)=negative-Disease_(X)=present");
    }

    private static DecisionTreeNode child(DecisionTreeNode node, String state) {
        return node.getChildBranches().stream().filter(branch -> branch.getBranchState().getName().equals(state))
                   .findFirst().orElseThrow().getChild();
    }

    /** The tree as text: each node with its variable, probability, evaluation and network, each branch with its state. */
    private static String text(DecisionTreeVisualNode element) {
        StringBuilder text = new StringBuilder();
        write(element, "", text);
        return text.toString();
    }

    private static void write(DecisionTreeVisualNode element, String indent, StringBuilder text) {
        if (element instanceof DecisionTreeNode node) {
            text.append(indent).append(node.getVariable().getName()).append(' ').append(node.getNodeType())
                .append(String.format(Locale.ROOT, " p=%.10f ", node.getScenarioProbability()))
                .append(evaluation(node)).append(" net=").append(node.getNetwork().getName()).append('\n');
        } else if (element instanceof DecisionTreeBranch branch && branch.getBranchState() != null) {
            text.append(indent).append("-- ").append(branch.getBranchState().getName())
                .append(String.format(Locale.ROOT, " %.10f", branch.getBranchProbability())).append('\n');
        }
        for (DecisionTreeVisualNode child : element.getChildren()) {
            write(child, indent + "  ", text);
        }
    }

    /** The evaluation of a scenario that cannot happen means nothing, and depends on how the node was built. */
    private static String evaluation(DecisionTreeNode node) {
        if (node.isImpossible()) {
            return "impossible";
        }
        return switch (node.getEvaluation()) {
            case ProbAndUtilityAndStrategy unicriterion -> String.format(Locale.ROOT, "U=%.10f", unicriterion.getUtility());
            case ProbAndCEP ce -> cep(ce.getCep());
            case null -> "-";
        };
    }

    private static String cep(CEP cep) {
        if (cep.isZero()) {
            return "CEP=0";
        }
        StringBuilder text = new StringBuilder("CEP");
        for (int i = 0; i < cep.getCosts().length; i++) {
            text.append(String.format(Locale.ROOT, " [%.6f %.6f]", cep.getCosts()[i], cep.getEffectivities()[i]));
        }
        return text.toString();
    }
}
