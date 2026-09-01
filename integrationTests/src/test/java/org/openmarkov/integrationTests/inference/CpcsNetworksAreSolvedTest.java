/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The four CPCS networks, of increasing size, up to 422 nodes and 17 parents on a single node.
 * They come from the CPCS-PM patient case simulation system (Pradhan et al., 1994) and reach this
 * repository through the UAI 2008 evaluation set. Their tables arrive already expanded, so they
 * carry no canonical model: what they bring is the size and the number of parents.
 *
 * @author Manuel Arias
 */
public class CpcsNetworksAreSolvedTest {

    /**
     * How many nodes, links and parents each network has.
     */
    private record Profile(String file, int nodes, int links, int largestFanIn, int fromFourParents) {
    }
    
    private static final List<Profile> TEST_PROFILES = List.of(
            new Profile("networks/bn/BN-cpcs54.pgmx", 54, 108, 9, 8),
            new Profile("networks/bn/BN-cpcs179.pgmx", 179, 239, 8, 6)
            //These two profiles take way too long for tests to complete
            //new Profile("networks/bn/BN-cpcs360b.pgmx", 360, 729, 11, 51),
            //new Profile("networks/bn/BN-cpcs422b.pgmx", 422, 867, 17, 59)
    );

    private static Stream<Profile> everyNetwork() {
        return TEST_PROFILES.stream();
    }

    @Tag(TestSpeed.SLOW)
    @ParameterizedTest(name = "{0}")
    @MethodSource("everyNetwork")
    public void theNetworkIsReadWholeAndKeepsItsShape(Profile profile) throws Exception {
        ProbNet network = load(profile.file());

        assertEquals(profile.nodes(), network.getNumNodes(), "nodes");
        assertEquals(profile.links(), network.getLinks().size(), "links");

        int largestFanIn = 0;
        int fromFourParents = 0;
        for (Node node : network.getNodes()) {
            int parents = network.getParents(node).size();
            largestFanIn = Math.max(largestFanIn, parents);
            if (parents >= 4) {
                fromFourParents++;
            }
        }
        assertEquals(profile.largestFanIn(), largestFanIn, "parents of the node with the most");
        assertEquals(profile.fromFourParents(), fromFourParents, "nodes with four parents or more");
    }

    @Tag(TestSpeed.SLOW)
    @ParameterizedTest(name = "{0}")
    @MethodSource("everyNetwork")
    public void everyPosteriorIsADistribution(Profile profile) throws Exception {
        ProbNet network = load(profile.file());
        List<Variable> ofInterest = network.getNodes(NodeType.CHANCE).stream().map(Node::getVariable).toList();

        VEPropagation propagation = new VEPropagation(network);
        propagation.setVariablesOfInterest(ofInterest);
        HashMap<Variable, TablePotential> posteriors = propagation.getPosteriorValues();

        assertEquals(ofInterest.size(), posteriors.size(), "one posterior per chance variable");
        for (var posterior : posteriors.entrySet()) {
            double added = 0;
            for (double value : posterior.getValue().getValues()) {
                assertTrue(value >= -1E-9 && value <= 1 + 1E-9,
                        () -> "value outside [0,1] in " + posterior.getKey().getName());
                added += value;
            }
            assertEquals(1.0, added, 1E-9, "the posterior of " + posterior.getKey().getName() + " adds up");
        }
    }

    // ------------------------------------------------------------- helpers

    private static ProbNet load(String file) throws Exception {
        return new PGMXReader()
                .read(CpcsNetworksAreSolvedTest.class.getClassLoader().getResource(file))
                .probNet();
    }
}
