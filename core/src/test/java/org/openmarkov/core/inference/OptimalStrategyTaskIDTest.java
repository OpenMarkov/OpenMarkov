/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

public abstract class OptimalStrategyTaskIDTest extends OptimalStrategyTaskDecTest {

    // TODO: Many of the code written in this test class reference code no longer existing
    /*
    
	protected void testOptimalStrategy(ProbNet net,Intervention expectedStrategy) throws IncompatibleEvidenceException, InvalidStateException {
		OptimalStrategy algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(net);
		testScenariosIntervention(net,algorithm);
	}


	
	// Checks that the Intervention (optimal strategy) obtained from the evaluation optimal is not null
	// and that it is consistent with the Cooper policy network (CPN) built using the policies obtained
	// from the method getOptimizedPolicies
	
	private void testScenariosIntervention(ProbNet net,
										   OptimalStrategy algorithm) throws IncompatibleEvidenceException, InvalidStateException {
        Intervention interv = algorithm.getOptimalIntervention();
		assertNotNull(interv);
		testIntervention(algorithm, interv, new EvidenceCase());
	}

	
	// Checks that the Intervention 'interv' rooted at the evidence scenario 'parentEvi' is consistent with the
	// Cooper policy network (CPN) that has been obtained by the inference algorithm 'algorithm'
	// The  method checks correctness and completeness:
	// - Correctness: Every scenario in the intervention has non-zero probability in the CPN
	// - Completeness: The Intervention covers all the non-zero probability states of the CPN
	private void testIntervention(OptimalStrategy algorithm, Intervention interv, EvidenceCase parentEvi)
            throws IncompatibleEvidenceException, InvalidStateException {

		if (interv != null) {
			interv.getRootVariable();
			List<TreeADDBranch> branches = interv.getBranches();
			Variable rootVariable = interv.getRootVariable();
			algorithm.setPostResolutionEvidence(parentEvi);
			List<Variable> interestVariables = new ArrayList<>();
			interestVariables.add(rootVariable);
			//TablePotential probs = algorithm.getProbsAndUtilities().get(rootVariable);
			TablePotential probs = algorithm.getProbability();
			if (branches != null) {
				// Check that the number of branches is equal to the non-zero
				// probability states
				assertEquals(getNumStatesBranches(branches), getNumProbsNotZero(probs));
				for (int i = 0; i < branches.size(); i++) {
					TreeADDBranch auxBranch = branches.get(i);
					Intervention auxInterventionBranch = Intervention.getInterventionBranch(auxBranch);
					for (State state : auxBranch.getStates()) {
						// Check that 'state' has non-zero probability in the
						// CPN
						assertTrue(probs.getValues()[rootVariable.getStateIndex(state)] > 0);
						EvidenceCase newEvi = new EvidenceCase(parentEvi.getFindings());
						Finding finding = new Finding(rootVariable, state);
							newEvi.addFinding(finding);
						testIntervention(algorithm, auxInterventionBranch, newEvi);
					}
				}
			}
		}
	}

	// @return The total number of states in 'branches'

	private int getNumStatesBranches(List<TreeADDBranch> branches) {
		int numStates = 0;
		Set<State> states = new HashSet<>();
		if (branches != null){
			for (int i = 0; i < branches.size(); i++) {
				TreeADDBranch auxBranch = branches.get(i);
				if (auxBranch != null){
					states.addAll(auxBranch.getStates());
				}
			}
		}
		numStates = states.size();
		return numStates;
	}

	// @return The number of values in the potential that are greater than zero
	private int getNumProbsNotZero(TablePotential probs) {
		int numNotZero = 0;
		double[] values = probs.getValues();
		for (double value : values) {
			if (value > 0.0) {
				numNotZero = numNotZero + 1;
			}
		}
		return numNotZero;
	}

	// @return An Intervention with the assignment 'decision = state'
	protected Intervention createSimpleIntervention(ProbNet id, String decision, String state) throws NodeNotFoundException, InvalidStateException {
		Intervention interv;
		List<Variable> vars = new ArrayList<>();
		List<State> states = new ArrayList<>();
		Variable dec = null;
			dec = id.getVariable(decision);
		vars.add(dec);
		interv = new Intervention(dec, states);
		interv.setRootVariable(dec);
			states.add(dec.getState(state));
		TreeADDBranch branch = new TreeADDBranch(states, dec, vars);
		interv.addBranch(branch);
		return interv;
	}

	protected Intervention getStrategyDiagnosisProblem(ProbNet id,
													   String resultTestName,
													   String decisionName,
													   String positiveResult,
													   String negativeResult,
													   String yesTherapy,
													   String noTherapy) throws InvalidStateException, NodeNotFoundException {
		Intervention interv;
		List<Variable> vars = new ArrayList<>();
		List<State> states = new ArrayList<>();
        String statesResultTestNames[] = new String[2];
		String statesTherapyNames[]=new String[2];

		statesResultTestNames[0]=positiveResult;
		statesResultTestNames[1]=negativeResult;
		statesTherapyNames[0]=yesTherapy;
		statesTherapyNames[1]=noTherapy;
        Variable dec = id.getVariable(decisionName);
        Variable resultTest = id.getVariable(resultTestName);

		vars.add(dec);
		vars.add(resultTest);
		List<State> statesRoot = new ArrayList<>();
		for (String nameState:statesResultTestNames){
			statesRoot.add(resultTest.getState(nameState));
		}

		List<Intervention> interventionsChildren;

		interventionsChildren = new ArrayList<>();
		for (String nameState:statesTherapyNames){
			interventionsChildren.add(createSimpleIntervention(id,decisionName,nameState));
		}


		interv = new Intervention(resultTest, statesRoot, interventionsChildren);

		return interv;
	}

	protected void testMEU(ProbNet diagram,double expectedMeu) throws IncompatibleEvidenceException{
		OptimalStrategy algorithm = buildInferenceTaskAndSkipTestIfNotEvaluable(diagram);
		// test max expected utility
        Double meuEvaluation = algorithm.getGlobalUtility().getValues()[0];
		assertEquals(expectedMeu, meuEvaluation, maxError);
	}

	@Test
	public void testIDOneDecision() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDOneDecision(), null);
	}

	@Test
	public void testIDPerfectKnowledge() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDPerfectKnowledge(), null);
	}

	@Test
	public void testIDPerfectKnowledgeCostTherapy() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDPerfectKnowledgeCostTherapy(), null);
	}

	@Test
	public void testIDNoKnowledge() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDNoKnowledge(), null);
	}

	@Test
	public void testIDTestAlways() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDTestAlways(), null);
	}

	@Test
	public void testIDDecideTest() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(getIDDecideTest(), null);
	}

	protected ProbNet getIDDecideTest() {
		return IDFactory.buildIDDecideTest();
	}

	@Test
	public void testIDDecideTestSymptom() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDDecideTestSymptom(), null);
	}

	@Test
	public void testIDQaleMediastinet() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDQaleMediastinet(), null);
	}

	@Test
	public void testIDMediastinetWithoutSV() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDMediastinetWithoutSV(), null);
	}

	@Test
	public void testIDMediastinetWithoutMediastinoscopy() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDMediastinetWithoutMediastinoscopy(), null);
	}

	@Test
	public void testIDMediastinet() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDMediastinet(), null);
	}

	@Test
	public void testIDArthronet() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDArthronet(), null);
	}

	@Test
	public void testIDRedundantChance() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDRedundantChance(), null);
	}

	@Test
	public void testIDTwoIndependentDecisions() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDTwoIndependentDecisions(), null);
	}

	@Test
	public void testIDConcatenateOrderTwoDecisions() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDConcatenateOrderTwoDecisions(), null);
	}

	@Test
	public void testIDThreeIndependentDecisions() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDThreeIndependentDecisions(), null);
	}

	@Test
	public void testIDStatesTies() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDStatesTies(), null);
	}

	@Test
	public void testIDStatesTiesPerfectKnowledge() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDStatesTiesPerfectKnowledge(), null);
	}

	@Test
	public void testIDConsecutiveDecisions() throws IncompatibleEvidenceException, InvalidStateException {
		testOptimalStrategy(IDFactory.buildIDConsecutiveDecisions(), null);
	}

	// @return An InferenceAlgorithm for 'network'. If the network is not evaluable
	// with the algorithm then the test calling this method is skipped.

	protected OptimalStrategy buildInferenceTaskAndSkipTestIfNotEvaluable(
			ProbNet network) {
        OptimalStrategy task;
		//If the network is not evaluable then the test is skipped
        boolean isEvaluable = true;
		try {
			task = buildInferenceTask(network, new EvidenceCase());
		} catch (NotEvaluableNetworkException | IncompatibleEvidenceException | UnexpectedInferenceException e1) {
			isEvaluable = false;
		}
		assumeTrue(isEvaluable);
		return task;
	}
    */
}