/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyOneAgent;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "DEC_POMDP", visualName = "DEC POMDP") public final class DECPOMDPType extends POMDPType {
    private static final DECPOMDPType INSTANCE = new DECPOMDPType();

	// Constructor
	private DECPOMDPType() {
		super();

		overrideConstraintBehavior(OnlyOneAgent.class, ConstraintBehavior.NO);
	}

	// Methods
	public static DECPOMDPType getUniqueInstance() {
        return INSTANCE;
	}
	
}

