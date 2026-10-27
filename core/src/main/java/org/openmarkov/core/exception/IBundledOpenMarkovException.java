package org.openmarkov.core.exception;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.openmarkov.core.localize.ClassLocalizable;
import org.openmarkov.core.localize.Localizable;
import org.openmarkov.core.stringformat.LocalizationFormatter;

import java.util.*;

/**
 * Common interface for exceptions declared in OpenMarkov.
 *
 * @author jrico
 */
public interface IBundledOpenMarkovException extends IOpenMarkovException, ClassLocalizable {
    
    @Override @Nullable default String getExceptionMessage() {
        return this.localize();
    }
    
    @Override @Nullable default String getExceptionTitle() {
        return IBundledOpenMarkovException.titlifyExceptionName(this);
    }
    
    /**
     * The title of an exception made of its name: «WrongVersionException» gives «Wrong version». A series of capitals
     * stays together and in capitals, so that «TreeADDWithoutPotential» gives «Tree ADD without potential» and
     * «XMLInvalid» «XML invalid»: letter by letter, they used to give «Tree a d d…» and «X m l invalid» (error 182).
     */
    @NotNull static String titlifyExceptionName(@NotNull Object exception) {
        String simpleName = exception.getClass().getSimpleName();
        if (simpleName.endsWith("Exception")) {
            simpleName = simpleName.substring(0, simpleName.length() - "Exception".length());
        }
        // A word starts at a capital after a small letter or a digit, and at the last capital of a series followed by
        // a small letter
        String[] words = simpleName.split("(?<=[a-z0-9])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");
        StringBuilder title = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!title.isEmpty()) {
                title.append(' ');
            }
            boolean acronym = word.length() > 1 && word.equals(word.toUpperCase());
            title.append(acronym ? word : word.toLowerCase());
        }
        if (!title.isEmpty()) {
            title.setCharAt(0, Character.toUpperCase(title.charAt(0)));
        }
        return title.toString().replace("\\n", System.lineSeparator());
    }
    
    static @NotNull String toString(IBundledOpenMarkovException exception) {
        return exception.localize();
    }
    
    @Override @NotNull default String path() {
        return IOpenMarkovException.super.path();
    }
    
    @Override @NotNull default String localize(LocalizationFormatter formatter) {
        return Localizable.localize(this, formatter, this.path());
    }
    
    
}
