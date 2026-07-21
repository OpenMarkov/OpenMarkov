/*
 * Copyright (c) CISIAD, UNED, Spain,  2018. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */

package org.openmarkov.core.inference;

import org.openmarkov.core.inference.tasks.OptimalIntervention;
import org.openmarkov.core.model.network.EvidenceCase;
import org.openmarkov.core.model.network.ProbNet;

/**
 * @author manolo
 * Tests class for models that contain decisions. Different subclasses share that they have to test the MEU and the strategy
 */
public abstract class OptimalStrategyTaskDecTest extends InferenceTaskTest {
    public abstract OptimalIntervention buildInferenceTask(ProbNet probNet, EvidenceCase preResolutionEvidence)
            ;
}
