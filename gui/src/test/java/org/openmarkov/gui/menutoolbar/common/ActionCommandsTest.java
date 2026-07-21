/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * This class tests the class
 * {@link ActionCommands}.
 *
 * @author jmendoza
 */
public class ActionCommandsTest {

	/**
	 * This method tests that the method isZoomActionCommand can recognize the
	 * zoomManager action commands.
	 */
	@Test public final void testIsZoomActionCommand() {
		assertTrue(ActionCommands.isZoomActionCommand("Zoom_115"));
		assertFalse(ActionCommands.isZoomActionCommand(" Zoom_115"));
		assertFalse(ActionCommands.isZoomActionCommand("Zoom_115 "));
		assertFalse(ActionCommands.isZoomActionCommand("ZOOM_115"));
		assertFalse(ActionCommands.isZoomActionCommand("Zoo115"));
		assertFalse(ActionCommands.isZoomActionCommand("Zoom115"));
		assertFalse(ActionCommands.isZoomActionCommand("ZoomManager 115"));
		assertFalse(ActionCommands.isZoomActionCommand("ZoomManager-115"));
		assertFalse(ActionCommands.isZoomActionCommand("ZoomManager.115"));
		assertFalse(ActionCommands.isZoomActionCommand("Zoom11.5"));
		assertFalse(ActionCommands.isZoomActionCommand("ZoomManager.115"));
		assertFalse(ActionCommands.isZoomActionCommand("ZoomManager.cien"));
		assertFalse(ActionCommands.isZoomActionCommand("ZoomManager.100c"));
		assertFalse(ActionCommands.isZoomActionCommand("ZoomManager.c100"));
	}

	/**
	 * This method tests how the method getValueZoomActionCommand can extract
	 * the zoomManager value from the action command string.
	 */
	@Test public final void testGetValueZoomActionCommand() {
		assertEquals(ActionCommands.getValueZoomActionCommand("Zoom_100"), 1.0, 0.1);
		assertEquals(ActionCommands.getValueZoomActionCommand("Zoom_200"), 2.0, 0.1);
		assertEquals(ActionCommands.getValueZoomActionCommand("Zoom_75"), 0.75, 0.1);
		assertEquals(ActionCommands.getValueZoomActionCommand("Zoom_1"), 0.01, 0.1);
		assertEquals(ActionCommands.getValueZoomActionCommand("Zoom_4357"), 43.57, 0.1);
	}

	/**
	 * This method tests how the method getZoomActionCommandValue builds various
	 * zoomManager action commands from their values.
	 */
	@Test public final void testGetActionCommandZoomValue() {
		assertEquals(ActionCommands.getZoomActionCommandValue(1.0), "Zoom_100");
		assertEquals(ActionCommands.getZoomActionCommandValue(2.0), "Zoom_200");
		assertEquals(ActionCommands.getZoomActionCommandValue(0.75), "Zoom_75");
		assertEquals(ActionCommands.getZoomActionCommandValue(0.01), "Zoom_1");
		assertEquals(ActionCommands.getZoomActionCommandValue(43.57), "Zoom_4357");
	}
}
