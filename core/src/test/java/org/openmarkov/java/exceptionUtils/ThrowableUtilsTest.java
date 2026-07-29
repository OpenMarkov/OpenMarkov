/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.java.exceptionUtils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

/**
 * Unit tests for {@link ThrowableUtils#flatten}: walking the chain of causes must end, whatever
 * shape the chain has.
 *
 * @author Manuel Arias
 */
public class ThrowableUtilsTest {

    @Test
    public void flattenReachesTheRootCause() {
        Throwable root = new Throwable("root");
        Throwable top = new Throwable("top", new Throwable("middle", root));

        assertSame(root, ThrowableUtils.flatten(top));
    }

    /**
     * The language forbids an error being its own cause, but not a cycle of two; walking one
     * never ended. The walk now closes on the first already-visited link, and the last new one
     * plays the root.
     */
    @Test
    public void flattenEndsEvenIfTheCausesFormACycle() {
        Throwable b = new Throwable("b");
        Throwable a = new Throwable("a", b);
        b.initCause(a);

        Throwable flat = assertTimeoutPreemptively(Duration.ofSeconds(5),
                                                   () -> ThrowableUtils.flatten(a));
        assertSame(b, flat, "the last link before the chain repeats itself plays the root");
    }
}
