/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import java.io.Serializable;

/**
 * @author manuel
 */
public enum ConstraintBehavior implements Serializable {
	/** Every network of the type has the constraint. */
	YES,
	/** No network of the type has the constraint. */
	NO,
	/** The type does not put the constraint, but whoever needs it adds it to a network, and it is kept
	 * when the network changes its type. */
	ADDED_BY_CALLER
}
