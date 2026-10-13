/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.graph.Link;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.DESSimulablePotential;
import org.openmarkov.core.model.network.potential.DistributionTablePotential;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

/**
 * Pasting the nodes of a discrete-event simulation network. The pasted relations kept looking
 * inside for the variables of the original nodes, so every pasted node answered as if each of its
 * parents were in its first state (error 21 of the pending list).
 *
 * <p>The network: a therapy and the sex decide when the onset happens; the onset makes the patient
 * ill; death comes after the onset or after a checkup, sooner if ill; and being ill has a cost.
 */
public class PastedEventNodesAnswerLikeTheOriginalsTest {

    private static final double[] RANDOM = {0.5};

    private ProbNet original;
    private List<Node> nodes;
    private List<Link<Node>> links;

    private Node node(String name, NodeType nodeType, VariableType variableType, String... states) {
        Variable variable;
        if (variableType == VariableType.NUMERIC) {
            variable = new Variable(name);
        } else {
            State[] variableStates = new State[states.length];
            for (int i = 0; i < states.length; i++) {
                variableStates[i] = new State(states[i]);
            }
            variable = new Variable(name, variableStates);
            variable.setVariableType(variableType);
        }
        Node node = original.addNode(variable, nodeType);
        nodes.add(node);
        return node;
    }

    private void relation(Node child, Potential potential, Node... parents) {
        for (Node parent : parents) {
            original.addLink(parent, child, true);
            links.add(original.getLink(parent, child, true));
        }
        child.setPotentials(List.of(potential));
    }

    private static List<Variable> variables(Node child, Node... parents) {
        List<Variable> variables = new ArrayList<>();
        variables.add(child.getVariable());
        for (Node parent : parents) {
            variables.add(parent.getVariable());
        }
        return variables;
    }

    @BeforeEach
    void setUp() {
        original = new ProbNet(DESNetworkType.getUniqueInstance());
        nodes = new ArrayList<>();
        links = new ArrayList<>();
        Node therapy = node("Therapy", NodeType.DECISION, VariableType.FINITE_STATES, "none", "drug");
        Node sex = node("Sex", NodeType.CHANCE, VariableType.FINITE_STATES, "male", "female");
        Node onset = node("Onset", NodeType.EVENT, VariableType.EVENT, "no", "yes");
        Node checkup = node("Checkup", NodeType.EVENT, VariableType.EVENT, "no", "yes");
        Node ill = node("Ill", NodeType.CHANCE, VariableType.FINITE_STATES, "no", "yes");
        Node death = node("Death", NodeType.EVENT, VariableType.EVENT, "no", "yes");
        Node careCost = node("CareCost", NodeType.UTILITY, VariableType.NUMERIC);

        // Years to the onset: 2 or 5 for men (none, drug) and 3 or 6 for women
        DistributionTablePotential timeToOnset = new DistributionTablePotential(
                variables(onset, therapy, sex), PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
        timeToOnset.getTableWithEvents().setValues(new double[]{2, 5, 3, 6});
        relation(onset, timeToOnset, therapy, sex);

        // The onset makes the patient ill and the checkup cures
        TransitionTablePotential becomingIll = new TransitionTablePotential(
                variables(ill, onset, checkup), PotentialRole.CONDITIONAL_PROBABILITY);
        becomingIll.setValues(new double[]{0, 1, 1, 0});
        relation(ill, becomingIll, onset, checkup);

        // Years to death: 8 or 3 after the onset and 5 or 1 after the checkup (healthy, ill)
        DistributionTablePotential timeToDeath = new DistributionTablePotential(
                variables(death, onset, checkup, ill), PotentialRole.CONDITIONAL_PROBABILITY, "Exact", "Nu");
        timeToDeath.getTableWithEvents().setValues(new double[]{8, 3, 5, 1});
        relation(death, timeToDeath, onset, checkup, ill);

        relation(careCost, new ExactDistrPotential(variables(careCost, ill), PotentialRole.UNSPECIFIED,
                new double[]{100, 2000}), ill);
    }

    private static double answer(ProbNet net, String nodeName, Object... namesAndStates) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        for (int i = 0; i < namesAndStates.length; i += 2) {
            evidence.addFinding(new Finding(net.getVariable((String) namesAndStates[i]), (int) namesAndStates[i + 1]));
        }
        return ((DESSimulablePotential) net.getNode(nodeName).getPotential()).sampleConditionedVariable(RANDOM, evidence);
    }

    private static void assertAnswers(ProbNet net, String pastedSuffix) throws Exception {
        String s = pastedSuffix;
        assertEquals(6, answer(net, "Onset" + s, "Therapy" + s, 1, "Sex" + s, 1), "onset with the drug, for a woman");
        assertEquals(5, answer(net, "Onset" + s, "Therapy" + s, 1, "Sex" + s, 0), "onset with the drug, for a man");
        assertEquals(1, answer(net, "Ill" + s, "Onset" + s, 1), "ill after the onset");
        assertEquals(0, answer(net, "Ill" + s, "Checkup" + s, 1), "healthy after the checkup");
        assertEquals(3, answer(net, "Death" + s, "Onset" + s, 1, "Ill" + s, 1), "death after the onset, ill");
        assertEquals(1, answer(net, "Death" + s, "Checkup" + s, 1, "Ill" + s, 1), "death after the checkup, ill");
        assertEquals(2000, answer(net, "CareCost" + s, "Ill" + s, 1), "cost when ill");
    }

    @Test
    void theOriginalNetworkAnswersWhatItsTablesSay() throws Exception {
        assertAnswers(original, "");
    }

    @Test
    void nodesPastedInAnotherNetworkAnswerLikeTheOriginals() throws Exception {
        ProbNet other = new ProbNet(DESNetworkType.getUniqueInstance());

        new PasteEdit(other, new SelectedContent(nodes, links), null).executeEdit();

        assertNotSame(original.getVariable("Sex"), other.getVariable("Sex"));
        assertAnswers(other, "");
    }

    /** In the same network the pasted nodes get a new name, and so do the events the tables are indexed by. */
    @Test
    void nodesPastedInTheSameNetworkAnswerLikeTheOriginals() throws Exception {
        new PasteEdit(original, new SelectedContent(nodes, links), null).executeEdit();

        assertAnswers(original, "'");
        assertAnswers(original, "");
    }
}
