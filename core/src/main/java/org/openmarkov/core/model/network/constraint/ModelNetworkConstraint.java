/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import io.github.jorgericovivas.rust_essentials.tuples.Tuple2Record;
import io.github.jorgericovivas.rust_essentials.tuples.Tuples;
import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.action.base.linkEdits.BaseLinkEdit;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * This constraint ensures that the editions done during the learning of a
 * network respect the structure of the model net and the constraints
 * selected by the user.
 */
@Constraint(name = "ModelNetworkConstraint", defaultBehavior = ConstraintBehavior.OPTIONAL)
public class ModelNetworkConstraint extends PNConstraint {
    
    private final Set<Tuple2Record<String, String>> forbiddenLinksToModify;
    private final boolean linkAdditionAllowed;
    private final boolean linkRemovalAllowed;
    private final boolean linkInversionAllowed;
    
    // Constructors
    public ModelNetworkConstraint(ProbNet modelNet, boolean linkAdditionAllowed, boolean linkRemovalAllowed, boolean linkInversionAllowed) {
        this.linkAdditionAllowed = linkAdditionAllowed;
        this.linkRemovalAllowed = linkRemovalAllowed;
        this.linkInversionAllowed = linkInversionAllowed;
        var forbiddenLinksToModify = new HashSet<Tuple2Record<String, String>>();
        for (var sourceNode : modelNet.getNodes()) {
            for (var destinationNode : modelNet.getNodes()) {
                if (modelNet.getLink(sourceNode, destinationNode, true) != null) {
                    forbiddenLinksToModify.add(Tuples.record(sourceNode.getName(), destinationNode.getName()));
                }
            }
        }
        this.forbiddenLinksToModify = Collections.unmodifiableSet(forbiddenLinksToModify);
    }
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
    }
    
    public boolean canEditBeDone(BaseLinkEdit simpleEdit) {
        return !this.forbiddenLinksToModify.contains(Tuples.record(simpleEdit.getVariableFrom()
                                                                             .getName(), simpleEdit.getVariableTo()
                                                                                                   .getName()));
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
