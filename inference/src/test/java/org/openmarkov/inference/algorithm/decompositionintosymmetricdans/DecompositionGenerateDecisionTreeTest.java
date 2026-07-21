package org.openmarkov.inference.algorithm.decompositionintosymmetricdans;

import org.junit.jupiter.api.Test;

import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.DecompositionGenerateDecisionTree;

public class DecompositionGenerateDecisionTreeTest {
	
	
	@Test
	public void testOnlyTwoUtilitySV() {
			
		testNetworkEvaluation("only-two-utility-sv", 5);
	}

	private void testNetworkEvaluation(String string, double eu) {
		// TODO Auto-generated method stub
		DecompositionGenerateDecisionTree eval;
		
	}

}
