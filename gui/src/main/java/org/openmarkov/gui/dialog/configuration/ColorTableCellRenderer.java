/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.dialog.configuration;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

/**
 * ColorTableCellRenderes handles the actions to allow users to select colors
 * from a ColorChooser
 *
 * @author jlgozalo
 * @version 1.0 - 11 Mar 2010 initial version
 */
public class ColorTableCellRenderer extends JPanel implements TableCellRenderer {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -803397354369463131L;

	/* (non-Javadoc)
	 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
	 */
	@Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                                                             int row, int column) {

		setBackground((Color) value);
		if (hasFocus) {
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		} else {
			setBorder(null);
		}
		return this;
	}
}
