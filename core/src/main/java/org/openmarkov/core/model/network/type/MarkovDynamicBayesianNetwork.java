/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

@NetworkTypeInfo(name = "Markov DAN", visualName = "Markov DAN")
public final class MarkovDynamicBayesianNetwork extends NetworkType {
    private static final MarkovDynamicBayesianNetwork INSTANCE = new MarkovDynamicBayesianNetwork();
    
    // Constructor
    private MarkovDynamicBayesianNetwork() {
        super();
        overrideConstraintBehavior(NoRevelationArc.class, ConstraintBehavior.NO);
        overrideConstraintBehavior(NoLinkRestriction.class, ConstraintBehavior.NO);
        overrideConstraintBehavior(OnlyAtemporalVariables.class, ConstraintBehavior.NO);
        overrideConstraintBehavior(OnlyTemporalVariables.class, ConstraintBehavior.NO);
    }
    
    // Methods
    public static MarkovDynamicBayesianNetwork getUniqueInstance() {
        return INSTANCE;
    }
    
}

