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
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.FunctionPotential;
import org.openmarkov.core.model.network.potential.PiecewiseExponentialPotential;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.WeibullHazardPotential;
import org.openmarkov.core.model.network.potential.plugin.PotentialUtils;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * «Checkup», of DES-illness.pgmx, has a link to itself, so its relation names it twice.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AnEventWithALinkToItselfKeepsItsFormulaTest {

    private ProbNet network() throws Exception {
        return new PGMXReader().read(getClass().getClassLoader().getResource("DES-illness.pgmx")).probNet();
    }

    /** Gives «Checkup» a relation of {@code type}, as the dialog of the node does. */
    private static Potential giveCheckup(Class<? extends Potential> type, ProbNet network) {
        Node checkup = network.getNode("Checkup");
        Potential potential = PotentialUtils.instanciateSafely(type, checkup.getPotential().getVariables(),
                                                               checkup.getPotential().getPotentialRole());
        checkup.setPotential(potential);
        return potential;
    }

    @ParameterizedTest @ValueSource(classes = {FunctionPotential.class, WeibullHazardPotential.class})
    void aFormulaSavedInFormat10IsOpenedAgain(Class<? extends Potential> type, @TempDir Path folder) throws Exception {
        ProbNet network = network();
        List<String> variables = giveCheckup(type, network).getVariables().stream().map(Variable::getName).toList();
        Path file = folder.resolve("network.pgmx");

        new PGMXWriter_1_0().write(file.toString(), network, List.of());
        Potential reread = new PGMXReader().read(file.toUri().toURL()).probNet().getNode("Checkup").getPotential();

        assertInstanceOf(type, reread);
        assertEquals(variables, reread.getVariables().stream().map(Variable::getName).toList());
    }

    @Test void aPiecewiseExponentialCanBeChosen() throws Exception {
        assertInstanceOf(PiecewiseExponentialPotential.class, giveCheckup(PiecewiseExponentialPotential.class, network()));
    }
}
