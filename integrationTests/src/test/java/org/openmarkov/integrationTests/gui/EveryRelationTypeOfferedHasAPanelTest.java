/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExternalPotential;
import org.openmarkov.core.model.network.potential.IncrementPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.NetworkType;
import org.openmarkov.gui.configuration.UserPreference;
import org.openmarkov.gui.dialog.common.NotEditablePotentialPanel;
import org.openmarkov.gui.dialog.common.PotentialPanelManager;
import org.openmarkov.gui.dialog.node.PotentialEditPanel;

import javax.swing.JComboBox;
import java.awt.Component;
import java.awt.Container;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The selector of the relation type of a node offers only the types that have an editing panel (errors 101 and 307 of
 * the pending list), and a node whose relation has no panel still opens its window.
 *
 * @author Manuel Arias
 */
class EveryRelationTypeOfferedHasAPanelTest {

    private static boolean ignoreStorage;

    /** Building the panels must not read or write the preferences of whoever runs the tests. */
    @BeforeAll static void ignoreTheStoredPreferences() {
        ignoreStorage = UserPreference.IGNORE_STORAGE;
        UserPreference.IGNORE_STORAGE = true;
    }

    @AfterAll static void restoreTheStoredPreferences() {
        UserPreference.IGNORE_STORAGE = ignoreStorage;
    }

    private static <T extends Component> T find(Container container, Class<T> type, String name) {
        for (Component component : container.getComponents()) {
            if (type.isInstance(component) && (name == null || name.equals(component.getName()))) {
                return type.cast(component);
            }
            if (component instanceof Container inner && find(inner, type, name) instanceof Component found) {
                return type.cast(found);
            }
        }
        return null;
    }

    /** The relation types offered in the window of {@code node}. */
    private static List<Class<?>> offeredFor(Node node) throws Exception {
        JComboBox<?> types = find(new PotentialEditPanel(node, false, false), JComboBox.class, "jComboBoxRelationType");
        List<Class<?>> offered = new ArrayList<>();
        for (int i = 0; i < types.getItemCount(); i++) {
            offered.add((Class<?>) types.getItemAt(i));
        }
        return offered;
    }

    private static Variable numeric(String name) {
        return new Variable(name, true, 0, 100, true, 0.01);
    }

    static Stream<Arguments> nodes() {
        return Stream.of(DESNetworkType.getUniqueInstance(), BayesianNetworkType.getUniqueInstance(),
                         InfluenceDiagramType.getUniqueInstance())
                     .flatMap(type -> {
                         List<Arguments> nodes = new ArrayList<>();
                         nodes.add(Arguments.of(type, NodeType.CHANCE, new Variable("Finite", "a", "b")));
                         nodes.add(Arguments.of(type, NodeType.CHANCE, numeric("Numeric")));
                         if (type != BayesianNetworkType.getUniqueInstance()) {
                             nodes.add(Arguments.of(type, NodeType.UTILITY, numeric("Utility")));
                         }
                         return nodes.stream();
                     });
    }

    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The window of a node needs a screen")
    @ParameterizedTest(name = "{1} {2} in {0}")
    @MethodSource("nodes")
    void everyOfferedTypeHasAPanel(NetworkType networkType, NodeType nodeType, Variable variable) throws Exception {
        Node node = new ProbNet(networkType).addNode(variable, nodeType);
        for (Class<?> offered : offeredFor(node)) {
            @SuppressWarnings("unchecked") Class<? extends Potential> potentialClass = (Class<? extends Potential>) offered;
            assertNotNull(PotentialPanelManager.getInstance().getPotentialPanelClassOf(potentialClass),
                          "There is no panel for editing " + offered.getSimpleName());
        }
    }

    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The window of a node needs a screen")
    @Test void externalAndIncrementAreNotOfferedInANetworkOfEvents() throws Exception {
        Node node = new ProbNet(DESNetworkType.getUniqueInstance()).addNode(numeric("X"), NodeType.CHANCE);
        List<Class<?>> offered = offeredFor(node);
        assertFalse(offered.contains(ExternalPotential.class), offered.toString());
        assertFalse(offered.contains(IncrementPotential.class), offered.toString());
    }

    static Stream<Potential> potentialsWithoutAPanel() {
        Variable x = numeric("X");
        return Stream.of(new ExternalPotential(PotentialRole.CONDITIONAL_PROBABILITY, x),
                         new IncrementPotential(PotentialRole.CONDITIONAL_PROBABILITY, x));
    }

    @DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The window of a node needs a screen")
    @ParameterizedTest(name = "{0}")
    @MethodSource("potentialsWithoutAPanel")
    void aNodeWhoseRelationHasNoPanelOpensItsWindow(Potential potential) throws Exception {
        Node node = new ProbNet(DESNetworkType.getUniqueInstance()).addNode(potential.getVariable(0), NodeType.CHANCE);
        node.setPotential(potential);
        PotentialEditPanel window = new PotentialEditPanel(node, false, false);
        assertNotNull(find(window, NotEditablePotentialPanel.class, null));
        JComboBox<?> types = find(window, JComboBox.class, "jComboBoxRelationType");
        assertEquals(potential.getClass(), types.getSelectedItem());
        assertTrue(types.getItemCount() > 1, "The user can choose another relation type");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("potentialsWithoutAPanel")
    void aRelationWithoutAPanelGetsTheNoticeInstead(Potential potential) {
        Node node = new ProbNet(DESNetworkType.getUniqueInstance()).addNode(potential.getVariable(0), NodeType.CHANCE);
        node.setPotential(potential);
        assertInstanceOf(NotEditablePotentialPanel.class, PotentialPanelManager.getInstance().createPotentialPanel(node, null));
    }
}
