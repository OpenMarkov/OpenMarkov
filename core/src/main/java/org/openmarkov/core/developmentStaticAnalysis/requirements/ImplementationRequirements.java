package org.openmarkov.core.developmentStaticAnalysis.requirements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A class annotated with {@link ImplementationRequirements} must meet some boundaries specified in the annotation.
 * The same boundaries should be met by child classes of the annotated class.
 * <p>
 * If the target annotated with {@link ImplementationRequirements} is not a class, but an annotation, then the
 * requirements will apply to the classes annotated with said target and its child classes.
 * <p>
 * Checks for the requirements don't happen at runtime.
 * Instead, they are checked in a test from {@code org.openmarkov.integrationTests}.
 *
 * @author jrico
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ImplementationRequirements {
    
    /**
     * The class should extend at least one of the annotated classes.
     * <p>
     * The default value is empty, and it means it is not required to extend any specific class.
     * <p>
     * This is mostly used with interfaces or annotations.
     */
    Class[] hasToExtendOneOfTheseClasses() default {};
    
    /**
     * The class should have at least one constructor matching the given parameters.
     * <p>
     * This is only checks against concrete class. Interfaces and abstract classes are ignored as they are not
     * instantiable.
     */
    RequiredConstructor[] requiresOneOfTheseConstructors() default {};
    
    //TODO: Add a 'must implement methods' annotation to indicate methods that it should implement.
    
    /**
     * The class should have the following methods.
     * <p>
     * This is only checks against concrete class. Interfaces and abstract classes are ignored as they are not
     * instantiable.
     * <p>
     * This is especially useful when used over annotations rather than interfaces or abstract classes, as they can do
     * that by themselves.
     */
    RequiredMethod[] requiresMethods() default {};
    
}
