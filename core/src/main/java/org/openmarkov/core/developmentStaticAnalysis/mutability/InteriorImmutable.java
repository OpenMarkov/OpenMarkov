package org.openmarkov.core.developmentStaticAnalysis.mutability;

/**
 * An Exterior Immutable object is an object whose fields internal state cannot be changed after the object is created.
 * <p>
 * See more in {@link org.openmarkov.core.developmentStaticAnalysis.mutability}
 */
public interface InteriorImmutable {
    
    default boolean isInteriorImmutable() {
        return MutabilityKind.INTERIOR.mutabilityOf(this.getClass()).isImmutable();
    }
    
}
