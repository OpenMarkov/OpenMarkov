/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

/**
 * This constraint ensures that the editions done during the learning of a
 * network respect the structure of the model net and the constraints
 * selected by the user.
 */
@Constraint(name = "ModelNetworkConstraint", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class ModelNetworkConstraint extends PNConstraint {
    
    private final ProbNet modelNet;
    private final boolean linkAdditionAllowed;
    private final boolean linkRemovalAllowed;
    private final boolean linkInversionAllowed;
    
    // Constructors
    public ModelNetworkConstraint(ProbNet modelNet, boolean linkAdditionAllowed, boolean linkRemovalAllowed, boolean linkInversionAllowed) {
        this.linkAdditionAllowed = linkAdditionAllowed;
        this.linkRemovalAllowed = linkRemovalAllowed;
        this.linkInversionAllowed = linkInversionAllowed;
        this.modelNet = modelNet.copy();
    }
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
    }
    
    public boolean canEditBeDone(BaseLinkEdit simpleEdit) {
        Node source = modelNet.getNode(simpleEdit.getVariableFrom().getName());
        Node destination = modelNet.getNode(simpleEdit.getVariableTo().getName());
        return modelNet.getLink(destination, source, true) == null;
    }
    
    public boolean isLinkAdditionAllowed() {
        return this.linkAdditionAllowed;
    }
    
    public boolean isLinkRemovalAllowed() {
        return this.linkRemovalAllowed;
    }
    
    public boolean isLinkInversionAllowed() {
        return this.linkInversionAllowed;
    }
}
