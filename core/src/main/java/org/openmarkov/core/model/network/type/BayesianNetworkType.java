/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "BayesianNetwork", visualName = "Bayesian Network")
public final class BayesianNetworkType extends NetworkType {
    private static final BayesianNetworkType INSTANCE = new BayesianNetworkType();

	// Constructor
	private BayesianNetworkType() {
		super();
		overrideConstraintBehavior(OnlyChanceNodes.class, ConstraintBehavior.YES);
	}

	// Methods
    public static synchronized BayesianNetworkType getUniqueInstance() {
        return INSTANCE;
	}

}
