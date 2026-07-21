/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.action.base.linkEdits;

import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.operation.DiscretePotentialOperations;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author artasom
 * @author iagoparis - summer 2018
 * <p>
 * Inverts the arc between two nodes.
 * <p>
 * Being X -&#62; Y the link that is going to be inverted and being:
 * A: the group of nodes that are parents of X and are not parents of Y,
 * C: the group of nodes that are parents of Y (except X) and are not parents of X, and
 * B the group of parents that X and Y share,
 * <p>
 * The process takes five steps:
 * <p>
 * 1. Invert the arc.
 * <p>
 * 2. Share parents between the nodes.
 * <p>
 * 3. 	Calculate P(x, y|a, b, c) through P(x, y|a, b, c) = P(x|a, b) · P(y|x, b, c)
 * Meaning: P(x, y|a, b, c) = pot(x) · pot(y)
 * <p>
 * 4. Calculate P(y|a, b, c) through P(y|a, b, c) = Σ(x) P(x, y|a, b, c) and assign to node Y this probability.
 *
 * 5. Calculate P(x|a, b, c, y) through P(x|a, b, c, y) = P(x, y|a, b, c) / P(y|a, b, c) and assign to node X this probability.
 */
public final class InvertLinkAndUpdatePotentialsEdit extends BaseLinkEdit {
    
    
    // x (parent) node
    private final Node parent;
	// y (child) node
    private final Node child;
	// In case of undo, this list will keep the links created so they can be deleted
	private final List<Link<Node>> linksToUndo = new ArrayList<>();
	// Parent node's old potentials
	private List<Potential> parentsOldPotentials;
	// Child node's old potentials
	private List<Potential> childsOldPotentials;
    // Parent node's new potentials
    private TablePotential parentNewPotential;
    // Child node's new potentials
    private TablePotential childNewPotential;

	// Constructor

	/**
	 * @param probNet      the probabilistic network
	 * @param variableFrom the current parent variable
	 * @param variableTo   the current child variable
	 */
    public InvertLinkAndUpdatePotentialsEdit(ProbNet probNet, Variable variableFrom, Variable variableTo) {
        super(probNet, variableFrom, variableTo, true);
        parent = probNet.getNode(this.getVariableFrom());
        child = probNet.getNode(this.getVariableTo());
    }

	// Methods

	/**
	 */
	@Override protected void doEdit() throws DoEditException.CannotDoEditException {
		// The parents of x are retrieved
        List<Node> parentParents = parent.getParents();
		// The parents of y are retrieved
        List<Node> childParents = child.getParents();
		// The nodes will share their parents
        
        // 1. Invert the arc.
		// The link between i and j can be removed
        probNet.removeLink(parent, child, true);
		// and the link between j and i can be created
        probNet.addLink(child, parent, true);

		// 2. Share parents between the nodes.
		// The list of created links is emptied
		linksToUndo.clear();
		// {C(x) \ C(y)}: parents of X that were not parents of Y must become parents of Y
        List<Node> newParentsOfY = new ArrayList<>(parentParents);
        newParentsOfY.removeAll(childParents);
		for (Node newParent : newParentsOfY) {
            linksToUndo.add(new Link<>(newParent, child, true));
		}

		// {C(y) \ C(x) \ {x}}: parents of Y (except X) that were not parents of X must become parents of X
        List<Node> newParentsOfX = new ArrayList<>(childParents);
        newParentsOfX.removeAll(parentParents);
        newParentsOfX.remove(parent);
		for (Node newParent : newParentsOfX) {
            linksToUndo.add(new Link<>(newParent, parent, true));
		}

		List<TablePotential> xyPotentials = new ArrayList<>();
        
        parentsOldPotentials = parent.getPotentials();
        childsOldPotentials = child.getPotentials();


		// 3. 	Calculate P(x, y|a, b, c) through P(x, y|a, b, c) = P(x|a, b) · P(y|x, b, c)
		// Meaning: P(x, y|a, b, c) = pot(x) · pot(y)

		// pot(x) are added to xyPotentials
		for (Potential parentsOldPotential : parentsOldPotentials) {
            try {
                xyPotentials.add(parentsOldPotential.getCPT());
            } catch (NonProjectablePotentialException e) {
                throw new DoEditException.CannotDoEditException(e, this);
            }
        }

		// pot(y) are added to xyPotentials
		for (Potential childOldPotential : childsOldPotentials) {
            try {
                xyPotentials.add(childOldPotential.getCPT());
            } catch (NonProjectablePotentialException e) {
                throw new DoEditException.CannotDoEditException(e, this);
            }
        }

		// Correct order of variables
		Set<Variable> variables = new LinkedHashSet<>();
		// The first variable from each factor should go before the others
		for (Potential potential: xyPotentials) {
			if (potential.getNumVariables() > 0) {
				variables.add(potential.getVariable(0));
			}
		}
		for (Potential potential : xyPotentials) {
			variables.addAll(potential.getVariables());
		}
		List<Variable> orderedVariables = new ArrayList<>(variables);

		// xyPotentials are multiplied
		TablePotential xyPotentialMultiplied = DiscretePotentialOperations.multiply(xyPotentials);

		// Apply the correction of the order of the variables: Σ(x) P(x, y|a, b, c) = P(a|b, y, c) to Σ(x) P(x, y|a, b, c) = P(y|a, b, c)
		xyPotentialMultiplied = (TablePotential) xyPotentialMultiplied.reorder(new ArrayList<>(orderedVariables));

		// 4. Calculate P(y|a, b, c) through P(y|a, b, c) = Σ(x) P(x, y|a, b, c) and assign to node Y this probability.
        childNewPotential = DiscretePotentialOperations.marginalize(xyPotentialMultiplied, parent.getVariable());
        childNewPotential.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY);
        child.setPotential(childNewPotential);

		// 5. Calculate P(x|a, b, c, y) through P(x|a, b, c, y) = P(x, y|a, b, c) / P(y|a, b, c) and assign to node X this probability.
        parentNewPotential = DiscretePotentialOperations.divide(xyPotentialMultiplied, childNewPotential);
        parentNewPotential = DiscretePotentialOperations.imposeOtherDistributionWhenDistributionIsZero(parentNewPotential);
        parentNewPotential.setPotentialRole(PotentialRole.CONDITIONAL_PROBABILITY);
        parent.setPotential(parentNewPotential);

		for (Link<Node> link : linksToUndo) {
            probNet.addLink(link.getFrom(), link.getTo(), true);
		}
	}
	
	@Override public void undo() {
		super.undo();
        // Delete link Y -> X
        probNet.removeLink(variableTo, variableFrom, isDirected);
        // Re-create link X -> Y
        probNet.addLink(variableFrom, variableTo, isDirected);
        // Delete the links created when the nodes shared their fathers
        for (Link<Node> undoLink : linksToUndo) {
            probNet.removeLink(undoLink.getFrom(), undoLink.getTo(), true);
        }
        // The potentials of X are restored to the original ones
        parent.setPotentials(parentsOldPotentials);
        // The potentials of Y are restored to the original ones
        child.setPotentials(childsOldPotentials);
    }


    @Override public void redo() {
	    setTypicalRedo(false);
        super.redo();
        // Re-remove link X -> Y
        probNet.removeLink(variableFrom, variableTo, isDirected);
        // Recreate link Y -> X
        probNet.addLink(variableTo, variableFrom, isDirected);
        // Re-created the links of shared fathers
        for (Link<Node> linkToRedo : linksToUndo) {
            probNet.addLink(linkToRedo.getFrom(), linkToRedo.getTo(), true);
        }
        // The potentials of X are restored to the original ones. I convert the only potential to a list of one
        // element to use the same method in undo() and redo(). Using setPotential() (withous s) will modify the
        // parentsOldPotentials and childOldPotential objects, making the next undo()'s useless.
        List<Potential> xNewPotentials= new ArrayList<>();
        xNewPotentials.add(parentNewPotential);
        parent.setPotentials(xNewPotentials);
        // The potentials of Y are restored to the original ones
        List<Potential> yNewPotentials= new ArrayList<>();
        yNewPotentials.add(childNewPotential);
        child.setPotentials(yNewPotentials);
        
    }

	/**
	 * Method to compare two InvertLinkEdits comparing the names of
	 * the source and destination variable alphabetically.
	 *
	 * @param obj InvertLinkAndUpdatePotentialsEdit
	 * @return result of the comparison
	 */
	public int compareTo(InvertLinkAndUpdatePotentialsEdit obj) {
		int result;

		if ((
                result = variableFrom.getName().compareTo(obj.getVariableFrom().
                                                             getName())
		) != 0)
			return result;
		if ((
                result = variableTo.getName().compareTo(obj.getVariableTo().
                                                           getName())
		) != 0)
			return result;
        return 0;
    }

	@Override public String getOperationName() {
		return "Invert link and update potentials";
	}

	public String toString() {
		return "Invert link and update potentials: " + variableFrom.getName() + "-->" + variableTo.getName() + " ==> " + variableFrom.getName() + "<--" + variableTo.getName();
	}

	@Override public BaseLinkEdit getUndoEdit() {
        return new InvertLinkAndUpdatePotentialsEdit(getProbNet(), getVariableTo(),
                                                     getVariableFrom());
	}

}