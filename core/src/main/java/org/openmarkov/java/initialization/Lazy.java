package org.openmarkov.java.initialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnrecoverableException;

public class Lazy<T> {
    private volatile boolean isInitialized;
    private final @NotNull ThrowingSupplier<? extends T, Exception> initializer;
    private volatile @Nullable T value;
    
    public Lazy(@NotNull ThrowingSupplier<? extends T, Exception> initializer) {
        this.initializer = initializer;
        this.isInitialized = false;
        this.value = null;
    }
    
    public static <T> Lazy<T> of(@NotNull ThrowingSupplier<? extends T, Exception> initializer) {
        return new Lazy<>(initializer);
    }
    
    public T get() {
        if (!this.isInitialized) {
            synchronized (this) {
                if (!this.isInitialized) {
                    try {
                        this.value = this.initializer.get();
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new UnrecoverableException(e);
                    }
                    this.isInitialized = true;
                }
            }
        }
        return this.value;
    }
    
    /**
     * Whether the value has been computed. Reads the volatile flag without
     * taking the monitor: {@code get()} holds that monitor for the whole run of
     * the initializer, so an initializer that hands work to other threads which
     * ask this question — a parallel stream whose tasks check whether the value
     * is ready yet — deadlocked here: the initializing thread waited for the
     * workers, and every worker waited for the monitor. The volatile read gives
     * the same visibility the outer check of {@code get()} already relies on,
     * and blocks nobody.
     */
    public boolean isInitialized() {
        return this.isInitialized;
    }
    
    public void reset() {
        synchronized (this) {
            this.isInitialized = false;
            this.value = null;
        }
    }
    
    @FunctionalInterface
    public interface ThrowingSupplier<T, E extends Exception> {
        T get() throws E;
    }
    
}
