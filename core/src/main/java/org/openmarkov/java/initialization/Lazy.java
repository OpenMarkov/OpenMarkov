package org.openmarkov.java.initialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnrecoverableException;

public class Lazy<T> {

    /**
     * The computed value, boxed, so that the fact of being computed and the value itself travel
     * in a single reference: a reader sees both or neither, and a {@link #reset()} cannot leave
     * it with the flag of one state and the value of another.
     */
    private static final class Computed<T> {
        private final @Nullable T value;

        private Computed(@Nullable T value) {
            this.value = value;
        }
    }

    private volatile @Nullable Computed<T> computed;
    private final @NotNull ThrowingSupplier<? extends T, Exception> initializer;
    /**
     * Raised while the initializer runs; guarded by the monitor. Its only job is to refuse, by
     * name, an initializer that asks this same Lazy for its own value.
     */
    private boolean initializing;

    public Lazy(@NotNull ThrowingSupplier<? extends T, Exception> initializer) {
        this.initializer = initializer;
    }

    public static <T> Lazy<T> of(@NotNull ThrowingSupplier<? extends T, Exception> initializer) {
        return new Lazy<>(initializer);
    }

    public T get() {
        Computed<T> snapshot = this.computed;
        if (snapshot != null) {
            return snapshot.value;
        }
        synchronized (this) {
            snapshot = this.computed;
            if (snapshot == null) {
                // The monitor lets its own thread back in: an initializer that asks this same
                // Lazy for its value would run itself again, and again, until the stack
                // overflowed with an error that names nothing. Refused by name instead.
                if (this.initializing) {
                    throw new IllegalStateException(
                            "the initializer of this Lazy asks this same Lazy for its value");
                }
                this.initializing = true;
                try {
                    snapshot = new Computed<>(this.initializer.get());
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new UnrecoverableException(e);
                } finally {
                    this.initializing = false;
                }
                this.computed = snapshot;
            }
            return snapshot.value;
        }
    }

    /**
     * Whether the value has been computed. Reads the volatile reference without
     * taking the monitor: {@code get()} holds that monitor for the whole run of
     * the initializer, so an initializer that hands work to other threads which
     * ask this question — a parallel stream whose tasks check whether the value
     * is ready yet — deadlocked here: the initializing thread waited for the
     * workers, and every worker waited for the monitor. The volatile read gives
     * the same visibility the outer check of {@code get()} already relies on,
     * and blocks nobody.
     */
    public boolean isInitialized() {
        return this.computed != null;
    }

    /**
     * Forgets the value; the next {@link #get()} computes it again. Taking the monitor makes a
     * reset called during a computation wait for it and then forget it, instead of being
     * silently overwritten by it.
     */
    public void reset() {
        synchronized (this) {
            this.computed = null;
        }
    }

    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception> {
        T get() throws E;
    }

}
