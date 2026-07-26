/*
 * Copyright (c) CISIAD, UNED, Spain,  2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.java.classUtils;

import org.junit.jupiter.api.Test;
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
}
