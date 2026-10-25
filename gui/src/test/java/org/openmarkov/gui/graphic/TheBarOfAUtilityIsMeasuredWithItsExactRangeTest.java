/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.graphic;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.testTags.TestSpeed;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The bar of a utility in a range of hundredths is measured with the ends of the range, not with the ends rounded to
 * two decimals that the scale writes, and it never leaves its frame.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class TheBarOfAUtilityIsMeasuredWithItsExactRangeTest {

    private static final double FULL_LENGTH = 100;

    private static NumericVariableBox boxFrom0To(double max) throws Exception {
        Variable variable = new Variable("Fraction");
        variable.setVariableType(VariableType.NUMERIC);
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        probNet.addNode(variable, NodeType.UTILITY);
        var box = (NumericVariableBox) new VisualNetwork(probNet, null).getAllNodes().getFirst().getInnerBox();
        box.setMinValue(0);
        box.setMaxValue(max);
        return box;
    }

    @Test void theMiddleOfTheRangeFillsHalfTheBar() throws Exception {
        assertEquals(FULL_LENGTH / 2, boxFrom0To(0.0149).barLength(0.00745, FULL_LENGTH), 1e-9);
    }

    @Test void theEndOfTheRangeFillsTheWholeBar() throws Exception {
        assertEquals(FULL_LENGTH, boxFrom0To(0.0149).barLength(0.0149, FULL_LENGTH), 1e-9);
    }

    @Test void aValueOutsideTheRangeStaysInTheFrame() throws Exception {
        NumericVariableBox box = boxFrom0To(0.0149);
        assertEquals(FULL_LENGTH, box.barLength(0.02, FULL_LENGTH));
        assertEquals(0, box.barLength(-0.01, FULL_LENGTH));
    }

    @Test void aRangeWithInfiniteEndsPaintsNoBar() throws Exception {
        assertEquals(0, boxFrom0To(Double.POSITIVE_INFINITY).barLength(0.5, FULL_LENGTH));
    }
}
