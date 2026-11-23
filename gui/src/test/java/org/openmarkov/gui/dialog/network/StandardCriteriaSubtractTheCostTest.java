/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.network;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every set of «Standard criteria» gives its cost the scale -1, so that an analysis with a single criterion subtracts
 * it (error 375 of the pending list).
 *
 * @author Manuel Arias
 */
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The window needs a screen")
class StandardCriteriaSubtractTheCostTest {

    @Test void theCostOfEverySetHasANegativeScale() throws Exception {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            SwingUtilities.invokeAndWait(() -> {
                JFrame owner = new JFrame();
                ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
                probNet.setDecisionCriteria(new ArrayList<>(List.of(new Criterion("one"))));
                StandardCriteriaDialog dialog = new StandardCriteriaDialog(owner, probNet);
                try {
                    StandardCriteriaPanel panel = (StandardCriteriaPanel) dialog.getJPanelStandardCriteria();
                    List<JRadioButton> sets = panel.getRadioButtons();
                    assertEquals(7, sets.size());
                    for (JRadioButton set : sets) {
                        set.setSelected(true);
                        assertTrue(dialog.doOkClickBeforeHide());
                        Criterion cost = probNet.getDecisionCriteria().getFirst();
                        assertEquals("Cost", cost.getCriterionName(), set.getText());
                        assertEquals(-1.0, cost.getUnicriterizationScale(), set.getText());
                        assertEquals(1.0, probNet.getDecisionCriteria().getLast().getUnicriterizationScale(),
                                     set.getText());
                    }
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
