/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.core.DecisionCriterionUnitEdit;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.NetworkCurrency;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The tab of the decision criteria of a network shows its currency in a selector; choosing another one changes the
 * unit of the cost in one step that is undone at once. When the cost is measured in something else, the selector says
 * so and changes nothing.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheCurrencyOfANetworkIsChosenInTheTabOfItsCriteriaTest {

    private ProbNet net;

    @BeforeEach void anInfluenceDiagramInEuros() {
        net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        new NetworkCurrency("EUR", "€").giveToANewNetwork(net);
        net.getPNESupport().setWithUndo(true);
    }

    private static JComboBox<?> selector(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?> comboBox && "currencyComboBox".equals(comboBox.getName())) {
                return comboBox;
            }
            if (component instanceof Container child) {
                JComboBox<?> found = selector(child);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /** Builds the tab, lets it show the currency and returns its selector. */
    private JComboBox<?> theSelectorOfTheTab() throws Exception {
        JComboBox<?>[] selector = new JComboBox<?>[1];
        SwingUtilities.invokeAndWait(() -> selector[0] = selector(new NetworkDecisionCriteriaPanel(null, net)));
        SwingUtilities.invokeAndWait(() -> { });
        return selector[0];
    }

    private List<String> units() {
        return net.getDecisionCriteria().stream().map(Criterion::getCriterionUnit).toList();
    }

    @Test void itShowsTheCurrencyOfTheNetwork() throws Exception {
        assertEquals("EUR (€)", theSelectorOfTheTab().getSelectedItem());
    }

    @Test void choosingDollarsChangesTheCostAndIsUndoneAtOnce() throws Exception {
        JComboBox<?> selector = theSelectorOfTheTab();
        SwingUtilities.invokeAndWait(() -> selector.setSelectedItem("USD ($)"));
        SwingUtilities.invokeAndWait(() -> { });

        assertEquals(List.of("$", "QALY"), units());
        assertEquals("USD", net.getAdditionalProperties().get("Currency"));
        assertEquals("USD ($)", selector.getSelectedItem());

        net.getPNESupport().undo();
        assertEquals(List.of("€", "QALY"), units());
        assertEquals("EUR", net.getAdditionalProperties().get("Currency"));
    }

    @Test void aCostInAnotherUnitIsShownAndKept() throws Exception {
        new DecisionCriterionUnitEdit(net, "Cost", "£").executeEdit();

        JComboBox<?> selector = theSelectorOfTheTab();

        assertEquals("Other (£)", selector.getSelectedItem());
        assertEquals(List.of("£", "QALY"), units());
        assertEquals("EUR", net.getAdditionalProperties().get("Currency"));
    }
}
