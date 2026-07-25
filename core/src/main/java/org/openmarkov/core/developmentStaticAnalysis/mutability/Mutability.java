package org.openmarkov.core.developmentStaticAnalysis.mutability;

import java.lang.reflect.Field;

/**
 * The verdict on a class: which of its fields stop it from being immutable, of the kind being checked.
 *
 * @param nonFinalFields the fields that stop it, or {@code null} when nothing does — which is what
 *                       {@link #isImmutable()} reads. The name is the historical one; for the interior check these
 *                       are not "non final" fields but fields whose contents can change.
 */
public record Mutability(Field[] nonFinalFields) {
    public boolean isImmutable() {
        return this.nonFinalFields == null;
    }
    
    public boolean isMutable() {
        return !this.isImmutable();
    }
    
    public static Mutability immutable() {
        return new Mutability(null);
    }
}