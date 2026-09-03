package org.openmarkov.integrationTests.io;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.UnivariateDistrPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Writing a network and reading it again must give back the parameters of its
 * {@link UnivariateDistrPotential}s. The 1.0 writer always writes the {@code Functions}
 * element, and the reader used to answer that element by replacing the whole table, which
 * dropped the numbers it had just read.
 *
 * @author Manuel Arias
 */
class UnivariateDistrParametersSurviveARoundTripTest {

    private static final String NETWORK = "networks/dan/DAN-dec-util-product-1-0-0.pgmx";

    @Tag(TestSpeed.FAST)
    @Test void theParametersAreTheOnesTheFileCarried() throws Exception {
        ProbNet original = new PGMXReader().read(
                getClass().getClassLoader().getResource(NETWORK)).probNet();
        List<double[]> valuesBefore = univariateValues(original);
        assertFalse(valuesBefore.isEmpty(), "the network must carry UnivariateDistr potentials");

        Path written = Files.createTempDirectory("roundTrip").resolve("written.pgmx");
        new PGMXWriter_1_0().write(written.toString(), original, List.of());

        ProbNet reread = new PGMXReader().read(written.toUri().toURL()).probNet();
        List<double[]> valuesAfter = univariateValues(reread);

        assertArrayEquals(valuesBefore.toArray(), valuesAfter.toArray());
    }

    private static List<double[]> univariateValues(ProbNet probNet) {
        List<double[]> values = new ArrayList<>();
        for (Potential potential : probNet.getPotentials()) {
            if (potential instanceof UnivariateDistrPotential univariate) {
                values.add(univariate.getDistributionTable().getValues());
            }
        }
        return values;
    }
}
