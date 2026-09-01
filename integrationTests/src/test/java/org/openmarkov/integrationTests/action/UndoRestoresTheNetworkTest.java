/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.integrationTests.action;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openmarkov.core.action.base.PNEdit;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.io.probmodel.writer.PGMXWriter_1_0;
import org.openmarkov.plugin.PluginSearch;

import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Undoing an edit must leave the network as it was. Each case builds a small network, creates
 * one edit on it, writes the network, executes the edit, undoes it, writes the network again,
 * and compares the two texts. The written file is the yardstick because it already knows how to
 * put a whole network into words; what it does not write, this test cannot see.
 *
 * <p>The edits that do not restore the network today are declared in {@link EditUndoCases} with
 * the identifier of the finding that describes them. Those are checked the other way round: they
 * must still fail. When one is fixed, this test asks for its entry to be removed.
 *
 * @author Manuel Arias
 */
public class UndoRestoresTheNetworkTest {

    @TempDir Path directory;

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "{0}")
    @MethodSource("casesThatMustPass")
    public void undoLeavesTheNetworkAsItWas(EditUndoCases.Case testCase) throws Exception {
        PNEdit edit = testCase.build().create();
        ProbNet net = edit.getProbNet();

        String before = write(net, "before");
        edit.executeEdit();
        edit.undo();
        String after = write(net, "after");

        List<String> differences = differences(before, after);
        assertTrue(differences.isEmpty(), testCase + ": undo() did not restore the network\n"
                                          + String.join("\n", differences));
    }

    @Tag(TestSpeed.FAST)
    @ParameterizedTest(name = "{0}")
    @MethodSource("casesWithAKnownDefect")
    public void theKnownDefectsAreStillThere(EditUndoCases.Case testCase) throws Exception {
        PNEdit edit = testCase.build().create();
        ProbNet net = edit.getProbNet();

        String before = write(net, "before");
        edit.executeEdit();
        edit.undo();
        String after = write(net, "after");

        assertNotEquals(before, after, testCase + ": this edit now restores the network. Remove its entry "
                                       + "from the list of known defects in EditUndoCases so that it is "
                                       + "checked from now on.");
    }

    /**
     * The lines where the two networks differ, at most ten of them.
     */
    private static List<String> differences(String before, String after) {
        List<String> linesBefore = before.lines().toList();
        List<String> linesAfter = after.lines().toList();
        List<String> differences = new ArrayList<>();
        for (int line = 0; line < Math.max(linesBefore.size(), linesAfter.size()) && differences.size() < 10; line++) {
            String oneLine = line < linesBefore.size() ? linesBefore.get(line).strip() : "(no line)";
            String otherLine = line < linesAfter.size() ? linesAfter.get(line).strip() : "(no line)";
            if (!oneLine.equals(otherLine)) {
                differences.add("line " + (line + 1) + "   before: " + oneLine + "   after: " + otherLine);
            }
        }
        return differences;
    }

    /**
     * Every concrete edit of the core module has a case here, or is named in the list of the ones
     * still without one. A new edit class with neither makes this test fail.
     */
    @Tag(TestSpeed.FAST)
    @Test public void everyEditOfTheCoreModuleIsAccountedFor() {
        Set<String> named = EditUndoCases.all().stream()
                                        .map(testCase -> testCase.edit().getSimpleName())
                                        .collect(Collectors.toSet());
        named.addAll(EditUndoCases.WITHOUT_A_CASE.keySet());

        List<String> unaccounted = concreteEditsOfTheCoreModule().stream()
                                                                 .filter(name -> !named.contains(name))
                                                                 .sorted()
                                                                 .toList();

        assertTrue(unaccounted.isEmpty(),
                "These edits have neither a case nor an entry in WITHOUT_A_CASE: " + unaccounted);
    }

    /**
     * Nothing is declared twice, and nothing is declared that no longer exists.
     */
    @Tag(TestSpeed.FAST)
    @Test public void theDeclarationsMatchTheEditsThatExist() {
        List<String> cased = EditUndoCases.all().stream()
                                          .map(testCase -> testCase.edit().getSimpleName())
                                          .toList();
        List<String> declaredTwice = cased.stream()
                                          .filter(EditUndoCases.WITHOUT_A_CASE::containsKey)
                                          .toList();
        assertTrue(declaredTwice.isEmpty(),
                "These edits have a case and are also listed as having none: " + declaredTwice);

        Set<String> existing = concreteEditsOfTheCoreModule();
        List<String> gone = Stream.concat(EditUndoCases.WITHOUT_A_CASE.keySet().stream(), cased.stream())
                                  .distinct()
                                  .filter(name -> !existing.contains(name))
                                  .sorted()
                                  .toList();
        assertTrue(gone.isEmpty(), "These edits are named here but the core module no longer has them: " + gone);
    }

    private static Set<String> concreteEditsOfTheCoreModule() {
        return PluginSearch.init().childrenOf(PNEdit.class).stream()
                           .filter(edit -> edit.getPackageName().startsWith("org.openmarkov.core.action"))
                           .filter(edit -> !Modifier.isAbstract(edit.getModifiers()))
                           .filter(edit -> edit.getEnclosingClass() == null)
                           .map(Class::getSimpleName)
                           .collect(Collectors.toSet());
    }

    private String write(ProbNet net, String name) throws Exception {
        Path file = directory.resolve(name + ".pgmx");
        new PGMXWriter_1_0().write(file.toString(), net, List.of());
        return Files.readString(file);
    }

    private static List<EditUndoCases.Case> casesThatMustPass() {
        return EditUndoCases.all().stream().filter(testCase -> testCase.knownDefect() == null).toList();
    }

    private static List<EditUndoCases.Case> casesWithAKnownDefect() {
        return EditUndoCases.all().stream().filter(testCase -> testCase.knownDefect() != null).toList();
    }
}
