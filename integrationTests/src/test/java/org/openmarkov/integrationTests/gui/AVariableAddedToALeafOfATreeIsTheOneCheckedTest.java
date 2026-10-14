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
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.dialog.treeadd.AddVariablesCheckBoxPanel;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import javax.swing.JCheckBox;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * «Add variables to potential» on the leaf of the branch «no» of the tree of «Puerta escogida 2» in
 * ID-Monty-Hall-spanish, a table over «Puerta escogida 2» and «Puerta escogida 1». The dialog offers «Puerta escogida
 * por el presentador»; the user checks it.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AVariableAddedToALeafOfATreeIsTheOneCheckedTest {

    @Test void andTheLeafDependsOnIt() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/ID-Monty-Hall-spanish.pgmx"))
                          .findFirst()
                          .orElseThrow();
        ProbNet probNet = new PGMXReader().read(url).probNet();
        TreeADDPotential tree = (TreeADDPotential) probNet.getNode("Puerta escogida 2").getPotential();
        TreeADDBranch no = tree.getBranches().stream()
                               .filter(branch -> branch.getBranchStates().getFirst().getName().equals("no"))
                               .findFirst()
                               .orElseThrow();
        Variable host = probNet.getVariable("Puerta escogida por el presentador");
        AddVariablesCheckBoxPanel panel = new AddVariablesCheckBoxPanel(no, tree);
        List<JCheckBox> boxes = panel.getCheckBoxes();
        assertEquals(List.of(host.getName()), boxes.stream().map(JCheckBox::getText).toList());

        boxes.getFirst().setSelected(true);
        Potential leaf = no.getPotential();
        for (Variable variable : panel.getSelectedVariables()) {
            leaf = leaf.addVariable(variable);
        }

        assertEquals(List.of(probNet.getVariable("Puerta escogida 2"), probNet.getVariable("Puerta escogida 1"), host),
                     leaf.getVariables());
        assertEquals(27, ((TablePotential) leaf).getValues().length);
    }
}
