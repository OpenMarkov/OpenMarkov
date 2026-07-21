/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */


package org.openmarkov.inference.algorithm.likelihoodWeighting;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.ProbNetOperations;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;



/*
 * Tests for likelihood weighting and logic sampling
 *
 * @author iagoparis - spring 2018
 * @version 1.0
 */

// Tests for LikelihoodWeighting and LogicalSampling classes. Detailed explanations in each test header.
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class StochasticPropagationTest {

	private ProbNet defaultNet;
	private double maxError; // Maximum error allowed when comparing to exact result
	private final int DEFAULT_SAMPLE_SIZE = 10000;
	private final long seed = 10071856; // Tesla was born

    private StochasticPropagation algorithm; // A place to put the algorithm instance that tests need
	public enum AlgorithmName {LIKELIHOOD_WEIGHTING, LOGIC_SAMPLING} // Algorithms implemented
	private AlgorithmName algorithmName;


	// Sets a five-node net as default and a max error for stochastic deviation.
	@BeforeEach public void setUp() {
		defaultNet = ExampleNets.bigNet();
		maxError = 2E-2;
	}

    // Tests if topological sorting is working.
    @Test
    public void testTopologicalSorting() throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
        for (int i = 0; i < 2; i++) {

            // Algorithm to use
            algorithmName = AlgorithmName.values()[i];
            //printRoundedByBars(algorithmName.name());

            algorithm = returnChosenAlgorithm(defaultNet, algorithmName);
            defaultNet.addLink(defaultNet.getNode("C"), defaultNet.getNode("B"), true);
            List<Variable> variablesToSort = new ArrayList<Variable>();
            variablesToSort.add(defaultNet.getVariable("B"));
            variablesToSort.add(defaultNet.getVariable("C"));
            variablesToSort.add(defaultNet.getVariable("D"));
            List<Variable> sortedVariables = ProbNetOperations.sortTopologically(defaultNet, variablesToSort);
            Assertions.assertEquals(sortedVariables.get(0), defaultNet.getVariable("C"));
            Assertions.assertEquals(sortedVariables.get(1), defaultNet.getVariable("B"));
            Assertions.assertEquals(sortedVariables.get(2), defaultNet.getVariable("D"));
            //System.out.println("OK");
        }
    }


	/*
	 * This subroutine applies an algorithm with "name" to a "net".
	 * It was made to allows the switching of algorithms through an enum.
	 *
	 * @param net The net suffering the algorithm.
	 * @param name The name of the algorithm.
	 * @return The algorithm instance.
	 *
	 * @throws NotEvaluableNetworkException when the algorithm is incompatible with the net.
	 */
    public StochasticPropagation returnChosenAlgorithm(ProbNet net, AlgorithmName name) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
	    switch (name) {
            case LIKELIHOOD_WEIGHTING:
                return new LikelihoodWeighting(net);
            case LOGIC_SAMPLING:
                return new LogicSampling(net);
            default:
                return new LikelihoodWeighting(net);

        }
    }


	/*
	 * Tests a net of one node. It will print the propagated potentials and assert their correctness.
	 *
	 * @throws NotEvaluableNetworkException when the algorithm is incompatible with the net.
	 * @throws IncompatibleEvidenceException when evidence can't be propagated by the algorithm.
	 */
	@Test
    public void testOneNet() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, IncompatibleEvidenceException.SamplesWeightIsZero, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {

	    for (int i = 0; i < 2; i++) {

            // Algorithm to use
            algorithmName = AlgorithmName.values()[i];
            //printRoundedByBars(algorithmName.name());

            // Net creation
            ProbNet oneNet = ExampleNets.oneNet();

            // No findings
            EvidenceCase noFindings = new EvidenceCase();

            // Propagation
            algorithm = returnChosenAlgorithm(oneNet, algorithmName);
            algorithm.setPostResolutionEvidence(noFindings);
            algorithm.setSampleSize(DEFAULT_SAMPLE_SIZE);
            algorithm.setSeed(seed);
            HashMap<Variable, TablePotential> results = algorithm.getPosteriorValues();
            printResults(algorithm);

            // Assertion
            Variable A = oneNet.getVariable("A");
            assertEquals(results.get(A).getValues()[0], 0.750001, maxError);
            assertEquals(results.get(A).getValues()[1], 0.249999, maxError);
        }

	}


	/*
	 * Tests a net of two nodes.
	 * First without evidence and then with two different evidence cases.
	 * It will print the propagated potentials and assert their correctness.
	 *
	 * @throws NotEvaluableNetworkException when the algorithm is incompatible with the net.
	 * @throws IncompatibleEvidenceException when evidence can't be propagated by the algorithm.
	 * @throws NodeNotFoundException
	 * @throws InvalidStateException when the state from the evidence is wrong.
	 */
	@Test
    public void testTwoNet() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, IncompatibleEvidenceException.SamplesWeightIsZero, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {

	    for (int i = 0; i < 2; i++) {

            // Algorithm to use
            algorithmName = AlgorithmName.values()[i];
            //printRoundedByBars(algorithmName.name());

		// Net creation
		ProbNet twoNet = ExampleNets.twoNet();


		// EVIDENCE CASES
		List<Variable> variables = twoNet.getVariables();
		ArrayList<EvidenceCase> findingCases = new ArrayList<>();

		// No findings
		EvidenceCase noFindings = new EvidenceCase();
		findingCases.add(noFindings);

		// One finding
			// Evidence: B = B1
		EvidenceCase oneFinding = new EvidenceCase();
		oneFinding.addFinding(new Finding(variables.get(1), 1));
		findingCases.add(oneFinding);

			// Evidence: A = A1
		EvidenceCase oneOtherFinding = new EvidenceCase();
		oneOtherFinding.addFinding(new Finding(variables.get(0), 1));
		findingCases.add(oneOtherFinding);

        // Propagation
        for (EvidenceCase findings : findingCases) {
            algorithm = returnChosenAlgorithm(twoNet, algorithmName);
            algorithm.setSampleSize(DEFAULT_SAMPLE_SIZE);
            algorithm.setSeed(seed);
            algorithm.setPostResolutionEvidence(findings);
            HashMap<Variable, TablePotential> results = algorithm.getPosteriorValues();
            printResults(algorithm);

            // Assertions
            if (findings.equals(noFindings)) {
                Variable A = twoNet.getVariable("A");
                assertEquals(results.get(A).getValues()[0], 0.5, maxError);
                assertEquals(results.get(A).getValues()[1], 0.5, maxError);

                Variable B = twoNet.getVariable("B");
                assertEquals(results.get(B).getValues()[0], 0.515, maxError);
                assertEquals(results.get(B).getValues()[1], 0.485, maxError);

            } else if (findings.equals(oneFinding)) {
                Variable A = twoNet.getVariable("A");
                assertEquals(results.get(A).getValues()[0], 0.072, maxError);
                assertEquals(results.get(A).getValues()[1], 0.928, maxError);

            } else if (findings.equals(oneOtherFinding)) {

                Variable B = twoNet.getVariable("B");
                assertEquals(results.get(B).getValues()[0], 0.1, maxError);
                assertEquals(results.get(B).getValues()[1], 0.9, maxError);

            }
        }


		}

	}


	/*
	 * Tests a net of five nodes. Check its structure in the links section of ExampleNets.bigNet().
	 * First without evidence and then with three different evidence cases, varying the number of findings.
	 * It will print the propagated potentials and assert their correctness.
	 *
	 * @throws NotEvaluableNetworkException when the algorithm is incompatible with the net.
	 * @throws IncompatibleEvidenceException when evidence can't be propagated by the algorithm.
	 * @throws InvalidStateException when the state from the evidence is wrong.
	 */
    @Tag(TestSpeed.MEDIUM)
	@Test
    public void testBigNet() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, IncompatibleEvidenceException.SamplesWeightIsZero, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {

        for (int i = 0; i < 2; i++) {

            // Algorithm to use
            algorithmName = AlgorithmName.values()[i];
            //printRoundedByBars(algorithmName.name());

		// Net creation
		ProbNet bigNet = ExampleNets.bigNet();


		// EVIDENCE CASES
		List<Variable> variables = bigNet.getVariables();
		ArrayList<EvidenceCase> findingCases = new ArrayList<>();

		// No findings
		EvidenceCase noFindings = new EvidenceCase();
		findingCases.add(noFindings);

		// One finding: A = A1
		EvidenceCase oneFinding = new EvidenceCase();
		oneFinding.addFinding(new Finding(variables.get(0), 1));
		findingCases.add(oneFinding);

		// Two findings: D = D1, E = E0
		EvidenceCase twoFindings = new EvidenceCase();
		twoFindings.addFinding(new Finding(variables.get(3), 1));
		twoFindings.addFinding(new Finding(variables.get(4), 0));
		findingCases.add(twoFindings);

		// Three findings: A = A1, C = C0, E = E0
		EvidenceCase threeFindings = new EvidenceCase();
		threeFindings.addFinding(new Finding(variables.get(0), 1));
		threeFindings.addFinding(new Finding(variables.get(2), 0));
		threeFindings.addFinding(new Finding(variables.get(4), 0));
		findingCases.add(threeFindings);

		// Propagation
        for (EvidenceCase findings : findingCases) {
            algorithm = returnChosenAlgorithm(bigNet, algorithmName);
            algorithm.setPostResolutionEvidence(findings);
            algorithm.setSampleSize(DEFAULT_SAMPLE_SIZE);
            algorithm.setSeed(seed);
            HashMap<Variable, TablePotential> results = algorithm.getPosteriorValues();
            printResults(algorithm);

            // Assertions
            if (findings.equals(noFindings)) {
                Variable A = bigNet.getVariable("A");
                assertEquals(results.get(A).getValues()[0], 0.5, maxError);
                assertEquals(results.get(A).getValues()[1], 0.5, maxError);

                Variable B = bigNet.getVariable("B");
                assertEquals(results.get(B).getValues()[0], 0.55, maxError);
                assertEquals(results.get(B).getValues()[1], 0.45, maxError);

                Variable C = bigNet.getVariable("C");
                assertEquals(results.get(C).getValues()[0], 0.575, maxError);
                assertEquals(results.get(C).getValues()[1], 0.425, maxError);

                Variable D = bigNet.getVariable("D");
                assertEquals(results.get(D).getValues()[0], 0.625, maxError);
                assertEquals(results.get(D).getValues()[1], 0.375, maxError);

                Variable E = bigNet.getVariable("E");
                assertEquals(results.get(E).getValues()[0], 0.601, maxError);
                assertEquals(results.get(E).getValues()[1], 0.399, maxError);

            } else if (findings.equals(oneFinding)) {
                Variable B = bigNet.getVariable("B");
                assertEquals(results.get(B).getValues()[0], 0.3, maxError);
                assertEquals(results.get(B).getValues()[1], 0.7, maxError);

                Variable C = bigNet.getVariable("C");
                assertEquals(results.get(C).getValues()[0], 0.4, maxError);
                assertEquals(results.get(C).getValues()[1], 0.6, maxError);

                Variable D = bigNet.getVariable("D");
                assertEquals(results.get(D).getValues()[0], 0.757, maxError);
                assertEquals(results.get(D).getValues()[1], 0.243, maxError);

                Variable E = bigNet.getVariable("E");
                assertEquals(results.get(E).getValues()[0], 0.54, maxError);
                assertEquals(results.get(E).getValues()[1], 0.46, maxError);


            } else if (findings.equals(twoFindings)) {
                Variable A = bigNet.getVariable("A");
                assertEquals(results.get(A).getValues()[0], 0.728, maxError);
                assertEquals(results.get(A).getValues()[1], 0.272, maxError);

                Variable B = bigNet.getVariable("B");
                assertEquals(results.get(B).getValues()[0], 0.951, maxError);
                assertEquals(results.get(B).getValues()[1], 0.049, maxError);

                Variable C = bigNet.getVariable("C");
                assertEquals(results.get(C).getValues()[0], 0.723, maxError);
                assertEquals(results.get(C).getValues()[1], 0.277, maxError);

            } else if (findings.equals(threeFindings)) {
                Variable B = bigNet.getVariable("B");
                assertEquals(results.get(B).getValues()[0], 0.3, maxError);
                assertEquals(results.get(B).getValues()[1], 0.7, maxError);

                Variable D = bigNet.getVariable("D");
                assertEquals(results.get(D).getValues()[0], 0.813, maxError);
                assertEquals(results.get(D).getValues()[1], 0.187, maxError);

            }
        }
        }

	}


	/*
	 * Tests the Asia net, a net about diseases that you may have brought from Asia.
	 * Check its structure in the links section of ExampleNets.asiaNet().
	 *
	 * There are a test without evidence and then with one evidence case.
	 * It will print the propagated potentials and assert their correctness.
	 *
	 * @throws NotEvaluableNetworkException when the algorithm is incompatible with the net.
	 * @throws IncompatibleEvidenceException when evidence can't be propagated by the algorithm.
	 * @throws InvalidStateException when the state from the evidence is wrong.
	 */
    @Tag(TestSpeed.MEDIUM)
	@Test
    public void testAsiaNet() throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException {

        for (int i = 0; i < 2; i++) {

            // Algorithm to use
            algorithmName = AlgorithmName.values()[i];
            //printRoundedByBars(algorithmName.name());

            // Net creation
            ProbNet asiaNet = ExampleNets.buildBN_asia_java();

            // No findings
            EvidenceCase noFindings = new EvidenceCase();

            // Because of the assertions, each algorithm run must be explicitly before
            algorithm = returnChosenAlgorithm(asiaNet, algorithmName);
            algorithm.setPostResolutionEvidence(noFindings);
            algorithm.setSampleSize(DEFAULT_SAMPLE_SIZE);
            algorithm.setSeed(seed);
            HashMap<Variable, TablePotential> results = algorithm.getPosteriorValues();
            printResults(algorithm);

            // Check probabilities

            // AsiaNet variables for reference
            List<Variable> variables = asiaNet.getVariables();

            Variable A = asiaNet.getVariable("VisitToAsia");
            TablePotential tpA = results.get(A);
            assertEquals(tpA.getValues()[0], 0.9900, maxError);
            assertEquals(tpA.getValues()[1], 0.0100, maxError);

            Variable S = asiaNet.getVariable("Smoker");
            TablePotential tpS = results.get(S);
            assertEquals(tpS.getValues()[0], 0.5, maxError);
            assertEquals(tpS.getValues()[1], 0.5, maxError);

            Variable T = asiaNet.getVariable("Tuberculosis");
            TablePotential tpT = results.get(T);
            assertEquals(tpT.getValues()[0], 0.9896, maxError);
            assertEquals(tpT.getValues()[1], 0.0104, maxError);

            Variable L = asiaNet.getVariable("LungCancer");
            TablePotential tpL = results.get(L);
            assertEquals(tpL.getValues()[0], 0.945, maxError);
            assertEquals(tpL.getValues()[1], 0.055, maxError);

            Variable B = asiaNet.getVariable("Bronchitis");
            TablePotential tpB = results.get(B);
            assertEquals(tpB.getValues()[0], 0.55, maxError);
            assertEquals(tpB.getValues()[1], 0.45, maxError);

            Variable E = asiaNet.getVariable("TuberculosisOrCancer");
            TablePotential tpE = results.get(E);
            assertEquals(tpE.getValues()[0], 0.935172, maxError);
            assertEquals(tpE.getValues()[1], 0.064828, maxError);

            Variable X = asiaNet.getVariable("X-ray");
            TablePotential tpX = results.get(X);
            assertEquals(tpX.getValues()[0], 0.8897096, maxError);
            assertEquals(tpX.getValues()[1], 0.1102904, maxError);

            Variable D = asiaNet.getVariable("Dyspnea");
            TablePotential tpD = results.get(D);
            assertEquals(tpD.getValues()[0], 0.5640294, maxError);
            assertEquals(tpD.getValues()[1], 0.4359706, maxError);


            // One finding: X_Ray = yes
            EvidenceCase oneFinding = new EvidenceCase();
            oneFinding.addFinding(new Finding(X, X.getStateIndex("yes")));

            algorithm = returnChosenAlgorithm(asiaNet, algorithmName);
            algorithm.setPreResolutionEvidence(oneFinding);
            algorithm.setSampleSize(DEFAULT_SAMPLE_SIZE);
            algorithm.setSeed(seed);
            results = algorithm.getPosteriorValues();
            printResults(algorithm);

            // Check probabilities

            // Variable A: "VisitToAsia"
            tpA = results.get(A);
            assertEquals(tpA.getValues()[0], 0.9868, maxError);
            assertEquals(tpA.getValues()[1], 0.0132, maxError);

            // Variable S: "Smoker"
            tpS = results.get(S);
            assertEquals(tpS.getValues()[0], 0.3122, maxError);
            assertEquals(tpS.getValues()[1], 0.6878, maxError);

            // Variable T: "Tuberculosis"
            tpT = results.get(T);
            assertEquals(tpT.getValues()[0], 0.9076, maxError);
            assertEquals(tpT.getValues()[1], 0.0924, maxError);

            // Variable L: "LungCancer"
            tpL = results.get(L);
            assertEquals(tpL.getValues()[0], 0.5113, maxError);
            assertEquals(tpL.getValues()[1], 0.4887, maxError);

            // Variable B: "Bronchitis"
            tpB = results.get(B);
            assertEquals(tpB.getValues()[0], 0.4937, maxError);
            assertEquals(tpB.getValues()[1], 0.5063, maxError);

            // Variable E: "TuberculosisOrCancer"
            tpE = results.get(E);
            assertEquals(tpE.getValues()[0], 0.4240, maxError);
            assertEquals(tpE.getValues()[1], 0.5760, maxError);

            // Variable D: "Dyspnea"
            tpD = results.get(D);
            assertEquals(tpD.getValues()[0], 0.3592, maxError);
            assertEquals(tpD.getValues()[1], 0.6408, maxError);
        }



	}

    // Nicely print to standard output the posterior values
	private void printResults(StochasticPropagation algorithm) {

		// Nested class to compare variable's names and order them
		class VariableNameOrder implements Comparator<Variable> {
			@Override public int compare(Variable v1, Variable v2) {
                String s1 = v1.getName().toLowerCase(); // Convert to lower case.
                String s2 = v2.getName().toLowerCase();
				return s1.compareTo(s2); // Compare lower-case Strings.
			}
		}

		// Ordering
		TreeMap<Variable, TablePotential> orderedPropagationResult = new TreeMap<>(new VariableNameOrder());
		orderedPropagationResult.putAll(algorithm.getLastPosteriorValues());
		// Put evidence variables
        for (Variable variable : algorithm.fusedEvidence.getVariables()) {

            TablePotential potential = new TablePotential(PotentialRole.JOINT_PROBABILITY, variable);
            int finding = algorithm.fusedEvidence.getState(variable);

            double[] potentialValues = new double[variable.getStates().length];
            potentialValues[finding] = 1;
            potential.setValues(potentialValues);

            orderedPropagationResult.put(variable, potential);
        }

		// Printing
        //System.out.println("Samples: " + algorithm.getSampleSize());
        //System.out.println();

		for (Map.Entry<Variable, TablePotential> node : orderedPropagationResult.entrySet()) {
            
            String key = node.getKey().getName();
			String value = node.getValue().toString();
            //System.out.println(key + "   " + value);

		}
        //System.out.println();
	}

    /*
     * Prints a string like:
     * ----------
     * - string -
     * ----------
     */
    public void printRoundedByBars(String string) {

        // Top bar
        for (int i = 0; i < string.length() + 3; i++) {
            System.out.print("-");
        }
        System.out.println("-");

        // String
        System.out.println("- " + string + " -");

        // Bottom bar
        for (int i = 0; i < string.length() + 3; i++) {
            System.out.print("-");
        }
        System.out.println("-");
    }

}
