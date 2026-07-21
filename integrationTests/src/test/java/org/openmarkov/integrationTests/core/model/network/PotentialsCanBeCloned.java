package org.openmarkov.integrationTests.core.model.network;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.exception.ProbNetParserException;
import org.openmarkov.core.io.format.annotation.NoReaderForFileException;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.gui.dialog.io.NetsIO;
import org.openmarkov.gui.exception.CorruptNetworkFile;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PotentialsCanBeCloned {
    
    record DeepCloneTestData(Potential potential, ProbNet probNet) {
    }
    
    Stream<DeepCloneTestData> deepCloneTestData() throws NoReaderForFileException, ProbNetParserException, IOException, CorruptNetworkFile {
        var net = NetsIO.openNetworkURL(PotentialsCanBeCloned.DAN_WITH_EVERY_POTENTIAL_URL)
                        .probNet();
        return net.getNodes().stream()
                  .filter(node -> !node.getName().endsWith("Parent1"))
                  .filter(node -> !node.getName().endsWith("Parent2"))
                  .map(Node::getPotential).map(potential -> new DeepCloneTestData(potential, net));
    }
    
    @ParameterizedTest
    @MethodSource("deepCloneTestData")
    void testDeepCopy(DeepCloneTestData deepCloneTestData) {
        Potential sourcePotential = deepCloneTestData.potential;
        ProbNet probNet = deepCloneTestData.probNet;
        Potential clonedPotential = sourcePotential.deepCopy(probNet);
        assertThat(sourcePotential).usingRecursiveComparison().isEqualTo(clonedPotential);
    }
    
    
    Stream<Potential> cloneTestData() throws NoReaderForFileException, ProbNetParserException, IOException, CorruptNetworkFile {
        return this.deepCloneTestData().map(DeepCloneTestData::potential);
    }
    
    @ParameterizedTest
    @MethodSource("cloneTestData")
    void testCopy(Potential sourcePotential) {
        Potential clonedPotential = sourcePotential.copy();
        assertThat(sourcePotential).usingRecursiveComparison().isEqualTo(clonedPotential);
    }
    
    private static final URL DAN_WITH_EVERY_POTENTIAL_URL = Objects.requireNonNull(
            PotentialsCanBeCloned.class.getResource("/networks_for_every_potential/Dynamic-LIMID-every-potential.pgmx"));
}
