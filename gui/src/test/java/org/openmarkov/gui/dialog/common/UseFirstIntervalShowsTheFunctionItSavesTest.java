/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The life table of an event node, with stretches from time 2 and from time 5, and 3 as the function of the time of
 * entry. «Use first interval» shows the time of the first stretch, unchecking it puts back the 3, and what is saved
 * is what is shown (error 238).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class UseFirstIntervalShowsTheFunctionItSavesTest {

    private Node node;
    private PiecewiseExponentialPanel panel;

    @BeforeEach void anEventWithTwoStretches() throws Exception {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Variable death = new Variable("Death", "no", "yes");
        death.setVariableType(VariableType.EVENT);
        node = probNet.addNode(death, NodeType.EVENT);
        PiecewiseExponentialPotential potential =
                new PiecewiseExponentialPotential(List.of(death), PotentialRole.CONDITIONAL_PROBABILITY);
        potential.setPiecewiseTable(new TreeMap<>(Map.of(2.0, 0.1, 5.0, 0.2)));
        potential.setInitTimeFunction(new FunctionPotential(List.of(death), PotentialRole.CONDITIONAL_PROBABILITY,
                                                            new VariableExpression(List.of(), "3")));
        node.setPotentials(List.of(potential));
        panel = new PiecewiseExponentialPanel(node);
    }

    private String shown() {
        return panel.functionPanel.getFunctionTextArea().getText();
    }

    private String saved() throws Exception {
        panel.saveChanges();
        return ((PiecewiseExponentialPotential) node.getPotentials().getFirst()).getInitTimeFunction().getFunction()
                                                                                .asStringExpression();
    }

    @Test void checkingTheBoxShowsTheTimeOfTheFirstStretchAndSavesIt() throws Exception {
        panel.initValueCheck.doClick();

        assertEquals("2.0", shown());
        assertEquals(shown(), saved());
    }

    @Test void uncheckingTheBoxPutsBackTheFunctionThereWas() throws Exception {
        assertEquals("3", shown());

        panel.initValueCheck.doClick();
        panel.initValueCheck.doClick();

        assertEquals("3", shown());
        assertEquals(shown(), saved());
    }
}
