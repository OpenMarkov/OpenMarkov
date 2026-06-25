package org.openmarkov.core.model.network;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.IncompatibleEvidenceException;
import org.openmarkov.core.exception.NonProjectablePotentialException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.constraint.NoCycle;
import org.openmarkov.core.model.network.constraint.OnlyDirectedLinks;
import org.openmarkov.core.model.network.factory.BNFactory;
import org.openmarkov.core.model.network.factory.DANFactory;
import org.openmarkov.core.model.network.factory.IDFactory;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.core.model.network.type.MIDType;
import org.openmarkov.core.util.UtilTestMethods;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.openmarkov.core.util.UtilTestMethods.addLink;
//TODO - Adapt these test to

///** @author Manuel Arias */
public class ProbNetOperationsTest {
    /*  Public scope for use in all tests. */
    public static final double maxError = 0.0001;
    
    public static ProbNet createInfluenceForAddNoForgettingArcsTest() {
        ProbNet probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        //Define the variables
        //Chance variables
        String diseaseStates[] = {
                "present", "absent"
        };
        String yesNoStates[] = {"yes", "no"};
        Variable variableR1 = new Variable("R1", diseaseStates);
        Variable variableR2 = new Variable("R2", diseaseStates);
        Variable variableR3 = new Variable("R3", diseaseStates);
        Variable variableR4 = new Variable("R4", diseaseStates);
        //Decision variables
        Variable variableD1 = new Variable("D1", yesNoStates);
        Variable variableD2 = new Variable("D2", yesNoStates);
        Variable variableD3 = new Variable("D3", yesNoStates);
        Variable variableD4 = new Variable("D4", yesNoStates);
        //Utility variables
        Variable variableU1 = new Variable("U1");
        Variable variableU2 = new Variable("U2");
        Variable variableU3 = new Variable("U3");
        Variable variableU4 = new Variable("U4");
        //Add variables to the network
        addVariables(probNet, NodeType.CHANCE, variableR1, variableR2, variableR3, variableR4);
        addVariables(probNet, NodeType.DECISION, variableD1, variableD2, variableD3, variableD4);
        addVariables(probNet, NodeType.UTILITY, variableU1, variableU2, variableU3, variableU4);
        //Links throws NodeNotFoundException
        
        //Links from decision nodes
        probNet.addLink(variableD1, variableD2, true);
        probNet.addLink(variableD1, variableU3, true);
        probNet.addLink(variableD2, variableD3, true);
        probNet.addLink(variableD2, variableR1, true);
        probNet.addLink(variableD3, variableU1, true);
        probNet.addLink(variableD3, variableD4, true);
        probNet.addLink(variableD4, variableU4, true);
        probNet.addLink(variableD4, variableR3, true);
        //Links from chance nodes
        probNet.addLink(variableR1, variableR2, true);
        probNet.addLink(variableR1, variableR4, true);
        probNet.addLink(variableR1, variableU3, true);
        probNet.addLink(variableR2, variableR3, true);
        probNet.addLink(variableR2, variableU1, true);
        probNet.addLink(variableR3, variableU2, true);
        probNet.addLink(variableR4, variableD4, true);
        probNet.addLink(variableR4, variableU4, true);
        return probNet;
    }
    
    /**
     * @param net Network
     * @param nodeType The type of node
     * @param variables List of variables to add
     * It adds a list of variables to the network.
     */
    private static void addVariables(ProbNet net, NodeType nodeType, Variable... variables) {
        for (Variable variable : variables) {
            net.addNode(variable, nodeType);
        }
    }
    
    //task approach(or remove them)
    
    public static Variable getVariableAndAssertNotNull(ProbNet network, String variableName) {
        Variable variable = network.getVariable(variableName);
        assertNotNull(variable);
        return variable;
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @Test
    public void testPrune1() throws NonProjectablePotentialException {
        //probNet peque
        //Variables
        String aName = new String("A");
        String b = new String("B");
        String c = new String("C");
        //finite States variables
        Variable variableA = new Variable(aName, 2);
        Variable variableB = new Variable(b, 2);
        Variable variableC = new Variable(c, 2);
        //additional properties
        String relevance = new String("Relevance");
        String value = new String("7.0");
        variableA.setAdditionalProperty(relevance, value);
        variableB.setAdditionalProperty(relevance, value);
        variableC.setAdditionalProperty(relevance, value);
        //Setting variable states
        State absent = new State("ausente");
        State present = new State("presente");
        State[] states = {absent, present};
        variableA.setStates(states);
        variableB.setStates(states);
        variableC.setStates(states);
        //Potentials
        //PotentialType type = PotentialType.TABLE;
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        //Potential A
        double[] tableA = {0.2, 0.8};
        List<Variable> variablesA = new ArrayList<>();
        variablesA.add(variableA);
        TablePotential potentialvaluesA = new TablePotential(variablesA, role, tableA);
        //Potential BA
        double[] tableBA = {0.7, 0.3, 0.9, 0.1};
        List<Variable> variablesBA = new ArrayList<>();
        variablesBA.add(variableB);
        variablesBA.add(variableA);
        TablePotential potentialvaluesBA = new TablePotential(variablesBA, role, tableBA);
        //potencial CAB
        double[] tableCAB = {0.15, 0.29, 0.84, 0.98, 0.85, 0.71, 0.16, 0.02};
        List<Variable> variablesCAB = new ArrayList<>();
        variablesCAB.add(variableC);
        variablesCAB.add(variableA);
        variablesCAB.add(variableB);
        TablePotential potentialvaluesCAB = new TablePotential(variablesCAB, role, tableCAB);
        ProbNet peque = new ProbNet();
        NodeType nodeType = NodeType.CHANCE;
        peque.addNode(variableA, nodeType);
        peque.addNode(variableB, nodeType);
        peque.addNode(variableC, nodeType);
        //Links throws NodeNotFoundException
        peque.addLink(variableA, variableB, true);
        peque.addLink(variableA, variableC, true);
        peque.addLink(variableB, variableC, true);
        peque.addPotential((Potential) potentialvaluesA);
        peque.addPotential((Potential) potentialvaluesBA);
        peque.addPotential((Potential) potentialvaluesCAB);
        Node nodeA = peque.getNode("A");
        Node nodeB = peque.getNode("B");
        Variable A = nodeA.getVariable();
        Variable B = nodeB.getVariable();
        Finding findingA = new Finding(A, 1);
        //A: absent(0)
        HashMap<Variable, Finding> findings = new HashMap<>();
        findings.put(A, findingA);
        EvidenceCase evidenceCase = new EvidenceCase(findings);
        List<Variable> variablesOfInterest = new ArrayList<>();
        variablesOfInterest.add(B);
        //test pruned net
        ProbNet pruned = ProbNetOperations.
                getPruned(peque, variablesOfInterest, evidenceCase);
        ProbNetOperations.projectEvidence(pruned, evidenceCase);
        assertEquals(1, pruned.getNumNodes());
        assertNull(pruned.getVariable("A"));
        assertNull(pruned.getVariable("C"));
        
        assertNotNull(pruned.getVariable("B"));
        assertEquals(1, pruned.getNumPotentials());
        nodeB = pruned.getNode("B");
        TablePotential bPotential =
                (TablePotential) nodeB.getPotentials().get(0);
        assertEquals(1, bPotential.getNumVariables());
        assertTrue(bPotential.contains(B));
        assertEquals(2, ((TablePotential) bPotential).getValues().length);
        int[] offsets = bPotential.getOffsets();
        assertEquals(1, offsets.length);
        int initialPosition = bPotential.getInitialPosition();
        assertEquals(0, initialPosition);
        double a = bPotential.getValues()[initialPosition];
        assertEquals(0.9, a, maxError);
        assertEquals(0.1, bPotential.getValues()[initialPosition + offsets[0]], maxError);
    }
    
    @Test
    /** Test: prune barren nodes and prune parts of the network isolated due to
     *  evidence. */
    public void testPrune2() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        /** ProbNet for test: Two chance nodes A --> B, one decision D, B --> D;
         * one utility U, A --> U, D --> U. */
        Variable variableA = new Variable("A", 2);
        Variable variableB = new Variable("B", 2);
        Variable variableC = new Variable("C", 2);
        Variable variableD = new Variable("D", 2);
        Variable variableE = new Variable("E", 2);
        Variable variableF = new Variable("F", 2);
        Variable variableG = new Variable("G", 2);
        Variable variableH = new Variable("H", 2);
        Variable variableI = new Variable("I", 2);
        Variable U = new Variable("U");
        //create simpleProbNet
        //create Arrays of variables used in potentials
        List<Variable> aVariables = new ArrayList<>(1);
        aVariables.add(variableA);
        List<Variable> baVariables = new ArrayList<>(2);
        baVariables.add(variableB);
        baVariables.add(variableA);
        List<Variable> adVariables = new ArrayList<>(2);
        adVariables.add(variableA);
        adVariables.add(variableD);
        //create potentials
        TablePotential pA = new TablePotential(
                aVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        pA.getValues()[0] = 0.9;
        pA.getValues()[1] = 0.1;
        TablePotential pBA = new TablePotential(
                baVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        pBA.getValues()[0] = 0.2;
        pBA.getValues()[1] = 0.8;
        pBA.getValues()[2] = 0.9;
        pBA.getValues()[3] = 0.1;
        TablePotential pU = new TablePotential(
                adVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        //pU.setUtilityVariable(U);
        pU.getValues()[0] = 1;
        pU.getValues()[1] = 2;
        pU.getValues()[2] = 3;
        pU.getValues()[3] = 4;
        ProbNet simpleProbNet = new ProbNet();
        simpleProbNet.addConstraint(new NoCycle());
        simpleProbNet.addConstraint(new OnlyDirectedLinks());
        //add potentials and variables
        simpleProbNet.addPotential(pA);
        //add variable and potential
        simpleProbNet.addNode(variableD, NodeType.DECISION);
        simpleProbNet.addPotential(pU);
        simpleProbNet.addPotential(pBA);
        simpleProbNet.addLink(variableB, variableD, true);
        Finding eA = new Finding(variableA, 0);
        Finding eB = new Finding(variableB, 1);
        EvidenceCase simpleEvidence = new EvidenceCase();
        simpleEvidence.addFinding(eA);
        //ProbNet pruebaInferencia
        //PotentialType type = PotentialType.TABLE;
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        //Potential CA
        double[] tableCA = {0.81, 0.19, 0.98, 0.02};
        List<Variable> variablesCA = new ArrayList<>();
        variablesCA.add(variableC);
        variablesCA.add(variableA);
        TablePotential potentialvaluesCA = new TablePotential(variablesCA, role, tableCA);
        //Potential EBC
        double[] tableEBC = {0.02, 0.98, 0.68, 0.32, 0.24, 0.76, 0.79, 0.21};
        List<Variable> variablesEBC = new ArrayList<>();
        variablesEBC.add(variableE);
        variablesEBC.add(variableB);
        variablesEBC.add(variableC);
        TablePotential potentialvaluesEBC = new TablePotential(variablesEBC, role, tableEBC);
        //potentialFE
        double[] tableFE = {0.12, 0.88, 0.77, 0.23};
        List<Variable> variablesFE = new ArrayList<>();
        variablesFE.add(variableF);
        variablesFE.add(variableE);
        TablePotential potentialvaluesFE = new TablePotential(variablesFE, role, tableFE);
        //potentialGD
        double[] tableGD = {0.49, 0.51, 0.75, 0.25};
        List<Variable> variablesGD = new ArrayList<>();
        variablesGD.add(variableG);
        variablesGD.add(variableD);
        TablePotential potentialvaluesGD = new TablePotential(variablesGD, role, tableGD);
        //Potential I
        double[] tableI = {0.85, 0.15};
        List<Variable> variablesI = new ArrayList<>();
        variablesI.add(variableI);
        TablePotential potentialvaluesI = new TablePotential(variablesI, role, tableI);
        //Potential DBI
        double[] tableDBI = {0.22, 0.78, 0.86, 0.14, 0.57, 0.43, 0.9, 0.1};
        List<Variable> variablesDBI = new ArrayList<>();
        variablesDBI.add(variableD);
        variablesDBI.add(variableB);
        variablesDBI.add(variableI);
        TablePotential potentialvaluesDBI = new TablePotential(variablesDBI, role, tableDBI);
        //potentialBA
        double[] tableba = {0.77, 0.23, 0.26, 0.74};
        List<Variable> variablesba = new ArrayList<>();
        variablesba.add(variableB);
        variablesba.add(variableA);
        TablePotential potentialvaluesBA = new TablePotential(variablesba, role, tableba);
        //potentialAH
        double[] tableAH = {0.09, 0.91, 0.83, 0.17};
        List<Variable> variablesAH = new ArrayList<>();
        variablesAH.add(variableA);
        variablesAH.add(variableH);
        TablePotential potentialvaluesAH = new TablePotential(variablesAH, role, tableAH);
        //Potential H
        double[] tableH = {0.68, 0.32};
        List<Variable> variablesH = new ArrayList<>();
        variablesH.add(variableH);
        TablePotential potentialvaluesH = new TablePotential(variablesH, role, tableH);
        ProbNet inferenceTestNet = new ProbNet();
        NodeType nodeType = NodeType.CHANCE;
        inferenceTestNet.addNode(variableA, nodeType);
        inferenceTestNet.addNode(variableB, nodeType);
        inferenceTestNet.addNode(variableC, nodeType);
        inferenceTestNet.addNode(variableD, nodeType);
        inferenceTestNet.addNode(variableE, nodeType);
        inferenceTestNet.addNode(variableF, nodeType);
        inferenceTestNet.addNode(variableG, nodeType);
        inferenceTestNet.addNode(variableH, nodeType);
        inferenceTestNet.addNode(variableI, nodeType);
        inferenceTestNet.addLink(variableA, variableB, true);
        inferenceTestNet.addLink(variableA, variableC, true);
        inferenceTestNet.addLink(variableB, variableD, true);
        inferenceTestNet.addLink(variableB, variableE, true);
        inferenceTestNet.addLink(variableC, variableE, true);
        inferenceTestNet.addLink(variableD, variableG, true);
        inferenceTestNet.addLink(variableE, variableF, true);
        inferenceTestNet.addLink(variableH, variableA, true);
        inferenceTestNet.addLink(variableI, variableD, true);
        inferenceTestNet.addPotential((Potential) potentialvaluesCA);
        inferenceTestNet.addPotential((Potential) potentialvaluesEBC);
        inferenceTestNet.addPotential((Potential) potentialvaluesFE);
        inferenceTestNet.addPotential((Potential) potentialvaluesGD);
        inferenceTestNet.addPotential((Potential) potentialvaluesI);
        inferenceTestNet.addPotential((Potential) potentialvaluesDBI);
        inferenceTestNet.addPotential((Potential) potentialvaluesBA);
        inferenceTestNet.addPotential((Potential) potentialvaluesH);
        inferenceTestNet.addPotential((Potential) potentialvaluesAH);
        //Set up evidence: A = 1and D = 1
        Finding findingA = new Finding(variableA, 1);
        Finding findingD = new Finding(variableD, 1);
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(findingA);
        evidence.addFinding(findingD);
        //Set up variables of interest: E
        List<Variable> variablesOfInterest = new ArrayList<>();
        variablesOfInterest.add(variableE);
        ProbNet pruned = ProbNetOperations.getPruned(
                inferenceTestNet, variablesOfInterest, evidence);
        ProbNetOperations.projectEvidence(pruned, evidence);
        List<Variable> variablesPruned = pruned.getVariables();
        assertFalse(variablesPruned.contains(variableA));
        assertTrue(variablesPruned.contains(variableB));
        assertTrue(variablesPruned.contains(variableC));
        assertFalse(variablesPruned.contains(variableD));
        assertTrue(variablesPruned.contains(variableE));
        assertFalse(variablesPruned.contains(variableF));
        assertFalse(variablesPruned.contains(variableG));
        assertFalse(variablesPruned.contains(variableH));
        assertTrue(variablesPruned.contains(variableI));
        //Test B potentials
        Node nodeB = pruned.getNode("B");
        List<Potential> potentialsB = nodeB.getPotentials();
        assertEquals(2, potentialsB.size());
        //Test projected potential p (B | A), A = 1 = psi(B)
        //Get psi (B)
        TablePotential potential0B = (TablePotential) potentialsB.get(0);
        if (potential0B.getNumVariables() == 2) {
            potential0B = (TablePotential) potentialsB.get(1);
        }
        assertEquals(1, potential0B.getNumVariables());
        assertTrue(potential0B.contains(variableB));
        assertEquals(2, potential0B.getValues().length);
        int[] offsets0B = potential0B.getOffsets();
        assertEquals(1, offsets0B.length);
        assertEquals(1, offsets0B[0]);
        int initialPosition = potential0B.getInitialPosition();
        assertEquals(0, initialPosition);
        assertEquals(0.26, potential0B.getValues()[potential0B.getInitialPosition()], maxError);
        assertEquals(0.74, potential0B.getValues()[
                potential0B.getInitialPosition() + offsets0B[0]], maxError);
        //Test projected potential p (D | B, I), D = 1 = psi(B, I)
        TablePotential potential1B = (TablePotential) potentialsB.get(1);
        if (potential1B.getNumVariables() == 1) {
            potential1B = (TablePotential) potentialsB.get(0);
        }
        assertEquals(2, potential1B.getNumVariables());
        assertTrue(potential1B.contains(variableB));
        assertTrue(potential1B.contains(variableI));
    }
    
    @Test
    public final void testPrune3() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        //Create asia.Do not add potentials because they will not be used.
        String strAsia = "Asia";
        String strSmoker = "Smoker";
        String strTuberculosis = "Tuberculosis";
        String strCancer = "Cancer";
        String strTuberculosisOrCancer = "TuberculosisOrCancer";
        String strDyspnea = "Dyspnea";
        String strBronchitis = "Bronchitis";
        String strXRay = "XRay";
        ProbNet probNetAsia = UtilTestMethods.createProbNet(strAsia, strSmoker, strTuberculosis,
                                                            strCancer, strTuberculosisOrCancer, strDyspnea, strBronchitis, strXRay);
        addLink(probNetAsia, strAsia, strTuberculosis, true);
        addLink(probNetAsia, strSmoker, strCancer, true);
        addLink(probNetAsia, strSmoker, strBronchitis, true);
        addLink(probNetAsia, strTuberculosis, strTuberculosisOrCancer, true);
        addLink(probNetAsia, strCancer, strTuberculosisOrCancer, true);
        addLink(probNetAsia, strBronchitis, strDyspnea, true);
        addLink(probNetAsia, strTuberculosisOrCancer, strDyspnea, true);
        addLink(probNetAsia, strTuberculosisOrCancer, strXRay, true);
        EvidenceCase evidence = addEvidence(probNetAsia, null, strTuberculosis, 0);
        addEvidence(probNetAsia, null, strTuberculosisOrCancer, 0);
        List<Variable> variablesOfInterest = new ArrayList<>(1);
        variablesOfInterest.add(probNetAsia.getVariable(strDyspnea));
        //Call method
        ProbNet pruned = ProbNetOperations.getPruned(probNetAsia, variablesOfInterest, evidence);
        assertNotNull(pruned.getVariable(strTuberculosis));
    }
    
    private EvidenceCase addEvidence(ProbNet probNet, EvidenceCase evidence, String variableName, int stateNumber) throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        if (evidence == null) {
            evidence = new EvidenceCase();
        }
        Variable variable = probNet.getVariable(variableName);
        Finding finding = new Finding(variable, stateNumber);
        evidence.addFinding(finding);
        return evidence;
    }
    
    /**
     * Test method for {@link ProbNetOperations#projectEvidence(ProbNet, EvidenceCase)}.
     */
    @Test
    public final void testProjectEvidence() {
    }
    
    /**
     * Test method for {@link ProbNetOperations#removeBarrenNodes(ProbNet, Collection, HashSet)}.
     */
    @Test
    public final void testRemoveBarrenNodes() {
    }
    
    /**
     * Test method for {@link ProbNetOperations#removeUnreachableNodes(ProbNet, Collection, HashSet)}.
     */
    @Test
    public final void testRemoveUnreachableNodes() {
    }
    
    /**
     */
    @Test
    public void testGetPrunedMethodBN_Asia() {
        ProbNet network;
        ProbNet outputNetwork;
        ProbNet intermediate;
        HashSet<Variable> variablesOfEvidence;
        List<Variable> variablesOfInterest;
//Repeat the test, because the behaviour of method getPruned is non-deterministic
        for (int i = 1; i < 100; i++) {
            network = BNFactory.createBN_Asia();
            Variable variableD = getVariableAndAssertNotNull(network, "D");
            Variable variableTOrC = getVariableAndAssertNotNull(network, "TOrC");
            Variable variableT = getVariableAndAssertNotNull(network, "T");
            variablesOfInterest = new ArrayList<>();
            variablesOfInterest.add(variableD);
            variablesOfEvidence = new HashSet<>();
            variablesOfEvidence.add(variableTOrC);
            variablesOfEvidence.add(variableT);
            intermediate = ProbNetOperations.removeBarrenNodes(network, variablesOfInterest, variablesOfEvidence);
//Nodes shouldn't appear
            assertTrue(intermediate.containsVariable("A"));
            assertFalse(intermediate.containsVariable("X"));
//Nodes must appear
            assertTrue(intermediate.containsVariable("T"));
            assertTrue(intermediate.containsVariable("TOrC"));
            assertTrue(intermediate.containsVariable("S"));
            assertTrue(intermediate.containsVariable("L"));
            assertTrue(intermediate.containsVariable("B"));
            assertTrue(intermediate.containsVariable("D"));
            outputNetwork = ProbNetOperations.removeUnreachableNodes(intermediate, variablesOfInterest, variablesOfEvidence);
//Nodes shouldn't appear
            assertFalse(outputNetwork.containsVariable("A"));
            assertFalse(outputNetwork.containsVariable("X"));
//Nodes must appear
            assertTrue(outputNetwork.containsVariable("T"));
            assertTrue(outputNetwork.containsVariable("TOrC"));
            assertTrue(outputNetwork.containsVariable("S"));
            assertTrue(outputNetwork.containsVariable("L"));
            assertTrue(outputNetwork.containsVariable("B"));
            assertTrue(outputNetwork.containsVariable("D"));
        }
    }
    
    @Disabled("Last check expects for the Potential to be a TablePotential, but it is WeibullHazardPotential")
    @Test
    public final void testConvertNumericalVariablesToFS() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther, NonProjectablePotentialException {
        //Initialize network
        ProbNet probNet = new ProbNet(MIDType.getUniqueInstance());
        //Declare variables
        Variable ageAtStateEntryVar_0 = new Variable("Age at state entry", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable ageAtStateEntryVar_1 = new Variable("Age at state entry", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable ageAtStateEntryVar_2 = new Variable("Age at state entry", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable timeInStateVar_0 = new Variable("Time in state", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable timeInStateVar_1 = new Variable("Time in state", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable timeInStateVar_2 = new Variable("Time in state", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable ageVar_0 = new Variable("Age", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable ageVar_1 = new Variable("Age", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable ageVar_2 = new Variable("Age", true, 0.0, Double.POSITIVE_INFINITY, false, 0.01);
        Variable transitionVar_1 = new Variable("Transition", "no", "yes");
        Variable transitionVar_2 = new Variable("Transition", "no", "yes");
        Variable transitionVar_3 = new Variable("Transition", "no", "yes");
        //Set time slices
        ageAtStateEntryVar_0.setTimeSlice(0);
        ageAtStateEntryVar_1.setTimeSlice(1);
        ageAtStateEntryVar_2.setTimeSlice(2);
        ageVar_0.setTimeSlice(0);
        ageVar_1.setTimeSlice(1);
        ageVar_2.setTimeSlice(2);
        timeInStateVar_0.setTimeSlice(0);
        timeInStateVar_1.setTimeSlice(1);
        timeInStateVar_2.setTimeSlice(2);
        transitionVar_1.setTimeSlice(1);
        transitionVar_2.setTimeSlice(2);
        transitionVar_3.setTimeSlice(3);
        //Nodes
        probNet.addNode(ageAtStateEntryVar_0, NodeType.CHANCE);
        probNet.addNode(ageAtStateEntryVar_1, NodeType.CHANCE);
        probNet.addNode(ageAtStateEntryVar_2, NodeType.CHANCE);
        probNet.addNode(ageVar_0, NodeType.CHANCE);
        probNet.addNode(ageVar_1, NodeType.CHANCE);
        probNet.addNode(ageVar_2, NodeType.CHANCE);
        probNet.addNode(timeInStateVar_0, NodeType.CHANCE);
        probNet.addNode(timeInStateVar_1, NodeType.CHANCE);
        probNet.addNode(timeInStateVar_2, NodeType.CHANCE);
        probNet.addNode(transitionVar_1, NodeType.CHANCE);
        probNet.addNode(transitionVar_2, NodeType.CHANCE);
        probNet.addNode(transitionVar_3, NodeType.CHANCE);
        //Links
        probNet.addLink(ageVar_0, ageVar_1, true);
        probNet.addLink(ageVar_1, ageVar_2, true);
        probNet.addLink(ageVar_0, ageAtStateEntryVar_0, true);
        probNet.addLink(ageVar_1, ageAtStateEntryVar_1, true);
        probNet.addLink(ageVar_2, ageAtStateEntryVar_2, true);
        probNet.addLink(timeInStateVar_0, timeInStateVar_1, true);
        probNet.addLink(timeInStateVar_1, timeInStateVar_2, true);
        probNet.addLink(timeInStateVar_0, ageAtStateEntryVar_0, true);
        probNet.addLink(timeInStateVar_1, ageAtStateEntryVar_1, true);
        probNet.addLink(timeInStateVar_2, ageAtStateEntryVar_2, true);
        probNet.addLink(timeInStateVar_0, transitionVar_1, true);
        probNet.addLink(timeInStateVar_1, transitionVar_2, true);
        probNet.addLink(timeInStateVar_2, transitionVar_3, true);
        probNet.addLink(ageAtStateEntryVar_0, transitionVar_1, true);
        probNet.addLink(ageAtStateEntryVar_1, transitionVar_2, true);
        probNet.addLink(ageAtStateEntryVar_2, transitionVar_3, true);
        probNet.addLink(transitionVar_1, timeInStateVar_1, true);
        probNet.addLink(transitionVar_2, timeInStateVar_2, true);
        //Potentials
        PotentialRole role = PotentialRole.CONDITIONAL_PROBABILITY;
        DeltaPotential agePotential_0 = new DeltaPotential(Arrays.asList(ageVar_0), role);
        agePotential_0.setValue(4.4);
        probNet.getNode(ageVar_0).setPotential(agePotential_0);
        probNet.getNode(ageVar_1)
               .setPotential(new CycleLengthShift(Arrays.asList(ageVar_1, ageVar_0), probNet.getCycleLength()));
        probNet.getNode(ageVar_2)
               .setPotential(new CycleLengthShift(Arrays.asList(ageVar_2, ageVar_1), probNet.getCycleLength()));
        LinearCombinationPotential ageAtStateEntryPotential_0 = new LinearCombinationPotential(Arrays.asList(ageAtStateEntryVar_0,
                                                                                                             ageVar_0,
                                                                                                             timeInStateVar_0),
                                                                                               role);
        ageAtStateEntryPotential_0.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "Constant"),
                ageAtStateEntryVar_0.asVariableExpression(),
                timeInStateVar_0.asVariableExpression()}
        );
        ageAtStateEntryPotential_0.setCoefficients(new double[]{0, 1, -1});
        probNet.getNode(ageAtStateEntryVar_0).setPotential(ageAtStateEntryPotential_0);
        LinearCombinationPotential ageAtStateEntryPotential_1 = new LinearCombinationPotential(Arrays.asList(ageAtStateEntryVar_1,
                                                                                                             ageVar_1,
                                                                                                             timeInStateVar_1),
                                                                                               role);
        ageAtStateEntryPotential_1.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "Constant"),
                ageAtStateEntryVar_1.asVariableExpression(),
                timeInStateVar_1.asVariableExpression()}
        );
        
        ageAtStateEntryPotential_1.setCoefficients(new double[]{0, 1, -1});
        probNet.getNode(ageAtStateEntryVar_1).setPotential(ageAtStateEntryPotential_1);
        LinearCombinationPotential ageAtStateEntryPotential_2 = new LinearCombinationPotential(Arrays.asList(ageAtStateEntryVar_2,
                                                                                                             ageVar_2,
                                                                                                             timeInStateVar_2),
                                                                                               role);
        ageAtStateEntryPotential_2.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "Constant"),
                ageAtStateEntryVar_2.asVariableExpression(),
                timeInStateVar_2.asVariableExpression()}
        );
        ageAtStateEntryPotential_2.setCoefficients(new double[]{0, 1, -1});
        probNet.getNode(ageAtStateEntryVar_2).setPotential(ageAtStateEntryPotential_2);
        Potential timeInStatePotential_0 = new DeltaPotential(Arrays.asList(timeInStateVar_0), role);
        probNet.getNode(timeInStateVar_0).setPotential(timeInStatePotential_0);
        TreeADDPotential timeInStatePotential_1 = new TreeADDPotential(Arrays.asList(timeInStateVar_1,
                                                                                     timeInStateVar_0,
                                                                                     transitionVar_1), transitionVar_1, role);
        Potential noTransitionPotentialBranch_1 = new CycleLengthShift(Arrays.asList(timeInStateVar_1, timeInStateVar_0), probNet.getCycleLength());
        Potential transitionPotentialBranch_1 = new DeltaPotential(Arrays.asList(timeInStateVar_1), role, 0.0);
        timeInStatePotential_1.setBranches(Arrays.asList(
                new TreeADDBranch(Arrays.asList(transitionVar_1.getStates()[0]), transitionVar_1,
                                  noTransitionPotentialBranch_1, new ArrayList<Variable>()),
                new TreeADDBranch(Arrays.asList(transitionVar_1.getStates()[1]), transitionVar_1,
                                  transitionPotentialBranch_1, new ArrayList<Variable>())));
        probNet.getNode(timeInStateVar_1).setPotential(timeInStatePotential_1);
        TreeADDPotential timeInStatePotential_2 = new TreeADDPotential(Arrays.asList(timeInStateVar_2,
                                                                                     timeInStateVar_1,
                                                                                     transitionVar_2), transitionVar_2, role);
        Potential noTransitionPotentialBranch_2 = new CycleLengthShift(Arrays.asList(timeInStateVar_2, timeInStateVar_1), probNet.getCycleLength());
        Potential transitionPotentialBranch_2 = new DeltaPotential(Arrays.asList(timeInStateVar_2), role, 0.0);
        timeInStatePotential_2.setBranches(Arrays.asList(
                new TreeADDBranch(Arrays.asList(transitionVar_2.getStates()[0]), transitionVar_2,
                                  noTransitionPotentialBranch_2, new ArrayList<Variable>()),
                new TreeADDBranch(Arrays.asList(transitionVar_2.getStates()[1]), transitionVar_2,
                                  transitionPotentialBranch_2, new ArrayList<Variable>())));
        probNet.getNode(timeInStateVar_2).setPotential(timeInStatePotential_2);
        WeibullHazardPotential transitionPotential_1 = new WeibullHazardPotential(Arrays.asList(transitionVar_1, ageAtStateEntryVar_0, timeInStateVar_0), role);
        transitionPotential_1.setTimeVariable(timeInStateVar_0);
        transitionPotential_1.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "Gamma"),
                new VariableExpression(Collections.emptyList(), "Constant"),
                ageAtStateEntryVar_0.asVariableExpression()
        });
        transitionPotential_1.setCoefficients(new double[]{0.3757164, -1.166541, 0.002097});
        probNet.getNode(transitionVar_1).setPotential(transitionPotential_1);
        WeibullHazardPotential transitionPotential_2 = new WeibullHazardPotential(Arrays.asList(transitionVar_2, ageAtStateEntryVar_1, timeInStateVar_1), role);
        transitionPotential_2.setTimeVariable(timeInStateVar_1);
        transitionPotential_2.setCovariates(new VariableExpression[]{
                new VariableExpression(Collections.emptyList(), "Gamma"),
                new VariableExpression(Collections.emptyList(), "Constant"),
                ageAtStateEntryVar_1.asVariableExpression()
        });
        transitionPotential_2.setCoefficients(new double[]{0.3757164, -1.166541, 0.002097});
        probNet.getNode(transitionVar_2).setPotential(transitionPotential_2);
        probNet.getNode(transitionVar_3).setPotential(new UniformPotential(Arrays.asList(transitionVar_3), role));
        ProbNet convertedNet = ProbNetOperations.convertNumericalVariablesToFS(probNet);
        double[] ageAtStateEntry_1_expectedValues = new double[]{0, 1, 1, 0};
        double[] ageAtStateEntry_1_Values = ((TablePotential) convertedNet.getNode("Age at state entry [1]")
                                                                          .getPotentials()
                                                                          .get(0)).getValues();
        assertArrayEquals(ageAtStateEntry_1_expectedValues, ageAtStateEntry_1_Values, 0.001);
        double[] timeInState_1_expectedValues = new double[]{0, 1, 1, 0};
        double[] timeInState_1_Values = ((TablePotential) convertedNet.getNode("Time in state [1]")
                                                                      .getPotentials()
                                                                      .get(0)).getValues();
        assertArrayEquals(timeInState_1_expectedValues, timeInState_1_Values, 0.001);
        double[] ageAtStateEntry_2_expectedValues = new double[]{0, 0, 1, 0, 1, 0, 1, 0, 0};
        double[] ageAtStateEntry_2_Values = ((TablePotential) convertedNet.getNode("Age at state entry [2]")
                                                                          .getPotentials()
                                                                          .get(0)).getValues();
        assertArrayEquals(ageAtStateEntry_2_expectedValues, ageAtStateEntry_2_Values, 0.001);
        double[] timeInState_2_expectedValues = new double[]{0, 1, 0, 0, 0, 1, 1, 0, 0, 1, 0, 0};
        double[] timeInState_2_Values = ((TablePotential) convertedNet.getNode("Time in state [2]")
                                                                      .getPotentials()
                                                                      .get(0)).getValues();
        assertArrayEquals(timeInState_2_expectedValues, timeInState_2_Values, 0.001);
        //Test projection of potentials of not -numeric nodes with numeric parents
        double[] transitionPotential_2_expectedValues = new double[]{0.730278527, 0.269721473, 0.729796819, 0.270203181, 0.578080011, 0.421919989, 0.57741534, 0.42258466};
        Potential potential = convertedNet.getNode("Transition [2]")
                                          .getPotentials()
                                          .get(0);
        
        double[] transitionPotential_2_Values = ((TablePotential) potential).getValues();
        assertArrayEquals(transitionPotential_2_expectedValues, transitionPotential_2_Values, 0.001);
    }
    
    @Test
    public final void testSumProjectedPotential() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Variable varA = new Variable("A", "a0", "a1", "a2");
        Variable varB = new Variable("B", "b0", "b1");
        Variable varC = new Variable("C", "c0", "c1", "c2", "c3");
        Variable varD = new Variable("D", "d0", "d1", "d2");
        List<Variable> originalVariables = Arrays.asList(varA, varB, varC, varD);
        List<Variable> projectedPotentialVariables = Arrays.asList(varA, varC);
        TablePotential originalPotential = new TablePotential(originalVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        projectedPotential.setValues(new double[]{0.01, 0.02, 0.03, 0.04, 0.05, 0.06, 0.07, 0.08, 0.09, 0.10, 0.11, 0.12});
        EvidenceCase configuration = new EvidenceCase();
        configuration.addFinding(new Finding(varB, 0));
        configuration.addFinding(new Finding(varD, 1));
        for (int i = 0; i < originalPotential.getValues().length; ++i) {
            originalPotential.getValues()[i] = 0;
        }
        ProbNetOperations.sumProjectedPotential(originalPotential, projectedPotential, configuration);
        double[] expectedValues = new double[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0, 0.01, 0.02, 0.03, 0, 0, 0, 0.04, 0.05, 0.06, 0, 0, 0, 0.07, 0.08, 0.09,
                0, 0, 0, 0.1, 0.11, 0.12, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0};
        assertArrayEquals(expectedValues, originalPotential.getValues(), 0.0001);
    }
    
    @Test
    public final void testSumProjectedPotentialUnorderedVariables() {
        Variable varTransition = new Variable("Transition", "no", "yes");
        Variable varTherapy = new Variable("Therapy", "placebo", "drug");
        Variable varState = new Variable("State", "dead", "low", "medium", "high");
        List<Variable> originalVariables = Arrays.asList(varTransition, varState, varTherapy);
        List<Variable> projectedPotentialVariables = Arrays.asList(varTransition, varTherapy, varState);
        TablePotential originalPotential = new TablePotential(originalVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        TablePotential projectedPotential = new TablePotential(projectedPotentialVariables, PotentialRole.CONDITIONAL_PROBABILITY);
        projectedPotential.setValues(new double[]{1, 0, 1, 0, 1, 0, 1, 0, 0.883, 0.117, 0.904, 0.096, 0.764, 0.236, 0.773, 0.227});
        EvidenceCase configuration = new EvidenceCase();
        for (int i = 0; i < originalPotential.getValues().length; ++i) {
            originalPotential.getValues()[i] = 0;
        }
        ProbNetOperations.sumProjectedPotential(originalPotential, projectedPotential, configuration);
        double[] expectedValues = new double[]{1, 0, 1, 0, 0.883, 0.117, 0.764, 0.236, 1, 0, 1, 0, 0.904, 0.096, 0.773, 0.227};
        assertArrayEquals(expectedValues, originalPotential.getValues(), 0.0001);
    }
    
    @Test
    public final void testSumProjectedUtilityPotential() throws IncompatibleEvidenceException.EvidenceIsIncompatibleWithOther {
        Variable costVar = new Variable("Cost", true, 0.0, Double.POSITIVE_INFINITY, false, 0.001);
        Variable varTherapy = new Variable("Therapy", "placebo", "drug");
        Variable varStateA = new Variable("State A", "no", "yes");
        Variable varStateB = new Variable("State B", "no", "yes");
        Variable ageVar = new Variable("Age", "4.4");
        ExactDistrPotential originalPotential = new ExactDistrPotential(Arrays.asList(costVar, varTherapy, varStateA, varStateB, ageVar));
        ExactDistrPotential projectedPotential = new ExactDistrPotential(Arrays.asList(costVar, varTherapy, varStateA, varStateB));
        projectedPotential.setValues(new double[]{19.969, 19.542, 18.858, 18.455, 20.161, 19.731, 19.04, 18.633});
        EvidenceCase configuration = new EvidenceCase();
        configuration.addFinding(new Finding(ageVar, 0));
        ProbNetOperations.sumProjectedPotential(originalPotential.getTablePotential(), projectedPotential.getTablePotential(), configuration);
        double[] expectedValues = new double[]{19.969, 19.542, 18.858, 18.455, 20.161, 19.731, 19.04, 18.633};
        assertArrayEquals(expectedValues, originalPotential.getValues(), 0.001);
    }
    
    @Test
    public void testHasStructuralAssymetry() {
        ProbNet decideTestDAN = DANFactory.buildDecideTestDAN();
        ProbNet decideTestID = IDFactory.buildIDDecideTest();
        ProbNet datingDAN = DANFactory.buildDatingDAN();
        assertTrue(ProbNetOperations.hasStructuralAsymmetry(decideTestDAN));
        assertFalse(ProbNetOperations.hasStructuralAsymmetry(decideTestID));
        assertTrue(ProbNetOperations.hasStructuralAsymmetry(datingDAN));
    }
    
    @Test
    public void testHasOrderAssymetry() {
        ProbNet decideTestDAN = DANFactory.buildDecideTestDAN();
        ProbNet decideTestID = IDFactory.buildIDDecideTest();
        ProbNet datingDAN = DANFactory.buildDatingDAN();
        ProbNet reactorDAN = DANFactory.buildReactorDAN();
        ProbNet diabetesDAN = DANFactory.buildDiabetesDAN();
        assertTrue(ProbNetOperations.hasOrderAsymmetry(decideTestDAN));
        assertFalse(ProbNetOperations.hasOrderAsymmetry(decideTestID));
        assertFalse(ProbNetOperations.hasOrderAsymmetry(reactorDAN));
        assertTrue(ProbNetOperations.hasOrderAsymmetry(datingDAN));
        assertTrue(ProbNetOperations.hasOrderAsymmetry(diabetesDAN));
    }
    
    @Test
    public void testGetObservableAndNonObservedVariables() {
        auxTestGetObservableAndNonObservedVariables(DANFactory.buildDecideTestDAN(), Arrays.asList("Result of test"), Arrays.asList("Disease"));
        auxTestGetObservableAndNonObservedVariables(DANFactory.buildDiabetesDAN(), Arrays.asList("Blood test result", "Urine test result", "Symptom"), Arrays.asList("Diabetes"));
        auxTestGetObservableAndNonObservedVariables(DANFactory.buildDatingDAN(),
                                                    Arrays.asList("Accept", "ToDo", "TVExp", "Club", "MeetFr", "mExp", "rExp", "TV"), Arrays.asList("LikesMe", "mMood", "rMood", "NCExp"));
        auxTestGetObservableAndNonObservedVariables(DANFactory.buildReactorDAN(), Arrays.asList("Result of test", "Result of conventional reactor", "Result of advanced reactor"),
                                                    Arrays.asList("Advanced reactor reliability"));
    }
    
    private void auxTestGetObservableAndNonObservedVariables(ProbNet probNet, List<String> observable, List<String> nonObservable) {
        checkEqualVariables(ProbNetOperations.getObservableVariables(probNet), observable);
        checkEqualVariables(ProbNetOperations.getNeverObservedVariables(probNet), nonObservable);
    }
    
    public void checkEqualVariables(Collection<Node> nodes, List<String> stringVariables) {
        assertEquals(nodes.size(), stringVariables.size());
        for (Node node : nodes) {
            assertTrue(stringVariables.contains(node.getVariable().getName()));
        }
    }
    
    @Test
    public void testAddNoForgettingArcs() {
        Boolean shareAllLinks = false;
        //We create the influence diagram
        ProbNet influenceDiagram = createInfluenceForAddNoForgettingArcsTest();
        //And manually no - forgetting arcs
        ProbNet idWithNoForgettingArcs = influenceDiagram.copy();
        //New links from decision nodes idWithNoForgettingArcs.
        idWithNoForgettingArcs.addLink(idWithNoForgettingArcs.getVariable("D1"), idWithNoForgettingArcs.getVariable("D3"), true);
        idWithNoForgettingArcs.addLink(idWithNoForgettingArcs.getVariable("D1"), idWithNoForgettingArcs.getVariable("D4"), true);
        idWithNoForgettingArcs.addLink(idWithNoForgettingArcs.getVariable("D2"), idWithNoForgettingArcs.getVariable("D4"), true);
        
        //Along with all the links
        List<Link<Node>> allLinks = idWithNoForgettingArcs.getLinks();
        //We call the method to transform the influence diagram in its limid version
        ProbNetOperations.addNoForgettingArcs(influenceDiagram);
        //And we iterate over its links
        for (Link<Node> nodeLink : influenceDiagram.getLinks()) {
            shareAllLinks = false;
            for (Link<Node> limidLink : allLinks) {
                if (nodeLink.getFrom().getName().compareTo(limidLink.getFrom().getName()) == 0 &&
                        nodeLink.getTo().getName().compareTo(limidLink.getTo().getName()) == 0 &&
                        nodeLink.isDirected() == limidLink.isDirected()) {
                    shareAllLinks = true;
                    break;
                }
            }
            if (shareAllLinks) {
                allLinks.remove(nodeLink);
            } else {
                break;
            }
        }
        assertTrue(shareAllLinks);
    }
}
