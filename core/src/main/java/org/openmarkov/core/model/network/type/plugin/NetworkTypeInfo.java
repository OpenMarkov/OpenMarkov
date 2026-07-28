/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.core.model.network.type.plugin;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface NetworkTypeInfo {
	String name();
	
	String visualName();
	/**
	 * Alternative serialization names, for files written before a type was
	 * renamed. The default is an empty array — "no alternative names". It used
	 * to be the literal {@code ""}, which as an array default means
	 * <i>one</i> alternative name, the empty string: every type without
	 * alternatives answered to {@code ""}, and looking a type up by the empty
	 * string returned whichever type came first instead of null.
	 */
	String[] alternativeNames() default {};
}