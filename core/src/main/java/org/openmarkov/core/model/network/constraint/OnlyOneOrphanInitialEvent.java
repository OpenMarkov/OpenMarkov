package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.PurposeType;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.List;
import java.util.stream.Collectors;

/**
 * This constrain  checks that there is only one initial node with no parents
 * @author cmyago - cmyago adapted it from NoSelfLoop - 26/01/2020 - this constraint is only used in DESNets
 */
@Constraint(name = "InitialNodeConstrain", defaultBehavior = ConstraintBehavior.NO)
public class OnlyOneOrphanInitialEvent extends PNConstraint {
	
	@Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		List<Node> eventNodes =  probNet.getNodes(NodeType.EVENT);
		List<Node> initialEventList = eventNodes.stream().filter(node->node.getPurpose().equals(PurposeType.INITIAL_EVENT.getName())).collect(Collectors.toList());
		if ((initialEventList.size()>1)
				|| ((initialEventList.size()==1) && initialEventList.get(0).getNumParents()>0)) {
			constraintChecker.addException(new ConstraintViolatedException.OnlyOneOrphanInitialEventException(this, initialEventList));
		}
	}
	
}
