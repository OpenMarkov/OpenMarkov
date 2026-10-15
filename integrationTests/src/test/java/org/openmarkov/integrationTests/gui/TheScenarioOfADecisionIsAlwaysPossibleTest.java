/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import networks.Networks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.inference.common.ScopeSelectorPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * In ID-CEA-test-2therapies-new-test, «Test» is «not done» if and only if «Dec: Test» is «no». The scenario of a
 * decision, in the cost-effectiveness analysis, offers only the states that are possible with the findings and with
 * the states chosen before.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
public class TheScenarioOfADecisionIsAlwaysPossibleTest {

    private ProbNet net;

    @BeforeEach void readTheNetwork() throws Exception {
        net = new PGMXReader().read(Networks.getNetworks()
                                            .filter(u -> u.getPath().endsWith("/id/ID-CEA-test-2therapies-new-test.pgmx"))
                                            .findFirst().orElseThrow()).probNet();
    }

    private ScopeSelectorPanel scenario(String decision, EvidenceCase evidence) {
        ScopeSelectorPanel panel = new ScopeSelectorPanel(net, evidence);
        panel.setDecisionSelected(net.getVariable(decision));
        return panel;
    }

    /** The selector of the scenario after the label {@code name}. */
    @SuppressWarnings("unchecked")
    private static JComboBox<String> selector(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (component instanceof JPanel row && row.getComponentCount() == 2
                    && row.getComponent(0) instanceof JLabel label && label.getText().equals(name)) {
                return (JComboBox<String>) row.getComponent(1);
            }
            if (component instanceof Container inner) {
                JComboBox<String> found = selector(inner, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static List<String> offered(JComboBox<String> selector) {
        List<String> states = new ArrayList<>();
        for (int i = 0; i < selector.getItemCount(); i++) {
            states.add(selector.getItemAt(i));
        }
        return states;
    }

    /** Before, the scenario proposed «Dec: Test = no» with «Test = negative», and the analysis failed. */
    @Test public void withAFindingOnTheTestItsDecisionCanOnlyBeYes() throws Exception {
        Variable test = net.getVariable("Test");
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(test, test.getStateIndex("negative")));
        ScopeSelectorPanel panel = scenario("Dec: New test", evidence);
        assertEquals(List.of("yes"), offered(selector(panel, "Dec: Test")));
        assertTrue(panel.getSelectedFindings().stream().anyMatch(
                finding -> finding.getVariable().getName().equals("Dec: Test") && finding.getState().equals("yes")));
    }

    @Test public void choosingADecisionLeavesThePossibleResultsOfItsTest() {
        ScopeSelectorPanel panel = scenario("Therapy", new EvidenceCase());
        JComboBox<String> decision = selector(panel, "Dec: Test");
        JComboBox<String> result = selector(panel, "Test");
        decision.setSelectedItem("no");
        assertEquals(List.of("not done"), offered(result));
        decision.setSelectedItem("yes");
        assertEquals(List.of("negative", "positive"), offered(result));
        assertTrue(panel.getSelectedFindings().stream().anyMatch(
                finding -> finding.getVariable().getName().equals("Test") && finding.getState().equals("negative")));
    }
}
