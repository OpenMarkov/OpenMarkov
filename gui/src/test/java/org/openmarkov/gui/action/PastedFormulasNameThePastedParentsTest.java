/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.gui.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.type.InfluenceDiagramType;
import org.openmarkov.gui.window.edition.SelectedContent;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pasting a node whose relation is a formula together with the parent the formula names. In the
 * same network the pasted parent gets a new name and the formula kept the old one, so the pasted
 * node could not be computed (error 234 of the pending list).
 */
public class PastedFormulasNameThePastedParentsTest {

    private ProbNet probNet;
    private Node age;
    private Node cost;

    @BeforeEach
    void setUp() {
        probNet = new ProbNet(InfluenceDiagramType.getUniqueInstance());
        age = probNet.addNode(new Variable("Age"), NodeType.CHANCE);
        cost = probNet.addNode(new Variable("Cost"), NodeType.UTILITY);
        probNet.addLink(age, cost, true);
        List<Variable> variables = List.of(cost.getVariable(), age.getVariable());
        cost.setPotentials(List.of(new FunctionPotential(variables, PotentialRole.UNSPECIFIED,
                new VariableExpression(variables, "{Age}*3"))));
    }

    private static double costAt40(ProbNet net, String costName, String ageName) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(net.getVariable(ageName), 40.0));
        return ((FunctionPotential) net.getNode(costName).getPotential()).sampleConditionedVariable(new double[]{0}, evidence);
    }

    @Test
    void aFormulaPastedInTheSameNetworkNamesThePastedParent() throws Exception {
        new PasteEdit(probNet, new SelectedContent(List.of(age, cost), List.of(probNet.getLink(age, cost, true))), null)
                .executeEdit();

        FunctionPotential pasted = (FunctionPotential) probNet.getNode("Cost'").getPotential();
        assertEquals("{Age'}*3", pasted.getFunction().asStringExpression());
        assertEquals(120, costAt40(probNet, "Cost'", "Age'"));
        assertEquals(120, costAt40(probNet, "Cost", "Age"), "the original keeps its own");
    }

    @Test
    void aFormulaPastedInAnotherNetworkIsWrittenOnTheNodesOfThatNetwork() throws Exception {
        ProbNet other = new ProbNet(InfluenceDiagramType.getUniqueInstance());

        new PasteEdit(other, new SelectedContent(List.of(age, cost), List.of(probNet.getLink(age, cost, true))), null)
                .executeEdit();

        FunctionPotential pasted = (FunctionPotential) other.getNode("Cost").getPotential();
        assertSame(other.getVariable("Age"), pasted.getFunction().references().getFirst());
        assertEquals(120, costAt40(other, "Cost", "Age"));
    }
}
