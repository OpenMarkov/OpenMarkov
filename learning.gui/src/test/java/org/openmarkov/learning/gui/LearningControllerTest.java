package org.openmarkov.learning.gui;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The learning window asks the model network how it discretizes each column of the case database.
 *
 * @author Manuel Arias
 */
class LearningControllerTest {

    @Test
    void aColumnThatTheModelNetworkDoesNotHaveIsNotDiscretizedInIt() {
        ProbNet modelNet = new ProbNet();
        modelNet.addNode(new Variable("A", "0", "1"), NodeType.CHANCE);

        assertThat(LearningController.isDiscretizedInModelNet(modelNet, new Variable("E", "0", "1"))).isFalse();
    }

    @Test
    void aColumnThatTheModelNetworkDiscretizesIsFound() {
        ProbNet modelNet = new ProbNet();
        modelNet.addNode(discretized("Age"), NodeType.CHANCE);
        modelNet.addNode(new Variable("A", "0", "1"), NodeType.CHANCE);

        assertThat(LearningController.isDiscretizedInModelNet(modelNet, new Variable("Age"))).isTrue();
        assertThat(LearningController.isDiscretizedInModelNet(modelNet, new Variable("A", "0", "1"))).isFalse();
    }

    private static Variable discretized(String name) {
        Variable variable = new Variable(name, "low", "high");
        variable.setVariableType(org.openmarkov.core.model.network.VariableType.DISCRETIZED);
        return variable;
    }
}
