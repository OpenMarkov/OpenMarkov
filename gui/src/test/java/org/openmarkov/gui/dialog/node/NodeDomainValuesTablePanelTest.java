/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.node;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;


/**
 * This class tests the NodeDiscretizeValuesTablePanel class (not the visual
 * behaviour).
 *
 * @author jlgozalo
 * @version 1.0
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class NodeDomainValuesTablePanelTest {

	NodeDomainValuesTablePanel panel = null;

	@BeforeEach public void setUp() {
		panel = new NodeDomainValuesTablePanel(false);

	}

	/**
	 * test to verify the getter and setter methods (non visual elements)
	 */
	@Test public void testGetterAndSetters() {

	}

	/**
	 * test to verify the convertStringsToTableFormat method
	 */
	@Test public void testConvertStringsToTableFormat() {

		State[] stateValues = new State[4];
		stateValues[0] = new State("(0.7,1]");
		stateValues[1] = new State("nameTest(0.4,0.7]");
		stateValues[2] = new State("name Test(0.15,0.4]");
		stateValues[3] = new State("name TEST 1 [0,0.15]");

		Object[][] result = panel.convertStringsToTableFormat(stateValues);

		//assertEquals(result.length,20);
		assertEquals("", result[0][0]);
		assertEquals("(", result[0][1]);
		assertEquals(0.7, result[0][2]);
		assertEquals(",", result[0][3]);
		assertEquals(1.0, result[0][4]);
		assertEquals("nameTest", result[1][0]);
		assertEquals("]", result[0][5]);
		assertEquals("(", result[1][1]);
		assertEquals(0.4, result[1][2]);
		assertEquals(",", result[1][3]);
		assertEquals(0.7, result[1][4]);
		assertEquals("]", result[1][5]);
		assertEquals("name Test", result[2][0]);
		assertEquals("(", result[2][1]);
		assertEquals(0.15, result[2][2]);
		assertEquals(",", result[2][3]);
		assertEquals(0.4, result[2][4]);
		assertEquals("]", result[2][5]);
		assertEquals("name TEST 1 ", result[3][0]);
		assertEquals("[", result[3][1]);
		assertEquals(0.0, result[3][2]);
		assertEquals(",", result[3][3]);
		assertEquals(0.15, result[3][4]);
		assertEquals("]", result[3][5]);

	}

	/**
	 * The precision box shows the precision of the variable, also when it is 0.0001
	 * (written "1.0E-4") or a value that is not in the list.
	 */
	@Test public void thePrecisionBoxShowsThePrecisionOfTheVariable() throws Exception {
		assertThat(precisionShownFor(0.0001)).isEqualTo("0.0001");
		assertThat(precisionShownFor(0.01)).isEqualTo("0.01");
		assertThat(precisionShownFor(2.0)).isEqualTo("2");
		assertThat(precisionShownFor(0.5)).isEqualTo("0.5");
		assertThat(precisionShownFor(0.0)).isEqualTo("0");
	}

	/** A precision that is not in the list is added in its place, keeping the list in order. */
	@Test public void aPrecisionThatIsNotInTheListIsAddedInOrder() throws Exception {
		NodeDomainValuesTablePanel panel = openPanel(numericNode(0.5));
		var comboBox = panel.getJComboBoxPrecision();
		var items = new java.util.ArrayList<String>();
		for (int i = 0; i < comboBox.getItemCount(); i++) {
			items.add(comboBox.getItemAt(i));
		}
		assertThat(items).containsExactly("1", "0.5", "0.1", "0.01", "0.001", "0.0001");
	}

	/** Opening the panel does not round the limits of the variable to its precision. */
	@Test public void openingThePanelDoesNotChangeTheLimits() throws Exception {
		Node node = numericNode(0.1);
		openPanel(node);
		assertThat(node.getVariable().getPartitionedInterval().getLimits()).containsExactly(0.25, 7.75);
	}

	/**
	 * The panel of a utility node, built as the dialog builds it, shows the precision of its variable, which opening it
	 * does not change; and choosing «1» gives the variable that precision (error 263).
	 */
	@Test public void thePrecisionBoxOfAUtilityNodeShowsThePrecisionOfItsVariable() throws Exception {
		Node twoUnits = utilityNode(2);
		Node hundredths = utilityNode(0.01);

		assertThat(new NodeDomainValuesTablePanel(twoUnits).getJComboBoxPrecision().getSelectedItem()).isEqualTo("2");
		NodeDomainValuesTablePanel panel = new NodeDomainValuesTablePanel(hundredths);
		assertThat(panel.getJComboBoxPrecision().getSelectedItem()).isEqualTo("0.01");
		assertThat(twoUnits.getVariable().getPrecision()).isEqualTo(2.0);
		assertThat(hundredths.getVariable().getPrecision()).isEqualTo(0.01);

		panel.getJComboBoxPrecision().setSelectedItem("1");

		assertThat(hundredths.getVariable().getPrecision()).isEqualTo(1.0);
	}

	/** Building the panel of a utility node, as the dialog builds it, edits nothing (error 315). */
	@Test public void thePanelOfAUtilityNodeEditsNothingWhenItIsBuilt() {
		for (double precision : new double[]{2, 0.01}) {
			Node node = utilityNode(precision);
			ProbNet net = node.getProbNet();
			net.getPNESupport().setWithUndo(true);

			new NodeDomainValuesTablePanel(node);

			assertThat(net.getPNESupport().getCanUndo()).as("an edit to undo").isFalse();
			assertThat(net.getPNESupport().networkIsModified()).as("the network is modified").isFalse();
			assertThat(node.getVariable().getPrecision()).isEqualTo(precision);
		}
	}

	/**
	 * A precision that is not a finite number, which BigDecimal cannot write, is shown as Java writes it, also in the
	 * tab of a utility node (error 264).
	 */
	@Test public void aPrecisionThatIsNotAFiniteNumberIsShownAsItIs() throws Exception {
		assertThat(precisionShownFor(Double.NaN)).isEqualTo("NaN");
		assertThat(precisionShownFor(Double.POSITIVE_INFINITY)).isEqualTo("Infinity");
		assertThat(precisionShownFor(Double.NEGATIVE_INFINITY)).isEqualTo("-Infinity");

		assertThat(new NodeDomainValuesTablePanel(utilityNode(Double.NaN)).getJComboBoxPrecision().getSelectedItem())
				.isEqualTo("NaN");
	}

	private static Node utilityNode(double precision) {
		Variable variable = new Variable("U");
		variable.setPrecision(precision);
		return new ProbNet(InfluenceDiagramType.getUniqueInstance()).addNode(variable, NodeType.UTILITY);
	}

	private static String precisionShownFor(double precision) throws Exception {
		return (String) openPanel(numericNode(precision)).getJComboBoxPrecision().getSelectedItem();
	}

	private static NodeDomainValuesTablePanel openPanel(Node node) throws Exception {
		NodeDomainValuesTablePanel panel = new NodeDomainValuesTablePanel(node);
		panel.setFieldsFromProperties(node);
		return panel;
	}

	private static Node numericNode(double precision) {
		Variable variable = new Variable("X", true, 0.25, 7.75, true, precision);
		return new ProbNet().addNode(variable, NodeType.CHANCE);
	}

}
