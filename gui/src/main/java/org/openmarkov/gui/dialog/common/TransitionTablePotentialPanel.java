package org.openmarkov.gui.dialog.common;

import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;

/**
 * TransitionTablePotentialPanel for TransitionPotential
 * @author cmyago
 * @version 1.0 - cmyago - 24/03/2019 - only one TableWithEventsPanel
 * 04/10/2023 FIXME Check if it complies with OM wiki
 */
@SuppressWarnings("serial") @PotentialPanelPlugin(potentialClasses = TransitionTablePotential.class)
public class TransitionTablePotentialPanel
		extends TableWithEventsPanel {

	public TransitionTablePotentialPanel(Node node) {
		//When removing a link a uniform potential is returned which cannot be cast to TableWithEvents
		//FIXME 29/08/2023; functions not allowed
		super(node, replaceUniform(node),false);
	}

	/**
	 * 12/01/2023 replaces UniformPotential for TransitionTablePotential.
	 * When removing/adding a link a UniformPotential is assigned to the node. This method changes this potential to a TransitionTablePotential.
	 * If the node has already assigned a TransitionTablePotential, nothing is done.
	 * @param node whose Potential is checked
	 * @return the TransitionTablePotential associated to the node
	 */
	private static TransitionTablePotential replaceUniform(Node node){
		 Potential firstPotential = node.getPotentials().get(0);
		 if (!(firstPotential instanceof TransitionTablePotential )){
			 node.setPotential(new TransitionTablePotential(node.getPotentials().get(0).getVariables()));
		 }
		 return (TransitionTablePotential) node.getPotentials().get(0);
	}

}




