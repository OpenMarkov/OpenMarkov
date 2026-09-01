/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.type;

import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.ConstraintManager;
import org.openmarkov.core.model.network.constraint.PNConstraint;

import java.util.HashMap;

public abstract sealed class NetworkType implements ClassLocalizable permits BayesianNetworkType, DESNetworkType, DecisionAnalysisNetworkType, DynamicBayesianNetwork, DynamicLimidType, InfluenceDiagramType, LIMIDType, MDPType, MIDType, MarkovDANType, MarkovNetworkType, POMDPType, TuningNetworkType {
	protected final HashMap<Class<? extends PNConstraint>, ConstraintBehavior> constraints;

	public NetworkType() {
		constraints = new HashMap<>();
	}

	public boolean isApplicableConstraint(PNConstraint constraint) {
		ConstraintBehavior behavior = (constraints.get(constraint.getClass()) != null) ?
				constraints.get(constraint.getClass()) :
				ConstraintManager.getUniqueInstance().
						getDefaultBehavior(constraint.getClass());
		return (behavior != ConstraintBehavior.NO);
	}

	protected void overrideConstraintBehavior(Class<? extends PNConstraint> constraintClass,
			ConstraintBehavior behavior) {
		constraints.put(constraintClass, behavior);
	}

	public HashMap<Class<? extends PNConstraint>, ConstraintBehavior> getOverwrittenConstraints() {
		return constraints;
	}
	
	@Override public String toString() {
		return this.localize();
	}
    
    public final String codeName() {
        return switch (this) {
            case BayesianNetworkType _ -> "bn";
            case DecisionAnalysisNetworkType _ -> "dan";
            case InfluenceDiagramType _ -> "id";
            case LIMIDType _ -> "limids";
            case MIDType _ -> "mid";
            case DECPOMDPType _ -> "decpomdp";
            case POMDPType _ -> "pomdp";
            case DESNetworkType _ -> "des";
            case DynamicBayesianNetwork _ -> "dbn";
            case DynamicLimidType _ -> "dlimit";
            case MDPType _ -> "mdp";
            case MarkovDANType _ -> "mdan";
            case MarkovNetworkType _ -> "markov";
            case TuningNetworkType _ -> "tuning";
        };
    }
	
}
