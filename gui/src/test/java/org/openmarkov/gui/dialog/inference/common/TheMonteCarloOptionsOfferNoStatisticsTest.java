/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.inference.common;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.MonteCarloOptions;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.AbstractButton;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Monte Carlo options of a network of events had four boxes, «Mean», «Sum», «Trimmed Mean» and «Median», that
 * did nothing: the results always show the mean (error 109 of the pending list). The team decided to remove them.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheMonteCarloOptionsOfferNoStatisticsTest {

    private static List<String> buttonTexts(Container container, List<String> texts) {
        for (Component component : container.getComponents()) {
            if (component instanceof AbstractButton button) {
                texts.add(button.getText());
            }
            if (component instanceof Container inner) {
                buttonTexts(inner, texts);
            }
        }
        return texts;
    }

    private static MonteCarloOptionsPanel panel() {
        ProbNet network = new ProbNet(DESNetworkType.getUniqueInstance());
        MonteCarloOptions options = network.getInferenceOptions().getMonteCarloOptions();
        options.setNumSimulations(250);
        options.setNumSeries(3);
        return new MonteCarloOptionsPanel(network);
    }

    @Test void thereAreNoBoxesForStatistics() {
        List<String> texts = buttonTexts(panel(), new ArrayList<>());

        for (String statistic : List.of("Mean", "Sum", "Trimmed Mean", "Median")) {
            assertTrue(!texts.contains(statistic), statistic + " is among " + texts);
        }
    }

    @Test void theOptionsAreStillWhatThePanelSays() {
        MonteCarloOptions options = panel().getMonteCarloOptions();

        assertEquals(250, options.getNumSimulations());
        assertEquals(3, options.getNumSeries());
    }
}
