/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.io.ProbNetWriter;
import org.openmarkov.core.io.format.annotation.FormatManager;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.State;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.CycleLengthShift;
import org.openmarkov.core.model.network.potential.GLMPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.WeibullHazardPotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;
import org.openmarkov.java.classUtils.ClassUtils;

import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A file that OpenMarkov has just saved follows the schema with which «Open» checks it, whatever relation each node
 * has among those that the dialog of the node offers.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class WhatTheWritersWriteFollowsTheSchemaTest {

    private static final List<String> NETWORKS = List.of("DES-illness.pgmx", "MID-mammography.pgmx",
                                                         "test-ici-reading.pgmx", "ImposedPolicy.pgmx",
                                                         "BN-conditional-gaussian.pgmx", "test-decpomdp.pgmx");

    private static ProbNetWriter writer(String format) {
        return format.equals("0.2") ? new PGMXWriter_0_2() : new PGMXWriter_1_0();
    }

    /** The problems of the schema of {@code network} saved in {@code format}; none if the format refuses it. */
    private static List<String> problemsOfSaving(ProbNet network, String format, Path file) throws Exception {
        try {
            writer(format).write(file.toString(), network, List.of());
        } catch (WriterException.RelationNotSupportedByFormat | WriterException.NetworkTypeNotSupportedByFormat refused) {
            return List.of();
        }
        return FormatManager.getInstance().schemaProblems(file.toUri().toURL());
    }

    /** The relation of {@code type} for {@code node}, as the dialog of the node makes it, with every part filled. */
    private static Potential relation(Class<? extends Potential> type, Node node, ProbNet network) {
        Potential current = node.getPotential();
        Potential potential = type == CycleLengthShift.class
                ? PotentialUtils.instanciateSafely(type, current.getVariables(), current.getPotentialRole(),
                                                   network.getCycleLength())
                : PotentialUtils.instanciateSafely(type, current.getVariables(), current.getPotentialRole());
        if (potential instanceof WeibullHazardPotential weibull) {
            network.getVariables().stream().filter(v -> v.getVariableType() == VariableType.NUMERIC).findFirst()
                   .ifPresent(weibull::setTimeVariable);
        }
        if (potential instanceof GLMPotential regression) {
            regression.setCholeskyDecomposition(new double[]{1});
        }
        return potential;
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void everyRelationOfferedForEveryNodeIsSavedFollowingTheSchema(String format, @TempDir Path folder)
            throws Exception {
        List<String> problems = new ArrayList<>();
        int saved = 0;
        for (String name : NETWORKS) {
            URL url = getClass().getClassLoader().getResource(name);
            ProbNet network = new PGMXReader().read(url).probNet();
            for (Node node : network.getNodes().stream().filter(n -> n.getPotential() != null).toList()) {
                Potential original = node.getPotential();
                // The dialog offers only the concrete types
                for (Class<? extends Potential> type : PotentialUtils.getFilteredPotentialClasses(node).stream()
                                                                     .filter(ClassUtils::isConcrete).toList()) {
                    node.setPotential(relation(type, node, network));
                    Path file = folder.resolve("network" + saved++ + ".pgmx");
                    problemsOfSaving(network, format, file).forEach(
                            problem -> problems.add(name + ", " + node.getName() + ", " + type.getSimpleName() + ": "
                                                    + problem));
                    node.setPotential(original);
                }
            }
        }

        assertEquals(List.of(), problems);
    }

    @ParameterizedTest @ValueSource(strings = {"0.2", "1.0"})
    void aBranchThatRefersToTheLabelOfAnotherFollowsTheSchema(String format, @TempDir Path folder) throws Exception {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable x = new Variable("X", "no", "yes");
        Variable y = new Variable("Y", "no", "yes");
        network.addNode(y, NodeType.CHANCE).setPotential(new TablePotential(List.of(y), PotentialRole.CONDITIONAL_PROBABILITY));
        network.addNode(x, NodeType.CHANCE);
        network.addLink(y, x, true);
        List<Variable> variables = List.of(x, y);
        TreeADDBranch labelled = new TreeADDBranch(List.of(y.getStates()[0]), y,
                new TablePotential(List.of(x), PotentialRole.CONDITIONAL_PROBABILITY, new double[]{0.9, 0.1}), variables);
        labelled.setLabel("L");
        TreeADDBranch referring = new TreeADDBranch(List.<State>of(y.getStates()[1]), y, "L", variables);
        network.getNode(x).setPotential(new TreeADDPotential(variables, y, PotentialRole.CONDITIONAL_PROBABILITY,
                                                             List.of(labelled, referring)));

        assertEquals(List.of(), problemsOfSaving(network, format, folder.resolve("network.pgmx")));
    }

    @Test void theNetworkOfEventsOfTheTestsFollowsTheSchemaAsItIs() throws Exception {
        assertEquals(List.of(), FormatManager.getInstance().schemaProblems(
                getClass().getClassLoader().getResource("DES-illness.pgmx")));
    }
}
