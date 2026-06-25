package org.openmarkov.core.localize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Overrides most of the functionality of {@link Localizable}, and makes it so the {@link Localizable#path()} is the
 * name of the class' name.
 * <p>
 * This is intended for classes that should be localized in Class Localization bundle files.
 *
 * @author jrico
 */
public interface ClassLocalizable extends Localizable {
    
    /**
     * Returns the key/path of the localization key in the Bundle file.
     * <p>
     * The default implementation returns this class' name.
     *
     * @return the key/path of the localization key in the Bundle file.
     */
    @Override @NotNull default String path() {
        return this.getClass().getName();
    }
    
    /**
     * Gets the name of the Bundle file where the key should be localized.
     * <p>
     * The default implementation returns {@code null}.
     *
     * @return The name of the Bundle file where the key should be localized.
     */
    @Override @Nullable default String bundle() {
        return null;
    }
    
    
}
