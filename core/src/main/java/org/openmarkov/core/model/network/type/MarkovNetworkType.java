/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.OnlyUndirectedLinks;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "MarkovNetwork", visualName = "Markov Network")
public final class MarkovNetworkType extends NetworkType {
    private static final MarkovNetworkType INSTANCE = new MarkovNetworkType();

	// Constructor
	private MarkovNetworkType() {
		super();
		overrideConstraintBehavior(OnlyChanceNodes.class, ConstraintBehavior.YES);
		overrideConstraintBehavior(OnlyDirectedLinks.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(OnlyUndirectedLinks.class, ConstraintBehavior.YES);
	}

	// Methods
	public static MarkovNetworkType getUniqueInstance() {
        return INSTANCE;
	}
	
}

