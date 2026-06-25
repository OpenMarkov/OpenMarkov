/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.common;

import javax.swing.*;
import java.awt.*;

/**
 * Event object carrying the source panel and its new dimensions after a resize.
 */
public class PanelResizeEvent {
	private final JPanel source;
	private final Dimension newDimension;

	public PanelResizeEvent(JPanel source, Dimension newDimension) {
		super();
		this.source = source;
		this.newDimension = newDimension;
	}

	public JPanel getSource() {
		return source;
	}

	public Dimension getNewDimension() {
		return newDimension;
	}

}
