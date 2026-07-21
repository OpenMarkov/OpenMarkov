/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.menutoolbar.common;

/**
 * This interface defines the method that menus and toolbars must implement to
 * manage zoomManager.
 *
 * @author jmendoza
 */
public interface ZoomMenuToolBar {

	/**
	 * Sets the zoom level displayed in the corresponding field.
	 *
	 * @param value the zoom factor (e.g. 1.0 = 100%)
	 */
    void setZoom(double value);
}
