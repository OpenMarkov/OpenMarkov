package org.openmarkov.core.developmentStaticAnalysis.mutability;

/**
 * An Interior Immutable object is an object whose fields' internal state cannot be changed after the object is
 * created: not only can its fields not be reassigned, what they hold cannot change either.
 * <p>
 * See more in {@link org.openmarkov.core.developmentStaticAnalysis.mutability}
 */
public interface InteriorImmutable {
    
    default boolean isInteriorImmutable() {
        return MutabilityKind.INTERIOR.mutabilityOf(this.getClass()).isImmutable();
    }
    
}
