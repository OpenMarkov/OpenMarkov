/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.component;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PartitionedInterval;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.gui.action.RevelationIntervalEdit;

import javax.swing.JButton;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openmarkov.gui.component.AnyIntervalRevealedByALinkCanBeRemovedTest.button;
import static org.openmarkov.gui.component.AnyIntervalRevealedByALinkCanBeRemovedTest.linkFromAge;
import static org.openmarkov.gui.component.AnyIntervalRevealedByALinkCanBeRemovedTest.panelOf;

/**
 * In the revelation conditions of a link, «Add» is disabled while the last interval reaches infinity, since the new
 * one would start there, and is enabled again when the end of the last one is written.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AddNeedsRoomForANewRevealedIntervalTest {

    @Test void addFollowsTheEndOfTheLastInterval() throws Exception {
        Link<Node> link = linkFromAge();
        RevelationArcDiscretizeTablePanel panel = panelOf(link);
        JButton add = button(panel, "KeyTablePanel.addValueButton");
        assertTrue(add.isEnabled(), "a link with no intervals");

        add.doClick();
        assertEquals(1, link.getRevealingIntervals().size());
        assertFalse(add.isEnabled(), "with [-∞, +∞)");

        new RevelationIntervalEdit(link, StateAction.MODIFY_VALUE_INTERVAL, 0, 10, false).executeEdit();
        panel.setPartitionedInterval();
        assertTrue(add.isEnabled(), "with [-∞, 10)");

        add.doClick();
        PartitionedInterval added = link.getRevealingIntervals().getLast();
        assertEquals(2, link.getRevealingIntervals().size());
        assertEquals(10, added.getMin());
        assertEquals(Double.POSITIVE_INFINITY, added.getMax());
        assertFalse(add.isEnabled(), "with [10, +∞) last");
    }
}
