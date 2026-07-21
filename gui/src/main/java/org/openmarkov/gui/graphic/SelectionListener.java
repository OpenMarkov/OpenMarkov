/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.graphic;

import java.util.List;

/**
 * This interface is used in order to a network panel advise to the listener
 * that some objects are selected, to copy, cut or remove them.
 *
 * @author jmendoza
 * @author asaez
 * @version 1.1    Added arrayOfNodes as a new parameter
 * Needed for knowing which nodes are currently selected
 */
public interface SelectionListener {

	/**
	 * This method indicates the selected elements
	 */
	void objectsSelected();

}
