/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.huginPropagation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.CannotNormalizePotentialException;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.UniformPotential;
import org.openmarkov.inference.algorithm.huginPropagation.ClusterPropagation.StorageLevel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Manuel Arias
 */

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class HuginPropagationTest {

	private final double maxError = 0.0001;

    private ProbNet bigNet;
    private ArrayList<Variable> variables;

	@BeforeEach public void setUp() {
	    bigNet = buildTestNet();
    }
    
    
    public InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		HuginPropagation algorithm = null;
		algorithm = new HuginPropagation(probNet);
		return algorithm;
	}

    private ProbNet buildTestNet() {
        ProbNet testNet = new ProbNet();
        variables = new ArrayList<Variable>();
        variables.add(new Variable("A", 2));
        variables.add(new Variable("B", 2));
        variables.add(new Variable("C", 2));
        variables.add(new Variable("D", 2));
        variables.add(new Variable("E", 2));
        for (Variable variable : variables) {
            testNet.addNode(variable, NodeType.CHANCE);
        }

        testNet.addLink(testNet.getNode("A"), testNet.getNode("B"), true);
        testNet.addLink(testNet.getNode("A"), testNet.getNode("C"), true);
        testNet.addLink(testNet.getNode("B"), testNet.getNode("D"), true);
        testNet.addLink(testNet.getNode("B"), testNet.getNode("E"), true);
        testNet.addLink(testNet.getNode("C"), testNet.getNode("D"), true);
        // A
        UniformPotential aPotential = new UniformPotential(PotentialRole.JOINT_PROBABILITY, variables.get(0));
        testNet.addPotential(aPotential);
        // A -> B
        TablePotential abPotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(1),
                variables.get(0));
        abPotential.setValues(new double[] { 0.7, 0.3, 0.2, 0.8 }); // States (a,b) -> +a+b, +a-b, -a+b, -a-b
        testNet.addPotential(abPotential);
        // A -> C
        TablePotential acPotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(2),
                variables.get(0));
        acPotential.setValues(new double[] { 0.6, 0.4, 0.25, 0.75 });
        testNet.addPotential(acPotential);
        // B, C -> D
        TablePotential bcdPotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(3),
                variables.get(2), variables.get(1)); // Variable of the node, variable of second father
        // variable of first father (the highest in OpenMarkov table). It goes backwards and you have to fill
        // OpenMarkov tables backwards too.

        // In a tree they would be b<c<d. < symbolizes branching direction
        // States (b,c,d) -> 	+b+c+d, +b+c-d, +b-c+d, +b-c-d
        //						-b+c+d, -b+c-d, -b-c+d, -b-c-d
        // All this comments are to understand the order of the nest values
        bcdPotential.setValues(new double[] { 0.1, 0.9, 0.01, 0.99, 0.7, 0.3, 0.6, 0.4 });
        testNet.addPotential(bcdPotential);
        // C -> E
        TablePotential cePotential = new TablePotential(PotentialRole.CONDITIONAL_PROBABILITY, variables.get(4),
                variables.get(2));
        cePotential.setValues(new double[] { 0.6, 0.4, 0.25, 0.75 });
        testNet.addPotential(cePotential);
        return testNet;
    }
    
    @Test
    public void testHuginBigNet() throws Exception {
        ProbNet probNet;
        HashMap<Variable, TablePotential> posteriorProbabilities;

        // Two findings: D = D1, E = E0
        EvidenceCase twoFindings = new EvidenceCase();
        twoFindings.addFinding(new Finding(variables.get(3), 1));
        twoFindings.addFinding(new Finding(variables.get(4), 0));

        for (StorageLevel storage : StorageLevel.values()) {
            // other test: bigNet
            // gets a priori probabilities


            posteriorProbabilities = null;
            ClusterPropagation propagation = new HuginPropagation(bigNet);
            ((HuginPropagation) propagation).setStorageLevel(storage);
            propagation.compilePriorPotentials();

            propagation.setPreResolutionEvidence(twoFindings);

            posteriorProbabilities = propagation.getPosteriorValues();
            
            //printPosteriorProbabilities(posteriorProbabilities);
        }
    }

    private void printPosteriorProbabilities(HashMap<Variable,TablePotential> posteriorProbabilities) {

        // Nested class to compare variable's names
        class VariableNameOrder implements Comparator<Variable> {
            @Override public int compare(Variable v1, Variable v2) {
                String s1 = v1.getName().toLowerCase(); // Convert to lower case.
                String s2 = v2.getName().toLowerCase();
                return s1.compareTo(s2); // Compare lower-case Strings.
            }
        }

        TreeMap<Variable, TablePotential> orderedLwInferenceResult =
                new TreeMap<Variable, TablePotential>( new VariableNameOrder());
        orderedLwInferenceResult.putAll(posteriorProbabilities);

        // Printing
        for (Map.Entry<Variable, TablePotential> node : orderedLwInferenceResult.entrySet()) {
            
            String key = node.getKey().getName();
            String value = node.getValue().toString();
            System.out.println(key + " " + value);

        }

        System.out.println();

    }
    
    @Test
    public void testHuginNoEvidence() throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException, CannotNormalizePotentialException {
            ProbNet probNet;
            HashMap<Variable, TablePotential> posteriorProbabilities;

            for (StorageLevel storage : StorageLevel.values()) {
                // first test: asia.elv
                // gets a priori probabilities
                probNet = BNAsia.buildBN_asia_java();

                List<List<Variable>> variables = new ArrayList<List<Variable>>();
                variables.add(probNet.getChanceAndDecisionVariables());

                posteriorProbabilities = null;
                ClusterPropagation propagation = new HuginPropagation(probNet);
                ((HuginPropagation) propagation).setStorageLevel(storage);
                propagation.compilePriorPotentials();

                propagation.setPreResolutionEvidence(new EvidenceCase());

                posteriorProbabilities = propagation.getPosteriorValues();
                // test probabilities
                Variable A = probNet.getVariable("VisitToAsia");
                TablePotential tpA = posteriorProbabilities.get(A);
                assertEquals(tpA.getValues()[0], 0.9900, maxError);
                assertEquals(tpA.getValues()[1], 0.0100, maxError);

                Variable S = probNet.getVariable("Smoker");
                TablePotential tpS = posteriorProbabilities.get(S);
                assertEquals(tpS.getValues()[0], 0.5, maxError);
                assertEquals(tpS.getValues()[1], 0.5, maxError);

                Variable T = probNet.getVariable("Tuberculosis");
                TablePotential tpT = posteriorProbabilities.get(T);
                assertEquals(tpT.getValues()[0], 0.9896, maxError);
                assertEquals(tpT.getValues()[1], 0.0104, maxError);

                Variable L = probNet.getVariable("LungCancer");
                TablePotential tpL = posteriorProbabilities.get(L);
                assertEquals(tpL.getValues()[0], 0.945, maxError);
                assertEquals(tpL.getValues()[1], 0.055, maxError);

                Variable B = probNet.getVariable("Bronchitis");
                TablePotential tpB = posteriorProbabilities.get(B);
                assertEquals(tpB.getValues()[0], 0.55, maxError);
                assertEquals(tpB.getValues()[1], 0.45, maxError);

                Variable E = probNet.getVariable("TuberculosisOrCancer");
                TablePotential tpE = posteriorProbabilities.get(E);
                assertEquals(tpE.getValues()[0], 0.935172, maxError);
                assertEquals(tpE.getValues()[1], 0.064828, maxError);

                Variable X = probNet.getVariable("X-ray");
                TablePotential tpX = posteriorProbabilities.get(X);
                assertEquals(tpX.getValues()[0], 0.8897096, maxError);
                assertEquals(tpX.getValues()[1], 0.1102904, maxError);

                Variable D = probNet.getVariable("Dyspnea");
                TablePotential tpD = posteriorProbabilities.get(D);
                assertEquals(tpD.getValues()[0], 0.5640294, maxError);
                assertEquals(tpD.getValues()[1], 0.4359706, maxError);
            }
        }
    
    @Test
    public void testHuginWithEvidence() throws IncompatibleEvidenceException, NotEvaluableNetworkException.NotApplicableNetwork, NonProjectablePotentialException, ConstraintViolatedException, CannotNormalizePotentialException {
            ProbNet probNet;
            HashMap<Variable, TablePotential> posteriorProbabilities;
            for (StorageLevel storage : StorageLevel.values()) {
                // first test: asia.elv
                // gets a priori probabilities
                probNet = BNAsia.buildBN_asia_java();

                Variable X = probNet.getVariable("X-ray");
                EvidenceCase evidence = new EvidenceCase();
                evidence.addFinding(new Finding(X, X.getStateIndex("yes")));

                List<List<Variable>> variables = new ArrayList<List<Variable>>();
                variables.add(probNet.getChanceAndDecisionVariables());

                posteriorProbabilities = null;
                ClusterPropagation propagation = new HuginPropagation(probNet);
                ((HuginPropagation) propagation).setStorageLevel(storage);
                propagation.compilePriorPotentials();
                propagation.setPreResolutionEvidence(evidence);


                posteriorProbabilities = propagation.getPosteriorValues();
                // test probabilities
                Variable A = probNet.getVariable("VisitToAsia");
                TablePotential tpA = posteriorProbabilities.get(A);
                assertEquals(tpA.getValues()[0], 0.9868, maxError);
                assertEquals(tpA.getValues()[1], 0.0132, maxError);

                Variable S = probNet.getVariable("Smoker");
                TablePotential tpS = posteriorProbabilities.get(S);
                assertEquals(tpS.getValues()[0], 0.3122, maxError);
                assertEquals(tpS.getValues()[1], 0.6878, maxError);

                Variable T = probNet.getVariable("Tuberculosis");
                TablePotential tpT = posteriorProbabilities.get(T);
                assertEquals(tpT.getValues()[0], 0.9076, maxError);
                assertEquals(tpT.getValues()[1], 0.0924, maxError);

                Variable L = probNet.getVariable("LungCancer");
                TablePotential tpL = posteriorProbabilities.get(L);
                assertEquals(tpL.getValues()[0], 0.5113, maxError);
                assertEquals(tpL.getValues()[1], 0.4887, maxError);

                Variable B = probNet.getVariable("Bronchitis");
                TablePotential tpB = posteriorProbabilities.get(B);
                assertEquals(tpB.getValues()[0], 0.4937, maxError);
                assertEquals(tpB.getValues()[1], 0.5063, maxError);

                Variable E = probNet.getVariable("TuberculosisOrCancer");
                TablePotential tpE = posteriorProbabilities.get(E);
                assertEquals(tpE.getValues()[0], 0.4240, maxError);
                assertEquals(tpE.getValues()[1], 0.5760, maxError);

                Variable D = probNet.getVariable("Dyspnea");
                TablePotential tpD = posteriorProbabilities.get(D);
                assertEquals(tpD.getValues()[0], 0.3592, maxError);
                assertEquals(tpD.getValues()[1], 0.6408, maxError);
            }
        }
    }

