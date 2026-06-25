/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "MID", visualName = "Markov Influence Diagram", alternativeNames = "MPAD")
public final class MIDType extends NetworkType {
	// Attributes
    private static final MIDType INSTANCE = new MIDType();

	// Constructor
	private MIDType() {
		super();
		overrideConstraintBehavior(OnlyAtemporalVariables.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(OnlyTemporalVariables.class, ConstraintBehavior.NO);
	}

	// Methods
	public static MIDType getUniqueInstance() {
        return INSTANCE;
	}
	
}
