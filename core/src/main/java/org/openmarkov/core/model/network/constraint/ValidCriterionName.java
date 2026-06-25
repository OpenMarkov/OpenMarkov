/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.constraint;

import org.openmarkov.core.action.base.ConstraintChecker;
import org.openmarkov.core.model.network.GraphNetwork;
import org.openmarkov.core.model.network.constraint.annotation.Constraint;

@Constraint(name = "NoValidCriterionName", defaultBehavior = ConstraintBehavior.YES) public class ValidCriterionName
		extends PNConstraint {

	// Constants for possible errors
    private static final int IS_EMPTY_NAME = 0;
    private static final int IS_NAME_ALREADY_EXIST = 1;
    
    
    @Override public void checkProbNet(GraphNetwork probNet, ConstraintChecker constraintChecker) {
		
		/*
		List<Criterion> criteria = probNet.getDecisionCriteria();
		
		for(int i = 0; i < criteria.size(); i++){
			Criterion criterion = criteria.get(i);
			
			if(criterion.getCriterionName().equals("")){
				type_error = IS_EMPTY_NAME;
				return false;
			} else {
				for(int j = i+1; j < criteria.size(); j++){
					if(criterion.getCriterionName().equals(criteria.get(j).getCriterionName())){
						type_error = IS_NAME_ALREADY_EXIST;
						return false;
					}
				}
			}
		}*/

	}
 
}
