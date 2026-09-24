/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.graphic;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A compiled node writes its values on the right of the bars. The columns of the box are measured with
 * the font the box draws with, so the values end before the border and the bar takes what is left.
 *
 * @author Manuel Arias
 */
class ValuesFitInTheInnerBoxTest {

	private static InnerBox boxOf(Variable variable, NodeType nodeType) throws Exception {
		ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
		probNet.addNode(variable, nodeType);
		return new VisualNetwork(probNet, null).getAllNodes().getFirst().getInnerBox();
	}

	private static void assertValueFits(InnerBox box, double value) {
		double end = box.getValueX() + InnerBox.getTextWidth(VisualState.formatValue(value));
		assertTrue(end <= InnerBox.BOX_WIDTH - InnerBox.STATES_INDENT,
				"The value " + VisualState.formatValue(value) + " ends at " + end + " in a box of "
						+ InnerBox.BOX_WIDTH);
	}

	@Test
	void aProbabilityEndsBeforeTheBorder() throws Exception {
		InnerBox box = boxOf(new Variable("Disease", "present", "absent"), NodeType.CHANCE);

		assertValueFits(box, 1.0);
	}

	@Test
	void aUtilityAsWideAsItsRangeEndsBeforeTheBorder() throws Exception {
		Variable variable = new Variable("Cost");
		variable.setVariableType(VariableType.NUMERIC);
		NumericVariableBox box = (NumericVariableBox) boxOf(variable, NodeType.UTILITY);
		box.setMinValue(-1500);
		box.setMaxValue(20);

		assertValueFits(box, -1234.5678);
	}

	@Test
	void theBarTakesWhatTheColumnsLeave() throws Exception {
		InnerBox box = boxOf(new Variable("Disease", "present", "absent"), NodeType.CHANCE);

		double expected = InnerBox.BOX_WIDTH - 3 * InnerBox.STATES_INDENT - box.getLabelColumnWidth()
				- box.getValueColumnWidth();
		assertTrue(Math.abs(box.getBarFullLength() - expected) < 1e-9,
				"The bar measures " + box.getBarFullLength() + " and the columns leave " + expected);
	}
}
