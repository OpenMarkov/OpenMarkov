package org.openmarkov.core.developmentStaticAnalysis.mutability;

/**
 * An Exterior Immutable object is an object whose fields cannot be changed after it is created.
 * <p>
 * See more in {@link org.openmarkov.core.developmentStaticAnalysis.mutability}
 */
public interface ExteriorImmutable {
    
    default boolean isExteriorImmutable() {
        return MutabilityKind.EXTERIOR.mutabilityOf(this.getClass()).isImmutable();
    }
    
}
