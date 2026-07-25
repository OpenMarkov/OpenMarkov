package org.openmarkov.core.developmentStaticAnalysis.mutability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Takes this field as impossible to reassign, so that the <strong>exterior</strong> immutability check lets it through
 * even though the field is not {@code final}.
 * <p>
 * For the contents of the field, rather than its reference, use {@link ConsiderFieldAsInteriorImmutable}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface ConsiderFieldAsExteriorImmutable {
}
