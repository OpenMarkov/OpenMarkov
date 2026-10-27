/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.openmarkov.core.action.base.EditsHistory;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The window «Standard criteria» opened with no option chosen and «OK» enabled. Accepting it so removed all the
 * criteria of the network, ended in an unexpected error and left open a sub-history of edits, after which the network
 * was no longer marked as modified (error 174 of the pending list); and it had no title (error 180). Now «OK» waits for
 * a choice, and the window is named after the button that opens it.
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The window needs a screen")
class StandardCriteriaWaitForAChoiceTest {

    @Test void nothingChangesUntilAnOptionIsChosen() throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            probNet.setDecisionCriteria(new ArrayList<>(List.of(new Criterion("coste"), new Criterion("efectividad"))));
            probNet.getPNESupport().setWithUndo(true);
            EditsHistory mainHistory = probNet.getPNESupport().getCurrentEditHistory();
            SwingUtilities.invokeAndWait(() -> {
                // The properties of the network, which the window repaints when it is accepted
                JFrame owner = new JFrame();
                StandardCriteriaDialog dialog = new StandardCriteriaDialog(owner, probNet);
                try {
                    assertEquals("Standard criteria", dialog.getTitle());
                    assertFalse(dialog.getOKButton().isEnabled());
                    assertFalse(dialog.doOkClickBeforeHide());
                    assertEquals(List.of("coste", "efectividad"),
                                 probNet.getDecisionCriteria().stream().map(Criterion::getCriterionName).toList());
                    assertSame(mainHistory, probNet.getPNESupport().getCurrentEditHistory());

                    StandardCriteriaPanel panel = (StandardCriteriaPanel) dialog.getJPanelStandardCriteria();
                    JRadioButton costEffectiveness = panel.getRadioButtons().stream()
                            .filter(button -> button.getText().contains("€/QALY")).findFirst().orElseThrow();
                    costEffectiveness.setSelected(true);
                    assertTrue(dialog.getOKButton().isEnabled());
                    assertTrue(dialog.doOkClickBeforeHide());
                    assertEquals(List.of("Cost", "Effectiveness"),
                                 probNet.getDecisionCriteria().stream().map(Criterion::getCriterionName).toList());
                    assertSame(mainHistory, probNet.getPNESupport().getCurrentEditHistory());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                } finally {
                    dialog.dispose();
                    owner.dispose();
                }
            });
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }
}
