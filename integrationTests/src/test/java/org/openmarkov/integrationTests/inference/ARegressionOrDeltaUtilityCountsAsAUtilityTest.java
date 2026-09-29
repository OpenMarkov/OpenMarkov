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
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.CEP;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.GTablePotential;
import org.openmarkov.core.model.network.potential.LinearCombinationPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEAnalysis;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEEvaluation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A utility node whose relation is a regression or a delta is computed as a utility, like one whose
 * relation is an exact table with the same numbers.
 *
 * @author Manuel Arias
 */
public class ARegressionOrDeltaUtilityCountsAsAUtilityTest {

    private static ProbNet read(String path) throws Exception {
        URL url = Networks.getNetworks().filter(u -> u.getPath().endsWith(path)).findFirst().orElseThrow();
        return new PGMXReader().read(url).probNet();
    }

    private static PotentialRole roleOf(Node node) {
        return node.getPotentials().getFirst().getPotentialRole();
    }

    private static VariableExpression covariate(Variable variable) {
        return new VariableExpression(List.of(variable), "{" + variable.getName() + "}");
    }

    private static GTablePotential<?> costEffectiveness(ProbNet net, Variable decision) throws Exception {
        VECEAnalysis analysis = new VECEAnalysis(net);
        analysis.setDecisionVariable(decision);
        analysis.setPreResolutionEvidence(new EvidenceCase());
        return analysis.getUtility();
    }

    /** ID-CEA-minimal.pgmx, whose cost is 0 without therapy and 14 000 with it, as a linear combination. */
    private static ProbNet minimalWithLinearCost() throws Exception {
        ProbNet net = read("/id/ID-CEA-minimal.pgmx");
        Node cost = net.getNode("Cost");
        Variable therapy = net.getVariable("Therapy");
        cost.setPotential(new LinearCombinationPotential(List.of(cost.getVariable(), therapy), roleOf(cost),
                new VariableExpression[] { VariableExpression.Common.CONSTANT, covariate(therapy) },
                new double[] { 0, 14000 }));
        return net;
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aLinearCostInTheGlobalAnalysis() throws Exception {
        CEP cep = (CEP) costEffectiveness(minimalWithLinearCost(), null).elementTable.getFirst();
        assertArrayEquals(new double[] { 0, 14000 }, cep.getCosts(), 1E-9);
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aLinearCostInTheAnalysisOfItsDecision() throws Exception {
        ProbNet net = minimalWithLinearCost();
        List<?> ceps = costEffectiveness(net, net.getVariable("Therapy")).elementTable;
        assertEquals(0, ((CEP) ceps.get(0)).getCost(0), 1E-9);
        assertEquals(14000, ((CEP) ceps.get(1)).getCost(0), 1E-9);
    }

    /** The exact table with these numbers gives 151 600. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void aLinearEffectivenessThatDependsOnAChanceVariable() throws Exception {
        ProbNet net = read("/id/ID-CEA-test-2therapies-new-test.pgmx");
        Node qale = net.getNode("QALE");
        Variable disease = net.getVariable("Disease");
        Variable therapy = net.getVariable("Therapy");
        qale.setPotential(new LinearCombinationPotential(List.of(qale.getVariable(), disease, therapy), roleOf(qale),
                new VariableExpression[] { VariableExpression.Common.CONSTANT, covariate(disease), covariate(therapy) },
                new double[] { 5, -2, 1 }));
        assertEquals(151600, new VEEvaluation(net).getUtility().getValues()[0], 1E-6);
    }

    @Tag(TestSpeed.MEDIUM)
    @Test public void aConstantCostDefinedAsADelta() throws Exception {
        ProbNet net = read("/id/ID-CEA-minimal.pgmx");
        Node cost = net.getNode("Cost");
        net.removeLink(net.getVariable("Therapy"), cost.getVariable(), true);
        cost.setPotential(new DeltaPotential(List.of(cost.getVariable()), roleOf(cost), 500));
        CEP cep = (CEP) costEffectiveness(net, null).elementTable.getFirst();
        assertArrayEquals(new double[] { 500 }, cep.getCosts(), 1E-9);
    }
}
