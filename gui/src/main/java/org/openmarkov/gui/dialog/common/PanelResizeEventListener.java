/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

/**
 * Listener notified when a {@link PotentialPanel} changes its preferred size.
 */
public interface PanelResizeEventListener {

	/**
	 * Called when the panel has been resized.
	 *
	 * @param event the resize event containing the new dimensions
	 */
	void panelSizeChanged(PanelResizeEvent event);
}
