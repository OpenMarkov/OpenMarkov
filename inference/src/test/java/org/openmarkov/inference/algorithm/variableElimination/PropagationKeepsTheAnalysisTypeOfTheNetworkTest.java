/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.inference.algorithm.variableElimination;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.inference.MulticriteriaOptions;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TablePotential;
import org.openmarkov.core.model.network.type.BayesianNetworkType;
import org.openmarkov.core.testTags.TestSpeed;
import org.openmarkov.inference.algorithm.variableElimination.tasks.VEPropagation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Building a propagation sets the analysis type of the copy it works on, not of the network it receives.
 *
 * @author Manuel Arias
 */
public class PropagationKeepsTheAnalysisTypeOfTheNetworkTest {

    @Tag(TestSpeed.FAST)
    @Test public void buildingAPropagationLeavesTheAnalysisTypeOfTheNetworkAlone() throws Exception {
        ProbNet net = new ProbNet(BayesianNetworkType.getUniqueInstance());
        Variable y = new Variable("Y", 2);
        net.addNode(y, NodeType.CHANCE);
        net.getNode(y).setPotential(new TablePotential(List.of(y), PotentialRole.CONDITIONAL_PROBABILITY,
                new double[] { 0.4, 0.6 }));
        MulticriteriaOptions options = net.getInferenceOptions().getMultiCriteriaOptions();
        options.setMulticriteriaType(MulticriteriaOptions.Type.COST_EFFECTIVENESS);

        new VEPropagation(net);

        assertEquals(MulticriteriaOptions.Type.COST_EFFECTIVENESS, options.getMulticriteriaType());
    }
}
