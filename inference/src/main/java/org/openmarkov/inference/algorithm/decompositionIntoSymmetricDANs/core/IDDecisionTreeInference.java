/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core;

import java.util.List;

import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;

public class IDDecisionTreeInference extends DANDecisionTreeInference {
    
    
    public IDDecisionTreeInference(ProbNet probNet, boolean isCEA) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(probNet, isCEA);
	}
    
    public IDDecisionTreeInference(ProbNet probNet, boolean computeDecisionTreeForGUI, boolean isCEA) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(probNet, computeDecisionTreeForGUI,isCEA);
	}
    
    public IDDecisionTreeInference(ProbNet probNet, int treeDepth, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(probNet,treeDepth, computeDecisionTreeForGUI);
	}
    
    
    public IDDecisionTreeInference(ProbNet probNet, EvidenceCase evidenceCase, boolean isCEA) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(probNet, evidenceCase, isCEA);
	}
	
	public IDDecisionTreeInference(ProbNet probNet, int treeDepth, boolean computeDecisionTreeForGUI, boolean isCEA)
            throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(probNet, treeDepth, computeDecisionTreeForGUI, isCEA);
	}
    
    public IDDecisionTreeInference(ProbNet probNet, int treeDepth, boolean computeDecisionTreeForGUI, EvidenceCase evidenceCase) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(probNet,treeDepth, computeDecisionTreeForGUI, evidenceCase);
	}
    
    public IDDecisionTreeInference(ProbNet probNet, EvidenceCase evidenceCase, boolean computeDecisionTreeForGUI, boolean isCEA) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(probNet, evidenceCase, computeDecisionTreeForGUI,isCEA);
	}
	
	public IDDecisionTreeInference(ProbNet dan, EvidenceCase evidenceCase, int maximumTreeDepth,
                                   boolean computeDecisionTreeForGUI, boolean isCEA) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		this(dan, evidenceCase, maximumTreeDepth, 0, computeDecisionTreeForGUI, isCEA);
	}

	public IDDecisionTreeInference(ProbNet dan, EvidenceCase evidenceCase, int maximumTreeDepth, int currentTreeDepth,
                                   boolean computeDecisionTreeForGUI, boolean isCEAnalysis) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		super(dan, evidenceCase, maximumTreeDepth, currentTreeDepth, computeDecisionTreeForGUI, isCEAnalysis);
	}

	@Override
	protected List<Variable> getAlwaysObservedVariables(ProbNet network, List<Variable> conditioningVariablesList,
			EvidenceCase evidenceCase) {
		return DANOperations.getVariablesObservedFromTheBegginning(network,conditioningVariablesList,evidenceCase, false);
	}
	
	@Override protected DANDecisionTreeInference constructDecisionTreeInference(ProbNet dan, EvidenceCase evidenceCase,
                                                                                int maximumTreeDepth, int childTreeDepth) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		return new IDDecisionTreeInference(dan, evidenceCase, maximumTreeDepth, childTreeDepth, computeDecisionTreeForGUI(), isCEAnalysis);
	}

}
