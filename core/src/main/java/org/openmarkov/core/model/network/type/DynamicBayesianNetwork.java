/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "DBN", visualName = "Dynamic Bayesian Network")
public final class DynamicBayesianNetwork extends NetworkType {
    private static final DynamicBayesianNetwork INSTANCE = new DynamicBayesianNetwork();

	// Constructor
	private DynamicBayesianNetwork() {
		super();
		overrideConstraintBehavior(OnlyChanceNodes.class, ConstraintBehavior.YES);
		overrideConstraintBehavior(OnlyAtemporalVariables.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(OnlyTemporalVariables.class, ConstraintBehavior.YES);
	}

	// Methods
	public static DynamicBayesianNetwork getUniqueInstance() {
        return INSTANCE;
	}
	
}

