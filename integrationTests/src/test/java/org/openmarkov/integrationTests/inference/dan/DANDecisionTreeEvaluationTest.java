/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.dan;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.*;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANDecisionTreeEvaluation;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.evaluation.DANEvaluation;
//@Ignore
public class DANDecisionTreeEvaluationTest extends DANEvaluationTest {
    
    @Override
    public void testNetworkEvaluation(ProbNet network, double expectedEU, String... namesVariablesIntervention) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		testNetworkEvaluationAndDecisionTree(network,expectedEU,namesVariablesIntervention);
	}
    
    @Override
    protected DANEvaluation buildNetworkEvaluation(ProbNet network, boolean computeDecisionTreeForGUI) throws NotEvaluableNetworkException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
        return new DANDecisionTreeEvaluation(network, computeDecisionTreeForGUI);
	}

	//Next tests are commented because running them very often takes too much time
	@Override @Disabled("Old DAN evaluation") @Test public void testDANKingNobleDescentYesFirstTask1() {
		//testDANEvaluation("king-noble-descent-yes-first-task-1",9.03);

	}

	@Override @Disabled("Old DAN evaluation") @Test public void testDANKingNobleDescentYesFirstTask1SecondTask2() {
		//testDANEvaluation("king-noble-descent-yes-first-task-1-second-task-2",9.03);
	}

	@Override @Disabled("Old DAN evaluation") @Test public void testDANKingNobleDescentNo() {
		//testDANEvaluation("king-noble-descent-no",6.43);

	}

	@Override @Disabled("Old DAN evaluation") @Test public void testDANKingNobleDescentYes() {
		//testDANEvaluation("king-noble-descent-yes",9.03);
	}

	@Override @Disabled("Old DAN evaluation") @Test public void testDANKing() {
		//testDANEvaluation("king",7.73);
	}





	@Override
	protected DANEvaluation buildNetworkEvaluation(ProbNet network) {
		// TODO Auto-generated method stub
		return null;
	}

	
	/*	
	@Test
	public void testDANKing() throws IncompatibleEvidenceException, NodeNotFoundException, NotEvaluableNetworkException{
		//TODO
		//This test works now with DANDecisionTreeEvaluation, but it takes 52 seconds in the core i7 laptop borrowed from Miguel.
		//That's why I have decided to comment it
		//testDANEvaluation("king",7.73);
	}
	*/
	

}
