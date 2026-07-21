package org.openmarkov.core.developmentStaticAnalysis.mutability;

import java.lang.reflect.Field;

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