/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.AugmentedProbTable;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.common.UnivariateDistrPotentialPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.awt.Container;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The panel of the effectiveness of ID-CEA-minimal in format 1.0, which is 0.8 without the therapy. The user
 * starts editing that cell, writes 2 and presses Enter.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AParameterOfADistributionIsWrittenInItsCellTest {

    private static void tables(Container container, List<JTable> found) {
        for (Component component : container.getComponents()) {
            if (component instanceof JTable table) {
                found.add(table);
            }
            if (component instanceof Container inner) {
                tables(inner, found);
            }
        }
    }

    /** The table with the parameters; the other one holds the first column, with the names. */
    private static JTable parameters(UnivariateDistrPotentialPanel panel) {
        List<JTable> found = new ArrayList<>();
        tables(panel, found);
        return found.stream().max(Comparator.comparingInt(JTable::getColumnCount)).orElseThrow();
    }

    private static Node effectiveness() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/ID-CEA-minimal-1-0.pgmx"))
                          .findFirst()
                          .orElseThrow();
        return new PGMXReader().read(url).probNet().getNode("Effectiveness");
    }

    /** The panel as the dialog of the relation leaves it. */
    private static JTable parametersOfThePanel(Node node) throws Exception {
        UnivariateDistrPotentialPanel panel = new UnivariateDistrPotentialPanel(node);
        panel.setReadOnly(false);
        return parameters(panel);
    }

    @Test void theNumberWrittenIsTheParameter() throws Exception {
        Node node = effectiveness();

        SwingUtilities.invokeAndWait(() -> {
            try {
                JTable table = parametersOfThePanel(node);
                assertTrue(table.editCellAt(1, 0));
                ((JTextComponent) table.getEditorComponent()).setText("2");
                assertTrue(table.getCellEditor().stopCellEditing());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        AugmentedProbTable parameters = ((UnivariateDistrPotential) node.getPotential()).getDistributionTable();
        assertEquals(2.0, parameters.getValues()[0]);
        assertEquals("2", parameters.getFunctionValues()[0].asStringExpression());
    }

    @Test void theCellsWithTheStatesOfTheParentAreNotEdited() throws Exception {
        Node node = effectiveness();

        SwingUtilities.invokeAndWait(() -> {
            try {
                assertFalse(parametersOfThePanel(node).editCellAt(0, 0));
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });
    }
}
