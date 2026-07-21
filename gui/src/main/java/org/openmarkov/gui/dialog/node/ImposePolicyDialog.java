package org.openmarkov.gui.dialog.node;

import org.openmarkov.core.localize.StringDatabase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.potential.TablePotential;

import java.awt.*;

/**
 * Dialog for imposing a policy on a decision node. If the node does not already
 * have a policy, a new {@link TablePotential} with role POLICY is created.
 */
public class ImposePolicyDialog extends PotentialEditDialog {
    
    public ImposePolicyDialog(Window owner, boolean readOnly, Node node) {
        super(owner, node, readOnly);
    }
    
    @Override PotentialEditPanel generatePotentialEditPanel(Node node, boolean readOnly) {
        return new ImposePolicyPanel(node, readOnly, true);
    }
    
    @Override protected String getBaseTitle() {
        return StringDatabase.getUniqueInstance().getString("NodePropertiesDialog.EditPotentialTab.EditPolicyTitle");
    }
}
