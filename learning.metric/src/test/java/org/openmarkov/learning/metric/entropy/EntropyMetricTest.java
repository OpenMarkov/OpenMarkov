/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.entropy;


import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.InvertLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.model.database.CaseDatabase;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
//import org.openmarkov.io.database.elvira.ElviraDataBaseIO;

import java.util.ArrayList;

@Disabled("Elvira's Database is not properly implemented yet")
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class EntropyMetricTest {

	private final double maxError = 1E-6;
	private final double expectedScore = -3981.22537135;
	private final double expectedScoreLinkAdded = 260.68843349;
	private final double expectedScoreLinkInverted = -1.136868e-13;
	private final double expectedScoreLinkRemoved = -260.68843349;
	ProbNet probNet;
	EntropyMetric metric;
	private String dbFilename = "/learnTestDataBase.dbc";

	@BeforeEach
	public void setUp() {
		//TODO Commented and making CaseDatabase = null until fixing Elvira database parser with antlr4
		//ElviraDataBaseIO databaseIO = new ElviraDataBaseIO();
		//CaseDatabase database = databaseIO.load(getClass().getResource(dbFilename).getFile());
		CaseDatabase database = null;
		probNet = new ProbNet();

		for (Variable variable : database.getVariables()) {
			probNet.addNode(variable, NodeType.CHANCE);
		}

		ArrayList<Integer> variableIndex = new ArrayList<Integer>();
		for (int i = 0; i < probNet.getNumNodes(); i++) {
			variableIndex.add(i);
		}

		metric = new EntropyMetric();
		metric.init(probNet, database);
	}

	@Test
	public void testScores() {
		assertEquals(expectedScore, metric.getScore(), maxError);
	}

	@Test
	public void testScoreLinkAdded() {
		AddLinkEdit edition = new AddLinkEdit(probNet, probNet.getVariable("E"), probNet.getVariable("D"), true);
		assertEquals(expectedScoreLinkAdded, metric.getScore(edition), maxError);
	}

	@Test
    public void testScoreLinkInverted() throws DoEditException {
		AddLinkEdit auxiliarEdition = new AddLinkEdit(probNet, probNet.getVariable("E"), probNet.getVariable("D"),
				true);
        
        auxiliarEdition.executeEdit();
        
        InvertLinkEdit edition = new InvertLinkEdit(probNet, probNet.getVariable("E"), probNet.getVariable("D"), true);
		assertEquals(expectedScoreLinkInverted, metric.getScore(edition), maxError);
	}

	@Test
    public void testScoreLinkRemoved() throws DoEditException {
		AddLinkEdit auxiliarEdition = new AddLinkEdit(probNet, probNet.getVariable("D"), probNet.getVariable("E"),
				true);
        
        auxiliarEdition.executeEdit();
        
        RemoveLinkEdit edition = new RemoveLinkEdit(probNet, probNet.getVariable("D"), probNet.getVariable("E"), true);
		assertEquals(expectedScoreLinkRemoved, metric.getScore(edition), maxError);
	}
}
