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
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * In inference mode, a state name too long for its box is shortened when it is drawn. It used to be kept shortened,
 * so that zooming out and in again left it so (error 168 of the pending list).
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AStateNameShortenedToBeDrawnKeepsItsNameTest {

    private static final String LONG_NAME = "present with a very long description of how it shows";

    @Test void theNameIsWholeAfterBeingDrawnShortened() throws Exception {
        ProbNet probNet = new ProbNet(BayesianNetworkType.getUniqueInstance());
        probNet.addNode(new Variable("Disease", "absent", LONG_NAME), NodeType.CHANCE);
        FSVariableBox box = (FSVariableBox) new VisualNetwork(probNet, null).getAllNodes().getFirst().getInnerBox();
        VisualState state = box.getVisualState(LONG_NAME);

        Graphics2D g = new BufferedImage(400, 300, BufferedImage.TYPE_INT_RGB).createGraphics();
        state.paint(g);
        g.dispose();

        assertEquals(LONG_NAME, state.getStateName());
    }
}
