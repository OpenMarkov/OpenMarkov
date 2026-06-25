/*
 * Copyright (c) CISIAD, UNED, Spain,  2019. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.learning.gui;

import org.openmarkov.learning.core.algorithm.LearningAlgorithm;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an {@link AlgorithmParametersDialog} subclass as the configuration dialog for a
 * specific learning algorithm. Used by {@link AlgorithmConfigurationManager} for discovery.
 */
@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE) public @interface AlgorithmConfiguration {
	Class<? extends LearningAlgorithm> algorithm();
}
