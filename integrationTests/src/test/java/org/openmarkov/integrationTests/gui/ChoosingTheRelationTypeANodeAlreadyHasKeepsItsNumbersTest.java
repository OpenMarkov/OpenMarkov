/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.gui;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.node.PotentialEditPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * In the tab of the relation of a node, the user opens «Relation Type» and clicks on the type that is already chosen:
 * a table of BN-hepar and a Weibull hazard of MID-hip-Briggs.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
@DisabledIf(value = "java.awt.GraphicsEnvironment#isHeadless", disabledReason = "The panel needs a display")
class ChoosingTheRelationTypeANodeAlreadyHasKeepsItsNumbersTest {

    private static JComboBox<?> relationType(Container container) {
        for (Component component : container.getComponents()) {
            if (component instanceof JComboBox<?> comboBox && "jComboBoxRelationType".equals(comboBox.getName())) {
                return comboBox;
            }
            if (component instanceof Container inner) {
                JComboBox<?> comboBox = relationType(inner);
                if (comboBox != null) {
                    return comboBox;
                }
            }
        }
        return null;
    }

    @ParameterizedTest
    @CsvSource({ "/networks/bn/BN-hepar.pgmx, ggtp", "/mid/MID-hip-Briggs.pgmx, Failure [1]" })
    void keepsItsRelation(String file, String nodeName) throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith(file))
                          .findFirst()
                          .orElseThrow();
        Node node = new PGMXReader().read(url).probNet().getNode(nodeName);
        Potential before = node.getPotential();
        double[] numbers = before instanceof TablePotential table ? table.getValues().clone() : null;

        SwingUtilities.invokeAndWait(() -> {
            try {
                JComboBox<?> comboBox = relationType(new PotentialEditPanel(node, false, false));
                comboBox.setSelectedItem(comboBox.getSelectedItem());
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        });

        assertSame(before, node.getPotential());
        if (numbers != null) {
            assertArrayEquals(numbers, ((TablePotential) node.getPotential()).getValues());
        }
    }
}
