package org.openmarkov.core.model.network.type;

import org.openmarkov.core.model.network.constraint.ConstraintBehavior;
import org.openmarkov.core.model.network.constraint.NoBackwardLink;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.NoEventNodes;
import org.openmarkov.core.model.network.constraint.NoLoops;
import org.openmarkov.core.model.network.constraint.NoSelfLoop;
import org.openmarkov.core.model.network.constraint.OnlyAtemporalVariables;
import org.openmarkov.core.model.network.constraint.OnlyOneOrphanInitialEvent;
import org.openmarkov.core.model.network.constraint.OnlySelfLoopsWithEventAndChanceNodes;
import org.openmarkov.core.model.network.constraint.OnlyTemporalVariables;
import org.openmarkov.core.model.network.type.plugin.NetworkTypeInfo;

/**
 * PGM network to implement Discrete Event Simulation (DES) Models
 * @author cmyago - 10/01/2019
 * @version 1.0 -cmyago- 10/01/2019
 * @version 1.1 -cmyago - 31/21/2019 -constrains changed to allow self loops in event nodes
 */
@NetworkTypeInfo(name = "DESNet", visualName = "DESNet")
public class DESNetworkType extends NetworkType {
	private static DESNetworkType instance = null;

	// Constructor
	private DESNetworkType() {
		super();
		overrideConstraintBehavior(OnlyAtemporalVariables.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(OnlyTemporalVariables.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoCycle.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoSelfLoop.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoEventNodes.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoBackwardLink.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(NoLoops.class, ConstraintBehavior.NO);
		overrideConstraintBehavior(OnlySelfLoopsWithEventAndChanceNodes.class, ConstraintBehavior.YES);
		overrideConstraintBehavior(OnlyOneOrphanInitialEvent.class, ConstraintBehavior.YES);
		//overrideConstraintBehavior(DistinctLinks.class, ConstraintBehavior.NO);

	}

	// Methods
	public static DESNetworkType getUniqueInstance() {
		if (instance == null) {
			instance = new DESNetworkType();
		}
		return instance;
	}

	// TODO
	/**
	 * @return String "DecisionAnalysisNetwork".
	 */
	public String toString() {
		return "DESNet";
	}

}
