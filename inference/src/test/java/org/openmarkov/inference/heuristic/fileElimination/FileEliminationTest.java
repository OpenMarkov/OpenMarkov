package org.openmarkov.inference.heuristic.fileElimination; ///*
//* Copyright 2011 CISIAD, UNED, Spain
//*
//* Licensed under the European Union Public Licence, version 1.1 (EUPL)
//*
//* Unless required by applicable law, this code is distributed
//* on an "AS IS" basis, WITHOUT WARRANTIES OF ANY KIND.
//*/
//
//package org.openmarkov.inference.heuristic.fileElimination;
//
//import static org.junit.Assert.assertTrue;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import org.junit.Before;
//import org.junit.Test;
//import org.openmarkov.core.action.core.PNUndoableEditEvent;
//import org.openmarkov.core.action.core.RemoveNodeEdit;
//import org.openmarkov.core.io.ProbNetReader;
//import org.openmarkov.core.model.network.Node;
//import org.openmarkov.core.model.network.ProbNet;
//import org.openmarkov.core.model.network.Variable;
//import org.openmarkov.io.elvira.ElviraParser;
//
//
//public class FileEliminationTest {
//
//	private ProbNet probNet4_4;
//
//	private String netName4_4 = "grid_rows_4_columns_4";
//
//	private ProbNet probNet5_5;
//
//	private String netName5_5 = "grid_rows_5_columns_5";
//
//	private ProbNet probNet6_6;
//
//	private String netName6_6 = "grid_rows_6_columns_6";
//
//	@Before public void setUp() throws Exception {
//		netName4_4 = getClass().getResource ("/nets/" + netName4_4 + ".elv").getFile ();
//	   	netName5_5 = getClass().getResource ("/nets/" + netName5_5 + ".elv").getFile ();
//	   	netName6_6 = getClass().getResource ("/nets/" + netName6_6 + ".elv").getFile ();
//
//	   	ProbNetReader reader = new ElviraParser();
//		probNet4_4 = reader.loadProbNet(netName4_4).getProbNet();
//// TODO Ver si se pueden eliminar estas tres lineas
////		probNet4_4.getGraph().makeLinksExplicit(false);
//		probNet5_5 = reader.loadProbNet(netName5_5).getProbNet();
////		probNet5_5.getGraph().makeLinksExplicit(false);
//		probNet6_6 = reader.loadProbNet(netName6_6).getProbNet();
////		probNet6_6.getGraph().makeLinksExplicit(false);
//	}
//
//	@Test public void testFileElimination() {
//		String[] variablesNames4_4 = { "Grid3_2", "Grid3_1", "Grid3_0",
//				"Grid2_3", "Grid2_1", "Grid2_0", "Grid1_3", "Grid1_2",
//				"Grid0_3", "Grid0_2", "Grid1_0", "Grid0_1", "Grid3_3",
//				"Grid2_2", "Grid1_1", "Grid0_0" };
//		List<Node> probNetNodes4_4 = probNet4_4.getNodes();
//		List<List<Variable>> variablesToEliminate4_4 = new ArrayList<>(1);
//		List<Variable> variables4_4 = new ArrayList<Variable>();
//		variablesToEliminate4_4.add(variables4_4);
//		// Eliminate Column and Row nodes
//		for (Node node : probNetNodes4_4) {
//			Variable variable = node.getVariable();
//			String name = variable.getName();
//			if (name.contains("Column") || name.contains("Row")) {
//				continue;
//			}
//			variables4_4.add(variable);
//		}
//		FileElimination fileElimination4_4 =
//			new FileElimination(probNet4_4,	variablesToEliminate4_4,
//				netName4_4);
//		Variable variableToDelete4_4;
//		int i = variablesNames4_4.length - 1;
//		variableToDelete4_4 = fileElimination4_4.getVariableToDelete();
//		do {
//			assertTrue(variableToDelete4_4.getName()
//				.equals(variablesNames4_4[i--]));
//			RemoveNodeEdit edit =
//				new RemoveNodeEdit(probNet4_4, variableToDelete4_4);
//			PNUndoableEditEvent event =
//				new PNUndoableEditEvent(this, edit, probNet4_4);
//			fileElimination4_4.undoableEditWillHappen(event);
//			fileElimination4_4.undoableEditHappened(event);
//			variableToDelete4_4 = fileElimination4_4.getVariableToDelete();
//		} while (variableToDelete4_4 != null);
//
//		String[] variablesNames5_5 = { "Grid4_3", "Grid4_2", "Grid4_1",
//				"Grid4_0", "Grid3_4", "Grid3_2", "Grid3_1", "Grid3_0",
//				"Grid2_4", "Grid2_3", "Grid1_4", "Grid1_3", "Grid0_4",
//				"Grid0_3", "Grid2_1", "Grid2_0", "Grid1_2", "Grid0_2",
//				"Grid1_0", "Grid0_1", "Grid4_4", "Grid3_3", "Grid2_2",
//				"Grid1_1", "Grid0_0" };
//		List<Node> probNetNodes5_5 = probNet5_5.getNodes();
//		List<List<Variable>> variablesToEliminate5_5 = new ArrayList<>();
//		List<Variable> variables5_5 = new ArrayList<>();
//		variablesToEliminate5_5.add(variables5_5);
//		// Eliminate Column and Row nodes
//
//		for (Node node : probNetNodes5_5) {
//			Variable variable = node.getVariable();
//			String name = variable.getName();
//			if (name.contains("Column") || name.contains("Row")) {
//				continue;
//			}
//			variables5_5.add(variable);
//		}
//		FileElimination fileElimination5_5 = new FileElimination(probNet5_5,
//			variablesToEliminate5_5, netName5_5);
//		Variable variableToDelete5_5;
//		i = variablesNames5_5.length - 1;
//		variableToDelete5_5 = fileElimination5_5.getVariableToDelete();
//		do {
//			assertTrue(variableToDelete5_5.getName()
//				.equals(variablesNames5_5[i--]));
//
//			RemoveNodeEdit edit =
//				new RemoveNodeEdit(probNet5_5, variableToDelete5_5);
//			PNUndoableEditEvent event =
//				new PNUndoableEditEvent(this, edit, probNet5_5);
//			fileElimination5_5.undoableEditWillHappen(event);
//			fileElimination5_5.undoableEditHappened(event);
//
//			variableToDelete5_5 = fileElimination5_5.getVariableToDelete();
//		} while (variableToDelete5_5 != null);
//
//		String[] variablesNames6_6 = { "Grid5_4", "Grid5_3", "Grid5_2",
//				"Grid5_1", "Grid5_0", "Grid4_5", "Grid4_3", "Grid4_2",
//				"Grid4_1", "Grid4_0", "Grid3_5", "Grid3_4", "Grid2_5",
//				"Grid2_4", "Grid1_5", "Grid1_4", "Grid0_5", "Grid0_4",
//				"Grid3_1", "Grid3_0", "Grid2_1", "Grid2_0", "Grid1_3",
//				"Grid1_2", "Grid0_3", "Grid0_2", "Grid3_2", "Grid2_3",
//				"Grid1_0", "Grid0_1", "Grid5_5", "Grid4_4", "Grid3_3",
//				"Grid2_2", "Grid1_1", "Grid0_0" };
//		List<Node> probNetNodes6_6 = probNet6_6.getNodes();
//		List<List<Variable>> variablesToEliminate6_6 = new ArrayList<>();
//		List<Variable> variables6_6 = new ArrayList<Variable>();
//		variablesToEliminate6_6.add(variables6_6);
//		// Eliminate Column and Row nodes
//		for (Node node : probNetNodes6_6) {
//			Variable variable = node.getVariable();
//			String name = variable.getName();
//			if (name.contains("Column") || name.contains("Row")) {
//				continue;
//			}
//			variables6_6.add(variable);
//		}
//		FileElimination fileElimination6_6 =
//			new FileElimination(probNet6_6,variablesToEliminate6_6, netName6_6);
//		Variable variableToDelete6_6;
//		i = variablesNames6_6.length - 1;
//		variableToDelete6_6 = fileElimination6_6.getVariableToDelete();
//		do {
//			assertTrue(variableToDelete6_6.getName()
//				.equals(variablesNames6_6[i--]));
//			RemoveNodeEdit edit =
//				new RemoveNodeEdit(probNet6_6, variableToDelete6_6);
//			PNUndoableEditEvent event =
//				new PNUndoableEditEvent(this, edit, probNet6_6);
//			fileElimination6_6.undoableEditWillHappen(event);
//			fileElimination6_6.undoableEditHappened(event);
//			variableToDelete6_6 = fileElimination6_6.getVariableToDelete();
//		} while (variableToDelete6_6 != null);
//
//	}
//
//}
