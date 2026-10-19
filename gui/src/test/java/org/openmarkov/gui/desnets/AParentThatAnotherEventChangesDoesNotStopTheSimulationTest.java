/*
 * Copyright (c) CISIAD, UNED, Spain, 2026. Licensed under the GPLv3 licence
 * Unless required by applicable law or agreed to in writing,
 * this code is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OF ANY KIND.
 */
package org.openmarkov.gui.desnets;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.openmarkov.core.model.network.Criterion;
import org.openmarkov.core.model.network.EqualCriterion;
import org.openmarkov.core.model.network.Node;
import org.openmarkov.core.model.network.NodeType;
import org.openmarkov.core.model.network.ProbNet;
import org.openmarkov.core.model.network.Variable;
import org.openmarkov.core.model.network.VariableType;
import org.openmarkov.core.model.network.potential.ExactDistrPotential;
import org.openmarkov.core.model.network.potential.IndicatorPotential;
import org.openmarkov.core.model.network.potential.PotentialRole;
import org.openmarkov.core.model.network.potential.TransitionTablePotential;
import org.openmarkov.core.model.network.type.DESNetworkType;
import org.openmarkov.core.testTags.TestSpeed;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A network of events, simulated for 10 years. «Onset», at year 3, makes «Ill» pass to «yes»; «Accident», at year 20,
 * would make «Injured» pass to «yes»; and «Injured» is a parent of «Ill». When «Onset» happens, «Ill» used to wait for
 * its parent «Injured», which no event had computed, since «Accident» comes after the horizon, and the simulation
 * failed with an index out of bounds (error 96). The utility «QALY» is 1.0 if «Ill» is «no» and 0.6 if it is «yes»,
 * and it is cumulative.
 *
 * @author Manuel Arias
 */
@Tag(TestSpeed.FAST)
class AParentThatAnotherEventChangesDoesNotStopTheSimulationTest {

    private static Variable event(String name) {
        Variable variable = new Variable(name, "no", "yes");
        variable.setVariableType(VariableType.EVENT);
        return variable;
    }

    private static Variable utility(String name, Criterion criterion) {
        Variable variable = new Variable(name);
        variable.setDecisionCriterion(criterion);
        return variable;
    }

    @Test void theNodeIsComputedWithTheValueItsParentHasAndTheSimulationGoesOn() throws Exception {
        ProbNet probNet = new ProbNet(DESNetworkType.getUniqueInstance());
        Criterion cost = new Criterion("Cost");
        Criterion effectiveness = new Criterion("Effectiveness");
        probNet.setDecisionCriteria(List.of(cost, effectiveness));
        probNet.getInferenceOptions().getTemporalOptions().setHorizon(10);

        Variable onset = event("Onset");
        Variable accident = event("Accident");
        Variable ill = new Variable("Ill", "no", "yes");
        Variable injured = new Variable("Injured", "no", "yes");
        Variable qaly = utility("QALY", effectiveness);
        Variable monitoring = utility("Monitoring", cost);
        Node onsetNode = probNet.addNode(onset, NodeType.EVENT);
        Node accidentNode = probNet.addNode(accident, NodeType.EVENT);
        Node illNode = probNet.addNode(ill, NodeType.CHANCE);
        Node injuredNode = probNet.addNode(injured, NodeType.CHANCE);
        Node qalyNode = probNet.addNode(qaly, NodeType.UTILITY);
        Node monitoringNode = probNet.addNode(monitoring, NodeType.UTILITY);
        probNet.addLink(onsetNode, illNode, true);
        probNet.addLink(accidentNode, injuredNode, true);
        probNet.addLink(injuredNode, illNode, true);
        probNet.addLink(illNode, qalyNode, true);
        onsetNode.setPotential(new IndicatorPotential(List.of(onset), PotentialRole.CONDITIONAL_PROBABILITY, 3, 1));
        accidentNode.setPotential(new IndicatorPotential(List.of(accident), PotentialRole.CONDITIONAL_PROBABILITY, 20, 1));
        // When its event happens, each node passes to «yes», whatever its parents
        TransitionTablePotential illTransition = new TransitionTablePotential(new ArrayList<>(List.of(ill, onset, injured)),
                                                                              PotentialRole.CONDITIONAL_PROBABILITY);
        illTransition.setValues(new double[] { 0, 1, 0, 1 });
        illNode.setPotential(illTransition);
        TransitionTablePotential injuredTransition = new TransitionTablePotential(new ArrayList<>(List.of(injured, accident)),
                                                                                  PotentialRole.CONDITIONAL_PROBABILITY);
        injuredTransition.setValues(new double[] { 0, 1 });
        injuredNode.setPotential(injuredTransition);
        qalyNode.setPotential(new ExactDistrPotential(List.of(qaly, ill), PotentialRole.UNSPECIFIED, new double[] { 1.0, 0.6 }));
        monitoringNode.setPotential(new ExactDistrPotential(List.of(monitoring), PotentialRole.UNSPECIFIED, new double[] { 10 }));

        DESInference inference = new DESInference(probNet);
        CriteriaValues results = inference.simulateOneIndividual(inference.decisionVariable.getStates()[0]);

        assertEquals(3 * 1.0 + 7 * 0.6, results.getValue(new EqualCriterion(effectiveness)), 1e-9);
    }
}
