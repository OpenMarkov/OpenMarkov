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
import org.openmarkov.core.exception.WriterException;
import org.openmarkov.core.expression.VariableExpression;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.modelUncertainty.NormalFunction;
import org.openmarkov.core.model.network.potential.AugmentedProbTablePotential;
import org.openmarkov.core.model.network.potential.DeltaPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A writer does not save what the reader of its format could not read back. Format 0.2 wrote a network of
 * events without its Monte Carlo options and with the relations of its events reduced to their variables, without
 * a warning, and the file could not be opened (error 235 of the pending list). Now it refuses with a message and
 * writes nothing.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
public class WhatAFormatCannotReadBackIsNotSavedTest {

    private ProbNet read(String resource) throws Exception {
        return new PGMXReader().read(getClass().getClassLoader().getResource(resource)).probNet();
    }

    private static ProbNet read(Path file) throws Exception {
        return new PGMXReader().read(file.toUri().toURL()).probNet();
    }

    /** The class of the relation of each node, by its name. */
    private static String relations(ProbNet probNet) {
        StringBuilder relations = new StringBuilder();
        for (Node node : probNet.getNodes()) {
            relations.append(node.getName()).append(':');
            node.getPotentials().forEach(potential -> relations.append(' ').append(potential.getClass().getSimpleName()));
            relations.append('\n');
        }
        return relations.toString();
    }

    @Test public void aNetworkOfEventsIsNotSavedInFormat02(@TempDir Path folder) throws Exception {
        ProbNet network = read("DES-illness.pgmx");
        Path file = folder.resolve("network.pgmx");

        var refused = assertThrows(WriterException.NetworkTypeNotSupportedByFormat.class,
                () -> new PGMXWriter_0_2().write(file.toString(), network, List.of()));

        assertEquals("The network is not saved: a network of type DESNet cannot be saved in format 0.2.0. "
                + "Save it in another format.", refused.getExceptionMessage());
        assertFalse(Files.exists(file));
    }

    @Test public void aNetworkOfEventsSavedInFormat10OpensTheSame(@TempDir Path folder) throws Exception {
        ProbNet network = read("DES-illness.pgmx");
        Path file = folder.resolve("network.pgmx");

        new PGMXWriter_1_0().write(file.toString(), network, List.of());
        ProbNet reread = read(file);

        assertEquals(relations(network), relations(reread));
        var options = network.getInferenceOptions().getMonteCarloOptions();
        var rereadOptions = reread.getInferenceOptions().getMonteCarloOptions();
        assertEquals(options.getNumSimulations(), rereadOptions.getNumSimulations());
        assertEquals(options.getNumSeries(), rereadOptions.getNumSeries());
    }

    @Test public void aRelationThatFormat02CannotReadIsNotSaved(@TempDir Path folder) throws Exception {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable cost = new Variable("Cost", true, 0, 100, true, 0.1);
        network.addNode(cost, NodeType.CHANCE).setPotential(
                new UnivariateDistrPotential(List.of(cost), NormalFunction.class, PotentialRole.CONDITIONAL_PROBABILITY));
        Path file = folder.resolve("network.pgmx");

        var refused = assertThrows(WriterException.RelationNotSupportedByFormat.class,
                () -> new PGMXWriter_0_2().write(file.toString(), network, List.of()));

        assertEquals("The network is not saved: the relation of Cost, of type UnivariateDistr, cannot be saved in "
                + "format 0.2.0, which could not read it back. Save the network in another format.",
                refused.getExceptionMessage());
        assertFalse(Files.exists(file));

        new PGMXWriter_1_0().write(file.toString(), network, List.of());
        assertInstanceOf(UnivariateDistrPotential.class, read(file).getNode("Cost").getPotentials().getFirst());
    }

    @Test public void aTableOfFormulasIsNotSavedInFormat02(@TempDir Path folder) throws Exception {
        ProbNet network = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable age = new Variable("Age", true, 0, 100, true, 1);
        Variable smoker = new Variable("Smoker", "no", "yes");
        network.addNode(age, NodeType.CHANCE).setPotential(
                new DeltaPotential(List.of(age), PotentialRole.CONDITIONAL_PROBABILITY, 30));
        network.addNode(smoker, NodeType.CHANCE);
        network.addLink(age, smoker, true);
        List<Variable> variables = List.of(smoker, age);
        AugmentedProbTablePotential formulas = new AugmentedProbTablePotential(variables, PotentialRole.CONDITIONAL_PROBABILITY);
        VariableExpression[] cells = formulas.getAugmentedProbTable().getFunctionValues();
        cells[0] = new VariableExpression(variables, "1-{Age}/100");
        cells[1] = new VariableExpression(variables, "{Age}/100");
        network.getNode(smoker).setPotential(formulas);
        Path file = folder.resolve("network.pgmx");

        var refused = assertThrows(WriterException.RelationNotSupportedByFormat.class,
                () -> new PGMXWriter_0_2().write(file.toString(), network, List.of()));

        assertEquals("The network is not saved: the relation of Smoker, of type AugmentedProbTable, cannot be saved in "
                + "format 0.2.0, which could not read it back. Save the network in another format.",
                refused.getExceptionMessage());
        assertFalse(Files.exists(file));

        new PGMXWriter_1_0().write(file.toString(), network, List.of());
        assertInstanceOf(AugmentedProbTablePotential.class, read(file).getNode("Smoker").getPotentials().getFirst());
    }

    @Test public void aNetworkThatFormat02CanReadIsStillSaved(@TempDir Path folder) throws Exception {
        ProbNet network = read("HPV-model-0.2.0.pgmx");
        Path file = folder.resolve("network.pgmx");

        new PGMXWriter_0_2().write(file.toString(), network, List.of());

        assertEquals(relations(network), relations(read(file)));
    }
}
