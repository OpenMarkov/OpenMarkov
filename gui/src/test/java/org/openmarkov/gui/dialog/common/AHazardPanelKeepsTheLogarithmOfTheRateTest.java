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

import javax.swing.JCheckBox;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A node whose hazard depends on the age. A Weibull hazard opens in its own panel, which keeps the time variable;
 * an exponential one opens in the regression panel. In both, a check box says whether the coefficients give the
 * logarithm of the rate, and OK keeps what it says.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AHazardPanelKeepsTheLogarithmOfTheRateTest {

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

    private WeibullHazardPotential hazard() {
        return (WeibullHazardPotential) node.getPotentials().get(0);
    }

    private static JCheckBox logCheckBox(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JCheckBox checkBox && WeibullPotentialPanel.LOG_TEXT.equals(checkBox.getText())) {
                return checkBox;
            }
            if (component instanceof Container inner) {
                JCheckBox found = logCheckBox(inner);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    @Test void aWeibullHazardOpensItsOwnPanel() {
        PotentialPanelManager manager = PotentialPanelManager.getInstance();

        assertAll(() -> assertEquals(WeibullPotentialPanel.class,
                                     manager.getPotentialPanelClassOf(WeibullHazardPotential.class)),
                  () -> assertEquals(GLMPotentialPanel.class,
                                     manager.getPotentialPanelClassOf(ExponentialHazardPotential.class)));
    }

    @Test void aNewHazardTakesTheCoefficientsAsTheLogarithm() {
        node.setPotential(new WeibullHazardPotential(List.of(risk, age), PotentialRole.CONDITIONAL_PROBABILITY));

        assertTrue(logCheckBox(new WeibullPotentialPanel(node)).isSelected());
    }

    @Test void theWeibullPanelKeepsWhatTheCheckBoxSaysAndTheTimeVariable() throws Exception {
        WeibullHazardPotential hazard = new WeibullHazardPotential(List.of(risk, age),
                                                                   PotentialRole.CONDITIONAL_PROBABILITY);
        hazard.setLog(false);
        hazard.setTimeVariable(age);
        node.setPotential(hazard);
        WeibullPotentialPanel panel = new WeibullPotentialPanel(node);
        assertFalse(logCheckBox(panel).isSelected());

        logCheckBox(panel).setSelected(true);
        panel.saveChanges();

        assertAll(() -> assertTrue(hazard().isLog()),
                  () -> assertEquals(age, hazard().getTimeVariable()));
    }

    @Test void theRegressionPanelKeepsWhatTheCheckBoxSaysForAnExponentialHazard() throws Exception {
        node.setPotential(new ExponentialHazardPotential(List.of(risk, age), PotentialRole.CONDITIONAL_PROBABILITY));
        GLMPotentialPanel panel = new GLMPotentialPanel(node);
        assertTrue(logCheckBox(panel).isVisible() && logCheckBox(panel).isSelected());

        logCheckBox(panel).setSelected(false);
        panel.saveChanges();

        assertAll(() -> assertInstanceOf(ExponentialHazardPotential.class, hazard()),
                  () -> assertFalse(hazard().isLog()));
    }
}
