/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.ThereIsNoPotentialsInNodeException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.gui.exception.NotEnoughMemoryException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;


public class TablePotentialPanelTest extends TablePotentialPanel {

	private static ProbNet probNet;
    
    public TablePotentialPanelTest() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ThereIsNoPotentialsInNodeException {
		super(probNet.getNode("E"));
	}

	@BeforeAll public static void setUp() {
		probNet = buildpotential_panel_reordered_pgmx();
	}

	private static ProbNet buildpotential_panel_reordered_pgmx() {
		ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
		// Variables
		Variable varA = new Variable("A", "absent", "mild", "moderate", "severe");
		Variable varB = new Variable("B", "no", "yes");
		Variable varC = new Variable("C", "negative", "positive");
		Variable varE = new Variable("E", "absent", "present");

		// Nodes
		Node nodeA = probNet.addNode(varA, NodeType.CHANCE);
		Node nodeB = probNet.addNode(varB, NodeType.CHANCE);
		Node nodeC = probNet.addNode(varC, NodeType.CHANCE);
		Node nodeE = probNet.addNode(varE, NodeType.CHANCE);

		// Links
		probNet.makeLinksExplicit(false);
		probNet.addLink(nodeA, nodeE, true);
		probNet.addLink(nodeB, nodeE, true);
		probNet.addLink(nodeC, nodeE, true);

		// Potentials
		UniformPotential potA = new UniformPotential(Arrays.asList(varA), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeA.setPotential(potA);

		UniformPotential potB = new UniformPotential(Arrays.asList(varB), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeB.setPotential(potB);

		UniformPotential potC = new UniformPotential(Arrays.asList(varC), PotentialRole.CONDITIONAL_PROBABILITY);
		nodeC.setPotential(potC);

		TablePotential potE = new TablePotential(Arrays.asList(varE, varA, varB, varC),
				PotentialRole.CONDITIONAL_PROBABILITY);
		potE.setValues(new double[] { 0.9, 0.1, 0.8, 0.2, 0.7, 0.3, 0.6, 0.4, 0.5, 0.5, 0.4, 0.6, 0.3, 0.7, 0.2, 0.8,
				0.001, 0.999, 0, 1, 0.9, 0.1, 0.8, 0.2, 0.7, 0.3, 0.6, 0.4, 0.5, 0.5, 0.4, 0.6 });
		nodeE.setPotential(potE);

		// Link restrictions and revealing states
		// Always observed nodes

		return probNet;
	}

	@Test public void testHowManyRows() {
        assertEquals(TablePotentialPanel.howManyRows(probNet.getNode("E")), 5);
	}

	@Test public void testNumberOfColumns() {
		assertTrue(this.columns.length == 17);
	}
    
    @Test
    public void testGetEvidenceCaseFromSelectedColumn() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ThereIsNoPotentialsInNodeException {
		this.selectedColumn = 3;
		EvidenceCase ec = this.getEvidenceCaseFromSelectedColumn();
		String i = null;
		for (Finding f : ec.getFindings()) {
			i = f.getVariable().getName();
			switch (i) {
			case "A":
				assertTrue(f.getStateIndex() == 0);
				break;
			case "B":
				assertTrue(f.getStateIndex() == 1);
				break;
			case "C":
				assertTrue(f.getStateIndex() == 0);
				break;
			}
		}
	}
    
    @Test
    public void testGetEvidenceCaseFromSelectedColumn2() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, ThereIsNoPotentialsInNodeException {
		this.selectedColumn = 9;
		EvidenceCase ec = this.getEvidenceCaseFromSelectedColumn();

		String i = null;

		for (Finding f : ec.getFindings()) {
			i = f.getVariable().getName();
			switch (i) {
			case "A":
				assertTrue(f.getStateIndex() == 2);
				break;
			case "B":
				assertTrue(f.getStateIndex() == 0);
				break;
			case "C":
				assertTrue(f.getStateIndex() == 0);
				break;
			}
		}
	}
}
