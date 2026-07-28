/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.java.initialization;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Safety net for {@link Lazy}, written when a deadlock in it was found to be
 * what hung the static-analysis tools (defect nº 35 of the global table): the
 * initializer ran a parallel stream whose workers asked
 * {@code isInitialized()}, which took the same monitor {@code get()} holds for
 * the whole run of the initializer.
 *
 * @author Manuel Arias
 */
class LazyTest {

    @Test
    void computesOnceAndAnswersInitialized() {
        AtomicInteger computations = new AtomicInteger();
        Lazy<Integer> lazy = Lazy.of(computations::incrementAndGet);

        assertFalse(lazy.isInitialized());
        assertEquals(1, lazy.get());
        assertTrue(lazy.isInitialized());
        assertEquals(1, lazy.get());
        assertEquals(1, computations.get(), "the initializer must run exactly once");
    }

    /**
     * The deadlock, reproduced: an initializer that hands work to a parallel
     * stream whose tasks ask {@code isInitialized()}. The initializing thread
     * held the monitor and waited for the workers; every worker waited for the
     * monitor. With the previous code this test dies by timeout; the volatile
     * read releases nobody's work into a lock.
     */
    @Test
    void anInitializerWhoseWorkersAskIsInitializedDoesNotDeadlock() {
        Lazy<List<Integer>>[] holder = new Lazy[1];
        holder[0] = Lazy.of(() -> IntStream.range(0, 1_000)
                .parallel()
                .mapToObj(i -> {
                    // What ParseUtils.parseClass does from inside the initializer.
                    boolean ready = holder[0].isInitialized();
                    return ready ? -i : i;
                })
                .toList());

        List<Integer> values = assertTimeoutPreemptively(Duration.ofSeconds(30), () -> holder[0].get());

        assertEquals(1_000, values.size());
        assertTrue(holder[0].isInitialized());
    }

    @Test
    void resetForgetsTheValueAndRecomputes() {
        AtomicInteger computations = new AtomicInteger();
        Lazy<Integer> lazy = Lazy.of(computations::incrementAndGet);

        assertEquals(1, lazy.get());
        lazy.reset();
        assertFalse(lazy.isInitialized());
        assertEquals(2, lazy.get());
    }
}
