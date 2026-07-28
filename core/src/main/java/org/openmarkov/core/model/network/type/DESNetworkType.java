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
	// Eager, like the other thirteen types: class initialization makes the one
	// instance unique and safe. The lazy unsynchronized version could hand two
	// different instances to two threads, and network types are compared by
	// reference identity.
	private static final DESNetworkType INSTANCE = new DESNetworkType();

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
		return INSTANCE;
	}

	// TODO
	/**
	 * @return String "DecisionAnalysisNetwork".
	 */
	public String toString() {
		return "DESNet";
	}

}
