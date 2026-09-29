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
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.Finding;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * A finding on a numeric chance variable entered after the resolution, as inference mode does, is propagated
 * as the same finding entered before it.
 *
 * @author Manuel Arias
 */
public class ANumericFindingAfterTheResolutionIsPropagatedTest {

    private static ProbNet read(String path) throws Exception {
        return new PGMXReader().read(Networks.getNetworks().filter(u -> u.getPath().endsWith(path))
                                             .findFirst().orElseThrow()).probNet();
    }

    private static EvidenceCase finding(Variable variable, double value) throws Exception {
        EvidenceCase evidence = new EvidenceCase();
        evidence.addFinding(new Finding(variable, value));
        return evidence;
    }

    /** The posterior probabilities of {@code variables}, by name. */
    private static Map<String, double[]> propagate(ProbNet net, List<Variable> variables, EvidenceCase preResolution,
                                                   EvidenceCase postResolution) throws Exception {
        VEPropagation propagation = new VEPropagation(net);
        propagation.setVariablesOfInterest(variables);
        propagation.setPreResolutionEvidence(preResolution);
        propagation.setPostResolutionEvidence(postResolution);
        Map<String, double[]> posteriors = new TreeMap<>();
        propagation.getPosteriorValues().forEach((variable, potential) -> posteriors.put(variable.getName(), potential.getValues()));
        return posteriors;
    }

    /** MID-Colorectal, with the screening from 50 imposed: the age changes the results of the colonoscopy. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void theAgeInTheColorectalModel() throws Exception {
        ProbNet net = read("/mid/MID-Colorectal.pgmx");
        Node screening = net.getNode("Screening policy");
        screening.setPotential(new TablePotential(List.of(screening.getVariable()), PotentialRole.POLICY, new double[] { 0, 1, 0 }));
        Variable age = net.getVariable("Age [0]");
        List<Variable> variables = List.of(net.getVariable("Colonoscopy result [0]"), net.getVariable("CRC stage [1]"));

        Map<String, double[]> before = propagate(net, variables, finding(age, 50), new EvidenceCase());
        Map<String, double[]> after = propagate(net, variables, new EvidenceCase(), finding(age, 50));
        assertEquals(before.keySet(), after.keySet());
        before.forEach((name, values) -> assertArrayEquals(values, after.get(name), 1E-12, name));
        assertNotEquals(after.get("Colonoscopy result [0]")[0],
                propagate(net, variables, new EvidenceCase(), finding(age, 70)).get("Colonoscopy result [0]")[0], 1E-3);
    }

    /** MID-dmhee-2.5: the duration of the treatment starts at 0. */
    @Tag(TestSpeed.MEDIUM)
    @Test public void theDurationOfTheTreatmentInDmhee() throws Exception {
        ProbNet net = read("/mid/DMHEE/MID-dmhee-2.5.pgmx");
        Variable duration = net.getVariable("Duration of treatment [0]");
        List<Variable> variables = List.of(net.getVariable("State [1]"));
        assertArrayEquals(propagate(net, variables, new EvidenceCase(), new EvidenceCase()).get("State [1]"),
                propagate(net, variables, new EvidenceCase(), finding(duration, 0)).get("State [1]"), 1E-12);
    }
}
