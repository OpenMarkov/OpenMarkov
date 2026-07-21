package org.openmarkov.core.developmentStaticAnalysis.mutability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bypasses the mechanism to detect if a field is exterior immutable and considers it as exterior immutable even when
 * not meeting exterior immutability requirements.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface ConsiderFieldAsExteriorImmutable {
}
