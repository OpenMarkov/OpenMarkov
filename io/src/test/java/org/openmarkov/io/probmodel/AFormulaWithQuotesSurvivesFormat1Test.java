/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * A formula names a node between braces, and the name of a node may have quotes and backslashes. Format 1.0
 * writes the formulas of a relation between quotes, and reads back each one as it was.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AFormulaWithQuotesSurvivesFormat1Test {

    @TempDir Path folder;

    private static List<String> formulas(VariableExpression[] expressions) {
        return Arrays.stream(expressions).map(VariableExpression::asStringExpression).toList();
    }

    private ProbNet savedAndOpened(ProbNet net) throws Exception {
        Path file = folder.resolve("network.pgmx");
        new PGMXWriter_1_0().write(file.toString(), net, List.of());
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    private static Variable numeric(String name) {
        Variable variable = new Variable(name);
        variable.setVariableType(VariableType.NUMERIC);
        return variable;
    }

    @ParameterizedTest @ValueSource(strings = {"Dose mg", "Dose \"mg\"", "\"Dose\"", "Dose \\mg", "Dose mg\\", "Dose \\\"mg\\\""})
    void theParametersOfADistribution(String name) throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable dose = numeric(name);
        Variable x = numeric("X");
        Node doseNode = net.addNode(dose, NodeType.CHANCE);
        Node xNode = net.addNode(x, NodeType.CHANCE);
        net.addLink(doseNode, xNode, true);
        List<Variable> variables = new ArrayList<>(List.of(x, dose));
        UnivariateDistrPotential distribution = new UnivariateDistrPotential(variables, "Normal", "mu, sigma",
                                                                             PotentialRole.CONDITIONAL_PROBABILITY);
        distribution.setParameter(0, new VariableExpression(variables, "2 * {" + name + "}"));
        distribution.setParameter(1, new VariableExpression(variables, "1"));
        xNode.setPotential(distribution);
        List<String> written = formulas(distribution.getDistributionTable().getFunctionValues());

        ProbNet opened = savedAndOpened(net);

        UnivariateDistrPotential read = (UnivariateDistrPotential) opened.getNode("X").getPotential();
        assertEquals(written, formulas(read.getDistributionTable().getFunctionValues()));
        assertSame(opened.getNode(name).getVariable(),
                   read.getDistributionTable().getFunctionValues()[0].references().getFirst());
    }

    @ParameterizedTest @ValueSource(strings = {"Dose mg", "Dose \"mg\"", "Dose \\mg"})
    void theCellsOfATableWithFormulas(String name) throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable dose = numeric(name);
        Variable y = new Variable("Y", "no", "yes");
        Node doseNode = net.addNode(dose, NodeType.CHANCE);
        Node yNode = net.addNode(y, NodeType.CHANCE);
        net.addLink(doseNode, yNode, true);
        List<Variable> variables = new ArrayList<>(List.of(y, dose));
        AugmentedProbTablePotential table = new AugmentedProbTablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        VariableExpression[] cells = table.getAugmentedProbTable().getFunctionValues();
        cells[0] = new VariableExpression(variables, "1 - {" + name + "}/100");
        cells[1] = new VariableExpression(variables, "{" + name + "}/100");
        yNode.setPotential(table);

        ProbNet opened = savedAndOpened(net);

        AugmentedProbTablePotential read = (AugmentedProbTablePotential) opened.getNode("Y").getPotential();
        assertEquals(List.of("1 - {" + name + "}/100", "{" + name + "}/100"),
                     formulas(read.getAugmentedProbTable().getFunctionValues()));
    }
}
