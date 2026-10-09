/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.inference;

import networks.Networks;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.exception.UnrecoverableException;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.modelUncertainty.BetaFunction;
import org.openmarkov.core.model.network.modelUncertainty.UncertainValue;
import org.openmarkov.core.model.network.potential.DistributionsOfAColumn;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDBranch;
import org.openmarkov.core.model.network.potential.treeadd.TreeADDPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECEPSA;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VECESensAnSpider;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnMap;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnPlot;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VESensAnTornadoSpider;
import org.openmarkov.io.probmodel.reader.PGMXReader;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The prevalence of the disease of ID-CEA-test-2therapies-new-test has a Beta for «present» and a complement for
 * «absent». Here the complement is taken away, which leaves the number of «absent», 0.86, fixed next to the Beta.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class SensitivityAnalysisDoesNotSampleDistributionsThatCannotGoTogetherTest {

    private ProbNet probNet;

    @BeforeEach void read() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/ID-CEA-test-2therapies-new-test.pgmx"))
                          .findFirst()
                          .orElseThrow();
        probNet = new PGMXReader().read(url).probNet();
    }

    private void takeTheComplementAway() {
        ((TablePotential) probNet.getNode("Disease").getPotential()).getUncertainValues()[0] = null;
    }

    private void assertRefused(Executable analysis) {
        UnrecoverableException refusal = assertThrows(UnrecoverableException.class, analysis);
        NotEvaluableNetworkException.DistributionsCannotGoTogether cause =
                assertInstanceOf(NotEvaluableNetworkException.DistributionsCannotGoTogether.class, refusal.getCause());
        assertEquals("Disease: State \"absent\" has the fixed value 0.86 next to the Beta distribution of state "
                             + "\"present\". With a fixed value other than 0, a Range or a Triangular, the other "
                             + "states can only be Exact, Range, Triangular or Complement.", cause.wrongColumns);
    }

    @Test void theFiveAnalysesAreRefused() {
        takeTheComplementAway();

        assertRefused(() -> new VECEPSA(probNet));
        assertRefused(() -> new VECESensAnSpider(probNet));
        assertRefused(() -> new VESensAnMap(probNet, null, null, null, null, null, 1));
        assertRefused(() -> new VESensAnPlot(probNet, null, null, null, 1));
        assertRefused(() -> new VESensAnTornadoSpider(probNet, null, List.of(), null, 1));
    }

    /** The transitions of MID-hip-Briggs are in the branches of a tree; here one of them gets two Beta. */
    @Test void aTableInABranchOfATreeIsCheckedToo() throws Exception {
        URL url = Networks.getNetworks()
                          .filter(network -> network.getPath().endsWith("/mid/MID-hip-Briggs.pgmx"))
                          .findFirst()
                          .orElseThrow();
        ProbNet briggs = new PGMXReader().read(url).probNet();
        assertEquals(List.of(), DistributionsOfAColumn.wrongIn(briggs));
        TreeADDPotential tree = (TreeADDPotential) briggs.getNode("State [1]").getPotential();
        TablePotential table = tablesWithUncertainty(tree).getFirst();
        Arrays.fill(table.getUncertainValues(), 0, 2, new UncertainValue(new BetaFunction(2, 5)));

        List<String> wrong = DistributionsOfAColumn.wrongIn(briggs);

        assertEquals(1, wrong.size(), wrong.toString());
        assertTrue(wrong.getFirst().matches("State \\[1] \\(.+\\): States \".+\" and \".+\" both have a Beta distribution\\..*"),
                   wrong.toString());
    }

    private static List<TablePotential> tablesWithUncertainty(TreeADDPotential tree) {
        List<TablePotential> tables = new ArrayList<>();
        for (TreeADDBranch branch : tree.getBranches()) {
            switch (branch.getPotential()) {
                case TreeADDPotential subtree -> tables.addAll(tablesWithUncertainty(subtree));
                case TablePotential table when table.isUncertain() -> tables.add(table);
                case null, default -> {
                }
            }
        }
        return tables;
    }

    @Test void theNetworkAsItIsInTheFileIsNotRefused() {
        assertDoesNotThrow(() -> new VECEPSA(probNet));
        assertDoesNotThrow(() -> new VECESensAnSpider(probNet));
    }
}
