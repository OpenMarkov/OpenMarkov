package org.openmarkov.core.localize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a type whose automatic localization the static analysis should take as valid.
 * <p>
 * It is read by the checks of {@code integrationTests}, and it only makes sense on a type: it used
 * to declare no {@code @Target} at all, so it could be written on a field, a parameter or a local
 * variable, where nothing would ever read it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConsiderAutoLocalizationIsValid {
}
