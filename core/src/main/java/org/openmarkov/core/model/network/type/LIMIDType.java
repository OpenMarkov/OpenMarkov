/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "LIMID", visualName = "LIMID") public final class LIMIDType extends NetworkType {
    private static final LIMIDType INSTANCE = new LIMIDType();

	// Constructor
	private LIMIDType() {
		super();
	}

	// Methods
	public static LIMIDType getUniqueInstance() {
        return INSTANCE;
	}
	
}
