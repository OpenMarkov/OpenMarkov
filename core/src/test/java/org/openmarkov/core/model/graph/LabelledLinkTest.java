/*
 * Copyright (c) CISIAD, UNED, Spain. Licensed under the GPLv3 licence.
 */

package org.openmarkov.core.model.graph;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for {@link LabelledLink}, which previously had none: construction, the label accessors, and
 * that it inherits {@link Link}'s endpoint and direction behaviour.
 *
 * @author Manuel Arias
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class LabelledLinkTest {

    @Test public void constructorSetsEndpointsDirectionAndLabel() {
        LabelledLink<String> link = new LabelledLink<>("A", "B", true, "myLabel");
        assertEquals("A", link.getFrom());
        assertEquals("B", link.getTo());
        assertTrue(link.isDirected());
        assertEquals("myLabel", link.getLabel());
    }

    @Test public void setLabelChangesTheLabel() {
        LabelledLink<String> link = new LabelledLink<>("A", "B", false, "first");
        link.setLabel("second");
        assertEquals("second", link.getLabel());
    }

    @Test public void isALinkAndInheritsItsBehaviour() {
        LabelledLink<String> link = new LabelledLink<>("A", "B", false, null);
        assertTrue(link instanceof Link);
        assertFalse(link.isDirected());
        assertTrue(link.contains("A"));
        assertTrue(link.contains("B"));
        assertFalse(link.contains("C"));
        assertNull(link.getLabel());
    }
}
