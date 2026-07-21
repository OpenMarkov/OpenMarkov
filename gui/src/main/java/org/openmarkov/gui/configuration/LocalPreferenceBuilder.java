package org.openmarkov.gui.configuration;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Builder for constructing {@link LocalPreference} instances with custom serialization,
 * deserialization, and instance verification strategies.
 *
 * @param <T> the type of the preference value
 */
public final class LocalPreferenceBuilder<T> {
    
    private final @NotNull String preferencePath;
    private final @NotNull Supplier<? extends T> defaultValue;
    private @Nullable Class<T> tClass;
    private @Nullable Predicate<Object> verifyIsInstance;
    private @Nullable Function<String, T> deserializeWith;
    private @Nullable Function<T, String> serializeWith;
    
    public LocalPreferenceBuilder(@NotNull String locatedAt, @NotNull Supplier<? extends T> defaultValue) {
        this(locatedAt, defaultValue, null);
    }
    
    public LocalPreferenceBuilder(@NotNull String locatedAt, @NotNull Supplier<? extends T> defaultValue, @NotNull Class<T> tClass) {
        this.preferencePath = locatedAt;
        this.defaultValue = defaultValue;
        this.tClass = tClass;
    }
    
    public LocalPreferenceBuilder<T> withObjectClass(@Nullable Class<T> objectClass) {
        this.tClass = objectClass;
        return this;
    }
    
    public LocalPreferenceBuilder<T> deserializeWith(@Nullable Function<String, T> deserializeWith) {
        this.deserializeWith = deserializeWith;
        return this;
    }
    
    public LocalPreferenceBuilder<T> serializeWith(@Nullable Function<T, String> serializeWith) {
        this.serializeWith = serializeWith;
        return this;
    }
    
    public LocalPreferenceBuilder<T> verifyIsInstanceWith(@Nullable Predicate<Object> verifyIsInstance) {
        this.verifyIsInstance = verifyIsInstance;
        return this;
    }
    
    /**
     * Builds the {@link LocalPreference} instance with the configured strategies.
     *
     * @return a new {@code LocalPreference} instance
     */
    public LocalPreference<T> build() {
        Function<String, T> fromString = this.deserializeWith != null ? this.deserializeWith : JavaSerializationUtils::javaDeserialize;
        Function<T, String> intoString = this.serializeWith != null ? this.serializeWith : JavaSerializationUtils::javaSerialize;
        Predicate<Object> verifyIsInstance = this.verifyIsInstance != null ? this.verifyIsInstance : o -> true;
        Class<T> tClass = this.tClass != null ? this.tClass : (Class<T>) Object.class;
        
        return new LocalPreference<>(
                this.preferencePath,
                this.defaultValue,
                tClass,
                verifyIsInstance,
                fromString,
                intoString);
    }
    
}