package org.openmarkov.core.action.core;

import java.util.ArrayList;
import java.util.List;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.UnreachableException;
import org.openmarkov.core.inference.BasicOperations;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.action.base.CompoundPNEdit;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.network.potential.TablePotential;

/**
 * Compound edit that absorbs (marginalizes out) the parents of a node,
 * reconnecting grandparents and updating potentials accordingly.
 */
@SuppressWarnings("serial") public class AbsorbParentsEdit extends CompoundPNEdit {
    private final Node node;
    
    public AbsorbParentsEdit(ProbNet probNet, Node node) {
        super(probNet);
        this.node = node;
    }
    
    @Override
    public ArrayList<PNEdit> generateEdits() {
        ArrayList<PNEdit> edits = new ArrayList<>();
        // gets neighbors of this node
        Variable nodeVariable = node.getVariable();
        List<Node> parents = probNet.getParents(node);
        ArrayList<TablePotential> parentsPotential = new ArrayList<>();
        for (Node node : parents) {
            try {
                parentsPotential.add(node.getPotential().tableProject(null, null));
            } catch (NonProjectablePotentialException e) {
                throw new UnreachableException(e);
            }
        }

        Potential potential = BasicOperations.absorbParentPotentials(nodeVariable,node.getPotential(),parentsPotential,null);
        for (Node parent : parents) {
            PNEdit newEdit;
            if (parent.getChildren().size() > 1) {
                newEdit = new RemoveLinkEdit(probNet, parent.getVariable(), nodeVariable, true, false);
            } else {  // parent.getChildren().size() == 1
                newEdit = new CRemoveNodeEdit(probNet, parent);
            }
            edits.add(newEdit);
        }
        
        for (Variable variable : potential.getVariables()) {
            if (variable != nodeVariable) {
                AddLinkEdit addLinkEdit = new AddLinkEdit(probNet, variable, nodeVariable, true);
                addLinkEdit.setUpdatePotentials(false);
                edits.add(addLinkEdit);
            }
        }
        edits.add(new SetPotentialEdit(node, potential));
        return edits;
    }
}
