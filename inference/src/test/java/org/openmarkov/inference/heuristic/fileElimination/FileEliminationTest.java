/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.heuristic.fileElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.testTags.TestSpeed;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The heuristic reads from a file the order in which to eliminate the variables. The file is the
 * one Hugin writes: a line per variable, the position and then the name. The order is answered
 * reversed, because it is consumed from the last entry to the first.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class FileEliminationTest {

    @TempDir Path directory;

    /**
     * The name of a variable can be part of the name of another. The line was matched with
     * {@code line.contains(name)}, so in BN-hepar, which has an {@code amn} and a {@code vh_amn},
     * the line naming {@code vh_amn} answered whichever of the two came first among the variables.
     * The order applied was not the order written: one variable was eliminated twice and the other
     * never, and nothing said so.
     */
    @Test
    void aNameThatIsPartOfAnotherNameIsNotConfusedWithIt() throws IOException {
        Variable amn = new Variable("amn", 2);
        Variable vhAmn = new Variable("vh_amn", 2);
        Variable ggt = new Variable("ggt", 2);
        Path net = orderFile("   3 vh_amn", "   2 amn", "   1 ggt");

        List<Variable> order = FileElimination.readEliminationOrder(net.toString(), List.of(List.of(amn, vhAmn, ggt)))
                                              .get(0);

        assertEquals(List.of(ggt, amn, vhAmn), order);
    }

    /** The position Hugin writes before the name is not part of the name. */
    @Test
    void thePositionInFrontOfTheNameIsDropped() throws IOException {
        Variable row = new Variable("Row0", 2);
        Path net = orderFile("   24 Row0");

        List<Variable> order = FileElimination.readEliminationOrder(net.toString(), List.of(List.of(row))).get(0);

        assertEquals(List.of(row), order);
    }

    /** A file that is only names, with no position in front, is read just the same. */
    @Test
    void aLineThatIsOnlyANameIsTakenWhole() throws IOException {
        Variable row = new Variable("Row0", 2);
        Path net = orderFile("Row0");

        List<Variable> order = FileElimination.readEliminationOrder(net.toString(), List.of(List.of(row))).get(0);

        assertEquals(List.of(row), order);
    }

    /**
     * A line naming a variable the network does not have is a file that does not belong to this
     * network, or a name spelt wrong. Either way the order asked for cannot be given, and giving a
     * different one in silence is the whole trouble this class had.
     */
    @Test
    void aLineThatNamesNoVariableOfTheNetworkIsRefused() throws IOException {
        Variable row = new Variable("Row0", 2);
        Path net = orderFile("   24 Row0", "   23 Column0");

        InvalidArgumentException refusal = assertThrows(InvalidArgumentException.class,
                () -> FileElimination.readEliminationOrder(net.toString(), List.of(List.of(row))));

        assertTrue(refusal.getMessage().contains("Column0"), "the refusal must name the line it could not place");
    }

    /**
     * A file that cannot be read used to be logged and swallowed, and the caller was handed an
     * empty order: the network was then eliminated in some other order, with nothing said.
     */
    @Test
    void aFileThatCannotBeReadIsRefused() {
        Path missing = directory.resolve("there-is-no-such-net.elv");

        assertThrows(UncheckedIOException.class,
                () -> FileElimination.readEliminationOrder(missing.toString(), List.of(List.of())));
    }

    /**
     * The order file is the network file with its extension changed. It used to be
     * {@code fileName.replace("elv", "hugin")}, which replaces every occurrence anywhere in the
     * path, so a directory called {@code elvira} became {@code huginira} and the file was looked
     * for where it is not.
     */
    @Test
    void onlyTheExtensionIsReplacedAndNotTheRestOfThePath() throws IOException {
        Variable row = new Variable("Row0", 2);
        Path elviraDirectory = Files.createDirectory(directory.resolve("elvira"));
        Files.writeString(elviraDirectory.resolve("net.hugin"), "   24 Row0\n");

        List<Variable> order = FileElimination.readEliminationOrder(elviraDirectory.resolve("net.elv").toString(),
                List.of(List.of(row))).get(0);

        assertEquals(List.of(row), order);
    }

    /** Writes the order file that {@code readEliminationOrder} looks for beside the network. */
    private Path orderFile(String... lines) throws IOException {
        Path net = directory.resolve("net.elv");
        Files.writeString(directory.resolve("net.hugin"), String.join("\n", lines) + "\n");
        return net;
    }
}
