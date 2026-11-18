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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_0_2;

import java.net.URL;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Saving in format 0.2 neither changes the relation of a utility in the open network nor loses its parents.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
class SavingInFormat02KeepsTheVariablesOfAUtilityTest {

    /** A utility node whose three parents are utilities too. */
    private static final String UTILITY = "Treatment cost [1]";

    private ProbNet network() throws Exception {
        URL file = getClass().getClassLoader().getResource("MID-mammography.pgmx");
        return new PGMXReader().read(file).probNet();
    }

    /** Gives the utility a relation of {@code type}, as the dialog of the node does. */
    private static List<Variable> giveTheUtility(Class<? extends Potential> type, ProbNet network) {
        Node node = network.getNode(UTILITY);
        List<Variable> variables = node.getPotential().getVariables();
        node.setPotential(PotentialUtils.instanciateSafely(type, variables, node.getPotential().getPotentialRole()));
        return variables;
    }

    @Test void aRefusedDistributionLeavesTheOpenNetworkAsItWas(@TempDir Path folder) throws Exception {
        ProbNet network = network();
        List<Variable> variables = giveTheUtility(UnivariateDistrPotential.class, network);

        assertThrows(WriterException.RelationNotSupportedByFormat.class,
                     () -> new PGMXWriter_0_2().write(folder.resolve("network.pgmx").toString(), network, List.of()));

        assertEquals(variables, network.getNode(UTILITY).getPotential().getVariables());
    }

    @Test void aTableOfUtilitiesKeepsItsParentsThatAreUtilities(@TempDir Path folder) throws Exception {
        ProbNet network = network();
        List<Variable> variables = giveTheUtility(ExactDistrPotential.class, network);
        Path file = folder.resolve("network.pgmx");

        new PGMXWriter_0_2().write(file.toString(), network, List.of());
        ProbNet reread = new PGMXReader().read(file.toUri().toURL()).probNet();

        assertEquals(variables.stream().map(Variable::getName).toList(),
                     reread.getNode(UTILITY).getPotential().getVariables().stream().map(Variable::getName).toList());
    }
}
