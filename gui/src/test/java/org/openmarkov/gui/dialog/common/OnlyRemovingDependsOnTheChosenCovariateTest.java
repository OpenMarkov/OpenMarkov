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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.ExponentialHazardPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.WeibullHazardPotential;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * In the panel of a hazard, choosing the row of a mandatory covariate, such as «Gamma» in a Weibull one, turns off
 * removing it; it used to turn off adding a covariate as well, which has nothing to do with the row (what remained of
 * error 157 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class OnlyRemovingDependsOnTheChosenCovariateTest {

    private Node node;
    private Variable risk;
    private Variable age;

    @BeforeEach void aHazardOnTheAge() {
        ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());
        age = new Variable("Age [1]", true, 0.0, Double.POSITIVE_INFINITY, false, 1.0);
        age.setTimeSlice(1);
        risk = new Variable("Risk [1]", "no", "yes");
        risk.setTimeSlice(1);
        Node ageNode = probNet.addNode(age, NodeType.CHANCE);
        ageNode.setPotential(new DeltaPotential(List.of(age), PotentialRole.CONDITIONAL_PROBABILITY, 65));
        node = probNet.addNode(risk, NodeType.CHANCE);
        probNet.addLink(ageNode, node, true);
    }

    private static GLMPanel covariates(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof GLMPanel panel) {
                return panel;
            }
            if (component instanceof Container inner && covariates(inner) != null) {
                return covariates(inner);
            }
        }
        return null;
    }

    /** «add enabled, remove enabled» after choosing the row of {@code covariate} */
    private static String buttonsFor(GLMPanel panel, String covariate) {
        int row = 0;
        while (!panel.getCovariates()[row].asStringExpression().equals(covariate)) {
            row++;
        }
        panel.valuesTable.setRowSelectionInterval(row, row);
        return panel.getAddValueButton().isEnabled() + " " + panel.getRemoveValueButton().isEnabled();
    }

    private static GLMPanel covariatesOf(JPanel potentialPanel) {
        GLMPanel panel = covariates(potentialPanel);
        assertNotNull(panel);
        return panel;
    }

    @Test void inAWeibullHazardGammaCannotBeRemovedButACovariateCanBeAdded() {
        node.setPotential(new WeibullHazardPotential(List.of(risk, age), PotentialRole.CONDITIONAL_PROBABILITY));
        GLMPanel panel = covariatesOf(new WeibullPotentialPanel(node));

        assertEquals("true false", buttonsFor(panel, "Gamma"));
    }

    @Test void inAnExponentialHazardOnlyConstantCannotBeRemoved() throws Exception {
        node.setPotential(new ExponentialHazardPotential(List.of(risk, age), PotentialRole.CONDITIONAL_PROBABILITY));
        GLMPanel panel = covariatesOf(new GLMPotentialPanel(node));

        assertEquals("true false", buttonsFor(panel, "Constant"));
        for (var covariate : panel.getCovariates()) {
            if (!covariate.asStringExpression().equals("Constant")) {
                assertEquals("true true", buttonsFor(panel, covariate.asStringExpression()), covariate.asStringExpression());
            }
        }
    }
}
