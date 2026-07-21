/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.model.network.modelUncertainty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@link ProbDensFunction} subclass as a registrable type for the
 * {@link ProbDensFunctionManager}. The annotation declares the textual name
 * used in serialisation, the optional univariate name shared by alternative
 * parametrisations, the kinds of variables the function applies to and the
 * names of its numeric parameters.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface ProbDensFunctionType {
	/** Unique identifier of this function type (used in network files). */
	String name();

	/**
	 * Common univariate identifier shared by several alternative
	 * parametrisations of the same family (for instance several
	 * &quot;Normal&quot; flavours). Defaults to {@code "default"}.
	 */
	String univariateName() default "default";

	/** Whether this function is admissible for probability values. */
	boolean isValidForProbabilities() default true;

	/** Whether this function is admissible for numeric values. */
	boolean isValidForNumeric() default true;

	/** Names of the parameters in their canonical order. */
	String[] parameters();
}
