/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.io.probmodel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.Potential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithEventsPotential;
import org.openmarkov.core.model.network.potential.treeadd.TreeWithExcludedEventsPotential;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.exception.PGMXParserException;
import org.openmarkov.io.probmodel.reader.PGMXReader;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * A tree of a relation with events is read like any other relation: a damaged one is rejected, and a sound one
 * opens the same. The reader used to print the error on the console and open the network with a default tree,
 * without a warning (error 236 of the pending list).
 *
 * <p>In the network of events of the tests, {@code Death} depends here on two events, {@code Onset} and
 * {@code Checkup}, and on {@code Ill}, which is not an event; its relation carries a comment, which used to shift
 * the elements the reader looked for by their position.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.MEDIUM)
public class ADamagedEventTreeIsRejectedWithAMessageTest {

    private static final String DEATH_CANNOT_BE_READ =
            "The relation of Death cannot be read: one of its trees is split on %s, which is not one of the variables "
                    + "that tree can be split on.";

    @TempDir Path folder;

    private ProbNet network;
    private List<Variable> variablesOfDeath;

    @BeforeEach public void setUp() throws Exception {
        network = new PGMXReader().read(getClass().getClassLoader().getResource("DES-illness.pgmx")).probNet();
        network.addLink(network.getVariable("Checkup"), network.getVariable("Death"), true);
        variablesOfDeath = List.of(network.getVariable("Death"), network.getVariable("Onset"),
                                   network.getVariable("Ill"), network.getVariable("Checkup"));
    }

    /** @return the network, with {@code relation} in {@code Death}, as a file says it */
    private String saved(Potential relation) throws Exception {
        relation.setComment("Two events, one tree each");
        network.getNode("Death").setPotential(relation);
        Path file = folder.resolve("saved.pgmx");
        new PGMXWriter_1_0().write(file.toString(), network, List.of());
        return Files.readString(file);
    }

    /** @return the relation of {@code Death} in the network that {@code text} says */
    private Potential deathIn(String text) throws Exception {
        Path file = folder.resolve("read.pgmx");
        Files.writeString(file, text);
        return new PGMXReader().read(file.toUri().toURL()).probNet().getNode("Death").getPotentials().getFirst();
    }

    private String withTreeOfDeath(String text, String from, String to) {
        int relationOfDeath = text.indexOf("<Potential type=\"Tree with");
        return text.substring(0, relationOfDeath) + text.substring(relationOfDeath).replaceFirst(from, to);
    }

    @Test public void aTreeWithEventsOpensTheSame() throws Exception {
        TreeWithEventsPotential original = new TreeWithEventsPotential(variablesOfDeath, PotentialRole.CONDITIONAL_PROBABILITY);

        TreeWithEventsPotential read = (TreeWithEventsPotential) deathIn(saved(original));

        assertEquals(2, read.getTrees().size());
        for (Variable event : original.getEvents()) {
            Variable readEvent = read.getEvents().stream().filter(e -> e.getName().equals(event.getName())).findFirst().orElseThrow();
            assertEquals(original.getTree(event).toString(), read.getTree(readEvent).toString());
        }
        assertEquals(original.getComment(), read.getComment());
    }

    @Test public void aTreeWithExcludedEventsOpensTheSame() throws Exception {
        TreeWithExcludedEventsPotential original =
                new TreeWithExcludedEventsPotential(variablesOfDeath, PotentialRole.CONDITIONAL_PROBABILITY);

        TreeWithExcludedEventsPotential read = (TreeWithExcludedEventsPotential) deathIn(saved(original));

        assertEquals(original.getNoEventTree().toString(), read.getNoEventTree().toString());
        assertEquals(original.getNoEventTree().getVariables().toString(), read.getNoEventTree().getVariables().toString());
    }

    @Test public void aTreeSplitOnAVariableThatDoesNotExistIsRejected() throws Exception {
        String text = withTreeOfDeath(saved(new TreeWithEventsPotential(variablesOfDeath, PotentialRole.CONDITIONAL_PROBABILITY)),
                                      "<TopVariable name=\"Onset\" />", "<TopVariable name=\"Onsett\" />");

        var rejected = assertThrows(PGMXParserException.TreeSplitOnAWrongVariable.class, () -> deathIn(text));

        assertEquals(DEATH_CANNOT_BE_READ.formatted("Onsett"), rejected.getExceptionMessage());
    }

    @Test public void aTreeWithEventsSplitOnAParentThatIsNotAnEventIsRejected() throws Exception {
        String text = withTreeOfDeath(saved(new TreeWithEventsPotential(variablesOfDeath, PotentialRole.CONDITIONAL_PROBABILITY)),
                                      "<TopVariable name=\"Checkup\" />", "<TopVariable name=\"Ill\" />");

        var rejected = assertThrows(PGMXParserException.TreeSplitOnAWrongVariable.class, () -> deathIn(text));

        assertEquals(DEATH_CANNOT_BE_READ.formatted("Ill"), rejected.getExceptionMessage());
    }

    @Test public void aTreeWithExcludedEventsSplitOnAnEventIsRejected() throws Exception {
        String text = withTreeOfDeath(saved(new TreeWithExcludedEventsPotential(variablesOfDeath, PotentialRole.CONDITIONAL_PROBABILITY)),
                                      "<TopVariable name=\"Ill\" />", "<TopVariable name=\"Onset\" />");

        var rejected = assertThrows(PGMXParserException.TreeSplitOnAWrongVariable.class, () -> deathIn(text));

        assertEquals(DEATH_CANNOT_BE_READ.formatted("Onset"), rejected.getExceptionMessage());
    }

    /** As with any other relation, a value that is not a number does not let the network open. */
    @Test public void aValueThatIsNotANumberIsRejected() throws Exception {
        String text = withTreeOfDeath(saved(new TreeWithEventsPotential(variablesOfDeath, PotentialRole.CONDITIONAL_PROBABILITY)),
                                      "<Values>1.0</Values>", "<Values>abc</Values>");

        assertThrows(NumberFormatException.class, () -> deathIn(text));
    }
}
