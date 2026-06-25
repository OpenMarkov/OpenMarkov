/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.AllChanceVariablesHaveChancePotentials;
import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.NoSelfLoop;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.constraint.UtilityNodes;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "MDP", visualName = "MDP") public final class MDPType extends NetworkType {
    private static final MDPType INSTANCE = new MDPType();

	// Constructor
	private MDPType() {
		super();
		overrideConstraintBehavior(NoCycle.class, ConstraintBehavior.YES);
		overrideConstraintBehavior(AllChanceVariablesHaveChancePotentials.class, ConstraintBehavior.YES);
		overrideConstraintBehavior(NoSelfLoop.class, ConstraintBehavior.YES);
		overrideConstraintBehavior(OnlyDirectedLinks.class, ConstraintBehavior.YES);
		overrideConstraintBehavior(UtilityNodes.class, ConstraintBehavior.YES);
	}

	// Methods
	public static MDPType getUniqueInstance() {
        return INSTANCE;
	}
	
}

