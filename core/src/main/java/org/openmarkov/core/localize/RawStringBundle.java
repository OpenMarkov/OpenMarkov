package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Set;

/**
 * Implements {@link StringBundle} for a {@link HashMap}, meaning the {@link StringBundle#getKeys()} is just the
 * {@link HashMap#keySet()}, and {@link StringBundle#getString(String)} is just {@link HashMap#get(Object)}.
 * <p>
 * This class is similar to the Adaptor pattern but with an interface.
 *
 * @author jrico
 */
public class RawStringBundle implements StringBundle {
    
    private final HashMap<String, String> keysAndLocalizations;
    
    /**
     * Constructs a new instance of RawStringBundle with the provided {@link HashMap}.
     *
     * @param keysAndLocalizations a HashMap where each key represents a string identifier, and the value contains the
     *                             localized string corresponding to that key.
     */
    public RawStringBundle(HashMap<String, String> keysAndLocalizations) {
        this.keysAndLocalizations = keysAndLocalizations;
    }
    
    @Override public @Nullable String getString(String key) {
        return this.keysAndLocalizations.get(key);
    }
    
    @Override public @NotNull Set<String> getKeys() {
        return this.keysAndLocalizations.keySet();
    }
    
    @Override public String toString() {
        return "RawStringBundle{keysAndLocalizations=" + this.keysAndLocalizations +
                '}';
    }
}
