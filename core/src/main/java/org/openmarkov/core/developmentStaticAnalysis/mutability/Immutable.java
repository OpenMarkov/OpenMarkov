package org.openmarkov.core.developmentStaticAnalysis.mutability;

/**
 * An Immutability object is one whose state cannot be changed by any means.
 * <p>
 * See more in {@link org.openmarkov.core.developmentStaticAnalysis.mutability}
 */
public interface Immutable extends InteriorImmutable, ExteriorImmutable {
    
    default boolean isImmutable() {
        return this.isExteriorImmutable() && this.isInteriorImmutable();
    }
    
}
