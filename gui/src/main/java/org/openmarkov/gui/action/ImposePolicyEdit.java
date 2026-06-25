package org.openmarkov.gui.action;

import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.Potential;

/**
 * Edit that imposes or replaces a policy on a decision node, supporting undo and redo.
 */
public class ImposePolicyEdit extends PNEdit {

    private final Node decisionNode;
    private final Potential newPolicy;
    private final Potential lastPolicy;

    /**
     * Creates a new edit that imposes a policy on a decision node.
     *
     * @param decisionNode the visual decision node to modify
     * @param lastPolicy         the previous policy (used for undo)
     * @param newPolicy          the new policy to impose
     */
    public ImposePolicyEdit(Node decisionNode,Potential lastPolicy, Potential newPolicy) {
        super(decisionNode.getProbNet());
        this.decisionNode = decisionNode;
        this.lastPolicy = lastPolicy;
        this.newPolicy = newPolicy;
    }
    /**
     * Creates a new edit that imposes a policy, using the node's current potential as the previous policy.
     *
     * @param decisionNode the visual decision node to modify
     * @param newPolicy          the new policy to impose
     */
    public ImposePolicyEdit(Node decisionNode, Potential newPolicy) {
        super(decisionNode.getProbNet());
        this.decisionNode = decisionNode;
        this.lastPolicy = this.decisionNode.getPotential();
        this.newPolicy = newPolicy;
    }

    @Override
    protected void doEdit() {
        decisionNode.setPotential(newPolicy);
    }


    @Override
    public void undo() {
        if(lastPolicy != null) {
            decisionNode.setPotential(lastPolicy);
        }else{
            decisionNode.clearPotentials();
        }
    }

    @Override
    public void redo() {
        decisionNode.setPotential(newPolicy);
    }
}
