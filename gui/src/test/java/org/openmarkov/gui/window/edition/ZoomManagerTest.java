/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.window.edition;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class is used to test the class {@link ZoomManager}.
 *
 * @author jmendoza
 */
public class ZoomManagerTest {
	/**
	 * Test the default constructor and the constructor with a value 0 as
	 * parameter.
	 */
	@Test public final void testZoom() {
		ZoomManager zoomManager;

		zoomManager = new ZoomManager();
		assertTrue(zoomManager.getZoom() == 1.0);
		zoomManager = new ZoomManager(0);
		assertTrue(zoomManager.getZoom() == 1.0);
	}

	/**
	 * Test the the constructor with a value non 0 as parameter.
	 */
	@Test public final void testZoomDouble() {
		ZoomManager zoomManager;

		zoomManager = new ZoomManager(0.5);
		assertTrue(zoomManager.getZoom() == 0.5);
	}

	/**
	 * Test the limits of the zoomManager.
	 */
	@Test public final void testZoomLimits() {
		ZoomManager zoomManager;

		zoomManager = new ZoomManager(6.0);
		assertTrue(zoomManager.getZoom() == 5.0);
		zoomManager = new ZoomManager(0.01);
		assertTrue(zoomManager.getZoom() == 0.1);
	}

	/**
	 * This method tests the method setZoom.
	 */
	@Test public final void testSetZoom() {
		ZoomManager zoomManager = new ZoomManager();

		zoomManager.setZoom(4.5);
		assertTrue(zoomManager.getZoom() == 4.5);
	}

	/**
	 * This method tests the conversion of screen coordinates to panel
	 * coordinates.
	 */
	@Test public final void testScreenToPanel() {
		ZoomManager zoomManager = new ZoomManager(4);
		double value;

		value = zoomManager.screenToPanel(2162.0);
		assertTrue(value == 540.5);
	}

	/**
	 * This method tests the conversion of panel coordinates to screen
	 * coordinates.
	 */
	@Test public final void testPanelToScreen() {
		ZoomManager zoomManager = new ZoomManager(1.5);
		double value;

		value = zoomManager.panelToScreen(428.0);
		assertTrue(value == 642.0);
	}
}
