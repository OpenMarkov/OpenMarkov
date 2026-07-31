/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.java.classUtils;

import org.junit.jupiter.api.Test;
import org.openmarkov.core.exception.InvalidArgumentException;
import org.openmarkov.core.model.network.ProbNet;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Finding the source file a compiled class came from, which the static-analysis tools are built on.
 * <p>
 * It worked out where the source was by rewriting {@code target\classes} into {@code src\main\java} in
 * the path - with backslashes, so on any system that does not use them the rewrite matched nothing, the
 * path still pointed inside the build directory, no {@code .java} was there and the answer was null.
 * Every one of the twelve tools under {@code staticAnalysis} asks this first and hands the answer
 * straight to the parser, so on Linux none of them could run at all.
 *
 * @author Manuel Arias
 */
public class ClassUtilsTest {

    @Test public void theSourceOfACompiledClassIsFound() {
        File source = ClassUtils.fileOfClass(ProbNet.class);

        assertNotNull(source, "no source file was found for ProbNet");
        assertTrue(source.exists(), "the file named does not exist: " + source);
        assertEquals("ProbNet.java", source.getName());
    }

    /** And the path it gives is inside the sources, not inside the build output. */
    @Test public void theSourceIsLookedForWhereSourcesLive() {
        File source = ClassUtils.fileOfClass(ProbNet.class);

        assertNotNull(source);
        String path = source.getAbsolutePath();
        assertTrue(path.contains("src" + File.separator + "main" + File.separator + "java"),
                   "expected a path under the sources, got " + path);
        assertFalse(path.contains("target" + File.separator + "classes"),
                    "the path still points into the build output: " + path);
    }

    /**
     * The lookup answers the path written the web way, where a space arrives as {@code %20};
     * it must be translated back or the file it names does not exist.
     */
    @Test public void aResourceOnAPathWithASpaceIsFound() {
        File resource = ClassUtils.getResourceAsFile(ClassUtilsTest.class, "with space.txt");

        assertFalse(resource.getPath().contains("%20"), "the path was not translated: " + resource);
        assertTrue(resource.exists(), "the file named does not exist: " + resource);
    }

    /**
     * The classes of the language itself, and every class inside an installer image, carry no
     * code source; asking whether one of them is a test class used to break with the error that
     * names nothing. Nothing to look at means it is not a test class.
     */
    @Test public void aClassOfTheLanguageItselfIsNotATestClass() {
        assertFalse(ClassUtils.isTestClass(String.class));
    }

    @Test public void aTestClassIsRecognizedAndAProductionClassIsNot() {
        assertTrue(ClassUtils.isTestClass(ClassUtilsTest.class));
        assertFalse(ClassUtils.isTestClass(ClassUtils.class));
    }

    /** Asking for a resource that does not exist must say what was asked for, not break naming nothing. */
    @Test public void aMissingResourceIsReportedByName() {
        InvalidArgumentException error = assertThrows(InvalidArgumentException.class,
                () -> ClassUtils.getResourceAsFile(ClassUtilsTest.class, "no-such-resource.txt"));

        assertTrue(error.getMessage().contains("no-such-resource.txt"),
                   () -> "the error must name the resource; it says: " + error.getMessage());
        assertTrue(error.getMessage().contains(ClassUtilsTest.class.getName()),
                   () -> "the error must name the class it looked in; it says: " + error.getMessage());
    }
}
