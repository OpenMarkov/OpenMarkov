/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.action.base.linkEdits.AddLinkEdit;
import org.openmarkov.core.action.base.linkEdits.RemoveLinkEdit;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.ProductPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * A utility node that multiplies its parents, or combines them with a formula, keeps the product or
 * the formula when a parent is added or removed.
 *
 * @author Manuel Arias
 */
public class ASuperValueNodeKeepsItsProductOrFormulaTest {

    private static double expectedUtility(ProbNet net) throws Exception {
        return new VEEvaluation(net).getUtility().getValues()[0];
    }

    private static List<String> variablesOf(Node node) {
        return node.getPotentials().getFirst().getVariables().stream().map(Variable::getName).toList();
    }

    /** In ID-mediastinet.pgmx, "Net_QALE" is the product of three utilities. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void aProductGainsAParentAndLosesIt() throws Exception {
        URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith("/id/ID-mediastinet.pgmx"))
                          .findFirst().orElseThrow();
        ProbNet net = new PGMXReader().read(url).probNet();
        double expectedUtility = expectedUtility(net);
        Node netQALE = net.getNode("Net_QALE");
        Variable tbnaMorbidity = net.getVariable("TBNA_Morbidity");

        new AddLinkEdit(net, tbnaMorbidity, netQALE.getVariable(), true).executeEdit();
        assertInstanceOf(ProductPotential.class, netQALE.getPotentials().getFirst());
        assertEquals(List.of("Net_QALE", "Inmediate_Survival", "MED_Survival", "Survivors_QALE", "TBNA_Morbidity"),
                variablesOf(netQALE));

        new RemoveLinkEdit(net, tbnaMorbidity, netQALE.getVariable(), true).executeEdit();
        assertInstanceOf(ProductPotential.class, netQALE.getPotentials().getFirst());
        assertEquals(List.of("Net_QALE", "Inmediate_Survival", "MED_Survival", "Survivors_QALE"), variablesOf(netQALE));
        assertEquals(expectedUtility, expectedUtility(net), 1E-9);
    }

    /** "U0" is abs({U1})*{U2}, which is -6; the new parent is not in the formula. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void aFormulaGainsAParent() throws Exception {
        ProbNet net = new PGMXReader().read(getClass().getResource(
                "/networks/dan/DAN-two-utility-and-chance-function-sv.pgmx")).probNet();
        Node u0 = net.getNode("U0");
        Variable u3 = new Variable("U3");
        u3.setDecisionCriterion(u0.getVariable().getDecisionCriterion());
        net.addNode(u3, NodeType.UTILITY).setPotential(new ExactDistrPotential(List.of(u3),
                u0.getPotentials().getFirst().getPotentialRole(), new double[] { 5 }));

        new AddLinkEdit(net, u3, u0.getVariable(), true).executeEdit();
        assertInstanceOf(FunctionPotential.class, u0.getPotentials().getFirst());
        assertEquals("abs({U1})*{U2}", u0.getPotentials().getFirst().toString());
        assertEquals(-6, expectedUtility(net), 1E-9);
    }
}
