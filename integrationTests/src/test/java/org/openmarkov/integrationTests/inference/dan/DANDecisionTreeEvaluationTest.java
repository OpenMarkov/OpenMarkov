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

import java.io.IOException;
import java.net.URISyntaxException;

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

	// The decision tree for the window adds nothing to the arithmetic, which is the same without it: these two networks,
	// most of the time of this class, are evaluated once, with the tree, and the others with it and without it
	@Override @Test public void testDANKingNobleDescentYesFirstTask1() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		testNetworkEvaluationAndDecisionTree(loadNetwork("king-noble-descent-yes-first-task-1"), true, 9.03);
	}

	@Override @Test public void testDANKingNobleDescentYesFirstTask1SecondTask2() throws NotEvaluableNetworkException, ProbNetParserException, URISyntaxException, IOException, IncompatibleEvidenceException, NonProjectablePotentialException, PotentialOperationException.DifferentSizesInPotentialsAndStates {
		testNetworkEvaluationAndDecisionTree(loadNetwork("king-noble-descent-yes-first-task-1-second-task-2"), true, 9.03);
	}

	//Next tests are commented because running them very often takes too much time
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
