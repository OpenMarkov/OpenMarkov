package org.openmarkov.core.model.network.modelUncertainty.ParametrizedFunction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation class with parametrization data for a parametrized ProbDensFuncion
 *
 * @author cmyago
 * @version 1.0 20/10/2020
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ParametrizedFunctionType {
    /**
     * @return name of the parametrized ProbDensFunction
     */
    String distributionName();

    /**
     * @return name of the parametrization for a ProbDensFunction
     */
    String parametrizationName();

    /**
     * @return list with the names of the parameters
     */
    String[] parameters();

    /**
     * Currently (20/10/2020) only distributions used in TTE computing are parametrized but it may be neccesary to parametrize other distributions in the future
     *
     * @return true if the parametrization is valid for TTE.
     */
    boolean isValidForTTE() default true;
}
