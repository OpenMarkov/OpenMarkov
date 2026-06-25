/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.inference.algorithm.adaptiveImportanceSampling;

import org.openmarkov.core.exception.ConstraintViolatedException;
import org.openmarkov.core.exception.NotEvaluableNetworkException;
import org.openmarkov.core.inference.InferenceAlgorithm;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.inference.algorithm.adaptiveImportanceSampling.AdaptiveImportanceSampling;

public class AdaptiveImportanceSamplingTests {
    public InferenceAlgorithm buildInferenceAlgorithm(ProbNet probNet) throws NotEvaluableNetworkException.NotApplicableNetwork, ConstraintViolatedException {
		return new AdaptiveImportanceSampling(probNet);
	}
}
