package org.openmarkov.java.initialization;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.exception.UnrecoverableException;

public class Lazy<T> {
    private boolean isInitialized;
    private final @NotNull ThrowingSupplier<? extends T, Exception> initializer;
    private @Nullable T value;
    
    public Lazy(@NotNull ThrowingSupplier<? extends T, Exception> initializer) {
        this.initializer = initializer;
        this.isInitialized = false;
        this.value = null;
    }
    
    public static <T> Lazy<T> of(@NotNull ThrowingSupplier<? extends T, Exception> initializer) {
        return new Lazy<>(initializer);
    }
    
    public @NotNull T get() {
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
            return this.value;
        }
    }
    
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
