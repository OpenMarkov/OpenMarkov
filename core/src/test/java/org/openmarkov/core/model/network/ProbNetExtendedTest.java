/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.openmarkov.core.action.base.StateAction;
import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.model.network.constraint.*;
import org.openmarkov.core.model.network.potential.*;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.model.network.type.DynamicLimidType;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended tests for {@link ProbNet} covering areas not tested by
 * {@link ProbNetTest} and {@link ProbNetCopyTest}.
 */
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class ProbNetExtendedTest {

    private ProbNet bn;
    private Variable A;
    private Variable B;
    private Variable C;
    private Node nodeA;
    private Node nodeB;
    private Node nodeC;

    @BeforeEach
    void setUp() {
        bn = new ProbNet(BayesianNetworkType.getUniqueInstance());
        A = new Variable("A", 2);
        B = new Variable("B", 2);
        C = new Variable("C", 2);
        nodeA = bn.addNode(A, NodeType.CHANCE);
        nodeB = bn.addNode(B, NodeType.CHANCE);
        nodeC = bn.addNode(C, NodeType.CHANCE);
        bn.addLink(A, B, true);
        bn.addLink(B, C, true);
    }

    // -----------------------------------------------------------------------
    // Variables
    // -----------------------------------------------------------------------

    @Nested
    class VariableTests {

        @Test
        void getVariableByName() {
            Variable found = bn.getVariable("A");
            assertNotNull(found);
            assertSame(A, found);
        }

        @Test
        void getVariableByNameReturnsNullForMissing() {
            assertNull(bn.getVariable("NonExistent"));
        }

        @Test
        void getVariableByBaseNameAndTimeSlice() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            Variable t0 = new Variable("X [0]", 2);
            Variable t1 = new Variable("X [1]", 2);
            dbn.addNode(t0, NodeType.CHANCE);
            dbn.addNode(t1, NodeType.CHANCE);

            assertSame(t0, dbn.getVariable("X", 0));
            assertSame(t1, dbn.getVariable("X", 1));
            assertNull(dbn.getVariable("X", 5));
        }

        @Test
        void containsVariableByName() {
            assertTrue(bn.containsVariable("A"));
            assertTrue(bn.containsVariable("B"));
            assertFalse(bn.containsVariable("Z"));
        }

        @Test
        void containsVariableByObject() {
            assertTrue(bn.containsVariable(A));
            assertFalse(bn.containsVariable(new Variable("Z", 2)));
        }

        @Test
        void containsShiftedVariable() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            Variable t0 = new Variable("X [0]", 2);
            Variable t1 = new Variable("X [1]", 2);
            dbn.addNode(t0, NodeType.CHANCE);
            dbn.addNode(t1, NodeType.CHANCE);

            assertTrue(dbn.containsShiftedVariable(t0, 1));
            assertFalse(dbn.containsShiftedVariable(t0, 5));
        }

        @Test
        void getShiftedVariable() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            Variable t0 = new Variable("X [0]", 2);
            Variable t1 = new Variable("X [1]", 2);
            dbn.addNode(t0, NodeType.CHANCE);
            dbn.addNode(t1, NodeType.CHANCE);

            assertSame(t1, dbn.getShiftedVariable(t0, 1));
            assertNull(dbn.getShiftedVariable(t0, 99));
        }

        @Test
        void getVariablesNames() {
            ArrayList<String> names = bn.getVariablesNames();
            assertEquals(3, names.size());
            assertTrue(names.contains("A"));
            assertTrue(names.contains("B"));
            assertTrue(names.contains("C"));
        }

        @Test
        void getNonUtilityVariablesExcludesUtility() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            Variable ch = new Variable("Ch", 2);
            Variable dec = new Variable("Dec", 2);
            Variable util = new Variable("Util");
            util.setDecisionCriterion(new Criterion());
            id.addNode(ch, NodeType.CHANCE);
            id.addNode(dec, NodeType.DECISION);
            id.addNode(util, NodeType.UTILITY);

            List<Variable> nonUtil = id.getNonUtilityVariables();
            assertTrue(nonUtil.contains(ch));
            assertTrue(nonUtil.contains(dec));
            assertFalse(nonUtil.contains(util));
        }

        @Test
        void deprecatedGetChanceAndDecisionDelegatesToNonUtility() {
            @SuppressWarnings("deprecation")
            List<Variable> old = bn.getChanceAndDecisionVariables();
            List<Variable> newMethod = bn.getNonUtilityVariables();
            assertEquals(old, newMethod);
        }

        @Test
        void getVariablesReturnsAllVariables() {
            ArrayList<Variable> vars = bn.getVariables();
            assertEquals(3, vars.size());
            assertTrue(vars.contains(A));
            assertTrue(vars.contains(B));
            assertTrue(vars.contains(C));
        }
    }

    // -----------------------------------------------------------------------
    // Constraints
    // -----------------------------------------------------------------------

    @Nested
    class ConstraintTests {

        @Test
        void hasConstraintOfClassReturnsTrueForBNConstraints() {
            assertTrue(bn.hasConstraintOfClass(OnlyChanceNodes.class));
        }

        @Test
        void hasConstraintOfClassReturnsFalseForAbsent() {
            assertFalse(bn.hasConstraintOfClass(OnlyTemporalVariables.class));
        }

        @Test
        void getConstraintOfClassReturnsInstance() {
            OnlyChanceNodes c = bn.getConstraintOfClass(OnlyChanceNodes.class);
            assertNotNull(c);
        }

        @Test
        void getConstraintOfClassReturnsNullForAbsent() {
            assertNull(bn.getConstraintOfClass(OnlyTemporalVariables.class));
        }

        @Test
        void checkProbNetReturnsTrueForValidNetwork() {
            assertTrue(bn.checkProbNet());
        }

        @Test
        void getUnsatisfiedConstraintsEmptyForValidNetwork() {
            assertTrue(bn.getUnsatisfiedConstraints().isEmpty());
        }

        @Test
        void setNetworkTypeChangesConstraints() throws ConstraintViolatedException {
            ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
            assertTrue(net.hasConstraintOfClass(OnlyChanceNodes.class));

            net.setNetworkType(InfluenceDiagramType.getUniqueInstance());
            assertEquals(InfluenceDiagramType.getUniqueInstance(), net.getNetworkType());
        }

        @Test
        void setNetworkTypeRollsBackOnViolation() {
            ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            Variable d = new Variable("D", 2);
            net.addNode(d, NodeType.DECISION);

            // BN requires OnlyChanceNodes, but we have a decision node
            assertThrows(ConstraintViolatedException.class,
                    () -> net.setNetworkType(BayesianNetworkType.getUniqueInstance()));

            // Network type should be rolled back
            assertEquals(InfluenceDiagramType.getUniqueInstance(), net.getNetworkType());
        }

        @Test
        void checkConstraintsThrowsOnViolation() {
            ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            net.addNode(new Variable("X", 2), NodeType.CHANCE);
            // Add OnlyTemporalVariables constraint — atemporal variable violates it
            net.addConstraint(new OnlyTemporalVariables());

            assertThrows(ConstraintViolatedException.class, net::checkConstraints);
        }

        @Test
        void checkConstraintsPassesForValidNetwork() {
            assertDoesNotThrow((org.junit.jupiter.api.function.Executable) bn::checkConstraints);
        }
    }

    // -----------------------------------------------------------------------
    // Network classification
    // -----------------------------------------------------------------------

    @Nested
    class ClassificationTests {

        @Test
        void bayesianNetworkOnlyChanceNodes() {
            assertTrue(bn.onlyChanceNodes());
        }

        @Test
        void influenceDiagramNotOnlyChanceNodes() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            assertFalse(id.onlyChanceNodes());
        }

        @Test
        void bayesianNetworkIsNotTemporal() {
            // BN has OnlyAtemporalVariables by default
            assertFalse(bn.variablesCouldBeTemporal());
        }

        @Test
        void dynamicLimidCouldBeTemporal() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            assertTrue(dbn.variablesCouldBeTemporal());
        }

        @Test
        void dynamicLimidOnlyTemporal() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            assertTrue(dbn.onlyTemporal());
        }

        @Test
        void bayesianNetworkNotOnlyTemporal() {
            assertFalse(bn.onlyTemporal());
        }

        @Test
        void bayesianNetworkIsNotMultiagent() {
            assertFalse(bn.isMultiagent());
        }

        @Test
        void thereAreNoTemporalNodesInBN() {
            assertFalse(bn.thereAreTemporalNodes());
        }

        @Test
        void thereAreTemporalNodesWhenPresent() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            Variable t0 = new Variable("X [0]", 2);
            dbn.addNode(t0, NodeType.CHANCE);
            assertTrue(dbn.thereAreTemporalNodes());
        }

        @Test
        void getNumCriteriaReturnsZeroWhenNoPotentials() {
            ProbNet empty = new ProbNet();
            assertEquals(0, empty.getNumCriteria());
        }

        @Test
        void getNumCriteriaCountsDistinctCriteria() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            Variable u1 = new Variable("U1");
            Variable u2 = new Variable("U2");
            Criterion c1 = new Criterion("Cost", "euros");
            Criterion c2 = new Criterion("Effectiveness", "QALYs");
            u1.setDecisionCriterion(c1);
            u2.setDecisionCriterion(c2);

            Node n1 = id.addNode(u1, NodeType.UTILITY);
            Node n2 = id.addNode(u2, NodeType.UTILITY);

            TablePotential p1 = new TablePotential(List.of(u1), PotentialRole.UNSPECIFIED);
            p1.setCriterion(c1);
            n1.addPotential(p1);

            TablePotential p2 = new TablePotential(List.of(u2), PotentialRole.UNSPECIFIED);
            p2.setCriterion(c2);
            n2.addPotential(p2);

            assertEquals(2, id.getNumCriteria());
        }
    }

    // -----------------------------------------------------------------------
    // Agents
    // -----------------------------------------------------------------------

    @Nested
    class AgentTests {

        @Test
        void addAgent() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            id.setAgents(null); // clear agents
            id.modifyAgent(StateAction.ADD, "Agent1", null);

            List<StringWithProperties> agents = id.getAgents();
            assertNotNull(agents);
            assertEquals(1, agents.size());
            assertEquals("Agent1", agents.getFirst().getString());
        }

        @Test
        void addMultipleAgents() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            id.setAgents(new ArrayList<>());
            id.modifyAgent(StateAction.ADD, "A1", null);
            id.modifyAgent(StateAction.ADD, "A2", null);

            assertEquals(2, id.getAgents().size());
        }

        @Test
        void removeAgent() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            id.setAgents(new ArrayList<>());
            id.modifyAgent(StateAction.ADD, "A1", null);
            id.modifyAgent(StateAction.ADD, "A2", null);

            id.modifyAgent(StateAction.REMOVE, "A1", null);

            assertEquals(1, id.getAgents().size());
            assertEquals("A2", id.getAgents().getFirst().getString());
        }

        @Test
        void removeAgentClearsItFromNodes() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            Variable d = new Variable("D", 2);
            d.setAgent(new StringWithProperties("A1"));
            id.addNode(d, NodeType.DECISION);

            id.setAgents(new ArrayList<>());
            id.modifyAgent(StateAction.ADD, "A1", null);
            id.modifyAgent(StateAction.REMOVE, "A1", null);

            assertNull(id.getNode(d).getVariable().getAgent());
        }

        @Test
        void removeLastAgentDoesNotLeaveNull() {
            // B3 fix: removing the last agent should leave an empty list, not null
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            id.setAgents(new ArrayList<>());
            id.modifyAgent(StateAction.ADD, "OnlyAgent", null);
            id.modifyAgent(StateAction.REMOVE, "OnlyAgent", null);

            List<StringWithProperties> agents = id.getAgents();
            assertNotNull(agents, "agents must not be null after removing the last agent");
            assertTrue(agents.isEmpty());
        }

        @Test
        void renameAgentViaDataTable() {
            ProbNet id = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            id.setAgents(new ArrayList<>());
            id.modifyAgent(StateAction.ADD, "Old", null);

            Object[][] dataTable = {{"NewName"}};
            id.modifyAgent(StateAction.RENAME, "", dataTable);

            assertEquals(1, id.getAgents().size());
            assertEquals("NewName", id.getAgents().getFirst().getString());
        }
    }

    // -----------------------------------------------------------------------
    // Potentials (advanced)
    // -----------------------------------------------------------------------

    @Nested
    class PotentialAdvancedTests {

        @Test
        void getPotentialsByRoleReturnsCorrectPotentials() {
            TablePotential pA = new TablePotential(List.of(A), PotentialRole.CONDITIONAL_PROBABILITY);
            nodeA.addPotential(pA);

            List<Potential> result = bn.getPotentialsByRole(PotentialRole.CONDITIONAL_PROBABILITY);
            assertTrue(result.contains(pA));
        }

        @Test
        void getPotentialsByPredicateFilters() {
            TablePotential pA = new TablePotential(List.of(A), PotentialRole.CONDITIONAL_PROBABILITY);
            nodeA.addPotential(pA);

            List<Potential> result = bn.getPotentials(p -> p.getVariables().contains(A));
            assertTrue(result.contains(pA));
        }

        @Test
        void getSortedPotentialsReturnsTopologicalOrder() {
            TablePotential pA = new TablePotential(List.of(A), PotentialRole.CONDITIONAL_PROBABILITY);
            TablePotential pBA = new TablePotential(List.of(B, A), PotentialRole.CONDITIONAL_PROBABILITY);
            TablePotential pCB = new TablePotential(List.of(C, B), PotentialRole.CONDITIONAL_PROBABILITY);
            nodeA.addPotential(pA);
            nodeB.addPotential(pBA);
            nodeC.addPotential(pCB);

            List<Potential> sorted = bn.getSortedPotentials();
            assertEquals(3, sorted.size());
        }

        @Test
        void getConstantPotentialsInitiallyEmpty() {
            assertTrue(bn.getConstantPotentials().isEmpty());
        }

        @Test
        void addConstantPotential() {
            TablePotential constant = new TablePotential(List.of(), PotentialRole.UNSPECIFIED);
            bn.addPotential(constant);

            assertEquals(1, bn.getConstantPotentials().size());
            assertTrue(bn.getConstantPotentials().contains(constant));
        }

        @Test
        void addPotentialWithOriginalProbNetInfersNodeType() {
            ProbNet original = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            Variable d = new Variable("D", 2);
            original.addNode(d, NodeType.DECISION);

            ProbNet target = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            TablePotential pot = new TablePotential(List.of(d), PotentialRole.CONDITIONAL_PROBABILITY);
            target.addPotential(pot, original);

            Node node = target.getNode(d);
            assertNotNull(node);
            assertEquals(NodeType.DECISION, node.getNodeType());
        }

        @Test
        void removePotentialsFromList() {
            TablePotential pA = new TablePotential(List.of(A), PotentialRole.CONDITIONAL_PROBABILITY);
            TablePotential pB = new TablePotential(List.of(B, A), PotentialRole.CONDITIONAL_PROBABILITY);
            nodeA.addPotential(pA);
            nodeB.addPotential(pB);

            bn.removePotentials(List.of(pA, pB));

            assertFalse(nodeA.getPotentials().contains(pA));
            assertFalse(nodeB.getPotentials().contains(pB));
        }

        @Test
        void getProbPotentialsReturnsNonUtilityPotentials() {
            TablePotential pA = new TablePotential(List.of(A), PotentialRole.CONDITIONAL_PROBABILITY);
            nodeA.addPotential(pA);

            List<Potential> probPots = bn.getProbPotentials(A);
            assertTrue(probPots.contains(pA));
        }
    }

    // -----------------------------------------------------------------------
    // Nodes (advanced)
    // -----------------------------------------------------------------------

    @Nested
    class NodeAdvancedTests {

        @Test
        void invertLinkReversesDirection() {
            assertTrue(nodeA.getChildren().contains(nodeB));
            assertFalse(nodeB.getChildren().contains(nodeA));

            bn.invertLink(A, B);

            assertFalse(nodeA.getChildren().contains(nodeB));
            assertTrue(nodeB.getChildren().contains(nodeA));
        }

        @Test
        void moveNodeUpdatesCoordinates() {
            nodeA.setCoordinateX(0);
            nodeA.setCoordinateY(0);

            bn.moveNode(
                    List.of("A"),
                    List.of(new Point2D.Double(100.0, 200.0))
            );

            assertEquals(100.0, nodeA.getCoordinateX(), 1e-9);
            assertEquals(200.0, nodeA.getCoordinateY(), 1e-9);
        }

        @Test
        void moveNodeThrowsOnSizeMismatch() {
            assertThrows(IllegalArgumentException.class, () ->
                    bn.moveNode(List.of("A", "B"), List.of(new Point2D.Double(0, 0)))
            );
        }

        @Test
        void moveNodeIgnoresUnknownNodeName() {
            // B2 fix: should not throw NPE for non-existent node
            assertDoesNotThrow(() ->
                    bn.moveNode(List.of("NonExistent"), List.of(new Point2D.Double(0, 0)))
            );
        }

        @Test
        void addShiftedNodeCreatesTemporalClone() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            Variable t0 = new Variable("X [0]", 2);
            Node original = dbn.addNode(t0, NodeType.CHANCE);
            original.setCoordinateX(10);
            original.setCoordinateY(20);
            original.setPurpose("test purpose");

            Node shifted = dbn.addShiftedNode(original, 1, 50.0, 0.0);

            assertEquals("X [1]", shifted.getVariable().getName());
            assertEquals(1, shifted.getVariable().getTimeSlice());
            assertEquals(60.0, shifted.getCoordinateX(), 1e-9);
            assertEquals(20.0, shifted.getCoordinateY(), 1e-9);
            assertEquals("test purpose", shifted.getPurpose());
        }

        @Test
        void getNodeByVariable() {
            Node found = bn.getNode(A);
            assertNotNull(found);
            assertSame(nodeA, found);
        }

        @Test
        void getNodeByVariableReturnsNullForAbsent() {
            Variable z = new Variable("Z", 2);
            assertNull(bn.getNode(z));
        }

        @Test
        void addNodeConsistentlySetsPosition() {
            ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
            Variable v = new Variable("V", 2);
            net.addNodeConsistently(v, NodeType.CHANCE, new Point2D.Double(150.0, 250.0));

            Node node = net.getNode(v);
            assertNotNull(node);
            assertEquals(150, (int) node.getCoordinateX());
            assertEquals(250, (int) node.getCoordinateY());
            assertFalse(node.getPotentials().isEmpty());
        }

        @Test
        void addNodeConsistentlyDecisionSetsOptimalPolicy() {
            ProbNet net = new ProbNet(InfluenceDiagramType.getUniqueInstance());
            Variable d = new Variable("D", 2);
            net.addNodeConsistently(d, NodeType.DECISION, new Point2D.Double(0, 0));

            Node node = net.getNode(d);
            assertNotNull(node);
            assertEquals(PolicyType.OPTIMAL, node.getPolicyType());
        }
    }

    // -----------------------------------------------------------------------
    // Metadata and properties
    // -----------------------------------------------------------------------

    @Nested
    class MetadataTests {

        @Test
        void nameDefaultIsNull() {
            ProbNet net = new ProbNet();
            assertNull(net.getName());
        }

        @Test
        void setAndGetName() {
            bn.setName("TestNet");
            assertEquals("TestNet", bn.getName());
        }

        @Test
        void commentDefaultIsEmpty() {
            ProbNet net = new ProbNet();
            assertEquals("", net.getComment());
        }

        @Test
        void setAndGetComment() {
            bn.setComment("A test network");
            assertEquals("A test network", bn.getComment());
        }

        @Test
        void showCommentWhenOpeningDefaultIsFalse() {
            assertFalse(bn.getShowCommentWhenOpening());
        }

        @Test
        void setShowCommentWhenOpening() {
            bn.setShowCommentWhenOpening(true);
            assertTrue(bn.getShowCommentWhenOpening());
        }

        @Test
        void getDefaultStatesReturnsDefensiveCopy() {
            State[] states1 = bn.getDefaultStates();
            State[] states2 = bn.getDefaultStates();
            assertNotSame(states1, states2);
            assertEquals(states1.length, states2.length);
            assertEquals(states1[0].getName(), states2[0].getName());
        }

        @Test
        void setDefaultStates() {
            State[] custom = {new State("low"), new State("medium"), new State("high")};
            bn.setDefaultStates(custom);
            assertEquals(3, bn.getDefaultStates().length);
            assertEquals("low", bn.getDefaultStates()[0].getName());
        }

        @Test
        void additionalPropertiesInitiallyEmpty() {
            assertTrue(bn.getAdditionalProperties().isEmpty());
        }

        @Test
        void putAndGetAdditionalProperty() {
            bn.putAdditionalProperty("key", "value");
            assertEquals("value", bn.getAdditionalProperties().get("key"));
        }

        @Test
        void setAdditionalPropertiesReplacesAll() {
            bn.putAdditionalProperty("old", "1");
            bn.setAdditionalProperties(java.util.Map.of("new", "2"));
            assertNull(bn.getAdditionalProperties().get("old"));
            assertEquals("2", bn.getAdditionalProperties().get("new"));
        }

        @Test
        void setAdditionalPropertiesWithNullClearsAll() {
            bn.putAdditionalProperty("k", "v");
            bn.setAdditionalProperties(null);
            assertTrue(bn.getAdditionalProperties().isEmpty());
        }

        @Test
        void getAdditionalPropertiesIsUnmodifiable() {
            bn.putAdditionalProperty("k", "v");
            assertThrows(UnsupportedOperationException.class,
                    () -> bn.getAdditionalProperties().put("x", "y"));
        }

        @Test
        void cycleLengthAlwaysInitialized() {
            // cycleLength is initialized in the constructor before setNetworkType
            assertNotNull(bn.getCycleLength());
        }

        @Test
        void cycleLengthInitializedForTemporalNetwork() {
            ProbNet dbn = new ProbNet(DynamicLimidType.getUniqueInstance());
            assertNotNull(dbn.getCycleLength());
        }

        @Test
        void setAndGetInferenceOptions() {
            assertNotNull(bn.getInferenceOptions());
        }

        @Test
        void getPNESupport() {
            assertNotNull(bn.getPNESupport());
        }

        @Test
        void toStringContainsNetworkType() {
            String str = bn.toString();
            assertNotNull(str);
            assertTrue(str.contains("Bayesian"));
        }
    }
    
    // -----------------------------------------------------------------------
    // Network construction utilities
    // -----------------------------------------------------------------------

    @Nested
    class BuildTests {

        @Test
        void buildMarkovDecisionNetworkCreatesUndirectedNetwork() {
            TablePotential pot = new TablePotential(List.of(A, B), PotentialRole.CONDITIONAL_PROBABILITY);
            ProbNet markov = bn.buildMarkovDecisionNetwork(List.of(pot));

            assertNotNull(markov);
            assertTrue(markov.hasConstraintOfClass(OnlyUndirectedLinks.class));
            assertTrue(markov.containsVariable(A));
            assertTrue(markov.containsVariable(B));
        }

        @Test
        void defaultConstructorCreatesBayesianNetwork() {
            ProbNet net = new ProbNet();
            assertEquals(BayesianNetworkType.getUniqueInstance(), net.getNetworkType());
        }
    }

    // -----------------------------------------------------------------------
    // Static utilities
    // -----------------------------------------------------------------------

    @Nested
    class StaticUtilityTests {

        @Test
        void getVariablesFromNodesExtractsVariables() {
            List<Variable> vars = ProbNet.getVariables(List.of(nodeA, nodeB));
            assertEquals(2, vars.size());
            assertTrue(vars.contains(A));
            assertTrue(vars.contains(B));
        }

        @Test
        void getVariablesFromNullReturnsEmptyList() {
            List<Variable> vars = ProbNet.getVariables((java.util.Collection<Node>) null);
            assertNotNull(vars);
            assertTrue(vars.isEmpty());
        }
    }
}
