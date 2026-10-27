/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.dialog.node;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.gui.configuration.UserPreference;

import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The window of a node offers the types of variable that the network admits: a network of events does not admit
 * chance nodes divided into intervals (error 106 of the pending list), so it does not offer «Discretized» for them.
 *
 * @author Manuel Arias
 */
class ANetworkOfEventsDoesNotOfferIntervalsForAChanceNodeTest {

    private static JComboBox<?> find(Container container, String name) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?> comboBox && name.equals(comboBox.getName())) {
                return comboBox;
            }
            if (component instanceof Container inner && find(inner, name) instanceof JComboBox<?> found) {
                return found;
            }
        }
        return null;
    }

    /** The types of variable offered for a chance node of a network of {@code networkType}. */
    private static List<Object> offeredIn(NetworkType networkType) {
        boolean ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
        try {
            Node age = new ProbNet(networkType).addNode(new Variable("Age", "young", "old"), NodeType.CHANCE);
            JComboBox<?> types = find(new NodeDomainValuesTablePanel(age), "jComboBoxNodeVariableType");
            List<Object> offered = new ArrayList<>();
            for (int i = 0; i < types.getItemCount(); i++) {
                offered.add(types.getItemAt(i));
            }
            return offered;
        } finally {
            UserPreference.IGNORE_STORAGE = ignoreStorage;
        }
    }

    @Test void aNetworkOfEventsOffersFiniteStatesAndNumeric() {
        assertEquals(List.of(VariableType.FINITE_STATES, VariableType.NUMERIC), offeredIn(DESNetworkType.getUniqueInstance()));
    }

    @Test void otherNetworksStillOfferIntervals() {
        assertEquals(List.of(VariableType.FINITE_STATES, VariableType.DISCRETIZED, VariableType.NUMERIC),
                     offeredIn(BayesianNetworkType.getUniqueInstance()));
    }
}
