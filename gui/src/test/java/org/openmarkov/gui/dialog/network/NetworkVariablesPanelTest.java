/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.network;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.configuration.UserPreferences;

import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the drop-down list of default states of {@link NetworkVariablesPanel}: the states
 * it leaves selected must be the ones the network really has, whatever domains of their own the
 * user has written in the preferences.
 *
 * @author Manuel Arias
 */
class NetworkVariablesPanelTest {

    private static final List<String> USERS_OWN_DOMAIN = List.of("mine", "yours");

    /**
     * The states the drop-down list of a panel shows as chosen.
     */
    private static List<?> selectedDomainOf(NetworkVariablesPanel panel) {
        return (List<?>) comboBoxNamed(panel, "jComboBoxDefaultStates").getSelectedItem();
    }

    private static JComboBox<?> comboBoxNamed(Container container, String name) {
        for (Component component : container.getComponents()) {
            if ((component instanceof JComboBox<?> comboBox) && name.equals(comboBox.getName())) {
                return comboBox;
            }
            if (component instanceof Container inner) {
                JComboBox<?> found = comboBoxNamed(inner, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     * Builds the panel of a network with those default states, with one domain of the user's own
     * ahead of the catalogue's in the list. The preferences are left as they were, and nothing is
     * written to them.
     */
    private static List<?> domainShownFor(List<String> defaultStates) {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        ArrayList<ArrayList<String>> previousDomains = UserPreferences.CUSTOM_DOMAINS.get();
        try {
            UserPreference.IGNORE_STORAGE = true;
            UserPreferences.CUSTOM_DOMAINS.set(new ArrayList<>(List.of(new ArrayList<>(USERS_OWN_DOMAIN))));
            ProbNet probNet = new ProbNet();
            probNet.setDefaultStates(defaultStates.stream().map(State::new).toArray(State[]::new));
            return selectedDomainOf(new NetworkVariablesPanel(probNet));
        } finally {
            UserPreferences.CUSTOM_DOMAINS.set(previousDomains);
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }

    @Test
    void aDomainOfTheCatalogueIsShownAsChosen() {
        assertEquals(List.of("low", "medium", "high"), domainShownFor(List.of("low", "medium", "high")));
    }

    /**
     * The domains of the user go ahead of the catalogue's ones, which used to shift the chosen
     * entry: the panel showed a domain that was not the one the network had.
     */
    @Test
    void theDomainsOfTheUserDoNotShiftTheChosenOne() {
        assertEquals(List.of("absent", "present"), domainShownFor(List.of("absent", "present")));
    }

    @Test
    void aDomainOfTheUserIsShownAsChosen() {
        assertEquals(USERS_OWN_DOMAIN, domainShownFor(USERS_OWN_DOMAIN));
    }

    /**
     * A network may bring any states at all, since it may have been written elsewhere. The panel
     * then shows those states, instead of an unrelated domain of the catalogue.
     */
    @Test
    void statesThatAreNoKnownDomainAreShownAsTheyAre() {
        assertEquals(List.of("green", "amber", "red"), domainShownFor(List.of("green", "amber", "red")));
    }
}
