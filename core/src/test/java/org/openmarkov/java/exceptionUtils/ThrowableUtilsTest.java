/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.java.exceptionUtils;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    /**
     * A transfer moves, it does not copy: the target ends up with the merged trace and the source
     * is deliberately left with none, so that a printed error carries its information in one
     * place instead of repeated in every layer. Decision of 29 July 2026: documented, not
     * changed; this pins the whole contract, the half that was written and the half that was not.
     */
    @Test
    public void transferringMovesTheTraceAndEmptiesTheSource() {
        Throwable cause = new Throwable("cause");
        Throwable wrapper = new Throwable("wrapper", cause);

        ThrowableUtils.transferStackTrace(wrapper, cause);

        assertEquals(0, wrapper.getStackTrace().length,
                     "the source must be left without a trace: it is a move, not a copy");
        assertTrue(cause.getStackTrace().length > 0, "the target must carry the merged trace");
    }

    /** A missing source is answered by doing nothing, as a missing target already was. */
    @Test
    public void transferringFromNothingDoesNothing() {
        Throwable target = new Throwable("target");
        int framesBefore = target.getStackTrace().length;

        assertDoesNotThrow(() -> ThrowableUtils.transferStackTrace(null, target));
        assertEquals(framesBefore, target.getStackTrace().length);
    }
}
