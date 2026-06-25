/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoLinkRestriction;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "TuningNetwork", visualName = "Tuning Network")
public final class TuningNetworkType extends NetworkType {
    private static final TuningNetworkType INSTANCE = new TuningNetworkType();

	// Constructor
    private TuningNetworkType() {
		super();
		overrideConstraintBehavior(NoLinkRestriction.class, ConstraintBehavior.NO);
	}

	// Methods
	public static TuningNetworkType getUniqueInstance() {
        return INSTANCE;
	}
	
}
