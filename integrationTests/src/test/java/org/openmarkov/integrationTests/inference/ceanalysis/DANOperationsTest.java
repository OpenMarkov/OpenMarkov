/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.integrationTests.inference.ceanalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.ProbNetInfo;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.decompositionIntoSymmetricDANs.core.DANOperations;
import org.openmarkov.io.probmodel.reader.PGMXReader_0_2;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class DANOperationsTest {
    
    private ProbNet danTest2Therapies;
    private ProbNet danDecideTest2TherapiesNoCost;
    private ProbNet danDecideTest2TherapiesNoCostSymmetrized;
    private ProbNet danDiabetes;
    private ProbNet danDiabetesCE;
    
    @BeforeEach public void setUp() throws URISyntaxException, ProbNetParserException, IOException {
        PGMXReader_0_2 pgmxReader = new PGMXReader_0_2();
        
        ProbNetInfo probNetInfo;
        
        // Initialize danTest2Therapies network
        String networkName = "networks/dan/DAN-test-2therapies.pgmx";
        URL res = getClass().getClassLoader().getResource(networkName);
        File f = Paths.get(res.toURI()).toFile();
        String absolutePath = f.getAbsolutePath();
        danTest2Therapies = pgmxReader.read(new File(absolutePath).toURI().toURL()).probNet();
        
        // Initialize danDecideTest2TherapiesNoCost network
        networkName = "networks/dan/DAN-decide-test-2therapies-no-cost.pgmx";
        res = getClass().getClassLoader().getResource(networkName);
        f = Paths.get(res.toURI()).toFile();
        absolutePath = f.getAbsolutePath();
        danDecideTest2TherapiesNoCost = pgmxReader.read(new File(absolutePath).toURI().toURL()).probNet();
        
        // Initialize danDecideTest2TherapiesNoCost network
        networkName = "networks/dan/DAN-decide-test-2therapies-no-cost-symmetrized.pgmx";
        res = getClass().getClassLoader().getResource(networkName);
        f = Paths.get(res.toURI()).toFile();
        absolutePath = f.getAbsolutePath();
        danDecideTest2TherapiesNoCostSymmetrized = pgmxReader.read(new File(absolutePath).toURI().toURL()).probNet();
        
        // Initialize danDiabetes network
        networkName = "networks/dan/DAN-diabetes.pgmx";
        res = getClass().getClassLoader().getResource(networkName);
        f = Paths.get(res.toURI()).toFile();
        absolutePath = f.getAbsolutePath();
        danDiabetes = pgmxReader.read(new File(absolutePath).toURI().toURL()).probNet();
        
        // Initialize ID-A-D1-D2 network
        networkName = "networks/dan/DAN-diabetes-CE.pgmx";
        res = getClass().getClassLoader().getResource(networkName);
        f = Paths.get(res.toURI()).toFile();
        absolutePath = f.getAbsolutePath();
        danDiabetesCE = pgmxReader.read(new File(absolutePath).toURI().toURL()).probNet();
        
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test
    public void instantiateTest() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        ProbNet probNet = danTest2Therapies;
        
        // Instantiate the network for Disease
        Variable variableToInstantiate = probNet.getVariable("Disease");
        List<ProbNet> instantiatedNetworks = DANOperations.instantiate(probNet, variableToInstantiate);
        
        // Assert that there are two instantiated networks (one per each state of disease)
        Assertions.assertEquals(instantiatedNetworks.size(), 2);
        double[] expectedPotential;
        double[] obtainedPotential;
        Node diseaseNode;
        Node testNode;
        Node effectivenessNode;
        
        // TESTS FOR DISEASE = ABSENT
        diseaseNode = instantiatedNetworks.get(0).getNode("Disease");
        testNode = instantiatedNetworks.get(0).getNode("Test");
        effectivenessNode = instantiatedNetworks.get(0).getNode("Effectiveness");
        
        // Check disease potential
        expectedPotential = new double[]{0.86, 0.14};
        obtainedPotential = ((TablePotential) diseaseNode.getPotentials().get(0)).getValues();
        Assertions.assertTrue(Arrays.equals(obtainedPotential, expectedPotential));
        
        // Check Test potential. Assert that the potential of Test is now 0.07, 0.93 for the first instantiation
        expectedPotential = new double[]{0.93, 0.07};
        obtainedPotential = ((TablePotential) testNode.getPotentials().get(0)).getValues();
        Assertions.assertTrue(Arrays.equals(obtainedPotential, expectedPotential));
        
        // Check Effectiveness potential. Assert that the potential is now 10,9.9,9.3
        expectedPotential = new double[]{10, 9.9, 9.3};
        obtainedPotential = ((ExactDistrPotential) effectivenessNode.getPotentials().get(0)).getValues();
        Assertions.assertTrue(Arrays.equals(obtainedPotential, expectedPotential));
        
        // Check that the links from disease to test and effectiveness are removed
        Assertions.assertTrue(instantiatedNetworks.get(0).getLinks(diseaseNode).isEmpty());
        
        // TESTS FOR DISEASE = PRESENT
        diseaseNode = instantiatedNetworks.get(1).getNode("Disease");
        testNode = instantiatedNetworks.get(1).getNode("Test");
        effectivenessNode = instantiatedNetworks.get(1).getNode("Effectiveness");
        
        // Tests for disease = present
        expectedPotential = new double[]{0.86, 0.14};
        obtainedPotential = ((TablePotential) diseaseNode.getPotentials().get(0)).getValues();
        Assertions.assertTrue(Arrays.equals(obtainedPotential, expectedPotential));
        
        // Assert that the potential of Test is now 0.1,0.9 for the second instantiation
        expectedPotential = new double[]{0.1, 0.9};
        obtainedPotential = ((TablePotential) testNode.getPotentials().get(0)).getValues();
        Assertions.assertTrue(Arrays.equals(obtainedPotential, expectedPotential));
        
        // Check Effectiveness potential. Assert that the potential is now 1.2, 4, 6.5
        expectedPotential = new double[]{1.2, 4, 6.5};
        obtainedPotential = ((ExactDistrPotential) effectivenessNode.getPotentials().get(0)).getValues();
        Assertions.assertTrue(Arrays.equals(obtainedPotential, expectedPotential));
        
        // Check that the links from disease to test and effectiveness are removed
        Assertions.assertTrue(instantiatedNetworks.get(1).getLinks(diseaseNode).isEmpty());

        /*
        PGMXWriter0_2 pgmxWriter = new PGMXWriter0_2();
        pgmxWriter.writeProbNet("D://Instantiated-disease-absent_DAN-Test2Therapies.pgmx", instantiatedNetworks.get(0));
        pgmxWriter.writeProbNet("D://Instantiated-disease-present_DAN-Test2Therapies.pgmx", instantiatedNetworks.get(1));
        */

    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void prioritizeTest() {
        Variable urineTestVariable = danDiabetes.getVariable("Dec: Urine test");
        Variable bloodTestVariable = danDiabetes.getVariable("Dec: Blood Test");
        Node urineTestNode, bloodTestNode;
        ProbNet prioritizedNetwork;
        
        // Check the prioritization of urine test decision
        prioritizedNetwork = DANOperations.prioritize(danDiabetes, urineTestVariable);
        urineTestNode = prioritizedNetwork.getNode(urineTestVariable);
        bloodTestNode = prioritizedNetwork.getNode(bloodTestVariable);
        Link<Node> link = prioritizedNetwork.getLink(urineTestNode, bloodTestNode, true);
        Assertions.assertNotNull(link);
        Assertions.assertEquals(link.getFrom().getVariable(), urineTestVariable);
        Assertions.assertEquals(link.getTo().getVariable(), bloodTestVariable);
        
        // Check the prioritization of blood test decision
        prioritizedNetwork = DANOperations.prioritize(danDiabetes, bloodTestVariable);
        bloodTestNode = prioritizedNetwork.getNode(bloodTestVariable);
        urineTestNode = prioritizedNetwork.getNode(urineTestVariable);
        link = prioritizedNetwork.getLink(bloodTestNode, urineTestNode, true);
        Assertions.assertNotNull(link);
        Assertions.assertEquals(link.getFrom().getVariable(), bloodTestVariable);
        Assertions.assertEquals(link.getTo().getVariable(), urineTestVariable);
        
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void getNextDecisionsTest() {
        List<Node> nextDecisions = DANOperations.getNextDecisions(danDecideTest2TherapiesNoCost);
        Assertions.assertEquals(nextDecisions.size(), 1);
        
        nextDecisions = DANOperations.getNextDecisions(danDecideTest2TherapiesNoCostSymmetrized);
        Assertions.assertEquals(nextDecisions.size(), 1);
        
        nextDecisions = DANOperations.getNextDecisions(danDiabetes);
        Assertions.assertEquals(nextDecisions.size(), 2);
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void isSymmetricTest() {
        Assertions.assertTrue(DANOperations.isSymmetric(danTest2Therapies, null));
        Assertions.assertFalse(DANOperations.isSymmetric(danDecideTest2TherapiesNoCost, null));
        Assertions.assertFalse(DANOperations.isSymmetric(danDiabetes, null));
        
        //Assertions.assertTrue(DANOperations.isSymmetric(danDecideTest2TherapiesNoCostSymmetrized));
    }
    
    @Tag(TestSpeed.MEDIUM)
    @Test public void ceaIDAD1D2() {

    }

    /**
     * Reproduces Case 1 of the DAN → decision tree conversion (Section III
     * of Díez, Luque, König, Bermejo, "Decision analysis networks", 2014)
     * applied to the diabetes DAN of Figure 1: the network has an always-
     * observed chance node, {@code Symptom}, so the first step of the
     * recursive algorithm picks it and expands the DAN into one child DAN
     * per state of {@code Symptom}. This test asserts the structural
     * properties the paper requires of those children.
     */
    @Tag(TestSpeed.MEDIUM)
    @Test public void instantiateAlwaysObservedSymptomTest()
            throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther,
                   NonProjectablePotentialException {
        // Pre-condition: the diabetes DAN has Symptom as always-observed
        // (Figure 6 of the paper).
        List<Variable> alwaysObserved = DANOperations.getAlwaysObservedVariables(danDiabetes);
        Assertions.assertTrue(alwaysObserved.stream()
                .anyMatch(v -> v.getName().equals("Symptom")),
                "Symptom should be declared always-observed in DAN-diabetes");

        Variable symptom = danDiabetes.getVariable("Symptom");
        Assertions.assertEquals(2, symptom.getStates().length,
                "Diabetes paper assumes Symptom is dichotomous (absent/present)");

        // Operation: Case 1 of Section III — pick the always-observed
        // node and expand into one DAN per state.
        List<ProbNet> children = DANOperations.instantiate(danDiabetes, symptom);
        Assertions.assertEquals(2, children.size(),
                "instantiate over a 2-state variable must yield two child DANs");

        // Each child must keep the three decisions and three utilities of
        // Figure 1 — instantiating the always-observed Symptom should not
        // remove any decision or utility from the network.
        List<String> expectedDecisions = List.of(
                "Dec: Blood Test", "Dec: Urine test", "Therapy");
        List<String> expectedUtilities = List.of(
                "Cost of blood test", "Cost of urine test", "Quality of life");
        for (ProbNet child : children) {
            for (String name : expectedDecisions) {
                Node n = child.getNode(name);
                Assertions.assertNotNull(n,
                        "decision '" + name + "' should survive Symptom instantiation");
                Assertions.assertEquals(NodeType.DECISION, n.getNodeType());
            }
            for (String name : expectedUtilities) {
                Node n = child.getNode(name);
                Assertions.assertNotNull(n,
                        "utility '" + name + "' should survive Symptom instantiation");
                Assertions.assertEquals(NodeType.UTILITY, n.getNodeType());
            }
        }

        // Both expansions are valid starting points for the next recursion
        // step (Case 3 of the paper: two parentless decisions, no order
        // imposed) — getNextDecisions must therefore still find both
        // Dec: Blood Test and Dec: Urine test in each child.
        for (ProbNet child : children) {
            List<Node> next = DANOperations.getNextDecisions(child);
            Assertions.assertEquals(2, next.size(),
                    "child DAN should still have two parentless decisions");
        }
    }

}
