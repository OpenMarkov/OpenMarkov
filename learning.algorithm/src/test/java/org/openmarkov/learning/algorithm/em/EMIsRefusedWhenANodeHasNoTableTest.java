/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.learning.algorithm.em;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.learning.algorithm.pc.PCAlgorithm;
import org.openmarkov.learning.core.LearningManager;
import org.openmarkov.learning.core.algorithm.LearningAlgorithmManager;
import org.openmarkov.learning.core.exception.NodesWithoutProbabilityException;
import org.openmarkov.learning.core.util.ModelNetUse;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * EM only learns the tables of the network it starts from, so learning with it is refused, naming the nodes, when
 * that network has nodes without a table; and cross-validation, which has no model network, does not offer it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class EMIsRefusedWhenANodeHasNoTableTest {

    private static final ModelNetUse ONLY_THE_POSITIONS = new ModelNetUse(true, true, false, false, false, false);
    private static final ModelNetUse START_FROM_IT = new ModelNetUse(true, true, true, false, false, false);

    /** Twelve cases of A and B, and of C if asked. */
    private static CaseDatabase database(boolean withC) {
        List<Variable> variables = withC
                ? List.of(new Variable("A", 2), new Variable("B", 2), new Variable("C", 2))
                : List.of(new Variable("A", 2), new Variable("B", 2));
        int[][] cases = new int[12][variables.size()];
        for (int i = 0; i < cases.length; i++) {
            for (int j = 0; j < variables.size(); j++) {
                cases[i][j] = (i / (j + 1)) % 2;
            }
        }
        return new CaseDatabase(variables, cases);
    }

    /** A model network A → B with a table in each node. */
    private static ProbNet model() {
        ProbNet probNet = new ProbNet();
        Variable a = new Variable("A", 2);
        Variable b = new Variable("B", 2);
        probNet.addNode(a, NodeType.CHANCE).setPotential(new TablePotential(List.of(a), PotentialRole.CONDITIONAL_PROBABILITY));
        probNet.addNode(b, NodeType.CHANCE).setPotential(new TablePotential(List.of(b, a), PotentialRole.CONDITIONAL_PROBABILITY));
        probNet.addLink(a, b, true);
        return probNet;
    }

    private static String refusedNodes(CaseDatabase database, ProbNet model, ModelNetUse use) {
        return assertThrows(NodesWithoutProbabilityException.class,
                () -> new LearningManager(database, EMAlgorithm.class, model, use)).nodeNames;
    }

    @Test void withoutAModelNetworkItIsRefused() {
        assertEquals("A, B", refusedNodes(database(false), null, null));
    }

    @Test void withAModelNetworkUsedOnlyForThePositionsItIsRefused() {
        assertEquals("A, B", refusedNodes(database(false), model(), ONLY_THE_POSITIONS));
    }

    @Test void aVariableOfTheDatabaseThatIsNotInTheModelNetworkIsNamed() {
        assertEquals("C", refusedNodes(database(true), model(), START_FROM_IT));
    }

    @Test void theMessageSaysWhatToDo() {
        NodesWithoutProbabilityException refusal = assertThrows(NodesWithoutProbabilityException.class,
                () -> new LearningManager(database(false), EMAlgorithm.class, null, null));

        assertThat(refusal.getExceptionMessage()).contains("Expectation maximization (EM)", "A, B",
                "«Start learning from model network»");
    }

    @Test void startingFromTheModelNetworkItLearns() throws Exception {
        CaseDatabase database = database(false);
        LearningManager manager = new LearningManager(database, EMAlgorithm.class, model(), START_FROM_IT);
        manager.init(new EMAlgorithm(manager.getLearnedNet(), database, 0.0));
        manager.learn();

        assertThat(manager.getLearnedNet().getNodes()).allSatisfy(node -> assertEquals(1, node.getNumPotentials()));
    }

    @Test void crossValidationDoesNotOfferIt() {
        assertThat(LearningAlgorithmManager.INSTANCE.getGenerativeAlgorithms().toList()).contains(EMAlgorithm.class);
        assertThat(LearningAlgorithmManager.INSTANCE.getGenerativeAlgorithmsThatLearnTheStructure().toList())
                .contains(PCAlgorithm.class).doesNotContain(EMAlgorithm.class);
    }
}
