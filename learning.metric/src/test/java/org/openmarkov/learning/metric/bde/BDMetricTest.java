/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.metric.bde;

import org.junit.jupiter.api.*;
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
public class BDMetricTest {

	private final double maxError = 1E-6;
	ProbNet probNet;
	BDeMetric metric;
	private double alpha = 0.5;
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

		metric = new BDeMetric(alpha);
		metric.init(probNet, database);
	}

	@Test
	public void testScores() {
		double score = metric.getScore();
		Assertions.assertEquals(score, -4003.30495017, maxError);
	}

	@Test
	public void testScoreLinkAdded() {
		AddLinkEdit edition = new AddLinkEdit(probNet, probNet.getVariable("E"), probNet.getVariable("D"), true);
		double score = metric.getScore(edition);
		Assertions.assertEquals(score, 257.07201665, maxError);
	}

	@Test
    public void testScoreLinkInverted() throws DoEditException {
		AddLinkEdit auxiliarEdition = new AddLinkEdit(probNet, probNet.getVariable("E"), probNet.getVariable("D"),
				true);
        
        auxiliarEdition.executeEdit();
        
        InvertLinkEdit edition = new InvertLinkEdit(probNet, probNet.getVariable("E"), probNet.getVariable("D"), true);
		double score = metric.getScore(edition);
		Assertions.assertEquals(score, 4.5474735e-13, maxError);
	}

	@Test
    public void testScoreLinkRemoved() throws DoEditException {
		AddLinkEdit auxiliarEdition = new AddLinkEdit(probNet, probNet.getVariable("D"), probNet.getVariable("E"),
				true);
        
        auxiliarEdition.executeEdit();
        
        RemoveLinkEdit edition = new RemoveLinkEdit(probNet, probNet.getVariable("D"), probNet.getVariable("E"), true);
		double score = metric.getScore(edition);
		Assertions.assertEquals(score, -257.07201665, maxError);
	}
}
