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
    
    List<String> UPPERCASE_LETTERS = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z");
    
    @NotNull static String titlifyExceptionName(@NotNull Object exception) {
        String simpleName = exception.getClass().getSimpleName();
        if (simpleName.endsWith("Exception")) {
            simpleName = simpleName.substring(0, simpleName.length() - "Exception".length());
        }
        for (var uppercaseLetter : IBundledOpenMarkovException.UPPERCASE_LETTERS) {
            simpleName = simpleName.replace(uppercaseLetter, " " + uppercaseLetter.toLowerCase());
        }
        while (simpleName.startsWith(" ")) {
            simpleName = simpleName.substring(1);
        }
        if (!simpleName.isBlank()) {
            simpleName = simpleName.substring(0, 1).toUpperCase() + simpleName.substring(1);
        }
        return simpleName.replace("\\n", System.lineSeparator());
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
