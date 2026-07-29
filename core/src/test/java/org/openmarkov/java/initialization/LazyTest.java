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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    /**
     * An initializer that asks this same Lazy for its own value used to run itself again ---the
     * monitor lets its own thread back in, and the flag was still down--- and again, until the
     * stack overflowed with an error that names nothing. The answer must be a refusal that says
     * what happened.
     */
    @Test
    void anInitializerThatAsksForItsOwnValueIsRefusedWithAnExplanation() {
        Lazy<Integer>[] holder = new Lazy[1];
        holder[0] = Lazy.of(() -> holder[0].get());

        IllegalStateException error = assertThrows(IllegalStateException.class, () -> holder[0].get());
        assertTrue(error.getMessage().contains("initializer"),
                   () -> "the error must explain the situation; it says: " + error.getMessage());
    }

    /**
     * A reader that had just seen the flag up could read the value after a concurrent reset()
     * nulled it, and answer null for an initializer that never produces one. The flag and the
     * value travel now in a single reference, so a reader sees both or neither. The window is
     * narrow: this hammers it rather than proving it, and pins the property from then on.
     */
    @Test
    void aConcurrentResetNeverMakesGetAnswerNull() throws InterruptedException {
        Lazy<Integer> lazy = Lazy.of(() -> 7);
        AtomicBoolean sawNull = new AtomicBoolean();
        Thread resetter = new Thread(() -> {
            for (int i = 0; i < 200_000; i++) {
                lazy.reset();
            }
        });
        Thread reader = new Thread(() -> {
            for (int i = 0; i < 200_000; i++) {
                if (lazy.get() == null) {
                    sawNull.set(true);
                    return;
                }
            }
        });
        resetter.start();
        reader.start();
        resetter.join(30_000);
        reader.join(30_000);

        assertFalse(sawNull.get(), "get() answered a null its initializer never produced");
    }
}
