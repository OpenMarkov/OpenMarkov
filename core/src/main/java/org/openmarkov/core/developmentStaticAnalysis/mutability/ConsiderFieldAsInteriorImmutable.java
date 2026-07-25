package org.openmarkov.core.developmentStaticAnalysis.mutability;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Takes the contents of this field as unable to change, so that the <strong>interior</strong> immutability check
 * lets it through even if its type looks mutable.
 * <p>
 * For a field whose <em>reference</em> is the point — the exterior check — use
 * {@link ConsiderFieldAsExteriorImmutable}. Each annotation waives the check its own name refers to; they used to be
 * wired to the opposite one, and both carried this same text, written as if both were about exterior immutability.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(java.lang.annotation.ElementType.FIELD)
public @interface ConsiderFieldAsInteriorImmutable {
}
