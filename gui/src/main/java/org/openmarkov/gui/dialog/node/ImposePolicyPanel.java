package org.openmarkov.gui.dialog.node;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.exception.DoEditException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.PolicyType;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.gui.action.ImposePolicyEdit;

import java.util.ArrayList;

/**
 * Dialog for imposing a policy on a decision node. If the node does not already
 * have a policy, a new {@link TablePotential} with role POLICY is created.
 */
public class ImposePolicyPanel extends PotentialEditPanel {
    
    public ImposePolicyPanel(Node node, boolean readOnly, boolean potentialInitializesOnEditHistory) {
        super(node, readOnly, potentialInitializesOnEditHistory);
    }
    
    @Override protected void setPotentialInNode(@NotNull Potential newPotential) {
        this.getNode().setPotential(newPotential);
    }
    
    @Override protected void initializePotential() {
        if (getNode().getPotentials().isEmpty()) {
            Node node = getNode();
            node.setPolicyType(PolicyType.OPTIMAL);
            var variables = new ArrayList<>(node.getParents().stream().map(Node::getVariable).toList());
            variables.addFirst(node.getVariable());
            try {
                new ImposePolicyEdit(node, new TablePotential(variables, PotentialRole.POLICY)).executeEdit();
            } catch (DoEditException e) {
                throw new UnrecoverableException(e);
            }
        }
    }
    
    @Override
    protected @NotNull PNEdit generateSetPotentialEdit(@Nullable Potential originalPotential, @Nullable Potential newPotential) {
        return new ImposePolicyEdit(this.getNode(), originalPotential, newPotential);
    }
    
    @Override protected void removePotentialOnClose(@Nullable Potential originalPotential) {
        if (originalPotential != null) {
            this.getNode().setPotential(originalPotential);
        } else {
            this.getNode().clearPotentials();
        }
    }
    
}
